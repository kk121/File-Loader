package com.krishna.fileloader.builder;

import android.content.Context;
import android.graphics.Bitmap;

import com.krishna.fileloader.FileLoader;
import com.krishna.fileloader.listener.FileRequestListener;
import com.krishna.fileloader.pojo.FileResponse;
import com.krishna.fileloader.request.FileLoadRequest;
import com.krishna.fileloader.utility.FileExtension;

import java.io.File;

/**
 * Created by krishna on 15/10/17.
 */

public class FileLoaderBuilder {
    private Context context;
    private String uri;
    private String directoryName = FileLoader.DEFAULT_DIR_NAME;
    private int directoryType = FileLoader.DEFAULT_DIR_TYPE;
    private String fileExtension = FileExtension.UNKNOWN;

    private FileRequestListener listener;
    @FileLoadRequest.ReturnFileType
    private int returnFileType;
    private Class requestClass;
    private FileLoader fileLoader;
    private boolean forceLoadFromNetwork;
    private boolean autoRefresh;
    private boolean checkIntegrity;
    private String fileNamePrefix = "";


    public FileLoaderBuilder(Context context) {
        this.context = context;
    }

    public FileLoaderBuilder(Context context, boolean autoRefresh) {
        this.context = context;
        this.autoRefresh = autoRefresh;
    }

    public FileLoaderBuilder load(String uri) {
        this.uri = uri;
        return this;
    }

    public FileLoaderBuilder load(String uri, boolean forceLoadFromNetwork) {
        this.forceLoadFromNetwork = forceLoadFromNetwork;
        return load(uri);
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public FileLoaderBuilder fromDirectory(String directoryName, @FileLoader.DirectoryType int directoryType) {
        this.directoryName = directoryName;
        this.directoryType = directoryType;
        return this;
    }

    public FileLoaderBuilder checkFileintegrity(boolean checkIntegrity) {
        this.checkIntegrity = checkIntegrity;
        return this;
    }

    public FileResponse asFile() throws Exception {
        returnFileType = FileLoadRequest.TYPE_FILE;
        buildFileLoader();
        return fileLoader.loadFile();
    }

    public void asFile(FileRequestListener<File> listener) {
        returnFileType = FileLoadRequest.TYPE_FILE;
        this.listener = listener;
        buildFileLoader();
        fileLoader.loadFileAsync();
    }

    public FileResponse asBitmap() throws Exception {
        returnFileType = FileLoadRequest.TYPE_BITMAP;
        buildFileLoader();
        return fileLoader.loadFile();
    }

    public void asBitmap(FileRequestListener<Bitmap> listener) {
        returnFileType = FileLoadRequest.TYPE_BITMAP;
        this.listener = listener;
        buildFileLoader();
        fileLoader.loadFileAsync();
    }

    public FileResponse asString() throws Exception {
        returnFileType = FileLoadRequest.TYPE_STRING;
        buildFileLoader();
        return fileLoader.loadFile();
    }

    public void asString(FileRequestListener<String> listener) {
        returnFileType = FileLoadRequest.TYPE_STRING;
        this.listener = listener;
        buildFileLoader();
        fileLoader.loadFileAsync();
    }

    public FileResponse asObject(Class clazz) throws Exception {
        returnFileType = FileLoadRequest.TYPE_OBJECT;
        requestClass = clazz;
        buildFileLoader();
        return fileLoader.loadFile();
    }

    public void asObject(FileRequestListener<? extends Object> listener) {
        returnFileType = FileLoadRequest.TYPE_OBJECT;
        this.listener = listener;
        buildFileLoader();
        fileLoader.loadFileAsync();
    }

    private void buildFileLoader() {
        fileLoader = new FileLoader(context);
        fileLoader.setFileLoadRequest(new FileLoadRequest(uri, directoryName, directoryType, returnFileType, requestClass, fileExtension, forceLoadFromNetwork, autoRefresh, checkIntegrity, listener, fileNamePrefix));
    }
}
