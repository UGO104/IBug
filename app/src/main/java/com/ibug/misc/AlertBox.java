package com.ibug.misc;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.text.HtmlCompat;

import android.content.DialogInterface;
import com.ibug.IAnnotations.*;
import com.ibug.R;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static com.ibug.misc.Utils.Tag;


public class AlertBox extends AlertDialog.Builder {

    private int msgTextColor;
    private EditText alertInput;
    private TextWatcher watcher;
    private boolean showAlertInput;
    private String title = "Dropbox";
    private Spanned message = new SpannableString("Default alert notification message");


    public AlertBox(@NonNull Context context) {
        super(new ContextThemeWrapper(context, R.style.AlertDialog));
    }

    public AlertDialog show() {
        Runnable runnable = () -> {
            super.setTitle(title);
            super.setView(alertLayoutView());
            super.create().show();
        };

        new Handler(Looper.getMainLooper()).post(runnable);
        return null;
    }

    private View alertLayoutView() {
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.alert_box_input, null);
        TextView alertMessage = view.findViewById(R.id.alertMessage);
        alertMessage.setText(this.message);

        if (showAlertInput) {
            alertInput = view.findViewById(R.id.alertInput);
            alertInput.setVisibility(View.VISIBLE);
            if (watcher!= null)
            alertInput.addTextChangedListener(watcher);
        }

        return view;
    }

    @StartMethod
    public void title(String value) {
        this.title = value;
    }

    @StartMethod
    public void message(String message) {
        this.message = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    @StartMethod
    public void message(String message, String color) {
        if (color.startsWith("#")) color = '#' + color.substring(3);
        String msg = "<font color=" + color + ">" + message + "</font>";
        this.message = HtmlCompat.fromHtml(msg, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    @StartMethod
    public void message(String message, String stressText, String color) {
        if (color.startsWith("#")) color = '#' + color.substring(3);
        String stressText0 = "<font color=" + color + ">" + stressText + "</font>";
        String message0 = message.replace(stressText, stressText0);
        this.message = HtmlCompat.fromHtml(message0, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    @StartMethod
    public void messageTextColor(int color) { this.msgTextColor = color; }

    @StartMethod
    public void showAlertInput(boolean show) { this.showAlertInput = show; }

    @StartMethod
    public String getInputText() { return alertInput.getText().toString(); }

    @StartMethod
    public void inputTextChangeListener(TextWatcher watcher) { this.watcher = watcher; }

}
