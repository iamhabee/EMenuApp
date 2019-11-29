package com.arke.sdk.utilities;

import com.arke.sdk.models.EMenuItem;

import java.util.StringTokenizer;

public class EMenuGenUtils {
    public static String getDecimalFormattedString(String value) {
        StringTokenizer lst = new StringTokenizer(value, ".");
        String str1 = value;
        String str2 = "";
        if (lst.countTokens() > 1) {
            str1 = lst.nextToken();
            str2 = lst.nextToken();
        }
        String str3 = "";
        int i = 0;
        int j = -1 + str1.length();
        if (str1.charAt(-1 + str1.length()) == '.') {
            j--;
            str3 = ".";
        }
        for (int k = j; ; k--) {
            if (k < 0) {
                if (str2.length() > 0)
                    str3 = str3 + "." + str2;
                return str3;
            }
            if (i == 3) {
                str3 = "," + str3;
                i = 0;
            }
            str3 = str1.charAt(k) + str3;
            i++;
        }
    }

    public static String computeAccumulatedPrice(EMenuItem eMenuItem) {
        long itemPrice = Long.parseLong(eMenuItem.getMenuItemPrice().replace(",", ""));
        int orderedQuantity = eMenuItem.getOrderedQuantity();
        if (orderedQuantity == 0) {
            orderedQuantity = 1;
        }
        long totalPack = orderedQuantity * itemPrice;
        String newPrice = String.valueOf(totalPack);
        newPrice = EMenuGenUtils.getDecimalFormattedString(newPrice);
        return newPrice;
    }

}
