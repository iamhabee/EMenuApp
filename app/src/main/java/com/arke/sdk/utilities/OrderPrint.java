package com.arke.sdk.utilities;

import java.io.IOException;

        import android.app.AlertDialog;
        import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

        import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.util.printer.Printer;
import com.parse.ParseUser;
import com.usdk.apiservice.aidl.printer.ASCScale;
        import com.usdk.apiservice.aidl.printer.ASCSize;
        import com.usdk.apiservice.aidl.printer.AlignMode;
        import com.usdk.apiservice.aidl.printer.HZScale;
        import com.usdk.apiservice.aidl.printer.HZSize;
        import com.usdk.apiservice.aidl.printer.OnPrintListener;

        import java.io.File;
        import java.io.FileInputStream;
import java.text.NumberFormat;
import java.util.List;


public class OrderPrint {

    private Context context;

    private static final int FONT_SIZE_SMALL = 0;
    private static final int FONT_SIZE_NORMAL = 1;
    private static final int FONT_SIZE_LARGE = 2;
    private static final String DIR = "/storage/emulated/0/RukkabetAgent/assets/screenshots/";
    private static final String divider = "===========================================";
    private static final String single_divider = "***********************************";

    SharedPreferences preferences;
    /**
     * Alert dialog.
     */
    private AlertDialog dialog;

    /**
     * Constructor.
     */
    public OrderPrint(Context context, AlertDialog dialog) {
        this.context = context;
        this.dialog = dialog;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void validateSlipThenPrint(List<EMenuItem> orders, boolean hasPaid) {
        // generate some other information
        printSlip(orders, hasPaid);
    }

    public void printQRCode(String generateRandString) {
        // Show dialog
        showDialog(R.string.waiting_for_printing, false);
        try {
            // Get status
            Printer.getInstance().getStatus();

            // Set gray
            Printer.getInstance().setPrnGray(5);
//            setFontSpec(FONT_SIZE_NORMAL);
//            Printer.getInstance().addText(AlignMode.CENTER, divider);
//            Printer.getInstance().addText(AlignMode.CENTER, AppPrefs.getRestaurantOrBarName());
//            Printer.getInstance().addText(AlignMode.CENTER, "TAG");
//            Printer.getInstance().addText(AlignMode.CENTER, divider);


            Printer.getInstance().addQrCode(AlignMode.CENTER, 300, 1,generateRandString );
            setFontSpec(FONT_SIZE_LARGE);
            Printer.getInstance().addText(AlignMode.CENTER, generateRandString);
            setFontSpec(FONT_SIZE_NORMAL);
            // Add whitespace
            Printer.getInstance().feedLine(5);
            // Start printing
            Printer.getInstance().start(new OnPrintListener.Stub() {

                @Override
                public void onFinish() throws RemoteException {
                    Log.d("Print", "----- onFinish -----");

                    hideDialog();
                    Toast.makeText(context.getApplicationContext(), generateRandString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int error) throws RemoteException {
                    Log.d("Print", "----- onError ----");

                    hideDialog();
                    Toast.makeText(context.getApplicationContext(), Printer.getErrorId(error), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void printSlip(List<EMenuItem> orders, boolean hasPaid) {
        String customerTag = null;
        String tableTag = null;
        int result = 0;

        // Show dialog
        showDialog(R.string.waiting_for_printing, false);
        try {
            // Get status
            Printer.getInstance().getStatus();

            // Set gray
            Printer.getInstance().setPrnGray(5);

            // Loop through stakes array list and add items

            if (orders != null && !orders.isEmpty()) {
                int count = 1, itemQty = 0;
                double total = 0, unitPrice = 0, sumTotal = 0;

                setFontSpec(FONT_SIZE_NORMAL);
                Printer.getInstance().addText(AlignMode.CENTER, AppPrefs.getRestaurantOrBarName());
                Printer.getInstance().addText(AlignMode.CENTER, AppPrefs.getRestaurantOrBarEmailAddress());
                Printer.getInstance().addText(AlignMode.CENTER, single_divider);
                setFontSpec(FONT_SIZE_LARGE);
                Printer.getInstance().addText(AlignMode.CENTER, "ORDER ITEMS");
                setFontSpec(FONT_SIZE_NORMAL);
                Printer.getInstance().addText(AlignMode.CENTER, single_divider);


                for (EMenuItem eMenuItem : orders) {
                    unitPrice = Double.parseDouble(eMenuItem.getMenuItemPrice());
                    itemQty = eMenuItem.getOrderedQuantity();
                    total = unitPrice * itemQty;
                    sumTotal = sumTotal + total;

                    if (count == 1) {
                        customerTag = eMenuItem.getCustomerTag();
                        tableTag = eMenuItem.getTableTag();
                        setFontSpec(FONT_SIZE_NORMAL);
                        Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("CUSTOMER TAG:", "" + eMenuItem.getCustomerTag()));
                        setFontSpec(FONT_SIZE_NORMAL);
                        Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("TABLE TAG:", "" + eMenuItem.getTableTag()));
                        setFontSpec(FONT_SIZE_NORMAL);
                        Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("WAITER TAG:", "" + ParseUser.getCurrentUser().getUsername()));

                        setFontSpec(FONT_SIZE_NORMAL);
                        Printer.getInstance().addText(AlignMode.CENTER, divider);
                        setFontSpec(FONT_SIZE_LARGE);
                        Printer.getInstance().addText(AlignMode.CENTER, hasPaid?("PAID"):("NOT PAID"));

                    }

                    if (eMenuItem.getOrderedQuantity() > 0){
                        setFontSpec(FONT_SIZE_NORMAL);
                        Printer.getInstance().addText(AlignMode.CENTER, divider);
                        Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("ITEM:", "" + eMenuItem.getMenuItemName()));
                        Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("QUANTITY:", "" + eMenuItem.getOrderedQuantity()));
                        Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("UNIT PRICE:", formatAmount(unitPrice, true)));
                        Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("TOTAL:", formatAmount(total, true)));
                        count = count + 1;
                    }


                }

                Printer.getInstance().addText(AlignMode.CENTER, single_divider);
                setFontSpec(FONT_SIZE_NORMAL);
                Printer.getInstance().addText(AlignMode.CENTER, "SUMMARY");
                setFontSpec(FONT_SIZE_LARGE);
                Printer.getInstance().addText(AlignMode.CENTER, formatAmount(sumTotal, true));
                setFontSpec(FONT_SIZE_NORMAL);

                Printer.getInstance().addText(AlignMode.CENTER, single_divider);
                Printer.getInstance().addQrCode(AlignMode.CENTER, 200, 0, customerTag);

                // Add company details
                setFontSpec(FONT_SIZE_NORMAL);
                Printer.getInstance().addText(AlignMode.CENTER, "E-MENU");
                Printer.getInstance().addText(AlignMode.CENTER, "VERSION 1.0");
                Printer.getInstance().addText(AlignMode.CENTER, "Powered By:");
                Printer.getInstance().addText(AlignMode.CENTER, "Efull Technologies Nigeria");



                // Add whitespace
                Printer.getInstance().feedLine(6);
                // Start printing
                Printer.getInstance().start(new OnPrintListener.Stub() {

                    @Override
                    public void onFinish() throws RemoteException {
                        Log.d("Print", "----- onFinish -----");

                        hideDialog();
                        Toast.makeText(context.getApplicationContext(), R.string.succeed, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int error) throws RemoteException {
                        Log.d("Print", "----- onError ----");

                        hideDialog();
                        Toast.makeText(context.getApplicationContext(), Printer.getErrorId(error), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                // notify user abt empty order
            }
        } catch (RemoteException e) {
            e.printStackTrace();

            hideDialog();

            // show alert for error while trying to print
            showDialogError(e.getMessage(), true);
        }
    }



    private String formatAmount(double totalAuthAmount, boolean curSym) {
        String Currency = "NGN";
        String Separator = ",";
        Boolean Spacing = false;
        Boolean Delimiter = false;
        Boolean Decimals = true;
        String currencyFormat = "";
        if (Spacing) {
            if (Delimiter) {
                currencyFormat = ". ";
            } else {
                currencyFormat = " ";
            }
        } else if (Delimiter) {
            currencyFormat = ".";
        } else {
            currencyFormat = "";
        }
        if(curSym){
            currencyFormat = Currency+currencyFormat;
        }
        String tformatted = NumberFormat.getCurrencyInstance().format(totalAuthAmount / 1.0D).replace(NumberFormat.getCurrencyInstance().getCurrency().getSymbol(), currencyFormat);
        return tformatted;
    }

    /**
     * Hide dialog.
     */
    private void hideDialog() {
        dialog.cancel();
    }

    private void showDialog(int resId, boolean cancelable) {
        dialog.setMessage(context.getString(resId));
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(cancelable);
        if (dialog.getWindow() != null) {
            // // TODO: 2017/9/8 屏蔽的Home的方法在 4.0 以后已经不再支持，在 C10  Android 7.0 上会报错。
            /** 为了更好地实现应用屏蔽HOME键的功能而又不引起异常，现已在P990及W280PV2的版本上新增了一个接口，
             该接口可以让应用的window屏蔽HOME键及APP_SWITCH键（就是调出近期应用的键），调用方法如下：

             @Override
             public void onAttachedToWindow() {
             super.onAttachedToWindow();
             Window win = getWindow();
             try {
             Class<?> cls = win.getClass();
             final Class<?>[] PARAM_TYPES = new Class[] {int.class};
             Method method = cls.getMethod("addCustomFlags", PARAM_TYPES);
             method.setAccessible(true);
             method.invoke(win, new Object[] {0x00000001});
             } catch (Exception e) {
             // handle the error here.
             }
             }

             该方法在2015.03.17号的烧片版本才有效。以后的应用屏蔽HOME键，请尽量使用此方法，不要再使用TYPE_KEYGUARD_DIALOG方式。
             **/
//            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        }
    }

    private void showDialogError(String res, boolean cancelable) {
        dialog.setMessage(res);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(cancelable);
        if (dialog.getWindow() != null) {

        }
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


    private static String formatAlignedJustified(String left, String right) {
        if (left != null && right != null) {
            int leftlen = left.length();
            int rightlen = right.length();
            int space = 32 - (leftlen + rightlen);
            String sp = "";

            for(int i = 0; i < space; ++i) {
                sp = sp + " ";
            }

            return left + sp + right;
        } else {
            return "";
        }
    }

}
