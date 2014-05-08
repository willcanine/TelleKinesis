package com.example.tellekinesis;

import org.json.JSONObject;

import io.socket.*;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

public class MainActivity extends Activity implements SensorEventListener {
	
	SocketIO socket;
	
	Vibrator vib;
	
	static final String LOGTAG = "Accelerometer";
	SensorManager sensorManager;
    SensorEventListener sensorListener;
    Sensor accelerometerSensor;
    
    float SHAKE_THRESH  = 8;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

		try {
			
			socket = new SocketIO("http://ec2-54-186-5-67.us-west-2.compute.amazonaws.com:8080/");

			socket.connect(new IOCallback() {
			    
				//WHERE EVERYTHING COMES IN FROM THE SOCKET
				@Override
			    public void on(String event, IOAcknowledge ack, Object... args) {
			        if (event.equals("vibrate")) {
			        	if (args[0].equals("shake")) {
			        		vib.vibrate(500);
			        	} else {
			        		vib.cancel();
			        	}
			            Log.v("SocketIO", "vibrate " + args[0]);
			        } else {
			        	//logs event if it is not "vibrate"	
			        	Log.v("SocketIO", event + " " + args[0]);
			        }
			    }

			    @Override
			    public void onMessage(JSONObject json, IOAcknowledge ack) {
			    		Log.v("SocketIO", json.toString());
			    		//handleMessage(json.toString());	    		    		
			    }

			    @Override
			    public void onMessage(String data, IOAcknowledge ack) {
			    		Log.v("SocketIO", data);
			    		//handleMessage(data);
			    	}

			    @Override
			    public void onError(SocketIOException socketIOException) {
			    		socketIOException.printStackTrace();
			    }
			    @Override
			    public void onDisconnect() {
	    				Log.v("SocketIO", "Disconnected");    			
			    }
			    @Override
			    public void onConnect() {
		    			Log.v("SocketIO", "Connected");
			    }		
			});
			
			//not in the callback, just when we start
			socket.emit("message", "android says hello");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	// Handlers let us interact with threads on the UI thread
//	// The handleMessage method receives messages from other threads and will act upon it on the UI thread
//	
//		Handler handler = new Handler() {
//		  
//		@Override
//		  public void handleMessage(Message msg) {
//
//		    // Pull out the data that was packed into the message with the key "message"
//			String messageData = msg.getData().getString("message");
//
//			// Send it over to the view
//			tv.setText(messageData);
//		  }
//	};	
//
//	public void handleMessage(String message) {
//		// First we obtain a message object
//		Message msg = handler.obtainMessage();
//
//		// Create a bundle to hold data
//		Bundle bundle = new Bundle();
//
//		// Put our value with the key "message"
//		bundle.putString("message", message);
//
//		// Set the message data to our bundle
//		msg.setData(bundle);
//
//		// and finally send the message via the handler
//		handler.sendMessage(msg);
//	}
	
	protected void onResume(){
		super.onResume();
        
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
       
    }
		
public void onSensorChanged(SensorEvent event) {
    	
    	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
    		
    		float[] accelVals = event.values;
    		float averageVals = 0;
    		
    		for (int i = 0; i < accelVals.length; i++){
    			averageVals = accelVals[i] + averageVals;
    		}
    		averageVals = averageVals/accelVals.length;
    		
    		if (accelVals[0] > SHAKE_THRESH){
    			socket.emit("vibrate", "shake");
    		} else if(averageVals <= SHAKE_THRESH){
    			socket.emit("vibrate", "NOShake");
    		}
    		
    	}
	}

@Override
public void onAccuracyChanged(Sensor sensor, int accuracy) {
	// needed for library to run	
}

}
