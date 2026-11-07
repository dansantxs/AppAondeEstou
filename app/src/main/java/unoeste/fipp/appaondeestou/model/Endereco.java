package unoeste.fipp.appaondeestou.model;

import java.io.Serializable;

/** Endereço retornado pela busca do ViaCEP. */
public class Endereco implements Serializable {
    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    private String localidade;
    private String uf;
    private String estado;
    private String ibge;
    private String ddd;

    public String getCep() { return cep; }
    public String getLogradouro() { return logradouro; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getLocalidade() { return localidade; }
    public String getUf() { return uf; }
    public String getEstado() { return estado; }
    public String getIbge() { return ibge; }
    public String getDdd() { return ddd; }

    public boolean temComplemento() {
        return complemento != null && !complemento.trim().isEmpty();
    }

    public String getCidadeUf() {
        return localidade + "/" + uf;
    }

    /** Ex.: "Avenida Brasil, Vila Furquim, Presidente Prudente/SP" */
    public String getEnderecoCompleto() {
        StringBuilder sb = new StringBuilder(logradouro);
        if (bairro != null && !bairro.isEmpty()) sb.append(", ").append(bairro);
        sb.append(", ").append(getCidadeUf());
        return sb.toString();
    }
}
