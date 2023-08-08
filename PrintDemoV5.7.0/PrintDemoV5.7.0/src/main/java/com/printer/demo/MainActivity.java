package com.printer.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.printer.demo.global.GlobalContants;
import com.printer.demo.utils.UpdateUtils;
import com.printer.demo.utils.XTUtils;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.utils.XLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    // 设置打印机的LinearLayout
    private LinearLayout llSetPrinter;
    // 打印标签的LinearLayout
    private LinearLayout llLablePrint;
    // 打印文本的LinearLayout
    private LinearLayout llTextPrint;
    // 打印PDF文件的LinearLayout
    private LinearLayout llPDFPrint;
    // 打印条码的LinearLayout
    private LinearLayout llBarcodePrint;
    // 打印图片的LinearLayout
    private LinearLayout llImagePrint;
    //采集打印的LinearLayout
    private LinearLayout llCollectPrint;
    //在线升级的LinearLayout
    private LinearLayout llUpdate;
    private ImageView imgRedpoint;
    private LinearLayout header;
    private static Context mContext;
    private View v1;
    private ProgressDialog dialog;
    private UpdateHandler updateHandler = new UpdateHandler();
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
    };
    private final static int REQUEST_PERMISSION_CODE = 200;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v1 = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(v1);
        init();
        //判断是否有相应权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!checkPermissionAllGranted(PERMISSIONS_STORAGE)) {
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQUEST_PERMISSION_CODE
                );
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbAttachReceiver, filter);

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        if (!UpdateUtils.hasNet(mContext)) {
            Message message = new Message();
            message.what = GlobalContants.NETWORK_ERROR;
            updateHandler.sendMessage(message);
            return;
        }
        checkUpdate();

    }

    private boolean checkPermissionAllGranted(String[] perssions) {
        for (String perssion : perssions) {
            if (ActivityCompat.checkSelfPermission(this, perssion) != PackageManager.PERMISSION_GRANTED) {
                XLog.d("yxz", "yxz at MainActivity.java checkPermissionAllGranted() perssion：" + "没有权限");
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int i = 0; i < grantResults.length; i++) {
                int grant = grantResults[i];
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    XLog.d("yxz", "yxz at MainActivity.java checkPermissionAllGranted() permissions[" + i + "]：" + permissions[i] + "授权未成功");
                    isAllGranted = false;
                    break;
                }
            }
            if (!isAllGranted) {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                Toast.makeText(mContext, "权限请求异常可能导致不封功能不可使用", Toast.LENGTH_SHORT).show();
                return;
            }
            return;

        }
    }

    // 初始化界面
    private void init() {
        mContext = getApplicationContext();
        header = (LinearLayout) findViewById(R.id.ll_header_mainactivity);
        llSetPrinter = (LinearLayout) findViewById(R.id.ll_setting);
        llLablePrint = (LinearLayout) findViewById(R.id.ll_lable_print);
        llTextPrint = (LinearLayout) findViewById(R.id.ll_text_print);
        llPDFPrint = (LinearLayout) findViewById(R.id.ll_pdf_print);
        llBarcodePrint = (LinearLayout) findViewById(R.id.ll_barcode_print);
        llImagePrint = (LinearLayout) findViewById(R.id.ll_image_print);
        llCollectPrint = (LinearLayout) findViewById(R.id.ll_collect_print);
        llUpdate = (LinearLayout) findViewById(R.id.ll_update);
        imgRedpoint = (ImageView) findViewById(R.id.img_redpoint);
        // 设置点击监听事件
        llSetPrinter.setOnClickListener(this);
        llLablePrint.setOnClickListener(this);
        llTextPrint.setOnClickListener(this);
        llPDFPrint.setOnClickListener(this);
        llBarcodePrint.setOnClickListener(this);
        llImagePrint.setOnClickListener(this);
        llCollectPrint.setOnClickListener(this);
        llUpdate.setOnClickListener(this);
        ChangeUpdateStatus(0);
        initHeader();
    }

    /**
     * 升级状态的显示
     *
     * @param flag 0表示没有新版本，1表示有新版本
     */
    private void ChangeUpdateStatus(int flag) {
        if (flag == 1) {
            imgRedpoint.setVisibility(View.VISIBLE);
            return;
        } else {
            imgRedpoint.setVisibility(View.GONE);
            return;
        }
    }

    // 点击监听事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_setting:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                // intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                List<InputMethodInfo> mInputMethodProperties = imm.getInputMethodList();
//                for (int i = 0; i < mInputMethodProperties.size(); i++) {
//                   XLog.e("yxz",i + "、"
//                            + mInputMethodProperties.get(i).getServiceName() + " , id:"
//                            + mInputMethodProperties.get(i).getId());
//                }

//                PackageManager pckMan = mContext.getPackageManager();
//                List<PackageInfo> packageInfo = pckMan.getInstalledPackages(0);
//                for (PackageInfo pInfo : packageInfo) {
//                    if("打印工具V1.07".equals(pInfo.applicationInfo.loadLabel(pckMan).toString())){
//                        Log.e(TAG,"\n\nappName:"+pInfo.applicationInfo.loadLabel(pckMan).toString());
//                        Log.e(TAG,"packageName:"+pInfo.packageName);
//                        Log.e(TAG,"versionCode:"+pInfo.versionCode);
//                        Log.e(TAG,"versionName:"+pInfo.versionName);
//                    }
//                }

                break;
            case R.id.ll_lable_print:
                Intent intent_lable = new Intent(MainActivity.this, LablePrintActivity.class);
                startActivity(intent_lable);
                break;
            case R.id.ll_text_print:
                Intent intent_text = new Intent(MainActivity.this, TextPrintActivity.class);
                startActivity(intent_text);
                break;
            case R.id.ll_pdf_print:
                Intent intent2 = new Intent(MainActivity.this, PdfPrintActivity.class);
                startActivity(intent2);
                break;
            case R.id.ll_barcode_print:
                Intent intent_barcode = new Intent(MainActivity.this, BarcoePrintActivity.class);
                startActivity(intent_barcode);
                break;
            case R.id.ll_image_print:
                Intent intent_image = new Intent(MainActivity.this, PicturePrintActivity.class);
                startActivity(intent_image);
                break;
            case R.id.ll_collect_print:
                Intent intent_collect_print = new Intent(MainActivity.this, CollectPrintActivity.class);
                startActivity(intent_collect_print);
                break;
            case R.id.ll_update:
                //APP在线升级
                if (GlobalContants.NEED_UPDATE == 1) {
                    showUpdateDialog();
                } else {
                    Toast.makeText(mContext, getString(R.string.current_app_version) + UpdateUtils.getVersionName(mContext), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 初始化标题上的信息
     */
    private void initHeader() {
        setHeaderLeftText(header, "", null);
        headerConnecedState.setText(getTitleState());
    }

    @Override
    protected void onStart() {
        super.onStart();
        initHeader();
    }

    private static BroadcastReceiver mUsbAttachReceiver = new BroadcastReceiver() {
        @SuppressLint("NewApi")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.w("fdh", "receiver action: " + action);

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Toast.makeText(mContext, "USB设备已接入", 0).show();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Toast.makeText(mContext, "USB设备已拔出", 0).show();
                if (PrinterInstance.mPrinter != null && SettingActivity.isConnected) {
                    PrinterInstance.mPrinter.closeConnection();
                    PrinterInstance.mPrinter = null;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mUsbAttachReceiver);
    }

    // @Override
    // protected void onStart() {
    // super.onStart();
    // initHeader();
    // }

    /**
     * 检查是否有新版本，如果有就升级
     */
    private void checkUpdate() {
        new Thread() {
            public void run() {
                Message mes = Message.obtain();
                try {
                    URL url = new URL(getString(R.string.serverurl));
                    // 联网
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(4000);
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        // 请求成功
                        InputStream is = conn.getInputStream();
                        // 把流转成String
                        String result = UpdateUtils.readFromStream(is);
                        XLog.d(TAG, "联网成功了" + result);
                        // json解析
                        JSONObject obj = new JSONObject(result);
                        // 得到服务器的版本信息
                        String version = (String) obj.get("version");
                        if (UpdateUtils.isZh(mContext))
                            GlobalContants.description = (String) obj.get("description");
                        else
                            GlobalContants.description = (String) obj.get("descriptionE");
                        GlobalContants.apkurl = (String) obj.get("apkurl");
                        XLog.d("yxz", "apkurl:" + GlobalContants.apkurl);
                        // 校验是否有新版本
                        if (UpdateUtils.getVersionName(mContext).equals(version)) {
                            // 版本一致，没有新版本,设置 GlobalContants.NEED_UPDATE=0;
                            mes.what = GlobalContants.NO_NEW_VERSION;
                        } else {
                            // 有新版本,设置 GlobalContants.NEED_UPDATE=1;
                            mes.what = GlobalContants.HAS_NEW_VERSION;
                        }
                    }

                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    mes.what = GlobalContants.URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    mes.what = GlobalContants.NETWORK_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    mes.what = GlobalContants.JSON_ERROR;
                } finally {
                    updateHandler.sendMessage(mes);
                }
            }
        }.start();

    }


    private class UpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GlobalContants.HAS_NEW_VERSION:// 有新版本更新
                    GlobalContants.NEED_UPDATE = 1;
                    Toast.makeText(getApplicationContext(), R.string.start_checkupdate_has_new_version, Toast.LENGTH_LONG).show();
                    break;
                case GlobalContants.NO_NEW_VERSION:// 无新版本更新
                    GlobalContants.NEED_UPDATE = 0;
                    break;
                case GlobalContants.URL_ERROR:// URL错误
                    GlobalContants.NEED_UPDATE = 0;
                    Toast.makeText(getApplicationContext(), R.string.start_checkupdate_url_error, Toast.LENGTH_LONG).show();
                    break;
                case GlobalContants.NETWORK_ERROR:// 网络异常
                    GlobalContants.NEED_UPDATE = 0;
                    Toast.makeText(getApplicationContext(), R.string.start_checkupdate_net_error, Toast.LENGTH_LONG).show();
                    break;

                case GlobalContants.JSON_ERROR:// JSON解析出错
                    GlobalContants.NEED_UPDATE = 0;
                    Toast.makeText(getApplicationContext(), R.string.start_checkupdate_json_error, Toast.LENGTH_LONG).show();
                    break;
//                D:\webapps\sprtwork
//                D:\qzfeng_20160325\apache-tomcat-6.0.37-windows-x86\apache-tomcat-6.0.37\bin
            }
            ChangeUpdateStatus(GlobalContants.NEED_UPDATE);
        }
    }

    /**
     * 弹出升级对话框
     */
    protected void showUpdateDialog() {
        AllenVersionChecker
                .getInstance()
                .downloadOnly(
                        UIData.create().setTitle(getString(R.string.current_app_version) + UpdateUtils.getVersionName(mContext)).setContent(GlobalContants.description).setDownloadUrl(GlobalContants.apkurl)
                )
                .excuteMission(mContext);
    }
}
