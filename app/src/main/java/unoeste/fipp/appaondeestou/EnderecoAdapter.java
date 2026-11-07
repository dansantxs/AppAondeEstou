package unoeste.fipp.appaondeestou;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import unoeste.fipp.appaondeestou.model.Endereco;

/** Adapter personalizado do ListView de resultados (layout item_endereco). */
public class EnderecoAdapter extends ArrayAdapter<Endereco> {
    private final LayoutInflater inflater;

    public EnderecoAdapter(@NonNull Context context, @NonNull List<Endereco> enderecos) {
        super(context, R.layout.item_endereco, enderecos);
        inflater = LayoutInflater.from(context);
    }

    private static class ViewHolder {
        TextView tvLogradouro, tvComplemento, tvBairro, tvCidade, tvCep;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_endereco, parent, false);
            h = new ViewHolder();
            h.tvLogradouro = convertView.findViewById(R.id.tvLogradouro);
            h.tvComplemento = convertView.findViewById(R.id.tvComplemento);
            h.tvBairro = convertView.findViewById(R.id.tvBairro);
            h.tvCidade = convertView.findViewById(R.id.tvCidade);
            h.tvCep = convertView.findViewById(R.id.tvCep);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        Endereco e = getItem(position);
        if (e != null) {
            h.tvLogradouro.setText(e.getLogradouro());
            h.tvComplemento.setVisibility(e.temComplemento() ? View.VISIBLE : View.GONE);
            h.tvComplemento.setText(e.getComplemento());
            String bairro = e.getBairro();
            h.tvBairro.setText(bairro == null || bairro.isEmpty() ? "—" : bairro);
            h.tvCidade.setText(e.getCidadeUf());
            h.tvCep.setText(getContext().getString(R.string.cep_formato, e.getCep()));
        }
        return convertView;
    }
}
