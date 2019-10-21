package com.arke.sdk;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.arke.sdk.demo.PrinterDemo;
import com.arke.sdk.util.printer.PrintingWebView;

import java.util.concurrent.CountDownLatch;

import io.reactivex.Single;
import timber.log.Timber;

//import com.arke.sdk.demo.R;

public class WebviewActivity extends AppCompatActivity {

    /**
     * Alert dialog.
     */
    private AlertDialog dialog;

    /**
     * Toast.
     */
    private Toast toast;


    private PrintingWebView printingWebView;

    private WebView webview;

    private boolean loadingFinished = true;

    private CountDownLatch loadingFinishedLatch = new CountDownLatch(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        load web view
//        Toast.makeText(WebviewActivity.this, "loading web page", Toast.LENGTH_LONG).show();
        webview = (WebView) findViewById(R.id.appWebView);
        webview.setWebViewClient(new MyWebViewClient());
//        WebSettings webSettings = webview.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webview.loadUrl("https://google.com");

        // init printing web view
        PrintingWebView.getInstance().setIndexUrl("https://en.wikipedia.org/wiki/%22Hello%2C_World!%22_program");
        PrintingWebView.getInstance().create(WebviewActivity.this);
        PrintingWebView.getInstance().loadPrintingContent("mw-content-text", "{}");
        try {
            PrintingWebView.getInstance().awaitPageLoaded();
            Single<Bitmap> screenshot = PrintingWebView.getInstance().captureWebView();
            Log.d("screenshot", screenshot.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // Init toast
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        // Init alert dialog
        dialog = new AlertDialog.Builder(this)
                .setNegativeButton(getString(R.string.cancel), null)
                .setCancelable(false)
                .create();
//
//
        Button fab = (Button) findViewById(R.id.loadWebBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(WebviewActivity.this, "Fetching Web Page...", Toast.LENGTH_SHORT).show();
                Log.d("WebView", "Initializing");
                webview.setInitialScale(100);

                webview.setVerticalScrollBarEnabled(false);
                webview.setHorizontalScrollBarEnabled(false);
                webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                webview.getSettings().setDefaultTextEncodingName("utf-8");
                webview.getSettings().setJavaScriptEnabled(true);
                webview.getSettings().setUseWideViewPort(true);
                webview.getSettings().setLoadWithOverviewMode(true);
                webview.loadUrl("http://cardsxchange.net");

                // TODO process finish
                webview.setWebViewClient(new MyWebViewClient());

                webview.setWebChromeClient(new WebChromeClient() {
                    public boolean onConsoleMessage(ConsoleMessage cm) {
                        Timber.d(cm.message() + " -- From line "
                                + cm.lineNumber() + " of "
                                + cm.sourceId());
                        return true;
                    }
                });
            }
        });
    }

    private void setLoadingFinished(boolean finished) {
        this.loadingFinished = finished;
        if (this.loadingFinished) {
            loadingFinishedLatch.countDown();
        }
    }

//    custom WebViewClient class
    private class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d("WebView", "Override Url Loading");
            setLoadingFinished(false);
            view.loadUrl(request.getUrl().toString());
            Toast.makeText(WebviewActivity.this, "Override Url Loading", Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public void onPageStarted(
                WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Toast.makeText(WebviewActivity.this, "On Page Started", Toast.LENGTH_SHORT).show();
            Log.d("WebView", "On Page Started");
            setLoadingFinished(false);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            setLoadingFinished(true);
            Toast.makeText(WebviewActivity.this, "On Page Finished", Toast.LENGTH_SHORT).show();
            Log.d("WebView", "On Page Finished");
        }
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
