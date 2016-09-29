package com.example.eemeliheinonen.miniproject3;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainFragment extends Fragment {
    private static final String ARG_PARAM1 = "txt";
    private String dTxt;

    public static MainFragment newInstance(String dogTxt) {
        MainFragment fragment = new MainFragment();
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
        LinearLayout myView =(LinearLayout) inflater.inflate(R.layout.fragment_main, container, false);
        TextView tv1 = (TextView) myView.findViewById(R.id.tv1);
        tv1.setText(dTxt);
        return myView;
    }



}

