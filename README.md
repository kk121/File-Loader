# File Loader
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-File%20Loader-green.svg?style=flat )](https://android-arsenal.com/details/1/6638)  [![Jitpack](https://jitpack.io/v/kk121/File-Loader.svg)](https://jitpack.io/#kk121/File-Loader)

<br>
Android library for downloading, saving/caching and retrieving any type of files ( image, video, pdf, apk etc ) easily.

## Download
### Gradle:
```sh
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
    
dependencies {
    implementation 'com.github.kk121:File-Loader:1.2'
}
```
### Maven:
```sh
<dependency>
  <groupId>com.github.kk121</groupId>
  <artifactId>File-Loader</artifactId>
  <version>1.2</version>
</dependency>
```
## How do I use File Loader?
### Asynchronously load file as generic file:
```sh
FileLoader.with(this)
                .load("https://upload.wikimedia.org/wikipedia/commons/3/3c/Enrique_Simonet_-_Marina_veneciana_6MB.jpg",false) //2nd parameter is optioal, pass true to force load from network
                .fromDirectory("test4", FileLoader.DIR_INTERNAL)
                .asFile(new FileRequestListener<File>() {
                    @Override
                    public void onLoad(FileLoadRequest request, FileResponse<File> response) {
                        File loadedFile = response.getBody();
                        // do something with the file
                    }

                    @Override
                    public void onError(FileLoadRequest request, Throwable t) {
                    }
                });
```
### Load file as Object:
```sh
FileLoader.with(this)
                .load("http://echo.jsontest.com/key1/value1/key2/value2")
                .fromDirectory("test3", FileLoader.DIR_INTERNAL)
                .asObject(new FileRequestListener<JsonTest>() {
                    @Override
                    public void onLoad(FileLoadRequest request, FileResponse<JsonTest> response) {
                        JsonTest jsonTest = response.getBody();
                        //do something with jsonTest object
                    }

                    @Override
                    public void onError(FileLoadRequest request, Throwable t) {

                    }
                });
```
### Load multiple files with progress update listener ( load all from same directory ):
```sh
final String[] uris = {"https://images.pexels.com/photos/45170/kittens-cat-cat-puppy-rush-45170.jpeg",
                "https://upload.wikimedia.org/wikipedia/commons/3/3c/Enrique_Simonet_-_Marina_veneciana_6MB.jpg",
                "https://d15shllkswkct0.cloudfront.net/wp-content/blogs.dir/1/files/2017/01/Google-acquires-Fabric.png"};
                
FileLoader.multiFileDownload(this)
                .fromDirectory(Environment.DIRECTORY_PICTURES, FileLoader.DIR_EXTERNAL_PUBLIC)
                .progressListener(new MultiFileDownloadListener() {
                    @Override
                    public void onProgress(File downloadedFile, int progress, int totalFiles) {
                    }

                    @Override
                    public void onError(Exception e, int progress) {
                        super.onError(e, progress);
                    }
                }).loadMultiple(uris);
```
### Load multiple files with progress update listener ( load from different directory ):
```sh
List<MultiFileLoadRequest> multiFileLoadRequests = new ArrayList<>();
        multiFileLoadRequests.add(new MultiFileLoadRequest(uris[0], "test2", FileLoader.DIR_INTERNAL, false));
        multiFileLoadRequests.add(new MultiFileLoadRequest(uris[1], Environment.DIRECTORY_DOWNLOADS, FileLoader.DIR_EXTERNAL_PRIVATE, false));
        multiFileLoadRequests.add(new MultiFileLoadRequest(uris[2], Environment.DIRECTORY_PICTURES, FileLoader.DIR_EXTERNAL_PUBLIC, false));
        
MultiFileDownloader multiFileDownloader = FileLoader.multiFileDownload(this);
multiFileDownloader.progressListener(new MultiFileDownloadListener() {
            @Override
            public void onProgress(File downloadedFile, int progress, int totalFiles) {
                
            }

            @Override
            public void onError(Exception e, int progress) {
                super.onError(e, progress);
            }
        }).loadMultiple(true, uris);
```
### Cancel the Multiple loading:
```sh
multiFileDownloader.cancelLoad();
```
### Delete multiple files:
```sh
final String[] uris = {"https://images.pexels.com/photos/45170/kittens-cat-cat-puppy-rush-45170.jpeg",
                "https://upload.wikimedia.org/wikipedia/commons/3/3c/Enrique_Simonet_-_Marina_veneciana_6MB.jpg",
                "https://d15shllkswkct0.cloudfront.net/wp-content/blogs.dir/1/files/2017/01/Google-acquires-Fabric.png"};
try {
      FileLoader.deleteWith(this).fromDirectory("test2", FileLoader.DIR_INTERNAL).deleteFiles(uris);
    } catch (Exception e) {
      e.printStackTrace();
    }
```
### Delete all files of the directory:
```sh
FileLoader.deleteWith(this).fromDirectory("test2", FileLoader.DIR_INTERNAL).deleteAllFiles();
```
### Delete all files of directory except files passed in argument:
```sh
FileLoader.deleteWith(this).fromDirectory("test3", FileLoader.DIR_INTERNAL).deleteAllFilesExcept(uris);
``` 
## Status
This is still in beta.Comments/bugs/questions/pull requests are always welcome!
### **Cheers :)**

