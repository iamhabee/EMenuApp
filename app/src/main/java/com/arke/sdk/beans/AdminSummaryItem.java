package com.arke.sdk.beans;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class AdminSummaryItem {
    private int id;
    private int summaryIcon;
    private String summaryTitle;
    private String summaryDescription;
    private String priceTag;

    public AdminSummaryItem(int id, String summaryTitle, String summaryDescription, int summaryIcon) {
        this.id = id;
        this.summaryTitle = summaryTitle;
        this.summaryDescription = summaryDescription;
        this.summaryIcon = summaryIcon;
    }

    public String getPriceTag() {
        return priceTag;
    }

    public void setPriceTag(String priceTag) {
        this.priceTag = priceTag;
    }

    public void setSummaryTitle(String summaryTitle) {
        this.summaryTitle = summaryTitle;
    }

    public void setSummaryDescription(String summaryDescription) {
        this.summaryDescription = summaryDescription;
    }

    public int getSummaryIcon() {
        return summaryIcon;
    }

    public String getSummaryDescription() {
        return summaryDescription;
    }

    public String getSummaryTitle() {
        return summaryTitle;
    }

    public int getId() {
        return id;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this.id, ((AdminSummaryItem) obj).getId());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
                append(summaryTitle).
                append(summaryDescription).
                append(priceTag).
                toHashCode();
    }

}
