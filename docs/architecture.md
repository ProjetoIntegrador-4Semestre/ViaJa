# Viajá — arquitetura e dados

Inspeção de código: 05/11/2026. Regras desejadas: [produto](product-baseline.md). Limitações e testes: [estado](status.md). Este documento substitui os inventários prematuros de classes e o DDL PostgreSQL usado como se fosse o banco atual.

## Estrutura observada e padrão a preservar

| Área | Implementação atual | Direção |
| --- | --- | --- |
| Android | `com.senac.viaja`, Kotlin/XML, Activities, Intents, ViewModels/LiveData, repositórios, Retrofit/Gson e RecyclerView | Reaproveitar; resolver duplicação/navegação sem migrar para Compose ou impor Navigation Component |
| API | `com.senac.viajaapi`, Java/Spring Boot, JPA, controllers, repositories, assemblers, Security/JWT e Swagger | Preservar o padrão da disciplina; serviço específico somente quando uma operação real o exigir |
| Persistência | H2 em memória, Hibernate `create-drop`, LoadDatabase | Preparar H2 persistente para cobrança; não introduzir PostgreSQL, Supabase, RLS ou Flyway por causa de um documento antigo |
| Integração | Android fala com API própria; integrações externas futuras pelo servidor | Exceções naturais: renderização de mapa e navegação para sites/Checkout |
| Offline | Ainda ausente | Leitura de viagem baixada; solução local será escolhida pelo fluxo, sem impor Room/DataStore antecipadamente |

Fluxo já demonstrado: Activity → ViewModel/repositório existente → Retrofit → controller/repository → resposta tipada → Adapter/ViewHolder → XML. Há acessos diretos ao repositório em algumas Activities. Isso deve ser avaliado pelo comportamento e ciclo de vida, não convertido automaticamente em UseCases, interfaces e fábricas.

Referências da disciplina: `/Users/rafa/AndroidStudioProjects/MyApplication` e `/Users/rafa/IdeaProjects/corp-financial-api`. Transferir padrões de Retrofit/callbacks/Intents/listas e de controllers/assemblers/paginação/respostas; não copiar regras financeiras nem considerar todo comportamento da referência correto para o Viajá.

## Contratos existentes relevantes

Não há prefixo `/api` nas rotas abaixo. Prefixos e nomes planejados em prompts antigos não autorizam alterações no cliente.

| Operação | Contrato atual |
| --- | --- |
| Login | `POST /auth/login`, corpo `email` e `senha`; usuário no JSON, JWT no header `Authorization` da resposta |
| Cadastro | `POST /auth/signup`, `name`, `email`, `senha` |
| Destinos | `GET /cities?page=&size=&sort=`, HAL; coleção `_embedded.cityList` |
| Busca | `GET /places/search?q=`, lista simples de PlaceResponse; busca nome/endereço/cidade |
| Detalhe | `GET /places/{id}`, PlaceResponse mais links; `price` em centavos, não `priceLevel` |
| Catálogo paginado/escrita | `/places`, entidade Local; ainda difere do contrato de leitura do app |
| Viagens | `/trips` e `/trips/user/{userId}` paginados em `_embedded.trips`; detalhe/escrita por ID |
| Criar viagem | `name`, `cityId`, `mode`; datas/orçamento opcionais; proprietário do JWT; 201/Location |
| Paradas | `/trip-locals`; lista da viagem em `/trip-locals/trip/{tripId}`; detalhe por `/{tripId}/{localId}` |
| Salvos | `/users/{userId}/saved-locals`, HAL em `_embedded.savedLocals`; `/{localId}` identifica o vínculo pelo lugar |
| Notas/memos | `/trips/{tripId}/notes` e `/trip-memos`; existem, mas não são seguros/completos para a UI |
| Outros CRUDs | users, interests, user-interests, local-interests, reviews, subscriptions e medias; não equivalem a fluxos Android concluídos |
| IA, recuperação, Stripe e despesas | Não há contrato implementado; definir na integração, sem publicar rotas imaginadas como existentes |

`Long` é o tipo dos IDs simples no servidor. Alguns modelos Android recebem a representação textual do ID; isso não transforma o banco em UUID. Valores em centavos e datas de calendário devem manter significado nos dois lados. O campo legado `senha` viola a convenção de identificadores em inglês; uma alteração futura precisa atualizar app e API juntos, não apenas a documentação.

HAL oferece links e metadados. O Android hoje carrega todas as páginas de cidades/viagens/salvos; isso deve ser revisto para coleções grandes. Salvos buscam detalhes dos lugares sequencialmente. Busca de lugares e lista de paradas ainda não são paginadas. Não chamar todas as listas de paginadas.

## Dados presentes no código

São 14 entidades JPA. O inventário é de responsabilidades, não um script de migração:

| Entidade | Papel e campos importantes |
| --- | --- |
| User | Conta, identidade, hash, contatos, provedor e modo padrão; hash não pode ser resposta pública |
| Interest / UserInterest | Interesses e associação por chave composta |
| City | Destino, estado, país, imagem |
| Local / LocalInterest | Cidade, tipo, descrição, coordenadas, preço, nota/contagem, horário e fonte; associação de interesses |
| Trip | Usuário, cidade, nome, modo, datas, orçamento, status e timestamps |
| TripLocal | Chave composta viagem/lugar, dia, ordem e nota; não há horário nem ID independente da parada |
| TripNote | Texto geral ligado à viagem |
| TripMemo | Texto ligado à parada; IDs viagem/lugar e relação JPA composta com TripLocal |
| SavedLocal | Favorito com ID simples, usuário/lugar e unicidade do par |
| Review | Lugar, nota e data; não possui comentário nem autor no modelo atual |
| Subscription | Usuário, plano/status, valor/período; não possui integração Stripe |
| Media | Tipo de entidade/ID, URL e tipo; associação polimórfica |

Despesas, itens de checklist, tokens de recuperação, rascunhos estruturados de IA, quota, registros Stripe/eventos e download offline ainda não têm suporte completo. Não inventar tabelas existentes para preencher uma matriz.

Muitos vínculos são apenas campos de ID; não assumir FK/cascade em todos. TripMemo já declara FK composta, ao contrário do DDL histórico que tentava ligar uma coluna a duas. Remover uma parada com memos pode falhar por integridade: o controlador ainda não resolve esses dependentes. Exclusão da viagem remove memos, notas, paradas e mídias de viagem em transação; a limpeza de cada tipo precisa de teste direto além do fluxo sem dependentes.

## Regras de integridade a fechar

- Unicidade/normalização do e-mail e validação real de senha/campos no servidor.
- Regras de exclusão de usuário/cidade/lugar e dos registros associados.
- Ordem sem colisões por dia e horários opcionais; repetição do lugar em paradas distintas é uma decisão pendente de modelo.
- Troca de cidade/datas não pode deixar paradas incompatíveis.
- Notas/memos precisam validar viagem/parada da URL e proprietário; não apenas o ID global do texto.
- Média e quantidade de avaliações devem acompanhar gravações; não misturar totais demonstrativos com novas notas como se fossem calculados.
- Mídia/anexos pessoais precisam de autorização e armazenamento privado; URL polimórfica não resolve isso.
- Stripe exige persistência, associações únicas e processamento de evento idempotente; a tabela Subscription atual é insuficiente.

## Autorização e erros

JWT autentica a identidade; cada operação privada também confere o proprietário. Isso está aplicado a salvos, viagens e paradas, não ao sistema inteiro. Hoje há leitura cruzada de usuários/notas/interesses, hash em resposta, catálogo com escrita pública e tratamento deficiente de JWT inválido. São falhas abertas, não decisões aceitas.

O contrato desejado distingue 401, 403, 404, 400 e conflitos. Criação usa 201 com Location; exclusão usa 204 sem corpo. A aplicação ainda transforma RuntimeException em 404, além de expor mensagens internas. Revisar recurso por recurso e verificar Swagger/resposta real; não reescrever todos os controladores apenas para padronizar aparência.

## Desenvolvimento e configuração

API: consultar o README em `/Users/rafa/IdeaProjects/api-viaja/README.md` para execução/testes. Android local:

```sh
./gradlew :app:assembleDebug -PapiBaseUrl=http://127.0.0.1:18080/ --offline
adb reverse tcp:18080 tcp:18080
```

A propriedade é de debug; release permanece com a URL hospedada. Não afirmar que os contratos locais já foram publicados. H2 atual reinicia com dados de exemplo; LoadDatabase contém poucas cidades/lugares e URLs de imagem ilustrativas. Chaves externas nunca entram no APK/vault. O segredo JWT padrão no código é dívida a remover, não credencial de produção aceitável.

Versões e dependências devem ser consultadas nos arquivos Gradle/pom.xml. Não congelar limites de API, preços, SDK de mapa ou modelo Gemini a partir dos textos de setembro. Testes de provedores e de publicação precisam de documentação oficial vigente no momento da integração.
