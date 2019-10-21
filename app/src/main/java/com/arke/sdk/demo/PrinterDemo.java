package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.util.printer.Printer;
import com.arke.sdk.util.transaction.TerminalInfo;
import com.usdk.apiservice.aidl.printer.ASCScale;
import com.usdk.apiservice.aidl.printer.ASCSize;
import com.usdk.apiservice.aidl.printer.AlignMode;
import com.usdk.apiservice.aidl.printer.ECLevel;
import com.usdk.apiservice.aidl.printer.HZScale;
import com.usdk.apiservice.aidl.printer.HZSize;
import com.usdk.apiservice.aidl.printer.OnPrintListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableCompletableObserver;

/**
 * Printer demo.
 */

public class PrinterDemo extends ApiDemo {

    private static final String TAG = "PrinterDemo";
    private static final int FONT_SIZE_SMALL = 0;
    private static final int FONT_SIZE_NORMAL = 1;
    private static final int FONT_SIZE_LARGE = 2;

    private static final String JSON_DATA = "{\"ACQUIRER\":\"收单行\",\"AMOUNT\":\"金额\",\"AUTH_NO\":\"授权码\",\"BATCH_NO\":\"批次号\",\"BONUS_POINTS\":\"奖励积分\",\"CARDHOLDER_SIGN\":\"持卡人签名(CARDHOLDER SIGNATURE)\",\"CARDHOLDER_SIGN_UPCASE\":\"持卡人签名(CARDHOLDER SIGNATURE)\",\"CARDHOLDER_TIP\":\"持卡人手续费\",\"CARD_NO\":\"卡号\",\"CASH_SERIAL_NUMBER\":\"收银流水号\",\"COMMODITY_CODE\":\"商品代码\",\"DATE_TIME\":\"日期时间\",\"DESKER\":\"结账数\",\"DOMESTIC_ACCOUNT_IS_BALANCED\":\"国内帐户平衡\",\"DOMESTIC_ACCOUNT_IS_UNBALANCED\":\"国内的帐户不平衡\",\"DOMESTIC_CARD\":\"内卡\",\"DOWN_PAYMENT_AMOUNT\":\"首付还款金额\",\"DUPLICATED\":\"重打印凭证/DUPLICATED\",\"ECASH_BALANCE\":\"电子现金余额\",\"EXCHANGE_INTEGRAL\":\"兑换积分数\",\"EXPIRY_DATE\":\"有效期\",\"FOREIGN_ACCOUNT_IS_BALANCED\":\"境外账户平衡\",\"FOREIGN_ACCOUNT_IS_UNBALANCED\":\"外卡不平衡\",\"FOREIGN_CARD\":\"外卡\",\"FULL_PAYMENT\":\"一次性支付\",\"INSTALLMENT\":\"分期付款\",\"ISSUER\":\"发卡行\",\"I_ACKNOWLEDGE_SATISFACTORY_OF_RELATIVE_GOODS_SERVICE\":\"本人确认以上交易,同意将其计入本卡账户\",\"MERCHANT_NAME\":\"商户名称\",\"MERCHANT_NO\":\"商户编号\",\"NUMBER\":\"笔数\",\"OLD_AUTH_NO\":\"原授权码\",\"OLD_DATE\":\"原交易日期\",\"OLD_REF_NO\":\"原参考号\",\"OLD_VOUCHER\":\"原凭证号\",\"OPERATOR_NO\":\"操作员号\",\"PAYMENT_AMOUNT\":\"自付金金额\",\"PHONE_NUMBER\":\"手机号\",\"REFERENCE\":\"备注\",\"REFERENCE_NO\":\"参考号\",\"REPAYMENT_CURRENCY\":\"还款币种\",\"RE_PRINT_THE_BILL\":\"重打印结算单\",\"RMB\":\"RMB\",\"SERVICE_CHARGE_OF_DOWN_PAYMENT\":\"首期手续费\",\"SERVICE_CHARGE_OF_EACH_PERIODS\":\"每期手续费\",\"SERVICE_CHARGE_PAYMENT_MODE\":\"手续费支付方式\",\"SERVICE_HOTLINE\":\"服务热线号码\",\"SETTLEMENT_STATISTIC\":\"结算统计\",\"STATISTIC_LIST\":\"统计单\",\"TERMINAL_NO\":\"终端编号\",\"THE_BALANCE_OF_INTEGRAL\":\"积分余额\",\"THE_NUMBER_OF_PERIODS\":\"分期期数\",\"TIP\":\"小费\",\"TOTAL\":\"总计(TOTAL)\",\"TOTAL_AMOUNT\":\"金额总计\",\"TOTAL_NUM\":\"笔数总计\",\"TRANSACTION_AMOUNT\":\"交易 AMT\",\"TRANSACTION_LINES\":\"交易笔数\",\"TRANSACTION_TYPE\":\"交易类型\",\"TRANSFER_CARD_NUMBER\":\"转入卡号\",\"TYPE\":\"类型\",\"VOUCHER_NO\":\"凭证号\",\"acquirer\":\"测试系统\",\"amtTrans\":\"0.12\",\"authCode\":\"A54321\",\"batchNo\":\"000003\",\"cardNo\":\"5187 10** **** 6333 /S\",\"copyType\":\"商户存根(MERCHANT COPY)\",\"dateTime\":\"2017/08/01 14:17:13\",\"hotline\":\"400-200-200\",\"icTagFlag\":false,\"issuer\":\"测试系统\",\"merchantName\":\"ABCDEFGHIJKLMNOPQRSTUV    12345678901234\",\"merchantNo\":\"123456789012345\",\"noSignText\":\"\",\"operatorNo\":\"01\",\"playEnglishFlag\":true,\"printTipFlag\":false,\"referenceNo\":\"220801141713\",\"reprintFlag\":false,\"shouldDeclare\":true,\"shouldSign\":true,\"signImage\":\"\",\"terminalNo\":\"12345678\",\"transTypePrint\":\"消费\",\"voucherNo\":\"000106\"}";
//    private static final String JSON_DATA = "{\n" +
//        "\"ACQUIRER\":\"???\",\n" +
//        "\"AMOUNT\":\"??\",\n" +
//        "\"AUTH_NO\":\"???\",\n" +
//        "\"BATCH_NO\":\"???\",\n" +
//        "\"BONUS_POINTS\":\"????\",\n" +
//        "\"CARDHOLDER_SIGN\":\"?????(CARDHOLDER SIGNATURE)\",\n" +
//        "\"CARDHOLDER_SIGN_UPCASE\":\"?????(CARDHOLDER SIGNATURE)\",\n" +
//        "\"CARDHOLDER_TIP\":\"??????\",\n" +
//        "\"CARD_NO\":\"??\",\n" +
//        "\"CASH_SERIAL_NUMBER\":\"?????\",\n" +
//        "\"COMMODITY_CODE\":\"????\",\n" +
//        "\"DATE_TIME\":\"????\",\n" +
//        "\"DESKER\":\"???\",\n" +
//        "\"DOMESTIC_ACCOUNT_IS_BALANCED\":\"??????\",\n" +
//        "\"DOMESTIC_ACCOUNT_IS_UNBALANCED\":\"????????\",\n" +
//        "\"DOMESTIC_CARD\":\"??\",\n" +
//        "\"DOWN_PAYMENT_AMOUNT\":\"??????\",\n" +
//        "\"DUPLICATED\":\"?????/DUPLICATED\",\n" +
//        "\"ECASH_BALANCE\":\"??????\",\n" +
//        "\"EXCHANGE_INTEGRAL\":\"?????\",\n" +
//        "\"EXPIRY_DATE\":\"???\",\n" +
//        "\"FOREIGN_ACCOUNT_IS_BALANCED\":\"??????\",\n" +
//        "\"FOREIGN_ACCOUNT_IS_UNBALANCED\":\"?????\",\n" +
//        "\"FOREIGN_CARD\":\"??\",\n" +
//        "\"FULL_PAYMENT\":\"?????\",\n" +
//        "\"INSTALLMENT\":\"????\",\n" +
//        "\"ISSUER\":\"???\",\n" +
//        "\"I_ACKNOWLEDGE_SATISFACTORY_OF_RELATIVE_GOODS_SERVICE\":\"????????,??????????\",\n" +
//        "\"MERCHANT_NAME\":\"????\",\n" +
//        "\"MERCHANT_NO\":\"????\",\n" +
//        "\"NUMBER\":\"??\",\n" +
//        "\"OLD_AUTH_NO\":\"????\",\n" +
//        "\"OLD_DATE\":\"?????\",\n" +
//        "\"OLD_REF_NO\":\"????\",\n" +
//        "\"OLD_VOUCHER\":\"????\",\n" +
//        "\"OPERATOR_NO\":\"????\",\n" +
//        "\"PAYMENT_AMOUNT\":\"?????\",\n" +
//        "\"PHONE_NUMBER\":\"???\",\n" +
//        "\"REFERENCE\":\"??\",\n" +
//        "\"REFERENCE_NO\":\"???\",\n" +
//        "\"REPAYMENT_CURRENCY\":\"????\",\n" +
//        "\"RE_PRINT_THE_BILL\":\"??????\",\n" +
//        "\"RMB\":\"RMB\",\n" +
//        "\"SERVICE_CHARGE_OF_DOWN_PAYMENT\":\"?????\",\n" +
//        "\"SERVICE_CHARGE_OF_EACH_PERIODS\":\"?????\",\n" +
//        "\"SERVICE_CHARGE_PAYMENT_MODE\":\"???????\",\n" +
//        "\"SERVICE_HOTLINE\":\"??????\",\n" +
//        "\"SETTLEMENT_STATISTIC\":\"????\",\n" +
//        "\"STATISTIC_LIST\":\"???\",\n" +
//        "\"TERMINAL_NO\":\"????\",\n" +
//        "\"THE_BALANCE_OF_INTEGRAL\":\"????\",\n" +
//        "\"THE_NUMBER_OF_PERIODS\":\"????\",\n" +
//        "\"TIP\":\"??\",\n" +
//        "\"TOTAL\":\"??(TOTAL)\",\n" +
//        "\"TOTAL_AMOUNT\":\"????\",\n" +
//        "\"TOTAL_NUM\":\"????\",\n" +
//        "\"TRANSACTION_AMOUNT\":\"?? AMT\",\n" +
//        "\"TRANSACTION_LINES\":\"????\",\n" +
//        "\"TRANSACTION_TYPE\":\"????\",\n" +
//        "\"TRANSFER_CARD_NUMBER\":\"????\",\n" +
//        "\"TYPE\":\"??\",\n" +
//        "\"VOUCHER_NO\":\"???\",\n" +
//        "\"acquirer\":\"????\",\n" +
//        "\"amtTrans\":\"0.12\",\n" +
//        "\"authCode\":\"A54321\",\n" +
//        "\"batchNo\":\"000003\",\n" +
//        "\"cardNo\":\"5187 10** **** 6333 /S\",\n" +
//        "\"copyType\":\"????(MERCHANT COPY)\",\n" +
//        "\"dateTime\":\"2017/08/01 14:17:13\",\n" +
//        "\"hotline\":\"400-200-200\",\n" +
//        "\"icTagFlag\":false,\n" +
//        "\"issuer\":\"????\",\n" +
//        "\"merchantName\":\"ABCDEFGHIJKLMNOPQRSTUV    12345678901234\",\n" +
//        "\"merchantNo\":\"123456789012345\",\n" +
//        "\"noSignText\":\"\",\n" +
//        "\"operatorNo\":\"01\",\n" +
//        "\"playEnglishFlag\":true,\n" +
//        "\"printTipFlag\":false,\n" +
//        "\"referenceNo\":\"220801141713\",\n" +
//        "\"reprintFlag\":false,\n" +
//        "\"shouldDeclare\":true,\n" +
//        "\"shouldSign\":true,\n" +
//        "\"signImage\":\"\",\n" +
//        "\"terminalNo\":\"12345678\",\n" +
//        "\"transTypePrint\":\"??\",\n" +
//        "\"voucherNo\":\"000106\"\n" +
//        "}";
    /**
     * Constructor.
     */
    private PrinterDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get printer demo instance.
     */
    public static PrinterDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new PrinterDemo(context, toast, dialog);
    }

    /**
     * Do printer functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.print))) {
            print();

        } else if (value.equals(getContext().getString(R.string.get_printer_status))) {
            getStatus();

        } else if (value.equals(getContext().getString(R.string.feed_paper))) {
            feedPaper();

        } else if (value.equals(getContext().getString(R.string.print_html5))) {
            printByHtml5();

        } else if (value.equals(getContext().getString(R.string.print_multi_languages))) {
            printMultiLanguages();
        }
    }

    /**
     * Print multi languages.
     */
    private void printMultiLanguages() throws RemoteException {
        // Get statue
        Printer.getInstance().getStatus();

        // Set gray
        Printer.getInstance().setPrnGray(2);

        // Show dialog
        showDialog(R.string.waiting_for_printing, false);

        Printer.getInstance().addHtml("multi-languages-template", "{}")
                .andThen(Printer.getInstance().print())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        hideDialog();
                        showToast(R.string.succeed);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        hideDialog();
                        showToast(e.getMessage());
                    }
                });
    }

    /**
     * Print by HTML5.
     */
    private void printByHtml5() throws RemoteException {
        // Get statue
        Printer.getInstance().getStatus();

        // Set gray
        Printer.getInstance().setPrnGray(2);

        // Show dialog
        showDialog(R.string.waiting_for_printing, false);
//template id: pay-template
        Printer.getInstance().addHtml("content", JSON_DATA)
                .andThen(Printer.getInstance().print())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        hideDialog();
                        showToast(R.string.succeed);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        hideDialog();
                        showToast(e.getMessage());
                    }
                });
    }

    /**
     * Feed paper.
     */
    private void feedPaper() throws RemoteException {
        // Get statue
        Printer.getInstance().getStatus();

        // Show dialog
        showDialog(R.string.waiting_for_printing, false);

        // Feed one line
        Printer.getInstance().feedLine(1);

        // Start printing
        Printer.getInstance().start(new OnPrintListener.Stub() {

            @Override
            public void onFinish() throws RemoteException {
                Log.d(TAG, "----- onFinish -----");

                hideDialog();
                showToast(R.string.succeed);
            }

            @Override
            public void onError(int error) throws RemoteException {
                Log.d(TAG, "----- onError ----");

                hideDialog();
                showToast(Printer.getErrorId(error));
            }
        });
    }

    /**
     * Get status.
     */
    private void getStatus() throws RemoteException {
        // Get status
        Printer.getInstance().getStatus();

        // Show message
        showToast(R.string.succeed);
    }

    /*
        Print web view
     */
    private void printWebview() throws RemoteException {
        // Get statue
        Printer.getInstance().getStatus();

        // Set gray
        Printer.getInstance().setPrnGray(2);

        // Show dialog
        showDialog(R.string.waiting_for_printing, false);

        Printer.getInstance().initWebView(context.getApplicationContext());
        Printer.getInstance().print()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        hideDialog();
                        showToast(R.string.succeed);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        hideDialog();
                        showToast(e.getMessage());
                    }
                });
    }

    /**
     * Print screen shot.
     */
    public void printScreenshot(String filename) throws RemoteException {
        String dir = "/storage/emulated/0/RukkabetAgent/assets/screenshots/";

        showToast("Fetching content");

        // Get statue
        Printer.getInstance().getStatus();

        // Show dialog
        showDialog(R.string.waiting_for_printing, false);

        // Set gray
        Printer.getInstance().setPrnGray(5);

        // Print text with large font size
        setFontSpec(FONT_SIZE_LARGE);
        Printer.getInstance().addText(AlignMode.CENTER, "RUKKA BET SLIP");

        // Print image
//        if (readAssetsFileStorage(dir+"ic_header_logo.bmp") != null) {
            Printer.getInstance().addImage(AlignMode.CENTER, readAssetsFileStorage(dir+filename));
//        }else{
//            Toast.makeText(this.context.getApplicationContext(), "Failed to fetch "+dir+filename, Toast.LENGTH_SHORT).show();
//        }
        setFontSpec(FONT_SIZE_SMALL);
        Printer.getInstance().addText(AlignMode.CENTER, filename);
        // Print QR code
        Printer.getInstance().addQrCode(AlignMode.CENTER, 200, ECLevel.ECLEVEL_Q, ""+filename);

        // Feed lines
        Printer.getInstance().feedLine(6);

        // Start printing
        Printer.getInstance().start(new OnPrintListener.Stub() {

            @Override
            public void onFinish() throws RemoteException {
                Log.d(TAG, "----- onFinish -----");

                hideDialog();
                showToast(R.string.succeed);
            }

            @Override
            public void onError(int error) throws RemoteException {
                Log.d(TAG, "----- onError ----");

                hideDialog();
                showToast(Printer.getErrorId(error));
            }
        });
    }

    /**
     * Print.
     */
    private void print() throws RemoteException {
        // Get statue
        Printer.getInstance().getStatus();

        // Show dialog
        showDialog(R.string.waiting_for_printing, false);

        // Set gray
        Printer.getInstance().setPrnGray(5);

        // Print image
        Printer.getInstance().addImage(AlignMode.LEFT, readAssetsFile("ic_bank_logo.bmp"));

        // Print text with normal font size
        setFontSpec(FONT_SIZE_NORMAL);
        Printer.getInstance().addText(AlignMode.LEFT, "CARD HOLDER COPY");
        Printer.getInstance().addText(AlignMode.LEFT, "POS SALES SLIP");
        Printer.getInstance().addText(AlignMode.LEFT, "MERCHANT NAME");
        Printer.getInstance().addText(AlignMode.LEFT, "  " + TerminalInfo.merchantName);

        // Print text with small font size
        setFontSpec(FONT_SIZE_SMALL);
        Printer.getInstance().addText(AlignMode.LEFT, "MERCHANT NO:");
        Printer.getInstance().addText(AlignMode.LEFT, "  " + TerminalInfo.merchantNo);
        Printer.getInstance().addText(AlignMode.LEFT, "TERMINAL NO:");
        Printer.getInstance().addText(AlignMode.LEFT, "  " + TerminalInfo.terminalNo);
        Printer.getInstance().addText(AlignMode.LEFT, "OPERATOR NO:" + TerminalInfo.operatorNo);
        Printer.getInstance().addText(AlignMode.LEFT, "ISSUER:" + TerminalInfo.issuer);
        Printer.getInstance().addText(AlignMode.LEFT, "ACQUIRER:" + TerminalInfo.acquirer);
        Printer.getInstance().addText(AlignMode.LEFT, "CARD NO:");

        // Print text with normal font size
        setFontSpec(FONT_SIZE_NORMAL);
        Printer.getInstance().addText(AlignMode.CENTER, "6230 58** **** ***5 815 (C)");

        // Print text with small font size
        setFontSpec(FONT_SIZE_SMALL);
        Printer.getInstance().addText(AlignMode.LEFT, "TRANS TYPE:");

        // Print text with normal font size
        setFontSpec(FONT_SIZE_NORMAL);
        Printer.getInstance().addText(AlignMode.LEFT, "  FAST PAYMENT/CLSS SALE");

        // Print text with small font size
        setFontSpec(FONT_SIZE_SMALL);
        Printer.getInstance().addText(AlignMode.LEFT, "BATCH NO:0000001");

        // Print text with normal font size
        setFontSpec(FONT_SIZE_NORMAL);
        Printer.getInstance().addText(AlignMode.LEFT, "VOUCHER NO:001954");
        Printer.getInstance().addText(AlignMode.LEFT, "REF NO:160918094357");
        Printer.getInstance().addText(AlignMode.LEFT, "AUTH NO:ECC001");

        // Print text with small font size
        setFontSpec(FONT_SIZE_SMALL);
        Printer.getInstance().addText(AlignMode.LEFT, "DATE:2016/09/18");
        Printer.getInstance().addText(AlignMode.LEFT, "TIME:09:43:00");
        Printer.getInstance().addText(AlignMode.LEFT, "EXP DATE:2023/12");

        // Print text with normal font size
        setFontSpec(FONT_SIZE_NORMAL);
        Printer.getInstance().addText(AlignMode.LEFT, "AMOUNT:RMB 0.01 ");

        // Print text with small font size
        setFontSpec(FONT_SIZE_SMALL);
        Printer.getInstance().addText(AlignMode.LEFT, "REFERENCE:");

        // Print text with normal font size
        setFontSpec(FONT_SIZE_NORMAL);
        Printer.getInstance().addText(AlignMode.LEFT, "ELECTRONIC CASH BALANCE:RMB 19.89 ");

        // Print text with small font size
        setFontSpec(FONT_SIZE_SMALL);
        Printer.getInstance().addText(AlignMode.LEFT, "TC:4AA3C1579C7FD6BC  ATC:0047");
        Printer.getInstance().addText(AlignMode.LEFT, "TUR:0000000000 SN:000 TSI:0000");
        Printer.getInstance().addText(AlignMode.LEFT, "AID:A000000333010101");
        Printer.getInstance().addText(AlignMode.LEFT, "APP LABEL:EMV DEBIT");
        Printer.getInstance().addText(AlignMode.LEFT, "UNPR NUM:283CBA05");
        Printer.getInstance().addText(AlignMode.LEFT, "AIP:7C00  Term Capa:000000");
        Printer.getInstance().addText(AlignMode.LEFT, "9F10:07020103900000010A01000000198933660FAD");
        Printer.getInstance().addText(AlignMode.LEFT, "Ver:00000606160101");
        Printer.getInstance().addText(AlignMode.LEFT, "S/N:50629416");

        // Print barcode
        Printer.getInstance().addBarCode(AlignMode.CENTER, 2, -1, "12345678901234567890");

        // Print QR code
        Printer.getInstance().addQrCode(AlignMode.LEFT, 240, ECLevel.ECLEVEL_Q, "www.landicorp.com");

        // Feed lines
        Printer.getInstance().feedLine(6);

        // Start printing
        Printer.getInstance().start(new OnPrintListener.Stub() {

            @Override
            public void onFinish() throws RemoteException {
                Log.d(TAG, "----- onFinish -----");

                hideDialog();
                showToast(R.string.succeed);
            }

            @Override
            public void onError(int error) throws RemoteException {
                Log.d(TAG, "----- onError ----");

                hideDialog();
                showToast(Printer.getErrorId(error));
            }
        });
    }

    /**
     * Set font spec.
     */
    private void setFontSpec(int fontSpec) throws RemoteException {
        switch (fontSpec) {
            case FONT_SIZE_SMALL:
                Printer.getInstance().setHzSize(HZSize.DOT16x16);
                Printer.getInstance().setHzScale(HZScale.SC1x1);
                Printer.getInstance().setAscSize(ASCSize.DOT16x8);
                Printer.getInstance().setAscScale(ASCScale.SC1x1);
                break;

            case FONT_SIZE_NORMAL:
                Printer.getInstance().setHzSize(HZSize.DOT24x24);
                Printer.getInstance().setHzScale(HZScale.SC1x1);
                Printer.getInstance().setAscSize(ASCSize.DOT24x12);
                Printer.getInstance().setAscScale(ASCScale.SC1x1);
                break;

            case FONT_SIZE_LARGE:
                Printer.getInstance().setHzSize(HZSize.DOT24x24);
                Printer.getInstance().setHzScale(HZScale.SC1x2);
                Printer.getInstance().setAscSize(ASCSize.DOT24x12);
                Printer.getInstance().setAscScale(ASCScale.SC1x2);
                break;
        }
    }

    /**
     * Read the file in the assets directory.
     */
    private byte[] readAssetsFile(String fileName) throws RemoteException {

        InputStream input = null;
        try {
            input = getContext().getAssets().open(fileName);
            byte[] buffer = new byte[input.available()];
            int size = input.read(buffer);
            if (size == -1) {
                throw new RemoteException(getContext().getString(R.string.read_fail));
            }
            return buffer;
        } catch (IOException e) {
            throw new RemoteException(e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    showToast(e.getLocalizedMessage());
                }
            }
        }
    }

    private static byte[] readAssetsFileStorage(String fileName) throws RemoteException {
        FileInputStream input = null;
        Object var5;
        try {
            File f = new File(fileName);
            if (f == null || !f.exists()) {
                Object var18 = null;
                return (byte[])var18;
            }

            input = new FileInputStream(f);
            byte[] buffer = new byte[input.available()];
            int size = input.read(buffer);
            if (size != -1) {
                byte[] var19 = buffer;
                return var19;
            }

            var5 = null;
        } catch (IOException var16) {
            throw new RemoteException(var16.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException var15) {

                }
            }

        }

        return (byte[])var5;
    }
}
