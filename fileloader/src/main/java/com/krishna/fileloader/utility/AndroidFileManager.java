package com.krishna.fileloader.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.krishna.fileloader.FileLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by krishna on 12/10/17.
 */

public class AndroidFileManager {

    public static File getFileForRequest(Context context, String fileUri, String dirName, int dirType) throws Exception {
        String fileName = getFileName(fileUri);
        return new File(getAppropriateDirectory(context, dirName, dirType), fileName);
    }

    public static String getFileName(String uri) throws Exception {
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

    public static File getAppropriateDirectory(Context context, String directoryName, int directoryType) {
        File file;
        switch (directoryType) {
            case FileLoader.DIR_CACHE:
                file = new File(context.getCacheDir(), directoryName);
                break;
            case FileLoader.DIR_EXTERNAL_PRIVATE:
                // TODO: 12/10/17 check if dir is readable/writable
                file = new File(context.getExternalFilesDir(null), directoryName);
                break;
            case FileLoader.DIR_EXTERNAL_PUBLIC:
                // TODO: 12/10/17 check if dir is readable/writable
                file = new File(Environment.getExternalStoragePublicDirectory(null), directoryName);
                break;
            default:
                //by default take internal directory
                file = new File(context.getFilesDir(), directoryName);
        }

        if (!file.exists())
            file.mkdirs();
        return file;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String readFileAsString(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            if (file.exists()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static Bitmap getBitmap(File downloadedFile) {
        return BitmapFactory.decodeFile(downloadedFile.getPath());
    }

    public static void deleteFile(Context context, String fileUri, String dirName, @FileLoader.DirectoryType int dirType) throws Exception {
        File fileToDelete = getFileForRequest(context, fileUri, dirName, dirType);
        if (fileToDelete.exists())
            fileToDelete.delete();
    }

    public static File searchAndGetLocalFile(Context context, String uri, String dirName, @FileLoader.DirectoryType int dirType) throws Exception {
        File foundFile = null;
        if (!TextUtils.isEmpty(dirName)) {
            File dir = AndroidFileManager.getAppropriateDirectory(context, dirName, dirType);
            if (dir != null && dir.exists()) {
                File[] allFiles = dir.listFiles();
                if (allFiles != null) {
                    for (File file : allFiles) {
                        if (!file.isDirectory() && file.getName().equals(getFileName(uri))) {
                            foundFile = file;
                            break;
                        }
                    }
                }
            }
        }
        return foundFile;
    }
}
