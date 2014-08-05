package com.andrefurlan.rc_arduino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
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

public class StartingPointActivity extends Activity implements
		OnItemClickListener {

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
	BluetoothSocket mBtSocket;
	ConnectedThread connectedThread;

	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// private static final UUID MY_UUID =
	// UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	String tag = "debugging";
	private static Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_starting_point);
		init();
		setButtonsListeners();
		checkBtEnable();
		setBtSwitchListener();
		getPairedDevices();
		mHandler = new HandlerExtension();
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
				arrayAdapter.add(d.getName() + " (paired)\n" + d.getAddress());
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
		if (resultCode == RESULT_OK) {
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
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// add found devices in list
					arrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
					devicesListView.setAdapter(arrayAdapter);
				} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
						.equals(action)) {
					Toast.makeText(getApplicationContext(),
							"Discovery started", Toast.LENGTH_SHORT).show();
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
						.equals(action)) {
					Toast.makeText(getApplicationContext(),
							"Discovery finished", Toast.LENGTH_SHORT).show();
				} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
					if (btAdapter.getState() == btAdapter.STATE_OFF) {
						// do something here in case the bluetooth is turned off
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
				// Intent startRCView = new Intent("android.intent.action.RC");
				// startActivity(startRCView);
				int a = 6;
				ByteBuffer bb = ByteBuffer.allocate(4);
				bb.putInt(a);
				connectedThread.write(bb.array());
			}
		});

		scanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				startDiscovery();
//				btAdapter.cancelDiscovery();
				
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
		if (arrayAdapter.getItem(position).contains("paired")) {
			BluetoothDevice selectedDevice = (BluetoothDevice) devicesSet
					.toArray()[position];
			ConnectThread connect = new ConnectThread(selectedDevice);
			Toast.makeText(getApplicationContext(), selectedDevice.getName(),
					Toast.LENGTH_SHORT).show();
			connect.start();

		}

	}

	private final class HandlerExtension extends Handler {
		@Override
		public void handleMessage(Message msg) {

			Log.i(tag, "in handler");
			super.handleMessage(msg);
			switch (msg.what) {
			case SUCCESS_CONNECT:
				// DO something
				connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
				connectedThread.start();
				Toast.makeText(getApplicationContext(), "Connected",
						Toast.LENGTH_SHORT).show();
				String s = "successfully connected";
				connectedThread.write(s.getBytes());
				Log.i(tag, "connected");
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				Toast.makeText(getApplicationContext(), "oh well",
						Toast.LENGTH_SHORT).show();
				String string = new String(readBuf);
				Toast.makeText(getApplicationContext(), string,
						Toast.LENGTH_SHORT).show();
				break;
			}
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
			Log.i(tag, "construct");
			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.i(tag, "get socket failed");
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			btAdapter.cancelDiscovery();
			Log.i(tag, "connect - run");
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
				mBtSocket = mmSocket;

				Log.i(tag, "connect - succeeded");
			} catch (IOException connectException) {
				Log.i(tag, "connect failed");
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
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
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI activity
					String s = new String(buffer);
					Log.i(tag, s + "\nfrom read");
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();

				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				String s = new String(bytes);
				Log.i(tag, s + "\nfrom write");
				mmOutStream.write(bytes);
			} catch (IOException e) {
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}
}
