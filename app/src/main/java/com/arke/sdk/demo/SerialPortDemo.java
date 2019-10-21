package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.RemoteException;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.api.SerialPort;
import com.usdk.apiservice.aidl.serialport.BaudRate;
import com.usdk.apiservice.aidl.serialport.DataBit;
import com.usdk.apiservice.aidl.serialport.DeviceName;
import com.usdk.apiservice.aidl.serialport.ParityBit;

import java.io.File;

/**
 * Serial port demo.
 */

public class SerialPortDemo extends ApiDemo {

    /**
     * Constructor.
     */
    private SerialPortDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get serial port demo instance.
     */
    public static SerialPortDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new SerialPortDemo(context, toast, dialog);
    }

    /**
     * Do serial port functions.
     */
    public void execute(String value) throws RemoteException {
        byte[] readData = new byte[16];
        byte[] writeData = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA};

        if (value.equals(getContext().getString(R.string.open))) {
            open();

        } else if (value.equals(getContext().getString(R.string.send_data_timeout_0))) {
            sendData(writeData);

        } else if (value.equals(getContext().getString(R.string.read_data_timeout_0))) {
            readData(readData);

        } else if (value.equals(getContext().getString(R.string.send_data_timeout_10000))) {
            sendDataWithTimeout(writeData);

        } else if (value.equals(getContext().getString(R.string.read_data_timeout_10000))) {
            readDataWithTimeout(readData);

        } else if (value.equals(getContext().getString(R.string.flush))) {
            flush();

        } else if (value.equals(getContext().getString(R.string.close))) {
            close();
        }

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Close.
     */
    private void close() throws RemoteException {
        SerialPort.getInstance().close();
    }

    /**
     * Flush.
     */
    private void flush() throws RemoteException {
        if (!SerialPort.getInstance().isBufferEmpty(true)) {
            SerialPort.getInstance().clearInputBuffer();
        }
    }

    /**
     * Read data with timeout.
     */
    private void readDataWithTimeout(byte[] readData) throws RemoteException {
        SerialPort.getInstance().read(readData, 10000);
    }

    /**
     * Send data with timeout.
     */
    private void sendDataWithTimeout(byte[] writeData) throws RemoteException {
        SerialPort.getInstance().write(writeData, 10000);
    }

    /**
     * Read data.
     */
    private void readData(byte[] readData) throws RemoteException {
        SerialPort.getInstance().read(readData, 0);
    }

    /**
     * Send data.
     */
    private void sendData(byte[] writeData) throws RemoteException {
        SerialPort.getInstance().write(writeData, 0);
    }

    /**
     * Open.
     */
    private void open() throws RemoteException {
        if (isConnectBase()) {
            String deviceName = getDeviceName("ttyUSB", "ttyACM");
            if (deviceName == null) {
                throw new RemoteException(getContext().getString(R.string.no_device));
            }
            SerialPort.getInstance().open(deviceName);
        } else {
            SerialPort.getInstance().open(DeviceName.USBD);
        }

        SerialPort.getInstance().init(BaudRate.BPS_115200, ParityBit.NOPAR, DataBit.DBS_8);
    }

    /**
     * Get device name.
     */
    private String getDeviceName(String... prefixes) {
        File dev = new File( "/dev" );
        for (File file : dev.listFiles()) {
            for (String prefix : prefixes) {
                if (file.getAbsolutePath().startsWith("/dev/" + prefix)) {
                    return file.toString().substring(5);
                }
            }
        }
        return null;
    }

    /**
     * Is connect base.
     */
    private boolean isConnectBase() {
        File dev = new File("/sys/bus/usb/devices/1-1.1");
        return dev.exists();
    }
}
