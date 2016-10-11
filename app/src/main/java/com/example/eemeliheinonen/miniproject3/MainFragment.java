package com.example.eemeliheinonen.miniproject3;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainFragment extends Fragment {
    private static final String ARG_PARAM1 = "txt";
    private String dTxt;
    private TextView tv1;
    private String TAG = "jeee mainfragment";
    private RadioGroup radioButtonGroup;


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
                        break;
                    case R.id.radioIw:
                        Log.d(TAG, "onCheckedChanged: interval"+checkedId);
                        break;
                    case R.id.radioFwork:
                        Log.d(TAG, "onCheckedChanged: free workout"+checkedId);
                        break;
                }
            }
        });


        return myView;
    }

}