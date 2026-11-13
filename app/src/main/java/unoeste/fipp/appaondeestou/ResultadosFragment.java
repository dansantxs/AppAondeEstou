package unoeste.fipp.appaondeestou;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import unoeste.fipp.appaondeestou.model.Endereco;

public class ResultadosFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_resultados, container, false);
        TextView tvResumo = view.findViewById(R.id.tvResumo);
        ListView lvResultados = view.findViewById(R.id.lvResultados);
        lvResultados.setEmptyView(view.findViewById(R.id.tvVazio));

        AppViewModel viewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        EnderecoAdapter adapter = new EnderecoAdapter(requireContext(), new ArrayList<>());
        lvResultados.setAdapter(adapter);

        viewModel.getResultados().observe(getViewLifecycleOwner(), lista -> {
            adapter.clear();
            adapter.addAll(lista);
            String descricao = viewModel.getDescricaoBusca().getValue();
            tvResumo.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
            tvResumo.setText(getResources().getQuantityString(R.plurals.resumo_resultados,
                    lista.size(), lista.size(), descricao));
            lvResultados.setSelection(0);
        });

        lvResultados.setOnItemClickListener((parent, v, position, id) -> {
            Endereco endereco = adapter.getItem(position);
            if (endereco == null) return;
            viewModel.selecionar(endereco);
            ((MainActivity) requireActivity()).irParaPagina(MainActivity.PAGINA_MAPA);
        });
        return view;
    }
}
