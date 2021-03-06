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
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private String polarinUUID = "0000180d-0000-1000-8000-00805f9b34fb";
    private String heartRateUUID = "00002A37-0000-1000-8000-00805F9B34FB";
    private int format = BluetoothGattCharacteristic.FORMAT_UINT8;
    private BluetoothGattCharacteristic characteristic;
    private int heartRate;
    private GoogleApiClient gac;
    private Location loc;
    private String TAG = "jeee";
    private LocationRequest locReq;
    private String mainMode = "walk"; // walk or interval or free
    private Toast toastRest;
    private Toast toastBurstStart;
    private Toast toastWorkHarder;


    private int intSpeed; //User's speed will be set to this.
    int walkingSpeedCount = 0; // how many seconds of continious walking
    int intervalBurstCount = 0; // how many seconds of continious interval training
    int startMotivatingAfter = 10; // how many seconds it takes to start sending motivational notifications when walking
    int initialRestLength = 15;  // Sets the length of Interval workouts initial rest period in seconds.
    boolean initialResting = true;
    boolean postInitialResting = false;
    boolean freeWorkoutActive = false;
    boolean walking = false; //toggling walking mode
    int burstLenght = 30; // user set duration for interval burst
    int intervalRestCount = 0;
    int age = 26; // users age
    int maxHr = 220-age; // calculated max HR
    int walkingBeat_min = (int) (0.4* maxHr); // minimum HR for optimal walking
    int runningBeat_min = (int) (0.7*maxHr);
    int zone1_max = (int) (0.6*maxHr); // max HR for zone1

    private final static int UPDATE_DEVICE = 0;
    private final static int UPDATE_VALUE = 1;
    private final Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            final int what = msg.what;
            final String value = (String) msg.obj;
            switch (what) {
                case UPDATE_DEVICE:
                    updateDevice(value);
                    break;
                case UPDATE_VALUE:
                    updateValue(value);
                    break;
            }
        }
    };

    private void updateDevice(String devName) {
    }

    private void updateValue(String value) {
        TextView t = (TextView) findViewById(R.id.tv3);
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
        toastRest = Toast.makeText(getApplicationContext(), "Burst over, good job!", Toast.LENGTH_LONG);
        toastBurstStart = Toast.makeText(getApplicationContext(), "Burst started, Go fast!", Toast.LENGTH_LONG);
        toastWorkHarder = Toast.makeText(getApplicationContext(), "Pick up the pace!", Toast.LENGTH_LONG);


        gac = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locReq = new LocationRequest();
        locReq.setInterval(1000);
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
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
        Log.d("lol", "scanledevice alkaa");
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        Log.d("lol", "scanledevice stopscan");
                        mLEScanner.stopScan(mScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                Log.d("lol", "ennen startscan");
                mLEScanner.startScan(filters, settings, mScanCallback);
                Log.d("lol", "jälkeen startscannin");
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
            connectToDevice(btDevice);
            Log.d("lol", btDevice.toString());
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
            Log.d("lol", "connected, stop scan");
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

            for (BluetoothGattService service : services) {
                Log.d("lollero", "for ennen if");
                if (service.getUuid().toString().equals(polarinUUID)) {
                    Log.d("lollero", "uuid equald polarinuuid");
                    // Found heart rate service
                    Log.d("tässä tulee hruuid", UUID.fromString(heartRateUUID).toString());
                    Log.d("lollero size", "" + service.getCharacteristics().size());
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
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.d("onCharacteristicRead", characteristic.toString());
            heartRate = characteristic.getIntValue(format, 1);
            Log.d("lollero", String.format("Received heart rate: %d", heartRate));
            gatt.disconnect();
        }

        @Override
        // Characteristic notification
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            heartRate = characteristic.getIntValue(format, 1);
            String hr = "" + heartRate;
            Log.d("lollero notif", hr);

            //update UI
            Message msg = Message.obtain();
            msg.obj = hr;
            msg.what = 1;
            msg.setTarget(uiHandler);
            msg.sendToTarget();

            startTraining();
        }

    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        loc = LocationServices.FusedLocationApi.getLastLocation(gac);
        loc.getSpeed();
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                gac, locReq, this);
    }


    @Override
    public void onLocationChanged (Location location){
        Log.d(TAG, "onLocationChanged: ");
        float s = location.getSpeed();
        intSpeed = (int) (s*3.6);
        Log.d(TAG, String.valueOf(intSpeed + "  "+ heartRate));
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        gac.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        gac.disconnect();
        super.onStop();
    }

    public void setAge(int i){
        this.age = i;
        Log.d(TAG, "setAge: "+age);
    }

    public void setBurstLenght(int i){
        this.burstLenght = i;
        Log.d(TAG, "setBurstLenght: "+this.burstLenght);
    }

    public void setMainMode(String s){
        mainMode = s;
    }
    
    public void beeb(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startTraining(){

        if(mainMode == "walk"){
            Log.d(TAG, "onLocationChanged: main mode on WALK");

            if(intSpeed < 16 && intSpeed > 2){
                walking = true;
            }
            else{
                walking = false;
            }

            if(walking){
                walkingSpeedCount++;
            }
            else{
                walkingSpeedCount = 0;
            }

            if(walking && heartRate < walkingBeat_min && walkingSpeedCount >= startMotivatingAfter){
                beeb();
                toastWorkHarder.show();
            }
            else if(!walking){
            }
            else {

            }
        }

        else if(mainMode == "interval"){
            Log.d(TAG, "onLocationChanged: main mode on INTERVAL");

            if(initialResting){
                intervalRestCount++;
                if(intervalRestCount==initialRestLength){
                    initialResting = false;
                    beeb();
                    intervalRestCount = 0;
                    toastBurstStart.show();
                }
            }

            else if(!initialResting && !postInitialResting){
                intervalBurstCount++;
                if(intervalBurstCount >= burstLenght){
                    intervalBurstCount = 0;
                    beeb();
                    postInitialResting = true;
                    toastRest.show();
                }
            }
            else if(postInitialResting){
                if(heartRate<= zone1_max){
                    beeb();
                    postInitialResting = false;
                    toastBurstStart.show();
                }
            }

        }

        else if(mainMode == "free"){
            Log.d(TAG, "onLocationChanged: main mode on FREE");
            if(heartRate >= 90){ // Tähän runningBeat_min
                freeWorkoutActive = true;
            }

            if(heartRate < 90 && freeWorkoutActive){
                beeb();
                toastWorkHarder.show();
            }

            else if(heartRate >= runningBeat_min){
                //tvMotivation.setText("hyvin menee");
            }
        }
    }

}
