package unoeste.fipp.appaondeestou.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import unoeste.fipp.appaondeestou.model.Cidade;
import unoeste.fipp.appaondeestou.model.Estado;

/** https://servicodados.ibge.gov.br/api/v1/localidades/ */
public interface IbgeService {
    @GET("api/v1/localidades/estados")
    Call<List<Estado>> listarEstados();

    @GET("api/v1/localidades/estados/{uf}/municipios")
    Call<List<Cidade>> listarMunicipios(@Path("uf") String uf, @Query("orderBy") String orderBy);
}
