package com.printer.demo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Created by yangxuezhi on 2018-07-11.
 */

public class UpdateUtils {
    /**
     * 得到应用程序的版本名称
     */

    public static String getVersionName(Context mContext) {
        // 用来管理手机的APK
        PackageManager pm = mContext.getPackageManager();
        try {
            // 得到知道APK的功能清单文件
            PackageInfo info = pm.getPackageInfo(mContext.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 系统语言是不是中文
     * @param context
     * @return
     */
    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    /**
     * @param is 输入流
     * @return String 返回的字符串
     * @throws IOException
     */
    public static String readFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        is.close();
        String result = baos.toString();
        baos.close();
        return result;
    }

    public static boolean hasNet(Context mContext) {
        ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }
}
