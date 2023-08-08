package com.printer.demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.printer.sdk.utils.XLog;

import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothDeviceList extends Activity {
    private static final String TAG = "DeviceListActivity";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_RE_PAIR = "re_pair";
    public static String EXTRA_DEVICE_NAME = "device_name";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ListView pairedListView;
    private Button scanButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        setTitle(R.string.select_device);
        setResult(Activity.RESULT_CANCELED);
        initView();
    }

    private void initView() {
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_item);
        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + " ( " + getResources().getText(R.string.has_paired)
                        + " )" + "\n" + device.getAddress());
            }
        }

        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
				doDiscovery();
				v.setEnabled(false);
            }
        });

        backButton = (Button) findViewById(R.id.button_bace);
        backButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });

    }

    @Override
    protected void onStop() {
        if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        super.onResume();
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // If we're already discovering, stop it
        // if (mBtAdapter.isDiscovering()) {
        // mBtAdapter.cancelDiscovery();
        // }

        mPairedDevicesArrayAdapter.clear();
        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    private void returnToPreviousActivity(String address, boolean re_pair, String name) {
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
        intent.putExtra(EXTRA_RE_PAIR, re_pair);
        intent.putExtra(EXTRA_DEVICE_NAME, name);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            String name = info.substring(0, info.length() - 17);
            returnToPreviousActivity(address, false, name);
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int deviceType = device.getBluetoothClass().getMajorDeviceClass();
                if(((deviceType==1536)||(deviceType==7936))&&(device.getType()!=2)){
                    String itemName = device.getName() + " ( "
                            + getResources().getText(device.getBondState() == BluetoothDevice.BOND_BONDED
                            ? R.string.has_paired : R.string.not_paired)
                            + " )" + "\n" + device.getAddress();

                    mPairedDevicesArrayAdapter.remove(itemName);
                    mPairedDevicesArrayAdapter.add(itemName);
                    pairedListView.setEnabled(true);
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mPairedDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mPairedDevicesArrayAdapter.add(noDevices);
                    pairedListView.setEnabled(false);
                }
                scanButton.setEnabled(true);

            }
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        XLog.d(TAG, "----------bounding......,mac" + device.getAddress() + "----------");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        XLog.d(TAG, "----------bound success, mac:" + device.getAddress() + "----------");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        XLog.e(TAG, "----------bound cancel, mac:" + device.getAddress() + "----------");
                        Toast.makeText(BluetoothDeviceList.this,"设备："+device.getAddress()+"已解除配对",Toast.LENGTH_SHORT).show();;
                        break;
                    default:
                        break;
                }
            }
        }
    };

    //与设备解除配对
    static public boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

}
