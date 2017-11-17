package com.krishna.fileloader.builder;

import android.content.Context;

import com.krishna.fileloader.FileLoader;
import com.krishna.fileloader.MultiFileDownloadTask;
import com.krishna.fileloader.listener.MultiFileDownloadListener;
import com.krishna.fileloader.utility.Utils;

/**
 * Created by krishna on 15/10/17.
 */

public class MultiFileDownloader {
    private Context context;
    private String directoryName = FileLoader.DEFAULT_DIR_NAME;
    private int directoryType = FileLoader.DEFAULT_DIR_TYPE;

    private MultiFileDownloadListener listener;
    private boolean forceLoadFromNetwork;

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
        new MultiFileDownloadTask(context, directoryName, directoryType, listener, forceLoadFromNetwork).executeOnExecutor(Utils.getThreadPoolExecutor(), uris);
    }

    public void loadMultiple(boolean forceLoadFromNetwork, String... uris) {
        this.forceLoadFromNetwork = forceLoadFromNetwork;
        loadMultiple(uris);
    }
}
