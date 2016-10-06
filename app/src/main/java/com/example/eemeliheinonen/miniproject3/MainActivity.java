package com.example.eemeliheinonen.miniproject3;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private String polarinMac = "00:22:D0:32:F6:6A";
    private String polarinUUID = "0000180d-0000-1000-8000-00805f9b34fb";
    private String heartRateUUID = "00002A37-0000-1000-8000-00805F9B34FB";
    private int format = BluetoothGattCharacteristic.FORMAT_UINT8;
    private BluetoothGattCharacteristic characteristic;
    private int heartRate;

    private final static int UPDATE_DEVICE = 0;
    private final static int UPDATE_VALUE = 1;
    private final Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            final int what = msg.what;
            final String value = (String) msg.obj;
            switch(what) {
                case UPDATE_DEVICE: updateDevice(value); break;
                case UPDATE_VALUE: updateValue(value); break;
            }
        }
    };

    private void updateDevice(String devName){
        TextView t=(TextView)findViewById(R.id.tv2);
        t.setText(devName);
    }

    private void updateValue(String value){
        TextView t=(TextView)findViewById(R.id.tv3);
        t.setText(value);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.fixed_tabs);
        tabLayout.setupWithViewPager(viewPager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.act_title);
        toolbar.inflateMenu(R.menu.menu);

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) { int id = item.getItemId();
                return true; }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
                ScanFilter scanFilterService = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(polarinUUID)).build();
                filters.add(scanFilterService);
            }
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        Log.d("lol","scanledevice alkaa");
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        Log.d("lol","scanledevice stopscan");
                        mLEScanner.stopScan(mScanCallback);

                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                Log.d("lol","ennen startscan");
                mLEScanner.startScan(filters, settings, mScanCallback);
                Log.d("lol","jälkeen startscannin");
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType lol", String.valueOf(callbackType));
            Log.i("result lol", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            Log.d("lol","ennen connectdevice");
            connectToDevice(btDevice);
            Log.d("lol","jälkeen connectdevicen");
            Log.d("lol",btDevice.toString());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult Results lol", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            Log.d("lol","connected, stop scan");
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");

                    //update UI
                    Message msg = Message.obtain();
                    String deviceName = gatt.getDevice().getName();
                    msg.obj = deviceName;
                    msg.what = 0;
                    msg.setTarget(uiHandler);
                    msg.sendToTarget();

                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
           /* for (BluetoothGattService service : services) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics){
                    Log.d("lol",characteristic.getIntValue(-1, 1).toString());
                }
            }*/
            for (BluetoothGattService service : services) {
                Log.d("lollero", "for ennen if");
                if (service.getUuid().toString().equals(polarinUUID)) {
                    Log.d("lollero","uuid equald polarinuuid");
                    // Found heart rate service
                    //  gatt.readCharacteristic(service.getCharacteristic());
                    Log.d("tässä tulee hruuid", UUID.fromString(heartRateUUID).toString());
                    //gatt.readCharacteristic(service.getCharacteristic(UUID.fromString(heartRateUUID)));
                    Log.d("lollero size",""+service.getCharacteristics().size());
                    gatt.readCharacteristic(service.getCharacteristics().get(0));
                    characteristic = service.getCharacteristics().get(0);
                    mGatt.setCharacteristicNotification(characteristic, true);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                            UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mGatt.writeDescriptor(descriptor);
                    break;
                }
            }


            //gatt.readCharacteristic(services.get(5).getCharacteristics().get(0));

            // Log.d("lollero 1", gatt.readCharacteristic(services.get(3).getCharacteristics());
            //Log.d("lol",services.get(3).getCharacteristics().get(0).getIntValue(-1, 1).toString());
        }



        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.d("onCharacteristicRead", characteristic.toString());
            //Log.d("lol getintvalue",characteristic.getIntValue(-1,1).toString());
            Log.d("lollero", characteristic.getUuid().toString());

            heartRate = characteristic.getIntValue(format, 1);
            Log.d("lollero", String.format("Received heart rate: %d", heartRate));

            gatt.disconnect();
        }

        @Override
// Characteristic notification
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            heartRate = characteristic.getIntValue(format, 1);
            String hr = ""+heartRate;
            Log.d("lollero notif",hr);

            //update UI
            Message msg = Message.obtain();
            msg.obj = hr;
            msg.what = 1;
            msg.setTarget(uiHandler);
            msg.sendToTarget();
        }


    };
}
