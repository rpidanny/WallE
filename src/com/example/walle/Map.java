package com.example.walle;






import java.util.regex.Pattern;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.util.Linkify;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


public class Map extends FragmentActivity implements OnMapClickListener, OnMapLongClickListener, OnMarkerDragListener{

	private static GoogleMap map1;
	TextView txt;
	
	
	 boolean markerClicked;
	 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
       	 
       	 TextView error=new TextView(this);
            error.setTextSize(20);
            error.setText("\n\n\n\n\nGoogle Play Services not found. \n\nPlease Install Google Play Services\n\n\n\n\n\n\n\n\n#Download Google Play Services.");
           
            Pattern wikiWordMatcher = Pattern.compile("#Download Google Play Services.");
            String wikiViewURL =    "https://play.google.com/store/apps/details?id=com.google.android.gms";
            Linkify.addLinks(error, wikiWordMatcher, wikiViewURL);
            setContentView(error);
           
        }
        else
        {
       	 
       		 
           	 setContentView(R.layout.activity_map);
           		
           	 txt=(TextView)findViewById(R.id.textView1);
           	 txt.setTextColor(Color.parseColor("#FFFFFF"));
           	 txt.setText(Double.toString(WallE.destLat) + "," + Double.toString(WallE.destlong));
           	 
           		map1  = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
           		map1.setMapType(GoogleMap.MAP_TYPE_HYBRID);
           		CameraUpdate update1 = CameraUpdateFactory.newLatLngZoom(new LatLng(WallE.destLat,WallE.destlong), 17);
           		map1.setMyLocationEnabled(true);
           		map1.getUiSettings().setCompassEnabled(true);
           		map1.getUiSettings().setMyLocationButtonEnabled(true);
           		map1.setOnMapClickListener(this);
           		map1.setOnMapLongClickListener(this);
           		map1.setOnMarkerDragListener(this);
           		
           		@SuppressWarnings("unused")
				final Marker marker = map1.addMarker(new MarkerOptions()
                .position(new LatLng(WallE.destLat,WallE.destlong))
                .title("Wall E")
                .snippet("Drag marker").draggable(true));
           		
           		map1.animateCamera(update1);
           		
           		markerClicked = false;
           
        }
    }
	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub
		LatLng temp=null;
 		// TODO Auto-generated method stub
		temp=arg0.getPosition();
		//marker.setPosition(temp);
		txt.setText(Double.toString(temp.latitude) + "," + Double.toString(temp.longitude));
	}
	@Override
	public void onMarkerDragEnd(Marker arg0) {
		
		LatLng temp=null;
 		// TODO Auto-generated method stub
		temp=arg0.getPosition();
		//marker.setPosition(temp);
		txt.setText(Double.toString(temp.latitude) + "," + Double.toString(temp.longitude));
		WallE.destLat=temp.latitude;
		WallE.destlong=temp.longitude;
	}
	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub
		LatLng temp=null;
 		// TODO Auto-generated method stub
		temp=arg0.getPosition();
		//marker.setPosition(temp);
		txt.setText(Double.toString(temp.latitude) + "," + Double.toString(temp.longitude));
	}
	@Override
	public void onMapLongClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}
}