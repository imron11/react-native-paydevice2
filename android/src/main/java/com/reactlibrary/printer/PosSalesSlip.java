package com.reactlibrary.printer;

// SDK PayDevice
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.paydevice.smartpos.sdk.SmartPosException;
import com.paydevice.smartpos.sdk.printer.PrinterManager;

import java.util.Locale;

public class PosSalesSlip {

    private static String TAG = "PosSalesSlip";
    /** Printer object */
    private PrinterManager mPrinterManager;
    /** Application context */
    private Context mContext;

    public PosSalesSlip(Context context, PrinterManager printer) {
        this.mContext = context;
        this.mPrinterManager = printer;
    }

    // prepare printer
    public int prepare() {
        try {
            mPrinterManager.connect();
            if (mPrinterManager.getPrinterType() == PrinterManager.PRINTER_TYPE_SERIAL) {
                if (mPrinterManager.cmdGetPrinterModel() == PrinterManager.PRINTER_MODEL_PRN2103) {
                    Log.d(TAG,"model:PRN 2103");
                } else {
                    Log.d(TAG,"model:UNKNOWN");
                }
            }
            mPrinterManager.checkPaper();
        } catch (SmartPosException e) {
            return e.getErrorCode();
        }
        return 0;
    }

    //print text
    public void printText(String str, int size) {
        try {
            Locale locale = mContext.getResources().getConfiguration().locale;
            String language = locale.getLanguage();
            boolean leftIsDoubleByte = false;

            if (language.endsWith("zh")) {
                mPrinterManager.cmdSetPrinterLanguage(PrinterManager.CODE_PAGE_GB18030);
                mPrinterManager.setStringEncoding("GB18030");
                leftIsDoubleByte = true;
            } else {
                mPrinterManager.cmdSetPrinterLanguage(PrinterManager.CODE_PAGE_CP437);
                mPrinterManager.setStringEncoding("CP437");
            }

            // set size
            if (size == 1) {
                mPrinterManager.cmdSetPrintMode(PrinterManager.FONT_DOUBLE_HEIGHT);
            } else {
                mPrinterManager.cmdSetPrintMode(PrinterManager.FONT_DEFAULT);
            }

            mPrinterManager.cmdSetAlignMode(PrinterManager.ALIGN_MIDDLE);
            mPrinterManager.sendData(str);
            mPrinterManager.cmdLineFeed();
        } catch (SmartPosException e) {
        }
    }

    //print pic
    public void printPic(Bitmap imgToPrint, int newWidth) {
        try {
            int width = 0;
            Bitmap img = imgToPrint;
            final int totalDots = mPrinterManager.getDotsPerLine();

            if (img.getWidth() >= totalDots) {
                img = Bitmap.createScaledBitmap(img, 328, (328 * img.getHeight())/img.getWidth(), true);
            }

            if (newWidth != 0) {
                img = Bitmap.createScaledBitmap(img, newWidth, (newWidth * img.getHeight())/img.getWidth(), true);
            }

            int xPos = (totalDots - img.getWidth()) >> 1;//horizontal centre
            int yPos = 0;

            mPrinterManager.cmdBitmapPrintEx(img, xPos, yPos);
            mPrinterManager.cmdLineFeed();
        } catch (SmartPosException e) {
        }
    }
}
