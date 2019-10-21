package com.arke.sdk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

//import com.arke.sdk.demo.R;

import com.arke.sdk.demo.PrinterDemo;
import com.arke.sdk.util.printer.Printer;
import com.arke.sdk.util.printer.PrintingWebView;
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
import java.util.Objects;

import io.reactivex.Single;

public class PrintPreviewActivity extends AppCompatActivity {

    private static final int FONT_SIZE_SMALL = 0;
    private static final int FONT_SIZE_NORMAL = 1;
    private static final int FONT_SIZE_LARGE = 2;
    private static final String DIR = "/storage/emulated/0/RukkabetAgent/assets/screenshots/";
    private String innerHTML;

    /**
     * Alert dialog.
     */
    private AlertDialog dialog;

    /**
     * Toast.
     */
    private Toast toast;
    private String divider = "===========================================";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_preview);

        // Init toast
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        // Init alert dialog
        dialog = new AlertDialog.Builder(this)
                .setNegativeButton(getString(R.string.cancel), null)
                .setCancelable(false)
                .create();
        
//        get extras from intent
        this.innerHTML = Objects.requireNonNull(getIntent().getExtras()).getString("innerHTML");

        WebView webview = (WebView) findViewById(R.id.prevWebView);
        webview.setWebViewClient(new WebViewClient());
        webview.setInitialScale(100);
        webview.clearCache(true);
        webview.clearHistory();
        webview.setVerticalScrollBarEnabled(false);
        webview.setHorizontalScrollBarEnabled(false);
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webview.getSettings().setDefaultTextEncodingName("utf-8");
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.loadUrl(this.innerHTML);
//        webview.loadData(this.innerHTML, "text/html", "UTF-8");



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PrintPreviewActivity.this, "Printing", Toast.LENGTH_SHORT).show();
//                 save betting slip details
                BettingStake slipInfo = new BettingStake();
                slipInfo.stakeDetails("RUKBET-773883-38849", "9987690", "23/06/2019 15:08", "2,000.00", "5.00", "0.00", "10,000.00");
//                create array list to store betting stakes
                ArrayList<BettingStake> stakes = new ArrayList<>();
//                static stakes for testing
                BettingStake stake1 = new BettingStake();
                stake1.NewStake(1, "ENGLISH PREMIERSHIP", "MAN. UTD", "REAL MAR", "Over 1.2");

                BettingStake stake2 = new BettingStake();
                stake2.NewStake(1, "LA LIGA", "BARCA. F.C", "JUVENTUS", "Under 2.5");

                // add stakes to the array list
                stakes.add(stake1);
                stakes.add(stake2);
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
                    for (BettingStake stake: stakes) {

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

//                            hideDialog();
                            Toast.makeText(PrintPreviewActivity.this, R.string.succeed, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(int error) throws RemoteException {
                            Log.d("Print", "----- onError ----");

//                            hideDialog();
                            Toast.makeText(PrintPreviewActivity.this, Printer.getErrorId(error), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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
