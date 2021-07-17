package com.ibug.misc;

import android.content.Context;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.RetryException;
import com.dropbox.core.util.IOUtil.ProgressListener;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishErrorException;
import com.dropbox.core.v2.files.UploadSessionLookupErrorException;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static com.ibug.misc.Utils.getDbxClientV2;


public class UploadTask {
    // Adjust the chunk size based on your network speed and reliability. Larger chunk sizes will
    // result in fewer network requests, which will be faster. But if an error occurs, the entire
    // chunk will be lost and have to be re-uploaded. Use a multiple of 4MiB for your chunk size.
    private static final long CHUNKED_UPLOAD_CHUNK_SIZE = 8L << 20; // 8MiB
    private static final int CHUNKED_UPLOAD_MAX_ATTEMPTS = 5;

    private final Context context;
    private String remoteFile;

    private final DbxClientV2 dbxClient;
    private final DbxUserFilesRequests filesRequests;

    private static String TAG = Utils.Tag;

    public interface Callback {
        void action (File fileUploaded, long fileSize);
    }
    /**
     * UploadToDropbox constructor
     *
     * @param context Application context
     */
    public UploadTask(Context context) {
        this.context = context;
        // Create a DbxClientV2, which is what you use to make API calls.
        dbxClient = getDbxClientV2(context);
        filesRequests = dbxClient.files();
    }
    
    public void upload(String localFile, String remoteFile,  Callback callback) {
        try
        {
            String pathError = DbxPathV2.findError(remoteFile);
            if ( pathError != null ) {
                Log.d(TAG, "Invalid <dropbox-path>: " + pathError );
                return;
            }

            File _localFile = new File(localFile);
            if ( !_localFile.exists( ) ) {
                Log.d(TAG, "Invalid <local-path>: file does not exist.");
                return;
            }

            if ( !_localFile.isFile( ) ) {
                Log.d(TAG, "Invalid <local-path>: not a file.");
                return;
            }

            if ( !(_localFile.length() > 0) ) {
                Log.d(TAG, "Invalid <local-path>: no content in file.");
                return;
            }

            // upload the file with simple upload API if it is small enough, otherwise use chunked
            // upload API for better performance. Arbitrarily chose 2 times our chunk size as the
            // deciding factor. This should really depend on your network.
            boolean choice = localFile.length() <= (2 * CHUNKED_UPLOAD_CHUNK_SIZE );
            if(choice) uploadFile(_localFile, remoteFile, callback);
            else chunkedUploadFile(_localFile, remoteFile, callback);

        }
        
        catch ( Exception e ) {
            Log.d( TAG, " Error @ UploadToDropbox.upload( ) : " + String.valueOf(e));
        }
        
    }

    /**
     * Uploads a file in a single request. This approach is preferred for small files since it
     * eliminates unnecessary round-trips to the servers.
     *
     * @param localFile local file to upload
     * @param callback called after upload is Finished ~ progressListener
     */
    private void uploadFile(File localFile, String remoteFile, Callback callback )
    {
        try ( InputStream inputStream = new FileInputStream(localFile) ) {
            ProgressListener progressListener = (uploadSize) -> {
                callback.action (localFile, uploadSize);
            };
            UploadBuilder builder = filesRequests.uploadBuilder(remoteFile);
            builder.withMode(WriteMode.OVERWRITE)
                    .withClientModified(new Date(localFile.lastModified()))
                    .uploadAndFinish(inputStream, progressListener);

        }
        catch (UploadErrorException ex)
        {
            Log.d( TAG,"UploadErrorException @ uploadFile: " + ex.getMessage( ) );
        }
        catch (DbxException ex)
        {
            Log.d( TAG,"DbxException @ uploadFile: \"" + localFile + "\": " + ex.getMessage( ) );
        }
        catch (IOException ex)
        {
            Log.d( TAG,"IOException @ uploadFile:\"" + localFile + "\": " + ex.getMessage( ) );
        }

    }

    /**
     * Uploads a file in chunks using multiple requests. This approach is preferred for larger files
     * since it allows for more efficient processing of the file contents on the server side and
     * also allows partial uploads to be retried (e.g. network connection problem will not cause you
     * to re-upload all the bytes).
     *
     * @param localFile local file to upload
     */
    private void chunkedUploadFile(File localFile, String remoteFile,  Callback callback ) {
        long size = localFile.length();

        // assert our file is at least the chunk upload size. We make this assumption in the code
        // below to simplify the logic.
        if ( size < CHUNKED_UPLOAD_CHUNK_SIZE ) {
            Log.d( TAG,"File too small, use upload() instead.");
            return;
        }

        long uploaded = 0L;
        DbxException thrown = null;

        ProgressListener progressListener = new ProgressListener ( ) {
            long uploadedBytes = 0;
            @Override
            public void onProgress(long uploadedSize) {
                if (uploadedSize == CHUNKED_UPLOAD_CHUNK_SIZE) uploadedBytes += CHUNKED_UPLOAD_CHUNK_SIZE;
                callback.action (localFile, uploadedBytes);
            }
        };

        // Chunked uploads have 3 phases, each of which can accept uploaded bytes:
        //
        //    (1)  Start: initiate the upload and get an upload session ID
        //    (2) Append: upload chunks of the file to append to our session
        //    (3) Finish: commit the upload and close the session
        //
        // We track how many bytes we uploaded to determine which phase we should be in.
        String sessionId = null;
        for (int i = 0; i < CHUNKED_UPLOAD_MAX_ATTEMPTS; ++i)
        {
            if (i > 0)
            {
                Log.d( TAG, String.format("Retrying chunked upload (%d / %d attempts)\n", i + 1, CHUNKED_UPLOAD_MAX_ATTEMPTS ) );
            }

            try ( InputStream in = new FileInputStream( localFile ) )
            {
                // if this is a retry, make sure seek to the correct offset
                in.skip( uploaded );

                // (1) Start
                if (sessionId == null) {
                    sessionId = dbxClient.files().uploadSessionStart()
                            .uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE, progressListener)
                            .getSessionId();
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                    printProgress(uploaded, size);
                }

                UploadSessionCursor cursor = new UploadSessionCursor(sessionId, uploaded);

                // (2) Append
                while ((size - uploaded) > CHUNKED_UPLOAD_CHUNK_SIZE) {
                    dbxClient.files().uploadSessionAppendV2(cursor)
                            .uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE, progressListener);
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                    printProgress(uploaded, size);
                    cursor = new UploadSessionCursor(sessionId, uploaded);
                }

                // (3) Finish
                long remaining = size - uploaded;
                CommitInfo commitInfo = CommitInfo.newBuilder(remoteFile)
                        .withMode(WriteMode.ADD)
                        .withClientModified(new Date(localFile.lastModified()))
                        .build();
                FileMetadata metadata = dbxClient.files().uploadSessionFinish(cursor, commitInfo)
                        .uploadAndFinish(in, remaining, progressListener);

                Log.d( TAG, metadata.toStringMultiline());
                return;
            } 
            catch (RetryException ex)
            {
                thrown = ex;
                // RetryExceptions are never automatically retried by the client for uploads. Must
                // catch this exception even if DbxRequestConfig.getMaxRetries() > 0.
                sleepQuietly(ex.getBackoffMillis());
                continue;
            }
            catch ( NetworkIOException ex )
            {
                thrown = ex;
                // network issue with Dropbox (maybe a timeout?) try again
                continue;
            }
            catch ( UploadSessionLookupErrorException ex )
            {
                if (ex.errorValue.isIncorrectOffset( ) )
                {
                    thrown = ex;
                    // server offset into the stream doesn't match our offset (uploaded). Seek to
                    // the expected offset according to the server and try again.
                    uploaded = ex.errorValue
                            .getIncorrectOffsetValue()
                            .getCorrectOffset();
                    continue;
                }
                else
                {
                    // Some other error occurred, give up.
                    Log.d( TAG,"Error uploading to Dropbox: " + ex.getMessage());
                    return;
                }
            }

            catch (UploadSessionFinishErrorException ex)
            {
                if (ex.errorValue.isLookupFailed() && ex.errorValue.getLookupFailedValue().isIncorrectOffset( ) )
                {
                    thrown = ex;
                    // server offset into the stream doesn't match our offset (uploaded). Seek to
                    // the expected offset according to the server and try again.
                    uploaded = ex.errorValue
                            .getLookupFailedValue()
                            .getIncorrectOffsetValue()
                            .getCorrectOffset();
                    continue;
                }
                else
                {
                    // some other error occurred, give up.
                    Log.d( TAG,"Error uploading to Dropbox: " + ex.getMessage());
                    return;
                }
            }
            catch ( DbxException ex )
            {
                Log.d( TAG,"Error uploading to Dropbox: " + ex.getMessage());
                return;
            }
            catch ( IOException ex )
            {
                Log.d( TAG,"Error reading from file \"" + localFile + "\": " + ex.getMessage());
                return;
            }

        }

        // if we made it here, then we must have run out of attempts
        Log.d( TAG,"Maxed out upload attempts to Dropbox. Most recent error: " + thrown.getMessage( ) );
    }

    private static void printProgress( long uploaded, long size ) {
        System.out.printf("Uploaded %12d / %12d bytes (%5.2f%%)\n", uploaded, size, 100 * (uploaded / (double) size));
    }

    private static void sleepQuietly(long millis) {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException ex)
        {
            // just exit
            Log.d( TAG,"Error uploading to Dropbox: interrupted during backoff.");
        }
    }

}
