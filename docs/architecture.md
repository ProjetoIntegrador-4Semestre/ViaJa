# Viajá — arquitetura e dados

Revisão: 18/11/2026. Esta descrição inclui o código Android e a API preparada no patch; não significa que a API hospedada já o executa. Regras: [produto](product-baseline.md). Evidências: [estado](status.md).

## Estrutura

| Área | Implementação |
| --- | --- |
| Android | Kotlin/XML, Activities/Intents, ViewModels/LiveData, repositórios, Retrofit/Gson e RecyclerView; componentes Material e tokens visuais compartilhados |
| API | Java/Spring Boot/JPA; controllers/repositories e gateways específicos para provedores |
| Banco | H2; padrão de demonstração em memória/create-drop; perfil optativo `persistent` em arquivo/update, sem seed automático |
| Sessão | JWT identifica e-mail e versão de sessão; segredo obrigatório no servidor; token Android criptografado com AES/GCM no Keystore |
| Provedores | OpenAI Responses, Google OIDC, Resend e Open-Meteo pelo servidor; Stripe existente |
| Mapa | Leaflet/OSM em WebView restrita sem ponte JavaScript, coordenadas da viagem; navegação externa por Intent |
| Offline | JSON schema 1, AtomicFile em armazenamento privado separado por conta; somente leitura, excluído do backup |
| PDF/recibos | PdfDocument + seletor de destino; comprovantes autenticados no banco e cache privado via FileProvider |

Activity → ViewModel/repositório → Retrofit → API própria → banco/gateway → DTO → interface. Credenciais externas não ficam no APK. Mapa e sites/Checkout abrem recursos externos conforme sua finalidade; a IA não recebe permissão de escrita direta.

## Contratos

Sem prefixo `/api`. Os contratos novos abaixo exigem o [patch cumulativo](../backend-patches/README.md).

| Área | Rotas principais |
| --- | --- |
| Acesso | `POST /auth/login`, `/auth/signup`; login legado devolve JWT no header Authorization |
| Recuperação | `POST /auth/recovery`, `/auth/reset` |
| Google | `POST /auth/google/start`, `GET /auth/google/callback`, `POST /auth/google/result` |
| Perfil | `GET/PUT /account/profile`; gravação devolve perfil seguro e JWT renovado |
| Descoberta | `GET /discovery/options`, `/discovery/places`; `PUT /discovery/places/{id}/rating` |
| Catálogo legado | `GET /cities`, `/places/search`, `/places/{id}`; formatos HAL/lista preservados |
| Viagens e salvos | `/trips`, `/trips/user/{userId}`, `/users/{userId}/saved-locals` |
| Roteiro | `GET/PUT /trips/{tripId}/itinerary`, `GET .../itinerary/places`; snapshot esperado e lista final |
| Organização | `/trips/{tripId}/notes`, `/trip-memos`, `/trips/{tripId}/checklist/{uuid}` |
| Gastos | `/trips/{tripId}/expenses/{uuid}` e `/receipt`; versões e UUID estável |
| IA | `/ai/quota`, `/ai/sessions/{uuid}`, `/messages`, `/messages/{requestId}/apply` |
| Recursos | `/trips/{tripId}/map`, `/weather`, `/export?full=false`, `/offline` |
| Assinatura | `/billing/stripe/*`, conforme integração existente |

IDs simples continuam Long; UUID identifica operações/itens específicos, não substitui a chave de lugar ou viagem. Valores monetários são centavos BRL; datas de viagem são calendário e horários das paradas são locais sem conversão automática. Quota gratuita usa mês civil em America/Sao_Paulo, Premium usa término do período pago. As listas legadas não são todas paginadas; descoberta nova pagina resultados, mas ainda filtra o catálogo em memória no servidor.

## Dados e integridade

O domínio mantém conta/interesses/cidades/lugares, viagens e paradas com chave composta viagem/lugar, notas/memos/salvos/avaliações e mídia. Paradas têm dia/ordem/nota/horário/conclusão; o mesmo lugar não se repete na mesma viagem. Novos dados incluem checklist, despesas/recibos privados, sessões/turnos de IA, tokens de recuperação e tentativas Google. StripeMembership/StripeEvent já existiam e sustentam os benefícios reais.

Roteiro e suas dependências usam transação/bloqueio por viagem; snapshot detecta edição concorrente. Despesas usam versão; notas e mensagens têm chaves estáveis para retentativa. A proposta da Vi guarda o snapshot anterior à chamada do modelo e só grava com aprovação explícita. Excluir viagem limpa dependentes implementados; não inferir cascatas para qualquer recurso legado.

Avaliação tem um registro por conta/lugar. A média/contagem é recalculada dos registros persistidos, incluindo avaliações demonstrativas existentes; números agregados antigos sem registros correspondentes não são preservados artificialmente. Não há moderação implementada nem autoria pública.

## Autorização

JWT autentica; cada recurso pessoal confere o proprietário. Interesses, notas/memos/checklist, gastos, recursos de viagem e IA não são públicos. Escrita genérica de catálogo e mídia fica bloqueada sem modelo de administrador. E-mail muda pelo endpoint seguro com senha atual; reset invalida sessões pela versão da conta. O segredo JWT padrão conhecido foi removido e é rejeitado se fornecido novamente.

Google usa navegador do sistema, PKCE, nonce/state e verificação de assinatura/claims no servidor. O app consulta o resultado com segredo independente; JWT não circula em URLs. Contas existentes só são vinculadas automaticamente quando o Google é autoridade do e-mail (Gmail/domínio verificado); e-mail externo exige entrada apropriada, sem vínculo silencioso.

Offline mantém o acesso local da última conta após expiração da sessão; logout explícito/401 revogam esse acesso. Arquivos não são apagados por expiração do Premium, mas novos downloads completos exigem direito no servidor. Arquivos são privados pelo sandbox Android, sem criptografia extra dos conteúdos JSON; não confundir com a proteção adicional do JWT.

## Execução

Configuração e aplicação: [backend-patches/README.md](../backend-patches/README.md). Android debug local preserva a propriedade existente:

```sh
./gradlew :app:assembleDebug -PapiBaseUrl=http://127.0.0.1:18080/ --offline
adb reverse tcp:18080 tcp:18080
```

A API padrão ainda usa H2 em memória e seed demonstrativo. O perfil persistente requer URL/credenciais e volume durável; `ddl-auto=update` não substitui revisão de migração/backup de um banco existente. Nenhuma implantação foi alterada nesta rodada. Versões/dependências estão nos arquivos Gradle/pom; o modelo de IA é configuração obrigatória, sem versão presumida.
