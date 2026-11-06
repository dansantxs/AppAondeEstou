package unoeste.fipp.appaondeestou;

import android.Manifest;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.osmdroid.util.GeoPoint;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;
    private PermissionsMarshmallow permissionsMashmallow = new PermissionsMarshmallow(this);
    private String[] PERMISSIONS = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private GeoPoint geoPoint=null;

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager=findViewById(R.id.viewPager);
        bottomNavigation=findViewById(R.id.bottomNavigation);
        // instanciando o adapter para o viewPager
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        //navegando entre os fragments
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_gps) { viewPager.setCurrentItem(0); return true; }
                if (id == R.id.nav_mapa) { viewPager.setCurrentItem(1); return true; }
                if (id == R.id.nav_creditos) { viewPager.setCurrentItem(2); return true; }
                return false;
            }
        });
        //selecionando os buttons correspondentes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) bottomNavigation.setSelectedItemId(R.id.nav_gps);
                else if (position == 1) bottomNavigation.setSelectedItemId(R.id.nav_mapa);
                else if (position == 2) bottomNavigation.setSelectedItemId(R.id.nav_creditos);
            }
        });
        CheckPermissionGranted();
    }

    private void CheckPermissionGranted() {
        if (permissionsMashmallow.hasPermissions(PERMISSIONS)) {
            //  permission granted
        } else {
            // request permission
            permissionsMashmallow.requestPermissions(PERMISSIONS, 2);
        }
    }
}