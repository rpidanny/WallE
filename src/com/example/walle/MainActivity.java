package com.example.walle;





import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		new MyTask().execute();
	}

	private class MyTask extends AsyncTask<String,String,String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			
	    
			try {
				Thread.sleep(6000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//Toast.makeText(MainActivity.this,"Cannot delay", Toast.LENGTH_LONG).show();
			} 
			return null;
			//MyTask.cancel(true);
		}
		
		protected void onPostExecute(String result) {
			//Toast.makeText(MainActivity.this,"Login Activity", Toast.LENGTH_LONG).show();
			Intent login= new Intent(MainActivity.this,WallE.class);
			startActivity(login);
		  }
		
	}
	
	
	public void onDestroy() {
	    super.onDestroy();
	    
	    System.exit(0);
	}

}
