package com.krishna.fileloader.request;

import android.support.annotation.IntDef;

import com.krishna.fileloader.listener.FileRequestListener;
import com.krishna.fileloader.utility.FileExtension;

/**
 * Created by krishna on 12/10/17.
 */

public class FileLoadRequest {
    //Return File type
    public static final int TYPE_FILE = 1;
    public static final int TYPE_BITMAP = 2;
    public static final int TYPE_OBJECT = 3;
    public static final int TYPE_STRING = 4;

    @IntDef({TYPE_FILE, TYPE_BITMAP, TYPE_OBJECT, TYPE_STRING})
    public @interface ReturnFileType {
    }

    private String uri;
    private String directoryName;
    private int directoryType;
    @ReturnFileType
    private int fileType;
    private String fileExtension = FileExtension.NONE;
    private Class requestClass;
    private FileRequestListener requestListener;

    public FileLoadRequest(String uri, String directoryName, int directoryType, int fileType, Class requestClass, String fileExtension, FileRequestListener listener) {
        this.uri = uri;
        this.directoryName = directoryName;
        this.directoryType = directoryType;
        this.fileType = fileType;
        this.requestClass = requestClass;
        this.fileExtension = fileExtension;
        this.requestListener = listener;
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

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public FileRequestListener getRequestListener() {
        return requestListener;
    }

    public void setRequestListener(FileRequestListener requestListener) {
        this.requestListener = requestListener;
    }

    public Class getRequestClass() {
        return requestClass;
    }

    public void setRequestClass(Class requestClass) {
        this.requestClass = requestClass;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileLoadRequest that = (FileLoadRequest) o;

        if (directoryType != that.directoryType) return false;
        if (fileType != that.fileType) return false;
        if (!uri.equals(that.uri)) return false;
        return directoryName.equals(that.directoryName);

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + directoryName.hashCode();
        result = 31 * result + directoryType;
        result = 31 * result + fileType;
        return result;
    }
}
