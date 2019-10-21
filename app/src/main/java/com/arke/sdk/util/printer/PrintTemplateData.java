package com.arke.sdk.util.printer;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.arke.sdk.ArkeSdkDemoApplication;
import com.arke.sdk.R;

/**
 * PrintTemplateData
 * Created by Bear on 2017/6/16.
 */

public class PrintTemplateData {
    //condition
    private boolean playEnglishFlag;

    private Context context = ArkeSdkDemoApplication.getContext();

    // Label
    public String MERCHANT_NAME = context.getString(R.string.merchant_name);
    public String MERCHANT_NO = context.getString(R.string.merchant_no);
    public String TERMINAL_NO = context.getString(R.string.terminal_no);
    public String OPERATOR_NO = context.getString(R.string.operator_no);
    public String ACQUIRER = context.getString(R.string.acquirer);
    public String ISSUER = context.getString(R.string.issuer);

    public String CARD_NO = context.getString(R.string.card_no);
    public String EXPIRY_DATE = context.getString(R.string.expiry_date);
    public String TRANSACTION_TYPE = context.getString(R.string.trans_type);
    public String BATCH_NO = context.getString(R.string.batch_no);
    public String DATE_TIME = context.getString(R.string.date_time);
    public String AMOUNT = context.getString(R.string.amount);

    public boolean isPlayEnglishFlag() {
        return playEnglishFlag;
    }

    public String toJSONString(){
        return JSON.toJSONString(this);
    }
}
