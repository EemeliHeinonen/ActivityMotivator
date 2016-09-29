package com.example.eemeliheinonen.miniproject3;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

public class BluetoothSingleton extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private static BluetoothSingleton singleton = new BluetoothSingleton( );

    /* A private Constructor prevents any other
  * class from instantiating.
  */
    private BluetoothSingleton() {
        // Initializes Bluetooth adapter.
        bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    /* Static 'instance' method */
    public static BluetoothSingleton getInstance( ) {
        return singleton;
    }

    public BluetoothAdapter getAdapter(){
        return this.mBluetoothAdapter;
    }

    public BluetoothManager getManager(){
        return this.bluetoothManager;
    }
}
