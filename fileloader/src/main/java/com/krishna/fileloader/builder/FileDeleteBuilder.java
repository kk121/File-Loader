package com.krishna.fileloader.builder;

import android.content.Context;
import android.support.annotation.NonNull;

import com.krishna.fileloader.request.FileDeleteRequest;
import com.krishna.fileloader.FileLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by krishna on 15/10/17.
 */

public class FileDeleteBuilder {
    private ArrayList<String> fileUriList;
    private String directoryName = FileLoader.DEFAULT_DIR_NAME;
    private int directoryType = FileLoader.DEFAULT_DIR_TYPE;

    private Context context;
    private FileLoader fileLoader;

    public FileDeleteBuilder(Context context) {
        this.context = context.getApplicationContext();
    }

    public FileDeleteBuilder fromDirectory(String directoryName, @FileLoader.DirectoryType int directoryType) {
        this.directoryName = directoryName;
        this.directoryType = directoryType;
        return this;
    }

    public int deleteFiles(String... fileUris) throws Exception {
        fileUriList = new ArrayList<>();
        fileUriList.addAll(Arrays.asList(fileUris));
        fileLoader = buildFileLoader();
        return fileLoader.deleteFiles();
    }

    @NonNull
    private FileLoader buildFileLoader() {
        FileDeleteRequest deleteRequest = new FileDeleteRequest(fileUriList, directoryName, directoryType);
        FileLoader fileLoader = new FileLoader(context);
        fileLoader.setFileDeleteRequest(deleteRequest);
        return fileLoader;
    }

    public int deleteFiles(ArrayList<String> fileUriList) throws Exception {
        return deleteFiles((String[]) fileUriList.toArray());
    }

    public int deleteAllFiles() throws Exception {
        fileLoader = buildFileLoader();
        return fileLoader.deleteAllFiles();
    }

    public int deleteAllFilesExcept(String... fileUris) throws Exception {
        fileUriList = new ArrayList<>();
        for (String fileUri : fileUris) {
            fileUriList.add(fileUri);
        }
        fileLoader = buildFileLoader();
        return fileLoader.deleteAllFilesExcept();
    }

    public int deleteAllFilesExcept(List<String> fileUriList) throws Exception {
        return deleteAllFilesExcept((String[]) fileUriList.toArray());
    }
}
