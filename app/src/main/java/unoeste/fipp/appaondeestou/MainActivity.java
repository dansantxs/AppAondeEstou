package unoeste.fipp.appaondeestou;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;

public class MainActivity extends AppCompatActivity {
    public static final int PAGINA_BUSCA = 0;
    public static final int PAGINA_RESULTADOS = 1;
    public static final int PAGINA_MAPA = 2;
    private static final int REQ_PERMISSOES = 2;

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;
    private final PermissionsMarshmallow permissionsMashmallow = new PermissionsMarshmallow(this);
    private final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private AppViewModel viewModel;
    private LocationManager locationManager;
    private final LocationListener locationListener = location ->
            viewModel.setLocalizacaoAtual(new GeoPoint(location.getLatitude(), location.getLongitude()));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // necessário para API superior à 13
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        // instanciando o adapter para o viewPager
        viewPager.setAdapter(new ViewPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        //navegando entre os fragments
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_busca) { viewPager.setCurrentItem(PAGINA_BUSCA); return true; }
            if (id == R.id.nav_resultados) { viewPager.setCurrentItem(PAGINA_RESULTADOS); return true; }
            if (id == R.id.nav_mapa) { viewPager.setCurrentItem(PAGINA_MAPA); return true; }
            return false;
        });
        //selecionando os buttons correspondentes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == PAGINA_BUSCA) bottomNavigation.setSelectedItemId(R.id.nav_busca);
                else if (position == PAGINA_RESULTADOS) bottomNavigation.setSelectedItemId(R.id.nav_resultados);
                else bottomNavigation.setSelectedItemId(R.id.nav_mapa);
                viewPager.setUserInputEnabled(position != PAGINA_MAPA);
            }
        });

        if (!temPermissaoLocalizacao())
            permissionsMashmallow.requestPermissions(PERMISSIONS, REQ_PERMISSOES);
    }

    public void irParaPagina(int pagina) {
        viewPager.setCurrentItem(pagina, true);
    }

    private boolean temPermissaoLocalizacao() {
        return permissionsMashmallow.hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                || permissionsMashmallow.hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSOES) iniciarGPS();
    }

    @Override
    protected void onResume() {
        super.onResume();
        iniciarGPS();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private void iniciarGPS() {
        if (!temPermissaoLocalizacao()) return;
        String[] provedores = {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER};
        for (String provedor : provedores) {
            try {
                if (!locationManager.isProviderEnabled(provedor)) continue;
                locationManager.requestLocationUpdates(provedor, 2000, 5, locationListener);
                Location ultima = locationManager.getLastKnownLocation(provedor);
                if (ultima != null && viewModel.getLocalizacaoAtual().getValue() == null)
                    locationListener.onLocationChanged(ultima);
            } catch (SecurityException | IllegalArgumentException e) {
            }
        }
    }
}
