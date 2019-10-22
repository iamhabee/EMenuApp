package com.arke.sdk.beans;

import com.arke.sdk.eventbuses.CardProcessorEvent;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.utilities.EMenuLogger;
import com.google.gson.Gson;

public class CurrentCardPaymentProcessor {
    private EMenuOrder eMenuOrder;
    private String[] customerKeys;

    public CurrentCardPaymentProcessor(EMenuOrder eMenuOrder, String... customerKeys) {
        this.eMenuOrder = eMenuOrder;
        this.customerKeys = customerKeys;
    }

    public static CurrentCardPaymentProcessor getLastProcessing() {
        return new Gson().fromJson(AppPrefs.getCurrentCardData(),CurrentCardPaymentProcessor.class);
    }

    public EMenuOrder getEMenuOrder() {
        return eMenuOrder;
    }

    public String[] getCustomerKeys() {
        return customerKeys;
    }

    public CurrentCardPaymentProcessor serializeAndPersist() {
        String data = new Gson().toJson(this, CurrentCardPaymentProcessor.class);
        EMenuLogger.d(CardProcessorEvent.class.getSimpleName(), "PersistableData\n" + data);
        AppPrefs.persistCurrentCardProcessorData(data);
        return this;
    }

}
