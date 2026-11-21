# API dos pacotes 2–9

Revisão: 18/11/2026. O cumulativo vigente é [all-packages.patch](all-packages.patch). Ele contém todos os fontes Java/testes, ajustes no pom e perfil persistente necessários aos pacotes 2–9. Substitui os patches anteriores no checkout original que ainda não recebeu nenhum deles; **não aplicar cumulativos em sequência**.

## Situação da aplicação

A fonte `/Users/rafa/IdeaProjects/api-viaja` permanece fora das áreas graváveis. A aplicação foi autorizada pelo responsável, mas rejeitada automaticamente pela política de escrita da sessão. O original não foi modificado nem publicado. O desenvolvimento e os testes aconteceram em cópia isolada; o patch preserva o código no projeto Android.

O `git apply --check` passou contra o checkout original em 18/11. Quando a área estiver gravável, executar na raiz da API, revisando mudanças posteriores antes de aplicar:

```sh
git apply --check /Users/rafa/AndroidStudioProjects/Viaja/backend-patches/all-packages.patch
git apply /Users/rafa/AndroidStudioProjects/Viaja/backend-patches/all-packages.patch
./mvnw test
./mvnw -DskipTests package
```

## Configuração obrigatória e opcional

Variáveis somente no servidor; valores reais não estão no patch, no APK ou na documentação. O Spring faz o vínculo das variáveis abaixo com as propriedades `viaja.*`.

| Variável | Uso |
| --- | --- |
| `VIAJA_JWT_SECRET` | Assinatura JWT; pelo menos 32 bytes, aleatório; segredo ausente/fraco ou antigo padrão conhecido é rejeitado |
| `VIAJA_AI_KEY` | Chave da API OpenAI |
| `VIAJA_AI_MODEL` | Modelo habilitado na conta e compatível com Responses e JSON Schema estrito |
| `VIAJA_MAIL_KEY` | Chave Resend |
| `VIAJA_MAIL_FROM` | Remetente verificado no Resend |
| `VIAJA_GOOGLE_CLIENT_ID` | Cliente OAuth Web do Google, usado pelo backend |
| `VIAJA_GOOGLE_SECRET` | Segredo do cliente Google |
| `VIAJA_GOOGLE_REDIRECT` | URL HTTPS cadastrada no Google e exposta pela API, terminando em `/auth/google/callback` |
| `SPRING_PROFILES_ACTIVE=persistent` | Ativa H2 em arquivo sem seed automático |
| `VIAJA_DATABASE_URL` | No perfil persistente: URL H2 `jdbc:h2:file:/caminho/do/volume/viaja` |
| `VIAJA_DATABASE_USERNAME` | Usuário H2; padrão `sa` |
| `VIAJA_DATABASE_PASSWORD` | Senha obrigatória no perfil persistente |

Sem IA/e-mail/Google configurados, o serviço correspondente retorna indisponibilidade, sem sucesso local fictício. Google abre consentimento no navegador e usa resultado por polling com segredo próprio; o callback não entrega JWT em URL. O e-mail de recuperação traz um código temporário para colar no app; não depende de um App Link de reset.

Open-Meteo e mapa não exigem chave no código preparado. Verificar condições/licenças para o uso pretendido. O mapa usa Leaflet 1.9.4 e tiles OpenStreetMap com atribuição; não existe download de mapa. Stripe conserva sua configuração existente (`stripe.secret-key`, `stripe.webhook-secret`, `stripe.payment-link-id`, `stripe.price-id`, `stripe.return-url`), somente em testes para a demonstração.

### Persistência

O padrão `application.properties` não foi trocado: continua H2 em memória/create-drop e seed demonstrativo. O novo perfil `persistent` usa `ddl-auto=update`, desativa console/SQL e seed, e requer volume durável. A primeira execução desse perfil começa sem catálogo/contas; provisionar dados revisados. Seed pode ser habilitado explicitamente com `viaja.seed.enabled=true` somente no ambiente demonstrativo, conservando o comportamento anterior de não semear quando já há usuários.

O teste reinicia dois contextos Spring no mesmo arquivo e confirma conta, viagem e direito de acesso. Isso não configura o disco do provedor de hospedagem nem migra um banco antigo. Antes de atualizar banco existente, revisar duplicidades de e-mail/google_id, novas colunas/tabelas e backup; update não substitui migração revisada. Não usar create-drop para comprovar continuidade de assinatura.

## Contratos dos pacotes 5–9

| Área | Contrato e regras |
| --- | --- |
| Perfil | `GET/PUT /account/profile`; nome/e-mail/modo/interesses, validação, senha atual para trocar e-mail, perfil sem hash e JWT renovado |
| Descoberta | `GET /discovery/options`, `/discovery/places`; q/cityId/type/maxPrice/minRating/sort/page/size; busca normalizada e paginação de resultados |
| Avaliação | `PUT /discovery/places/{id}/rating`, rating 1–5; um registro por conta/lugar e média recalculada |
| IA | `/ai/quota`; GET/PUT/DELETE de sessões; POST mensagens e apply; UUID idempotente, contexto autorizado e snapshot antes da geração |
| Mapas/clima | `GET /trips/{tripId}/map`, `/weather`; coordenadas conhecidas, Open-Meteo com origem/fuso/atualização e cache de 15 minutos |
| Portabilidade | `GET /trips/{tripId}/export?full=false`, `/offline`; pacote schema 1, proprietário e snapshot; full/offline requerem Premium vigente |
| Recuperação | `POST /auth/recovery`, `/auth/reset`; resposta genérica, hash de código, 20 minutos/uso único, entrega assíncrona após commit e invalidação dos JWTs anteriores |
| Google | `POST /auth/google/start`, GET callback, POST result; PKCE, nonce, estado, assinatura/claims; expiração de 10 minutos e consumo único do resultado |

IA conta 2 planos/mês civil gratuito e 20/ciclo pago Premium. Teto operacional separado: 40/200 interações diárias. Falha do provedor não gera consumo confirmado. Aplicar exige aprovação e passa pelas validações do roteiro; nenhuma ferramenta faz reserva ou grava despesas automaticamente. Ver [Vi](../docs/vi-assistant.md).

Recuperação limita pedidos por e-mail/hora em memória; não há rate limit distribuído nem fila durável de e-mail. Falha da entrega assíncrona é registrada sem segredo e o usuário pode pedir novamente. Não interpretar a resposta genérica como confirmação de recebimento da mensagem.

## Pacote 2: roteiro

`GET /trips/{tripId}/itinerary` consulta o roteiro editável; `GET .../itinerary/places` lista lugares da cidade. PUT recebe `expected` e `stops` completos com `localId`, `day`, `orderIndex`, `note`, `scheduledTime` e `completed`. Snapshot divergente retorna 409; repetir estado final não duplica paradas. Exclusão resolve memos na mesma transação. A chave composta viagem/lugar permanece. Limite técnico de 1000 entradas por lista.

## Pacote 4: gastos, reembolsos e comprovantes

- `GET /trips/{tripId}/expenses` devolve a lista, total em centavos, orçamento opcional, saldo opcional, totais por categoria, total reembolsável e recebido informado. Valores são inteiros `Long`, sem arredondamento por ponto flutuante. Ausência de orçamento não vira zero; orçamento zero pode ter saldo negativo.
- `PUT /trips/{tripId}/expenses/{uuid}` recebe `description` (até 160 caracteres), `amountCents` (positivo, até 2147483647 por despesa), `expenseDate` (`yyyy-MM-dd`), `category`, `reimbursement` e `version` opcional somente na criação. UUID estável evita duplicar criação/reenvio. Alterações exigem a versão carregada; conflito retorna 409. Uma edição de registro removido não o recria.
- Categorias: `transport`, `lodging`, `food`, `activities`, `shopping`, `other`. Reembolso: `none`, `pending`, `requested`, `received`. Estados são informados manualmente e não confirmam pagamento externo. Reembolso não é subtraído automaticamente do total das despesas.
- `DELETE /trips/{tripId}/expenses/{uuid}?version=...` exclui com verificação de versão. Repetir uma exclusão já concluída retorna 204. Exclusão de viagem limpa despesas e seus arquivos vinculados.
- `PUT /trips/{tripId}/expenses/{uuid}/receipt?version=...` recebe multipart `file`. `GET` nesse caminho baixa o arquivo; `DELETE` o remove sem apagar a despesa. PDF, PNG e JPEG são identificados por assinatura, com limite de 5 MiB por arquivo. Substituir/remover exige versão atual; repetir a mesma substituição não muda o estado novamente.
- Arquivos são BLOBs privados da despesa no banco, sem URL pública. Download valida proprietário e vínculo com a viagem e usa `Content-Disposition: attachment`, `Cache-Control: no-store` e `X-Content-Type-Options: nosniff`. Identificação por assinatura não equivale a análise completa do documento.
- `ReceiptMultipartConfig` limita a recepção multipart; o controller aplica o limite efetivo de 5 MiB. O modelo usa FK de despesa para viagem; o ciclo de exclusão inclui os BLOBs.

O Android usa seletor de documentos, leitura limitada fora da thread de UI, envio autenticado e cache privado para abrir comprovantes. O `FileProvider` não é exportado e só expõe a pasta restrita de comprovantes mediante concessão temporária de leitura. Não requer acesso amplo ao armazenamento. A renderização por aplicativo externo e o fluxo multipart HTTP não foram executados em emulador nesta rodada.

O resumo do painel recarrega ao voltar da página de gastos. O filtro de categoria altera apenas a lista exibida; o resumo continua sendo da viagem inteira. Datas fora do intervalo da viagem são aceitas para registros como compras antecipadas; não são geradas estimativas automaticamente.

A criação das tabelas/colunas foi testada em schema H2 isolado. Se a implantação usar banco persistente, verificar a atualização de schema sem apagar dados antes de publicar. Enquanto os endpoints não estiverem presentes, o app indica gastos indisponíveis, sem inventar total zero.

## Pacote 3: contratos e dados

- Anotações de viagem continuam em `/trips/{tripId}/notes`; anotações de parada continuam em `/trip-memos`. A nota rápida de `TripLocal.note` é separada desses registros.
- Notas e memos passam a aceitar até 4000 caracteres, com validação de texto não vazio. Colunas são ajustadas no modelo JPA. O cliente envia `requestId` UUID ao criar; repetir a mesma solicitação devolve o registro existente. Reutilizar a chave com outro conteúdo retorna 409.
- `GET /trips/{tripId}/checklist` lista itens da viagem.
- `PUT /trips/{tripId}/checklist/{uuid}` cria/atualiza item com `label` (até 240 caracteres) e `completed`. O UUID estável evita criar cópias ao repetir o salvamento.
- `DELETE /trips/{tripId}/checklist/{uuid}` remove o item. A repetição da exclusão de um item já ausente retorna 204.
- As escritas usam o bloqueio da viagem do pacote 2. Itens e notas não podem ser acessados por proprietário de outra conta ou manipulados por ID em outra viagem.
- Excluir viagem remove também checklist, notas e memos; a chave de lugar nas paradas permanece composta.

O modelo de checklist cria uma tabela própria com vínculo à viagem. Não houve configuração nem migração de um banco persistente de produção: a validação utiliza schema H2 novo, isolado. Uma implantação com schema persistente exige verificar sua atualização antes de publicar.

Sem a atualização da API, anotações continuam usando os contratos antigos e os erros preservam o texto; o checklist aparece como indisponível. O limite antigo de texto e a garantia de idempotência das anotações dependem da versão efetivamente publicada. O app não simula criação ou conclusão de itens.


## Evidência final

Oito testes API passaram em sete classes, incluindo integração Spring/H2, criptografia Google, parsing meteorológico e persistência após reinício. Maven empacotou a API isolada; o cumulativo passou no check de aplicação. Provedores são substituídos nos testes de integração; não houve envio real de e-mail, login Google, cobrança Stripe ou consumo OpenAI.

No Android: 30 testes Kotlin, compilação independente dos fontes com R real e compile/link AAPT2 passaram. Gradle completo, APK, emulador, HTTP e deploy continuam pendentes. Ver [entrega dos pacotes restantes](../docs/remaining-packages.md).

## Referências oficiais

- [OpenAI Responses](https://developers.openai.com/api/reference/resources/responses/methods/create) e [Structured Outputs](https://developers.openai.com/api/docs/guides/structured-outputs).
- [Google OpenID Connect](https://developers.google.com/identity/openid-connect/openid-connect).
- [Resend: envio de e-mail](https://resend.com/docs/api-reference/emails/send-email).
- [Open-Meteo](https://open-meteo.com/en/docs), [Leaflet 1.9.4](https://leafletjs.com/reference-1.9.4.html) e [política de tiles OSM](https://operations.osmfoundation.org/policies/tiles/).
