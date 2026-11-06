package unoeste.fipp.appaondeestou;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapaFragment extends Fragment {
    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mapa, container, false);
        mapView = view.findViewById(R.id.mapaId);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        // necessário para API superior à 13
        Configuration.getInstance().load(view.getContext(), PreferenceManager.getDefaultSharedPreferences(view.getContext()));
        //Cria um ponto de referência com base na latitude e longitude
        //GeoPoint pontoInicial = new GeoPoint(-22.132828, -51.402655);
        IMapController mapController = mapView.getController();
        //Faz zoom no mapa
        mapController.setZoom(15.0);
        //Centraliza o mapa no ponto de referência
        GeoPoint posicaoAtual=((MainActivity)getActivity()).getGeoPoint();
        mapController.setCenter(posicaoAtual);
        //Cria um marcador no mapa
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(posicaoAtual);
        startMarker.setTitle("Ponto Atual");
        //Posição do ícone
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(startMarker);
        return view;
    }

    @Override
    public void onResume() {
        //ponto adequado para reposicionar o apontador
        super.onResume();
    }
}