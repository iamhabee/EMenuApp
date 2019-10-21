package com.arke.sdk.util.printer;

/**
 * PayTemplateData
 * Created by Bear on 2017/6/16.
 */

public class PayTemplateData extends PrintTemplateData {
    // Print data
    private String merchantName;
    private String merchantNo;
    private String terminalNo;
    private String operatorNo;
    private String acquirer;
    private String issuer;
    private String payTraceAuditNo;
    private String cardNo;
    private String expiryDate;
    private String transTypePrint;
    private String batchNo;
    private String voucherNo;
    private String authCode;
    private String referenceNo;
    private String dateTime;
    private String desker;
    private String amtTrans;
    private String discount;
    private String prevVoucherNo;
    private String prevRetrievalRefNo;
    private String prevTransDate;
    private String prevAuthCode;
    private String installmentPeriod;
    private String chargeCurrencyCode;
    private String initialInstallmentPayment;
    private String installmentCharge;
    private String initialInstallmentCharge;
    private String installmentPerCharge;
    private String rewardPoints;
    private String itemCode;
    private String exchangePoints;
    private String pointsBanlance;
    private String pointsOutstandingAmt;
    private String mobileNo;
    private String intoAcc;
    private String balance;
    private String appCryptogramType;
    private String appCryptogram;
    private String termVerificationResult;
    private String panSeqNo;
    private String appId;
    private String appTransCounter;
    private String appStatusInfo;
    private String appName;
    private String appLable;
    private String unpredictableNo;
    private String appInterchangerProfile;
    private String cvmResults;
    private String issuerAppData;
    private String termCap;
    private String cardProductId;
    private String issuerMsg;
    private String cpuMsg;
    private String acquireMsg;
    private String noSignText;
    private String signImage;
    private String copyType;
    private String hotline;

    // other condition
    private boolean printTipFlag;
    private String chargeMethod;
    private boolean icTagFlag;
    private boolean reprintFlag;
    private boolean shouldSign;
    private boolean shouldDeclare;

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getOperatorNo() {
        return operatorNo;
    }

    public void setOperatorNo(String operatorNo) {
        this.operatorNo = operatorNo;
    }

    public String getAcquirer() {
        return acquirer;
    }

    public void setAcquirer(String acquirer) {
        this.acquirer = acquirer;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getPayTraceAuditNo() {
        return payTraceAuditNo;
    }

    public void setPayTraceAuditNo(String payTraceAuditNo) {
        this.payTraceAuditNo = payTraceAuditNo;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getTransTypePrint() {
        return transTypePrint;
    }

    public void setTransTypePrint(String transTypePrint) {
        this.transTypePrint = transTypePrint;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDesker() {
        return desker;
    }

    public void setDesker(String desker) {
        this.desker = desker;
    }

    public String getAmtTrans() {
        return amtTrans;
    }

    public void setAmtTrans(String amtTrans) {
        this.amtTrans = amtTrans;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getPrevVoucherNo() {
        return prevVoucherNo;
    }

    public void setPrevVoucherNo(String prevVoucherNo) {
        this.prevVoucherNo = prevVoucherNo;
    }

    public String getPrevRetrievalRefNo() {
        return prevRetrievalRefNo;
    }

    public void setPrevRetrievalRefNo(String prevRetrievalRefNo) {
        this.prevRetrievalRefNo = prevRetrievalRefNo;
    }

    public String getPrevTransDate() {
        return prevTransDate;
    }

    public void setPrevTransDate(String prevTransDate) {
        this.prevTransDate = prevTransDate;
    }

    public String getPrevAuthCode() {
        return prevAuthCode;
    }

    public void setPrevAuthCode(String prevAuthCode) {
        this.prevAuthCode = prevAuthCode;
    }

    public String getInstallmentPeriod() {
        return installmentPeriod;
    }

    public void setInstallmentPeriod(String installmentPeriod) {
        this.installmentPeriod = installmentPeriod;
    }

    public String getChargeCurrencyCode() {
        return chargeCurrencyCode;
    }

    public void setChargeCurrencyCode(String chargeCurrencyCode) {
        this.chargeCurrencyCode = chargeCurrencyCode;
    }

    public String getInitialInstallmentPayment() {
        return initialInstallmentPayment;
    }

    public void setInitialInstallmentPayment(String initialInstallmentPayment) {
        this.initialInstallmentPayment = initialInstallmentPayment;
    }

    public String getInstallmentCharge() {
        return installmentCharge;
    }

    public void setInstallmentCharge(String installmentCharge) {
        this.installmentCharge = installmentCharge;
    }

    public String getInitialInstallmentCharge() {
        return initialInstallmentCharge;
    }

    public void setInitialInstallmentCharge(String initialInstallmentCharge) {
        this.initialInstallmentCharge = initialInstallmentCharge;
    }

    public String getInstallmentPerCharge() {
        return installmentPerCharge;
    }

    public void setInstallmentPerCharge(String installmentPerCharge) {
        this.installmentPerCharge = installmentPerCharge;
    }

    public String getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(String rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getExchangePoints() {
        return exchangePoints;
    }

    public void setExchangePoints(String exchangePoints) {
        this.exchangePoints = exchangePoints;
    }

    public String getPointsBanlance() {
        return pointsBanlance;
    }

    public void setPointsBanlance(String pointsBanlance) {
        this.pointsBanlance = pointsBanlance;
    }

    public String getPointsOutstandingAmt() {
        return pointsOutstandingAmt;
    }

    public void setPointsOutstandingAmt(String pointsOutstandingAmt) {
        this.pointsOutstandingAmt = pointsOutstandingAmt;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getIntoAcc() {
        return intoAcc;
    }

    public void setIntoAcc(String intoAcc) {
        this.intoAcc = intoAcc;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getAppCryptogramType() {
        return appCryptogramType;
    }

    public void setAppCryptogramType(String appCryptogramType) {
        this.appCryptogramType = appCryptogramType;
    }

    public String getAppCryptogram() {
        return appCryptogram;
    }

    public void setAppCryptogram(String appCryptogram) {
        this.appCryptogram = appCryptogram;
    }

    public String getTermVerificationResult() {
        return termVerificationResult;
    }

    public void setTermVerificationResult(String termVerificationResult) {
        this.termVerificationResult = termVerificationResult;
    }

    public String getPanSeqNo() {
        return panSeqNo;
    }

    public void setPanSeqNo(String panSeqNo) {
        this.panSeqNo = panSeqNo;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppTransCounter() {
        return appTransCounter;
    }

    public void setAppTransCounter(String appTransCounter) {
        this.appTransCounter = appTransCounter;
    }

    public String getAppStatusInfo() {
        return appStatusInfo;
    }

    public void setAppStatusInfo(String appStatusInfo) {
        this.appStatusInfo = appStatusInfo;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppLable() {
        return appLable;
    }

    public void setAppLable(String appLable) {
        this.appLable = appLable;
    }

    public String getUnpredictableNo() {
        return unpredictableNo;
    }

    public void setUnpredictableNo(String unpredictableNo) {
        this.unpredictableNo = unpredictableNo;
    }

    public String getAppInterchangerProfile() {
        return appInterchangerProfile;
    }

    public void setAppInterchangerProfile(String appInterchangerProfile) {
        this.appInterchangerProfile = appInterchangerProfile;
    }

    public String getCvmResults() {
        return cvmResults;
    }

    public void setCvmResults(String cvmResults) {
        this.cvmResults = cvmResults;
    }

    public String getIssuerAppData() {
        return issuerAppData;
    }

    public void setIssuerAppData(String issuerAppData) {
        this.issuerAppData = issuerAppData;
    }

    public String getTermCap() {
        return termCap;
    }

    public void setTermCap(String termCap) {
        this.termCap = termCap;
    }

    public String getCardProductId() {
        return cardProductId;
    }

    public void setCardProductId(String cardProductId) {
        this.cardProductId = cardProductId;
    }

    public String getIssuerMsg() {
        return issuerMsg;
    }

    public void setIssuerMsg(String issuerMsg) {
        this.issuerMsg = issuerMsg;
    }

    public String getCpuMsg() {
        return cpuMsg;
    }

    public void setCpuMsg(String cpuMsg) {
        this.cpuMsg = cpuMsg;
    }

    public String getAcquireMsg() {
        return acquireMsg;
    }

    public void setAcquireMsg(String acquireMsg) {
        this.acquireMsg = acquireMsg;
    }

    public String getNoSignText() {
        return noSignText;
    }

    public void setNoSignText(String noSignText) {
        this.noSignText = noSignText;
    }

    public String getSignImage() {
        return signImage;
    }

    public void setSignImage(String signImage) {
        this.signImage = signImage;
    }

    public String getCopyType() {
        return copyType;
    }

    public void setCopyType(String copyType) {
        this.copyType = copyType;
    }

    public String getHotline() {
        return hotline;
    }

    public void setHotline(String hotline) {
        this.hotline = hotline;
    }

    public boolean isPrintTipFlag() {
        return printTipFlag;
    }

    public void setPrintTipFlag(boolean printTipFlag) {
        this.printTipFlag = printTipFlag;
    }

    public String getChargeMethod() {
        return chargeMethod;
    }

    public void setChargeMethod(String chargeMethod) {
        this.chargeMethod = chargeMethod;
    }

    public boolean isIcTagFlag() {
        return icTagFlag;
    }

    public void setIcTagFlag(boolean icTagFlag) {
        this.icTagFlag = icTagFlag;
    }

    public boolean isReprintFlag() {
        return reprintFlag;
    }

    public void setReprintFlag(boolean reprintFlag) {
        this.reprintFlag = reprintFlag;
    }

    public boolean isShouldSign() {
        return shouldSign;
    }

    public void setShouldSign(boolean shouldSign) {
        this.shouldSign = shouldSign;
    }

    public boolean isShouldDeclare() {
        return shouldDeclare;
    }

    public void setShouldDeclare(boolean shouldDeclare) {
        this.shouldDeclare = shouldDeclare;
    }
}
