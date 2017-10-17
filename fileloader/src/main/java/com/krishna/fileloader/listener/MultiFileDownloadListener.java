package com.krishna.fileloader.listener;

/**
 * Created by krishna on 17/10/17.
 */

public interface MultiFileDownloadListener {
    void onProgress(int progress, int totalFiles);
}
