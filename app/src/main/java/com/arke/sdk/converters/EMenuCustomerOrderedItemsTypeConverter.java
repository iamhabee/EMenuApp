package com.arke.sdk.converters;

import com.arke.sdk.models.EMenuItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.raizlabs.android.dbflow.converter.TypeConverter;

import java.lang.reflect.Type;
import java.util.List;

public class EMenuCustomerOrderedItemsTypeConverter extends TypeConverter<String, List<EMenuItem>> {

    private Gson getGSon() {
        return new Gson();
    }

    @Override
    public String getDBValue(List<EMenuItem> model) {
        Gson gson = getGSon();
        Type eMenuOrdersType = new TypeToken<List<EMenuItem>>() {
        }.getType();
        return gson.toJson(model, eMenuOrdersType);
    }

    @Override
    public List<EMenuItem> getModelValue(String data) {
        Gson gson = getGSon();
        Type eMenuOrdersType = new TypeToken<List<EMenuItem>>() {
        }.getType();
        return gson.fromJson(data, eMenuOrdersType);
    }

}
