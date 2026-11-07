package unoeste.fipp.appaondeestou.model;

/** Município retornado pela API de localidades do IBGE. */
public class Cidade {
    private int id;
    private String nome;

    public int getId() { return id; }
    public String getNome() { return nome; }

    @Override
    public String toString() {
        return nome;
    }
}
