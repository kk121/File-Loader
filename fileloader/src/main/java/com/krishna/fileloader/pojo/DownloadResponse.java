package com.krishna.fileloader.pojo;

import java.io.File;

/**
 * Created by krishna on 13/10/17.
 */

public class DownloadResponse {
    private File downloadedFile;
    private Exception e;

    public File getDownloadedFile() {
        return downloadedFile;
    }

    public void setDownloadedFile(File downloadedFile) {
        this.downloadedFile = downloadedFile;
    }

    public Exception getE() {
        return e;
    }

    public void setE(Exception e) {
        this.e = e;
    }
}
