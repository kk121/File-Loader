package com.krishna.fileloader.builder;

import android.content.Context;

import com.krishna.fileloader.FileLoader;
import com.krishna.fileloader.MultiFileDownloadTask;
import com.krishna.fileloader.listener.MultiFileDownloadListener;
import com.krishna.fileloader.request.MultiFileLoadRequest;
import com.krishna.fileloader.utility.Utils;

import java.util.List;

/**
 * Created by krishna on 15/10/17.
 */

public class MultiFileDownloader {
    private Context context;
    private String directoryName = FileLoader.DEFAULT_DIR_NAME;
    private int directoryType = FileLoader.DEFAULT_DIR_TYPE;

    private MultiFileDownloadListener listener;
    private boolean forceLoadFromNetwork;
    private MultiFileDownloadTask multiFileDownloadTask;

    public MultiFileDownloader(Context context) {
        this.context = context;
    }

    public MultiFileDownloader fromDirectory(String directoryName, @FileLoader.DirectoryType int directoryType) {
        this.directoryName = directoryName;
        this.directoryType = directoryType;
        return this;
    }

    public MultiFileDownloader progressListener(MultiFileDownloadListener listener) {
        this.listener = listener;
        return this;
    }

    public void loadMultiple(String... uris) {
        MultiFileLoadRequest[] loadRequestArr = new MultiFileLoadRequest[uris.length];
        for (int i = 0; i < uris.length; i++) {
            MultiFileLoadRequest loadRequest = new MultiFileLoadRequest(uris[i], directoryName, directoryType, forceLoadFromNetwork);
            loadRequestArr[i] = loadRequest;
        }
        multiFileDownloadTask = new MultiFileDownloadTask(context, listener);
        multiFileDownloadTask.executeOnExecutor(Utils.getThreadPoolExecutor(), loadRequestArr);
    }

    public void loadMultiple(boolean forceLoadFromNetwork, String... uris) {
        this.forceLoadFromNetwork = forceLoadFromNetwork;
        loadMultiple(uris);
    }

    public void loadMultiple(boolean forceLoadFromNetwork, List<MultiFileLoadRequest> multiFileLoadRequestList) {
        this.forceLoadFromNetwork = forceLoadFromNetwork;
        MultiFileLoadRequest[] loadRequestArr = new MultiFileLoadRequest[multiFileLoadRequestList.size()];
        for (int i = 0; i < multiFileLoadRequestList.size(); i++) {
            loadRequestArr[i] = multiFileLoadRequestList.get(i);
        }
        multiFileDownloadTask = new MultiFileDownloadTask(context, listener);
        multiFileDownloadTask.executeOnExecutor(Utils.getThreadPoolExecutor(), loadRequestArr);
    }

    public void loadMultiple(List<MultiFileLoadRequest> multiFileLoadRequestList) {
        loadMultiple(false, multiFileLoadRequestList);
    }

    public void cancelLoad() {
        if (multiFileDownloadTask != null)
            multiFileDownloadTask.cancel(true);
    }
}
