package unoeste.fipp.appaondeestou;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;


public class GpsFragment extends Fragment {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView tvLatitude, tvLongitude, tvAltitude;
    private Button btStartStop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_gps, container, false);

        tvLatitude=view.findViewById(R.id.tvLatitude);
        tvLongitude=view.findViewById(R.id.tvLongitude);
        tvAltitude=view.findViewById(R.id.tvAltitude);
        btStartStop=view.findViewById(R.id.btStartStop);

        locationManager=(LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                tvAltitude.setText(String.format("%.8f",location.getAltitude()));
                tvLatitude.setText(String.format("%.8f",location.getLatitude()));
                tvLongitude.setText(String.format("%.8f",location.getLongitude()));
                GeoPoint geoPoint=new GeoPoint(location.getLatitude(),location.getLongitude());
                ((MainActivity)getActivity()).setGeoPoint(geoPoint);
            }
        };
        btStartStop.setOnClickListener(e->{
            if(btStartStop.getText().toString().equals("Start GPS")) {
                iniciarGPS();
                btStartStop.setText("Stop GPS");
            }
            else{
                pararGPS();
                btStartStop.setText("Start GPS");
            }
        });
        return view;
    }

    private void pararGPS() {
        locationManager.removeUpdates(locationListener);
    }

    private void iniciarGPS() {

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
    }
}