package com.arke.sdk.ui.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
        EMenuRoundedImageCorner.TOP_LEFT, EMenuRoundedImageCorner.TOP_RIGHT,
        EMenuRoundedImageCorner.BOTTOM_LEFT, EMenuRoundedImageCorner.BOTTOM_RIGHT
})
public @interface EMenuRoundedImageCorner {
    int TOP_LEFT = 0;
    int TOP_RIGHT = 1;
    int BOTTOM_RIGHT = 2;
    int BOTTOM_LEFT = 3;
}
