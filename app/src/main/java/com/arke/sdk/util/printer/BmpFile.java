package com.arke.sdk.util.printer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * BmpFile
 * Created by guosx on 2017/5/8.
 */

public class BmpFile {
    // --- Private constants
    private final static int BITMAPFILEHEADER_SIZE = 14;
    private final static int BITMAPINFOHEADER_SIZE = 40;

    // --- Bitmap file header
    private byte bfType[] = {(byte) 'B', (byte) 'M'};
    private int bfSize = 0;
    private int bfReserved1 = 0;
    private int bfReserved2 = 0;
    private int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE + 8;

    // --- Bitmap info header
    private int biSize = BITMAPINFOHEADER_SIZE;
    private int biWidth = 0;
    private int biHeight = 0;
    private int biPlanes = 1;
    private int biBitCount = 1;
    private int biCompression = 0;
    private int biSizeImage = 0;
    private int biXPelsPerMeter = 0x0;
    private int biYPelsPerMeter = 0x0;
    private int biClrUsed = 2;
    private int biClrImportant = 2;

    // --- Bitmap raw data
    private byte bitmap[];

    // ---- Scanlinsize;
    int scanLineSize = 0;

    // -- Color Pallette to be used for pixels.
    private byte colorPalette[] = {0, 0, 0, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255};


    public byte[] getBitmapAsByteArray(byte[] imagePix, int parWidth,
                                       int parHeight) throws IOException {
        ByteArrayOutputStream baOut;
        convertImage(imagePix, parWidth, parHeight);
        baOut = new ByteArrayOutputStream(64 + scanLineSize * biHeight);
        writeBitmapFileHeader_b(baOut);
        writeBitmapInfoHeader_b(baOut);
        writePixelArray_b(baOut);
        return baOut.toByteArray();

    }

    /*
     * convertImage converts the memory image to the bitmap format (BRG). It
     * also computes some information for the bitmap info header.
     */
    private boolean convertImage(byte[] imagePix, int parWidth, int parHeight) {

        bitmap = imagePix;
        bfSize = 62 + (((parWidth + 31) / 32) * 4 * parHeight);
        biWidth = parWidth;
        biHeight = parHeight;
        scanLineSize = ((parWidth + 31) / 32) * 4;
        return (true);
    }

    /*
     *
     * intToWord converts an int to a word, where the return value is stored in
     * a 2-byte array.
     */
    private byte[] intToWord(int parValue) {

        byte retValue[] = new byte[2];
        retValue[0] = (byte) (parValue & 0x00FF);
        retValue[1] = (byte) ((parValue >> 8) & 0x00FF);
        return (retValue);

    }

    /*
     *
     * intToDWord converts an int to a double word, where the return value is
     * stored in a 4-byte array.
     */
    private byte[] intToDWord(int parValue) {

        byte retValue[] = new byte[4];
        retValue[0] = (byte) (parValue & 0x00FF);
        retValue[1] = (byte) ((parValue >> 8) & 0x000000FF);
        retValue[2] = (byte) ((parValue >> 16) & 0x000000FF);
        retValue[3] = (byte) ((parValue >> 24) & 0x000000FF);
        return (retValue);

    }

    private void writeBitmapFileHeader_b(ByteArrayOutputStream fos) throws IOException {
        fos.write(bfType);
        fos.write(intToDWord(bfSize));
        fos.write(intToWord(bfReserved1));
        fos.write(intToWord(bfReserved2));
        fos.write(intToDWord(bfOffBits));
    }

	/*
     *
	 * writeBitmapInfoHeader writes the bitmap information header to the file.
	 */

    private void writeBitmapInfoHeader_b(ByteArrayOutputStream fos) throws IOException {
        fos.write(intToDWord(biSize));
        fos.write(intToDWord(biWidth));
        fos.write(intToDWord(biHeight));
        fos.write(intToWord(biPlanes));
        fos.write(intToWord(biBitCount));
        fos.write(intToDWord(biCompression));
        fos.write(intToDWord(biSizeImage));
        fos.write(intToDWord(biXPelsPerMeter));
        fos.write(intToDWord(biYPelsPerMeter));
        fos.write(intToDWord(biClrUsed));
        fos.write(intToDWord(biClrImportant));
        fos.write(colorPalette);

    }

    /**
     * Reverse write byte according to line
     */
    private void writePixelArray_b(ByteArrayOutputStream fos) {
        for (int i = biHeight; i > 0; i--) {
            for (int k = (i - 1) * (scanLineSize); k < ((i - 1) * (scanLineSize))
                    + (scanLineSize); k++) {
                fos.write(bitmap[k] & 0xFF);
            }
        }
    }
}
