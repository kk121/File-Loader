package com.krishna.fileloader.listener;

import com.krishna.fileloader.request.FileLoadRequest;
import com.krishna.fileloader.pojo.FileResponse;

/**
 * Created by krishna on 12/10/17.
 */

public abstract class FileRequestListener<T> {
    public void onStatusChange(int status) {

    }

    public abstract void onLoad(FileLoadRequest request, FileResponse<T> response);

    public abstract void onError(FileLoadRequest request, Throwable t);
}
