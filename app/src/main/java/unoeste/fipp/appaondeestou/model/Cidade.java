package unoeste.fipp.appaondeestou.model;

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
