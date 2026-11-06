# Aonde Fica? 3.0

App Android que encontra locais a partir de referências (parte do nome de ruas, avenidas, praças ou estabelecimentos).

## Fluxo (3 fragments em um ViewPager2)

1. **Buscar** (`BuscaFragment`): seleciona UF e cidade (Spinners alimentados pela API do IBGE) e digita a referência (mín. 3 caracteres). A pesquisa usa o ViaCEP.
2. **Resultados** (`ResultadosFragment`): ListView com adapter personalizado (`EnderecoAdapter` / `item_endereco.xml`).
3. **Mapa** (`MapaFragment`): obtém a coordenada do endereço na LocationIQ e mostra no mapa (osmdroid) a posição atual, a posição do endereço, a linha reta entre eles, a distância (`calculaDistanciaEmKM`), CEP, bairro e DDD.

Os fragments compartilham dados pelo `AppViewModel`. As chamadas HTTP usam Retrofit + Gson (`api/`).

## APIs

- IBGE: `https://servicodados.ibge.gov.br/api/v1/localidades/estados` e `.../estados/{UF}/municipios`
- ViaCEP: `https://viacep.com.br/ws/{UF}/{cidade}/{referencia}/json/`
- LocationIQ: `https://us1.locationiq.com/v1/search`

## Configuração da LocationIQ

Crie uma conta gratuita em https://locationiq.com, copie o *Access Token* e adicione no `local.properties` (raiz do projeto, não versionado):

```
LOCATIONIQ_KEY=pk.sua_chave_aqui
```

Depois faça *Sync Project with Gradle Files*. Sem a chave, o app busca normalmente, mas o mapa avisa que não conseguiu localizar o endereço.
