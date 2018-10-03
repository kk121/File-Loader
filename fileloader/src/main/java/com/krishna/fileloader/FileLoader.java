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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * Created by krishna on 09/10/17.
 */

public class FileLoader {
    private static final String TAG = "FileLoader";
    public static final int STATUS_START_LOCAL_SEARCH = 100;
    public static final int STATUS_START_DOWNLOADING = 101;
    public static final int STATUS_DOWNLOAD_END = 102;
    // Directory type
    public static final int DIR_INTERNAL = 1; //Only your app can access. { android FilesDir() }
    public static final int DIR_CACHE = 2; // Only your app can access, can be deleted by system. { android CacheDir() }
    public static final int DIR_EXTERNAL_PRIVATE = 3; //Accessible by all apps but not by users. { android ExternalFilesDir() }
    public static final int DIR_EXTERNAL_PUBLIC = 4; //Accessible by all apps and users. { android ExternalStorageDirectory() }

    //Defaults
    public static final int DEFAULT_DIR_TYPE = DIR_CACHE;
    public static final String DEFAULT_DIR_NAME = "file_loader";

    @IntDef({DIR_INTERNAL, DIR_CACHE, DIR_EXTERNAL_PRIVATE, DIR_EXTERNAL_PUBLIC})
    public @interface DirectoryType {
    }

    private static Map<FileLoadRequest, Boolean> backingMap = new WeakHashMap<>();
    private static Set<FileLoadRequest> fileLoadRequestSet = Collections.newSetFromMap(backingMap);
    private static Map<FileLoadRequest, List<FileRequestListener>> requestListenersMap = new WeakHashMap<>();
    private static Map<FileLoadRequest, FileResponse> requestResponseMap = new WeakHashMap<>();
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

    public static FileLoaderBuilder with(Context context, boolean autoRefresh) {
        return new FileLoaderBuilder(context, autoRefresh);
    }

    public static FileDeleteBuilder deleteWith(Context context) {
        return new FileDeleteBuilder(context);
    }

    public static MultiFileDownloader multiFileDownload(Context context) {
        return new MultiFileDownloader(context);
    }

    public static MultiFileDownloader multiFileDownload(Context context, boolean autoRefresh) {
        return new MultiFileDownloader(context, autoRefresh);
    }

    public FileResponse loadFile() throws Exception {
        validateAllParameters();
        DownloadResponse response = getFileLoaderAsyncTask().executeOnExecutor(Utils.getThreadPoolExecutor()).get();
        if (response.getDownloadedFile() != null) {
            return createFileResponse(response.getDownloadedFile());
        } else {
            throw response.getE();
        }
    }

    public void loadFileAsync() {
        addRequestListenerToQueue();
        try {
            validateAllParameters();
            if (fileLoadRequest.getRequestListener() == null)
                throw new NullPointerException("File Request listener should not be null");
        } catch (Exception e) {
            callFailureMethodsOfListeners(e);
            return;
        }
        // All parameters are valid, move to next step
        if (!fileLoadRequestSet.contains(fileLoadRequest)) {
            synchronized (REQUEST_QUEUE_LOCK) {
                fileLoadRequestSet.add(fileLoadRequest);
            }
            getFileLoaderAsyncTask().executeOnExecutor(Utils.getThreadPoolExecutor());
        } else if (requestResponseMap.get(fileLoadRequest) != null) {
            sendFileResponseToListeners(requestResponseMap.get(fileLoadRequest));
        }
    }

    public int deleteFiles() throws Exception {
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

    public int deleteAllFiles() throws Exception {
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

    public int deleteAllFilesExcept() throws Exception {
        int fileCount = 0;
        File dir = AndroidFileManager.getAppropriateDirectory(context, fileDeleteRequest.getDirectoryName(), fileDeleteRequest.getDirectoryType());
        Set<String> filesToKeepSet = new HashSet<>();
        for (String fileUri : fileDeleteRequest.getFileUriList()) {
            try {
                filesToKeepSet.add(AndroidFileManager.getFileName(fileUri));
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
                    Iterator<FileRequestListener> it = listenerList.iterator();
                    while (it.hasNext()) {
                        try {
                            FileRequestListener listener = it.next();
                            it.remove();
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

    private void sendFileResponseToListeners(FileResponse fileResponse) {
        if (!requestListenersMap.isEmpty()) {
            synchronized (REQUEST_LISTENER_QUEUE_LOCK) {
                List<FileRequestListener> listenerList = requestListenersMap.get(fileLoadRequest);
                if (listenerList != null) {
                    Iterator<FileRequestListener> it = listenerList.iterator();
                    while (it.hasNext()) {
                        try {
                            FileRequestListener listener = it.next();
                            it.remove();
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
                requestResponseMap.remove(fileLoadRequest);
            }
        }
    }

    private void sendStatusToListeners(int status) {
        if (!requestListenersMap.isEmpty()) {
            synchronized (REQUEST_LISTENER_QUEUE_LOCK) {
                List<FileRequestListener> listenerList = requestListenersMap.get(fileLoadRequest);
                if (listenerList != null) {
                    for (FileRequestListener listener : listenerList) {
                        listener.onStatusChange(status);
                    }
                }
            }
        }
    }

    @NonNull
    private AsyncTask<Void, Integer, DownloadResponse> getFileLoaderAsyncTask() {
        return new AsyncTask<Void, Integer, DownloadResponse>() {

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if (values.length > 0)
                    sendStatusToListeners(values[0]);
            }

            @Override
            protected DownloadResponse doInBackground(Void... voids) {
                DownloadResponse downloadResponse = new DownloadResponse();
                File loadedFile = null;
                try {
                    if (!fileLoadRequest.isForceLoadFromNetwork()) {
                        //search file locally
                        publishProgress(STATUS_START_LOCAL_SEARCH);
                        loadedFile = AndroidFileManager.searchAndGetLocalFile(context, fileLoadRequest.getUri(),
                                fileLoadRequest.getDirectoryName(), fileLoadRequest.getDirectoryType());
                    }
                    if (loadedFile == null || !loadedFile.exists() || fileLoadRequest.isAutoRefresh()) {
                        //download from internet
                        publishProgress(STATUS_START_DOWNLOADING);
                        FileDownloader downloader = new FileDownloader(context, fileLoadRequest.getUri(), fileLoadRequest.getDirectoryName(), fileLoadRequest.getDirectoryType());
                        loadedFile = downloader.download(fileLoadRequest.isAutoRefresh(), fileLoadRequest.isCheckIntegrity());
                        publishProgress(STATUS_DOWNLOAD_END);
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
                if (downloadedFile != null && downloadResponse.getE() == null) {
                    FileResponse fileResponse = createFileResponse(downloadedFile);
                    requestResponseMap.put(fileLoadRequest, fileResponse);
                    sendFileResponseToListeners(fileResponse);
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
