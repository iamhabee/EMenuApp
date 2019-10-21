package com.arke.sdk.vas;

/**
 * Value added service action.
 */

public class VASAction {

    public static final String SALE = "SALE";

    private static String[] actions = new String[]{SALE};

    static int getIndex(String action) {
        for (int i = 0; i < actions.length; i++) {
            if (actions[i].equals(action)) {
                return i;
            }
        }
        return -1;
    }

    static String getAction(int index) {
        return actions[index];
    }
}
