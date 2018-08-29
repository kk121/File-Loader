package com.krishna.fileloader;

import android.content.Context;
import android.os.AsyncTask;

import com.krishna.fileloader.listener.MultiFileDownloadListener;
import com.krishna.fileloader.network.FileDownloader;
import com.krishna.fileloader.request.MultiFileLoadRequest;
import com.krishna.fileloader.utility.AndroidFileManager;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by krishna on 17/10/17.
 */

public class MultiFileDownloadTask extends AsyncTask<MultiFileLoadRequest, Integer, Void> {
    private MultiFileDownloadListener listener;
    private Context context;
    private int totalTasks = 0;
    private int progress = 0;
    private List<File> downloadedFiles;

    public MultiFileDownloadTask(Context context, MultiFileDownloadListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        downloadedFiles = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(MultiFileLoadRequest... requests) {
        totalTasks = requests.length;
        for (MultiFileLoadRequest loadRequest : requests) {
            try {
                File downloadedFile = AndroidFileManager.searchAndGetLocalFile(context, loadRequest.getUri(), loadRequest.getDirectoryName(), loadRequest.getDirectoryType());
                FileDownloader downloader = new FileDownloader(context, loadRequest.getUri(), loadRequest.getDirectoryName(), loadRequest.getDirectoryType());
                if (loadRequest.isForceLoadFromNetwork() || downloadedFile == null || !downloadedFile.exists()) {
                    downloadedFile = downloader.download(false, loadRequest.isCheckIntegrity());
                } else if (loadRequest.isAutoRefresh()) {
                    File tempFile = downloader.download(true, loadRequest.isCheckIntegrity());
                    if (tempFile != null) {
                        downloadedFile = tempFile;
                    }
                }
                downloadedFiles.add(downloadedFile);
                publishProgress(++progress);
            } catch (InterruptedIOException e) {
                sendErrorToListener(e, ++progress);
            } catch (Exception e) {
                sendErrorToListener(e, ++progress);
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
                sendErrorToListener(e, values[0]);
            }
        }
    }

    private void sendErrorToListener(Exception e, int progress) {
        try {
            listener.onError(e, progress);
        } catch (Exception e1) {
            //ignore
        }
    }
}
