package unoeste.fipp.appaondeestou.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import unoeste.fipp.appaondeestou.model.Endereco;

public interface ViaCepService {
    @GET("ws/{uf}/{cidade}/{referencia}/json/")
    Call<List<Endereco>> buscar(@Path("uf") String uf,
                                @Path("cidade") String cidade,
                                @Path("referencia") String referencia);
}
