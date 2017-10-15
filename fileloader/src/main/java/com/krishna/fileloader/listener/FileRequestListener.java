package com.krishna.fileloader.listener;

import com.krishna.fileloader.request.FileLoadRequest;
import com.krishna.fileloader.pojo.FileResponse;

/**
 * Created by krishna on 12/10/17.
 */

public interface FileRequestListener<T> {
    void onLoad(FileLoadRequest request, FileResponse<T> response);

    void onError(FileLoadRequest request, Throwable t);
}
