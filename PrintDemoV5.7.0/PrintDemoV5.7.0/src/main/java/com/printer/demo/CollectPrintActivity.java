package com.printer.demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.printer.sdk.Barcode;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.listener.OnPrintListener;
import com.printer.sdk.utils.IOUtils;
import com.printer.sdk.utils.Utils;
import com.printer.sdk.utils.XLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @Description 处理图片打印相关
 */
public class CollectPrintActivity extends BaseActivity implements OnCheckedChangeListener {
    protected static final String TAG = "yxz";
    private static Context mContext;
    private LinearLayout header;
    private RadioGroup rg_is_notifacation, rg_is_service_setable;
    private RadioButton rbYes, reNo, rbAllow, rbNotAllow;
    private PrinterInstance myPrinter;
    private TextView tvCollectData;
    private boolean isOpen;
    private TextView testPrint, testNotPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_collect_print);
        init();
        XLog.d("yxz", "oncreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        XLog.d("yxz", "onresume");
        if (!isOpen)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (myPrinter.getSetable()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rbAllow.setChecked(true);
                            // rg_is_service_setable.check(R.id.rb_allow);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rbNotAllow.setChecked(true);
                            // rg_is_service_setable.check(R.id.rb_not_allow);
                        }
                    });
                }
            }
        }).start();

    }

    private void init() {

        header = (LinearLayout) findViewById(R.id.ll_header_picture);
        rg_is_notifacation = (RadioGroup) findViewById(R.id.rg_is_notifacation);
        rbYes = (RadioButton) findViewById(R.id.yes);
        reNo = (RadioButton) findViewById(R.id.no);
        rg_is_notifacation.setOnCheckedChangeListener(this);
        rg_is_service_setable = (RadioGroup) findViewById(R.id.rg_is_service_setable);
        rbAllow = (RadioButton) findViewById(R.id.rb_allow);
        rbNotAllow = (RadioButton) findViewById(R.id.rb_not_allow);
        rg_is_service_setable.setOnCheckedChangeListener(this);
        tvCollectData = (TextView) findViewById(R.id.et_collect_data);
        // 设置文本可以滚动
        tvCollectData.setMovementMethod(ScrollingMovementMethod.getInstance());
        testPrint = (TextView) findViewById(R.id.tv_show_msg);
        testNotPrint = (TextView) findViewById(R.id.tv_show_is_notifacation);
        testPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // myPrinter.setPrintable(true);
                // myPrinter.setCutCommand("1d5555");
                myPrinter.setAppendAble(false);
                XLog.d("yxz","setAppendAble :"+false);

            }
        });
        testNotPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                myPrinter.setPrintable(false);
//                XLog.d("yxz", "setPrintable str:" + false);
                //String str= myPrinter.getCutCommand();
                //XLog.d("yxz","cutcommand str:"+str);
//                boolean isNewApk = myPrinter.isNewApk();
//                XLog.d("yxz", "isNewApk str:" + isNewApk);
//                boolean isAppendAble=myPrinter.getAppendAble();
//                XLog.d("yxz", "isAppendAble str:" + isAppendAble);
                myPrinter.setAppendAble(true);
                XLog.d("yxz","setAppendAble :"+true);
//                boolean isAppendAble2=myPrinter.getAppendAble();
//                XLog.d("yxz", "isAppendAble str2:" + isAppendAble2);

            }
        });
        initHeader(header);
        // 打开服务远程端口
        open();
    }

    /**
     * 初始化标题上的信息
     */
    private void initHeader(LinearLayout header) {
        setHeaderLeftText(header, getString(R.string.back), new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
        headerConnecedState.setText(getTitleState());
        setHeaderLeftImage(header, new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setHeaderCenterText(header, getString(R.string.headview_CollectPrint));
        setTitleState("");
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == rg_is_notifacation) {
            switch (checkedId) {
                case R.id.yes:
                    collect(true);
                    Toast.makeText(mContext, "等待数据上传后执行追加动作", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.no:
                    collect(false);
                    Toast.makeText(mContext, "追加并采集功能关闭", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
//		if (group == rg_is_service_setable) {
//			switch (checkedId) {
//			case R.id.rb_allow:
//				isServiceSetable(true);
//				Toast.makeText(mContext, "打开服务端配置", Toast.LENGTH_SHORT).show();
//				break;
//			case R.id.rb_not_allow:
//				isServiceSetable(false);
//				Toast.makeText(mContext, "关闭服务端配置", Toast.LENGTH_SHORT).show();
//				break;
//			}
//		}
    }

    private boolean open() {
        if (isOpen)
            return true;
        myPrinter = PrinterInstance.getPrinterInstance();
        return isOpen = myPrinter.openConnection();

    }

    public void isServiceSetable(boolean isSetable) {
        if (!isOpen) {
            if (isSetable)
                Toast.makeText(mContext, "开口失败，无法设置服务端为可配置", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext, "开口失败，无法设置服务端为不可配置", Toast.LENGTH_SHORT).show();
            return;
        }
        myPrinter.isSetAble(isSetable);
    }

    public void collect(boolean isCollect) {
        if (!isOpen) {
            Toast.makeText(mContext, "开口失败，无法追加或采集", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isCollect) {
            myPrinter.setOnPrintListener(null);
            return;
        }
        myPrinter.setOnPrintListener(new OnPrintListener() {
            @Override
            public int doBeforePrint() {
                XLog.i(TAG, "客户端 打印开始前打印开头,发送广播给Service端。");
//                try {
//                    int writeLen = myPrinter.sendBytesData("*************一单数据开始**************\r\n".getBytes("gbk"));
//                    XLog.i(TAG, "客户端 打印前发送的数据长度writeLen:" + writeLen);
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCollectData.setText("采集数据开始\r\n");

                    }
                });
                return 1;
            }

            @Override
            public int doAfterPrint() {
                XLog.i(TAG, "客户端 打印结束后追加数据尾，发送广播给Service端。");
//				try {

//					myPrinter.drawQrCode(0,"http://i.baiwang.com/f?d=100232916965&c=100232916965346972876963840", PrinterConstants.PRotate.Rotate_0,3,6);
//					myPrinter.drawQrCode(1,"http://i.baiwang.com/f?d=100232916965&c=100232916965346972876963840", PrinterConstants.PRotate.Rotate_0,3,6);
//					myPrinter.drawQrCode(2,"http://i.baiwang.com/f?d=100232916965&c=100232916965346972876963840", PrinterConstants.PRotate.Rotate_0,3,6);
//					int writeLen = myPrinter.sendBytesData("*************一单数据结束**************\r\n".getBytes("gbk"));
//					byte[]data=getBytes();
//					int writeLen =myPrinter.sendBytesData(getBytes());
//					IOUtils.writeStrToSD(mContext,"异常数据：" );
//					IOUtils.writeStrToSD(mContext,Utils.bytesToHexString(data,data.length) );
//					myPrinter.sendBytesData("\r\n".getBytes("gbk"));
//					XLog.i(TAG, "客户端 打印后发送的数据长度writeLen:" + writeLen);
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCollectData.append("\r\n采集数据结束");
                    }
                });
                return 1;
            }

            @Override
            public int onReceiveParserData(int type, final byte[] data) {
                if (data == null || data.length == 0) {
                    XLog.i(TAG, "客户端 上传的采集数据为空");
                    return 1;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        XLog.i(TAG, "客户端 上传的采集数据大小小于1M");
                        tvCollectData.append(Utils.bytesToHexString(data, data.length));
                    }
                });
                return 1;
            }

            @Override
            public int onReceiveParserData(int i, final byte[] bytes, final int i1) {
                if (bytes == null || bytes.length == 0) {
                    XLog.i(TAG, "客户端 上传的采集数据为空");
                    return 1;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        XLog.i(TAG, "客户端 上传的采集数据大小大于1M,分包传送。解析数据总长度：" + i1);
                        tvCollectData.append(Utils.bytesToHexString(bytes, bytes.length));
                        tempLength += bytes.length;
                        if (tempLength == i1) {
                            PrinterInstance.getPrinterInstance().endAidl();
                            XLog.i(TAG, "客户端 上传的采集数据大小大于1M,采集完成，服务端" );
                        }
                    }
                });
                return 1;
            }

            @Override
            public int onReceiveParserData(int i, final List<byte[]> list) {

                if (list == null || list.size() == 0) {
                    XLog.i(TAG, "客户端 上传的采集数据为空");
                    return 1;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        XLog.i(TAG, "客户端 上传的采集数据大小大于1M");
                        tvCollectData.append(Utils.bytesToHexString(list.get(0), list.get(0).length));
                        tvCollectData.append("list.size:" + list.size());
                    }
                });
                return 1;
            }
        });

    }

    public int tempLength = 0;

    private void close() {
        if (!isOpen)
            return;
        myPrinter.closeConnection();
        isOpen = false;
    }

    private void closeAndRelease() {
        close();
        myPrinter.setOnPrintListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeAndRelease();
    }

    public static byte[] getBytes() {
        ByteArrayOutputStream out = null;
        try {
            AssetManager manager = mContext.getAssets();
            InputStream is = manager.open("test.txt");

            out = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = is.read(b)) != -1) {
                out.write(b, 0, i);
            }
            out.close();
            is.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] s = out.toByteArray();
        return s;
    }

}
