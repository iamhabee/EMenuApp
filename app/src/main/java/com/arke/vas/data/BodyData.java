package com.arke.vas.data;


/**
 * Message body information, specific business data
 * <p>
 * 消息体信息，具体业务数据
 */
public class BodyData {

    /**
     * Value added service order number
     * <p>
     * 增值应用流水
     */
    private String orderNumber;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

}

