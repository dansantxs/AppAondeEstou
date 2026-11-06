package unoeste.fipp.appaondeestou;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unoeste.fipp.appaondeestou.api.RetrofitClient;
import unoeste.fipp.appaondeestou.model.Endereco;
import unoeste.fipp.appaondeestou.model.LocationIqResult;

/**
 * Dados compartilhados entre os 3 fragments (vive enquanto a Activity existir,
 * inclusive em rotações de tela).
 */
public class AppViewModel extends ViewModel {
    private final MutableLiveData<List<Endereco>> resultados = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> descricaoBusca = new MutableLiveData<>("");
    private final MutableLiveData<Endereco> selecionado = new MutableLiveData<>();
    private final MutableLiveData<GeoPoint> destino = new MutableLiveData<>();
    private final MutableLiveData<GeoPoint> localizacaoAtual = new MutableLiveData<>();
    private final MutableLiveData<Boolean> geocodificando = new MutableLiveData<>(false);
    private final MutableLiveData<String> erroGeocodificacao = new MutableLiveData<>();

    public LiveData<List<Endereco>> getResultados() { return resultados; }
    public LiveData<String> getDescricaoBusca() { return descricaoBusca; }
    public LiveData<Endereco> getSelecionado() { return selecionado; }
    public LiveData<GeoPoint> getDestino() { return destino; }
    public LiveData<GeoPoint> getLocalizacaoAtual() { return localizacaoAtual; }
    public LiveData<Boolean> getGeocodificando() { return geocodificando; }
    public LiveData<String> getErroGeocodificacao() { return erroGeocodificacao; }

    public void setResultados(List<Endereco> lista, String descricao) {
        descricaoBusca.setValue(descricao); // antes da lista: o observer da lista lê a descrição
        resultados.setValue(lista);
    }

    public void setLocalizacaoAtual(GeoPoint ponto) {
        localizacaoAtual.setValue(ponto);
    }

    /** Seleciona um resultado e busca suas coordenadas na LocationIQ. */
    public void selecionar(@NonNull Endereco endereco) {
        selecionado.setValue(endereco);
        destino.setValue(null);
        erroGeocodificacao.setValue(null);

        String key = BuildConfig.LOCATIONIQ_KEY;
        if (key == null || key.isEmpty()) {
            erroGeocodificacao.setValue("Chave da LocationIQ não configurada. "
                    + "Adicione LOCATIONIQ_KEY=sua_chave no local.properties.");
            return;
        }

        geocodificando.setValue(true);
        String consulta = endereco.getLogradouro() + ", " + endereco.getLocalidade()
                + ", " + endereco.getUf() + ", Brasil";
        RetrofitClient.locationIq()
                .buscar(key, consulta, "br", "json", 1)
                .enqueue(new Callback<List<LocationIqResult>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<LocationIqResult>> call,
                                           @NonNull Response<List<LocationIqResult>> response) {
                        if (endereco != selecionado.getValue()) return; // seleção mudou
                        if (!aplicarResultado(response)) buscarPorCep(key, endereco);
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<LocationIqResult>> call, @NonNull Throwable t) {
                        if (endereco != selecionado.getValue()) return;
                        buscarPorCep(key, endereco);
                    }
                });
    }

    /** Alternativa: geocodifica apenas pelo CEP quando o logradouro não é encontrado. */
    private void buscarPorCep(String key, Endereco endereco) {
        RetrofitClient.locationIq()
                .buscarPorCep(key, endereco.getCep(), "br", "json", 1)
                .enqueue(new Callback<List<LocationIqResult>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<LocationIqResult>> call,
                                           @NonNull Response<List<LocationIqResult>> response) {
                        if (endereco != selecionado.getValue()) return;
                        if (!aplicarResultado(response)) {
                            falhar(response.code() == 401
                                    ? "Chave da LocationIQ inválida."
                                    : "Não foi possível localizar este endereço no mapa.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<LocationIqResult>> call, @NonNull Throwable t) {
                        if (endereco != selecionado.getValue()) return;
                        falhar("Falha de conexão com a LocationIQ: " + t.getMessage());
                    }
                });
    }

    private boolean aplicarResultado(Response<List<LocationIqResult>> response) {
        List<LocationIqResult> lista = response.body();
        if (!response.isSuccessful() || lista == null || lista.isEmpty()) return false;
        try {
            LocationIqResult r = lista.get(0);
            destino.setValue(new GeoPoint(r.getLatitude(), r.getLongitude()));
            geocodificando.setValue(false);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void falhar(String mensagem) {
        geocodificando.setValue(false);
        erroGeocodificacao.setValue(mensagem);
    }
}
