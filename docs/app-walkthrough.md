# Como o Viajá funciona por dentro

O Viajá é um app Android em Kotlin, com telas em XML na maior parte dos fluxos. Uma forma simples de apresentar o projeto é acompanhar o que acontece quando alguém pesquisa um destino, adiciona um lugar ao roteiro e consulta a própria assinatura. Os três exemplos usam a mesma base de comunicação com o servidor.

## Da tela até o banco

O Android conversa com a API por HTTP. O Spring Boot recebe o pedido, identifica a conta quando necessário, aplica as regras do recurso e consulta ou grava os dados usando Spring Data JPA. No ambiente local padrão, o banco é H2 em memória. Ele recebe dados de demonstração ao iniciar. No perfil `stripe-sandbox`, o H2 grava em arquivo no diretório `data/`.

```text
Tela → ViewModel, quando usado → Repository → Retrofit → API Spring → Banco
Tela ← estado ou resultado     ← Repository ← JSON     ← API Spring
```

A `Activity` cuida da tela e reage a cliques. O `ViewModel` guarda dados e estados da tela, como carregamento e erro, e os publica via `LiveData`. A Activity observa esses valores para atualizar a interface. Em telas como o roteiro e o Premium, a Activity chama o repositório diretamente e atualiza a interface pelos callbacks.

Os repositórios organizam as operações por assunto. Por exemplo, `PlaceRepository` pesquisa lugares e carrega detalhes, enquanto `TripRepository` lida com viagens e paradas. Eles chamam a rota certa, interpretam a resposta e entregam dados ou uma mensagem de erro à camada da tela. Do lado da API, um repositório JPA tem outro papel. Ele acessa os registros no banco.

## Quais telas usam dados da API

| Tela | O que consulta ou grava |
| --- | --- |
| Login e cadastro | Enviam os dados a `/auth/login` e `/auth/signup`. O login recupera a conta e o token da sessão. |
| Início | Consulta viagens, paradas e lugares salvos para compor o painel. Também consulta candidatos do catálogo para exibir lugares próximos quando tem acesso à localização. |
| Explorar | Consulta as cidades em `GET /cities`. Ao buscar ou tocar numa cidade, abre a lista de lugares com o termo escolhido. |
| Resultados e detalhe | A lista chama `GET /places/search?q=...`. A ficha busca `GET /places/{id}` e permite salvar ou remover o lugar dos favoritos. |
| Salvos | Consulta os favoritos em `/users/{userId}/saved-locals`. Ao tocar em um item, abre a ficha pelo ID do lugar. |
| Viagens | Lista as viagens da conta e consulta as cidades para preencher o seletor do formulário. Ao confirmar, envia a nova viagem para `POST /trips`. |
| Painel da viagem e roteiro | Consultam a viagem e suas paradas. O roteiro agrupa as paradas por dia e permite alterar horário e conclusão. O fluxo de inclusão pela ficha do lugar adiciona a visita ao primeiro dia. |
| Perfil | Consulta `GET /users/{id}` e permite encerrar a sessão local. A gravação pelo formulário de edição ainda está indisponível no Android. |
| Premium | Consulta o estado da assinatura na API e pede a URL de checkout ou do portal de gerenciamento. |

A sessão fica em `SharedPreferences` no Android para ser restaurada ao reabrir o app. O catálogo e os roteiros são carregados pela API. As telas de recuperação de senha e o chat de viagem são exemplos de interface que ainda têm comportamento local ou incompleto.

## Retrofit, Gson e sessão

Em `ApiClient.kt`, a interface `ApiService` descreve como chamar a API. `@GET`, `@POST`, `@PUT` e `@DELETE` indicam a operação HTTP. `@Path` preenche uma parte do caminho, como o ID em `places/{id}`. `@Query` adiciona um parâmetro à URL, como `q` na busca. `@Body` envia um objeto no corpo do pedido.

O Retrofit monta a requisição a partir dessas declarações e da URL base da versão do app. O OkHttp envia a requisição pela rede. O Gson converte objetos Kotlin para JSON ao enviar dados e converte as respostas JSON de volta para objetos Kotlin. Assim, `Call<List<Place>>` descreve um pedido que espera receber uma lista de lugares.

O repositório executa o pedido com `enqueue`, de forma assíncrona. Em `onResponse`, ele recebe uma resposta HTTP e verifica se o código e o corpo indicam sucesso. Em `onFailure`, trata falhas da chamada, como falta de conexão. Isso permite mostrar carregamento enquanto a resposta está a caminho e exibir uma mensagem se houver erro.

No login, `AuthRepository` lê o JWT no cabeçalho `Authorization` e guarda a sessão. Nas chamadas seguintes, um interceptor do OkHttp adiciona `Authorization: Bearer <token>` quando há token disponível. A API usa o JWT para identificar quem fez a chamada e confere o dono dos recursos privados. Quando uma chamada com token retorna 401, o app pode invalidar a sessão associada a esse token.

## Como uma lista vira cartões na tela

Na tela Explorar, o `CityViewModel` pede as cidades ao `CityRepository`. O servidor responde em páginas. O repositório extrai `_embedded.cityList` de cada resposta e reúne as páginas. A tela observa a lista e a entrega ao `CityAdapter`. Ao tocar numa cidade, um `Intent` passa seu nome para `PlaceListActivity`.

Na tela de resultados, o `PlaceViewModel` chama `PlaceRepository`, que executa `GET /places/search?q=...`. A resposta já vem como uma lista de `Place`. O ViewModel publica a lista e a Activity cria um `PlaceAdapter` com os dados recebidos.

O `RecyclerView` exibe a lista, e o `LinearLayoutManager` posiciona os cartões em sequência. O adapter responde a três perguntas:

1. **Quantos itens existem?** `getItemCount` devolve o tamanho da lista.
2. **Como criar um cartão?** `onCreateViewHolder` carrega o XML `item_place_card` e cria um `ViewHolder`, que guarda referências para os campos do cartão.
3. **Quais dados entram neste cartão?** `onBindViewHolder` pega o objeto da posição, preenche nome, endereço, nota e preço, e liga o clique ao lugar certo. O preço recebido em centavos é formatado para exibição em reais.

Ao rolar, o RecyclerView reaproveita cartões que saíram da tela e pede ao adapter que preencha esses cartões com outros dados. O clique passa o `place.id` em um `Intent` para `PlaceDetailsActivity`, que consulta o detalhe atualizado. `SavedAdapter` e `TripAdapter` usam a mesma ideia para favoritos e viagens. Já o roteiro monta grupos e linhas por dia diretamente na Activity.

## Como os objetos são criados e enviados

Os modelos ficam principalmente em `data/model/Model.kt`. Uma `data class` descreve quais campos compõem um objeto e quais tipos o app espera receber. Há modelos de envio, de resposta e de coleções paginadas.

- **Criar uma viagem.** A tela lê o nome, o modo e a cidade selecionada. O `TripViewModel` cria um `TripRequest` e o passa ao `TripRepository`. Na versão atual, o formulário deixa as datas e o orçamento em branco. O Retrofit envia o objeto em `POST /trips`. O servidor atribui a viagem à conta identificada pelo JWT e devolve um `TripResponse`. Após o sucesso, o ViewModel recarrega a lista.
- **Editar uma parada.** A tela cria um `StopSchedule` com o horário e o estado de conclusão. Por exemplo, `StopSchedule("09:30", false)` representa uma visita às 9h30 ainda pendente. A rota `PUT /trip-locals/{tripId}/{localId}/schedule` usa os dois IDs para localizar a parada. O horário pode ser removido e a interface recarrega o roteiro depois que a API confirma a gravação.
- **Ler coleções.** O Gson monta um `Place` para cada item da busca. Em listas paginadas, ele monta primeiro o objeto da página. Por exemplo, `CityPage` usa `@SerializedName("_embedded")` para mapear o nome do JSON para o campo Kotlin `embedded`. O repositório extrai a lista e usa `totalPages` para saber quando terminou.

A distinção ajuda na apresentação. `TripRequest` representa os dados enviados para pedir a criação. `TripResponse` representa o que o servidor devolve, incluindo campos como ID e status. A entidade JPA que o Spring persiste no banco fica no servidor.

## Stripe, do toque em assinar ao status Premium

### 1. Abrir o checkout

Ao abrir a área Premium, `PremiumActivity` verifica a sessão e chama `BillingRepository.subscription()`, que consulta `GET /billing/stripe/subscription`. Ao confirmar o plano, o app chama `POST /billing/stripe/checkout` com o JWT da conta.

O servidor verifica se o Payment Link de teste está ativo e usa o preço mensal configurado. Se já houver uma assinatura que precisa ser gerenciada, retorna um conflito. Caso contrário, guarda uma referência temporária vinculada à conta e a inclui como `client_reference_id` na URL devolvida ao app. Essa referência permite identificar a conta Viajá quando o Stripe avisar sobre o checkout.

O Android recebe um `StripeHostedUrl`, verifica se é HTTPS e aponta para `buy.stripe.com`, e abre a URL no navegador. O Stripe recebe os dados de pagamento na própria página. Na demonstração, abra o link pelo app e informe no checkout o mesmo e-mail usado na conta Viajá. As credenciais do Stripe ficam na API.

### 2. Confirmar o pagamento no servidor

O webhook é uma chamada que o Stripe faz diretamente à API. Os eventos chegam a `POST /billing/stripe/webhook`. A API verifica a assinatura da mensagem, confirma que o evento veio do ambiente de testes e identifica a conta. No primeiro checkout, ela confere a referência e o e-mail. Depois, consulta a assinatura no Stripe e valida o cliente, o preço e a fatura.

Quando encontra uma fatura paga para o preço esperado, a API registra até quando o período está pago. Ela guarda também os IDs de cliente e assinatura, o status e a indicação de cancelamento no fim do período. Os eventos processados ficam registrados por ID para que uma entrega repetida do Stripe não seja processada de novo. Eventos de renovação, falha de pagamento e cancelamento disparam uma nova consulta ao Stripe.

### 3. Voltar ao app e mostrar o resultado

O Stripe direciona o navegador para a página `/billing/stripe/return` da API. Essa página oferece um link para `viaja://billing/return`, que o manifesto Android associa à `PremiumActivity`.

Ao voltar ao primeiro plano, a tela consulta novamente `GET /billing/stripe/subscription`. Se o webhook ainda não chegou, a API também pode procurar uma sessão paga no Stripe pela referência do checkout e atualizar o registro. O Android lê um objeto `StripeSubscription`, com o status, o campo `premiumActive`, a data paga e a possibilidade de abrir o portal. A tela de sucesso aparece quando o app aguarda um checkout e a API confirma `premiumActive`. O retorno pelo link serve para reabrir o app e disparar a consulta.

### 4. Gerenciar, renovar ou cancelar

Quando a conta já tem um cliente Stripe associado, o app oferece o gerenciamento. Ele chama `POST /billing/stripe/portal`, a API cria uma sessão para aquele cliente e o Android abre a URL de `billing.stripe.com` no navegador. Ao voltar, o app consulta o status novamente.

A API considera o Premium ativo enquanto o período pago estiver vigente e o status estiver em `active` ou `past_due`. Por isso, um cancelamento agendado para o fim do ciclo pode manter o acesso até a data paga. Uma renovação confirmada pode estender essa data. Esse fluxo usa os registros de cobrança Stripe em ambiente de testes. O CRUD de `Subscription` que também existe no projeto é separado.

### Configuração para a demonstração

Use um Payment Link ativo no sandbox, com um único preço mensal e sem período grátis. Aponte o redirecionamento após o pagamento para a página de retorno da API e habilite o cancelamento no Customer Portal. Configure no Stripe a limitação de assinaturas ativas duplicadas para o link hospedado. As variáveis da API estão no [README do servidor](https://github.com/ProjetoIntegrador-4Semestre/viaja-api-spring/tree/develop#assinaturas-no-stripe-sandbox).

O webhook trata `checkout.session.completed`, `checkout.session.async_payment_succeeded`, `customer.subscription.updated`, `customer.subscription.deleted`, `invoice.paid` e `invoice.payment_failed`. Para testar com o servidor na sua máquina, o Stripe CLI pode encaminhar os eventos para `localhost:18080/billing/stripe/webhook`. Use o segredo de assinatura informado pelo CLI. Em um ambiente hospedado, mantenha o diretório `data/` em armazenamento persistente para preservar os vínculos após reinícios.

## Uma sequência para apresentar

1. Abra **Explorar**, selecione uma cidade e toque num resultado. Mostre como os dados saem da API, passam pelo ViewModel e chegam ao adapter, e como o ID abre o detalhe.
2. Salve o lugar e abra **Salvos**. Crie uma viagem, inclua o lugar e altere o horário da parada. Use esses passos para explicar o JWT, os objetos enviados e a confirmação da gravação pela API.
3. Abra **Premium**. Mostre a passagem do app para o navegador e, ao voltar, destaque que a tela consulta o servidor para exibir o estado da assinatura. Se o sandbox estiver configurado, mostre também o portal e o efeito de agendar o cancelamento.
