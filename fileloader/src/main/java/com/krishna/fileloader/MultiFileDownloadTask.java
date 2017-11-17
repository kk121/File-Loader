package com.krishna.fileloader;

import android.content.Context;
import android.os.AsyncTask;

import com.krishna.fileloader.listener.MultiFileDownloadListener;
import com.krishna.fileloader.network.FileDownloader;
import com.krishna.fileloader.utility.AndroidFileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by krishna on 17/10/17.
 */

public class MultiFileDownloadTask extends AsyncTask<String, Integer, Void> {
    private MultiFileDownloadListener listener;
    private String dirName;
    private int dirType;
    private Context context;
    private int totalTasks = 0;
    private int progress = 0;
    private List<File> downloadedFiles;
    private boolean forceLoadFromNetwork;

    public MultiFileDownloadTask(Context context, String dirName, int dirType, MultiFileDownloadListener listener, boolean forceLoadFromNetwork) {
        this.dirName = dirName;
        this.dirType = dirType;
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.forceLoadFromNetwork = forceLoadFromNetwork;
        downloadedFiles = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(String... urls) {
        totalTasks = urls.length;
        for (String url : urls) {
            try {
                File downloadedFile = null;
                if (!forceLoadFromNetwork) {
                    //search file locally
                    downloadedFile = AndroidFileManager.searchAndGetLocalFile(context, url, dirName, dirType);
                }
                if (downloadedFile == null || !downloadedFile.exists()) {
                    FileDownloader downloader = new FileDownloader(context, url, dirName, dirType);
                    downloadedFile = downloader.download();
                }
                downloadedFiles.add(downloadedFile);
                publishProgress(++progress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (listener != null) {
            try {
                listener.onProgress(downloadedFiles.get(values[0] - 1), values[0], totalTasks);
            } catch (Exception e) {
                try {
                    listener.onError(e, values[0]);
                } catch (Exception e1) {
                    //ignore
                }
            }
        }
    }
}
