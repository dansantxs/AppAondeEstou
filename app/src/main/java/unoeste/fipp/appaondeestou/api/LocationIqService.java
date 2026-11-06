package unoeste.fipp.appaondeestou.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import unoeste.fipp.appaondeestou.model.LocationIqResult;

/** https://us1.locationiq.com/v1/search (forward geocoding) */
public interface LocationIqService {
    /** Busca livre: "Avenida Brasil, Presidente Prudente, SP, Brasil" */
    @GET("v1/search")
    Call<List<LocationIqResult>> buscar(@Query("key") String key,
                                        @Query("q") String consulta,
                                        @Query("countrycodes") String pais,
                                        @Query("format") String formato,
                                        @Query("limit") int limite);

    /** Busca estruturada pelo CEP (usada como alternativa quando a livre falha) */
    @GET("v1/search")
    Call<List<LocationIqResult>> buscarPorCep(@Query("key") String key,
                                              @Query("postalcode") String cep,
                                              @Query("countrycodes") String pais,
                                              @Query("format") String formato,
                                              @Query("limit") int limite);
}
