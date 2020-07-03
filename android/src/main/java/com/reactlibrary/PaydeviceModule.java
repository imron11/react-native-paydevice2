package com.reactlibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

// SDK PayDevice
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.paydevice.smartpos.sdk.SmartPosException;
import com.paydevice.smartpos.sdk.printer.PrinterManager;
import com.paydevice.smartpos.sdk.printer.Printer;
import com.paydevice.smartpos.sdk.printer.UsbPrinter;
import com.paydevice.smartpos.sdk.printer.SerialPortPrinter;
import com.paydevice.smartpos.sdk.cashdrawer.CashDrawer;

import com.reactlibrary.printer.PosSalesSlip;

import javax.annotation.Nullable;

public class PaydeviceModule extends ReactContextBaseJavaModule {

    private static String TAG = "PayDevice";
    private PosSalesSlip mTemplate = null;
    private PrinterManager mPrinterManager = null;

    private final ReactApplicationContext reactContext;

    // PayDevice Variable
    private Printer mPrinter = null;

    private static final String KEY_PRINTING_RESULT = "printing_result";
    private static final String KEY_PRINTER_UPDATE = "printer_update_result";

    //Message type
    private static final int MSG_PRINTING_RESULT = 1;
    private static final int MSG_PRINTER_UPDATE_START = 2;
    private static final int MSG_PRINTER_UPDATE_DONE = 3;
    private static final int MSG_PRINTER_SET_BMP_NVRAM = 4;
    private static final int MSG_PRINTER_DEL_BMP_NVRAM = 5;

    public PaydeviceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "Paydevice";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    private void initDevice() {
        //check printer for different models
        //Built-in serialport printer: FH070H-A,FH100H-A,FH070A2
        //Built-in usb printer:        FH116A3

        if (mPrinter == null) {
            //if usb printer no found then try serialport printer
            try {
                //80mm USB printer
                mPrinter = new UsbPrinter(this.reactContext);
                mPrinter.selectBuiltInPrinter();
                mPrinter.open();
                mPrinter.close();
            } catch (SmartPosException e) {
                Log.d(TAG,"no usb printer,try serialport printer");
                //58mm serialport printer
                mPrinter = new SerialPortPrinter();
                mPrinter.selectBuiltInPrinter();
            }

            mPrinterManager = new PrinterManager(mPrinter,
                    (mPrinter.getType() == PrinterManager.PRINTER_TYPE_USB)
                            ? PrinterManager.TYPE_PAPER_WIDTH_80MM
                            : PrinterManager.TYPE_PAPER_WIDTH_58MM);
        }
    }

    @ReactMethod
    public void checkPrinter(Callback callback) {
        initDevice();
        if (mTemplate == null) {
            mTemplate = new PosSalesSlip(this.reactContext, mPrinterManager);
        }
        int err = mTemplate.prepare();
        callback.invoke(err);
    }

    @ReactMethod
    public void printText(String txtToPrint, @Nullable ReadableMap options){
        int sizeText = 0;

        // check size
        if (options != null) {
            sizeText = options.hasKey("size") ? options.getInt("size") : 0;
        }

        mTemplate.printText(txtToPrint, sizeText);
    }

    @ReactMethod
    public  void printPic(String base64encodeStr, @Nullable ReadableMap options){
        int newWidth = 0;

        byte[] decodedString = Base64.decode(base64encodeStr, Base64.DEFAULT);
        Bitmap img = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        //check width
        if (options != null) {
            newWidth = options.hasKey("width") ? options.getInt("width") : 0;
        }


        mTemplate.printPic(img, newWidth);
    }
}
