package com.arke.sdk.converters;

import com.arke.sdk.companions.Globals;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.converter.TypeConverter;

public class OrderPaymentStatusTypeConverter extends TypeConverter<String, Globals.OrderPaymentStatus> {

    private Gson getGSon() {
        return new Gson();
    }

    @Override
    public String getDBValue(Globals.OrderPaymentStatus model) {
        Gson gson = getGSon();
        return gson.toJson(model, Globals.OrderPaymentStatus.class);
    }

    @Override
    public Globals.OrderPaymentStatus getModelValue(String data) {
        Gson gson = getGSon();
        return gson.fromJson(data, Globals.OrderPaymentStatus.class);
    }

}
