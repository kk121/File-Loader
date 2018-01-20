package com.krishna.fileloader.utility;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        int mCorePoolSize = 60;
        int mMaximumPoolSize = 80;
        int mKeepAliveTime = 30;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(mMaximumPoolSize);
        return new ThreadPoolExecutor(mCorePoolSize, mMaximumPoolSize, mKeepAliveTime, TimeUnit.SECONDS, workQueue);
    }

    public static boolean isValidFileName(String fileName) {
        Pattern pattern = Pattern.compile(".*\\..*");
        return pattern.matcher(fileName).matches();
    }
}
