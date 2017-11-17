package com.krishna.fileloader.request;

/**
 * Created by krishna on 12/10/17.
 */

public class MultiFileLoadRequest {
    private String uri;
    private String directoryName;
    private int directoryType;
    private boolean forceLoadFromNetwork;

    public MultiFileLoadRequest(String uri, String directoryName, int directoryType, boolean forceLoadFromNetwork) {
        this.uri = uri;
        this.directoryName = directoryName;
        this.directoryType = directoryType;
        this.forceLoadFromNetwork = forceLoadFromNetwork;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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

    public boolean isForceLoadFromNetwork() {
        return forceLoadFromNetwork;
    }

    public void setForceLoadFromNetwork(boolean forceLoadFromNetwork) {
        this.forceLoadFromNetwork = forceLoadFromNetwork;
    }
}
