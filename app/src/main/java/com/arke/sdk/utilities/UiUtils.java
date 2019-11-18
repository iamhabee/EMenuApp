package com.arke.sdk.utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.arke.sdk.ArkeSdkDemoApplication;
import com.arke.sdk.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.contracts.SnackBarActionClickedListener;
import com.arke.sdk.ui.views.LoadingImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.labters.lottiealertdialoglibrary.ClickListener;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;

import org.jetbrains.annotations.NotNull;

import static com.arke.sdk.utilities.DataStoreClient.TAG;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.indexOfIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class UiUtils {

    public static Handler handler = new Handler(Looper.getMainLooper());

    private static LottieAlertDialog lottieAlertDialog;
    private static Context context;

    public UiUtils(Context context) {
        this.context = context;
    }

    private static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void runOnMain(final @NonNull Runnable runnable) {
        if (isMainThread()) runnable.run();
        else handler.post(runnable);
    }

    public static void showSafeToast(final String toastMessage) {
        runOnMain(() -> Toast.makeText(ArkeSdkDemoApplication.getInstance(), toastMessage, Toast.LENGTH_LONG).show());
    }

    /***
     * Toggles a view visibility state
     *
     * @param view The view to toggle
     * @param show Flag indicating whether a view should be setVisible or not
     **/
    public static void showView(View view, boolean show) {
        if (view != null) {
            if (show) {
                if (view.getVisibility() != View.VISIBLE) {
                    view.setVisibility(View.VISIBLE);
                    view.invalidate();
                }
            } else {
                if (view.getVisibility() != View.GONE) {
                    view.setVisibility(View.GONE);
                    view.invalidate();
                }
            }
        }
    }

    public static void badgeTab(TabLayout tabLayout, int position, int count, boolean hideTab) {
        if (tabLayout != null) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null) {
                View viewAtTab = tab.getCustomView();
                if (viewAtTab != null) {
                    ViewFlipper tabIndicatorFlipper = viewAtTab.findViewById(R.id.bagde_indicator_flipper);
                    TextView tabBadgeView = viewAtTab.findViewById(R.id.tab_count);
                    if (hideTab) {
                        showView(tabIndicatorFlipper, false);
                    } else {
                        showView(tabIndicatorFlipper, true);
                        if (count == 0) {
                            toggleViewFlipperChild(tabIndicatorFlipper, 1);
                        } else {
                            toggleViewFlipperChild(tabIndicatorFlipper, 0);
                            if (count >= 99) {
                                count = 99;
                            }
                            tabBadgeView.setText(String.valueOf(count));
                        }
                    }
                }
            }
        }
    }

    public static void dismissKeyboard(View trigger) {
        InputMethodManager imm = (InputMethodManager) ArkeSdkDemoApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(trigger.getWindowToken(), 0);
        }
    }

    public static void forceShowKeyboard(View trigger) {
        InputMethodManager imm = (InputMethodManager) ArkeSdkDemoApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(trigger, InputMethodManager.SHOW_FORCED);
        }
    }

    public static void toggleViewVisibility(View view, boolean show) {
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public static void toggleViewAlpha(View view, boolean show) {
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public static Spannable highlightTextIfNecessary(String search, String originalText, int color) {
        if (isNotEmpty(search)) {
            if (containsIgnoreCase(originalText, search.trim())) {
                int startPost = indexOfIgnoreCase(originalText, search.trim());
                int endPost = startPost + search.length();
                Spannable spanText = Spannable.Factory.getInstance().newSpannable(originalText);
                if (startPost != -1) {
                    spanText.setSpan(new ForegroundColorSpan(color), startPost, endPost, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return spanText;
                } else {
                    return new SpannableString(originalText);
                }
            } else {
                return new SpannableString(originalText);
            }

        } else {
            return new SpannableString(originalText);
        }
    }

    public static Spannable highlightTextIfNecessary(String search, Spanned originalText, int color) {
        try {
            if (isNotEmpty(search)) {
                if (containsIgnoreCase(originalText, search.trim())) {
                    int startPost = indexOfIgnoreCase(originalText, search.trim());
                    int endPost = startPost + search.length();
                    Spannable spanText = Spannable.Factory.getInstance().newSpannable(originalText);
                    if (startPost != -1) {
                        spanText.setSpan(new ForegroundColorSpan(color), startPost, endPost, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return spanText;
                    } else {
                        return new SpannableString(originalText);
                    }
                } else {
                    return new SpannableString(originalText);
                }

            } else {
                return new SpannableString(originalText);
            }
        } catch (IndexOutOfBoundsException e) {
            return new SpannableString(originalText);
        }
    }

    /**
     * Change given image view tint
     *
     * @param imageView target image view
     * @param color     tint color
     */
    public static void tintImageView(ImageView imageView, int color) {
        imageView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public static void tintImageViewNoMode(ImageView imageView, int color) {
        imageView.setColorFilter(color);
    }

    public static void loadImageIntoView(ImageView imageView, String photoUrl) {
        if (imageView instanceof LoadingImageView) {
            LoadingImageView loadingImageView = (LoadingImageView) imageView;
            loadingImageView.startLoading();
        }
        RequestOptions imageLoadRequestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(ArkeSdkDemoApplication.getInstance())
                .load(photoUrl).apply(imageLoadRequestOptions).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                if (imageView instanceof LoadingImageView) {
                    LoadingImageView loadingImageView = (LoadingImageView) imageView;
                    loadingImageView.stopLoading();
                }
                return false;
            }
        }).into(imageView);
    }

    public static synchronized void animateView(View view, Animation animation) {
        if (view != null) {
            view.startAnimation(animation);
        }
    }

    public static Animation getAnimation(Context context, int animationId) {
        return AnimationUtils.loadAnimation(context, animationId);
    }

    public static void blinkView(View mView) {
        try {
            Animation mFadeInFadeIn = getAnimation(ArkeSdkDemoApplication.getInstance(), android.R.anim.fade_in);
            mFadeInFadeIn.setRepeatMode(Animation.REVERSE);
            animateView(mView, mFadeInFadeIn);
        } catch (IllegalStateException | NullPointerException ignored) {

        }
    }

    public static void snackMessage(String message, View anchorView, boolean shortDuration, String actionMessage, SnackBarActionClickedListener snackBarActionClickedListener) {
        if (anchorView != null) {
            Snackbar snackbar = Snackbar.make(anchorView, message, actionMessage != null ? Snackbar.LENGTH_INDEFINITE : (shortDuration ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG));
            if (actionMessage != null) {
                snackbar.setAction(actionMessage, view -> snackBarActionClickedListener.onSnackActionClicked());
            }
            snackbar.show();
        }
    }

    /**
     * Lightens a color by a given factor.
     *
     * @param color  The color to lighten
     * @param factor The factor to lighten the color. 0 will make the color unchanged. 1 will make the
     *               color white.
     * @return lighter version of the specified color.
     */
    public static int lighter(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }

    /**
     * Returns darker version of specified <code>color</code>.
     */
    public static int darker(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    public static int getRandomColor() {
        ColorGenerator generator = ColorGenerator.MATERIAL;
        return generator.getRandomColor();
    }

    public static void toggleViewFlipperChild(ViewFlipper viewFlipper, int child) {
        EMenuLogger.d(TAG, "Toggling to " + child);
        if (viewFlipper.getDisplayedChild() != child) {
            viewFlipper.setDisplayedChild(child);
        }
    }

    public static boolean whitish(int color) {
//        int red = 0xFF & (color >> 16);
//        int green = 0xFF & (color >> 8);
//        int blue = 0xFF & (color);
//        int luminance = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);
        return color == Color.WHITE;
    }

    public static void showMessage(Context context, String title, String description, String restaurantOrBarName,
                                   String restaurantEmailAddress, Class nextActivity) {
        LottieAlertDialog lottieAlertDialog = new LottieAlertDialog
                .Builder(context, DialogTypes.TYPE_SUCCESS)
                .setTitle(title).setDescription(description)
                .setPositiveText("OK").setPositiveListener(lottieAlertDialog1 -> {
                    lottieAlertDialog1.dismiss();
                    Intent intent = new Intent(context, nextActivity);
                    intent.putExtra("restaurantOrBarName", restaurantOrBarName);
                    intent.putExtra("restaurantEmailAddress", restaurantEmailAddress);
                    context.startActivity(intent);
                    ((Activity)context).finish();
                })
                .build();
        if (!lottieAlertDialog.isShowing()) {
            lottieAlertDialog.setCancelable(true);
            lottieAlertDialog.show();
        }
    }

    public static void showErrorMessage(Context context, String title, String description) {
        LottieAlertDialog lottieAlertDialog = new LottieAlertDialog
                .Builder(context, DialogTypes.TYPE_ERROR)
                .setTitle(title).setDescription(description)
                .setPositiveText("OK").setPositiveListener(Dialog::dismiss)
                .build();
        if (!lottieAlertDialog.isShowing()) {
            lottieAlertDialog.setCancelable(true);
            lottieAlertDialog.show();
        }
    }

    public static void showOperationsDialog(Context context, String title, String description) {
        lottieAlertDialog = new LottieAlertDialog
                .Builder(context, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        lottieAlertDialog.setCancelable(false);
        lottieAlertDialog.show();
    }


    public static void dismissProgressDialog() {
        if (lottieAlertDialog != null) {
            lottieAlertDialog.dismiss();
            lottieAlertDialog = null;
        }
    }

}
