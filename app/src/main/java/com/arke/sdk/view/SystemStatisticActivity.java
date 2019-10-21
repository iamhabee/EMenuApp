package com.arke.sdk.view;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.arke.sdk.R;
import com.smartpos.epay.SystemStat;

import java.util.ArrayList;

public class SystemStatisticActivity extends BaseActivity {
    private SystemStat mSystemStat;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_statistic);
        
        try {
            mSystemStat = new SystemStat();
        } catch (SystemStat.NoSupportException e) {
            mSystemStat = null;
        }

        ListView list = (ListView) findViewById(R.id.list);
        mAdapter = new MyAdapter(this, null);
        list.setAdapter(mAdapter);

        if(mSystemStat != null){
            try {
                ArrayList<SystemStat.StatisticsInfo> info=new ArrayList<SystemStat.StatisticsInfo>();
                for(SystemStat.StatisticsInfo item:mSystemStat.getAllStatisticsExt()) {
                    if(!TextUtils.isEmpty(item.value))
                    {
                        info.add(item);
                    }
                }
                mAdapter.setInfo(info);
                mAdapter.notifyDataSetChanged();
            } catch (SystemStat.NoSupportException e) {
            }
        }

    }
    
    private class MyAdapter extends BaseAdapter {
        private Context mContext;
        ArrayList<SystemStat.StatisticsInfo> mInfo;
        private LayoutInflater mInflater;

        public MyAdapter(Context context, ArrayList<SystemStat.StatisticsInfo> info) {
            mContext = context;
            mInfo = info;
            mInflater = LayoutInflater.from(context);
        }
        
        public void setInfo(ArrayList<SystemStat.StatisticsInfo> info){
            mInfo = info;
        }

        public int getCount() {
            return mInfo == null ? 0 : mInfo.size();
        }

        public Object getItem(int position) {
            return mInfo == null ? null: mInfo.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item, null);
            }
            SystemStat.StatisticsInfo info = mInfo.get(position);
            //((TextView)convertView.findViewById(R.id.name)).setText("name: "+info.name);
            ((TextView)convertView.findViewById(R.id.displayName)).setText(getString(R.string.statistic_item)+": "+info.displayName);
//            ((TextView)convertView.findViewById(R.id.tagNo)).setText("tagNo: "+info.tagNo);
//            ((TextView)convertView.findViewById(R.id.factorNo)).setText("factorNo: "+info.factorNo);
            ((TextView)convertView.findViewById(R.id.value)).setText(getString(R.string.value)+": "+info.value);
            return convertView;
        }
    }
}
