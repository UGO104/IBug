package com.ibug.misc;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ibug.IAnnotations.*;

import org.apache.commons.collections4.map.ListOrderedMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.ibug.misc.Utils.Tag;

public class Compressor {

    static int BUFFER_SIZE = 1024;

    @StartMethod
    public static void zip(String zipFile, String[ ] files, Runnable runnable) {
        try
        {
            if (files != null && files.length > 0) {
                int counter;
                BufferedInputStream origin;
                byte[] bufferedData = new byte[BUFFER_SIZE];
                OutputStream os = new FileOutputStream(zipFile);
                BufferedOutputStream bis = new BufferedOutputStream(os, BUFFER_SIZE);
                ZipOutputStream zos = new ZipOutputStream(bis);

                for (String file: Arrays.asList(files)) {
                    InputStream in = new FileInputStream(file);
                    origin = new BufferedInputStream(in, BUFFER_SIZE);
                    ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf('/') + 1));

                    zos.putNextEntry (entry);
                    while ((counter = origin.read(bufferedData)) != -1) {
                        zos.write(bufferedData, 0, counter);
                    }
                    origin.close();
                }
                zos.close();
                runnable.run();
            }

        } catch (Exception ex) { Log.d(Tag, "Compressor<zip> " + ex.getMessage());}

    }

    @StartMethod
    public static void unzip(String zipFile, String writeToDirectory, Runnable runnable) {
        try
        {
            int size;
            byte[] buffer = new byte[BUFFER_SIZE];
            File directory = new File(writeToDirectory);
            InputStream in = new FileInputStream(zipFile);
            BufferedInputStream bis = new BufferedInputStream(in,BUFFER_SIZE);
            ZipInputStream zin = new ZipInputStream(bis);
            ZipEntry entry = null;

            while ((entry = zin.getNextEntry()) != null) {
                String path = writeToDirectory + entry.getName();
                File unzippedFile = new File (path);

                if (entry.isDirectory()) {
                    if (!unzippedFile.isDirectory()) {
                        unzippedFile.mkdirs();
                    }
                }
                else {
                    OutputStream os = new FileOutputStream(unzippedFile, false);
                    BufferedOutputStream bos = new BufferedOutputStream(os, BUFFER_SIZE);
                    while ((size = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
                        bos.write(buffer, 0, size);
                    }
                    zin.closeEntry();
                    bos.flush();
                    bos.close();
                }
            }

            zin.close();

        } catch (Exception ex) { Log.d(Tag, "Compressor<unzip>" + ex.getMessage());}

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void setFileAttributes(File location, ListOrderedMap<String, String> attributes) {
        try
        {
            Path path = location.toPath();
            UserDefinedFileAttributeView fileAttr =
                    Files.getFileAttributeView (path, UserDefinedFileAttributeView.class);
            if (attributes !=null && attributes.size() > 0) {
                for (String key: attributes.keyList()) {
                    String value = attributes.get(key);
                    Log.d(Tag, String.valueOf(fileAttr) );
                    fileAttr.write(key, Charset.defaultCharset().encode(value));
                }
            }

        } catch (Exception ex) { Log.d( Tag, "CallRecorder <setFileAttributes> : " + ex.getMessage() ); }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Map<String, String> getFileAttributes(File location) {
        Map<String, String> userAttributes = new HashMap<>();
        try
        {
            Path path = location.toPath();
            UserDefinedFileAttributeView fileAttr =
                    Files.getFileAttributeView (path, UserDefinedFileAttributeView.class);
            List<String> userAttr = fileAttr.list();
            if (userAttr.size() > 0) {
                for (String attributeName : userAttr) {
                    ByteBuffer attributeValue = ByteBuffer.allocate(fileAttr.size(attributeName));
                    fileAttr.read(attributeName, attributeValue);
                    attributeValue.flip();
                    CharBuffer charBuffer = Charset.defaultCharset().decode(attributeValue);
                    userAttributes.put(attributeName, charBuffer.toString());
                }

            }

        } catch (Exception ex) { Log.d( Tag, "CallRecorder <getFileAttributes> : " + ex.getMessage() ); }

        return userAttributes.size() > 0 ? userAttributes : null;
    }



}
