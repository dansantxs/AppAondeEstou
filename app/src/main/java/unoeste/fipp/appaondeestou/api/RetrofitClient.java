package unoeste.fipp.appaondeestou.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {
    private static IbgeService ibge;
    private static ViaCepService viaCep;
    private static LocationIqService locationIq;

    private RetrofitClient() { }

    private static Retrofit criar(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized IbgeService ibge() {
        if (ibge == null)
            ibge = criar("https://servicodados.ibge.gov.br/").create(IbgeService.class);
        return ibge;
    }

    public static synchronized ViaCepService viaCep() {
        if (viaCep == null)
            viaCep = criar("https://viacep.com.br/").create(ViaCepService.class);
        return viaCep;
    }

    public static synchronized LocationIqService locationIq() {
        if (locationIq == null)
            locationIq = criar("https://us1.locationiq.com/").create(LocationIqService.class);
        return locationIq;
    }
}
