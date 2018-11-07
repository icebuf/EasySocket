package com.skyworth.led.easyclient.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skyworth.led.easyclient.R;
import com.skyworth.led.easyclient.bean.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Ice Nation
 * 日期：2018/11/6 17:53
 * 邮箱：tangjie@skyworth.com
 */
public class TestCaseAdapter extends BaseAdapter {

    private Context mContext;
    private List<TestCase> mDataList;

    public TestCaseAdapter(Context context){
        this.mContext = context;
        mDataList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public TestCase getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            convertView = View.inflate(mContext, R.layout.item_test_case,null);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.tv_test_case_name);
            holder.detail = convertView.findViewById(R.id.tv_test_case_detail);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(getItem(position).name);
        holder.detail.setText(getItem(position).detail);
        return convertView;
    }

    public void replaceData(List<TestCase> testCaseList) {
        if(testCaseList == null)
            return;
        mDataList.clear();
        mDataList.addAll(testCaseList);
    }

    private class ViewHolder{
        private TextView name;
        private TextView detail;
    }
}
