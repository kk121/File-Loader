package com.krishna.fileloader;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.krishna.fileloader.builder.FileDeleteBuilder;
import com.krishna.fileloader.builder.FileLoaderBuilder;
import com.krishna.fileloader.builder.MultiFileDownloader;
import com.krishna.fileloader.listener.FileRequestListener;
import com.krishna.fileloader.network.FileDownloader;
import com.krishna.fileloader.pojo.DownloadResponse;
import com.krishna.fileloader.pojo.FileResponse;
import com.krishna.fileloader.request.FileDeleteRequest;
import com.krishna.fileloader.request.FileLoadRequest;
import com.krishna.fileloader.utility.AndroidFileManager;
import com.krishna.fileloader.utility.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * Created by krishna on 09/10/17.
 */

public class FileLoader {
    private static final String TAG = "FileLoader";
    // Directory type
    public static final int DIR_INTERNAL = 1;
    public static final int DIR_CACHE = 2;
    public static final int DIR_EXTERNAL_PRIVATE = 3;
    public static final int DIR_EXTERNAL_PUBLIC = 4;

    //Defaults
    public static final int DEFAULT_DIR_TYPE = DIR_INTERNAL;
    public static final String DEFAULT_DIR_NAME = "file_loader";

    @IntDef({DIR_INTERNAL, DIR_CACHE, DIR_EXTERNAL_PRIVATE, DIR_EXTERNAL_PUBLIC})
    public @interface DirectoryType {
    }

    private static Map<FileLoadRequest, Boolean> backingMap = new WeakHashMap<>();
    private static Set<FileLoadRequest> fileLoadRequestSet = Collections.newSetFromMap(backingMap);
    private static Map<FileLoadRequest, List<FileRequestListener>> requestListenersMap = new WeakHashMap<>();
    private static final Object REQUEST_QUEUE_LOCK = new Object();
    private static final Object REQUEST_LISTENER_QUEUE_LOCK = new Object();

    private Context context;
    private FileLoadRequest fileLoadRequest;
    private FileDeleteRequest fileDeleteRequest;

    public FileLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    public static FileLoaderBuilder with(Context context) {
        return new FileLoaderBuilder(context);
    }

    public static FileDeleteBuilder deleteWith(Context context) {
        return new FileDeleteBuilder(context);
    }

    public static MultiFileDownloader multiFileDownload(Context context) {
        return new MultiFileDownloader(context);
    }

    public FileResponse loadFile() throws Exception {
        validateAllParameters();
        DownloadResponse response = getFileLoaderAsyncTask().execute().get();
        if (response.getDownloadedFile() != null) {
            return createFileResponse(response.getDownloadedFile());
        } else {
            throw response.getE();
        }
    }

    public void loadFileAsync() {
        try {
            validateAllParameters();
            if (fileLoadRequest.getRequestListener() == null)
                throw new NullPointerException("File Request listener should not be null");
        } catch (Exception e) {
            callFailureMethodsOfListeners(e);
            return;
        }
        // All parameters are valid, move to next step
        addRequestListenerToQueue();
        if (!fileLoadRequestSet.contains(fileLoadRequest)) {
            synchronized (REQUEST_QUEUE_LOCK) {
                fileLoadRequestSet.add(fileLoadRequest);
            }
            getFileLoaderAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public int deleteFiles() {
        int fileCount = 0;
        for (String fileUri : fileDeleteRequest.getFileUriList()) {
            File file = AndroidFileManager.getFileForRequest(context, fileUri, fileDeleteRequest.getDirectoryName(), fileDeleteRequest.getDirectoryType());
            if (file.exists()) {
                file.delete();
                fileCount++;
            }
        }
        return fileCount;
    }

    public int deleteAllFiles() {
        int fileCount = 0;
        File dir = AndroidFileManager.getAppropriateDirectory(context, fileDeleteRequest.getDirectoryName(), fileDeleteRequest.getDirectoryType());
        File[] allFiles = dir.listFiles();
        for (File file : allFiles) {
            file.delete();
            fileCount++;
        }
        dir.delete();
        return fileCount;
    }

    public int deleteAllFilesExcept() {
        int fileCount = 0;
        File dir = AndroidFileManager.getAppropriateDirectory(context, fileDeleteRequest.getDirectoryName(), fileDeleteRequest.getDirectoryType());
        Set<String> filesToKeepSet = new HashSet<>();
        for (String fileUri : fileDeleteRequest.getFileUriList()) {
            try {
                filesToKeepSet.add(getFileName(fileUri));
            } catch (Exception e) {
                //ignore
            }
        }
        String[] allFiles = dir.list();
        for (String fileName : allFiles) {
            if (!filesToKeepSet.contains(fileName)) {
                File file = new File(dir, fileName);
                file.delete();
                fileCount++;
            }
        }
        return fileCount;
    }

    private void validateAllParameters() throws Exception {
        if (TextUtils.isEmpty(fileLoadRequest.getDirectoryName()))
            throw new NullPointerException("Directory name should not be null or empty");
        if (TextUtils.isEmpty(fileLoadRequest.getUri()))
            throw new NullPointerException("File uri should not be null or empty");
        if (fileLoadRequest.getFileExtension() == null)
            throw new NullPointerException("File extension should not be null");
    }

    private void addRequestListenerToQueue() {
        if (requestListenersMap.containsKey(fileLoadRequest)) {
            synchronized (REQUEST_LISTENER_QUEUE_LOCK) {
                requestListenersMap.get(fileLoadRequest).add(fileLoadRequest.getRequestListener());
            }
        } else {
            List<FileRequestListener> listenersList = new ArrayList<>();
            listenersList.add(fileLoadRequest.getRequestListener());
            requestListenersMap.put(fileLoadRequest, listenersList);
        }
    }

    private void callFailureMethodsOfListeners(Throwable t) {
        if (!requestListenersMap.isEmpty()) {
            synchronized (REQUEST_LISTENER_QUEUE_LOCK) {
                List<FileRequestListener> listenerList = requestListenersMap.get(fileLoadRequest);
                if (listenerList != null) {
                    for (FileRequestListener listener : listenerList) {
                        try {
                            listener.onError(fileLoadRequest, t);
                        } catch (Exception e) {
                            //ignore
                        }
                    }
                    requestListenersMap.remove(fileLoadRequest);
                }
            }
            synchronized (REQUEST_QUEUE_LOCK) {
                fileLoadRequestSet.remove(fileLoadRequest);
            }
        }
    }

    private FileResponse createFileResponse(File downloadedFile) {
        FileResponse response;
        if (fileLoadRequest.getFileType() == FileLoadRequest.TYPE_BITMAP) {
            response = new FileResponse(200, AndroidFileManager.getBitmap(downloadedFile), downloadedFile.length());
        } else if (fileLoadRequest.getFileType() == FileLoadRequest.TYPE_STRING) {
            response = new FileResponse(200, AndroidFileManager.readFileAsString(downloadedFile), downloadedFile.length());
        } else if (fileLoadRequest.getFileType() == FileLoadRequest.TYPE_OBJECT) {
            response = new FileResponse(200, Utils.getObject(downloadedFile, fileLoadRequest.getRequestClass()), downloadedFile.length());
        } else {
            response = new FileResponse(200, downloadedFile, downloadedFile.length());
        }
        response.setDownloadedFile(downloadedFile);
        return response;
    }

    private void sendFileResponseToListeners(File loadedFile) {
        if (!requestListenersMap.isEmpty()) {
            FileResponse fileResponse = createFileResponse(loadedFile);
            synchronized (REQUEST_LISTENER_QUEUE_LOCK) {
                List<FileRequestListener> listenerList = requestListenersMap.get(fileLoadRequest);
                if (listenerList != null) {
                    for (FileRequestListener listener : listenerList) {
                        try {
                            listener.onLoad(fileLoadRequest, fileResponse);
                        } catch (Exception e) {
                            callFailureMethodsOfListeners(e);
                        }
                    }
                    requestListenersMap.remove(fileLoadRequest);
                }
            }
            synchronized (REQUEST_QUEUE_LOCK) {
                fileLoadRequestSet.remove(fileLoadRequest);
            }
        }
    }

    private File searchAndGetLocalFile() throws Exception {
        validateAllParameters();
        File foundFile = null;
        if (!TextUtils.isEmpty(fileLoadRequest.getDirectoryName())) {
            File dir = AndroidFileManager.getAppropriateDirectory(context, fileLoadRequest.getDirectoryName(), fileLoadRequest.getDirectoryType());
            if (dir != null && dir.exists()) {
                File[] allFiles = dir.listFiles();
                if (allFiles != null) {
                    for (File file : allFiles) {
                        if (!file.isDirectory() && file.getName().equals(getFileName(fileLoadRequest.getUri()))) {
                            foundFile = file;
                            break;
                        }
                    }
                }
            }
        }
        return foundFile;
    }

    private String getFileName(String uri) throws Exception {
        String fileName;
        try {
            //passed uri is valid url, create file name as hashcode of url
            new URL(uri);
            fileName = String.valueOf(uri.hashCode());
        } catch (MalformedURLException e) {
            if (uri.contains("/"))
                throw new Exception("File name should not contain path separator \"/\"");
            //passed uri is name of the file
            fileName = uri;
        }
        return fileName;
    }

    @NonNull
    private AsyncTask<Void, Void, DownloadResponse> getFileLoaderAsyncTask() {
        return new AsyncTask<Void, Void, DownloadResponse>() {
            @Override
            protected DownloadResponse doInBackground(Void... voids) {
                DownloadResponse downloadResponse = new DownloadResponse();
                File loadedFile;
                try {
                    //search file locally
                    loadedFile = searchAndGetLocalFile();
                    if (loadedFile == null || !loadedFile.exists()) {
                        //download from internet
                        FileDownloader downloader = new FileDownloader(context, fileLoadRequest.getUri(), fileLoadRequest.getDirectoryName(), fileLoadRequest.getDirectoryType());
                        loadedFile = downloader.download();
                    }
                    downloadResponse.setDownloadedFile(loadedFile);
                } catch (Exception e) {
                    Log.d(TAG, "doInBackground: " + e.getMessage());
                    downloadResponse.setE(e);
                }
                return downloadResponse;
            }

            @Override
            protected void onPostExecute(DownloadResponse downloadResponse) {
                super.onPostExecute(downloadResponse);
                //if task is synchronous then simply return
                if (fileLoadRequest.getRequestListener() == null) return;

                //if task is asynchronous, notify results to listeners
                File downloadedFile = downloadResponse.getDownloadedFile();
                if (downloadedFile != null) {
                    sendFileResponseToListeners(downloadedFile);
                } else {
                    callFailureMethodsOfListeners(downloadResponse.getE());
                }
            }
        };
    }

    public FileLoadRequest getFileLoadRequest() {
        return fileLoadRequest;
    }

    public void setFileLoadRequest(FileLoadRequest fileLoadRequest) {
        this.fileLoadRequest = fileLoadRequest;
    }

    public FileDeleteRequest getFileDeleteRequest() {
        return fileDeleteRequest;
    }

    public void setFileDeleteRequest(FileDeleteRequest fileDeleteRequest) {
        this.fileDeleteRequest = fileDeleteRequest;
    }
}
