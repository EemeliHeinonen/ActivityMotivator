package com.example.eemeliheinonen.miniproject3;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;



public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;


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

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) { int id = item.getItemId();
                return true; }

        });

    }
}
