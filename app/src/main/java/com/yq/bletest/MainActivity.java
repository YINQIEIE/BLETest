package com.yq.bletest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    String read_phone_state = Manifest.permission.READ_PHONE_STATE;

    private RecyclerView rv_device;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private BleDeviceAdapter adapter;

    Set<String> addressSet = new HashSet<>();
    private BluetoothAdapter bleAdapter;
    private BluetoothGatt bluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv_device = findViewById(R.id.rv_devices);
        adapter = new BleDeviceAdapter(this, deviceList);
        rv_device.setLayoutManager(new LinearLayoutManager(this));
        rv_device.setAdapter(adapter);
//        ActivityCompat.requestPermissions(this, permissions, 110);

        BluetoothManager bleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();
//        BluetoothAdapter.getDefaultAdapter();
        if (null != bleAdapter && !bleAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
//        bleAdapter.getBluetoothLeScanner();
        bleAdapter.startLeScan(leScanCallback);


    }

    BluetoothDevice currentDevice;

    public void startConnect(BluetoothDevice device) {
        currentDevice = device;
        bluetoothGatt = device.connectGatt(this, false, bleGattCallback);
    }

    private BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.i("ble", "disconnect");
                gatt.close();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    Log.i("ble_service ", service.getUuid().toString() + service.getType());
                }
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleAdapter.stopLeScan(leScanCallback);
        if (null != bluetoothGatt && bluetoothGatt.connect()) {
            bluetoothGatt.disconnect();
        }
    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!addressSet.contains(device.getAddress())) {
                addressSet.add(device.getAddress());
                deviceList.add(device);
                Log.i("ble_device", "name = " + device.getName() + ";address = " + device.getAddress() + ";rssi = " + rssi);
                adapter.notifyDataSetChanged();
            }
        }
    };
}
