package unoeste.fipp.appaondeestou;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unoeste.fipp.appaondeestou.api.RetrofitClient;
import unoeste.fipp.appaondeestou.model.Cidade;
import unoeste.fipp.appaondeestou.model.Endereco;
import unoeste.fipp.appaondeestou.model.Estado;

/** Fragment 1: seleção de UF/cidade e digitação da referência. */
public class BuscaFragment extends Fragment {
    private static final String UF_PADRAO = "SP";

    private Spinner spUf, spCidade;
    private TextInputLayout tilReferencia;
    private TextInputEditText etReferencia;
    private Button btBuscar;
    private ProgressBar pbCarregando;
    private TextView tvStatus;

    private ArrayAdapter<Estado> adapterUf;
    private ArrayAdapter<Cidade> adapterCidade;
    private AppViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_busca, container, false);
        spUf = view.findViewById(R.id.spUf);
        spCidade = view.findViewById(R.id.spCidade);
        tilReferencia = view.findViewById(R.id.tilReferencia);
        etReferencia = view.findViewById(R.id.etReferencia);
        btBuscar = view.findViewById(R.id.btBuscar);
        pbCarregando = view.findViewById(R.id.pbCarregando);
        tvStatus = view.findViewById(R.id.tvStatus);

        viewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        adapterUf = criarAdapter();
        adapterCidade = criarAdapter();
        spUf.setAdapter(adapterUf);
        spCidade.setAdapter(adapterCidade);
        spCidade.setEnabled(false);
        btBuscar.setEnabled(false);

        spUf.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                Estado estado = adapterUf.getItem(position);
                if (estado != null) carregarCidades(estado.getSigla());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btBuscar.setOnClickListener(v -> buscar());
        etReferencia.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (btBuscar.isEnabled()) buscar();
                return true;
            }
            return false;
        });

        carregarEstados();
        return view;
    }

    private <T> ArrayAdapter<T> criarAdapter() {
        ArrayAdapter<T> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private boolean ativo() {
        return isAdded() && getView() != null;
    }

    private void carregando(boolean sim, String mensagem) {
        pbCarregando.setVisibility(sim ? View.VISIBLE : View.GONE);
        tvStatus.setText(mensagem == null ? "" : mensagem);
    }

    private void carregarEstados() {
        carregando(true, getString(R.string.carregando_ufs));
        RetrofitClient.ibge().listarEstados().enqueue(new Callback<List<Estado>>() {
            @Override
            public void onResponse(@NonNull Call<List<Estado>> call, @NonNull Response<List<Estado>> response) {
                if (!ativo()) return;
                List<Estado> estados = response.body();
                if (!response.isSuccessful() || estados == null) {
                    carregando(false, getString(R.string.erro_ibge));
                    return;
                }
                estados.sort(Comparator.comparing(Estado::getSigla));
                adapterUf.clear();
                adapterUf.addAll(estados);
                carregando(false, null);
                for (int i = 0; i < estados.size(); i++)
                    if (UF_PADRAO.equals(estados.get(i).getSigla())) spUf.setSelection(i);
            }

            @Override
            public void onFailure(@NonNull Call<List<Estado>> call, @NonNull Throwable t) {
                if (ativo()) carregando(false, getString(R.string.erro_conexao, t.getMessage()));
            }
        });
    }

    private void carregarCidades(String uf) {
        spCidade.setEnabled(false);
        btBuscar.setEnabled(false);
        adapterCidade.clear();
        carregando(true, getString(R.string.carregando_cidades, uf));
        RetrofitClient.ibge().listarMunicipios(uf, "nome").enqueue(new Callback<List<Cidade>>() {
            @Override
            public void onResponse(@NonNull Call<List<Cidade>> call, @NonNull Response<List<Cidade>> response) {
                if (!ativo()) return;
                Estado atual = (Estado) spUf.getSelectedItem();
                if (atual == null || !uf.equals(atual.getSigla())) return; // UF trocada no meio
                List<Cidade> cidades = response.body();
                if (!response.isSuccessful() || cidades == null) {
                    carregando(false, getString(R.string.erro_ibge));
                    return;
                }
                adapterCidade.addAll(cidades);
                spCidade.setEnabled(true);
                btBuscar.setEnabled(true);
                carregando(false, null);
            }

            @Override
            public void onFailure(@NonNull Call<List<Cidade>> call, @NonNull Throwable t) {
                if (ativo()) carregando(false, getString(R.string.erro_conexao, t.getMessage()));
            }
        });
    }

    private void buscar() {
        Estado estado = (Estado) spUf.getSelectedItem();
        Cidade cidade = (Cidade) spCidade.getSelectedItem();
        String referencia = etReferencia.getText() == null ? "" : etReferencia.getText().toString().trim();
        if (estado == null || cidade == null) return;
        if (referencia.length() < 3) {
            tilReferencia.setError(getString(R.string.erro_referencia));
            return;
        }
        tilReferencia.setError(null);
        esconderTeclado();

        btBuscar.setEnabled(false);
        carregando(true, getString(R.string.buscando));
        String descricao = "\"" + referencia + "\" em " + cidade.getNome() + "/" + estado.getSigla();

        RetrofitClient.viaCep().buscar(estado.getSigla(), cidade.getNome(), referencia)
                .enqueue(new Callback<List<Endereco>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Endereco>> call,
                                           @NonNull Response<List<Endereco>> response) {
                        if (!ativo()) return;
                        btBuscar.setEnabled(true);
                        List<Endereco> lista = response.body();
                        if (!response.isSuccessful() || lista == null) {
                            carregando(false, getString(R.string.erro_viacep));
                            return;
                        }
                        if (lista.isEmpty()) {
                            carregando(false, getString(R.string.nenhum_resultado, descricao));
                            return;
                        }
                        carregando(false, getResources().getQuantityString(
                                R.plurals.qtd_resultados, lista.size(), lista.size()));
                        viewModel.setResultados(lista, descricao);
                        ((MainActivity) requireActivity()).irParaPagina(MainActivity.PAGINA_RESULTADOS);
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Endereco>> call, @NonNull Throwable t) {
                        if (!ativo()) return;
                        btBuscar.setEnabled(true);
                        carregando(false, getString(R.string.erro_conexao, t.getMessage()));
                    }
                });
    }

    private void esconderTeclado() {
        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etReferencia.getWindowToken(), 0);
        etReferencia.clearFocus();
    }
}
