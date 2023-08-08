package com.printer.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.printer.demo.global.GlobalContants;
import com.printer.demo.utils.PrefUtils;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.serial.SerialPortFinder;

public class SerialsDeviceList extends Activity implements RadioGroup.OnCheckedChangeListener {

    private Button scanButton;
    private Button backButton;
    private ArrayAdapter<String> deviceArrayAdapter;
    private ListView mFoundDevicesListView;
    //	private EditText baudrateEt;
    private String baudrateStr;
    private String path;
    private String[] entries;
    private String[] entryValues;
    private Spinner baudrateSpin;
    private RadioGroup rgLiuKong;
    private int liuKong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list_serial);
        setTitle("请选择设备");

        setResult(Activity.RESULT_CANCELED);

//		baudrateEt = (EditText) findViewById(R.id.editBaudrateText);
        baudrateSpin = (Spinner) findViewById(R.id.BaudrateSpin);

//		baudrateSpin.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//				
//				
//			}
//		});
        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
            }
        });

        backButton = (Button) findViewById(R.id.button_bace);
        backButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });

        deviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_item);

        mFoundDevicesListView = (ListView) findViewById(R.id.paired_devices);
        mFoundDevicesListView.setAdapter(deviceArrayAdapter);
        mFoundDevicesListView.setOnItemClickListener(mDeviceClickListener);
        rgLiuKong = (RadioGroup) findViewById(R.id.rg_liukong);
        rgLiuKong.setOnCheckedChangeListener(this);
        switch (PrefUtils.getInt(SerialsDeviceList.this, GlobalContants.LiuKongType, 1)) {
            case 0:
                rgLiuKong.check(R.id.rb_xon_xoff);
                break;
            case 1:
                rgLiuKong.check(R.id.rb_rts_cts);
                break;
            case 2:
                rgLiuKong.check(R.id.rb_none);
                break;
            default:
                rgLiuKong.check(R.id.rb_rts_cts);
                break;
        }
        doDiscovery();
    }

    public void doDiscovery() {
        if (!deviceArrayAdapter.isEmpty()) {
            deviceArrayAdapter.clear();
        }
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        entries = mSerialPortFinder.getAllDevices();
        if (entries == null || entries.length == 0) {
            deviceArrayAdapter.add(getString(R.string.notroot));
            Log.i("sprt", "entries.length为空！");
        }
        entryValues = mSerialPortFinder.getAllDevicesPath();
        for (int i = 0; i < entries.length; i++) {
            deviceArrayAdapter.add(entries[i] + "/npath:" + entryValues[i]);
        }
    }

    private void returnToPreviousActivity(String path, String baudrate) {
        Intent intent = new Intent();
        intent.putExtra("path", path);
        intent.putExtra("baudrate", baudrate);

        System.out.println("0path:" + path);
        System.out.println("0baudrate:" + baudrate);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            baudrateStr = baudrateSpin.getSelectedItem().toString();
            path = entryValues[position];
            returnToPreviousActivity(path, baudrateStr);
        }
    };

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == rgLiuKong) {
            switch (checkedId) {
                case R.id.rb_rts_cts:
                    liuKong = 1;
                    break;
                case R.id.rb_xon_xoff:
                    liuKong = 0;
                    break;
                case R.id.rb_none:
                    liuKong = 2;
                    break;
                default:
                    liuKong = 1;
                    break;
            }
            PrefUtils.setInt(SerialsDeviceList.this, GlobalContants.LiuKongType, liuKong);
        }
    }
}
