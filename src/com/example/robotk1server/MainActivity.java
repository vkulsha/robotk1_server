package com.example.robotk1server;

import com.example.robotk1server.R;

//import org.microbridge.server.Server;
//import org.microbridge.server.AbstractServerListener;

import android.os.*;
import android.app.Activity;
//import android.bluetooth.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.MediaController.MediaPlayerControl;
import android.media.*;
import android.media.MediaPlayer.*;
import android.content.*;
import android.os.Environment;
import android.speech.tts.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity implements OnTouchListener {
	Context context;
	VideoView videoView;
	MediaPlayer mediaPlayer;
	TextToSpeech tts;

/*	static Server mAdbServer = null;// adb server*/
	static byte[] data = new byte[54];
	static String dataBT = "";
	private final int SERVER_PORT = 4567; // Define the server port
	ServerThread serverThread;
	TTSThread mmTtsThread;
	Handler h;

	private static final String TAG = "robotk1";
	private static final int REQUEST_ENABLE_BT = 1;
	final int RECIEVE_MESSAGE_BT_CONNECT = 1, RECIEVE_MESSAGE_BT_CONNECTED = 2,
			RECIEVE_MESSAGE_CLIENT = 3, RECIEVE_MESSAGE_TTS_START = 4,
			RECEIVE_MESSAGE_TTS_STOP = 5;
/*	private BluetoothAdapter btAdapter = null;*/
	private StringBuilder sb = new StringBuilder();
	private static BTConnectedThread mBTConnectedThread;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static String address = "98:D3:31:80:5B:FF";
	private BTConnectThread mBTConnectThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);
		context = this.getApplicationContext();

/*		try {
			mAdbServer = new Server(4568); // ADK-port
			mAdbServer.start();

		} catch (IOException e) {
			Toast.makeText(this.getApplicationContext(), "error",
					Toast.LENGTH_SHORT).show();
			System.exit(-1);
		}

		mAdbServer.addListener(new AbstractServerListener() {
			@Override
			public void onReceive(org.microbridge.server.Client client,
					byte[] data) {
			}
		});
*/
		serverThread = new ServerThread(SERVER_PORT);
		serverThread.start();

		String videoSource = "/sdcard/morg.mp4";
		File f = new File(videoSource);
		if (f.exists()) {
			videoView = (VideoView) findViewById(R.id.videoView1);
			videoView.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					videoView.resume();
				}
			});
	
			videoView.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);
					videoView.start();
				}
			});
	
			videoView.setVideoPath(videoSource);
			MediaController mc = new MediaController(this);
			mc.setVisibility(View.INVISIBLE);
			videoView.setMediaController(mc);
			videoView.requestFocus(0);
			videoView.start();
		}
		// playSound("nosound.m4a");

		for (int i = 0; i <= 53; i++) {
			data[i] = 0;
		}

		h = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case RECIEVE_MESSAGE_CLIENT:
					String str = (String) msg.obj;//
					if (str.charAt(0) == '0') {
						String val = str.substring(3);
						byte pwmCount = Byte.valueOf(str.substring(0, 3));
						sss(val.substring(pwmCount * 3),
								val.substring(0, (pwmCount * 3)));

					} else if (str.contains("BT_OFF")) {
						//mBTConnectThread.stop();
						//mBTConnectThread.cancel();
						//mBTConnectThread = null;

					} else if (str.contains("BT_ON")) {
						//if (btAdapter != null && btAdapter.isEnabled()) {
						//	mBTConnectThread = new BTConnectThread(btAdapter, address);
						//	mBTConnectThread.start();
						//}
					} else {
						speak(str);
					}
					break;
				case RECIEVE_MESSAGE_BT_CONNECT:
/*					BluetoothSocket mmSocket;
					mmSocket = (BluetoothSocket) msg.obj;
					if (mmSocket.getRemoteDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
						Toast.makeText(context, "Bluetooth connected!",
								Toast.LENGTH_SHORT).show();
					}

					mBTConnectedThread = new BTConnectedThread(mmSocket);
					mBTConnectedThread.start();
*/					break;
				case RECIEVE_MESSAGE_BT_CONNECTED:
					byte[] readBuf = (byte[]) msg.obj;
					String strIncom = new String(readBuf, 0, msg.arg1);
					sb.append(strIncom);
					int endOfLineIndex = sb.indexOf("\r\n");
					if (endOfLineIndex > 0) {
						String sbprint = sb.substring(0, endOfLineIndex);
						// Toast.makeText(context, sbprint,
						// Toast.LENGTH_SHORT).show();
						sb.delete(0, sb.length());
					}
					break;
				case RECIEVE_MESSAGE_TTS_START:
					tts = new TextToSpeech(getApplicationContext(),
							new TextToSpeech.OnInitListener() {
								@Override
								public void onInit(int status) {
									if (status != TextToSpeech.ERROR) {
										tts.setLanguage(Locale.getDefault());
										tts.setPitch((float) 0.45);
										tts.setSpeechRate((float) 0.7);
									}
								}
							});

					break;
				case RECEIVE_MESSAGE_TTS_STOP:
					if (tts != null) {
						tts.stop();
						tts.shutdown();
						tts = null;
					}
					break;
				}
			}
		};

/*		btAdapter = BluetoothAdapter.getDefaultAdapter();
		checkBTState();*/
	}

	public void speak(String txt) {
		tts.speak(txt, TextToSpeech.QUEUE_FLUSH, null);
		// Toast.makeText(context, txt, Toast.LENGTH_SHORT).show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
		//if (btAdapter != null && btAdapter.isEnabled()) {
		//	mBTConnectThread = new BTConnectThread(btAdapter, address);
		//	mBTConnectThread.start();
		//}

		mmTtsThread = new TTSThread();
		mmTtsThread.start();

	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
/*		if (btAdapter != null && btAdapter.isEnabled()) {
			mBTConnectThread.cancel();
		}
		mmTtsThread.cancel();

		mAdbServer.stop();*/
		serverThread.cancel();
		// serverThread.stop();
		// serverThread.destroy();

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

	public void playSound(String filename) {
		final String fn = filename;
		String filePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/" + fn;
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(filePath);
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (Exception ex) {
		}
		;
	};

	public void sss(String comm, String speed) {
		byte s1 = 0, s2 = 0;
		boolean front = false, left = false, right = false, back = false, up1 = false, down1 = false, up2 = false, down2 = false, manip1on = false, manip1off = false, manip2on = false, manip2off = false, off = false;

		if (comm != "off") {
			front = comm.contains("front") && comm != "off";
			left = comm.contains("left") && comm != "off";
			right = comm.contains("right") && comm != "off";
			back = comm.contains("back") && comm != "off";
			up1 = comm.contains("up1") && comm != "off";
			down1 = comm.contains("down1") && comm != "off";
			up2 = comm.contains("up2") && comm != "off";
			down2 = comm.contains("down2") && comm != "off";
			manip1on = comm.contains("manip1on") && comm != "off";
			manip1off = comm.contains("manip1off") && comm != "off";
			manip2on = comm.contains("manip2on") && comm != "off";
			manip2off = comm.contains("manip2off") && comm != "off";

			s1 = speed.length() >= 3 ? Byte.valueOf(speed.substring(0, 3)) : 0;
			s2 = speed.length() >= 6 ? Byte.valueOf(speed.substring(3, 6)) : 0;
		}
		;

		sendData(front, left, right, back, up1, down1, up2, down2, manip1on,
				manip1off, manip2on, manip2off, s1, s2);

	}

	public static byte b2i(boolean value) {
		byte one = 1;
		byte nul = 0;
		return value ? one : nul;
	}

	public void sendData(boolean front, boolean left, boolean right,
			boolean back, boolean up1, boolean down1, boolean up2,
			boolean down2, boolean manip1on, boolean manip1off,
			boolean manip2on, boolean manip2off, byte val1, byte val2) {
		data[12] = (byte) (val1 * b2i(front || left || right || back));// pwm
																		// for
																		// left
																		// front
																		// motor
		data[11] = (byte) (val1 * b2i(front || left || right || back));// pwm
																		// for
																		// left
																		// back
																		// motor
		data[10] = (byte) (val1 * b2i(front || left || right || back));// pwm
																		// for
																		// right
																		// front
																		// motor
		data[9] = (byte) (val1 * b2i(front || left || right || back));// pwm for
																		// right
																		// back
																		// motor
		data[13] = (byte) (val2 * b2i(up1 || down1 || up2 || down2 || manip1on
				|| manip1off || manip2on || manip2off));// pwm for left and
														// right hands motors

		data[22] = b2i(front || right);// rel for left motors -> front
		data[23] = b2i(left || back);// rel for left motors -> back
		data[24] = b2i(front || left);// rel for right motors -> front
		data[25] = b2i(right || back);// rel for right motors -> back

		data[26] = b2i(up1);// rel for left hand -> up
		data[27] = b2i(down1);// rel for left hand -> down
		data[28] = b2i(up2);// rel for right hand -> up
		data[29] = b2i(down2);// rel right hand -> dowm

		data[30] = b2i(manip1on);// rel for left hand manipulator -> open
		data[31] = b2i(manip1off);// rel for left hand manipulator -> close
		data[32] = b2i(manip2on);// rel for right hand manipulator -> open
		data[33] = b2i(manip2off);// rel for right hand manipulator -> close

		// textView1.setText("13 = " + data[13] + ", 9 = " + data[9]);

		sendControlData(data);
	}

	public void sendControlData(byte[] data) {
/*		try {
			if (btAdapter != null
					&& btAdapter.isEnabled()
					&& mBTConnectedThread != null
					&& mBTConnectThread != null
					&& mBTConnectThread.mmSocket != null
					&& mBTConnectThread.mmSocket.getRemoteDevice()
							.getBondState() == BluetoothDevice.BOND_BONDED) {
				// Toast.makeText(context, Byte.toString((byte)data[13]),
				// Toast.LENGTH_SHORT).show();
				mBTConnectedThread.write(data);// bluetooth server send
			} else if (mAdbServer != null) {
				mAdbServer.send(data);// adb server send
			}
		} catch (IOException e) {
			Toast.makeText(this.getApplicationContext(), "error send",
					Toast.LENGTH_SHORT).show();
		}*/

	}

	private void checkBTState() {
/*		if (btAdapter == null) {
			errorExit("Fatal Error", "Bluetooth �� ��������������");
		} else {
			if (btAdapter.isEnabled()) {
			} else {
				Toast.makeText(this.getApplicationContext(),
						"Bluetooth ��������, �������� ����� ADB!",
						Toast.LENGTH_SHORT).show();
				// errorExit("Fatal Error",
				// "�������� Bluetooth � ������������� ���������!");
				// Prompt user to turn on Bluetooth
				// Intent enableBtIntent = new
				// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				// startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}*/
	}

	private void errorExit(String title, String message) {
		Toast.makeText(getBaseContext(), title + " - " + message,
				Toast.LENGTH_LONG).show();
		finish();
	}

	class ServerAsyncTask extends AsyncTask<Socket, Void, Void> {
		@Override
		protected Void doInBackground(Socket... params) {
			Socket mySocket = params[0];
			try {
				BufferedReader tmpIn = new BufferedReader(
						new InputStreamReader(mySocket.getInputStream()));
				PrintWriter tmpOut = new PrintWriter(
						mySocket.getOutputStream(), true);
				tmpOut.println("robot-k1:>");
				String str = tmpIn.readLine();
				// Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
				h.obtainMessage(RECIEVE_MESSAGE_CLIENT, -1, -1, str)
						.sendToTarget();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}
	}

	class ServerThread extends Thread {
		private ServerSocket socServer;
		private Socket socClient = null;

		public ServerThread(int serverPort) {
			try {
				socServer = new ServerSocket(serverPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				while (true) {
					socClient = socServer.accept();
					ServerAsyncTask serverAsyncTask = new ServerAsyncTask();
					serverAsyncTask.execute(new Socket[] { socClient });

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void cancel() {
			try {
				socServer.close();
				socServer = null;
			} catch (IOException e) {
			}
		}

	}

	class TTSThread extends Thread {
		public TTSThread() {
		}

		public void run() {
			h.obtainMessage(RECIEVE_MESSAGE_TTS_START, 0, -1, null)
					.sendToTarget(); 
		}

		public void cancel() {
			h.obtainMessage(RECEIVE_MESSAGE_TTS_STOP, 0, -1, null)
					.sendToTarget(); 
		}
	};

	class BTConnectThread extends Thread {
/*		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private final BluetoothAdapter mmAdapter;
		String tmpAddress;

		public BTConnectThread(BluetoothAdapter adapter, String address) {
			BluetoothSocket tmpSocket = null;
			mmAdapter = adapter;
			tmpAddress = address;
			mmDevice = mmAdapter.getRemoteDevice(tmpAddress);
			try {
				tmpSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
			}
			mmSocket = tmpSocket;
		}

		public void run() {
			mmAdapter.cancelDiscovery();

			try {
				mmSocket.connect();
				h.obtainMessage(RECIEVE_MESSAGE_BT_CONNECT, -1, -1, mmSocket)
						.sendToTarget(); 
											// Handler
			} catch (IOException connectException) {
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}*/
	}

	private class BTConnectedThread extends Thread {
/*		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public BTConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		@Override
		public void run() {
			byte[] buffer = new byte[256]; // buffer store for the stream
			int bytes; // bytes returned from read()
			// Toast.makeText(context, "Bluetooth3", Toast.LENGTH_SHORT).show();

			while (true) {
				try {
					bytes = mmInStream.read(buffer);
					h.obtainMessage(RECIEVE_MESSAGE_BT_CONNECTED, bytes, -1,
							buffer).sendToTarget(); 
				} catch (IOException e) {
					break;
				}
			}
		}

		public void write(String message) {
			byte[] msgBuffer = message.getBytes();
			try {
				mmOutStream.write(msgBuffer);
			} catch (IOException e) {
			}
		}

		public void write(byte[] message) {
			try {
				mmOutStream.write(message);
			} catch (IOException e) {
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}*/
	}

}
