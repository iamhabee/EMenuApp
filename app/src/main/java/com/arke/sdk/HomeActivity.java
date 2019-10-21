package com.arke.sdk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arke.sdk.demo.PrinterDemo;
import com.arke.sdk.util.printer.PrintingWebView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import io.reactivex.Single;
import timber.log.Timber;


public class HomeActivity extends AppCompatActivity {


    private static String innerHTML;
    private static AlertDialog dialog;
    private WebView webview;
    private boolean loadingFinished = true;
    private CountDownLatch loadingFinishedLatch = new CountDownLatch(1);
    private String targetUrl = "";
    private ProgressBar progressBar;
    private Button refreshBtn;



    /**
     * Toast.
     */
    private Toast toast;
    private String screenshotDir;
    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Init toast
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        // Init alert dialog
        dialog = new AlertDialog.Builder(this)
                .setNegativeButton(getString(R.string.cancel), null)
                .setCancelable(false)
                .create();
        // create a folder to store logo
        createFolder("/RukkabetAgent/assets/screenshots");
//        initialize web view
        webview = (WebView) findViewById(R.id.homeWebView);
//        initialize progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        refreshBtn = (Button) findViewById(R.id.refreshWebNavBtn);
//        set target url
//        https://mobile.rukkabet.com/mobile/index.aspx
        setTargetUrl("http://rukkabet.com");
//        load url
        loadUrlToWebview(this.targetUrl);
    }

    private void createFolder(String fname) {
        String myfolder = Environment.getExternalStorageDirectory() + "/" + fname;
        File f = new File(myfolder);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                Log.d("Storage path created", myfolder);
            }
        }
    }

    private void loadUrlToWebview(String url) {
        setLoadingFinished(false);

        webview.setWebViewClient(new HomeActivity.MyWebViewClient());
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
        webview.postDelayed(new Runnable() {

            @Override
            public void run() {
                webview.loadUrl(url);
            }
        }, 500);
//        webview.loadUrl(url);

        // TODO process finish
        webview.setWebViewClient(new HomeActivity.MyWebViewClient());

        webview.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Timber.d(cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });
    }

    //    set url
    private void setTargetUrl(String url){
        this.targetUrl = url;
    }


    //    custom WebViewClient class
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String newUrl = request.getUrl().toString();
            Log.d("WebView", "Override Url Loading "+newUrl);
            setLoadingFinished(false);
            view.postDelayed(new Runnable() {

                @Override
                public void run() {
                    view.loadUrl(newUrl);
                }
            }, 500);
//            view.loadUrl(newUrl);
//            Toast.makeText(HomeActivity.this, "Override Url Loading", Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public void onPageStarted(
                WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
//            Toast.makeText(HomeActivity.this, "On Page Started", Toast.LENGTH_SHORT).show();
            Log.d("WebView", "On Page Started- "+url);
            setLoadingFinished(false);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            setLoadingFinished(true);
            setTargetUrl(url);
//            capture screen shot
//            captureScreenshot(view);
//            grab html code from webview
            view.evaluateJavascript(
                    "(function() { return ('<html>'+document.getElementById('content').outerHTML+'</html>'); })();",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String html) {
                            Log.d("HTML", html);
                            HomeActivity.innerHTML = html;
                        }
                    });
//            Toast.makeText(HomeActivity.this, "On Page Finished", Toast.LENGTH_SHORT).show();
            Log.d("WebView", "On Page Finished- "+url);
        }
    }

    private void captureScreenshot(WebView view) {

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        this.screenshotDir = "/storage/emulated/0/RukkabetAgent/assets/screenshots/";
        this.filename = "screenshot_"+ Calendar.getInstance().getTimeInMillis()+".bmp";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream( this.screenshotDir + this.filename );
            if ( fos != null )
            {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 1, fos);

                fos.close();
            }
        }
        catch( Exception e )
        {

        }
    }


    private void setLoadingFinished(boolean finished) {
        this.loadingFinished = finished;
        if (this.loadingFinished) {
            loadingFinishedLatch.countDown();
//            hide progress bar show refresh btn
            progressBar.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
        }else{
//            show progress bar hide refresh btn
            progressBar.setVisibility(View.VISIBLE);
            refreshBtn.setVisibility(View.GONE);
        }
    }



    //    refresh page
    public void refreshPage(View view){
//        load url
        loadUrlToWebview(this.targetUrl);
    }



    //    nav back page
    public void webviewGoBack(View view){
        if(webview.canGoBack()){
            webview.goBack();
        }
    }

    //    nav back page
    public void webviewGoForward(View view){
        if(webview.canGoForward()){
            webview.goForward();
        }
    }

//    print page
    public void webviewPrint(View view){
//        captureScreenshot(webview);
        // init printing web view
//        this.setTargetUrl("https://en.wikipedia.org/wiki/%22Hello%2C_World!%22_program");
//        PrintingWebView.getInstance().setIndexUrl(this.targetUrl);
//        PrintingWebView.getInstance().create(HomeActivity.this);
//        PrintingWebView.getInstance().loadPrintingContent("toc", "{}");
//        try {
//            PrintingWebView.getInstance().awaitPageLoaded();
//            Single<Bitmap> screenshot = PrintingWebView.getInstance().captureWebView();
//            Log.d("screenshot", screenshot.toString());
//            PrinterDemo.getInstance(getApplicationContext(), toast, dialog).printScreenshot(this.filename);
//        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        open dialog and request for slip code
        LayoutInflater layoutInflater = LayoutInflater.from(HomeActivity.this);
        View promptView = layoutInflater.inflate(R.layout.request_slip_code_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText slipCode = (EditText) promptView.findViewById(R.id.editTextTemp);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Validate", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String code = slipCode.getText().toString();
                        SlipValidator slipValidator = new SlipValidator(HomeActivity.this, HomeActivity.dialog);
                        slipValidator.validateSlipThenPrint(code);

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

//        navigate to print preview
//        Intent preview = new Intent(HomeActivity.this, PrintPreviewActivity.class);
//        preview.putExtra("innerHTML", this.targetUrl);
//        startActivity(preview);
    }

    @Override
    public void onBackPressed(){
        if(webview.canGoBack()){
            webview.goBack();
        }else{
            super.onBackPressed();
        }
    }
}
