package com.arke.sdk.converters;

import com.arke.sdk.companions.Globals;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.converter.TypeConverter;

public class OrderProgressStatusTypeConverter extends TypeConverter<String, Globals.OrderProgressStatus> {

    private Gson getGSon() {
        return new Gson();
    }

    @Override
    public String getDBValue(Globals.OrderProgressStatus model) {
        Gson gson = getGSon();
        return gson.toJson(model, Globals.OrderProgressStatus.class);
    }

    @Override
    public Globals.OrderProgressStatus getModelValue(String data) {
        Gson gson = getGSon();
        return gson.fromJson(data, Globals.OrderProgressStatus.class);
    }

}
