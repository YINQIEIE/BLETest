package com.yq.bletest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    String read_phone_state = Manifest.permission.READ_PHONE_STATE;

    private TextView tv_rate;
    private Button btn_read;
    private RecyclerView rv_device;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private BleDeviceAdapter adapter;

    Set<String> addressSet = new HashSet<>();
    private BluetoothAdapter bleAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    BluetoothDevice currentDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_rate = findViewById(R.id.tv_rate);
        btn_read = findViewById(R.id.btn_read);
        rv_device = findViewById(R.id.rv_devices);
        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothGatt != null && bluetoothGatt.connect() && characteristic != null) {
                    bluetoothGatt.setCharacteristicNotification(characteristic, true);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(makeUuid(0x2902));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    boolean result = bluetoothGatt.writeDescriptor(descriptor);
//                    bluetoothGatt.readCharacteristic(characteristic);
                } else
                    Toast.makeText(MainActivity.this, "未连接成功！", Toast.LENGTH_LONG).show();
            }
        });
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

        Log.i("uuid", makeUuid(0x2902).toString());
    }

    public void startConnect(BluetoothDevice device) {
        currentDevice = device;
        bluetoothGatt = device.connectGatt(this, false, bleGattCallback);
    }

    private BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
                btn_read.setText("连接成功，点击测量心率");
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.i("ble", "disconnect");
                btn_read.setText("连接已断开");
                gatt.close();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                List<BluetoothGattService> services = gatt.getServices();
//                for (BluetoothGattService service : services) {
//                    Log.i("ble_service ", service.getUuid().toString() + service.getType());
//                }
                UUID serviceUuid = makeUuid(0x1814);
                BluetoothGattService service = gatt.getService(serviceUuid);
                if (null != service)
                    characteristic = service.getCharacteristic(makeUuid(0x2A53));
            }


        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            Log.i("onChanged:heart  = ", (value[16] & 255) + "");
            tv_rate.setText(String.format("当前心率：%1$d,当前步数：%2$d", (value[16] & 0xFF), ((value[3] & 255) * 256) + (value[2] & 255)));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            byte[] value = descriptor.getValue();
            Log.i("onDescriptor:heart  = ", (value[16] & 255) + "");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            byte[] value = characteristic.getValue();
            Log.i("onRead:heart  = ", (value[16] & 255) + "");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };

    String generUuid = "0000%04X-0000-1000-8000-00805f9b34fb";

    private UUID makeUuid(int shortUuid) {
        return UUID.fromString(String.format(generUuid, shortUuid));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleAdapter.stopLeScan(leScanCallback);
        if (null != bluetoothGatt && bluetoothGatt.connect()) {
            bluetoothGatt.setCharacteristicNotification(characteristic, false);
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
