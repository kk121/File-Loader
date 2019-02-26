package com.krishna.fileloader.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
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

    public static File getFileForRequest(Context context, String fileUri, String fileNamePrefix, String dirName, int dirType) throws Exception {
        String fileName = getFileName(fileUri, fileNamePrefix);
        return new File(getAppropriateDirectory(context, dirName, dirType), fileName);
    }

    public static String getFileName(String uri, String fileNamePrefix) throws Exception {
        String fileName;
        try {
            //passed uri is valid url, create file name as hashcode of url
            URL url = new URL(uri);
            fileName = getFileNameFromUrl(url);
            if (fileName == null) {
                fileName = String.valueOf(uri.hashCode());
            }
        } catch (MalformedURLException e) {
            if (uri.contains("/"))
                throw new Exception("File name should not contain path separator \"/\"");
            //passed uri is name of the file
            fileName = uri;
        }
        return fileNamePrefix + fileName;
    }

    private static String getFileNameFromUrl(URL url) {
        String fileName = null;
        String path = url.getPath();
        if (path != null) {
            String pathArr[] = path.split("/");
            if (pathArr.length > 0) {
                String lastPath = pathArr[pathArr.length - 1];
                if (Utils.isValidFileName(lastPath)) {
                    fileName = lastPath;
                }
            }
        }
        return fileName;
    }

    public static File getAppropriateDirectory(Context context, String directoryName, int directoryType) throws Exception {
        File file = null;
        switch (directoryType) {
            case FileLoader.DIR_CACHE:
                file = new File(context.getCacheDir(), directoryName);
                break;
            case FileLoader.DIR_EXTERNAL_PRIVATE:
                file = getExternalPrivateDirectory(context, directoryName);
                break;
            case FileLoader.DIR_EXTERNAL_PUBLIC:
                file = getExternalPublicDirectory(directoryName);
                break;
            default:
                //by default take internal directory
                file = new File(context.getFilesDir(), directoryName);
        }

        if (!file.exists())
            file.mkdirs();
        return file;
    }

    @NonNull
    private static File getExternalPublicDirectory(String directoryName) throws Exception {
        File file;
        if (isExternalStorageWritable()) {
            File rootDir = Environment.getExternalStorageDirectory();
            if (rootDir == null) {
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), directoryName);
            } else {
                file = new File(rootDir, directoryName);
            }
        } else {
            throw new Exception("External storage is not available for write operation");
        }
        return file;
    }

    private static File getExternalPrivateDirectory(Context context, String directoryName) throws Exception {
        String baseDir = null;
        if (isExternalStorageWritable()) {
            File baseDirFile = context.getExternalFilesDir(null);
            if (baseDirFile == null) {
                baseDir = context.getFilesDir().getAbsolutePath();
            } else {
                baseDir = baseDirFile.getAbsolutePath();
            }
        } else {
            throw new Exception("External storage is not available for write operation");
        }
        return new File(baseDir, directoryName);
    }

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
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

    public static void deleteFile(Context context, String fileUri, String fileNamePrefix, String dirName, @FileLoader.DirectoryType int dirType) throws Exception {
        File fileToDelete = getFileForRequest(context, fileUri, fileNamePrefix, dirName, dirType);
        if (fileToDelete.exists())
            fileToDelete.delete();
    }

    public static File searchAndGetLocalFile(Context context, String uri, String fileNamePrefix, String dirName, @FileLoader.DirectoryType int dirType) throws Exception {
        File foundFile = null;
        if (!TextUtils.isEmpty(dirName)) {
            File dir = AndroidFileManager.getAppropriateDirectory(context, dirName, dirType);
            if (dir != null && dir.exists()) {
                File[] allFiles = dir.listFiles();
                if (allFiles != null) {
                    for (File file : allFiles) {
                        if (!file.isDirectory() && file.getName().equals(getFileName(uri, fileNamePrefix))) {
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
