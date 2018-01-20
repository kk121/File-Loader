package com.krishna.fileloader.request;

/**
 * Created by krishna on 12/10/17.
 */

public class MultiFileLoadRequest {
    private String uri;
    private String directoryName;
    private int directoryType;
    private boolean forceLoadFromNetwork;

    /**
     *
     * @param uri Url of the image to download (ex. https://images.pexels.com/photos/45170/kittens-cat-cat-puppy-rush-45170.jpeg)
     * @param directoryName Name of the directory where you want to download (like. Environment.DIRECTORY_PICTURES , Environment.DIRECTORY_DOCUMENTS, Environment.DIRECTORY_DOWNLOADS or custom directory "My Directory"
     * @param directoryType Type of directory {@link com.krishna.fileloader.FileLoader.DirectoryType}
     * @param forceLoadFromNetwork if true, it will ignore the local file if exists and re-download else returns the existing file
     */
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
