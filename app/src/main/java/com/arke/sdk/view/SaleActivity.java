package com.arke.sdk.view;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arke.sdk.R;
import com.arke.sdk.api.Beeper;
import com.arke.sdk.api.DeviceManager;
import com.arke.sdk.api.EMV;
import com.arke.sdk.api.LED;
import com.arke.sdk.api.PinpadForDUKPT;
import com.arke.sdk.util.data.BytesUtil;
import com.arke.sdk.util.data.DateUtil;
import com.arke.sdk.util.data.StringUtil;
import com.arke.sdk.util.data.TLV;
import com.arke.sdk.util.data.TLVList;
import com.arke.sdk.util.emv.EmvData;
import com.arke.sdk.util.emv.EmvParameterException;
import com.arke.sdk.util.emv.EmvParameterInitializer;
import com.arke.sdk.util.emv.EmvTags;
import com.arke.sdk.util.pinpad.KeyId;
import com.arke.sdk.util.pinpad.MockKey;
import com.arke.sdk.util.printer.PayTemplateData;
import com.arke.sdk.util.printer.Printer;
import com.arke.sdk.util.transaction.Session;
import com.arke.sdk.util.transaction.TerminalInfo;
import com.arke.sdk.util.transaction.TransactionConfig;
import com.arke.sdk.vas.VASAction;
import com.arke.sdk.vas.VASService;
import com.arke.vas.data.ResponseBodyData;
import com.usdk.apiservice.aidl.constants.RFDeviceName;
import com.usdk.apiservice.aidl.emv.ACType;
import com.usdk.apiservice.aidl.emv.CAPublicKey;
import com.usdk.apiservice.aidl.emv.CVMFlag;
import com.usdk.apiservice.aidl.emv.CVMMethod;
import com.usdk.apiservice.aidl.emv.CandidateAID;
import com.usdk.apiservice.aidl.emv.CardRecord;
import com.usdk.apiservice.aidl.emv.EMVError;
import com.usdk.apiservice.aidl.emv.EMVEventHandler;
import com.usdk.apiservice.aidl.emv.EMVTag;
import com.usdk.apiservice.aidl.emv.FinalData;
import com.usdk.apiservice.aidl.emv.KernelINS;
import com.usdk.apiservice.aidl.emv.OfflinePinVerifyResult;
import com.usdk.apiservice.aidl.emv.SearchCardListener;
import com.usdk.apiservice.aidl.emv.TransData;
import com.usdk.apiservice.aidl.emv.WaitCardFlag;
import com.usdk.apiservice.aidl.led.Light;
import com.usdk.apiservice.aidl.pinpad.EncKeyFmt;
import com.usdk.apiservice.aidl.pinpad.KeyAlgorithm;
import com.usdk.apiservice.aidl.pinpad.KeyType;
import com.usdk.apiservice.aidl.pinpad.OfflinePinVerify;
import com.usdk.apiservice.aidl.pinpad.OnPinEntryListener;
import com.usdk.apiservice.aidl.pinpad.PinPublicKey;
import com.usdk.apiservice.aidl.pinpad.PinVerifyResult;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableCompletableObserver;

/**
 * Sale activity.
 */

public class SaleActivity extends BaseActivity {

    private static final String TAG = "SaleActivity";

    /**
     * Wait card event.
     */
    private static final int EMV_EVENT_WAIT_CARD = 0;

    /**
     * Card checked event.
     */
    private static final int EMV_EVENT_CARD_CHECKED = 1;

    /**
     * Final select event.
     */
    private static final int EMV_EVENT_FINAL_SELECT = 2;

    /**
     * Read record event.
     */
    private static final int EMV_EVENT_READ_RECORD = 3;

    /**
     * Card holder verify event.
     */
    private static final int EMV_EVENT_CARD_HOLDER_VERIFY = 4;

    /**
     * EMV online process event.
     */
    private static final int EMV_EVENT_ONLINE_PROCESS = 5;

    /**
     * End process event.
     */
    private static final int EMV_EVENT_END_PROCESS = 6;

    /**
     * Send out event.
     */
    private static final int EMV_EVENT_SEND_OUT = 7;

    /**
     * Swipe card event.
     */
    private static final int MAG_EVENT_SWIPE_CARD = 8;

    /**
     * Mag card online process event.
     */
    private static final int MAG_EVENT_ONLINE_PROCESS = 9;

    /**
     * Confirm pin input event.
     */
    private static final int PIN_EVENT_INPUT_CONFIRM = 10;

    /**
     * Cancel pin input event.
     */
    private static final int PIN_EVENT_INPUT_CANCEL = 11;

    /**
     * Error pin input event.
     */
    private static final int PIN_EVENT_INPUT_ERROR = 12;

    /**
     * Print finished event.
     */
    private static final int PRT_EVENT_FINISHED = 13;

    /**
     * Print error event.
     */
    private static final int PRT_EVENT_ERROR = 14;

    /**
     * Print HTML error event.
     */
    private static final int PRT_EVENT_HTML_ERROR = 15;

    /**
     * Communication completed event.
     */
    private static final int COM_EVENT_COMPLETED = 16;

    /**
     * Emv parameter initializer.
     */
    private EmvParameterInitializer emvParameter;

    /**
     * Session for transaction.
     */
    private Session session;

    /**
     * Transaction config.
     */
    private TransactionConfig transactionConfig;

    /**
     * Is in EMV process.
     */
    private boolean isEMVProcess;

    /**
     * Card record.
     */
    private CardRecord cardRecord;

    /**
     * Amount text view.
     */
    private TextView tvAmount;

    /**
     * Pan text view.
     */
    private TextView tvPan;

    /**
     * Date text view.
     */
    private TextView tvDate;

    /**
     * PIN text view.
     */
    private TextView tvPin;

    /**
     * Amount edit text.
     */
    private EditText etAmount;

    /**
     * Pan edit text.
     */
    private EditText etPan;

    /**
     * Date edit text.
     */
    private EditText etDate;

    /**
     * Amount button.
     */
    private Button btnAmount;

    /**
     * Pan button.
     */
    private Button btnPan;

    /**
     * Print button.
     */
    private Button btnPrint;

    /**
     * Input amount layout.
     */
    private LinearLayout layoutInputAmount;

    /**
     * Wait card layout.
     */
    private LinearLayout layoutWaitCard;

    /**
     * Input PIN layout.
     */
    private LinearLayout layoutInputPin;

    /**
     * Print layout.
     */
    private LinearLayout layoutPrint;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sale);

        etAmount = (EditText) findViewById(R.id.et_amount);
        etPan = (EditText) findViewById(R.id.et_pan);
        etDate = (EditText) findViewById(R.id.et_date);
        tvAmount = (TextView) findViewById(R.id.tv_amount);
        tvPan = (TextView) findViewById(R.id.tv_pan);
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvPin = (TextView) findViewById(R.id.tv_pin);
        btnAmount = (Button) findViewById(R.id.btn_amount);
        btnPan = (Button) findViewById(R.id.btn_pan);
        btnPrint = (Button) findViewById(R.id.btn_print);
        layoutInputAmount = (LinearLayout) findViewById(R.id.layout_input_amount);
        layoutWaitCard = (LinearLayout) findViewById(R.id.layout_wait_card);
        layoutInputPin = (LinearLayout) findViewById(R.id.layout_input_pin);
        layoutPrint = (LinearLayout) findViewById(R.id.layout_print);

        session = new Session();
        transactionConfig = new TransactionConfig();
        emvParameter = new EmvParameterInitializer(EMV.getInstance(), session, transactionConfig);

        onInputAmount();
    }

    @Override
    public void onBackPressed() {
        try {
            stopEMVProcess();
        } catch (RemoteException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        ResponseBodyData responseBodyData = new ResponseBodyData();
        responseBodyData.setResponseMessage(getString(R.string.transaction_declined));
        VASService.sendResultToVAS(VASAction.SALE, responseBodyData);
        super.onBackPressed();
    }

    /**
     * Show input amount view.
     */
    public void onInputAmount() {
        layoutInputAmount.setVisibility(View.VISIBLE);
        layoutWaitCard.setVisibility(View.GONE);
        layoutInputPin.setVisibility(View.GONE);
        layoutPrint.setVisibility(View.GONE);

        Double amount = getIntent().getDoubleExtra("amount", 0);
        if (amount != 0) {
            etAmount.setText(amount.toString());
        }

        // Init Pinpad.
        try {
            initPinpad();
            btnAmount.setEnabled(true);
        } catch (RemoteException e) {
            showToast(e.getLocalizedMessage());
        }
    }

    /**
     * Show wait card view.
     */
    public void onWaitCard() {
        layoutInputAmount.setVisibility(View.GONE);
        layoutWaitCard.setVisibility(View.VISIBLE);
        layoutInputPin.setVisibility(View.GONE);
        layoutPrint.setVisibility(View.GONE);
    }

    /**
     * Show card record view.
     */
    public void onConfirmCardRecord(String pan, String date) {
        etPan.setText(pan);
        etDate.setText(date);

        // Check value.
        btnPan.setEnabled(!etPan.getText().toString().isEmpty() && !etDate.getText().toString().isEmpty());
    }

    /**
     * Show input PIN view.
     */
    public void onInputPin(String message) {
        tvPin.setText(message);
        layoutInputAmount.setVisibility(View.GONE);
        layoutWaitCard.setVisibility(View.GONE);
        layoutInputPin.setVisibility(View.VISIBLE);
        layoutPrint.setVisibility(View.GONE);
    }

    /**
     * Show print view.
     */
    public void onPrint(String amount, String pan, String date) {
        tvAmount.setText(amount);
        tvPan.setText(pan);
        tvDate.setText(date);
        btnPrint.setEnabled(false);
        layoutInputAmount.setVisibility(View.GONE);
        layoutWaitCard.setVisibility(View.GONE);
        layoutInputPin.setVisibility(View.GONE);
        layoutPrint.setVisibility(View.VISIBLE);
    }

    /**
     * Called after inputting amount.
     */
    public void onInputAmountFinished(View view) {
        String amount = etAmount.getText().toString();
        if (amount.isEmpty()) {
            return;
        }

        int len = amount.length();
        int index = amount.indexOf(".");
        if (index >= 0) {
            if (index + 3 > len) {
                len += 2;
                amount = amount + "00";
            }

            if (index + 3 < len) {
                amount = amount.substring(0, index + 3);
            }

            amount = amount.replace(".", "");
            if (Long.parseLong(amount) == 0) {
                return;
            }
        } else {
            amount = amount + "00";
        }

        etAmount.setText(amount);
        btnAmount.setEnabled(false);
        onWaitCard();

        try {
            startSaleProcess();
        } catch (RemoteException e) {
            showToast(e.getLocalizedMessage());
        }

        // Show message
        showToast(getString(R.string.waiting_for_card));
    }

    /**
     * Called after waiting card.
     */
    public void onWaitCardFinished(View view) {
        btnPan.setEnabled(false);

        try {
            handleCardRecordConfirmed();
        } catch (RemoteException e) {
            showToast(e.getLocalizedMessage());
        }
    }

    /**
     * Called after printing.
     */
    public void onPrintFinished() {
        btnPrint.setEnabled(true);
    }

    /**
     * Called after user click CONFIRM button in printing view.
     */
    public void onTransactionFinished(View view) {
        btnPrint.setEnabled(false);
        ResponseBodyData responseBodyData = new ResponseBodyData();
        responseBodyData.setResponseMessage(getString(R.string.transaction_approved));
        VASService.sendResultToVAS(VASAction.SALE, responseBodyData);
        finish();
    }

    /**
     * Start sale process.
     */
    private void startSaleProcess() throws RemoteException {
        // Set session.
        session.setTransactionName(getString(R.string.sale));
        session.setTransactionAmount(Long.parseLong(etAmount.getText().toString()));
        session.setProcessingCode("000000");
        session.setSystemTraceAuditNumber("000001");
        session.setBatchNumber("0000001");

        // Set transaction config.
        transactionConfig.setMagCardSupported(true);
        transactionConfig.setContactIcCardSupported(true);
        transactionConfig.setRfCardSupported(true);
        transactionConfig.setRfOnlineForced(false);
        transactionConfig.setRfTransactionAmountLimitCheckNeeded(true);
        transactionConfig.setPinRule(new byte[]{0, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        transactionConfig.setQPSSupported(true);
        transactionConfig.setOnlineNeeded(true);
        transactionConfig.setPinInputNeeded(true);
        transactionConfig.setTransactionType(TransactionConfig.TRANSACTION_TYPE_FULL);

        // Start EMV process.
        startEMVProcess();
    }

    /**
     * Stop EMV process.
     */
    private void stopEMVProcess() throws RemoteException {
        EMV.getInstance().stopSearch();
        EMV.getInstance().halt();

        if (isEMVProcess) {
            EMV.getInstance().stopProcess();
        }
    }

    /**
     * Start EMV process.
     */
    private void startEMVProcess() throws RemoteException {
        // Set EMV param
        Bundle bundle = new Bundle();
        bundle.putByte("flagPSE", EMV.PSE_AID_LIST);
        bundle.putByte("flagCtlAsCb", EMV.DISABLE_CONTACTLESS_CARD_SELECT_APP);
        bundle.putBoolean("flagICCLog", false);

        // Start process
        isEMVProcess = true;
        EMV.getInstance().startProcess(bundle, new EMVEventHandler.Stub() {

            @Override
            public void onInitEMV() throws RemoteException {
                Log.d(TAG, "----- onInitEMV -----");

                handleInitEMV();
            }

            @Override
            public void onWaitCard(int flag) throws RemoteException {
                Log.d(TAG, "----- onWaitCard -----");
                Log.d(TAG, "flag : " + flag);

                sendMessage(EMV_EVENT_WAIT_CARD, flag);
            }

            @Override
            public void onCardChecked(int cardType) throws RemoteException {
                Log.d(TAG, "----- onCardChecked -----");
                Log.d(TAG, "cardType : " + cardType);

                sendMessage(EMV_EVENT_CARD_CHECKED, cardType);
            }

            @Override
            public void onAppSelect(boolean reselect, List<CandidateAID> aids) throws RemoteException {
                Log.d(TAG, "----- onAppSelect -----");
                Log.d(TAG, "aids.size : " + aids.size());

                handleAppSelect(aids);
            }

            @Override
            public void onFinalSelect(FinalData finalData) throws RemoteException {
                Log.d(TAG, "----- onFinalSelect -----");
                Log.d(TAG, "KernelID:" + finalData.getKernelID());
                Log.d(TAG, "AID:" + BytesUtil.bytes2HexString(finalData.getAID()));

                sendMessage(EMV_EVENT_FINAL_SELECT, finalData);
            }

            @Override
            public void onReadRecord(CardRecord cardRecord) throws RemoteException {
                Log.d(TAG, "----- onReadRecord -----");
                Log.d(TAG, "PAN:" + BytesUtil.bytes2HexString(cardRecord.getPan()));

                sendMessage(EMV_EVENT_READ_RECORD, cardRecord);
            }

            @Override
            public void onCardHolderVerify(CVMMethod cvmMethod) throws RemoteException {
                Log.d(TAG, "----- onCardHolderVerify -----");
                Log.d(TAG, "CVM:" + cvmMethod.getCVM());
                Log.d(TAG, "PINTimes:" + cvmMethod.getPINTimes());

                sendMessage(EMV_EVENT_CARD_HOLDER_VERIFY, cvmMethod);
            }

            @Override
            public void onOnlineProcess(TransData transData) throws RemoteException {
                Log.d(TAG, "----- onOnlineProcess -----");
                Log.d(TAG, "ACType:" + transData.getACType());
                Log.d(TAG, "CVM:" + transData.getCVM());
                Log.d(TAG, "FlowType:" + transData.getFlowType());

                sendMessage(EMV_EVENT_ONLINE_PROCESS, transData);
            }

            @Override
            public void onEndProcess(int resultCode, TransData transData) throws RemoteException {
                Log.d(TAG, "----- onEndProcess -----");
                Log.d(TAG, "resultCode:" + resultCode);

                sendMessage(EMV_EVENT_END_PROCESS, resultCode, transData);
            }

            @Override
            public void onVerifyOfflinePin(int flag, byte[] random, CAPublicKey caPublicKey, OfflinePinVerifyResult offlinePinVerifyResult) throws RemoteException {
                Log.d(TAG, "----- onVerifyOfflinePin -----");
                Log.d(TAG, "flag : " + flag);

                handleVerifyOfflinePin(flag, random, caPublicKey, offlinePinVerifyResult);
            }

            @Override
            public void onObtainData(int command, byte[] data) throws RemoteException {
                Log.d(TAG, "----- onObtainData -----");
                Log.d(TAG, command + " : " + BytesUtil.bytes2HexString(data));
            }

            @Override
            public void onSendOut(int command, byte[] data) throws RemoteException {
                Log.d(TAG, "----- onSendOut -----");
                Log.d(TAG, command + " : " + BytesUtil.bytes2HexString(data));

                sendMessage(EMV_EVENT_SEND_OUT, command);
            }
        });
    }

    /**
     * Search card.
     */
    private void searchCard() throws RemoteException {
        // Set card config
        Bundle bundle = new Bundle();
        bundle.putBoolean("supportICCard", transactionConfig.isContactIcCardSupported());
        bundle.putBoolean("supportRFCard", transactionConfig.isRfCardSupported());
        bundle.putBoolean("supportMagCard", transactionConfig.isMagCardSupported());
        if (DeviceManager.MODEL_AECR_C10.equals(DeviceManager.getInstance().getModel())) {
            bundle.putString("rfDeviceName", RFDeviceName.EXTERNAL);
        } else {
            bundle.putString("rfDeviceName", RFDeviceName.INNER);
        }

        // Start searching card
        EMV.getInstance().searchCard(bundle, 60, new SearchCardListener.Stub() {
            @Override
            public void onCardSwiped(Bundle bundle) throws RemoteException {
                Log.d(TAG, "----- onCardSwiped -----");
                Log.d(TAG, "PAN : " + bundle.getString("PAN"));
                Log.d(TAG, "TRACK1 : " + bundle.getString("TRACK1"));
                Log.d(TAG, "TRACK2 : " + bundle.getString("TRACK2"));
                Log.d(TAG, "TRACK3 : " + bundle.getString("TRACK3"));
                Log.d(TAG, "SERVICE_CODE : " + bundle.getString("SERVICE_CODE"));
                Log.d(TAG, "EXPIRED_DATE : " + bundle.getString("EXPIRED_DATE"));

                sendMessage(MAG_EVENT_SWIPE_CARD, bundle);
            }

            @Override
            public void onCardInsert() throws RemoteException {
                Log.d(TAG, "----- onCardInsert -----");

                EMV.getInstance().responseCard();
            }

            @Override
            public void onCardPass(int cardType) throws RemoteException {
                Log.d(TAG, "----- onCardPass -----");
                Log.d(TAG, "cardType: " + cardType);

                EMV.getInstance().responseCard();
            }

            @Override
            public void onTimeout() throws RemoteException {
                Log.d(TAG, "----- onTimeout -----");

                // Stop EMV process
                showToast(getString(R.string.wait_card_timeout));
                stopEMVProcess();
            }

            @Override
            public void onError(int error, String message) throws RemoteException {
                Log.d(TAG, "----- onError -----");
                Log.d(TAG, error + " : " + message);

                // Stop EMV process
                showToast(getString(EMV.getErrorId(error)));
                stopEMVProcess();
            }
        });
    }

    /**
     * Handle init EMV.
     */
    private void handleInitEMV() throws RemoteException {
        // Init aids.
        emvParameter.initEmvAids();
    }

    /**
     * Handle app select.
     */
    private void handleAppSelect(List<CandidateAID> aids) throws RemoteException {
        // Choose the first aid.
        TLVList tlvList = new TLVList();
        tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_AID, aids.get(0).getAID()));
        EMV.getInstance().responseEvent(tlvList.toString());
    }

    /**
     * Handle final select.
     */
    private void handleFinalSelect(FinalData finalData) throws RemoteException {
        byte kernelId = finalData.getKernelID();

        // Set transaction type(9C)
        EMV.getInstance().setTLV(kernelId, EmvTags.EMV_TAG_TM_TRANSTYPE, session.getProcessingCode().substring(0, 1));

        // For QPS
        if (session.getAccountEntryMode().equals(Session.ACCOUNT_ENTRY_MODE_CONTACTLESS)) {
            session.setFirstCurrencyCode(EMV.getInstance().getDataAPDU("9F51"));
            session.setSecondCurrencyCode(EMV.getInstance().getDataAPDU("DF71"));
        }

        // Init EMV parameters
        String aid = BytesUtil.bytes2HexString(finalData.getAID());
        String pid = BytesUtil.bytes2HexString(finalData.getPID());
        try {
            emvParameter.initEmvParameters(aid, kernelId, pid, session.getAccountEntryMode());
        } catch (EmvParameterException e) {
            throw new RemoteException(e.getMessage());
        }

        // Set gpo to EMV kernel
        TLVList tlvList = new TLVList();
        tlvList.addTLV(EmvTags.EMV_TAG_TM_AUTHAMNTN, BytesUtil.toBCDAmountBytes(session.getTransactionAmount()));
        tlvList.addTLV(EmvTags.EMV_TAG_TM_OTHERAMNTN, BytesUtil.toBCDAmountBytes(0L));
        tlvList.addTLV(EmvTags.EMV_TAG_TM_TRANSDATE, BytesUtil.hexString2ByteArray(DateUtil.getDate(new Date(), "yyMMdd")));
        tlvList.addTLV(EmvTags.EMV_TAG_TM_TRANSTIME, BytesUtil.hexString2ByteArray(DateUtil.getDate(new Date(), "HHmmss")));
        tlvList.addTLV(EmvTags.EMV_TAG_TM_TRSEQCNTR, BytesUtil.hexString2ByteArray(session.getSystemTraceAuditNumber()));
        tlvList.addTLV(EmvTags.DEF_TAG_SERVICE_TYPE, new byte[]{EMV.SERVICE_TYPE_GOODS_SERVICE});
        tlvList.addTLV(EmvTags.DEF_TAG_START_RECOVERY, new byte[]{(byte) 0x00}); // 0- false, 1- true
        if (transactionConfig.isRfOnlineForced()) {
            tlvList.addTLV(EmvTags.DEF_TAG_GAC_CONTROL, new byte[]{EMV.GAC_ONLINE});
        } else {
            tlvList.addTLV(EmvTags.DEF_TAG_GAC_CONTROL, new byte[]{EMV.GAC_NORMAL});
        }
        EMV.getInstance().responseEvent(tlvList.toString());
    }

    /**
     * Handle card checked.
     */
    private void handleCardChecked(int cardType) throws RemoteException {
        // Check and save card type.
        switch (cardType) {
            case 1: // IC card
                LED.getInstance().turnOffAll();
                session.setAccountEntryMode(Session.ACCOUNT_SERVICE_ENTRY_MODE_CONTACT);
                break;
            case 2: // RF card
                LED.getInstance().turnOn(Light.BLUE, Light.YELLOW);
                session.setAccountEntryMode(Session.ACCOUNT_ENTRY_MODE_CONTACTLESS);
                break;
        }
    }

    /**
     * Handle wait card.
     */
    private void handleWaitCard(int flag) throws RemoteException {
        // Handle flag.
        switch (flag) {
            case WaitCardFlag.NORMAL:
                // This case would never happen, if you have already searchCard before startEMV.
                // Otherwise it would happen for searching card.
                searchCard();
                break;

            case WaitCardFlag.ISS_SCRIPT_UPDATE:
            case WaitCardFlag.SHOW_CARD_AGAIN:
                transactionConfig.setMagCardSupported(false);
                transactionConfig.setContactIcCardSupported(false);
                searchCard();
                break;

            case WaitCardFlag.EXECUTE_CDCVM:
                // Halt RF card reader.
                EMV.getInstance().halt();

                // Delay and research.
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(1200);
                            transactionConfig.setMagCardSupported(false);
                            transactionConfig.setContactIcCardSupported(false);
                            searchCard();
                        } catch (RemoteException | InterruptedException e) {
                            showToast(e.getLocalizedMessage());
                        }
                    }
                }.start();
                break;

            default:
                // Stop EMV process
                showToast(getString(R.string.unknown_error));
                stopEMVProcess();
                return;
        }

        // Operate LED.
        LED.getInstance().turnOffAll();
        if (transactionConfig.isRfCardSupported()) {
            LED.getInstance().turnOn(Light.BLUE);
        }
    }

    /**
     * Handle end process.
     */
    private void handleEndProcess(int resultCode, TransData transData) throws RemoteException {
        isEMVProcess = false;

        // Check result code.
        if (resultCode != EMVError.SUCCESS) {
            if (session.getAccountEntryMode() == null ||
                    (session.getAccountEntryMode() != null && !session.getAccountEntryMode().equals(Session.ACCOUNT_ENTRY_MODE_MAGCARD))) {
                transactionInterrupted(resultCode);
            }
            return;
        }

        // Save transaction data.
        String pan = StringUtil.getDigits(BytesUtil.bytes2HexString(transData.getPAN()));
        String date = StringUtil.getDigits(BytesUtil.bytes2HexString(transData.getExpiry()));
        session.setPan(pan);
        session.setExpirationDate(date);

        // Check AC type.
        switch (transData.getACType()) {
            case ACType.EMV_ACTION_TC:
                // EMV kernel approves transaction.
                transactionApproved();
                break;

            case ACType.EMV_ACTION_AAC:
                // EMV kernel denies transaction.
                transactionDeclined();
                break;

            case ACType.EMV_ACTION_ARQC:
                // EMV kernel requires online transaction.
                handleOnlineProcess();
                break;
        }
    }

    /**
     * Transaction interrupted.
     */
    private void transactionInterrupted(int resultCode) throws RemoteException {
        // Turn on red light.
        LED.getInstance().turnOffAll();
        LED.getInstance().turnOn(Light.RED);

        // Show transaction result.
        if (resultCode != EMVError.ERROR_EMV_RESULT_STOP) {
            showToast(getString(EMV.getErrorId(resultCode)));
        }
    }

    /**
     * Transaction declined.
     */
    private void transactionDeclined() throws RemoteException {
        // Turn on red light.
        LED.getInstance().turnOffAll();
        LED.getInstance().turnOn(Light.RED);

        // Show transaction result.
        showToast(getString(R.string.transaction_declined));
    }

    /**
     * Transaction approved.
     */
    private void transactionApproved() throws RemoteException {
        // Close all lights.
        LED.getInstance().turnOffAll();

        // Show transaction result.
        showToast(getString(R.string.transaction_approved));

        // Show and print record.
        onPrint(session.getDisplayAmount(), session.getPan(), session.getExpirationDate());
        printRecordByHtml(session);
    }

    /**
     * Handle online process.
     */
    private void handleOnlineProcess() throws RemoteException {
        // If the transaction need to online process.
        if (!transactionConfig.isOnlineNeeded()) {
            // It is simple EMV process if no need to be online,the EMV process is stopProcess at this time so that exist process directly.
            stopEMVProcess();
            return;
        }

        // Simulate online
        new Thread() {
            @Override
            public void run() {
                try {
                    LED.getInstance().operateGreenLight();
                    sleep(300);
                    LED.getInstance().operateGreenLight();
                    sleep(300);
                    LED.getInstance().operateGreenLight();
                    sleep(300);
                    sendMessage(COM_EVENT_COMPLETED, "00"); // "00" means successful
                } catch (RemoteException | InterruptedException e) {
                    showToast(e.getLocalizedMessage());
                }
            }
        }.start();
    }

    /**
     * Handle communication completed.
     */
    private void handleCommunicationCompleted(String responseCode) throws RemoteException {
        session.setResponseCode(responseCode);
        session.setOnlineProcessSucceeded(true);
        session.setIcRemark(Session.IC_REMARK_TC_GENERATED);

        TLVList tlvList = new TLVList();

        // Online status.
        tlvList.addTLV(EmvTags.DEF_TAG_ONLINE_STATUS, new byte[]{(byte) (session.isOnlineProcessSucceeded() ? 0 : 1)});

        // Check communicate result.
        boolean isTransactionSucceeded;
        String respCode = session.getResponseCode();
        if (respCode != null && !respCode.isEmpty() && respCode.length() == 2) {
            tlvList.addTLV(EmvTags.EMV_TAG_TM_ARC, respCode.getBytes());
            isTransactionSucceeded = respCode.equals("00");
        } else {
            isTransactionSucceeded = false;
        }
        tlvList.addTLV(EmvTags.DEF_TAG_AUTHORIZE_FLAG, new byte[]{(byte) (isTransactionSucceeded ? 1 : 0)});

        // Online response chip data(Field 55 data).
        if (session.getField55() != null && !session.getField55().isEmpty()) {
            tlvList.addTLV(EmvTags.DEF_TAG_HOST_TLVDATA, BytesUtil.hexString2Bytes(session.getField55()));
        }

        // Auth code.
        if (session.getAuthCode() != null) {
            tlvList.addTLV(EmvTags.EMV_TAG_TM_AUTHCODE, BytesUtil.hexString2Bytes(session.getAuthCode()));
        }

        // Whether is in EMV process.
        if (!isEMVProcess) {
            if (isTransactionSucceeded) {
                transactionApproved();
            } else {
                transactionDeclined();
            }

            return;
        }

        // Response to emv kernel.
        EMV.getInstance().responseEvent(tlvList.toString());
    }

    /**
     * Print transaction record by HTML.
     */
    private void printRecordByHtml(Session session) throws RemoteException {

        // Build transaction data to fill in the template
        PayTemplateData payTemplateData = new PayTemplateData();
        payTemplateData.setMerchantName(TerminalInfo.merchantName);
        payTemplateData.setMerchantNo(TerminalInfo.merchantNo);
        payTemplateData.setTerminalNo(TerminalInfo.terminalNo);
        payTemplateData.setOperatorNo(TerminalInfo.operatorNo);
        payTemplateData.setAcquirer(TerminalInfo.acquirer);
        payTemplateData.setIssuer(TerminalInfo.issuer);
        payTemplateData.setCardNo(session.getDisplayPan());
        payTemplateData.setExpiryDate(session.getExpirationDate());
        payTemplateData.setTransTypePrint(session.getTransactionName());
        payTemplateData.setBatchNo(session.getBatchNumber());
        payTemplateData.setDateTime(session.getTransactionYear() + "/" + session.getTransactionDate() + " " + session.getTransactionTime());
        payTemplateData.setAmtTrans(session.getDisplayAmount());

        // Get statue
        Printer.getInstance().getStatus();

        // Set gray
        Printer.getInstance().setPrnGray(6);

        // Set HTML image
        Printer.getInstance().addHtml("pay-template", payTemplateData.toJSONString())
                .andThen(Printer.getInstance().print())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        sendMessage(PRT_EVENT_FINISHED, null);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        sendMessage(PRT_EVENT_HTML_ERROR, e);
                    }
                });
    }

    /**
     * Handle card record read.
     */
    private void handleReadRecord(CardRecord cardRecord) throws RemoteException {

        this.cardRecord = cardRecord;

        // Save the expiration.
        if (cardRecord.getExpiry() != null && cardRecord.getExpiry().length > 0) {
            String dateExpiration = BytesUtil.byteArray2HexString(cardRecord.getExpiry());
            // Format expiration (from YYYYMMDD to YYMM)
            dateExpiration = dateExpiration.substring(2, 4) + dateExpiration.substring(4, 6);

            session.setExpirationDate(dateExpiration);
        }

        // Save card number.
        if (cardRecord.getPan() != null && cardRecord.getPan().length > 0) {
            String primaryAccNo = StringUtil.getDigits(BytesUtil.byteArray2HexString(cardRecord.getPan()));

            session.setPan(primaryAccNo);
        }

        // Whether if RF card flow.
        if (session.getAccountEntryMode().equals(Session.ACCOUNT_ENTRY_MODE_CONTACTLESS)) {
            // RF card do not to need confirm card information, just response card record read.
            handleCardRecordConfirmed();
        } else {
            // Show the card record and confirm
            onConfirmCardRecord(session.getPan(), session.getExpirationDate());
        }
    }

    /**
     * Handle send out.
     */
    private void handleSendOut(int command) throws RemoteException {
        if (command == KernelINS.CLOSE_RF) {
            Beeper.getInstance().startBeep(500);
        }
    }

    /**
     * Handle card holder verify.
     */
    private void handleCardHolderVerify(final CVMMethod cvmMethod) throws RemoteException {
        switch (cvmMethod.getCVM()) {
            case CVMFlag.EMV_CVMFLAG_OFFLINEPIN:
                inputOfflinePin(cvmMethod.getPINTimes());
                break;

            case CVMFlag.EMV_CVMFLAG_ONLINEPIN:
                inputOnlinePin();
                break;
        }
    }

    /**
     * Init Pinpad.
     */
    private void initPinpad() throws RemoteException {
        // Open
        PinpadForDUKPT.getInstance().open();

        // Set key algorithm
        PinpadForDUKPT.getInstance().setKeyAlgorithm(KeyAlgorithm.KA_TDEA);

        // Set encrypted key format
        PinpadForDUKPT.getInstance().setEncKeyFormat(EncKeyFmt.ENC_KEY_FMT_NORMAL);

        // Format
        PinpadForDUKPT.getInstance().format();

        // Load main key
        byte[] mainKey = BytesUtil.hexString2Bytes(MockKey.dupktMainKey);
        PinpadForDUKPT.getInstance().loadPlainTextKey(KeyType.MAIN_KEY, KeyId.mainKey, mainKey);

        // Switch to work mode
        PinpadForDUKPT.getInstance().switchToWorkMode();

        // Init IK KSN
        byte[] ksnData = BytesUtil.hexString2Bytes(MockKey.ksnData);
        PinpadForDUKPT.getInstance().initDUKPTIkKSN(KeyId.mainKey, ksnData);

        // Close
        PinpadForDUKPT.getInstance().close();
    }

    /**
     * Handle card record confirm.
     */
    private void handleCardRecordConfirmed() throws RemoteException {
        // If the card type is mag card or manually input card or contact simple process, then stop emv
        if (session.getAccountEntryMode().equals(Session.ACCOUNT_ENTRY_MODE_MAGCARD)
                || session.getAccountEntryMode().equals(Session.ACCOUNT_ENTRY_MODE_MANUAL)
                || (session.getAccountEntryMode().equals(Session.ACCOUNT_SERVICE_ENTRY_MODE_CONTACT)
                && transactionConfig.getTransactionType() == TransactionConfig.TRANSACTION_TYPE_SIMPLE)) {

            // Stop EMV
            stopEMVProcess();

            if (transactionConfig.isPinInputNeeded()) {
                // request input online pin
                inputOnlinePin();
            } else {
                // request online process
                handleOnlineProcess();
            }

            return;
        }

        // for ic card or rf card, response the card result to emv.
        TLVList tlvList = new TLVList();

        // Get the first 5 bytes of aid.
        byte[] rid = BytesUtil.subBytes(cardRecord.getAID(), 0, 5);

        // Find public key from emv data.
        CAPublicKey caPublicKey = EmvData.getPublicKey(rid, cardRecord.getPubKIndex());
        if (caPublicKey != null) {
            // Set public key.
            EMV.getInstance().setCAPubKey(caPublicKey);
        }

        // Accumulated amount.
        tlvList.addTLV(EmvTags.DEF_TAG_ACCUMULATE_AMOUNT, BytesUtil.toBCDAmountBytes(0L));

        // Pan in black.
        tlvList.addTLV(EmvTags.DEF_TAG_PAN_IN_BLACK, new byte[]{(byte) 0x0}); // 0- false, 1- true

        EMV.getInstance().responseEvent(tlvList.toString());
    }

    /**
     * Input offline pin.
     */
    private void inputOfflinePin(int PINTimes) throws RemoteException {

        // Show offline pin input times
        if (PINTimes > 1) {
            onInputPin(String.format(getString(R.string.input_offline_pin_times), PINTimes));
        } else {
            onInputPin(getString(R.string.input_offline_pin_one_time));
        }

        Bundle param = new Bundle();
        param.putInt("timeout", 300);
        param.putByteArray("pinLimit", transactionConfig.getPinRule());
        //param.putByte("pinBlockFormat", ...);
        //param.putInt("betweenPinKeyTimeout", ...);

        PinpadForDUKPT.getInstance().open();
        PinpadForDUKPT.getInstance().startOfflinePinEntry(param, new OnPinEntryListener.Stub() {

            @Override
            public void onInput(int len, int key) throws RemoteException {
                Log.d(TAG, "----- onInput -----");
                Log.d(TAG, "len:" + len + ", key:" + key);

                Beeper.getInstance().startBeep(200);
            }

            @Override
            public void onError(int error) throws RemoteException {
                Log.d(TAG, "----- onError -----");
                Log.d(TAG, "error:" + error);

                sendMessage(PIN_EVENT_INPUT_ERROR, error);
            }

            @Override
            public void onConfirm(byte[] data, boolean isNonePin) throws RemoteException {
                Log.d(TAG, "----- onConfirm -----");
                Log.d(TAG, "isNonePin:" + isNonePin);

                sendMessage(PIN_EVENT_INPUT_CONFIRM, data);
            }

            @Override
            public void onCancel() throws RemoteException {
                Log.d(TAG, "----- onCancel -----");

                sendMessage(PIN_EVENT_INPUT_CANCEL, null);
            }
        });
    }

    /**
     * Input online pin.
     */
    private void inputOnlinePin() throws RemoteException {

        onInputPin(getString(R.string.please_input_pin));

        Bundle param = new Bundle();
        param.putByteArray("panBlock", BytesUtil.hexString2Bytes(session.getPan()));
        param.putInt("timeout", 300);
        param.putByteArray("pinLimit", transactionConfig.getPinRule());
//        param.putByte("pinBlockFormat", ...);
//        param.putInt("betweenPinKeyTimeout", ...);

        PinpadForDUKPT.getInstance().open();
        PinpadForDUKPT.getInstance().startPinEntry(KeyId.mainKey, param, new OnPinEntryListener.Stub() {

            @Override
            public void onInput(int len, int key) throws RemoteException {
                Log.d(TAG, "----- onInput -----");
                Log.d(TAG, "len:" + len + ", key:" + key);

                Beeper.getInstance().startBeep(200);
            }

            @Override
            public void onError(int error) throws RemoteException {
                Log.d(TAG, "----- onError -----");
                Log.d(TAG, "error:" + error);

                sendMessage(PIN_EVENT_INPUT_ERROR, error);
            }

            @Override
            public void onConfirm(byte[] data, boolean isNonePin) throws RemoteException {
                Log.d(TAG, "----- onConfirm -----");
                Log.d(TAG, "isNonePin:" + isNonePin + "\r\n");

                sendMessage(PIN_EVENT_INPUT_CONFIRM, data);
            }

            @Override
            public void onCancel() throws RemoteException {
                Log.d(TAG, "----- onCancel -----");

                sendMessage(PIN_EVENT_INPUT_CANCEL, null);
            }
        });
    }

    /**
     * Handle verify offline pin.
     */
    private void handleVerifyOfflinePin(int flag, byte[] random, CAPublicKey publicKey, OfflinePinVerifyResult result) throws RemoteException {

        // Set offline pin verify params.
        byte fmtOfPin = (flag == 1 ? OfflinePinVerify.FOPTBV_ENCRYPTED_BY_PUBLIC_KEY : OfflinePinVerify.FOPTBV_PLAIN_TEXT);
        int icToken = 0; // 0 is default value
        byte cmdFmt = OfflinePinVerify.VCF_DEFAULT;
        OfflinePinVerify offlinePinVerify = new OfflinePinVerify(fmtOfPin, icToken, cmdFmt, random);
        Log.d(TAG, "OfflinePinVerify(" +
                fmtOfPin + "," +
                icToken + "," +
                cmdFmt + "," +
                BytesUtil.bytes2HexString(random) + ")");

        // Set pin public key params.
        PinPublicKey pinPublicKey = new PinPublicKey();
        if (flag == 1) {
            pinPublicKey.mRid = publicKey.getRid();
            pinPublicKey.mExp = publicKey.getExp();
            pinPublicKey.mExpiredDate = publicKey.getExpDate();
            pinPublicKey.mHash = publicKey.getHash();
            pinPublicKey.mHasHash = publicKey.getHashFlag();
            pinPublicKey.mIndex = publicKey.getIndex();
            pinPublicKey.mMod = publicKey.getMod();
            Log.d(TAG, "PinPublicKey(" +
                    BytesUtil.bytes2HexString(pinPublicKey.mRid) + "," +
                    BytesUtil.bytes2HexString(pinPublicKey.mExp) + "," +
                    BytesUtil.bytes2HexString(pinPublicKey.mExpiredDate) + "," +
                    BytesUtil.bytes2HexString(pinPublicKey.mHash) + "," +
                    pinPublicKey.mHasHash + "," +
                    pinPublicKey.mIndex + "," +
                    BytesUtil.bytes2HexString(pinPublicKey.mMod) + ")");
        }

        // Set pin verify result.
        PinVerifyResult pinVerifyResult = new PinVerifyResult();

        // Verify offline pin by pinpad
        try {
            PinpadForDUKPT.getInstance().open();
            PinpadForDUKPT.getInstance().verifyOfflinePin(offlinePinVerify, pinPublicKey, pinVerifyResult);
            PinpadForDUKPT.getInstance().close();
        } catch (RemoteException e) {
            result.setResult(EMV.VERIFY_OFFLINE_PIN_ERROR);
            return;
        }

        // Get the verify result from pinpad
        byte sw1 = pinVerifyResult.getSW1();
        byte sw2 = pinVerifyResult.getSW2();
        byte apduRet = pinVerifyResult.getAPDURet();
        Log.d(TAG, "APDU ret = " + BytesUtil.byte2HexString(apduRet) + ", SW1 = " + BytesUtil.byte2HexString(sw1) + ", SW2 = " + BytesUtil.byte2HexString(sw2));

        // Set SW and result to EMV
        result.setSW(sw1, sw2);
        if (apduRet == (byte) 0xE6 || apduRet == (byte) 0xE7) {
            // Set success result
            result.setResult(EMV.VERIFY_OFFLINE_PIN_SUCCESS);
        } else {
            // Set APDU result
            result.setResult(apduRet);
        }
    }

    /**
     * Handle swipe card.
     */
    private void handleSwipeCard(Bundle bundle) throws RemoteException {
        // Save card record
        session.setAccountEntryMode(Session.ACCOUNT_ENTRY_MODE_MAGCARD);
        session.setPan(bundle.getString("PAN"));
        session.setExpirationDate(bundle.getString("EXPIRED_DATE"));

        // Show card record.
        onConfirmCardRecord(session.getPan(), session.getExpirationDate());
    }

    /**
     * Handle pin error.
     */
    private void handlePinError(int error) throws RemoteException {
        // Show error
        showToast(getString(PinpadForDUKPT.getErrorId(error)));

        // Close pinpad
        PinpadForDUKPT.getInstance().close();

        // Whether not EMV process
        if (!isEMVProcess) {
            return;
        }

        EMV.getInstance().responseEvent(TLV.fromData(EMVTag.DEF_TAG_CHV_STATUS, new byte[]{PinpadForDUKPT.VERIFY_STATUS_FAIL}).toString());
    }

    /**
     * Handle pin cancel.
     */
    private void handlePinCancel() throws RemoteException {
        // Close pinpad
        PinpadForDUKPT.getInstance().close();

        // Whether not EMV process
        if (!isEMVProcess) {
            stopEMVProcess();
            return;
        }

        EMV.getInstance().responseEvent(TLV.fromData(EMVTag.DEF_TAG_CHV_STATUS, new byte[]{PinpadForDUKPT.VERIFY_STATUS_CANCEL}).toString());
    }

    /**
     * Handle pin confirm.
     */
    private void handlePinConfirm(byte[] data) throws RemoteException {
        // Close pinpad
        PinpadForDUKPT.getInstance().close();

        // Whether not EMV process
        if (!isEMVProcess) {
            sendMessage(MAG_EVENT_ONLINE_PROCESS, data);
            return;
        }

        // Not input PIN
        if (data == null || data.length == 0) {
            session.setPinEntryMode(Session.PIN_ENTRY_MODE_NOT_EXIST);
            EMV.getInstance().responseEvent(TLV.fromData(EMVTag.DEF_TAG_CHV_STATUS, new byte[]{PinpadForDUKPT.VERIFY_STATUS_BY_PASS_PIN}).toString());
            return;
        }

        // Input PIN
        Log.d(TAG, "data:" + BytesUtil.bytes2HexString(data));
        try {
            session.setPinBlock(BytesUtil.fromGBK(data));
        } catch (UnsupportedEncodingException e) {
            throw new RemoteException(e.getMessage());
        }
        session.setPinEntryMode(Session.PIN_ENTRY_MODE_EXIST);
        EMV.getInstance().responseEvent(TLV.fromData(EMVTag.DEF_TAG_CHV_STATUS, new byte[]{PinpadForDUKPT.VERIFY_STATUS_SUCCESS}).toString());
    }

    /**
     * Handle print finished.
     */
    private void handlePrintFinished() {
        onPrintFinished();
    }

    /**
     * Handle print error.
     */
    private void handlePrintError(int error) {
        showToast(getString(Printer.getErrorId(error)));
    }

    /**
     * Handle print HTML error.
     */
    private void handlePrintHtmlError(RemoteException e) {
        showToast(e.getLocalizedMessage());
    }

    /**
     * Receive message from EMV.
     */
    @Override
    protected void receiveMessage(Message message) {
        super.receiveMessage(message);

        try {
            switch (message.what) {
                case EMV_EVENT_WAIT_CARD:
                    handleWaitCard((int) message.obj);
                    break;

                case EMV_EVENT_CARD_CHECKED:
                    handleCardChecked((int) message.obj);
                    break;

                case EMV_EVENT_FINAL_SELECT:
                    handleFinalSelect((FinalData) message.obj);
                    break;

                case EMV_EVENT_READ_RECORD:
                    handleReadRecord((CardRecord) message.obj);
                    break;

                case EMV_EVENT_CARD_HOLDER_VERIFY:
                    handleCardHolderVerify((CVMMethod) message.obj);
                    break;

                case EMV_EVENT_ONLINE_PROCESS:
                case MAG_EVENT_ONLINE_PROCESS:
                    handleOnlineProcess();
                    break;

                case EMV_EVENT_END_PROCESS:
                    handleEndProcess(message.arg1, (TransData) message.obj);
                    break;

                case EMV_EVENT_SEND_OUT:
                    handleSendOut((int) message.obj);
                    break;

                case MAG_EVENT_SWIPE_CARD:
                    handleSwipeCard((Bundle) message.obj);
                    break;

                case PIN_EVENT_INPUT_CONFIRM:
                    handlePinConfirm((byte[]) message.obj);
                    break;

                case PIN_EVENT_INPUT_CANCEL:
                    handlePinCancel();
                    break;

                case PIN_EVENT_INPUT_ERROR:
                    handlePinError((int) message.obj);
                    break;

                case PRT_EVENT_ERROR:
                    handlePrintError((int) message.obj);
                    break;

                case PRT_EVENT_HTML_ERROR:
                    handlePrintHtmlError((RemoteException) message.obj);
                    break;

                case PRT_EVENT_FINISHED:
                    handlePrintFinished();
                    break;

                case COM_EVENT_COMPLETED:
                    handleCommunicationCompleted((String) message.obj);
                    break;
            }
        } catch (RemoteException e) {
            showToast(e.getLocalizedMessage());
            try {
                stopEMVProcess();
            } catch (RemoteException e1) {
                showToast(e1.getLocalizedMessage());
            }
        }
    }
}
