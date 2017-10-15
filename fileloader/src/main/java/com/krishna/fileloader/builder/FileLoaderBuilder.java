package com.krishna.fileloader.builder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.krishna.fileloader.FileLoader;
import com.krishna.fileloader.listener.FileRequestListener;
import com.krishna.fileloader.pojo.FileResponse;
import com.krishna.fileloader.request.FileLoadRequest;
import com.krishna.fileloader.utility.FileExtension;
import com.krishna.fileloader.utility.Utils;

import java.io.File;

/**
 * Created by krishna on 15/10/17.
 */

public class FileLoaderBuilder {
    private Context context;
    private String uri;
    private String directoryName = FileLoader.DEFAULT_DIR_NAME;
    private int directoryType = FileLoader.DEFAULT_DIR_TYPE;
    private String fileExtension = FileExtension.NONE;

    private FileRequestListener listener;
    @FileLoadRequest.ReturnFileType
    private int returnFileType;
    private Class requestClass;
    private FileLoader fileLoader;


    public FileLoaderBuilder(Context context) {
        this.context = context;
    }

    public FileLoaderBuilder load(String uri) {
        this.uri = uri;
        return this;
    }

    public FileLoaderBuilder fromDirectory(String directoryName, @FileLoader.DirectoryType int directoryType) {
        this.directoryName = directoryName;
        this.directoryType = directoryType;
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

    public void into(final ImageView imageView, final Drawable placeholder) {
        returnFileType = FileLoadRequest.TYPE_FILE;
        this.listener = new FileRequestListener<File>() {
            @Override
            public void onLoad(FileLoadRequest request, FileResponse<File> response) {
                //Load returned image file into imageView using Glide
                Glide.with(context).setDefaultRequestOptions(Utils.getGlideDefaultRequestOptions(placeholder)).load(response.getBody()).into(imageView);
            }

            @Override
            public void onError(FileLoadRequest request, Throwable t) {

            }
        };
        //show placeholder
        Glide.with(context).setDefaultRequestOptions(Utils.getGlideDefaultRequestOptions(placeholder)).load("").into(imageView);
        buildFileLoader();
        fileLoader.loadFileAsync();
    }

    public void into(final ImageView imageView) {
        into(imageView, null);
    }

    private void buildFileLoader() {
        fileLoader = new FileLoader(context);
        fileLoader.setFileLoadRequest(new FileLoadRequest(uri, directoryName, directoryType, returnFileType, requestClass, fileExtension, listener));
    }
}
