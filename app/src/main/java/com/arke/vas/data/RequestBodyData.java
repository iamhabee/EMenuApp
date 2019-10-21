package com.arke.vas.data;

/**
 * Request message body information, specific request data
 * <p>
 * 请求消息体信息，具体请求数据
 */
public class RequestBodyData extends BodyData {

    /**
     * Transaction amount
     * <p>
     * 交易金额
     */
    private Double amount;

    /**
     * When the value is true that by the value added service to print a small ticket, when the value is false or does not exist that by the receipt application to print
     * <p>
     * 当该值为true表示由增值应用打小票，当该值为false或者不存在时表示由收单应用打小票
     */
    private Boolean needAppPrinted;

    /**
     * Original transaction voucher number
     * <p>
     * 原交易凭证号
     */
    private String originalVoucherNumber;

    /**
     * Value added service need to print the note information
     * <p>
     * 增值服务需要打印的备注信息
     */
    private String inputRemarkInfo;

    /**
     * Original transaction reference number
     * <p>
     * 原交易参考号
     */
    private String originalReferenceNumber;

    /**
     * Card number
     * <p>
     * 卡号
     */
    private String cardNumber;

    /**
     * Expiry date
     * <p>
     * 有效期
     */
    private String expiryDate;
    /**
     * Authorization method
     * <p>
     * 授权方式
     */
    private String authorizationMethod;
    /**
     * Authorization code
     * <p>
     * 授权码
     */
    private String authorizationCode;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Boolean getNeedAppPrinted() {
        return needAppPrinted;
    }

    public void setNeedAppPrinted(Boolean needAppPrinted) {
        this.needAppPrinted = needAppPrinted;
    }

    public String getOriginalVoucherNumber() {
        return originalVoucherNumber;
    }

    public void setOriginalVoucherNumber(String originalVoucherNumber) {
        this.originalVoucherNumber = originalVoucherNumber;
    }

    public String getInputRemarkInfo() {
        return inputRemarkInfo;
    }

    public void setInputRemarkInfo(String inputRemarkInfo) {
        this.inputRemarkInfo = inputRemarkInfo;
    }

    public String getOriginalReferenceNumber() {
        return originalReferenceNumber;
    }

    public void setOriginalReferenceNumber(String originalReferenceNumber) {
        this.originalReferenceNumber = originalReferenceNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getAuthorizationMethod() {
        return authorizationMethod;
    }

    public void setAuthorizationMethod(String authorizationMethod) {
        this.authorizationMethod = authorizationMethod;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
}
