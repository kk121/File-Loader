package com.krishna.fileloader.pojo;

import java.io.File;

/**
 * Created by krishna on 12/10/17.
 */

public class FileResponse<T> {
    private int code;
    private T body;
    private File downloadedFile;
    private long fileSize;

    public FileResponse(int code, T body, long fileSize) {
        this.code = code;
        this.body = body;
        this.fileSize = fileSize;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public File getDownloadedFile() {
        return downloadedFile;
    }

    public void setDownloadedFile(File downloadedFile) {
        this.downloadedFile = downloadedFile;
    }
}
