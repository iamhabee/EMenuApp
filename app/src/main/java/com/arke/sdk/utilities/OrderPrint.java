package com.arke.sdk.utilities;

import java.io.IOException;

        import android.app.AlertDialog;
        import android.content.Context;
        import android.os.RemoteException;
        import android.util.Log;
        import android.view.View;
        import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.util.printer.Printer;
        import com.usdk.apiservice.aidl.printer.ASCScale;
        import com.usdk.apiservice.aidl.printer.ASCSize;
        import com.usdk.apiservice.aidl.printer.AlignMode;
        import com.usdk.apiservice.aidl.printer.HZScale;
        import com.usdk.apiservice.aidl.printer.HZSize;
        import com.usdk.apiservice.aidl.printer.OnPrintListener;

        import java.io.File;
        import java.io.FileInputStream;
        import java.io.IOException;
        import java.util.ArrayList;

public class OrderPrint {

    private Context context;

    private static final int FONT_SIZE_SMALL = 0;
    private static final int FONT_SIZE_NORMAL = 1;
    private static final int FONT_SIZE_LARGE = 2;
    private static final String DIR = "/storage/emulated/0/RukkabetAgent/assets/screenshots/";
    private static final String divider = "===========================================";

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
    }


    public void validateSlipThenPrint(String slipCode){
//        Contact server to get all bets placed


        //  save betting slip details
        CustomerOrder slipInfo = new CustomerOrder();
        slipInfo.stakeDetails("RUKBET-773883-38849", "9987690", "23/06/2019 15:08", "2,000.00", "5.00", "0.00", "10,000.00");

        //      create array list to store betting stakes
        ArrayList<CustomerOrder> stakes = new ArrayList<>();

        //      static stakes for testing
        CustomerOrder stake1 = new CustomerOrder();
        stake1.NewStake(1, "ENGLISH PREMIERSHIP", "MAN. UTD", "REAL MAR", "Over 1.2");

        CustomerOrder stake2 = new CustomerOrder();
        stake2.NewStake(1, "LA LIGA", "BARCA. F.C", "JUVENTUS", "Under 2.5");

        // add stakes to the array list
        stakes.add(stake1);
        stakes.add(stake2);

        printSlip(slipInfo, stakes);
    }

    private void printSlip(CustomerOrder slipInfo, ArrayList<CustomerOrder> stakes) {

        // Show dialog
        showDialog(R.string.waiting_for_printing, false);
        try {
            // Get status
            Printer.getInstance().getStatus();

            // Set gray
            Printer.getInstance().setPrnGray(5);

            // Add logo
            Printer.getInstance().addImage(AlignMode.CENTER, readAssetsFileStorage(DIR+"rukka_bet_logo.png"));
//                    stake details
            Printer.getInstance().addText(0, formatAlignedJustified("SLIP ID", slipInfo.slipId));
            Printer.getInstance().addText(0, formatAlignedJustified("USER ID", slipInfo.userId));
            Printer.getInstance().addText(0, formatAlignedJustified("DATE", slipInfo.dateTime));

            // Loop through stakes array list and add items
            for (CustomerOrder stake: stakes) {

                setFontSpec(FONT_SIZE_NORMAL);
                Printer.getInstance().addText(AlignMode.CENTER, divider);
                setFontSpec(FONT_SIZE_LARGE);
                Printer.getInstance().addText(AlignMode.LEFT, stake.sportCategory);

                setFontSpec(FONT_SIZE_NORMAL);
                Printer.getInstance().addText(AlignMode.LEFT, stake.team1+" VS "+ stake.team2);
                Printer.getInstance().addText(AlignMode.LEFT, stake.stake);
            }
            // Add stake summary
            setFontSpec(FONT_SIZE_NORMAL);
            Printer.getInstance().addText(AlignMode.CENTER, divider);
            Printer.getInstance().addText(AlignMode.CENTER, "SUMMARY");
            Printer.getInstance().addText(AlignMode.CENTER, divider);
            Printer.getInstance().addText(0, formatAlignedJustified("TOTAL STAKE", "NGN"+slipInfo.totalStake));
            Printer.getInstance().addText(0, formatAlignedJustified("TOTAL ODDS", slipInfo.totalOdds));
            Printer.getInstance().addText(0, formatAlignedJustified("BONUS", "NGN"+slipInfo.bonus));
            Printer.getInstance().addText(AlignMode.CENTER, "POTENTIAL WINNINGS");
            setFontSpec(FONT_SIZE_LARGE);
            Printer.getInstance().addText(AlignMode.CENTER, "NGN"+slipInfo.potentialWinnings);

            // Add QR Code
            Printer.getInstance().addQrCode(AlignMode.CENTER, 200, 1, slipInfo.slipId);
            // Add company details
            setFontSpec(FONT_SIZE_NORMAL);
            Printer.getInstance().addText(AlignMode.CENTER, "RUKKA BET");
            Printer.getInstance().addText(AlignMode.CENTER, "Follow us on \nFacebook, Google+, and Twitter");
            Printer.getInstance().addText(AlignMode.CENTER, "Contact us on \n08162576039 - 08154013511");
            Printer.getInstance().addText(AlignMode.CENTER, "support@rukkabet.com");
            Printer.getInstance().addText(AlignMode.CENTER, "www.rukkabet.com");

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
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
