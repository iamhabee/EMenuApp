package com.arke.vas.data;


/**
 * Response to message body information, specific response data
 * <p>
 * 响应消息体信息，具体响应数据
 */
public class ResponseBodyData extends BodyData {

    /**
     * Transaction type, interface type
     * <p>
     * 接口Id
     */
    private String interfaceId;

    /**
     * Transaction response code
     * <p>
     * 交易返回码
     */
    private int responseCode = 0;

    /**
     * Transaction response information
     * <p>
     * 交易返回信息
     */
    private String responseMessage;

    /**
     * Transaction type.
     * <p>
     * 交易类型
     */
    private String transactionType;

    /**
     * Package name
     * <p>
     * 包名
     */
    private String packageName;

    /**
     * Field 39 response code
     * <p>
     * 39域返回码
     */
    private String responseCodeThirtyNine;

    /**
     * Field 39 response information
     * <p>
     * 39域返回信息
     */
    private String responseMessageThirtyNine;

    /**
     * Transaction amount
     * <p>
     * 交易金额
     */
    private Double amount;

    /**
     * Merchant Name
     * <p>
     * 商户名称
     */
    private String merchantName;

    /**
     * Merchant ID
     * <p>
     * 商户编号
     */
    private String merchantNumber;

    /**
     * Terminal number
     * <p>
     * 终端号
     */
    private String terminalNumber;

    /**
     * Operator number
     * <p>
     * 操作员号
     */
    private String operatorNumber;

    /**
     * Card number
     * <p>
     * 卡号
     */
    private String cardNumber;

    /**
     * Expire transactionDate of card.
     * <p>
     * 卡有效期
     */
    private String expirationDate;

    /**
     * Batch number
     * <p>
     * 批次号
     */
    private String batchNumber;
    /**
     * Voucher number
     * <p>
     * 凭证号
     */
    private String voucherNumber;
    /**
     * Reference number
     * <p>
     * 参考号
     */
    private String referenceNumber;
    /**
     * Authorization code
     * <p>
     * 授权码
     */
    private String authCode;
    /**
     * Transaction date
     * <p>
     * 交易日期
     */
    private String transactionDate;

    /**
     * Transaction time
     * <p>
     * 交易时间
     */
    private String transactionTime;

    /**
     * Has voided
     * <p>
     * 是否被撤销
     */
    private Boolean voided = null;

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getResponseCodeThirtyNine() {
        return responseCodeThirtyNine;
    }

    public void setResponseCodeThirtyNine(String responseCodeThirtyNine) {
        this.responseCodeThirtyNine = responseCodeThirtyNine;
    }

    public String getResponseMessageThirtyNine() {
        return responseMessageThirtyNine;
    }

    public void setResponseMessageThirtyNine(String responseMessageThirtyNine) {
        this.responseMessageThirtyNine = responseMessageThirtyNine;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantNumber() {
        return merchantNumber;
    }

    public void setMerchantNumber(String merchantNumber) {
        this.merchantNumber = merchantNumber;
    }

    public String getTerminalNumber() {
        return terminalNumber;
    }

    public void setTerminalNumber(String terminalNumber) {
        this.terminalNumber = terminalNumber;
    }

    public String getOperatorNumber() {
        return operatorNumber;
    }

    public void setOperatorNumber(String operatorNumber) {
        this.operatorNumber = operatorNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public Boolean getVoided() {
        return voided;
    }

    public void setVoided(Boolean voided) {
        this.voided = voided;
    }
}
