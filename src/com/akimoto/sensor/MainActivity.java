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

	//　リクエストコード
	private final int REQUEST_ENABLE_BT = 1;

	// 温度の測定範囲
	private static final float LOWEST_TEMPERATURE = -40.0f;
	private static final float HIGHEST_TEMPERATURE = 125.0f;
	private static final float RESOLUTION_TEMPERATURE = 0.5f;

	// レコード数
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
		
		// BLEに対応しているか確認する
		if (true == getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

			BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
			
			this.bluethoothAdapter = bluetoothManager.getAdapter();
	
			// ブルートゥースに対応していないとき
			if (null == this.bluethoothAdapter) {
				
				// アプリケーションを終了する
				this.finish();

			} else {

				this.calendar = new GregorianCalendar();

				this.record = new TemperatureRecord(MainActivity.RECORD_CAPACITY);

				// ブルートゥースが無効のとき
				if (false == this.bluethoothAdapter.isEnabled()) {
	
					// ブルートゥースの有効確認をする
					Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					
					this.startActivityForResult(intent, this.REQUEST_ENABLE_BT);

				} else {
				
					this.scanner = this.bluethoothAdapter.getBluetoothLeScanner();
				}
				
				SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView1);

				new MainSurfaceView(this, surfaceView);
			}

		} else {

			// アプリケーションを終了する
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

		// ブルートゥースが無効のとき
		if (false == this.bluethoothAdapter.isEnabled()) {

			// ブルートゥースの有効確認をする
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
			
			// ブルートゥースが有効のとき
			if (true == this.bluethoothAdapter.isEnabled()) {

				if (null != this.scanner) {

					this.scanner.stopScan(this.scanCallback);
				}

				// ブルートゥースを無効にする
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

		// settingsメニューが選択されたとき
		if (id == R.id.action_setting) {

			Intent intent = new Intent(this, SensorSettingActivity.class);

			// 設定画面に遷移する
			this.startActivity(intent);
			
			return true;
		}

		// aboutメニューが選択されたとき
		if (id == R.id.action_about) {

			try {

				AlertDialog.Builder dialog = new AlertDialog.Builder(this);

				// ダイアログに表示する文字列を取得する
				String title = getString(R.string.dialog_title);
				String button = getString(R.string.dialog_button);

				PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
				
				String version = packageInfo.versionName;

				String message = "Copyright Akimoto Masane.\n" + "All Rights Reserved.\n" + "Version : " + version;

				// ダイアログ画面の設定
				dialog.setTitle(title);
				dialog.setMessage(message);
				dialog.setPositiveButton(button, null);

				// ダイアログを表示する
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

		// ブルートゥースの有効化リクエストのとき
		if (this.REQUEST_ENABLE_BT == requestCode) {

			// 成功したとき
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

								// 温度がプラスの時
								if (0 <= sensor) {

									// 0.5度加える
									sensor += MainActivity.RESOLUTION_TEMPERATURE;
									
								} else {	// 温度がマイナスのとき

									// 0.5度減らす
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
		
							// 現在温度を取得する
							Temperature current = MainActivity.this.record.getCurrentTemperature();
							
							TextView text = (TextView)findViewById(R.id.textView4);
		
							// 現在温度を設定する
							text.setText(String.valueOf(current.getTemperature()));
							
							// 最高温度を取得する
							Temperature highest = MainActivity.this.record.getHighestTemperature();
							
							text = (TextView)findViewById(R.id.textView5);
		
							// 最高温度を設定する
							text.setText(String.valueOf(highest.getTemperature()));
							
							// 最低温度を取得する
							Temperature lowest = MainActivity.this.record.getLowestTemperature();
							
							text = (TextView)findViewById(R.id.textView6);
		
							// 最低温度を設定する
							text.setText(String.valueOf(lowest.getTemperature()));
						}

						TextView text = (TextView)findViewById(R.id.textView9);
						
						// デバイス名を設定する
						text.setText(name);
						
						// RSSIを取得する
						int rssi = result.getRssi();
						
						text = (TextView)findViewById(R.id.textView10);
						
						// RSSIを設定する
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

		// 得点保存用ファイル名
		private static final String FILE_NAME = "BarPosition";
		
		// 点の半径
		private static final float CIRCLE_RADIUS = 3.0f;

		// グラフの描画間隔
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

			// 設定値を取得する
			SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this.activity);

			String strMax = preference.getString("upper", "125");

			int max = Integer.parseInt(strMax);
			
			String strMin = preference.getString("lower", "-40");
			
			int min = Integer.parseInt(strMin);

			// 設定値を読み出す
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

			// 画面が作成されるとき
			if (true == holder.isCreating()) {

				if (null == this.soundEffect) {
	
					this.soundEffect = new SoundEffect();
	
					this.soundEffect.loadSoundFile(this.activity);
				}
			}
		}


		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

			// スレッド処理を終了する
			this.processing = false;

			try {

				// スレッドの終了を待つ
				this.thread.join(MainSurfaceView.SLEEP_TIME * 2);

				if (null != this.soundEffect) {

					// リソースを開放する
					this.soundEffect.releaseSoundFile();

					this.soundEffect = null;
				}

				float high = this.gauge.getHighBarPosition();

				this.barPosition.setHigh(high);

				float low = this.gauge.getLowBarPosition();

				this.barPosition.setLow(low);

				// 設定値を保存する
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

					// 温度を描画する
					this.doDraw();

					// 温度警告をチェック
					this.monitoringTemperature();
				}
				
				catch (InterruptedException exception) {
				}
			}
		}


		private void onActionDown(float x, float y) {

			// 設定値を取得する
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

			// 高温バーが掴まれていないとき
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

				// 背景を白で塗り潰す
				canvas.drawColor(Color.WHITE);

				// 設定値を取得する
				SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this.activity);

				String strMax = preference.getString("upper", "125");
				
				float max = Float.parseFloat(strMax);
				
				this.gauge.setHighestLimit(max);
				
				String strMin = preference.getString("lower", "-40");

				float min = Float.parseFloat(strMin);

				this.gauge.setLowestLimit(min);

				// 軸を描画する線の設定
				Paint axisPaint = new LinePen().getPaint();

				// ゼロ度が存在するとき
				if ( (0 < max) && (min < 0) ) {
				
					// X軸
					Line axisX = this.drawAxisX();
	
					// X軸を引く
					canvas.drawLine(axisX.getStartPoint().x, axisX.getStartPoint().y, axisX.getStopPoint().x, axisX.getStopPoint().y, axisPaint);
				}

				// Y軸
				Line axisY = this.drawAxisY();

				// Y軸を引く
				canvas.drawLine(axisY.getStartPoint().x, axisY.getStartPoint().y, axisY.getStopPoint().x, axisY.getStopPoint().y, axisPaint);
				
				// 温度を表示する
				int count = 0;

				Iterator<Temperature> iterator = MainActivity.this.record.getRecord();

				// 軸を描画する線の設定
				Paint paint = new CirclePen().getPaint();
				
				while (true == iterator.hasNext()) {
				
					Temperature startPoint = iterator.next();

					float point = startPoint.getTemperature();
					
					PointF dot = this.drawPoint(count, point);
					
					canvas.drawCircle(dot.x, dot.y, MainSurfaceView.CIRCLE_RADIUS, paint);

					count++;
				}
				
				// 設定値を取得する
				preference = PreferenceManager.getDefaultSharedPreferences(this.activity);

				// 高温バー表示
				boolean visible = preference.getBoolean("highbar", false);

				// 高温バー表示するとき
				if (true == visible) {

					float position = this.gauge.getHighBarPosition();
					
					Line bar = this.drawBar(position);

					Paint barPaint = this.highbarPen.getPaint();

					canvas.drawLine(bar.getStartPoint().x, bar.getStartPoint().y, bar.getStopPoint().x, bar.getStopPoint().y, barPaint);
				}

				// 低温バー表示の有無を取得する
				visible = preference.getBoolean("lowbar", false);

				// 低温バー表示するとき
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
		 * X軸を引く
		 * @return
		 */
		private Line drawAxisX() {

			int height = this.surfaceView.getHeight();

			float measure = this.gauge.getMeasure();
			
			float scaleY = height / measure;

			float max = this.gauge.getHighestLimit();
			
			// X軸の開始座標
			float startX = 0.0f;
			float startY = max * scaleY;

			PointF start = new PointF(startX, startY);

			// X軸の終了座標
			float stopX = this.surfaceView.getWidth();
			float stopY = max * scaleY;
			
			PointF stop = new PointF(stopX, stopY);

			Line line = new Line(start, stop);

			return line;
		}


		/**
		 * Y軸を引く
		 * @return
		 */
		private Line drawAxisY() {
			
			// Y軸の開始座標	
			PointF start = new PointF(0.0f, 0.0f);

			// Y軸の終了座標
			float stopX = 0.0f;
			float stopY = this.surfaceView.getHeight();

			PointF stop = new PointF(stopX, stopY);

			Line line = new Line(start, stop);

			return line;
		}
		
		
		private PointF drawPoint(int count, float point) {

			PointF dot;
			
			float limit = this.gauge.getHighestLimit();

			// 最大値を超えているとき
			if (limit < point) {

				int width = this.surfaceView.getWidth();

				int capacity = MainActivity.this.record.getCapacity();

				float scaleX = width / capacity;
				
				float startX = scaleX * count;

				dot = new PointF(startX, 0.0f);

			} else {

				limit = this.gauge.getLowestLimit();

				// 最小値を超えているとき
				if (point < limit) {

					int width = this.surfaceView.getWidth();

					int capacity = MainActivity.this.record.getCapacity();
					
					float scaleX = width / capacity;
					
					float startX = scaleX * count;

					float startY = this.surfaceView.getHeight();

					dot = new PointF(startX, startY);

				} else {	// 範囲内のとき
					
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
			
			// 最大値を超えているとき
			if (limit < point) {

				PointF start = new PointF(0.0f, 0.0f);
				
				int width = this.surfaceView.getWidth();
				
				PointF stop = new PointF(width, 0.0f);
				
				line = new Line(start, stop);

			} else {

				limit = this.gauge.getLowestLimit();

				// 最小値を超えているとき
				if (point < limit) {

					int height = this.surfaceView.getHeight();

					PointF start = new PointF(0.0f, height);

					int width = this.surfaceView.getWidth();

					PointF stop = new PointF(width, height);

					line = new Line(start, stop);

				} else {	// 範囲内のとき

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

			// 設定値を取得する
			SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this.activity);

			boolean warning = preference.getBoolean("highwarning", false);

			// 高温警告が有効のとき
			if (true == warning) {

				// 現在の気温を取得する
				Temperature temperature = MainActivity.this.record.getCurrentTemperature(); 

				if (null != temperature) {

					float current = temperature.getTemperature();

					float position = this.gauge.getHighBarPosition();
	
					if (position < current) {
	
						if (null != this.soundEffect) {

							// 警告音を鳴らす
							this.soundEffect.playSoundEffectWarning();
						}
					}
				}
			}

			warning = preference.getBoolean("lowwarning", false);

			// 低温警告が有効のとき
			if (true == warning) {

				// 現在の気温を取得する
				Temperature temperature = MainActivity.this.record.getCurrentTemperature(); 

				if (null != temperature) {

					float current = temperature.getTemperature();

					float position = this.gauge.getLowBarPosition();
	
					if (current < position) {
	
						if (null != this.soundEffect) {

							// 警告音を鳴らす
							this.soundEffect.playSoundEffectWarning();
						}
					}
				}
			}
		}


		/**
		 * ファイルに保存する
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
		 * 記録をロードする
		 * @param context
		 */
		public void load() {

			try {

				// ファイルのパス
				String path = this.activity.getFilesDir().getPath() + "/" + MainSurfaceView.FILE_NAME;

				File file = new File(path);

				// ファイルが存在するとき
				if (true == file.exists()) {

					// ファイルを開く
					ObjectInputStream input = new ObjectInputStream(this.activity.openFileInput(MainSurfaceView.FILE_NAME));

					// オブジェクトを読み込む
					this.barPosition = (BarPosition)input.readObject();

					if (null != input) {

						// ファイルを閉じる
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
