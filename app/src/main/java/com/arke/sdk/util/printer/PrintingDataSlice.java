package com.arke.sdk.util.printer;

/**
 * Printing data slice.
 *
 * @author feiq
 */

import android.support.annotation.NonNull;

class PrintingDataSlice implements Comparable<PrintingDataSlice> {
    public PrintingDataSlice(int index, byte[] data) {
        this.index = index;
        this.data = data;
    }

    /**
     * Slice index.
     */
    public int index;

    /**
     * Bitmap slice data.
     */
    public byte[] data;

    @Override
    public int compareTo(@NonNull PrintingDataSlice target) {
        return ((Integer) this.index).compareTo(target.index);

    }
}
