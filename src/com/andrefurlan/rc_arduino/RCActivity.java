package com.andrefurlan.rc_arduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class RCActivity extends Activity {

	TextView vSeekBarText;
	TextView hSeekBarText;
	VerticalSeekBar verticalSeekBar;
	SeekBar horizontalSeekBar;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rc);
		init();		
		setSeekBars();
		
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
								while (progress != 50) {
									try {
										sleep(6);
									} catch (InterruptedException e) {
										e.printStackTrace();
									} finally {
										if (progress > 50) {
											sb.setProgress(progress--);
										} else {
											sb.setProgress(progress++);
										}
									}
								}
								sb.setProgress(50);
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
						hSeekBarText.setText("" + (progress - 50));
					}
				});
	}

	private void init() {
		vSeekBarText = (TextView) findViewById(R.id.vSeekBarText);
		hSeekBarText = (TextView) findViewById(R.id.hSeekBarText);
		verticalSeekBar = (VerticalSeekBar) findViewById(R.id.verticalSeekBar);
		horizontalSeekBar = (SeekBar) findViewById(R.id.horizontalSeekBar);
		
	}
}
