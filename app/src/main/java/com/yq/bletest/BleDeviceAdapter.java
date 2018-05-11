package com.yq.bletest;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ItemHolder> {

    private Context mContext;
    private List<BluetoothDevice> data;

    public BleDeviceAdapter(Context mContext, List<BluetoothDevice> data) {
        this.mContext = mContext;
        this.data = data;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_list_device, parent, false);
        return new ItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        BluetoothDevice bluetoothDevice = data.get(position);
        holder.tv_name.setText(bluetoothDevice.getName());
        holder.tv_address.setText(bluetoothDevice.getAddress());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ItemHolder extends RecyclerView.ViewHolder {

        TextView tv_name;
        TextView tv_address;

        public ItemHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_address = itemView.findViewById(R.id.tv_address);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) mContext).startConnect(data.get(getAdapterPosition()));
                }
            });
        }
    }


}
