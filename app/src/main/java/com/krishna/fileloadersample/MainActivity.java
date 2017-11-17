package com.krishna.fileloadersample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.krishna.fileloader.FileLoader;
import com.krishna.fileloader.builder.MultiFileDownloader;
import com.krishna.fileloader.listener.FileRequestListener;
import com.krishna.fileloader.listener.MultiFileDownloadListener;
import com.krishna.fileloader.pojo.FileResponse;
import com.krishna.fileloader.request.FileLoadRequest;
import com.krishna.fileloader.request.MultiFileLoadRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageView iv = (ImageView) findViewById(R.id.image);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);
        final TextView tvProgress = (TextView) findViewById(R.id.tv_progress);

        //Asynchronously load file as generic file
        FileLoader.with(this)
                .load("https://upload.wikimedia.org/wikipedia/commons/3/3c/Enrique_Simonet_-_Marina_veneciana_6MB.jpg")
                .fromDirectory("test4", FileLoader.DIR_INTERNAL)
                .asFile(new FileRequestListener<File>() {
                    @Override
                    public void onLoad(FileLoadRequest request, FileResponse<File> response) {
                        Bitmap bitmap = BitmapFactory.decodeFile(response.getDownloadedFile().getPath());
//                        iv.setImageBitmap(bitmap);
                        Glide.with(MainActivity.this).load(response.getBody()).into(iv);
                    }

                    @Override
                    public void onError(FileLoadRequest request, Throwable t) {
                        Log.d(TAG, "onError: " + t.getMessage());
                    }
                });


        //Synchronous json file loading
        try {
            FileResponse response = FileLoader.with(this)
                    .load("http://echo.jsontest.com/key1/value1/key2/value2")
                    .fromDirectory("test3", FileLoader.DIR_INTERNAL)
                    .asObject(JsonTest.class);
            JsonTest test = (JsonTest) response.getBody();
            Log.d(TAG, "" + test);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String[] uris = {"https://images.pexels.com/photos/45170/kittens-cat-cat-puppy-rush-45170.jpeg",
                "https://upload.wikimedia.org/wikipedia/commons/3/3c/Enrique_Simonet_-_Marina_veneciana_6MB.jpg"};
        //delete files
        try {
            FileLoader.deleteWith(this).fromDirectory("test2", FileLoader.DIR_INTERNAL).deleteFiles(uris);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //delete all files from the directory
        FileLoader.deleteWith(this).fromDirectory("test2", FileLoader.DIR_INTERNAL).deleteAllFiles();

        //delete all files from directory except files passed in argument
        FileLoader.deleteWith(this).fromDirectory("test3", FileLoader.DIR_INTERNAL).deleteAllFilesExcept(uris);

        List<MultiFileLoadRequest> multiFileLoadRequests = new ArrayList<>();
        multiFileLoadRequests.add(new MultiFileLoadRequest(uris[0], "test2", FileLoader.DIR_INTERNAL, true));
        multiFileLoadRequests.add(new MultiFileLoadRequest(uris[1], "test4", FileLoader.DIR_INTERNAL, true));

        final MultiFileDownloader multiFileDownloader = FileLoader.multiFileDownload(this);
        multiFileDownloader.progressListener(new MultiFileDownloadListener() {
            @Override
            public void onProgress(File downloadedFile, int progress, int totalFiles) {
                multiFileDownloader.cancelLoad();
                tvProgress.setText(progress + " of " + totalFiles);
                Glide.with(MainActivity.this).load(downloadedFile).into(iv);
            }
        }).loadMultiple(multiFileLoadRequests);
    }

    private void loadImage(final ImageView iv, String imageUrl) {
        iv.setImageBitmap(null);
        FileLoader.with(this)
                .load(imageUrl)
                .fromDirectory("test4", FileLoader.DIR_INTERNAL)
                .asFile(new FileRequestListener<File>() {
                    @Override
                    public void onLoad(FileLoadRequest request, FileResponse<File> response) {
                        Bitmap bitmap = BitmapFactory.decodeFile(response.getDownloadedFile().getPath());
                        iv.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(FileLoadRequest request, Throwable t) {
                        Log.d(TAG, "onError: " + t.getMessage());
                    }
                });
    }
}
