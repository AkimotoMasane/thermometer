package com.akimoto.sensor;


import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import com.akimoto.sensor.R;
import com.akimoto.sensor.meter.Gauge;
import com.akimoto.sensor.pen.BarPen;
import com.akimoto.sensor.pen.CirclePen;
import com.akimoto.sensor.pen.LinePen;
import com.akimoto.sensor.pen.Pen;
import com.akimoto.sensor.record.BarPosition;
import com.akimoto.sensor.record.Temperature;
import com.akimoto.sensor.record.TemperatureRecord;
import com.akimoto.sensor.sound.SoundEffect;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;


public class MainActivity extends Activity {

	//�@���N�G�X�g�R�[�h
	private final int REQUEST_ENABLE_BT = 1;

	// ���x�̑���͈�
	private static final float LOWEST_TEMPERATURE = -40.0f;
	private static final float HIGHEST_TEMPERATURE = 125.0f;
	private static final float RESOLUTION_TEMPERATURE = 0.5f;

	// ���R�[�h��
	private static final int RECORD_CAPACITY = 30;
	
	private BluetoothAdapter bluethoothAdapter;
	
	private BluetoothLeScanner scanner;

	private ScanCallback scanCallback;

	private Calendar calendar;

	private TemperatureRecord record;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		this.initScanCallback();
		
		// BLE�ɑΉ����Ă��邩�m�F����
		if (true == getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

			BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
			
			this.bluethoothAdapter = bluetoothManager.getAdapter();
	
			// �u���[�g�D�[�X�ɑΉ����Ă��Ȃ��Ƃ�
			if (null == this.bluethoothAdapter) {
				
				// �A�v���P�[�V�������I������
				this.finish();

			} else {

				this.calendar = new GregorianCalendar();

				this.record = new TemperatureRecord(MainActivity.RECORD_CAPACITY);

				// �u���[�g�D�[�X�������̂Ƃ�
				if (false == this.bluethoothAdapter.isEnabled()) {
	
					// �u���[�g�D�[�X�̗L���m�F������
					Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					
					this.startActivityForResult(intent, this.REQUEST_ENABLE_BT);

				} else {
				
					this.scanner = this.bluethoothAdapter.getBluetoothLeScanner();
				}
				
				SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView1);

				new MainSurfaceView(this, surfaceView);
			}

		} else {

			// �A�v���P�[�V�������I������
			this.finish();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	protected void onResume() {

		super.onResume();

		// �u���[�g�D�[�X�������̂Ƃ�
		if (false == this.bluethoothAdapter.isEnabled()) {

			// �u���[�g�D�[�X�̗L���m�F������
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			
			this.startActivityForResult(intent, this.REQUEST_ENABLE_BT);

		} else {

			if (null != this.bluethoothAdapter) {

				if (null == this.scanner) {

					this.scanner = this.bluethoothAdapter.getBluetoothLeScanner();
				}

				if (null != this.scanner) {

					this.scanner.startScan(this.scanCallback);
				}
			}
		}
	}


	@Override
	protected void onPause() {

		if (null != this.bluethoothAdapter) {
			
			// �u���[�g�D�[�X���L���̂Ƃ�
			if (true == this.bluethoothAdapter.isEnabled()) {

				if (null != this.scanner) {

					this.scanner.stopScan(this.scanCallback);
				}

				// �u���[�g�D�[�X�𖳌��ɂ���
				this.bluethoothAdapter.disable();
			}
		}

		super.onPause();
	}


	@Override
	protected void onStop() {

		super.onStop();
	}


	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// settings���j���[���I�����ꂽ�Ƃ�
		if (id == R.id.action_setting) {

			Intent intent = new Intent(this, SensorSettingActivity.class);

			// �ݒ��ʂɑJ�ڂ���
			this.startActivity(intent);
			
			return true;
		}

		// about���j���[���I�����ꂽ�Ƃ�
		if (id == R.id.action_about) {

			try {

				AlertDialog.Builder dialog = new AlertDialog.Builder(this);

				// �_�C�A���O�ɕ\�����镶������擾����
				String title = getString(R.string.dialog_title);
				String button = getString(R.string.dialog_button);

				PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
				
				String version = packageInfo.versionName;

				String message = "Copyright Akimoto Masane.\n" + "All Rights Reserved.\n" + "Version : " + version;

				// �_�C�A���O��ʂ̐ݒ�
				dialog.setTitle(title);
				dialog.setMessage(message);
				dialog.setPositiveButton(button, null);

				// �_�C�A���O��\������
				dialog.show();
			}
			
			catch (NameNotFoundException exception) {
				
			}

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// �u���[�g�D�[�X�̗L�������N�G�X�g�̂Ƃ�
		if (this.REQUEST_ENABLE_BT == requestCode) {

			// ���������Ƃ�
			if (Activity.RESULT_OK == resultCode) {

				if (null != this.scanner) {

					this.scanner.startScan(this.scanCallback);
				}
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	
	private void initScanCallback() {

		this.scanCallback = new ScanCallback() {
			
			@Override
			public void onScanResult(int callbackType, ScanResult result) {
				
				super.onScanResult(callbackType, result);

				BluetoothDevice bluetoothDevice = result.getDevice();

				String name = bluetoothDevice.getName();
				
				if (null != name) {

					if (true == name.equals("BLECAST_TM")) {
					
						ScanRecord scanRecord = result.getScanRecord();
						
						byte[] pduPayload = scanRecord.getBytes();
						
						float sensor = (float)(pduPayload[9] & 0xFF);

						if ( (MainActivity.LOWEST_TEMPERATURE <= sensor) && (sensor <= MainActivity.HIGHEST_TEMPERATURE) ) {
						
							if (0 != (pduPayload[10] & 0x80)) {

								// ���x���v���X�̎�
								if (0 <= sensor) {

									// 0.5�x������
									sensor += MainActivity.RESOLUTION_TEMPERATURE;
									
								} else {	// ���x���}�C�i�X�̂Ƃ�

									// 0.5�x���炷
									sensor -= MainActivity.RESOLUTION_TEMPERATURE;
								}
							}
							
				    		int year = MainActivity.this.calendar.get(Calendar.YEAR);
				    		
				    		int month = MainActivity.this.calendar.get(Calendar.MONTH);
				    		
				    		int day = MainActivity.this.calendar.get(Calendar.DAY_OF_MONTH);
				    		
				    		int hour = MainActivity.this.calendar.get(Calendar.HOUR_OF_DAY);
				    		
				    		int minute = MainActivity.this.calendar.get(Calendar.MINUTE);
				    		
				    		int second = MainActivity.this.calendar.get(Calendar.SECOND);
							
							Temperature temperature = new Temperature(year, month, day, hour, minute, second, sensor);
							
							MainActivity.this.record.setTemperature(temperature);
		
							// ���݉��x���擾����
							Temperature current = MainActivity.this.record.getCurrentTemperature();
							
							TextView text = (TextView)findViewById(R.id.textView4);
		
							// ���݉��x��ݒ肷��
							text.setText(String.valueOf(current.getTemperature()));
							
							// �ō����x���擾����
							Temperature highest = MainActivity.this.record.getHighestTemperature();
							
							text = (TextView)findViewById(R.id.textView5);
		
							// �ō����x��ݒ肷��
							text.setText(String.valueOf(highest.getTemperature()));
							
							// �Œቷ�x���擾����
							Temperature lowest = MainActivity.this.record.getLowestTemperature();
							
							text = (TextView)findViewById(R.id.textView6);
		
							// �Œቷ�x��ݒ肷��
							text.setText(String.valueOf(lowest.getTemperature()));
						}

						TextView text = (TextView)findViewById(R.id.textView9);
						
						// �f�o�C�X����ݒ肷��
						text.setText(name);
						
						// RSSI���擾����
						int rssi = result.getRssi();
						
						text = (TextView)findViewById(R.id.textView10);
						
						// RSSI��ݒ肷��
						text.setText(String.valueOf(rssi));
					}
				}
			}
			
			@Override
			public void onScanFailed(int errorCode) {
				
				super.onScanFailed(errorCode);
			}
		};
	}


	private class MainSurfaceView implements SurfaceHolder.Callback, Runnable, OnTouchListener {

		// ���_�ۑ��p�t�@�C����
		private static final String FILE_NAME = "BarPosition";
		
		// �_�̔��a
		private static final float CIRCLE_RADIUS = 3.0f;

		// �O���t�̕`��Ԋu
		private static final int SLEEP_TIME = 500;

		private Activity activity;

		private SurfaceView surfaceView;

		private Thread thread;

		private boolean processing;

		private Gauge gauge;
		
		private SoundEffect soundEffect;
	
		private BarPosition barPosition;
		
		private boolean grabHighBar;
		private boolean grabLowBar;

		private Pen highbarPen;
		private Pen lowbarPen;
		

		public MainSurfaceView(Context context, SurfaceView surfaceView) {

			this.activity = (Activity)context;
			
			this.surfaceView = surfaceView;
			
			SurfaceHolder holder = this.surfaceView.getHolder();

			holder.addCallback(this);

			this.surfaceView.setOnTouchListener(this);
		}


		@Override
		public void surfaceCreated(SurfaceHolder holder) {

			// �ݒ�l���擾����
			SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this.activity);

			String strMax = preference.getString("upper", "125");

			int max = Integer.parseInt(strMax);
			
			String strMin = preference.getString("lower", "-40");
			
			int min = Integer.parseInt(strMin);

			// �ݒ�l��ǂݏo��
			this.load();
			
			if (null == this.barPosition) {

				this.barPosition = new BarPosition();
			}

			this.gauge = new Gauge(max, min, this.barPosition.getHigh(), this.barPosition.getLow());

			this.highbarPen = new BarPen(Style.FILL, Color.RED);

			this.lowbarPen = new BarPen(Style.FILL, Color.BLUE);

			this.processing = true;

			this.thread = new Thread(this);

			this.thread.start();
		}


		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

			// ��ʂ��쐬�����Ƃ�
			if (true == holder.isCreating()) {

				if (null == this.soundEffect) {
	
					this.soundEffect = new SoundEffect();
	
					this.soundEffect.loadSoundFile(this.activity);
				}
			}
		}


		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

			// �X���b�h�������I������
			this.processing = false;

			try {

				// �X���b�h�̏I����҂�
				this.thread.join(MainSurfaceView.SLEEP_TIME * 2);

				if (null != this.soundEffect) {

					// ���\�[�X���J������
					this.soundEffect.releaseSoundFile();

					this.soundEffect = null;
				}

				float high = this.gauge.getHighBarPosition();

				this.barPosition.setHigh(high);

				float low = this.gauge.getLowBarPosition();

				this.barPosition.setLow(low);

				// �ݒ�l��ۑ�����
				this.save();

				this.barPosition = null;

				this.gauge = null;

			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}


		@Override
		public boolean onTouch(View v, MotionEvent event) {

			int action = event.getAction();
			
			switch (action) {

			case MotionEvent.ACTION_DOWN:

				this.onActionDown(event.getX(), event.getY());

				break;

			case MotionEvent.ACTION_MOVE:
				
				this.onActionMove(event.getX(), event.getY());

				break;

			case MotionEvent.ACTION_UP:

				this.onActionUp(event.getX(), event.getY());

				break;

			default:
				break;
			}
			
			return true;
		}


		@Override
		public void run() {

			while (true == this.processing) {

				try {
				
					Thread.sleep(MainSurfaceView.SLEEP_TIME);

					// ���x��`�悷��
					this.doDraw();

					// ���x�x�����`�F�b�N
					this.monitoringTemperature();
				}
				
				catch (InterruptedException exception) {
				}
			}
		}


		private void onActionDown(float x, float y) {

			// �ݒ�l���擾����
			SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this.activity);

			boolean visible = preference.getBoolean("highbar", false);

			if (true == visible) {

				float scaleY = this.surfaceView.getHeight() / this.gauge.getMeasure();

				float temperature = this.gauge.getHighestLimit() - (y / scaleY);

				float current = this.gauge.getHighBarPosition();

				if ( ((current - 1.0f) < temperature) && (temperature < current + 1.0f) ) {

					this.grabHighBar = true;
					
					this.highbarPen = new BarPen(Style.STROKE, Color.RED);
					
					if (null != this.soundEffect) {

						this.soundEffect.playSoundEffectTouch();
					}
				}
			}

			// �����o�[���͂܂�Ă��Ȃ��Ƃ�
			if (true != this.grabHighBar) {

				visible = preference.getBoolean("lowbar", false);
				
				if (true == visible) {

					float scaleY = this.surfaceView.getHeight() / this.gauge.getMeasure();

					float temperature = this.gauge.getHighestLimit() - (y / scaleY);

					float current = this.gauge.getLowBarPosition();

					if ( ((current - 1.0f) < temperature) && (temperature < current + 1.0f) ) {

						this.grabLowBar = true;

						this.lowbarPen = new BarPen(Style.STROKE, Color.BLUE);

						if (null != this.soundEffect) {

							this.soundEffect.playSoundEffectTouch();
						}
					}
				}
			}
		}


		private void onActionMove(float x, float y) {

			if (true == this.grabHighBar) {

				float scaleY = this.surfaceView.getHeight() / this.gauge.getMeasure();

				float temperature = this.gauge.getHighestLimit() - (y / scaleY);

				this.gauge.setHighBarPosition(temperature);
			}

			if (true == this.grabLowBar) {

				float scaleY = this.surfaceView.getHeight() / this.gauge.getMeasure();

				float temperature = this.gauge.getHighestLimit() - (y / scaleY);

				this.gauge.setLowBarPosition(temperature);
			}
		}


		private void onActionUp(float x, float y) {

			if (true == this.grabHighBar) {

				this.highbarPen = new BarPen(Style.FILL, Color.RED);
				
				this.grabHighBar = false;
			}

			if (true == this.grabLowBar) {

				this.lowbarPen = new BarPen(Style.FILL, Color.BLUE);

				this.grabLowBar = false;
			}
		}


		private void doDraw() {

			SurfaceHolder holder = this.surfaceView.getHolder();

			Canvas canvas = holder.lockCanvas();

			if (null != canvas) {

				// �w�i�𔒂œh��ׂ�
				canvas.drawColor(Color.WHITE);

				// �ݒ�l���擾����
				SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this.activity);

				String strMax = preference.getString("upper", "125");
				
				float max = Float.parseFloat(strMax);
				
				this.gauge.setHighestLimit(max);
				
				String strMin = preference.getString("lower", "-40");

				float min = Float.parseFloat(strMin);

				this.gauge.setLowestLimit(min);

				// ����`�悷����̐ݒ�
				Paint axisPaint = new LinePen().getPaint();

				// �[���x�����݂���Ƃ�
				if ( (0 < max) && (min < 0) ) {
				
					// X��
					Line axisX = this.drawAxisX();
	
					// X��������
					canvas.drawLine(axisX.getStartPoint().x, axisX.getStartPoint().y, axisX.getStopPoint().x, axisX.getStopPoint().y, axisPaint);
				}

				// Y��
				Line axisY = this.drawAxisY();

				// Y��������
				canvas.drawLine(axisY.getStartPoint().x, axisY.getStartPoint().y, axisY.getStopPoint().x, axisY.getStopPoint().y, axisPaint);
				
				// ���x��\������
				int count = 0;

				Iterator<Temperature> iterator = MainActivity.this.record.getRecord();

				// ����`�悷����̐ݒ�
				Paint paint = new CirclePen().getPaint();
				
				while (true == iterator.hasNext()) {
				
					Temperature startPoint = iterator.next();

					float point = startPoint.getTemperature();
					
					PointF dot = this.drawPoint(count, point);
					
					canvas.drawCircle(dot.x, dot.y, MainSurfaceView.CIRCLE_RADIUS, paint);

					count++;
				}
				
				// �ݒ�l���擾����
				preference = PreferenceManager.getDefaultSharedPreferences(this.activity);

				// �����o�[�\��
				boolean visible = preference.getBoolean("highbar", false);

				// �����o�[�\������Ƃ�
				if (true == visible) {

					float position = this.gauge.getHighBarPosition();
					
					Line bar = this.drawBar(position);

					Paint barPaint = this.highbarPen.getPaint();

					canvas.drawLine(bar.getStartPoint().x, bar.getStartPoint().y, bar.getStopPoint().x, bar.getStopPoint().y, barPaint);
				}

				// �ቷ�o�[�\���̗L�����擾����
				visible = preference.getBoolean("lowbar", false);

				// �ቷ�o�[�\������Ƃ�
				if (true == visible) {

					float position = this.gauge.getLowBarPosition();

					Line bar = this.drawBar(position);

					Paint barPaint = this.lowbarPen.getPaint();

					canvas.drawLine(bar.getStartPoint().x, bar.getStartPoint().y, bar.getStopPoint().x, bar.getStopPoint().y, barPaint);
				}

				holder.unlockCanvasAndPost(canvas);
			}
		}
		
		
		/**
		 * X��������
		 * @return
		 */
		private Line drawAxisX() {

			int height = this.surfaceView.getHeight();

			float measure = this.gauge.getMeasure();
			
			float scaleY = height / measure;

			float max = this.gauge.getHighestLimit();
			
			// X���̊J�n���W
			float startX = 0.0f;
			float startY = max * scaleY;

			PointF start = new PointF(startX, startY);

			// X���̏I�����W
			float stopX = this.surfaceView.getWidth();
			float stopY = max * scaleY;
			
			PointF stop = new PointF(stopX, stopY);

			Line line = new Line(start, stop);

			return line;
		}


		/**
		 * Y��������
		 * @return
		 */
		private Line drawAxisY() {
			
			// Y���̊J�n���W	
			PointF start = new PointF(0.0f, 0.0f);

			// Y���̏I�����W
			float stopX = 0.0f;
			float stopY = this.surfaceView.getHeight();

			PointF stop = new PointF(stopX, stopY);

			Line line = new Line(start, stop);

			return line;
		}
		
		
		private PointF drawPoint(int count, float point) {

			PointF dot;
			
			float limit = this.gauge.getHighestLimit();

			// �ő�l�𒴂��Ă���Ƃ�
			if (limit < point) {

				int width = this.surfaceView.getWidth();

				int capacity = MainActivity.this.record.getCapacity();

				float scaleX = width / capacity;
				
				float startX = scaleX * count;

				dot = new PointF(startX, 0.0f);

			} else {

				limit = this.gauge.getLowestLimit();

				// �ŏ��l�𒴂��Ă���Ƃ�
				if (point < limit) {

					int width = this.surfaceView.getWidth();

					int capacity = MainActivity.this.record.getCapacity();
					
					float scaleX = width / capacity;
					
					float startX = scaleX * count;

					float startY = this.surfaceView.getHeight();

					dot = new PointF(startX, startY);

				} else {	// �͈͓��̂Ƃ�
					
					int capacity = MainActivity.this.record.getCapacity();

					int width = this.surfaceView.getWidth();

					float scaleX = width / capacity;
					
					float startX = scaleX * count;
					
					int height = this.surfaceView.getHeight();

					float mesure = this.gauge.getMeasure();

					float scaleY = height / mesure;

					float startY = (this.gauge.getHighestLimit() - point) * scaleY;

					dot = new PointF(startX, startY);
				}
			}

			return dot;
		}

		
		private Line drawBar(float point) {

			Line line;
			
			float limit = this.gauge.getHighestLimit();
			
			// �ő�l�𒴂��Ă���Ƃ�
			if (limit < point) {

				PointF start = new PointF(0.0f, 0.0f);
				
				int width = this.surfaceView.getWidth();
				
				PointF stop = new PointF(width, 0.0f);
				
				line = new Line(start, stop);

			} else {

				limit = this.gauge.getLowestLimit();

				// �ŏ��l�𒴂��Ă���Ƃ�
				if (point < limit) {

					int height = this.surfaceView.getHeight();

					PointF start = new PointF(0.0f, height);

					int width = this.surfaceView.getWidth();

					PointF stop = new PointF(width, height);

					line = new Line(start, stop);

				} else {	// �͈͓��̂Ƃ�

					int height = this.surfaceView.getHeight();
					
					float mesure = this.gauge.getMeasure();

					float scaleY = height / mesure;
					
					float y = (this.gauge.getHighestLimit() - point) * scaleY;
					
					PointF start = new PointF(0.0f, y);
					
					int width = this.surfaceView.getWidth();

					PointF stop = new PointF(width, y);

					line = new Line(start, stop);
				}
			}

			return line;
		}


		private void monitoringTemperature() {

			// �ݒ�l���擾����
			SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this.activity);

			boolean warning = preference.getBoolean("highwarning", false);

			// �����x�����L���̂Ƃ�
			if (true == warning) {

				// ���݂̋C�����擾����
				Temperature temperature = MainActivity.this.record.getCurrentTemperature(); 

				if (null != temperature) {

					float current = temperature.getTemperature();

					float position = this.gauge.getHighBarPosition();
	
					if (position < current) {
	
						if (null != this.soundEffect) {

							// �x������炷
							this.soundEffect.playSoundEffectWarning();
						}
					}
				}
			}

			warning = preference.getBoolean("lowwarning", false);

			// �ቷ�x�����L���̂Ƃ�
			if (true == warning) {

				// ���݂̋C�����擾����
				Temperature temperature = MainActivity.this.record.getCurrentTemperature(); 

				if (null != temperature) {

					float current = temperature.getTemperature();

					float position = this.gauge.getLowBarPosition();
	
					if (current < position) {
	
						if (null != this.soundEffect) {

							// �x������炷
							this.soundEffect.playSoundEffectWarning();
						}
					}
				}
			}
		}


		/**
		 * �t�@�C���ɕۑ�����
		 * @param context
		 */
		public void save() {

			try {

				ObjectOutputStream output = new ObjectOutputStream(this.activity.openFileOutput(MainSurfaceView.FILE_NAME, Context.MODE_PRIVATE));

				if (null != output) {

					output.writeObject(this.barPosition);

					output.close();
				}
			}

			catch (IOException exception) {
			}
		}
		

		/**
		 * �L�^�����[�h����
		 * @param context
		 */
		public void load() {

			try {

				// �t�@�C���̃p�X
				String path = this.activity.getFilesDir().getPath() + "/" + MainSurfaceView.FILE_NAME;

				File file = new File(path);

				// �t�@�C�������݂���Ƃ�
				if (true == file.exists()) {

					// �t�@�C�����J��
					ObjectInputStream input = new ObjectInputStream(this.activity.openFileInput(MainSurfaceView.FILE_NAME));

					// �I�u�W�F�N�g��ǂݍ���
					this.barPosition = (BarPosition)input.readObject();

					if (null != input) {

						// �t�@�C�������
						input.close();
					}
				}
			}
			
			catch (IOException exception) {	
			}

			catch (ClassNotFoundException exception) {
			}
		}
	}
}
