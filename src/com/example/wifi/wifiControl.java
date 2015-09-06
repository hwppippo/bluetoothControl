package com.example.wifi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.BluetoothChat.BluetoothChat;
import com.example.android.BluetoothChat.BluetoothChatService;
import com.example.android.BluetoothChat.R;
import com.example.tcputil.TcpSocketCallback;
import com.example.tcputil.TcpSocketConnect;

public class wifiControl extends Activity {

	// Debugging
	private static final String TAG = "wifiControl";
	private static final boolean D = true;

	// Message types sent from the conncet Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;

	private int mState;

	// layout view
	private Button open, close;
	private ListView mConversationView;
	private TextView mTitle;

	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;

	// tcp客户端连接
	private TcpSocketConnect wifiConnect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.wifi_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		mState = BluetoothChatService.STATE_NONE;

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		wifiConnect = new TcpSocketConnect(wifiCallback,
				getString(R.string.ip),
				Integer.parseInt(getString(R.string.port)));
		new Thread(wifiConnect).start();

		setChatView();
	}

	private void setChatView() {

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		mConversationView = (ListView) findViewById(R.id.wifi_info);
		mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the send button with a listener that for click events
		open = (Button) findViewById(R.id.wifi_open);
		open.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String message = "1";
				sendMessage(message);
			}
		});

		close = (Button) findViewById(R.id.wifi_close);
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String message = "0";
				sendMessage(message);
			}
		});
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the connect to write

			wifiConnect.sendButCmd(message.getBytes());

			// Share the sent message back to the UI Activity
			mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1,
					message.getBytes()).sendToTarget();
		}
	}

	TcpSocketCallback wifiCallback = new TcpSocketCallback() {
		@Override
		public void tcp_connected() {
			Log.e("", "连接成功");
			setState(BluetoothChatService.STATE_CONNECTED);
		}

		@Override
		public void tcp_receive(byte[] buffer) {
			mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, buffer.length,
					-1, buffer).sendToTarget();
		}

		@Override
		public void tcp_disconnect() {
			setState(BluetoothChatService.STATE_NONE);
		}
	};

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1)
				.sendToTarget();
	}

	// The Handler that gets information back from the connect
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Send:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				Log.e("rece", readBuf.length + "");
				mConversationArrayAdapter.add("Rcv" + ":  " + readMessage);
				break;
			}
		}
	};
	
	protected void onStop() {
		super.onStop();
		
		if (wifiConnect != null) {
			wifiConnect.disconnect();
		}
	};
}
