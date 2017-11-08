package com.krishna.fileloader.listener;

import java.io.File;

/**
 * Created by krishna on 17/10/17.
 */

public interface MultiFileDownloadListener {
    void onProgress(File downloadedFile, int progress, int totalFiles);
}
