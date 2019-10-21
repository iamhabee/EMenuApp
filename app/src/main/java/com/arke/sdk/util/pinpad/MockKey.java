package com.arke.sdk.util.pinpad;

/**
 * Mock key for Pinpad.
 */

public class MockKey {

    /**
     * KSN data.
     */
    public static String ksnData = "00000000000000000000";

    /**
     * Main key for DUKPT.
     */
    public static String dupktMainKey = "31313131313131313131313131313131";

    /**
     * Main key.
     *
     * It's length can not be less than all work keys.
     */
    public static String mainKey = "313131313131313131313131313131313131313131313131";

    /**
     * MAC key.
     */
    public static String macKey = "333333333333333333333333333333333333333333333333";

    /**
     * PIN key.
     */
    public static String pinKey = "353535353535353535353535353535353535353535353535";

    /**
     * TDK key.
     *
     * It's length must be 24 bytes in non-Chinese version.
     */
    public static String tdkKey = "373737373737373737373737373737373737373737373737";

    /**
     * DEK key.
     */
    public static String dekKey = "393939393939393939393939393939393939393939393939";

    /**
     * CBC MAC key.
     */
    public static String cbcKey = "414141414141414141414141414141414141414141414141";

    /**
     * MAC encrypt key.
     *
     * Encrypted with "333333333333333333333333333333333333333333333333".
     */
    public static String macEncKey = "4BF6E91B1E3A9D814BF6E91B1E3A9D814BF6E91B1E3A9D81";

    /**
     * MAC encrypt key check value.
     */
    public static String macEnvKCV = "ADC67D";

    /**
     * PIN encrypt key.
     *
     * Encrypted with "353535353535353535353535353535353535353535353535".
     */
    public static String pinEncKey = "D0FB24EA73F599C1D0FB24EA73F599C1D0FB24EA73F599C1";

    /**
     * PIN encrypt key check value.
     */
    public static String pinEnvKCV = "D2DB51";

    /**
     * TDK encrypt key.
     *
     * It's length must be 24 bytes in non-Chinese version.
     * Encrypted with "373737373737373737373737373737373737373737373737".
     */
    public static String tdkEncKey = "16B2CCB944DA2CE916B2CCB944DA2CE916B2CCB944DA2CE9";

    /**
     * TDK encrypt key check value.
     */
    public static String tdkEncKCV = "3AA3EA";

    /**
     * DEK encrypt key.
     *
     * Encrypted with "393939393939393939393939393939393939393939393939".
     */
    public static String dekEncKey = "ADBF8135A642B58AADBF8135A642B58AADBF8135A642B58A";

    /**
     * DEK encrypt key check value.
     */
    public static String dekEnvKCV = "74D669";

    /**
     * CBC MAC encrypt key.
     *
     * Encrypted with "414141414141414141414141414141414141414141414141".
     */
    public static String cbcEncKey = "4EC3CF8352B7613D4EC3CF8352B7613D4EC3CF8352B7613D";

    /**
     * CBC MAC encrypt key check value.
     */
    public static String cbcEnvKCV = "C4B738";
}
