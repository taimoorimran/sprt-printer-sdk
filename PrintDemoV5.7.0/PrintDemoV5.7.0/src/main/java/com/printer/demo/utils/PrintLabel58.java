package com.printer.demo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.printer.demo.R;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.PrinterConstants.LableFontSize;
import com.printer.sdk.PrinterConstants.LablePaperType;
import com.printer.sdk.PrinterConstants.PAlign;
import com.printer.sdk.PrinterConstants.PBarcodeType;
import com.printer.sdk.PrinterConstants.PRotate;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.exception.ParameterErrorException;
import com.printer.sdk.exception.PrinterPortNullException;
import com.printer.sdk.exception.ReadException;
import com.printer.sdk.exception.WriteException;
import com.printer.sdk.utils.Utils;

public class PrintLabel58 {

    private static int MULTIPLE = 5;
    private static final int line_width_border = 2;
    private static final int page_width = 75 * MULTIPLE;// 384����
    private static final int page_height = 90 * MULTIPLE;
    private static final int margin_horizontal = 2 * MULTIPLE;
    private static final int top_left_x = margin_horizontal;
    private static final int margin_vertical = 2 * MULTIPLE;
    private static final int top_left_y = margin_vertical;// 32
    private static final int border_width = page_width - 2 * margin_horizontal;
    private static final int border_height = page_height - 2 * margin_vertical;
    private static final int top_right_x = top_left_x + border_width;
    private static final int bottom_left_y = top_left_y + border_height;
    private static final int bottom_right_y = bottom_left_y;
    private static final int bottom_right_x = top_right_x;
    private static final int row36_column1_width = 10 * MULTIPLE;
    private static final int row37_column3_width = 20 * MULTIPLE;
    private static final int row36_sep1_x = top_left_x + row36_column1_width;
    private static final int row37_sep2_x = top_right_x - row37_column3_width;
    private static final int[] row_height = {8 * MULTIPLE * 2, 10 * MULTIPLE, 10 * MULTIPLE, 10 * MULTIPLE,
            10 * MULTIPLE, 10 * MULTIPLE};
    private static final String TAG = "PrintLabel";

    public void doPrint(final PrinterInstance iPrinter, final Resources resources) {
        iPrinter.pageSetup(LablePaperType.Size_58mm, 384, 540);
        iPrinter.drawText(0, 0, 200, 200, PAlign.START, PAlign.START, "扫一扫二维码连接打印机", LableFontSize.Size_48, 0,
                0, 0, 0, PRotate.Rotate_0);
       // iPrinter.drawQrCode(230, 0, "12345678", PRotate.Rotate_0, 6, 1);
        iPrinter.drawBarCode(1, 150, "12345678", PBarcodeType.CODE128, 2, 75, PRotate.Rotate_0);
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.ztl);
        iPrinter.drawGraphic(0, 240, Utils.zoomImage(bitmap, 384,0));
        iPrinter.print(PRotate.Rotate_0, 0);

    }

    public void doPrintTSPL(PrinterInstance iPrinter, Resources resources, Context mContext)
            throws WriteException, PrinterPortNullException, ReadException, ParameterErrorException, Exception {
        iPrinter.pageSetupTSPL(PrinterConstants.SIZE_58mm, 56 * 8, 45 * 8);
        iPrinter.printText("CLS\r\n");
        iPrinter.drawTextTSPL(30, 30, true, 1, 1, null, "区域内打印图片效果演示：");
        Bitmap bmp = BitmapFactory.decodeResource(resources, R.drawable.goodwork);
        iPrinter.drawBorderTSPL(3, 50, 50, 400, 350);
        iPrinter.drawBitmapTSPL(50, 50, 400, 350, PAlign.CENTER, PAlign.CENTER, 0, bmp);
        iPrinter.printTSPL(1, 1);
    }

}
