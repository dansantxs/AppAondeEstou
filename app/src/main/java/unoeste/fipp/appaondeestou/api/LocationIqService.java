package unoeste.fipp.appaondeestou.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import unoeste.fipp.appaondeestou.model.LocationIqResult;

public interface LocationIqService {
    @GET("v1/search")
    Call<List<LocationIqResult>> buscar(@Query("key") String key,
                                        @Query("q") String consulta,
                                        @Query("countrycodes") String pais,
                                        @Query("format") String formato,
                                        @Query("limit") int limite);

    @GET("v1/search")
    Call<List<LocationIqResult>> buscarPorCep(@Query("key") String key,
                                              @Query("postalcode") String cep,
                                              @Query("countrycodes") String pais,
                                              @Query("format") String formato,
                                              @Query("limit") int limite);
}
