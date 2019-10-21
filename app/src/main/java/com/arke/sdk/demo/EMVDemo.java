package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.api.DeviceManager;
import com.arke.sdk.api.EMV;
import com.arke.sdk.util.data.BytesUtil;
import com.arke.sdk.util.data.StringUtil;
import com.arke.sdk.util.data.TLV;
import com.arke.sdk.util.data.TLVList;
import com.arke.sdk.util.emv.EmvData;
import com.usdk.apiservice.aidl.constants.RFDeviceName;
import com.usdk.apiservice.aidl.emv.ACType;
import com.usdk.apiservice.aidl.emv.ActionFlag;
import com.usdk.apiservice.aidl.emv.CAPublicKey;
import com.usdk.apiservice.aidl.emv.CVMMethod;
import com.usdk.apiservice.aidl.emv.CandidateAID;
import com.usdk.apiservice.aidl.emv.CardRecord;
import com.usdk.apiservice.aidl.emv.DOLType;
import com.usdk.apiservice.aidl.emv.ECCardLog;
import com.usdk.apiservice.aidl.emv.EMVError;
import com.usdk.apiservice.aidl.emv.EMVEventHandler;
import com.usdk.apiservice.aidl.emv.EMVTag;
import com.usdk.apiservice.aidl.emv.FinalData;
import com.usdk.apiservice.aidl.emv.ICCardLog;
import com.usdk.apiservice.aidl.emv.KernelID;
import com.usdk.apiservice.aidl.emv.OfflinePinVerifyResult;
import com.usdk.apiservice.aidl.emv.SearchCardListener;
import com.usdk.apiservice.aidl.emv.TransData;
import com.usdk.apiservice.aidl.emv.WaitCardFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EMV demo.
 */

public class EMVDemo extends ApiDemo {

    private static final String TAG = "EMVDemo";

    /**
     * Is in EMV process.
     */
    private boolean isEmvProcess;

    /**
     * Is show DOL.
     */
    private boolean isShowDOL;

    /**
     * Is show TLV.
     */
    private boolean isShowTLV;

    /**
     * Is show data APDU.
     */
    private boolean isShowDataAPDU;

    /**
     * Is show balance.
     */
    private boolean isShowBalance;

    /**
     * Is get ECC log process.
     */
    private boolean isGetECCLogProcess;

    /**
     * Is get ICC log process.
     */
    private boolean isGetICCLogProcess;

    /**
     * Result message.
     */
    private String resultMessage;

    /**
     * Constructor.
     */
    private EMVDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get EMV demo instance.
     */
    public static EMVDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new EMVDemo(context, toast, dialog);
    }

    /**
     * Do EMV functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.start_process))) {
            startProcess();

        } else if (value.equals(getContext().getString(R.string.get_balance))) {
            getBalance();

        } else if (value.equals(getContext().getString(R.string.get_dol))) {
            getDOL();

        } else if (value.equals(getContext().getString(R.string.get_tlv))) {
            getTLV();

        } else if (value.equals(getContext().getString(R.string.get_data_apdu))) {
            getDataAPDU();
            
        } else if (value.equals(getContext().getString(R.string.get_icc_log))) {
            getICCLog();

        } else if (value.equals(getContext().getString(R.string.get_ecc_log))) {
            getECCLog();
        }
    }

    /**
     * Get ICC log.
     */
    private void getICCLog() throws RemoteException {
        isGetICCLogProcess = true;
        startProcess();
    }

    /**
     * Get ECC log.
     */
    private void getECCLog() throws RemoteException {
        isGetECCLogProcess = true;
        startProcess();
    }

    /**
     * Get data APDU.
     */
    private void getDataAPDU() throws RemoteException {
        isShowDataAPDU = true;
        startProcess();
    }

    /**
     * Get TLV.
     */
    private void getTLV() throws RemoteException {
        isShowTLV = true;
        startProcess();
    }

    /**
     * Get DOL.
     */
    private void getDOL() throws RemoteException {
        isShowDOL = true;
        startProcess();
    }

    /**
     * Get balance.
     */
    private void getBalance() throws RemoteException {
        isShowBalance = true;
        startProcess();
    }

    /**
     * Start process.
     */
    private void startProcess() throws RemoteException {
        isEmvProcess = true;
        resultMessage = "";

        // Show dialog
        if (! (isShowDOL || isShowTLV)) {
            showDialog(onDismissListener, R.string.waiting_for_card);
        }

        // Start EMV process
        Bundle bundle = new Bundle();
        bundle.putByte("flagPSE", EMV.PSE_AID_LIST);
        bundle.putByte("flagCtlAsCb", EMV.DISABLE_CONTACTLESS_CARD_SELECT_APP);
        bundle.putBoolean("flagICCLog", isGetECCLogProcess || isGetICCLogProcess);
        EMV.getInstance().startProcess(bundle, onEmvEventHandler);
    }

    /**
     * Stop process.
     */
    private void stopProcess() throws RemoteException {
        if (! (isShowDOL || isShowTLV)) {
            EMV.getInstance().stopSearch();
            EMV.getInstance().halt();
        }

        if (isEmvProcess) {
            EMV.getInstance().stopProcess();
        }
    }

    /**
     * Search card.
     */
    private void searchCard() throws RemoteException {
        // Start searching card
        Bundle bundle = new Bundle();
        bundle.putBoolean("supportICCard", true);
        bundle.putBoolean("supportRFCard", true);
        bundle.putBoolean("supportMagCard", true);
        if (DeviceManager.MODEL_AECR_C10.equals(DeviceManager.getInstance().getModel())) {
            bundle.putString("rfDeviceName", RFDeviceName.EXTERNAL);
        } else {
            bundle.putString("rfDeviceName", RFDeviceName.INNER);
        }
        EMV.getInstance().searchCard(bundle, 60, searchCardListener);
    }

    /**
     * Research RF card.
     */
    private void researchRFCard() throws RemoteException {
        // Start searching RF card
        Bundle bundle = new Bundle();
        bundle.putBoolean("supportICCard", false);
        bundle.putBoolean("supportRFCard", true);
        bundle.putBoolean("supportMagCard", false);
        if (DeviceManager.MODEL_AECR_C10.equals(DeviceManager.getInstance().getModel())) {
            bundle.putString("rfDeviceName", RFDeviceName.EXTERNAL);
        } else {
            bundle.putString("rfDeviceName", RFDeviceName.INNER);
        }
        EMV.getInstance().searchCard(bundle, 60, searchCardListener);
    }

    /**
     * Callback function for EMV transaction flow.
     */
    private EMVEventHandler onEmvEventHandler = new EMVEventHandler.Stub() {
        @Override
        public void onInitEMV() throws RemoteException {
            Log.d(TAG, "----- onInitEMV -----");

            // Clear all the aids in EMV kernel.
            EMV.getInstance().manageAID(ActionFlag.CLEAR, null, true);
            // Add all aids from EMV data to EMV kernel.
            for (Map.Entry<String, Boolean> entry : EmvData.aids.entrySet()) {
                EMV.getInstance().manageAID(ActionFlag.ADD, entry.getKey(), entry.getValue());
            }

            // Set DOL
            EMV.getInstance().setDOL(DOLType.DDOL, "9F3704");

            // Set TLV
            EMV.getInstance().setTLV(KernelID.PBOC, EMVTag.EMV_TAG_TM_TERMTYPE, "22");

            // Set TLV list
            TLVList tlvList = new TLVList();
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_CAP, BytesUtil.hexString2Bytes("E0F1C8")));
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_CAP_AD, BytesUtil.hexString2Bytes("6F00F0F001")));
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_CNTRYCODE, BytesUtil.hexString2Bytes("0156")));
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_CURCODE, BytesUtil.hexString2Bytes("0156")));
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_FLOORLMT, BytesUtil.hexString2Bytes("00000000")));
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_TAC_DECLINE, BytesUtil.hexString2Bytes("0000000000")));
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_TAC_ONLINE, BytesUtil.hexString2Bytes("FFFFFFFFFF")));
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_TAC_DEFAULT, BytesUtil.hexString2Bytes("FFFFFFFFFF")));
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_RAND_SLT_THRESHOLD, BytesUtil.hexString2Bytes("000000000000")));
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_RAND_SLT_PER, BytesUtil.hexString2Bytes("00")));
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_RAND_SLT_MAXPER, BytesUtil.hexString2Bytes("99")));
            tlvList.addTLV(TLV.fromData(EMVTag.C_TAG_TM_9F66, BytesUtil.hexString2Bytes("26000080")));
            tlvList.addTLV(TLV.fromData(EMVTag.C_TAG_TM_9F7A, BytesUtil.hexString2Bytes("01")));
            tlvList.addTLV(TLV.fromData(EMVTag.C_TAG_TM_9F7B, BytesUtil.hexString2Bytes("999999999999")));
            tlvList.addTLV(TLV.fromData(EMVTag.C_TAG_TM_DF69, BytesUtil.hexString2Bytes("01")));
            tlvList.addTLV(TLV.fromData(EMVTag.C_TAG_TM_TRANS_LIMIT, BytesUtil.hexString2Bytes("999999999999")));
            tlvList.addTLV(TLV.fromData(EMVTag.C_TAG_TM_CVM_LIMIT, BytesUtil.hexString2Bytes("999999999999")));
            tlvList.addTLV(TLV.fromData(EMVTag.C_TAG_TM_FLOOR_LIMIT, BytesUtil.hexString2Bytes("999999999999")));
            EMV.getInstance().setTLVList(KernelID.PBOC, tlvList.toString());
        }

        @Override
        public void onWaitCard(int flag) throws RemoteException {
            Log.d(TAG, "----- onWaitCard -----");
            Log.d(TAG, "flag : " + flag);

            // Get DOL and stop EMV
            if (isShowDOL) {
                // Get DOL
                isShowDOL = false;
                resultMessage = EMV.getInstance().getDOL(DOLType.DDOL);

                // Stop EMV process
                stopProcess();
                return;
            }

            // Get TLV and stop EMV
            if (isShowTLV) {
                // Get TLV
                isShowTLV = false;
                resultMessage = EMV.getInstance().getTLV(EMVTag.EMV_TAG_TM_TERMTYPE);

                // Stop EMV process
                stopProcess();
                return;
            }

            switch (flag) {
                case WaitCardFlag.NORMAL:
                    // This case would never happen, if you have already searchCard before startEMV.
                    // Otherwise it would happen for searching card.
                    searchCard();
                    break;

                case WaitCardFlag.ISS_SCRIPT_UPDATE:
                case WaitCardFlag.SHOW_CARD_AGAIN:
                    researchRFCard();
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
                                researchRFCard();
                            } catch (RemoteException | InterruptedException e) {
                                showToast(e.getLocalizedMessage());
                            }
                        }
                    }.start();
                    break;

                default:
                    // Stop EMV process
                    resultMessage = getContext().getString(R.string.unknown_error);
                    stopProcess();
                    break;
            }
        }

        @Override
        @Deprecated
        public void onCardChecked(int cardType) throws RemoteException {
            Log.d(TAG, "----- onCardChecked -----");
            Log.d(TAG, "cardType : " + cardType);

            // Only happen when use startProcess() which is deprecated
        }

        @Override
        public void onAppSelect(boolean reselect, List<CandidateAID> aids) throws RemoteException {
            Log.d(TAG, "----- onAppSelect -----");
            Log.d(TAG, "reselect : " + reselect);
            Log.d(TAG, "aidsSize : " + aids.size());

            // Get ECC log and stop EMV
            if (isGetECCLogProcess) {
                // Get ECC log
                isGetECCLogProcess = false;
                List<ECCardLog> ecLogs = new ArrayList<>();
                EMV.getInstance().getECCLog(aids.get(0).getAID(), ecLogs);
                if (ecLogs.size() > 0) {
                    for (ECCardLog ecCardLog : ecLogs) {
                        String date = "20" + BytesUtil.byteArray2HexString(ecCardLog.getDate());
                        String time = BytesUtil.byteArray2HexString(ecCardLog.getTime());
                        String beforeBalance = StringUtil.getReadableAmount(String.valueOf(Long.parseLong(BytesUtil.byteArray2HexString(ecCardLog.getPreValue()))));
                        String afterBalance = StringUtil.getReadableAmount(String.valueOf(Long.parseLong(BytesUtil.byteArray2HexString(ecCardLog.getAftValue()))));
                        resultMessage += date + "," + time + "," + beforeBalance + "," + afterBalance + "\r\n";
                    }
                } else {
                    resultMessage = getContext().getString(R.string.no_data);
                }

                // Stop EMV process
                stopProcess();
                return;
            }

            // Get ICC log and stop EMV
            if (isGetICCLogProcess) {
                // Get ICC log
                isGetICCLogProcess = false;
                List<ICCardLog> icLogs = new ArrayList<>();
                EMV.getInstance().getICCLog(aids.get(0).getAID(), icLogs);
                if (icLogs.size() > 0) {
                    for (ICCardLog icCardLog : icLogs) {
                        String date = "20" + BytesUtil.byteArray2HexString(icCardLog.getDate());
                        String time = BytesUtil.byteArray2HexString(icCardLog.getTime());
                        String amount = StringUtil.getReadableAmount(String.valueOf(Long.parseLong(BytesUtil.byteArray2HexString(icCardLog.getAmount()))));
                        resultMessage += date + "," + time + "," + amount + "\r\n";
                    }
                } else {
                    resultMessage = getContext().getString(R.string.no_data);
                }

                // Stop EMV process
                stopProcess();
                return;
            }

            // Response
            TLVList tlvList = new TLVList();
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_AID, aids.get(0).getAID()));
            EMV.getInstance().responseEvent(tlvList.toString());
        }

        @Override
        public void onFinalSelect(FinalData finalData) throws RemoteException {
            Log.d(TAG, "----- onFinalSelect -----");
            Log.d(TAG, "KernelID:" + finalData.getKernelID());
            Log.d(TAG, "AID:" + BytesUtil.bytes2HexString(finalData.getAID()));

            if (isShowDataAPDU) {
                // Get data APDU
                isShowDataAPDU = false;
                resultMessage = EMV.getInstance().getDataAPDU("9F51");

                // Stop EMV process
                stopProcess();
                return;
            }

            if (isShowBalance) {
                // Get balance
                isShowBalance = false;
                resultMessage = String.valueOf(EMV.getInstance().getBalance());

                // Stop EMV process
                stopProcess();
                return;
            }

            // Response
            TLVList tlvList = new TLVList();
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_AUTHAMNTN, BytesUtil.hexString2Bytes("000000012345")));
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_TRANSDATE, BytesUtil.hexString2Bytes("170209")));
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_TRANSTIME, BytesUtil.hexString2Bytes("000000")));
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_TRSEQCNTR, BytesUtil.hexString2Bytes("00000001")));
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_SERVICE_TYPE, BytesUtil.hexString2Bytes("00")));
            EMV.getInstance().responseEvent(tlvList.toString());
        }

        @Override
        public void onReadRecord(CardRecord cardRecord) throws RemoteException {
            Log.d(TAG, "----- onReadRecord -----");
            Log.d(TAG, "PAN:" + BytesUtil.bytes2HexString(cardRecord.getPan()));

            // CA Public Key
            CAPublicKey pubKey = new CAPublicKey();
            pubKey.setIndex((byte) 0x01);
            pubKey.setRid(BytesUtil.hexString2Bytes("A000000333"));
            pubKey.setMod(BytesUtil.hexString2Bytes("1234567890"));
            pubKey.setExp(BytesUtil.hexString2Bytes("03"));
            pubKey.setExpDate(BytesUtil.hexString2Bytes("20171212"));
            pubKey.setHashFlag((byte) 0x00);
            pubKey.setHash(BytesUtil.hexString2Bytes("1111111111111111111111111111111111111111"));
            EMV.getInstance().setCAPubKey(pubKey);

            // Response
            EMV.getInstance().responseEvent(null);
        }

        @Override
        public void onCardHolderVerify(CVMMethod cvmMethod) throws RemoteException {
            Log.d(TAG, "----- onCardHolderVerify -----");
            Log.d(TAG, "CVM:" + cvmMethod.getCVM());
            Log.d(TAG, "PINTimes:" + cvmMethod.getPINTimes());

            // Response
            TLVList tlvList = new TLVList();
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_CHV_STATUS, BytesUtil.hexString2Bytes("01")));
            EMV.getInstance().responseEvent(tlvList.toString());
        }

        @Override
        public void onOnlineProcess(TransData transData) throws RemoteException {
            Log.d(TAG, "----- onOnlineProcess -----");
            Log.d(TAG, "ACType:" + transData.getACType());
            Log.d(TAG, "CVM:" + transData.getCVM());
            Log.d(TAG, "FlowType:" + transData.getFlowType());

            // Response
            TLVList tlvList = new TLVList();
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_ONLINE_STATUS, BytesUtil.hexString2Bytes("00")));
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_AUTHORIZE_FLAG, BytesUtil.hexString2Bytes("01")));
            tlvList.addTLV(TLV.fromData(EMVTag.DEF_TAG_HOST_TLVDATA, transData.getTLVData()));
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_ARC, BytesUtil.hexString2Bytes("3030")));
            tlvList.addTLV(TLV.fromData(EMVTag.EMV_TAG_TM_AUTHCODE, BytesUtil.hexString2Bytes("000000000001")));
            EMV.getInstance().responseEvent(tlvList.toString());
        }

        @Override
        public void onEndProcess(int resultCode, TransData transData) throws RemoteException {
            Log.d(TAG, "----- onEndProcess -----");
            Log.d(TAG, "resultCode:" + resultCode);

            // Hide dialog
            isEmvProcess = false;
            hideDialog();

            // Show result message
            if (resultMessage != null && !resultMessage.isEmpty()) {
                showToast(resultMessage);
                return;
            }

            // Check process result
            if (resultCode != EMVError.SUCCESS) {
                showToast(EMV.getErrorId(resultCode));
                return;
            }

            // Show transaction message
            switch (transData.getACType()) {
                case ACType.EMV_ACTION_AAC:
                    showToast(R.string.transaction_declined);
                    break;
                case ACType.EMV_ACTION_TC:
                    showToast(R.string.transaction_approved);
                    break;
                case ACType.EMV_ACTION_ARQC:
                    showToast(R.string.request_online_authorization);
                    break;
            }
        }

        @Override
        public void onVerifyOfflinePin(int flag, byte[] random, CAPublicKey caPublicKey, OfflinePinVerifyResult offlinePinVerifyResult) throws RemoteException {
            Log.d(TAG, "----- onVerifyOfflinePin -----");
            Log.d(TAG, "flag : " + flag);

            // Response
            offlinePinVerifyResult.setSW(0x90, 0x00);
            offlinePinVerifyResult.setResult(EMV.VERIFY_OFFLINE_PIN_SUCCESS);
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
        }
    };

    /**
     * Search card listener.
     */
    private SearchCardListener searchCardListener = new SearchCardListener.Stub() {
        @Override
        public void onCardSwiped(Bundle bundle) throws RemoteException {
            Log.d(TAG, "----- onCardSwiped -----");
            Log.d(TAG, "PAN : " + bundle.getString("PAN"));
            Log.d(TAG, "TRACK1 : " + bundle.getString("TRACK1"));
            Log.d(TAG, "TRACK2 : " + bundle.getString("TRACK2"));
            Log.d(TAG, "TRACK3 : " + bundle.getString("TRACK3"));
            Log.d(TAG, "SERVICE_CODE : " + bundle.getString("SERVICE_CODE"));
            Log.d(TAG, "EXPIRED_DATE : " + bundle.getString("EXPIRED_DATE"));

            // Stop EMV process
            resultMessage = "PAN : " + bundle.getString("PAN") + "\n";
            resultMessage += "TRACK1 : " + bundle.getString("TRACK1") + "\n";
            resultMessage += "TRACK2 : " + bundle.getString("TRACK2") + "\n";
            resultMessage += "TRACK3 : " + bundle.getString("TRACK3") + "\n";
            resultMessage += "SERVICE_CODE : " + bundle.getString("SERVICE_CODE") + "\n";
            resultMessage += "EXPIRED_DATE : " + bundle.getString("EXPIRED_DATE") + "\n";
            stopProcess();
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
            resultMessage = getContext().getString(R.string.wait_card_timeout);
            stopProcess();
        }

        @Override
        public void onError(int error, String message) throws RemoteException {
            Log.d(TAG, "----- onError -----");
            Log.d(TAG, error + " : " + message);

            // Stop EMV process
            resultMessage = getContext().getString(EMV.getErrorId(error));
            stopProcess();
        }
    };

    /**
     * Dismiss listener for the dialog.
     */
    private DialogInterface.OnDismissListener onDismissListener = dialog -> {
        try {
            stopProcess();
        } catch (RemoteException e) {
            showToast(e.getLocalizedMessage());
        }
    };
}
