package com.example.walle;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class WallE extends Activity implements SensorEventListener, LocationListener{

	float x=0;
	LocationManager locationManager;
	static double lat;
	static double longi;
	
	String ipaddress="192.168.1.5";
	int port=5010;
	int accuracy=3;
	private static final int RESULT_SETTINGS = 1;
	boolean connectflag=false;
	boolean datastream=true;
	String previousmsg="";
	
	public static double destLat=27.69865;
	public static double destlong=85.29693;
    // define the display assembly compass picture
    private ImageView image,trackimg;

    // record the compass picture angle turned
    private float currentDegree = 0f;
 
    // device sensor manager
    private SensorManager mSensorManager;
 
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;
    
    private float[] valuesAccelerometer;
    private float[] valuesMagneticField;
      
    private float[] matrixR;
    private float[] matrixI;
    private float[] matrixValues;
    
    double azimuth=0;
    TextView tvHeading,tvloc,tvtemp,compassactions,eta;
    
    
    Button connectButton;
    
    int tempcount=0;
    private Socket clientSocket=null;
  	private DataOutputStream outToServer=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_wall_e);
 
        //
        image = (ImageView) findViewById(R.id.imageViewCompass);
        trackimg=(ImageView)findViewById(R.id.trackimg);
        
        eta=(TextView)findViewById(R.id.eta);
        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);
        tvloc=(TextView)findViewById(R.id.locationtext);
        tvtemp=(TextView)findViewById(R.id.temp);
        compassactions=(TextView)findViewById(R.id.compassactions);
        
        ImageButton getDestination =(ImageButton)findViewById(R.id.button1);
        getDestination.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				startIntentMap();
			}
		});
        
       connectButton = (Button)findViewById(R.id.button2);
        
        connectButton.setOnClickListener(new OnClickListener() {
    	    
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//connect();
				MyTask task = new MyTask();
		        task.execute();
		        
			}
        });
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
          Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
          startActivity(intent);
        } 
        
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
        
        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
           
        valuesAccelerometer = new float[3];
        valuesMagneticField = new float[3];
       
        matrixR = new float[9];
        matrixI = new float[9];
        matrixValues = new float[3];
    }
 
    
    void startIntentMap()
    {
    	 Intent destination = new Intent(this,Map.class);	
		startActivity(destination);
    }
    @Override
    protected void onResume() {
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    	mSensorManager.registerListener(this,
       sensorAccelerometer,
       SensorManager.SENSOR_DELAY_GAME);
    	mSensorManager.registerListener(this,
       sensorMagneticField,
       SensorManager.SENSOR_DELAY_GAME);
     super.onResume();
    }
     
    @Override
    protected void onPause() {
    	locationManager.removeUpdates(this);
    	mSensorManager.unregisterListener(this,
       sensorAccelerometer);
    	mSensorManager.unregisterListener(this,
       sensorMagneticField);
     super.onPause();
    }
 
    
    public void CompassProcess(float current, float destination,float distance){
    	if(distance>=accuracy){
    	if(destination-current>5.0)
    	{
    		compassactions.setText("TURN RIGHT");
    		if(connectflag){
    		 sendData("R");
    		}
    	}
    	else if(destination-current<-5.0)
    	{
    		compassactions.setText("TURN LEFT");
    		if(connectflag){
    		 sendData("L");
    		}
    	}
    	else
    	{
    		compassactions.setText("FORWARD");
    		if(connectflag){
    		 sendData("F");
    		}
    	}
    	}
    	else
    	{
    		compassactions.setText("Destination Reached.");
    		if(connectflag){
    		 sendData("H");
    		}
    	}
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
 
    	/**********************************/
    	 switch(event.sensor.getType()){
    	  case Sensor.TYPE_ACCELEROMETER:
    	   for(int i =0; i < 3; i++){
    	    valuesAccelerometer[i] = event.values[i];
    	   }
    	   break;
    	  case Sensor.TYPE_MAGNETIC_FIELD:
    	   for(int i =0; i < 3; i++){
    	    valuesMagneticField[i] = event.values[i];
    	   }
    	   break;
    	  }
    	    
    	  boolean success = SensorManager.getRotationMatrix(
    	       matrixR,
    	       matrixI,
    	       valuesAccelerometer,
    	       valuesMagneticField);
    	    
    	  if(success){
    	   SensorManager.getOrientation(matrixR, matrixValues);
    	     
    	   azimuth = Math.toDegrees(matrixValues[0]);
    	  
    	     
    	  }
    	/**************************************/
    	
    	
    	
        // get the angle around the z-axis rotated
        float degree = Math.round(azimuth);  ///
 
        if (degree < 0)
        	degree += 360;
        
        
        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");
 
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
 
        // how long the animation will take place
        ra.setDuration(210);
 
        // set the animation after the end of the reservation status
        ra.setFillAfter(true);
 
        // Start the animation
        image.startAnimation(ra);
        
        /**********************loc img*********************/
        
      //  float lonDiff = (float) (destlong -longi);
    	//float latDiff = (float) (destLat -lat);
    	
    	Location src=new Location("Current");
    	Location dest=new Location("Destination");
    	
        src.setLatitude(lat);
        src.setLongitude(longi);
        dest.setLatitude(destLat);
        dest.setLongitude(destlong);
    	
    //    src.setLatitude(0);
     //   src.setLongitude(0);
      //  dest.setLatitude(0);
      //  dest.setLongitude(1000000);
        
        float destAngle = src.bearingTo(dest);
        float distance = src.distanceTo(dest);
        
        eta.setText("EDA : "+distance + " Meter(s)");
      if (destAngle < 0)
		destAngle += 360;
        
        tvtemp.setText(Double.toString(destAngle));
        
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra1 = new RotateAnimation(
                x,
                -(float) destAngle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
 
        // how long the animation will take place
        ra1.setDuration(210);
 
        // set the animation after the end of the reservation status
        ra1.setFillAfter(true);
 
        // Start the animation
        trackimg.startAnimation(ra1);
        
        x=(float) destAngle;
        /****************************************/
        
       tempcount++;
       if(tempcount==10)
       {
        CompassProcess(degree,(float) destAngle,distance);
        tempcount=0;
       }
      currentDegree = -degree;
 
    }
 
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		tvloc.setText(Double.toString(location.getLatitude())+" , " +Double.toString(location.getLongitude()));
		lat=location.getLatitude();
		longi=location.getLongitude();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	 public void connect()
	 {
		 //TextView view1 = (TextView) findViewById(R.id.temp);
		 try {
			clientSocket= new Socket(ipaddress,port);
			outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
			connectflag=true;
			//view1.setText("Connected to Server.");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			connectflag=false;
			// view1.setText("Don't know about host: hostname");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			connectflag=false;
			//  view1.setText("Couldn't get I/O for the connection to: hostname");
		}
	 }
	       
	 
	 public void sendData(String msg)
	 {
		 TextView view = (TextView) findViewById(R.id.temp);
		 if (clientSocket != null && outToServer != null) {
	            try {
	               if(datastream){
	            	outToServer.writeBytes(msg);
	            	}
	               else{
	            	   if(msg.equals(previousmsg))
	            	   {
	            	   }
	            	   else
	            	   {
	            		   outToServer.writeBytes(msg);
	            	   }
	               }
	            	previousmsg=msg;
	            } catch (IOException e) {
	            	 view.setText("IOException:  " + e);
	            }
	           
	        }
		 
	 }
	
	 public void closeSocket()
	 {
		 TextView view = (TextView) findViewById(R.id.temp);
		 
		 if (clientSocket != null && outToServer != null) {
	            try {
	                outToServer.close();
	                clientSocket.close();
	                view.setText("Disconnected.");
	            } catch (UnknownHostException e) {
	                view.setText("Trying to connect to unknown host: " + e);
	            } catch (IOException e) {
	                view.setText("IOException:  " + e);
	            }
	        }
	 }
	 
	
	private class MyTask extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
		
			connect();
			
			return null;
			
		}
		
		protected void onPostExecute(String result) {
			  
			//sendData("connected..");
			 if(connectflag){
			connectButton.setVisibility(View.GONE);
			tvtemp.setText("Connected.");
			 }
			}
			
		}
	
	@Override
	public void onDestroy() {
	    super.onDestroy(); 
	    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
	    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    startActivity(intent);
	    //System.exit(0);
	}
	
	
	
	 @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			//getMenuInflater().inflate(R.menu.login, menu);
		   	getMenuInflater().inflate(R.menu.wall_e, menu);
		     menu.add(1, 1, 0, "Disconnect");
		     menu.add(1, 2, 1, "Exit");
			return true;
		}
		
		@Override
	    public boolean onOptionsItemSelected(MenuItem item)
	    {
	    
	     switch(item.getItemId())
	     {
	     case 1:
	    	 closeSocket();
	    	 connectflag=false;
	    	 //conb.setVisibility(View.VISIBLE);
	    	// statusimg.setImageResource(R.drawable.disconnected);
	    	 tvtemp.setText("Disconnected.");
	    	 connectButton.setVisibility(View.VISIBLE);
	    	 //myrelativelayout.setBackgroundColor(Color.parseColor("#585858"));
	    	 //txtstatus.setText("Disconnected");
	    	 
	      return true;
	     case 2:
	    	 Intent intent = new Intent(getApplicationContext(), MainActivity.class);
	 	    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	 	    startActivity(intent);
	      return true;
	     case R.id.action_settings:
				Intent i = new Intent(this, UserSettingActivity.class);
				startActivityForResult(i, RESULT_SETTINGS);
				break;

	     }
	     return super.onOptionsItemSelected(item);

	    }
		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);

			switch (requestCode) {
			case RESULT_SETTINGS:
				showUserSettings();
				break;

			}

		}

		private void showUserSettings() {
			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(this);

			ipaddress=sharedPrefs.getString("prefIpaddress", "NULL");
			port=Integer.parseInt(sharedPrefs.getString("prefPort", "8050"));
			datastream=sharedPrefs.getBoolean("prefDataStream", true);
			accuracy=Integer.parseInt(sharedPrefs.getString("prefAccuracy", "3"));
			
			
			
		}
	
}
