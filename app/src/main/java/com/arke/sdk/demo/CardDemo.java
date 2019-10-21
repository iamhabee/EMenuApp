package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.arke.sdk.view.MifareActivity;
import com.arke.sdk.R;
import com.arke.sdk.api.ICAT1604Reader;
import com.arke.sdk.api.ICAT1608Reader;
import com.arke.sdk.api.ICAT24CxxReader;
import com.arke.sdk.api.ICCpuReader;
import com.arke.sdk.api.ICPSamReader;
import com.arke.sdk.api.ICSIM4428Reader;
import com.arke.sdk.api.ICSIM4442Reader;
import com.arke.sdk.api.MagReader;
import com.arke.sdk.api.RFReader;
import com.arke.sdk.util.data.BytesUtil;
import com.usdk.apiservice.aidl.data.ApduResponse;
import com.usdk.apiservice.aidl.data.BytesValue;
import com.usdk.apiservice.aidl.data.IntValue;
import com.usdk.apiservice.aidl.icreader.OnInsertListener;
import com.usdk.apiservice.aidl.icreader.PowerMode;
import com.usdk.apiservice.aidl.icreader.VerifyFlag;
import com.usdk.apiservice.aidl.icreader.Voltage;
import com.usdk.apiservice.aidl.magreader.OnSwipeListener;
import com.usdk.apiservice.aidl.rfreader.CardType;
import com.usdk.apiservice.aidl.rfreader.OnPassListener;

import java.util.Arrays;

import static com.usdk.apiservice.aidl.icreader.KeyType.*;

/**
 * Card demo.
 */

public class CardDemo extends ApiDemo {

    private static final String TAG = "CardDemo";

    /**
     * The message is shown on alert dialog.
     */
    private int dialogMessage;

    /**
     * Rf card type.
     */
    private int rfCardType;

    private Handler handler;

    /**
     * Constructor.
     */
    private CardDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        showDialog((String)msg.obj, true);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * Get card demo instance.
     */
    public static CardDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new CardDemo(context, toast, dialog);
    }

    /**
     * Do card functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.magnetic_stripe_card))) {
            searchMagCard();

        } else if (value.equals(getContext().getString(R.string.cpu_ic_card))) {
            searchICCard();

        } else if (value.equals(getContext().getString(R.string.indonesia_rf_card))) {
            searchIndonesiaCard();

        } else if (value.equals(getContext().getString(R.string.psam_ic_card))) {
            searchPSamCard();

        } else if (value.equals(getContext().getString(R.string.at1604_ic_card))) {
            searchAT1604Card(); // TODO: 2017/7/10 only support 102 card now

        } else if (value.equals(getContext().getString(R.string.at1608_ic_card))) {
            searchAT1608Card();

        } else if (value.equals(getContext().getString(R.string.at24cxx_ic_card))) {
            searchAT24CXXCard();

        } else if (value.equals(getContext().getString(R.string.sim4428_ic_card))) {
            searchSIM4428Card();

        } else if (value.equals(getContext().getString(R.string.sim4442_ic_card))) {
            searchSIM4442Card();

        } else if (value.equals(getContext().getString(R.string.rf_card))) {
            searchRFCard();

        } else if (value.equals(getContext().getString(R.string.is_card_in))) {
            checkCardIn();
        }
    }

    private void searchIndonesiaCard() throws RemoteException {
        // Show dialog
        dialogMessage = R.string.please_present_card;
        showDialog(onDismissListener, dialogMessage);

        // Search RF card
        RFReader.getInstance().searchCard(new OnPassListener.Stub() {

            @Override
            public void onCardPass(int type) throws RemoteException {
                Log.d(TAG, "----- onCardPass -----");
                rfCardType = type;
                String message = "card type : " + rfCardType(type) + "\r\n";
                try {
                    switch (type) {
                        case CardType.S50_CARD:
                            message += handleS50S70Card(type);
                            break;

                        case CardType.S70_CARD:
                            message += handleS50S70Card(type);
                            break;

                        case CardType.PRO_CARD:
                        case CardType.S50_PRO_CARD:
                        case CardType.S70_PRO_CARD:
                        case CardType.CPU_CARD:
                            message += handleIndonesiaProCpuCard(type);
                            break;

                        default:
                            message += R.string.unknown_card;
                            break;
                    }

                } catch (RemoteException e) {
                    message += e.getLocalizedMessage();
                }

                RFReader.getInstance().halt();

                hideDialog();
//                showToast(message);
                Message msg =new Message();
                msg.obj = message;
                handler.sendMessage(msg);
            }

            @Override
            public void onFail(int error) throws RemoteException {
                Log.d(TAG, "----- onFail -----");

                RFReader.getInstance().halt();

                hideDialog();
                showToast(RFReader.getErrorId(error));
            }
        });
    }

    /**
     * Search PSam card.
     */
    private void searchPSamCard() throws RemoteException {
        String message;

        // Init module
        Log.d(TAG, "searchPSamCard: initModule");
        ICPSamReader.getInstance().initModule(Voltage.ICCpuCard.VOL_DEFAULT, PowerMode.DEFAULT);

        // Power up
        Log.d(TAG, "searchPSamCard: powerUp");
        BytesValue atr = new BytesValue();
        IntValue protocol = new IntValue();
        ICPSamReader.getInstance().powerUp(atr, protocol);

        // Exchange APDU
        Log.d(TAG, "searchPSamCard: exchangeApdu");
//        byte[] cmdHead = {0x00, (byte) 0xa4, 0x04, 0x00, 0x0e};
//        byte[] fileName = "1PAY.SYS.DDF01".getBytes();
        byte[] cmdHead = {0x00, (byte) 0xa4, 0x04, 0x00, 0x09};
        byte[] fileName = BytesUtil.hexString2Bytes("A00000000000000011");
        byte[] cmd = BytesUtil.merge(cmdHead, fileName, new byte[]{0x00});
        ApduResponse apduResponse = ICPSamReader.getInstance().exchangeApdu(cmd);
        message = "APDU ret:" + apduResponse.getAPDURet() + "|" + apduResponse.getSW1() + "|" + apduResponse.getSW2();

        // Power down
        Log.d(TAG, "searchPSamCard: powerDown");
        ICPSamReader.getInstance().powerDown();

        // Show message
        showToast(message);
    }

    /**
     * Search SIM4442 card.
     */
    private void searchSIM4442Card() throws RemoteException {
        // Power up
        Log.d(TAG, "searchSIM4442Card: powerUp");
        BytesValue atr = new BytesValue();
        ICSIM4442Reader.getInstance().powerUp(Voltage.SIM4428Card.VOL_5, atr);

        // Read the number of verifying password error
        Log.d(TAG, "searchSIM4442Card: readError");
        IntValue error = new IntValue();
        ICSIM4442Reader.getInstance().readError(error);
        if (error.getData() >= 6) {
            throw new RemoteException(getContext().getString(R.string.verify_fail) + ":" + error.getData());
        }

        // Verify
        Log.d(TAG, "searchSIM4442Card: verify");
        byte[] password = BytesUtil.hexString2ByteArray("FFFFFF");
        IntValue verifyError = new IntValue();
        ICSIM4442Reader.getInstance().verify(password, verifyError);
        if (verifyError.getData() != 0) {
            throw new RemoteException(getContext().getString(R.string.verify_fail));
        }

        // Write data
        Log.d(TAG, "searchSIM4442Card: write");
        byte[] writeBytes = BytesUtil.hexString2ByteArray("03");
        ICSIM4442Reader.getInstance().write(15, writeBytes);

        // Read data
        Log.d(TAG, "searchSIM4442Card: read");
        BytesValue readData = new BytesValue();
        ICSIM4442Reader.getInstance().read(15, writeBytes.length, readData);

        // Power down
        Log.d(TAG, "searchSIM4442Card: powerDown");
        ICSIM4442Reader.getInstance().powerDown();

        // Check and show message
        Log.d(TAG, "searchSIM4442Card: equals(" + BytesUtil.byteArray2HexString(readData.getData()) + "," + BytesUtil.byteArray2HexString(writeBytes) + ")");
        showToast(String.valueOf(Arrays.equals(readData.getData(), writeBytes)));
    }

    /**
     * Search SIM4428 card.
     */
    private void searchSIM4428Card() throws RemoteException {
        // Power up
        Log.d(TAG, "searchSIM4428Card: powerUp");
        BytesValue atr = new BytesValue();
        ICSIM4428Reader.getInstance().powerUp(Voltage.SIM4428Card.VOL_5, atr);

        // Read the number of verifying password error
        Log.d(TAG, "searchSIM4428Card: readError");
        IntValue error = new IntValue();
        ICSIM4428Reader.getInstance().readError(error);
        if (error.getData() >= 6) {
            throw new RemoteException(getContext().getString(R.string.verify_fail) + ":" + error.getData());
        }

        // Verify
        Log.d(TAG, "searchSIM4428Card: verify");
        byte[] password = BytesUtil.hexString2ByteArray("FFFF");
        IntValue verifyError = new IntValue();
        ICSIM4428Reader.getInstance().verify(password, verifyError);
        if (verifyError.getData() != 0) {
            throw new RemoteException(getContext().getString(R.string.verify_fail));
        }

        // Write data
        Log.d(TAG, "searchSIM4428Card: write");
        byte[] writeBytes = BytesUtil.hexString2ByteArray("03030304");
        ICSIM4428Reader.getInstance().write(0x00, 105, writeBytes);

        // Read data
        Log.d(TAG, "searchSIM4428Card: read");
        BytesValue readData = new BytesValue();
        ICSIM4428Reader.getInstance().read(105, writeBytes.length, readData);

        // Power down
        Log.d(TAG, "searchSIM4428Card: powerDown");
        ICSIM4428Reader.getInstance().powerDown();

        // Check and show message
        Log.d(TAG, "searchSIM4428Card: equals(" + BytesUtil.byteArray2HexString(readData.getData()) + "," + BytesUtil.byteArray2HexString(writeBytes) + ")");
        showToast(String.valueOf(Arrays.equals(readData.getData(), writeBytes)));
    }

    /**
     * Search AT1608 card.
     */
    private void searchAT1608Card() throws RemoteException {
        int userZone = 0;

        // Power up
        Log.d(TAG, "searchAT1608Card: powerUp");
        BytesValue atr = new BytesValue();
        ICAT1608Reader.getInstance().powerUp(Voltage.AT1608Card.VOL_5, atr);

        // Set user zone
        Log.d(TAG, "searchAT1608Card: serUserZone");
        ICAT1608Reader.getInstance().setUserZone(userZone);

        // Read the number of writing error
        Log.d(TAG, "searchAT1608Card: readError, write pwd");
        IntValue writeError = new IntValue();
        ICAT1608Reader.getInstance().readError(VerifyFlag.WRITEPWD, writeError);
        if (writeError.getData() >= 6) {
            throw new RemoteException(getContext().getString(R.string.write_fail) + ":" + writeError.getData());
        }

        // Read the number of reading error
        Log.d(TAG, "searchAT1608Card: readError, read pwd");
        IntValue readError = new IntValue();
        ICAT1608Reader.getInstance().readError(VerifyFlag.READPWD, readError);
        if (readError.getData() >= 6) {
            throw new RemoteException(getContext().getString(R.string.read_fail) + ":" + readError.getData());
        }

        // Power down
        Log.d(TAG, "searchAT1608Card: powerDown");
        ICAT1608Reader.getInstance().powerDown();

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Search AT1604 card.
     */
    private void searchAT1604Card() throws RemoteException {
        int userZone = 27; // User zone 1: 27-1221

        // Power up
        Log.d(TAG, "searchAT1604Card: powerUp");
        BytesValue atr = new BytesValue();
        ICAT1604Reader.getInstance().powerUp(Voltage.AT1604Card.VOL_5, atr);

        // Verify
        Log.d(TAG, "searchAT1604Card: verify");
        byte[] password = BytesUtil.hexString2ByteArray("F0F0");
        IntValue verifyError = new IntValue();
        ICAT1604Reader.getInstance().verify(SC, password, verifyError);
        if (verifyError.getData() != 0) {
            throw new RemoteException(getContext().getString(R.string.verify_fail));
        }

        // Write data
        Log.d(TAG, "searchAT1604Card: write");
        byte[] writeBytes = BytesUtil.hexString2ByteArray("313a1111110011001100f0f0ff121212");
        ICAT1604Reader.getInstance().write(userZone, writeBytes);

        // Read data
        Log.d(TAG, "searchAT1604Card: read");
        BytesValue readData = new BytesValue();
        ICAT1604Reader.getInstance().read(userZone, writeBytes.length, readData);

        // Power down
        Log.d(TAG, "searchAT1604Card: powerDown");
        ICAT1604Reader.getInstance().powerDown();

        // Check and show message
        Log.d(TAG, "searchAT1604Card: equals(" + BytesUtil.byteArray2HexString(readData.getData()) + "," + BytesUtil.byteArray2HexString(writeBytes) + ")");
        showToast(String.valueOf(Arrays.equals(readData.getData(), writeBytes)));
    }

    /**
     * Search AT24CXX card.
     */
    private void searchAT24CXXCard() throws RemoteException {
        // Power up
        Log.d(TAG, "searchAT24CXXCard: powerUp");
        ICAT24CxxReader.getInstance().powerUp(Voltage.AT24CxxCard.VOL_5);

        // Write data
        Log.d(TAG, "searchAT24CXXCard: write");
        byte[] writeBytes = BytesUtil.hexString2ByteArray("000102030405060708090A0B0C0D0E0F");
        ICAT24CxxReader.getInstance().write(0, writeBytes);

        // Read data
        Log.d(TAG, "searchAT24CXXCard: read");
        BytesValue readData = new BytesValue();
        ICAT24CxxReader.getInstance().read(0, writeBytes.length, readData);

        // Power down
        Log.d(TAG, "searchAT24CXXCard: powerDown");
        ICAT24CxxReader.getInstance().powerDown();

        // Check and show message
        Log.d(TAG, "searchAT24CXXCard: equals(" + BytesUtil.byteArray2HexString(readData.getData()) + "," + BytesUtil.byteArray2HexString(writeBytes) + ")");
        showToast(String.valueOf(Arrays.equals(readData.getData(), writeBytes)));
    }

    /**
     * Check card in.
     */
    private void checkCardIn() throws RemoteException {
        // Check
        boolean isSuccessful = ICCpuReader.getInstance().isCardIn();

        // Show message
        showToast(String.valueOf(isSuccessful));
    }

    /**
     * Search RF card.
     */
    private void searchRFCard() throws RemoteException {
        // Show dialog
        dialogMessage = R.string.please_present_card;
        showDialog(onDismissListener, dialogMessage);

        // Search RF card
        RFReader.getInstance().searchCard(new OnPassListener.Stub() {

            @Override
            public void onCardPass(int type) throws RemoteException {
                Log.d(TAG, "----- onCardPass -----");
                rfCardType = type;
                String message = "card type : " + rfCardType(type) + "\r\n";
                try {
                    switch (type) {
                        case CardType.S50_CARD:
                            message += handleS50S70Card(type);
                            break;

                        case CardType.S70_CARD:
                            message += handleS50S70Card(type);
                            break;

                        case CardType.PRO_CARD:
                        case CardType.S50_PRO_CARD:
                        case CardType.S70_PRO_CARD:
                            message += handleProCpuCard(type);
                            break;

                        case CardType.CPU_CARD:
                            message += handleProCpuCard(type);
                            break;

                        default:
                            message += R.string.unknown_card;
                            break;
                    }

                } catch (RemoteException e) {
                    message += e.getLocalizedMessage();
                }

                // 2017.11.08 Mifare卡的操作放另外一个activity，此处不能调用halt方法
//                RFReader.getInstance().halt();

                hideDialog();
                showToast(message);
            }

            @Override
            public void onFail(int error) throws RemoteException {
                Log.d(TAG, "----- onFail -----");

                RFReader.getInstance().halt();

                hideDialog();
                showToast(RFReader.getErrorId(error));
            }
        });
    }

    /**
     * Search IC card.
     */
    private void searchICCard() throws RemoteException {
        // Show dialog
        dialogMessage = R.string.please_insert_card;
        showDialog(onDismissListener, dialogMessage);

        // Search IC card
        ICCpuReader.getInstance().searchCard(new OnInsertListener.Stub() {

            @Override
            public void onCardInsert() throws RemoteException {
                Log.d(TAG, "----- onCardInsert -----");

                // Init module
                ICCpuReader.getInstance().initModule(Voltage.ICCpuCard.VOL_DEFAULT, PowerMode.DEFAULT);

                // Power up
                try {
                    BytesValue atr = new BytesValue();
                    IntValue protocol = new IntValue();
                    ICCpuReader.getInstance().powerUp(atr, protocol);
                } catch (RemoteException e) {
                    hideDialog();
                    showToast(e.getLocalizedMessage());
                    return;
                }

                // Exchange APDU
                String message;
                byte[] cmdHead = {0x00, (byte) 0xa4, 0x04, 0x00, 0x0e};
                byte[] fileName = "1PAY.SYS.DDF01".getBytes();
//                byte[] cmdHead = {0x00, (byte) 0xa4, 0x04, 0x00, 0x07};
//                byte[] fileName = BytesUtil.hexString2Bytes("A0000000032010"); // Visa electron aid
                byte[] cmd = BytesUtil.merge(cmdHead, fileName, new byte[]{0x00});
                try {
                    ApduResponse apduResponse = ICCpuReader.getInstance().exchangeApdu(cmd);
                    message = "APDU ret:" + apduResponse.getAPDURet() + "|" + apduResponse.getSW1() + "|" + apduResponse.getSW2();
                } catch (RemoteException e) {
                    message = e.getLocalizedMessage();
                }

                // Power down
                try {
                    ICCpuReader.getInstance().powerDown();
                } catch (RemoteException e) {
                    message = e.getLocalizedMessage();
                }

                hideDialog();
                showToast(message);
            }

            @Override
            public void onFail(int error) throws RemoteException {
                Log.d(TAG, "----- onFail -----");

                hideDialog();
                showToast(ICCpuReader.getErrorId(error));
            }
        });
    }

    /**
     * Search mag card.
     */
    private void searchMagCard() throws RemoteException {
        // Show dialog
        dialogMessage = R.string.please_swipe_card;
        showDialog(onDismissListener, dialogMessage);

        // Search mag card
        MagReader.getInstance().searchCard(60, new OnSwipeListener.Stub() {

            @Override
            public void onSuccess(Bundle bundle) throws RemoteException {
                Log.d(TAG, "----- onSuccess -----");

                String message = "PAN:" + bundle.getString(MagReader.PAN) + "\r\n";
                message += "TRACK1:" + bundle.getString(MagReader.TRACK1) + "\r\n";
                message += "TRACK2:" + bundle.getString(MagReader.TRACK2) + "\r\n";
                message += "TRACK3:" + bundle.getString(MagReader.TRACK3) + "\r\n";
                message += "SERVICE_CODE:" + bundle.getString(MagReader.SERVICE_CODE) + "\r\n";
                message += "EXPIRED_DATE:" + bundle.getString(MagReader.EXPIRED_DATE) + "\r\n";

                hideDialog();
                showToast(message);
            }

            @Override
            public void onError(int error) throws RemoteException {
                Log.d(TAG, "----- onError -----");

                hideDialog();
                showToast(MagReader.getErrorId(error));
            }

            @Override
            public void onTimeout() throws RemoteException {
                Log.d(TAG, "----- onTimeout -----");

                hideDialog();
                showToast(R.string.timeout);
            }
        });
    }

    /**
     * Handle PRO/CPU card.
     */
    private String handleProCpuCard(int cardType) throws RemoteException {
        // Activate
        BytesValue responseData = new BytesValue();
        RFReader.getInstance().activate(cardType, responseData);

        // Exchange APDU
        byte[] cmdHead = {0x00, (byte) 0xa4, 0x04, 0x00, 0x0e};
        byte[] fileName = "2PAY.SYS.DDF01".getBytes();
//        byte[] cmdHead = {0x00, (byte) 0xa4, 0x04, 0x00, 0x08};
//        byte[] fileName = BytesUtil.hexString2Bytes("A000000172950001");
        byte[] cmd = BytesUtil.merge(cmdHead, fileName, new byte[]{0x00});
        ApduResponse data = RFReader.getInstance().exchangeApdu(cmd);
        return "APDU ret:" + data.getAPDURet() + "|" + data.getSW1() + "|" + data.getSW2();
    }

    private String handleIndonesiaProCpuCard(int cardType) throws RemoteException {
        // Activate
        BytesValue responseData = new BytesValue();
        RFReader.getInstance().activate(cardType, responseData);

        // Exchange APDU
        byte[] getRandomCmd = {0x00, (byte)0x84, 0x00, 0x00, 0x08};
        byte[] getSnCmd = {0x00, (byte)0xCA, 0x01, 0x01, 0x08};
        ApduResponse random = RFReader.getInstance().exchangeApdu(getRandomCmd);
        ApduResponse sn = RFReader.getInstance().exchangeApdu(getSnCmd);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Random: ");
        stringBuilder.append(BytesUtil.byteArray2HexString((random.getData())));
        stringBuilder.append("\n");
        stringBuilder.append("SN: ");
        stringBuilder.append(BytesUtil.byteArray2HexString((sn.getData())));
        return stringBuilder.toString();
    }

    /**
     * Handle S50/S70 card.
     */
    private String handleS50S70Card(int cardType) throws RemoteException {
        BytesValue responseData = new BytesValue();
//        byte[] aucMifKeyB0 = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
//        byte[] writeData = {0x05, 0x00, 0x00, 0x00, (byte) 0xFA, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x05, 0x00, 0x00, 0x00, 0x01, (byte) 0xFE, 0x01, (byte) 0xFE};
//        BytesValue readData = new BytesValue();
        byte[] uid;

        // Activate
        RFReader.getInstance().activate(cardType, responseData);

        uid = RFReader.getInstance().getCardSerialNo(responseData.getData());

        // Auth sector
//        RFReader.getInstance().authSector(7, KEY_A, aucMifKeyB0);
//
//        // Auth block
//        RFReader.getInstance().authBlock(30, KEY_A, aucMifKeyB0);
//
//        // Write block
//        RFReader.getInstance().writeBlock(30, writeData);
//
//        // Read block
//        RFReader.getInstance().readBlock(30, readData);
//
//        // Increase value
//        RFReader.getInstance().increaseValue(30, 2);
//
//        // Decrease value
//        RFReader.getInstance().decreaseValue(30, 2);

        startMifareActivity(cardType, BytesUtil.bytes2HexString(uid));

        return "UID["+ BytesUtil.bytes2HexString(uid) + "]" + "\r\n" + getContext().getString(R.string.succeed);
    }

    /**
     * RF card type.
     * @param cardType
     * @return
     */
    private String rfCardType(int cardType) {
        String card = "unknow";

        switch (cardType) {
            case CardType.S50_CARD:
                card = "S50";
                break;
            case CardType.S70_CARD:
                card = "S70";
                break;
            case CardType.PRO_CARD:
                card = "PRO";
                break;
            case CardType.S50_PRO_CARD:
                card = "S50_PRO";
                break;
            case CardType.S70_PRO_CARD:
                card = "S70_PRO";
                break;
            case CardType.CPU_CARD:
                card = "CPU";
                break;
            default:
                break;
        }

        return card;
    }

    /**
     * Start mifare activity.
     *
     * @param uid
     */
    private void startMifareActivity(int cardType, String uid) {
        Intent intent = new Intent(getContext(), MifareActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("UID", uid);
        intent.putExtra("CardType", cardType);
        getContext().startActivity(intent);
    }

    /**
     * Dismiss listener for the dialog.
     */
    private DialogInterface.OnDismissListener onDismissListener = new DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            switch (dialogMessage) {
                case R.string.please_swipe_card:
                    try {
                        MagReader.getInstance().stopSearch();
                    } catch (RemoteException e) {
                        showToast(e.getLocalizedMessage());
                    }
                    break;

                case R.string.please_insert_card:
                    try {
                        ICCpuReader.getInstance().stopSearch();
                    } catch (RemoteException e) {
                        showToast(e.getLocalizedMessage());
                    }
                    break;

                case R.string.please_present_card:
                    if (rfCardType == CardType.S50_CARD || rfCardType == CardType.S70_CARD) {
                        break;
                    }

                    try {
                        RFReader.getInstance().stopSearch();
                        RFReader.getInstance().halt();
                    } catch (RemoteException e) {
                        showToast(e.getLocalizedMessage());
                    }
                    break;
            }
        }
    };
}
