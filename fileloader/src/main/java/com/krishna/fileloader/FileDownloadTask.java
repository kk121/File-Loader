package com.krishna.fileloader;

import android.content.Context;
import android.os.AsyncTask;

import com.krishna.fileloader.listener.MultiFileDownloadListener;
import com.krishna.fileloader.network.FileDownloader;

/**
 * Created by krishna on 17/10/17.
 */

public class FileDownloadTask extends AsyncTask<String, Integer, Void> {
    private MultiFileDownloadListener listener;
    private String dirName;
    private int dirType;
    private Context context;
    private int totalTasks = 0;
    private int progress = 0;

    public FileDownloadTask(Context context, String dirName, int dirType, MultiFileDownloadListener listener) {
        this.dirName = dirName;
        this.dirType = dirType;
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(String... urls) {
        totalTasks = urls.length;
        for (String url : urls) {
            try {
                FileDownloader downloader = new FileDownloader(context, url, dirName, dirType);
                downloader.download();
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
            listener.onProgress(values[0], totalTasks);
        }
    }
}
