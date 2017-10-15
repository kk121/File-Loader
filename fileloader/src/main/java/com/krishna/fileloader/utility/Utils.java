package com.krishna.fileloader.utility;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by krishna on 15/10/17.
 */

public class Utils {

    public static Object getObject(File file, Class clazz) {
        Object object;
        try {
            object = deserializeObject(file, clazz);
        } catch (IOException | ClassNotFoundException e) {
            object = deserializeJson(file, clazz);
        }
        return object;
    }

    private static Object deserializeJson(File file, Class clazz) {
        String jsonString = AndroidFileManager.readFileAsString(file);
        Gson gson = new Gson();
        return gson.fromJson(jsonString, clazz);
    }

    private static Object deserializeObject(File file, Class clazz) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        Object object = in.readObject();
        in.close();
        return object;
    }

    public static RequestOptions getGlideDefaultRequestOptions(Drawable placeholder) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.skipMemoryCache(true);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
        requestOptions.signature(new ObjectKey(System.currentTimeMillis()));
        return requestOptions;
    }
}
