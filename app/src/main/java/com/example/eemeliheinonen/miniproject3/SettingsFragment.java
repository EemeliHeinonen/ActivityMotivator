package com.example.eemeliheinonen.miniproject3;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by eemeliheinonen on 10/10/2016.
 */

public class SettingsFragment extends Fragment {
    private static final String ARG_PARAM1 = "txt";
    private String dTxt;
    private TextView tv1;
    private EditText editAge;
    private EditText editBurst;

    public static SettingsFragment newInstance(String dogTxt) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, dogTxt);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dTxt = getArguments().getString(ARG_PARAM1);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout myView =(RelativeLayout) inflater.inflate(R.layout.fragment_settings, container, false);
        tv1 = (TextView) myView.findViewById(R.id.tvFragment);
        editAge = (EditText) myView.findViewById(R.id.etAge);
        editBurst = (EditText) myView.findViewById(R.id.etBurstLength);
        tv1.setText(dTxt);
        return myView;
    }
}
