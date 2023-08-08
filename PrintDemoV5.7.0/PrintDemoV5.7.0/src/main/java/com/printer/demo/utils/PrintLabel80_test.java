package com.printer.demo.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.printer.demo.R;
import com.printer.sdk.PrinterConstants.LableFontSize;
import com.printer.sdk.PrinterConstants.LablePaperType;
import com.printer.sdk.PrinterConstants.PAlign;
import com.printer.sdk.PrinterConstants.PBarcodeType;
import com.printer.sdk.PrinterConstants.PRotate;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.utils.XLog;

import java.util.ArrayList;

public class PrintLabel80_test {

    private static int MULTIPLE = 8;
    private static final int line_width_border = 2;
    private static final int page_width = 77 * MULTIPLE;
    private static final int page_height = 50 * MULTIPLE;
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
    private static final int[] row_height = {6 * MULTIPLE, 14 * MULTIPLE, 7 * MULTIPLE, 4 * MULTIPLE, 14 * MULTIPLE};
    private static final String TAG = "PrintLabel";

    public void doPrint(final PrinterInstance iPrinter) {
        print(iPrinter, null);

    }

    ArrayList<String> mList;
    String mCode;

    public void doPrint(final PrinterInstance iPrinter, String code, final Resources resources, int begain, int end) {
        mCode = code;
        if (end - begain >= 20) {
          //  ComToastUtils.showShortToast("单次打印不能超过20个货单");
            return;//避免打印机损坏
        }
        int num = (end - begain) == 0 ? 1 : end - begain;
        for (int i = 0; i < num; i++) {
            print(iPrinter, resources);
        }
    }

    public void doPrint(final PrinterInstance iPrinter, final Resources resources, ArrayList<PrintBean> list) {
        for (int i = 0; i < list.size(); i++) {
            print(iPrinter, resources, list.get(i));
        }
    }

    private void print(PrinterInstance iPrinter, Resources resources, PrintBean data) {
        if (resources == null) return;
        iPrinter.pageSetup(LablePaperType.Size_80mm, page_width, page_height);
        drawBox(iPrinter);
        drawVerticalSeparator(iPrinter);
        drawRowContent(iPrinter, resources, data);
        iPrinter.print(PRotate.Rotate_0, 1);
    }

    private void print(PrinterInstance iPrinter, Resources resources) {
        if (resources == null) return;
        iPrinter.pageSetup(LablePaperType.Size_80mm, page_width, page_height);
        drawBox(iPrinter);
        drawVerticalSeparator(iPrinter);
        drawRowContent(iPrinter, resources);
        iPrinter.print(PRotate.Rotate_0, 1);
    }

    private void drawRowContent(PrinterInstance iPrinter, final Resources resources, PrintBean data) {
        int area_start_x = top_left_x;
        int area_start_y = top_left_y;

        // 第一左边图标
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.label_icon7);
        iPrinter.drawGraphic(
                top_left_x + 3,
                top_left_y + 3,
                top_left_x + 136,
                top_left_y + row_height[0] - 5,
                PAlign.START,
                PAlign.START,
                136,
                row_height[0] - 10,
                bitmap);


        // 第一行内容
        iPrinter.drawText(
                area_start_x,
                area_start_y,
                bottom_right_x,
                top_left_y + row_height[0] - line_width_border,
                PAlign.CENTER, PAlign.CENTER, "安迅卡航电子标签", LableFontSize.Size_24, 1, 0, 0, 0, PRotate.Rotate_0);


        // 第二行内容 条形码
        iPrinter.drawBarCode(
                top_left_x,
                top_left_y + row_height[0] + 3,
                top_left_x + 3 * (top_right_x - top_left_x) / 4,
                top_left_y + row_height[0] + row_height[1] - 30,
                PAlign.CENTER, PAlign.CENTER, 0, 0,
                data.transferWaybillNo,
                PBarcodeType.CODE128, 1, 75, PRotate.Rotate_0);

        // 第二行内容 条形码下文字
        iPrinter.drawText(
                top_left_x,
                top_left_y + row_height[0] + row_height[1] - 30,
                top_left_x + 3 * (top_right_x - top_left_x) / 4,
                top_left_y + row_height[0] + row_height[1],
                PAlign.CENTER, PAlign.CENTER,
                data.transferWaybillNo,
                LableFontSize.Size_24, 1, 0, 0, 0, PRotate.Rotate_0);

        // 第二行内容 右边文字
        iPrinter.drawText(
                top_left_x + 3 * (top_right_x - top_left_x) / 4,
                top_left_y + row_height[0],
                top_right_x,
                top_left_y + row_height[0] + row_height[1],
                PAlign.CENTER, PAlign.CENTER,
                "好到家",
                LableFontSize.Size_16, 1, 0, 0, 0, PRotate.Rotate_0);


        // 第三行内容
        iPrinter.drawText(top_left_x + 6,
                top_left_y + row_height[0] + row_height[1],
                bottom_right_x - 6,
                top_left_y + row_height[0] + row_height[1] + row_height[2],// +3弥补边框线宽误差
                PAlign.CENTER, PAlign.CENTER,
                data.flowDirectionCode,
                LableFontSize.Size_24, 1, 0, 0, 0, PRotate.Rotate_0);


        // 第四行内容 左边栏内容
        iPrinter.drawText(top_left_x + 10,
                top_left_y + row_height[0] + row_height[1] + row_height[2],
                top_left_x + 14 * MULTIPLE,
                top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3],
                PAlign.START,
                PAlign.CENTER,
                "货物编号",
                LableFontSize.Size_24, 1, 0, 0, 0, PRotate.Rotate_0);

        // 第四行内容 右边栏内容
        iPrinter.drawText(top_left_x + 14 * MULTIPLE,
                top_left_y + row_height[0] + row_height[1] + row_height[2],
                top_right_x,
                top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3],
                PAlign.CENTER,
                PAlign.CENTER,
                data.goodsNum.replace(data.transferWaybillNo + "-", ""),
                LableFontSize.Size_24,
                1, 0, 0, 0, PRotate.Rotate_0);

        // 第五行内容 条形码上方文字
        iPrinter.drawText(
                top_left_x,
                top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3] + 5,
                top_right_x,
                top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3] + 30,
                PAlign.CENTER, PAlign.CENTER,
                data.goodsNum,
                LableFontSize.Size_24,
                1, 0, 0, 0, PRotate.Rotate_0);

        // 第五行内容 条形码
        drawBarCode(
                top_left_x,
                top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3] + 30,
                top_right_x - 20,
                bottom_left_y - 5,
                PAlign.CENTER, PAlign.CENTER, 0, 0,
                data.goodsNum,
                PBarcodeType.CODE128, 1, 80, PRotate.Rotate_0);
    }

    private void drawRowContent(PrinterInstance iPrinter, final Resources resources) {
        int area_start_x = top_left_x;
        int area_start_y = top_left_y;

        // 第一左边图标
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.label_icon7);
        iPrinter.drawGraphic(
                top_left_x + 3,
                top_left_y + 3,
                top_left_x + 136,
                top_left_y + row_height[0] - 5,
                PAlign.START,
                PAlign.START,
                136,
                row_height[0] - 10,
                bitmap);


        // 第一行内容
        iPrinter.drawText(
                area_start_x,
                area_start_y,
                bottom_right_x,
                top_left_y + row_height[0] - line_width_border,
                PAlign.CENTER, PAlign.CENTER, "安迅卡航电子标签", LableFontSize.Size_24, 1, 0, 0, 0, PRotate.Rotate_0);


        // 第二行内容 条形码
        iPrinter.drawBarCode(
                top_left_x,
                top_left_y + row_height[0] + 3,
                top_left_x + 3 * (top_right_x - top_left_x) / 4,
                top_left_y + row_height[0] + row_height[1] - 30,
                PAlign.CENTER, PAlign.CENTER, 0, 0,
                mCode, PBarcodeType.CODE128, 1, 75, PRotate.Rotate_0);

        // 第二行内容 条形码下文字
        iPrinter.drawText(
                top_left_x,
                top_left_y + row_height[0] + row_height[1] - 30,
                top_left_x + 3 * (top_right_x - top_left_x) / 4,
                top_left_y + row_height[0] + row_height[1],
                PAlign.CENTER, PAlign.CENTER, mCode, LableFontSize.Size_24, 1, 0, 0, 0, PRotate.Rotate_0);

        // 第二行内容 右边文字
        iPrinter.drawText(
                top_left_x + 3 * (top_right_x - top_left_x) / 4,
                top_left_y + row_height[0],
                top_right_x,
                top_left_y + row_height[0] + row_height[1],
                PAlign.CENTER, PAlign.CENTER, "好到家", LableFontSize.Size_16, 1, 0, 0, 0, PRotate.Rotate_0);


        // 第三行内容 左边栏内容
        iPrinter.drawText(top_left_x + 6,
                top_left_y + row_height[0] + row_height[1],
                bottom_right_x - 6,
                top_left_y + row_height[0] + row_height[1] + row_height[2],// +3弥补边框线宽误差
                PAlign.CENTER, PAlign.CENTER, "YZ0100A1-010001-TC010001-TC021002-021003-BJ0210B1",
                LableFontSize.Size_24, 1, 0, 0, 0, PRotate.Rotate_0);


        // 第四行内容 左边栏内容
        iPrinter.drawText(top_left_x + 10,
                top_left_y + row_height[0] + row_height[1] + row_height[2],
                top_left_x + 14 * MULTIPLE,
                top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3],
                PAlign.START,
                PAlign.CENTER, "货物编号",
                LableFontSize.Size_24, 1, 0, 0, 0, PRotate.Rotate_0);

        // 第四行内容 右边栏内容
        iPrinter.drawText(top_left_x + 14 * MULTIPLE,
                top_left_y + row_height[0] + row_height[1] + row_height[2],
                top_right_x,
                top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3],
                PAlign.CENTER,
                PAlign.CENTER, "9999-9999", LableFontSize.Size_24,
                1, 0, 0, 0, PRotate.Rotate_0);

        // 第五行内容 条形码下文字
        iPrinter.drawText(
                top_left_x,
                top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3] + 5,
                top_right_x,
                top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3] + 30,
                PAlign.CENTER, PAlign.CENTER, "KY202104160000002-9999-1111", LableFontSize.Size_24,
                1, 0, 0, 0, PRotate.Rotate_0);

        // 第五行内容 条形码
        drawBarCode(
                top_left_x,
                top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3] + 30,
                top_right_x - 20,
                bottom_left_y - 5,
                PAlign.CENTER, PAlign.CENTER, 0, 0,
                "KY202104160000002-9999-1111", PBarcodeType.CODE128, 1, 80, PRotate.Rotate_0);
    }

    public void drawBarCode(int area_start_x, int area_start_y, int area_end_x, int area_end_y, PAlign xAlign, PAlign yAlign, int start_x, int start_y, String text, PBarcodeType type, int linewidth, int height, PRotate rotate) {
        // int xa = false;
        byte xa;
        if (xAlign == PAlign.CENTER) {
            xa = 1;
        } else if (xAlign == PAlign.END) {
            xa = 2;
        } else {
            xa = 0;
        }

        if (yAlign == PAlign.CENTER) {
            start_y = area_start_y + (area_end_y - area_start_y - height) / 2;
        } else if (yAlign == PAlign.END) {
            start_y = area_end_y - height;
        } else {
            start_y = area_start_y;
        }

        String barcodeType = "128";
        if (type == PBarcodeType.CODABAR) {
            barcodeType = "CODABAR";
        } else if (type == PBarcodeType.CODE128) {
            barcodeType = "128";
        } else if (type == PBarcodeType.CODE39) {
            barcodeType = "39";
        } else if (type == PBarcodeType.CODE93) {
            barcodeType = "93";
        } else if (type == PBarcodeType.JAN8_EAN8) {
            barcodeType = "EAN8";
        } else if (type == PBarcodeType.JAN3_EAN13) {
            barcodeType = "EAN13";
        } else if (type == PBarcodeType.UPC_A) {
            barcodeType = "UPCA";
        } else if (type == PBarcodeType.UPC_E) {
            barcodeType = "UPCE";
        }

        String str = "BA " + area_start_x + " " + area_start_y + " " + area_end_x + " " + area_end_y + " " + xa + "\r\n";
        XLog.d("PrinterInstance", "yxz at PrinterInstance.java drawBarCode() 区域内打印条码：str:" + str);
        PrinterInstance.mPrinter.printText(str);
        String st1 = "B";
        if (rotate != PRotate.Rotate_0) {
            st1 = "VB";
        }

        String str2 = st1 + " " + barcodeType + " " + linewidth + " 2 " + height + " " + area_start_x + " " + start_y + " " + text + "\r\n";
        PrinterInstance.mPrinter.printText(str2);
        XLog.d("PrinterInstance", "yxz at PrinterInstance.java drawBarCode() 区域内打印条码：str2:" + str2);
        String str3 = "BA 0 0 0 0 3\r\n";
        PrinterInstance.mPrinter.printText(str3);
        XLog.d("PrinterInstance", "yxz at PrinterInstance.java drawBarCode() 区域内打印条码：str3:" + str3);
    }

    private void drawHorizontalSeparator(PrinterInstance iPrinter, int start_x, int end_x) {

        int temp = top_left_y; //
        for (int i = 0; i < row_height.length; i++) {
            temp += row_height[i];
            // int start_x = top_left_x;
            // int end_x = top_right_x;
            // Log.i("temp", "第"+(i+1)+"次");
            if (i == 4) {
                end_x = bottom_right_x;
                return;
            }

            iPrinter.drawLine(2, start_x, temp, end_x, temp, true);
            /*
             * if(i!= 3){ iPrinter.drawLine(line_width_border, start_x, temp,
             * end_x, temp); }else{ iPrinter.drawLine(line_width_border,
             * row37_sep2_x, temp, end_x, temp); }
             */
        }
    }

    //
    private void drawVerticalSeparator(PrinterInstance iPrinter) {
        int start_x = top_left_x + 14 * MULTIPLE;
        int start_y = top_left_y + row_height[0] + row_height[1] + row_height[2];
        int end_x = start_x;
        int end_y = top_left_y + row_height[0] + row_height[1] + row_height[2] + row_height[3];
        // 从左边数起第一条分割线
        iPrinter.drawLine(line_width_border, start_x, start_y, end_x, end_y, true);
      /*  // 从左边数起第二条分割线
        start_x = start_x + 15 * MULTIPLE;
        // start_y = top_left_y;
        end_x = start_x;
        //end_y = start_y + row_height[0];
        Log.i(TAG, "start_x；" + start_x + "end_x：" + end_x);
        iPrinter.drawLine(line_width_border, start_x, start_y, end_x, end_y, true);*/
        // 从左边数起第三条分割线
        start_x = top_left_x + 3 * (top_right_x - top_left_x) / 4 + 4;
        start_y = top_left_y + row_height[0];
        end_x = start_x;
        end_y = top_left_y + row_height[0] + row_height[1];
        iPrinter.drawLine(line_width_border, start_x, start_y, end_x, end_y, true);
    }

    private void drawBox(PrinterInstance iPrinter) {
        int border_top_left_y = top_left_y;
        iPrinter.drawBorder(3, top_left_x, border_top_left_y, bottom_right_x, bottom_right_y);
        drawHorizontalSeparator(iPrinter, top_left_x, bottom_right_x);
    }
}
