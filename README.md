# Viajá

O Viajá é um aplicativo Android para planejar viagens de lazer ou trabalho. Você pode pesquisar o que visitar em uma cidade, guardar os lugares que mais interessam e organizar as visitas no roteiro.

O app foi desenvolvido como Projeto Integrador do Senac. O app é escrito em Kotlin, com telas em XML. O catálogo, os lugares salvos e as viagens ficam na [API do Viajá](https://github.com/ProjetoIntegrador-4Semestre/viaja-api-spring).

## O que dá para fazer no app

- **Explorar destinos.** Consulte as cidades do catálogo, pesquise lugares por nome, endereço ou cidade e abra a ficha de cada local para ver mais detalhes.
- **Guardar lugares.** Salve locais de interesse para voltar a eles quando estiver planejando uma viagem.
- **Montar um roteiro.** Crie uma viagem, adicione lugares ao roteiro e acompanhe as paradas por dia. Em cada parada, você pode definir um horário e marcar a visita como concluída.
- **Acessar sua conta.** Cadastre-se ou entre para consultar seus lugares salvos e retomar suas viagens ao reabrir o app.

O app também tem um fluxo de assinatura Premium para testes. Ele abre o pagamento e o gerenciamento da assinatura no navegador e consulta a API para atualizar o status da conta.

## Como rodar no Android Studio

Abra este repositório no Android Studio, instale o SDK do Android 37 e aguarde a sincronização do Gradle. O app roda em dispositivos com Android 7.0 (API 24) ou superior.

Para exibir o mapa nativo, ative o Maps SDK for Android, crie uma chave restrita ao application ID `com.senac.viaja` e ao certificado de assinatura usado no build, e salve-a fora do Git em `secrets.properties`:

```properties
MAPS_API_KEY=sua-chave-android
```

A chave Android é diferente da chave de servidor usada pela API para consultar Places.

A configuração padrão usa a API hospedada. Para testar o app com o backend da sua máquina, siga o [README da API](https://github.com/ProjetoIntegrador-4Semestre/viaja-api-spring) e deixe o servidor rodando na porta 18080. Com o emulador iniciado, execute na raiz deste repositório:

```sh
adb reverse tcp:18080 tcp:18080
./gradlew :app:installDebug -PapiBaseUrl=http://127.0.0.1:18080/
```

Abra o app no emulador e entre com a conta de demonstração local, `rafael@email.com`, e a senha `senha123`.

O [índice da documentação](docs/README.md) reúne a proposta do produto, a arquitetura e o material da entrega acadêmica.
