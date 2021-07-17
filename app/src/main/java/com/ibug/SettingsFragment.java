package com.ibug;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ibug.databinding.FragmentSettingsBinding;
import com.ibug.misc.AppSettings;


public class SettingsFragment extends Fragment {

    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentSettingsBinding binding = FragmentSettingsBinding.inflate(inflater, container, false);
        AppSettings appSettings = getSettings(context);
        binding.setSettings(appSettings);
        return binding.getRoot();
    }

    private AppSettings getSettings(Context c) {
        String device_name = adapter != null ? adapter.getName():"";
        String site = c.getString(R.string.app_name);
        AppSettings settings = new AppSettings(c);
        settings.setName(device_name);
        settings.setSite(site);
        return settings;
    }

}