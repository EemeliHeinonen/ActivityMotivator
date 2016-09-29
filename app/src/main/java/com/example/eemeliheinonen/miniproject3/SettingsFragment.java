package com.example.eemeliheinonen.miniproject3;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


public class SettingsFragment extends ListFragment {
    private static final String ARG_PARAM1 = "txt";
    private String dTxt;
    private BluetoothLeScanner mLeScanner;
    private boolean mScanning;
    private Handler mHandler;



    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


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
            BluetoothSingleton bluetoothSingletonn = BluetoothSingleton.getInstance();
            mLeScanner = bluetoothSingletonn.getAdapter().getBluetoothLeScanner();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout myView =(LinearLayout) inflater.inflate(R.layout.fragment_main, container, false);
        TextView tv1 = (TextView) myView.findViewById(R.id.tv1);
        tv1.setText(dTxt);
        return myView;
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private void scanLeDevice(final boolean enable) {
        System.out.println("ScanLeDevice metodin sisällä.");
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.out.println("scanledevice-> run");
                    mScanning = false;
                    mLeScanner.stopScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mLeScanner.startScan(mLeScanCallback);
            System.out.println("Ennen mLeScanCallback.toString printtiä");
            System.out.println(mLeScanCallback.toString());
        } else {
            mScanning = false;
            mLeScanner.stopScan(mLeScanCallback);
        }
    }


}

