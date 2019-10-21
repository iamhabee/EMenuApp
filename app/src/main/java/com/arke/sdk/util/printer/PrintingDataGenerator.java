package com.arke.sdk.util.printer;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.binarylibrary.Binary;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Generate printing slices according to specified big bitmap.
 *
 * @author feiq, guosx
 */
public class PrintingDataGenerator {

    private static final int BITMAP_BLOCK_SIZE = 256;
    private static final int BITMAP_SLICE_INCREASE_SIZE = 0;

    /**
     * Generate printing slices according to bitmap.
     */
    public static Observable<PrintingDataSlice> generatePrintingSlices(final Bitmap bitmap) {
        final Long[] startTime = new Long[1];
        Single<List<BitmapSlice>> slicesGenerator = Single.fromCallable(() -> {
            startTime[0] = Calendar.getInstance().getTimeInMillis();

            if (bitmap == null) {
                throw new Exception("The printing bitmap is null");
            }

            // Split bitmap and run multiple threads to handle bitmap in order to improve performance
            List<BitmapSlice> bitmapSlices = splitBitmap(bitmap, BITMAP_BLOCK_SIZE, BITMAP_SLICE_INCREASE_SIZE);
            recycleBitmap(bitmap);
            return bitmapSlices;
        });

        return slicesGenerator.flatMapObservable(slices -> Observable.fromIterable(slices))
                .flatMapSingle(bitmapSlice -> generatePrintingSlice(bitmapSlice))
                .doOnComplete(() -> Timber.d("PrintingDataGenerator.generatePrintingSlices[" + (Calendar.getInstance().getTimeInMillis() - startTime[0]) + "]ms"));
    }


    /**
     * Generate printing source from bitmap.
     * <p>
     * 1. Binarize
     * 2. Create new Bitmap.
     * 3. Generate printing data.
     *
     * @param bitmapSlice bitmapSlice.
     * @return
     */

    private static Single<PrintingDataSlice> generatePrintingSlice(BitmapSlice bitmapSlice) {
        return Single.fromCallable(() -> {
            long startTime = Calendar.getInstance().getTimeInMillis();

            Bitmap bitmap = bitmapSlice.bitmap;
            int index = bitmapSlice.index;

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            int dataWidth = ((width + 31) / 32) * 32;
            byte[] rawBitmapData = new byte[(dataWidth * height) / 8];

            int[] pix = new int[width * height];
            bitmap.getPixels(pix, 0, width, 0, 0, width, height);
            rawBitmapData = Binary.convertArgbToGrayscale(pix, width, height, dataWidth);

            // Build bitmap bytes
            BmpFile bmpFile = new BmpFile();
            byte[] bitmapBytes = bmpFile.getBitmapAsByteArray(rawBitmapData,
                    width, height);

            recycleBitmap(bitmap);

            Timber.d("PrintingDataGenerator.generatePrintingSlice[" + index + "] generated" + "[" + (Calendar.getInstance().getTimeInMillis() - startTime) + "]ms");

            return new PrintingDataSlice(index, bitmapBytes);
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Split the specified bitmap to multi bitmap according the height.
     *
     * @param bitmap        The bitmap to be split
     * @param firstHeight   First bitmap height
     * @param increaseSpace Slice height increase space
     * @return The list of bitmap
     */
    private static List<BitmapSlice> splitBitmap(Bitmap bitmap, int firstHeight, int increaseSpace) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int pointY = 0;
        int actualSliceHeight = firstHeight;

        ArrayList<BitmapSlice> slices = new ArrayList<>();
        int sliceCount = 1;

        while (pointY < height) {

            if (pointY + actualSliceHeight >= height) {
                actualSliceHeight = height - pointY;
            }

            Bitmap slice = Bitmap.createBitmap(bitmap, 0, pointY, width, actualSliceHeight);

            slices.add(new BitmapSlice(sliceCount++, slice));

            pointY += actualSliceHeight;
            actualSliceHeight += increaseSpace;
        }

        return slices;
    }

    /**
     * recycleBitmap
     * Avoid OOM
     *
     * @param bitmap The bitmap to be recycle
     */
    private static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * Convert bitmap from argb to gray.
     * <p>
     * 二值化转换.
     *
     * @param bmpOriginal Bitmap
     * @param width       Bitmap width
     * @param height      Bitmap height
     * @param graySource  the gray data array generated from bitmap.
     */
    private static void convertArgbToGrayscale(Bitmap bmpOriginal, int width, int height, byte[] graySource, int dataWidth) {
        int pixel;
        int k = 0;
        int B = 0, G = 0, R = 0;
        int threshold = 150; // Gray scale threshold 灰度阈值
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++, k++) {
                // 获取一个像素的颜色值
                // Get pixel's color
                pixel = bmpOriginal.getPixel(y, x);
                // 获取RGB数据
                // Get RGB data
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // 转换成灰度值
                R = (int) ((float) 0.299 * R + (float) 0.587 * G + (float) 0.114 * B);

                if (R < threshold) {
                    graySource[k] = 0;
                } else {
                    graySource[k] = 1;
                }
            }

            if (dataWidth > width) {
                for (int p = width; p < dataWidth; p++, k++) {
                    graySource[k] = 1;
                }
            }
        }
    }

    /**
     * Convert gray source to bitmap source.
     * <p>
     * Merge 8 bytes from gray source to 1 byte.
     *
     * @param graySource   gray source.
     * @param bitmapSource bitmap source.
     */
    private static void createRawMonochromeData(byte[] graySource, byte[] bitmapSource) {
        int length = 0;
        for (int i = 0; i < graySource.length; i = i + 8) {
            byte first = graySource[i];
            for (int j = 1; j <= 7; j++) {
                first = (byte) ((first << 1) | graySource[i + j]);
            }
            bitmapSource[length] = first;
            length++;
        }
    }
}
