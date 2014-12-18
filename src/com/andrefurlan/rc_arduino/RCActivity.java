package com.andrefurlan.rc_arduino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class RCActivity extends Activity {

	protected static final int SUCCESS_CONNECT = 0;
	protected static final int READ_MESSAGE = 1;
	private static Handler mHandler;
	private ConnectedThread connectedThread;
	private TextView vSeekBarText;
	private TextView hSeekBarText;
	private VerticalSeekBar verticalSeekBar;
	private SeekBar horizontalSeekBar;
	private String tag = "debugging";
	private byte lastVValue;
	private byte lastHValue;
	private ToggleButton reverseButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rc);
		init();
		setSeekBars();
		if (StartingPointActivity.connectedSocket != null){
			connect();
			byte code = (byte) 76;
			byte[] output = new byte[1];
			output[0] = code;
			connectedThread.write(output);
		}
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		byte code = (byte) 75;
		byte[] output = new byte[1];
		output[0] = code;
		connectedThread.write(output);
		connectedThread.interrupt();
		
	}

	private void connect() {
		connectedThread = new ConnectedThread(
				StartingPointActivity.connectedSocket);
		connectedThread.start();
	}

	private void setSeekBars() {
		vSeekBarText.setTextSize(48);
		hSeekBarText.setTextSize(48);
		verticalSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						vSeekBarText.setText("done");
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						vSeekBarText.setText("" + progress);
						byte code = (byte) 77;
						byte p = (byte) progress;
						byte[] output = new byte[2];
						output[0] = code;
						output[1] = p;
						connectedThread.write(output);
						
					}
				});

		horizontalSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						final SeekBar sb = seekBar;
						Thread timer = new Thread() {
							public void run() {
								int progress = sb.getProgress();
								while (progress != 32) {
									try {
										sleep(6);
									} catch (InterruptedException e) {
										e.printStackTrace();
									} finally {
										if (progress > 32) {
											sb.setProgress(progress--);
										} else {
											sb.setProgress(progress++);
										}
									}
								}
								sb.setProgress(32);

							}
						};

						timer.start();

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						hSeekBarText.setText("" + (progress -32));
						
						byte code = (byte) 83;
						
						byte p = (byte) progress;
						if( Math.abs(p - lastVValue) > 3){
							byte[] output = new byte[2];
							output[0] = code;
							output[1] = p;
							connectedThread.write(output);
							lastVValue = p;
						}
						
					}
				});
	}

	private void init() {
		vSeekBarText = (TextView) findViewById(R.id.vSeekBarText);
		hSeekBarText = (TextView) findViewById(R.id.hSeekBarText);
		verticalSeekBar = (VerticalSeekBar) findViewById(R.id.verticalSeekBar);
		horizontalSeekBar = (SeekBar) findViewById(R.id.horizontalSeekBar);
		mHandler = new HandlerExtension();
		reverseButton = (ToggleButton) findViewById(R.id.reverse_button);
		
		reverseButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				byte code = (byte) 82;
			
					byte[] output = new byte[1];
					output[0] = code;
					connectedThread.write(output);
			}
		});
	
				
			}
	

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private String tag = "debugging";

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
					
					int s = bytesToShort(buffer);
					
					Log.i(tag, bytes + " # of bytes");
					for (int i = 0; i < bytes; i++)
						Log.i(tag, buffer[i] + " from read");

//					mHandler.obtainMessage(READ_MESSAGE, bytes, -1, buffer)
//							.sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] b) {
			try {
				mmOutStream.write(b);
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

	private final class HandlerExtension extends Handler {
		@Override
		public void handleMessage(Message msg) {

			Log.i(tag, "in handler");
			super.handleMessage(msg);
			switch (msg.what) {
			case READ_MESSAGE:
//				Toast.makeText(getApplicationContext(), "Connection failed",
//						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}
	
	public int bytesToInteger(byte[] bytesArray) {
		return bytesArray[0] << 24 | (bytesArray[1]) << 16
				| (bytesArray[2]) << 8 | (bytesArray[3]);
	}
	public byte[] integerToBytes(int i) {
		byte[] result = new byte[4];
		result[0] = (byte) ((i >>> 24) & 0xFF);
		result[1] = (byte) ((i >>> 16) & 0xFF);
		result[2] = (byte) ((i >>> 8) & 0xFF);
		result[3] = (byte) (i & 0xFF);
		return result;
	}
	
	public short bytesToShort(byte[] bytesArray) {
		return (short) ((bytesArray[1]) << 8 | (bytesArray[0] & 0xFF));
	}
	public byte[] shortToBytes(short i) {
		byte[] result = new byte[2];
		result[1] = (byte) (i >> 8);
		result[0] = (byte) (i & 0xFF);
		return result;
	}

}
