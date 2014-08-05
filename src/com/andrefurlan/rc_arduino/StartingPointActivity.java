package com.andrefurlan.rc_arduino;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

//TODO fix duplicated devide discovered
//TODO second discovery is not working once the bluetooth is turned off

public class StartingPointActivity extends Activity implements OnItemClickListener {

	Button startButton;
	Button scanButton;
	Switch btSwitch;
	BluetoothAdapter btAdapter;
	ArrayAdapter<String> arrayAdapter;
	ListView devicesListView;
	boolean didITurnTheBtAdaptor;
	Set<BluetoothDevice> devicesSet;
	List<String> pairedDevices;
	IntentFilter btFilter;
	BroadcastReceiver btReceiver;
	private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_starting_point);
		init();
		setButtonsListeners();
		checkBtEnable();
		setBtSwitchListener();
		getPairedDevices();
	}

	private void startDiscovery() {
		// TODO Auto-generated method stub
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		btAdapter.cancelDiscovery();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(btReceiver);
		if (didITurnTheBtAdaptor)
			btAdapter.disable();
	}

	private void getPairedDevices() {
		devicesSet = btAdapter.getBondedDevices();
		if (devicesSet.size() > 0) {
			for (BluetoothDevice d : devicesSet) {
				arrayAdapter.add(d.getName()+ " (paired)\n" + d.getAddress());
			}
		}

	}

	private void setBtSwitchListener() {
		btSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				btAdapter.cancelDiscovery();
				if (isChecked) {
					Intent btIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(btIntent, 1);
					// btAdapter.enable();
					didITurnTheBtAdaptor = true;
				} else {
					btAdapter.disable();
					btAdapter.cancelDiscovery();
					arrayAdapter.clear();
				}
			}
		});
	}

	private void checkBtEnable() {
		if (btAdapter == null) {
			Toast.makeText(getApplicationContext(), "No bluetooth detected",
					Toast.LENGTH_SHORT).show();
			finish();
		} else {
			if (btAdapter.isEnabled()) {
				btSwitch.toggle();
				
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(),
					"You can only use the RC with bluetooth enabled",
					Toast.LENGTH_SHORT).show();
		}
		if(resultCode == RESULT_OK) {
			getPairedDevices();
		}
	}

	private void init() {
		didITurnTheBtAdaptor = false;
		startButton = (Button) findViewById(R.id.start_button);
		scanButton = (Button) findViewById(R.id.scan_button);
		btSwitch = (Switch) findViewById(R.id.bt_switch);
		pairedDevices = new ArrayList<String>();
		devicesListView = (ListView) findViewById(R.id.btDevicesList);
		devicesListView.setOnItemClickListener(this);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, 0);
		devicesListView.setAdapter(arrayAdapter);
		btFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		setBtReceiver();
	}

	private void setBtReceiver() {
		btReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (BluetoothDevice.ACTION_FOUND.equals(action)){
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					//add found devices in list
					arrayAdapter.add(device.getName() + "\n" + device.getAddress());
					devicesListView.setAdapter(arrayAdapter);
				}else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
					Toast.makeText(getApplicationContext(), "Discovery started",Toast.LENGTH_SHORT).show();
				}else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
					Toast.makeText(getApplicationContext(), "Discovery finished",Toast.LENGTH_SHORT).show();
				}else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
					if(btAdapter.getState() == btAdapter.STATE_OFF){
						//do something here in case the bluetooth is turned off
					}
				}
				
			}
		};
		registerReceiver(btReceiver, btFilter);
		btFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(btReceiver, btFilter);
		btFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(btReceiver, btFilter);
		btFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(btReceiver, btFilter);
	}

	private void setButtonsListeners() {
		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent startRCView = new Intent("android.intent.action.RC");
				startActivity(startRCView);

			}
		});
		
		scanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startDiscovery();
				btAdapter.cancelDiscovery();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.starting_point, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		btAdapter.cancelDiscovery();
		if(arrayAdapter.getItem(position).contains("Paired")){
			BluetoothDevice selectedDevice = (BluetoothDevice) devicesSet.toArray()[position];
			ConnectThread connect = new ConnectThread(selectedDevice);
			connect.start();
		}
			
	}
	
	private class ConnectThread extends Thread {
		
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	        btAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        manageConnectedSocket(mmSocket);
	    }
	 
	    private void manageConnectedSocket(BluetoothSocket mmSocket2) {
			// TODO Auto-generated method stub
			
		}

		/** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
}
