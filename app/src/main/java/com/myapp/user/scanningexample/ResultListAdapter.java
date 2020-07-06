package com.myapp.user.scanningexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ResultListAdapter extends BaseAdapter {
    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    List<InfoDeviceList> resultList;

    public ResultListAdapter(Context newContext, List <InfoDeviceList> newResultList){
        mContext = newContext;
        resultList = newResultList;
        mLayoutInflater = LayoutInflater.from(mContext);
    }


    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Object getItem(int i) {
        return resultList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View view = mLayoutInflater.inflate(R.layout.result_list_view, null);

        TextView deviceName = (TextView) view.findViewById(R.id.device_name);
        TextView deviceMac = (TextView) view.findViewById(R.id.device_mac);
        TextView deviceRssi = (TextView) view.findViewById(R.id.device_rssi);

        deviceName.setText("NAME : " + resultList.get(i).getDeviceName());
        deviceMac.setText("MAC : " + resultList.get(i).getDeviceMac());
        deviceRssi.setText("RSSI : " + resultList.get(i).getDeviceRssi());

        return view;
    }
}
