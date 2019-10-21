package com.arke.sdk.util.printer;

/**
 * Bitmap slice.
 *
 * @author feiq
 */

import android.graphics.Bitmap;

/**
 * Bitmap slice.
 */
class BitmapSlice {
    public BitmapSlice(int index, Bitmap bitmap) {
        this.index = index;
        this.bitmap = bitmap;
    }

    /**
     * Slice index.
     */
    public int index;

    /**
     * Bitmap slice data.
     */
    public Bitmap bitmap;
}
