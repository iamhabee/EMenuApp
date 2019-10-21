package com.arke.sdk.util.emv;

/**
 * EMV parameter.
 */

interface EmvParameter {

    String pack() throws EmvParameterException;
}
