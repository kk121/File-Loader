package com.krishna.fileloader.network;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.krishna.fileloader.BuildConfig;
import com.krishna.fileloader.utility.AndroidFileManager;
import com.krishna.fileloader.utility.MD5;
import com.krishna.fileloader.utility.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by krishna on 12/10/17.
 */

public class FileDownloader {
    private static final String TAG = "FileDownloader";
    private String uri;
    private String dirName;
    private int dirType;
    private static OkHttpClient httpClient;
    private Context context;

    public FileDownloader(Context context, String uri, String dirName, int dirType) {
        this.context = context.getApplicationContext();
        this.uri = uri;
        this.dirName = dirName;
        this.dirType = dirType;
        initHttpClient();
    }

    private void initHttpClient() {
        if (httpClient == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            if (BuildConfig.DEBUG)
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            else
                interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .build();
        }
    }

    @WorkerThread
    public File download(boolean autoRefresh, boolean checkIntegrity) throws Exception {
        Request.Builder requestBuilder = new Request.Builder().url(uri);

        //if auto-refresh is enabled then add header "If-Modified-Since" to the request and send last modified time of local file
        File downloadFilePath = AndroidFileManager.getFileForRequest(context, uri, dirName, dirType);
        if (autoRefresh) {
            String lastModifiedTime = Utils.getLastModifiedTime(downloadFilePath.lastModified());
            if (lastModifiedTime != null) {
                requestBuilder.addHeader("If-Modified-Since", lastModifiedTime);
            }
        }

        Request request = requestBuilder.build();
        Response response = httpClient.newCall(request).execute();

        //if file on server is not modified, return null.
        if (autoRefresh && response.code() == 304) {
            return null;
        }

        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Failed to download file: " + response);
        }

        //if file already exists, delete it
        if (downloadFilePath.exists()) {
            if (downloadFilePath.delete())
                downloadFilePath = AndroidFileManager.getFileForRequest(context, uri, dirName, dirType);
        }

        //write the body to file
        BufferedSink sink = Okio.buffer(Okio.sink(downloadFilePath));
        long readBytes;
        try {
            readBytes = sink.writeAll(response.body().source());
            sink.close();
        } catch (IOException e) {
            if (downloadFilePath.exists())
                downloadFilePath.delete();
            throw new IOException("Failed to download file: " + response);
        }

        //check if downloaded file is not corrupt
        int contentLength = 0;
        try {
            contentLength = Integer.parseInt(response.header("Content-Length", ""));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (readBytes < contentLength) {
            //delete the corrupt file
            downloadFilePath.delete();
            Log.i(TAG, "downloaded file is corrupt: " + request.url());
            throw new IOException("Failed to download file: " + response);
        }

        if (checkIntegrity) {
            String eTag = response.header("ETag", "").replaceAll("\"", "").trim();
            if (!TextUtils.isEmpty(eTag)) {
                if (!MD5.checkMD5(eTag, downloadFilePath)) {
                    //delete the corrupt file
                    downloadFilePath.delete();
                    Log.i(TAG, "checksum did not match: " + request.url());
                    throw new IOException("Failed to download file: " + response);
                }
            }
        }

        //set server Last-Modified time to file. send this time to server on next request.
        long timeStamp = Utils.parseLastModifiedHeader(response.header("Last-Modified"));
        if (timeStamp > 0) {
            downloadFilePath.setLastModified(timeStamp);
        }

        return downloadFilePath;
    }
}
