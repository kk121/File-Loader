package com.krishna.fileloader.network;

import android.content.Context;
import android.support.annotation.WorkerThread;

import com.krishna.fileloader.request.FileLoadRequest;
import com.krishna.fileloader.utility.AndroidFileManager;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by krishna on 12/10/17.
 */

public class FileDownloader {
    private FileLoadRequest fileLoadRequest;
    private OkHttpClient httpClient;
    private Context context;

    public FileDownloader(Context context, FileLoadRequest fileLoadRequest) {
        this.fileLoadRequest = fileLoadRequest;
        this.httpClient = new OkHttpClient();
        this.context = context.getApplicationContext();
    }

    @WorkerThread
    public File download() throws Exception {
        Request request = new Request.Builder().url(fileLoadRequest.getUri()).build();
        Response response = httpClient.newCall(request).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Failed to download file: " + response);
        }
        File downloadedFile = AndroidFileManager.getFileForRequest(context, fileLoadRequest.getUri(), fileLoadRequest.getDirectoryName(), fileLoadRequest.getDirectoryType());
        BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
        sink.writeAll(response.body().source());
        sink.close();
        return downloadedFile;
    }
}
