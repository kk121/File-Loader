package com.krishna.fileloader.request;

import java.util.ArrayList;

/**
 * Created by krishna on 15/10/17.
 */

public class FileDeleteRequest {
    private ArrayList<String> fileUriList;
    private String directoryName;
    private int directoryType;
    private String fileNamePrefix = "";

    public FileDeleteRequest(ArrayList<String> fileUriList, String directoryName, int directoryType) {
        this.fileUriList = fileUriList;
        this.directoryName = directoryName;
        this.directoryType = directoryType;
    }

    public ArrayList<String> getFileUriList() {
        return fileUriList;
    }

    public void setFileUriList(ArrayList<String> fileUriList) {
        this.fileUriList = fileUriList;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public int getDirectoryType() {
        return directoryType;
    }

    public void setDirectoryType(int directoryType) {
        this.directoryType = directoryType;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }
}
