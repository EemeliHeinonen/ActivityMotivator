package com.example.eemeliheinonen.miniproject3;


import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainFragment extends Fragment {
    private static final String ARG_PARAM1 = "txt";
    private String dTxt;
    private TextView tv1;
    private String TAG = "jeee mainfragment";
    private RadioGroup radioButtonGroup;
    private RadioButton rbWalk;
    private RadioButton rbFree;
    private RadioButton rbInterval;


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
        RelativeLayout myView =(RelativeLayout) inflater.inflate(R.layout.fragment_main, container, false);
        radioButtonGroup = (RadioGroup)myView.findViewById(R.id.radiobtnGroup);
        rbWalk = (RadioButton)myView.findViewById(R.id.radioFwalk);
        rbInterval = (RadioButton)myView.findViewById(R.id.radioIw);
        rbFree = (RadioButton)myView.findViewById(R.id.radioFwork);

        Log.d(TAG, "onCreateView: ");
        radioButtonGroup.check(R.id.radioFwalk);

        radioButtonGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch(checkedId)
                {
                    case R.id.radioFwalk:
                        Log.d(TAG, "onCheckedChanged: fwalk"+checkedId);
                        ((MainActivity)getActivity()).setMainMode("walk");
                        rbWalk.setTextColor(Color.parseColor("#be3e82"));
                        rbFree.setTextColor(Color.parseColor("#8e665899"));
                        rbInterval.setTextColor(Color.parseColor("#8e665899"));
                        break;
                    case R.id.radioIw:
                        Log.d(TAG, "onCheckedChanged: interval"+checkedId);
                        ((MainActivity)getActivity()).setMainMode("interval");
                        rbInterval.setTextColor(Color.parseColor("#be3e82"));
                        rbFree.setTextColor(Color.parseColor("#8e665899"));
                        rbWalk.setTextColor(Color.parseColor("#8e665899"));
                        break;
                    case R.id.radioFwork:
                        Log.d(TAG, "onCheckedChanged: free workout"+checkedId);
                        ((MainActivity)getActivity()).setMainMode("free");
                        rbFree.setTextColor(Color.parseColor("#be3e82"));
                        rbWalk.setTextColor(Color.parseColor("#8e665899"));
                        rbInterval.setTextColor(Color.parseColor("#8e665899"));

                        break;
                }
            }
        });


        return myView;
    }

}