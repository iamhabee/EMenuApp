package com.arke.sdk.converters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.raizlabs.android.dbflow.converter.TypeConverter;

import java.lang.reflect.Type;
import java.util.List;

public class StringListTypeConverter extends TypeConverter<String, List<String>> {

    private Gson getGSon() {
        return new Gson();
    }

    @Override
    public String getDBValue(List<String> model) {
        Gson gson = getGSon();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return gson.toJson(model, listType);
    }

    @Override
    public List<String> getModelValue(String data) {
        Gson gson = getGSon();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return gson.fromJson(data, listType);
    }

}
