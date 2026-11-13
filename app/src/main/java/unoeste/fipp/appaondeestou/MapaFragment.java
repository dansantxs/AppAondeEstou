package unoeste.fipp.appaondeestou;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.Arrays;
import java.util.Locale;

import unoeste.fipp.appaondeestou.model.Endereco;

public class MapaFragment extends Fragment {
    private static final GeoPoint CENTRO_PADRAO = new GeoPoint(-22.1256, -51.3889);

    private MapView mapView;
    private Marker markerAtual, markerDestino;
    private Polyline linha;
    private TextView tvTitulo, tvDetalhes, tvCoordDestino, tvCoordAtual, tvDistancia, tvStatus;
    private ProgressBar pbGeocodificando;

    private AppViewModel viewModel;
    private int enquadrado = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mapa, container, false);
        mapView = view.findViewById(R.id.mapaId);
        tvTitulo = view.findViewById(R.id.tvTitulo);
        tvDetalhes = view.findViewById(R.id.tvDetalhes);
        tvCoordDestino = view.findViewById(R.id.tvCoordDestino);
        tvCoordAtual = view.findViewById(R.id.tvCoordAtual);
        tvDistancia = view.findViewById(R.id.tvDistancia);
        tvStatus = view.findViewById(R.id.tvStatus);
        pbGeocodificando = view.findViewById(R.id.pbGeocodificando);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        //Faz zoom no mapa
        mapView.getController().setZoom(14.0);
        //Centraliza o mapa no ponto de referência
        mapView.getController().setCenter(CENTRO_PADRAO);

        //Cria um marcador no mapa
        markerAtual = new Marker(mapView);
        markerAtual.setTitle(getString(R.string.voce_esta_aqui));
        markerAtual.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_posicao_atual));
        //Posição do ícone
        markerAtual.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        markerDestino = new Marker(mapView);
        markerDestino.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        linha = new Polyline(mapView);
        linha.getOutlinePaint().setColor(Color.parseColor("#D32F2F"));
        linha.getOutlinePaint().setStrokeWidth(8f);

        viewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        viewModel.getSelecionado().observe(getViewLifecycleOwner(), e -> {
            enquadrado = 0;
            mostrarEndereco(e);
            atualizar();
        });
        viewModel.getDestino().observe(getViewLifecycleOwner(), p -> {
            enquadrado = 0;
            atualizar();
        });
        viewModel.getLocalizacaoAtual().observe(getViewLifecycleOwner(), p -> atualizar());
        viewModel.getGeocodificando().observe(getViewLifecycleOwner(), sim ->
                pbGeocodificando.setVisibility(Boolean.TRUE.equals(sim) ? View.VISIBLE : View.GONE));
        viewModel.getErroGeocodificacao().observe(getViewLifecycleOwner(), erro -> {
            tvStatus.setVisibility(erro == null ? View.GONE : View.VISIBLE);
            tvStatus.setText(erro);
        });
        return view;
    }

    private void mostrarEndereco(Endereco e) {
        if (e == null) {
            tvTitulo.setText(R.string.selecione_resultado);
            tvDetalhes.setVisibility(View.GONE);
            return;
        }
        tvTitulo.setText(e.getLogradouro());
        StringBuilder sb = new StringBuilder();
        if (e.temComplemento()) sb.append(e.getComplemento()).append('\n');
        if (e.getBairro() != null && !e.getBairro().isEmpty()) sb.append(e.getBairro()).append(" · ");
        sb.append(e.getCidadeUf()).append('\n');
        sb.append(getString(R.string.cep_formato, e.getCep()));
        if (e.getDdd() != null && !e.getDdd().isEmpty()) sb.append("  ·  DDD ").append(e.getDdd());
        tvDetalhes.setText(sb.toString());
        tvDetalhes.setVisibility(View.VISIBLE);
        markerDestino.setTitle(e.getLogradouro());
        markerDestino.setSnippet(e.getEnderecoCompleto() + " - CEP " + e.getCep());
    }

    private void atualizar() {
        GeoPoint atual = viewModel.getLocalizacaoAtual().getValue();
        GeoPoint destino = viewModel.getDestino().getValue();

        mapView.getOverlays().clear();
        if (atual != null) {
            markerAtual.setPosition(atual);
            mapView.getOverlays().add(markerAtual);
            tvCoordAtual.setText(getString(R.string.coord_atual, formatar(atual)));
        } else {
            tvCoordAtual.setText(R.string.aguardando_gps);
        }

        if (destino != null) {
            markerDestino.setPosition(destino);
            mapView.getOverlays().add(markerDestino);
            tvCoordDestino.setText(getString(R.string.coord_destino, formatar(destino)));
            tvCoordDestino.setVisibility(View.VISIBLE);
        } else {
            tvCoordDestino.setVisibility(View.GONE);
        }

        if (atual != null && destino != null) {
            linha.setPoints(Arrays.asList(atual, destino));
            mapView.getOverlays().add(0, linha);
            double km = calculaDistanciaEmKM(atual.getLatitude(), atual.getLongitude(),
                    destino.getLatitude(), destino.getLongitude());
            tvDistancia.setText(getString(R.string.distancia, formatarDistancia(km)));
            tvDistancia.setVisibility(View.VISIBLE);
        } else {
            tvDistancia.setVisibility(View.GONE);
        }

        enquadrar(atual, destino);
        mapView.invalidate();
    }

    private void enquadrar(GeoPoint atual, GeoPoint destino) {
        int estado = (atual != null ? 1 : 0) + (destino != null ? 2 : 0);
        if (estado == 0 || estado == enquadrado) return;
        enquadrado = estado;
        if (estado == 3) {
            BoundingBox caixa = BoundingBox.fromGeoPoints(Arrays.asList(atual, destino));
            mapView.post(() -> {
                if (atual.distanceToAsDouble(destino) < 50) {
                    mapView.getController().setZoom(17.0);
                    mapView.getController().setCenter(destino);
                } else {
                    mapView.zoomToBoundingBox(caixa.increaseByScale(1.3f), true, 80);
                }
            });
        } else if (estado == 2) {
            mapView.getController().animateTo(destino, 16.0, 800L);
        } else {
            mapView.getController().animateTo(atual, 15.0, 800L);
        }
    }

    public double calculaDistanciaEmKM(double lat1, double lon1, double lat2, double lon2) {
        Location start = new Location("Start Point");
        start.setLatitude(lat1);
        start.setLongitude(lon1);
        final Location finish = new Location("Finish Point");
        finish.setLatitude(lat2);
        finish.setLongitude(lon2);
        final float distance = start.distanceTo(finish);
        return distance / 1000;
    }

    private static String formatar(GeoPoint p) {
        return String.format(Locale.US, "%.6f, %.6f", p.getLatitude(), p.getLongitude());
    }

    private static String formatarDistancia(double km) {
        if (km < 1) return String.format(Locale.getDefault(), "%.0f m", km * 1000);
        return String.format(Locale.getDefault(), "%.2f km", km);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
