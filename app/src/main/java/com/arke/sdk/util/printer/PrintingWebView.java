package com.arke.sdk.util.printer;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.provider.MediaStore;
//import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * PrintingWebView
 * Create and load WebView
 * Created by guosx on 2017/5/5.
 */
public class PrintingWebView {

    private static PrintingWebView instance = null;

    private String indexUrl = "file:///android_asset/print-template/index.html";

    private boolean loadingFinished = true;

    private Context context;

    /**
     * Page load delay time, ms.
     */
    private int PAGE_LOAD_DELAY_TIME = 50;

    /**
     * Html content rendered timeout, unit ms.
     */
    private int HTML_CONTENT_LOAD_TIMEOUT = 2000;

    /**
     * Page loaded retry times.
     */
    private int PAGE_LOADED_RETRY_TIMES = 100;

    private CountDownLatch loadingFinishedLatch = new CountDownLatch(1);

    private WebView webView = null;

    public final static String APP_PATH_SD_CARD = "/RukkabetAgent/assets/";
    public final static String APP_THUMBNAIL_PATH_SD_CARD = "screenshots";

    public static PrintingWebView getInstance() {
        if (instance == null) {
            return instance = new PrintingWebView();
        } else {
            return instance;
        }
    }

    /**
     * Capture web view.
     *
     * @return
     */
    @Nullable
    public Single<Bitmap> captureWebView() {
        final Long[] startTime = new Long[1];
        Single<Bitmap> capture = Single.fromCallable(() -> {
            startTime[0] = Calendar.getInstance().getTimeInMillis();

            int width = webView.getWidth();
            int height = webView.getContentHeight();

            if (width <= 0 || height <= 10) {
                throw new PageNotLoadedException();
            }

            // WebView screenshot
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            // Save bitmap top internal storage
//            saveToExternalStorage(bitmap);
            Canvas canvas = new Canvas(bitmap);
            webView.draw(canvas);

            return bitmap;
        });

        return capture.subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(failures -> failures.zipWith(Flowable.range(1, PAGE_LOADED_RETRY_TIMES), (err, attempt) -> {
                    return attempt < PAGE_LOADED_RETRY_TIMES ?
                            Flowable.timer(PAGE_LOAD_DELAY_TIME, TimeUnit.MILLISECONDS) :
                            Flowable.error(err);
                }).flatMap(x -> x))
                .flatMap(bitmap -> clearPrintingContent().toSingleDefault(bitmap))
                .doOnSuccess(bitmap -> Timber.d("PrintingWebView.captureWebView[" + (Calendar.getInstance().getTimeInMillis() - startTime[0]) + "]ms"));
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("ScreenShot Path", directory.getAbsolutePath());
        return directory.getAbsolutePath();
    }

    public boolean saveToExternalStorage(Bitmap image) {
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SD_CARD + APP_THUMBNAIL_PATH_SD_CARD;

        try {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream fOut = null;
            File file = new File(fullPath, "desiredFilename.png");
            file.createNewFile();
            fOut = new FileOutputStream(file);

// 100 means no compression, the lower you go, the stronger the compression
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            return true;

        } catch (Exception e) {
            Log.e("saveToExternalStorage()", e.getMessage());
            return false;
        }
    }

    /**
     * Wait until content rendered.
     *
     * @return
     */
    private Completable utilContentRendered() {
        return Completable.create(e -> {
            ViewTreeObserver viewTreeObserver = webView.getViewTreeObserver();

            viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int height = webView.getMeasuredHeight();
                    if (height > 8) {
                        e.onComplete();
                        webView.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    return false;
                }
            });
        });
    }

    /**
     * Load printing template content.
     *
     * @param templateId templateId. d. The value should be exactly same with the id in index.html
     */
    public Completable loadPrintingContent(final String templateId, String json) {
        final Long[] startTime = new Long[1];

        String loadContentScript = "window.loadContent('" + templateId + "', " + json + ")";
//        String loadContentScript = "document.getElementById('" + templateId + "').innerHTML";

        return Single.just(loadContentScript)
                // // await util the page has been loaded
                .doOnSuccess(value -> {
                    startTime[0] = Calendar.getInstance().getTimeInMillis();
                    this.awaitPageLoaded();
                })
                .flatMapMaybe(script -> evaluateJavascript(script))
                .flatMapCompletable((value) -> Completable.complete())
                .doOnComplete(() -> {
                    Timber.d("PrintingWebView.loadPrintingContent[" + (Calendar.getInstance().getTimeInMillis() - startTime[0]) + "]ms");
                });
    }

    /**
     * Evaluate Javascript in webView.
     *
     * @param javascript
     * @return
     */
    Maybe<Object> evaluateJavascript(String javascript) {
        return Maybe.create(e -> {
            // Load template Page
            Long startTime = Calendar.getInstance().getTimeInMillis();
            webView.evaluateJavascript(javascript, value -> {
                if (value == null || "null".equals(value)) {
                    e.onComplete();
                } else {
                    e.onSuccess(JSON.parse(value));
                }

                Timber.d("PrintingWebView.evaluateJavascript[" + (Calendar.getInstance().getTimeInMillis() - startTime) + "]ms");
            });
        }).subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Init a web view for printing.
     * <p>
     * Maybe you can invoke when app starts to save print time.
     */
    public void create(Context context) {
        this.context = context;

        createWebView(context);

        // init web view
        initWebView();

        // load index page
        webView.loadUrl(indexUrl);
    }

    /**
     * Init web view.
     */
    private void initWebView() {
        webView.setInitialScale(100);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.setInitialScale(100);
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptEnabled(true);

        // TODO process finish
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, WebResourceRequest request) {

                setLoadingFinished(false);

                webView.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onPageStarted(
                    WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                setLoadingFinished(false);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setLoadingFinished(true);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Timber.d(cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });
    }


    /**
     * Create a new WebView
     *
     * @return WebView
     */
    private void createWebView(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;
        params.width = 384;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new RelativeLayout.LayoutParams(384, RelativeLayout.LayoutParams.WRAP_CONTENT));

        this.webView = new WebView(context);
        webView.setLayoutParams(new LinearLayout.LayoutParams(384, LinearLayout.LayoutParams.WRAP_CONTENT));
        webView.setVisibility(View.INVISIBLE);
        linearLayout.addView(webView);

        windowManager.addView(linearLayout, params);
    }

    /**
     * loadBlank
     * WebView should load blank page after load template page finish.
     * Because if no load blank page, the next time load template will get and print the previous page.
     */
    private Completable clearPrintingContent() {
        return evaluateJavascript("window.clearContent();")
                .flatMapCompletable(value -> Completable.complete());
    }

    WebView getWebView() {
        return webView;
    }

    public String getIndexUrl() {
        return indexUrl;
    }

    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }

    private void setLoadingFinished(boolean finished) {
        this.loadingFinished = finished;
        if (this.loadingFinished) {
            loadingFinishedLatch.countDown();
        }
    }

    /**
     * Wait the current page loaded.
     *
     * @throws InterruptedException
     */
    public void awaitPageLoaded() throws InterruptedException {
        this.loadingFinishedLatch.await();
    }

    /**
     * Wait current page loaded.
     *
     * @param time
     * @param unit
     * @throws InterruptedException
     */
    public void awaitPageLoaded(int time, TimeUnit unit) throws InterruptedException {
        this.loadingFinishedLatch.await(time, unit);
    }
}

