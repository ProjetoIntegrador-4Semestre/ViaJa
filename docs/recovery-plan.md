# Viajá — plano de execução

Revisão: 18/11/2026. Entrega prevista: 02/12/2026. Os pacotes 1–9 foram implementados no Android e, quando necessário, na cópia isolada da API. O patch cumulativo preserva o backend completo. Aplicação na fonte original, configuração dos provedores, publicação e validação ponta a ponta permanecem na fila de entrega; implementação não equivale a serviço publicado.

O [produto](product-baseline.md) define o esperado; o [estado](status.md) informa o existente. O [histórico de recuperação](history/recovery-2026-11-05.md) preserva as rodadas anteriores e seu cronograma substituído.

| Etapa | Trabalho | Critério de saída |
| --- | --- | --- |
| 1. Alinhamento | Consolidar regras, escopo, planos e telas; retirar autoridade das fontes conflitantes | Um índice, uma referência por assunto, decisões/propostas/evidências distintas |
| 2. Contratos de tela | Validar Home/Perfil/Premium/retorno e aplicar a mesma lógica às jornadas restantes | Cada tela tem propósito, dados, ação principal, navegação, carregamento/vazio/erro e resultado de envio |
| 3. Base segura e reproduzível | Corrigir exposição de usuário, propriedade dos demais recursos, catálogo público, JWT e sessão; resolver alterações pendentes sem perder trabalho | Testes negativos de acesso; nenhum hash exposto; build/execução em checkout limpo |
| 4. Assinatura de testes | Persistência H2, Checkout→webhook→acesso→retorno→cancelamento | Sucesso, abandono, falha, evento repetido/atrasado e reinício verificados; nenhuma cobrança real |
| 5. Fluxos completos | Revisar visual e concluir perfil/roteiro/notas/gastos, Google/reset, mapas/clima/avaliações e IA | Cada jornada funciona do Android à persistência, com falha recuperável; sem sucessos simulados |
| 6. Portabilidade e entrega | PDF básico/completo, leitura offline, histórico, deploy e materiais acadêmicos | Instalação/API novas reproduzem a demonstração; documentos citam código real e limitações |

Dentro de cada etapa, entregar um fluxo verificável de cada vez. A ordem pode mudar por dependência comprovada; não criar arquivos antecipados para preencher um diagrama. Reutilizar Kotlin/XML, Retrofit, Intents e o padrão de API da disciplina.

## Preparar cedo, sem fingir integração

Conta Stripe de testes/Price ID, URL de retorno e webhook; configuração OAuth Google; serviço de e-mail; credencial/provedor de IA; condições de uso e atribuição de mapa/clima; hospedagem e dados demonstrativos. Inventariar o que existe sem colocar segredos na documentação.

## Controle de prazo

Todas as áreas confirmadas permanecem no escopo, mas isso não comprova que cabem no tempo disponível. O volume pendente é alto. Estimar o restante após fechar contratos e dependências; não reutilizar o cronograma de setembro nem prometer uma funcionalidade apenas porque sua tela existe. Uma eventual renegociação precisa ser explícita com o responsável e o grupo, nunca um corte silencioso.

## Revisão de conclusão

Para cada fluxo: código já existente/redundante conferido, requisito consultado, regras no servidor, estados da tela, propriedade dos dados, teste relevante, ausência de conteúdo fictício enganoso e commit semântico. Não é necessário criar um teste que apenas repete a implementação ou uma nova camada para cada item.

## Sequência por página — situação de 18/11

| Pacote | Entrega no código | Situação |
| --- | --- | --- |
| 1 | Viagens e painel | Android implementado |
| 2 | Editor completo do roteiro | Android e patch API |
| 3 | Notas e checklists | Android e patch API |
| 4 | Gastos, reembolso e recibos privados | Android e patch API |
| 5 | Perfil e interesses persistidos | Android e patch API |
| 6 | Descoberta, filtros, fotos e avaliações | Android e patch API |
| 7 | Vi conectada, sessões, quota e propostas | Android e patch API; provedor por configurar |
| 8 | Mapa, navegação e clima | Android e patch API; validação externa pendente |
| 9 | Recuperação, Google, PDF e offline | Android e patch API; serviços por configurar |

O [relatório dos pacotes restantes](remaining-packages.md) registra a entrega de 5–9 e sua verificação. A fila agora é de integração e entrega, sem exigir novo pedido de pacote para redescobrir o escopo.

### Complementos necessários à coerência

- **Contexto consistente:** ID da viagem, datas/duração, modo e orçamento devem manter o mesmo significado na Home, no painel, no roteiro e na Vi. Preferências da conversa não alteram silenciosamente a viagem.
- **Planejado versus realizado:** estimativa de custo de parada não é despesa paga; sugestão da Vi não é item salvo; concluir parada não conclui automaticamente a viagem.
- **Ciclo de vida da viagem:** estados planejada/em andamento/concluída/cancelada, confirmação nas ações relevantes e histórico consultável; não esconder roteiro quando a viagem terminar.
- **Dias e horários:** exibir dias sem paradas e definir como a duração sem datas funciona no contrato; datas e horários exibidos precisam de uma regra de fuso coerente. Não inventar duração para completar a tela.
- **Retorno às telas:** editar uma viagem, salvar uma parada ou alterar um gasto deve atualizar os resumos quando o usuário volta, preservando busca e posição quando fizer sentido.
- **Falha recuperável:** preservar formulários, evitar duplo envio, oferecer retentativa e indicar autenticação expirada; sucesso somente após confirmação do servidor.
- **Dados e direitos:** arquivos e objetos por proprietário, exclusões com dependentes, backup adequado, plano/consumo consultados no servidor e catálogo com procedência.
- **Consistência visual:** extrair tokens compartilhados da direção da Vi e migrar página por página, validando contraste, teclado, texto ampliado e estados vazios antes de trocar a paleta global.

### Adições opcionais, ainda propostas

Resumo de pendências por viagem (dias vazios, orçamento ausente, paradas sem horário); ações de duplicar uma viagem ou usar uma viagem anterior como modelo; alertas de sobreposição de horários quando houver duração suficiente para calculá-los. Não tratar essas ideias como implementadas ou obrigatórias para a entrega. Notificações e compartilhamento colaborativo exigiriam decisões adicionais de permissão, privacidade e sincronização.

## Pacote 1 — cadastro/edição e painel: implementação de 17/11

Implementado no Kotlin/XML, sem mudança na API:

- `TripEditorActivity`/`TripEditorViewModel`: criação e edição reais por `TripRepository`, consulta de destinos, nome, modo, datas opcionais e orçamento opcional. O formulário mantém dados quando a operação falha, durante rotação e restaura o rascunho salvo no estado da Activity após recriação de processo. Envio repetido fica bloqueado durante a operação.
- `TripFormRules`: moeda BRL convertida sem arredondamento para centavos, distinção entre zero e desconhecido, limite do contrato Int da API, datas estritas e fim não anterior ao início. Consulta recente das paradas antes de salvar impede encurtar a viagem além do último dia ocupado e impede trocar a cidade com paradas existentes.
- `TripDashboardActivity`/`TripDashboardViewModel`: resumo do destino, datas, orçamento, estado, pendências e progresso do roteiro; navegação para edição, roteiro e conversa; exclusão confirmada; transições confirmadas entre os quatro estados canônicos. Roteiro e viagem concluída continuam consultáveis.
- Minhas viagens recebe a direção visual de verde profundo/marfim/sálvia, mostra estado/datas/orçamento, recarrega ao retornar e abre o painel após criação bem-sucedida. Dados de erro não fecham o formulário como se tivesse havido sucesso.
- `TripRequest.status` e `TripResponse.cityId` complementam o contrato já existente no servidor. Atualizar uma viagem preserva o status carregado; alterar somente o estado preserva os demais campos.

Limites: validação de conflitos de cidade/duração foi adicionada ao cliente; a API examinada ainda não faz essa checagem cruzada, portanto concorrência entre clientes exige reforço futuro no servidor. Não houve criação de endpoint, proteção transacional adicional nem mudança no backend. Um pedido concluído no servidor cujo retorno se perca pode exigir recarregar a listagem antes de repetir; idempotência de criação ainda não existe no contrato. Não foi simulada resposta de gravação nem incluído botão funcional para notas/gastos ausentes.

Validação: sete testes JUnit de moeda/datas/duração passaram em execução direta; compilação e vínculo de recursos Android passaram com AAPT2 e dependências do cache local. Checagem independente dos fontes Kotlin com os identificadores R gerados pelo AAPT2 passou. Isso não equivale a montar, instalar e executar o APK pelo Gradle. O build Gradle/emulador continuam limitados pelo bloqueio de sockets deste ambiente. Verificação de rede e persistência no servidor não foi executada nesta entrega.

## Escopo dos pacotes — registro da sequência

Todos os pacotes abaixo têm código implementado em 18/11. Os critérios continuam como aceitação ponta a ponta, ainda pendente; os relatos subsequentes preservam a evidência de cada rodada.

| Pacote | Escopo fechado para iniciar | Critério de conclusão |
| --- | --- | --- |
| **2 — Editor do roteiro (implementado; API pendente de aplicação)** | Dias vazios a partir das datas; selecionar dia/horário na inclusão; mover entre dias; reordenar; remover com confirmação e resolver notas/memos vinculados; preservar horário/conclusão existentes | Alterações persistidas e refletidas no painel/Home após voltar; retentativa sem duplicação; API valida propriedade, dias e ordem; sem declarar suporte à duração sem datas antes de definir seu contrato |
| **3 — Notas e checklists (implementado; API pendente de aplicação)** | Editor de notas da viagem e da parada; checklist com itens individuais e conclusão; acessos no painel | Dados persistidos e isolados por proprietário; exclusão coerente; retomada, vazio e falha tratados; API de checklist definida antes da tela funcional |
| **4 — Gastos e orçamento (implementado; API pendente de aplicação)** | Criar/editar/excluir despesas com categoria/data/valor; total e saldo versus orçamento; resumo no painel | Soma de centavos correta; zero distinto de desconhecido; estimativas separadas de despesas; autorização validada; recibos privados e reembolso implementados no pacote; validar leitura/upload HTTP e Android |
| **5 — Perfil e interesses (implementado)** | Atualização real do perfil, interesses persistidos e tratamento de sessão após troca de e-mail | Reabrir mostra dados salvos; falha preserva formulário; interesses chegam ao contexto autorizado da Vi |
| **6 — Descoberta e lugares (implementado)** | Filtros/ordenação reais, busca coerente, fotos/dados com procedência, salvos recuperáveis e inclusão contextual no roteiro | Busca → detalhe → salvar/adicionar funciona com escolha de dia; dados ausentes não viram exemplos fictícios |
| **7 — Vi conectada (implementado)** | Sessões/mensagens na API, provedor e ferramentas, contexto autorizado, limite de consumo e propostas revisáveis | Chamada real; falha preserva pedido; proposta validada; aprovação explícita antes de gravação; reenvio/aplicação não duplicam ações |
| **8 — Mapa e clima (implementado)** | Mapa das paradas, navegação externa e previsão com fonte e atualização | Falha/ausência de permissões recuperável; previsão e estimativa identificadas; sem inventar tempos de deslocamento |
| **9 — Conta, exportação e offline (implementado)** | Recuperação por e-mail/token, Google, PDF e pacote de leitura offline | Jornadas reais com acesso correto por plano; arquivos e dados privados; conteúdo exportado coerente com a viagem salva |

Stripe, persistência após reinício, sessão, autorização, acessibilidade e regressões seguem transversais: código de integração existente não dispensa prova do fluxo de testes. Se um pacote exigir mudança fora do checkout autorizado, preparar a alteração de forma revisável e registrar o impedimento real antes de afirmar conclusão.

## Pacote 2 — editor do roteiro: implementação de 17/11

Android: `ItineraryViewModel` mantém snapshot original e rascunho; `TripItineraryActivity` permite selecionar qualquer dia válido, navegar também por dias vazios, incluir lugar da cidade com dia/horário opcionais, mover, reordenar, editar nota, ajustar horário/conclusão e remover com confirmação. Todas as operações de edição completa são aplicadas em conjunto por “Salvar roteiro”. Erros preservam o rascunho; recarregar/sair com ajustes pede confirmação; rotação e estado salvo da Activity preservam ajustes. Dias sem datas não representam duração confirmada. A lista mostra um dia por vez, evitando criar milhares de Views para viagens longas.

A ordem manual é a sequência canônica do editor; a próxima parada continua escolhida cronologicamente pelos horários disponíveis. Incluir a partir do detalhe de um lugar agora abre o editor da viagem e destaca a opção selecionada, sem inserir silenciosamente no primeiro dia. As alterações persistidas voltam aos leitores já existentes da Home e do painel quando essas páginas retomam suas consultas.

API: implementação completa preparada em [backend-patches/itinerary-editor.patch](../backend-patches/itinerary-editor.patch), com instruções em [backend-patches/README.md](../backend-patches/README.md). Inclui consulta editável/opções da cidade, PUT atômico com snapshot esperado, reenvio idempotente, validações, bloqueio de escrita por viagem, exclusão transacional de memos e proteção da edição de cidade/duração. A API original fica fora da área gravável; o patch não foi aplicado nem publicado. Seu `git apply --check` passou no checkout local. Enquanto os endpoints novos não estiverem disponíveis, Android mantém leitura e horário/conclusão pelo contrato antigo e informa indisponibilidade da edição completa. Não marcar este pacote como validado ponta a ponta antes da aplicação.

Validação: 14 testes Kotlin/JUnit passaram (7 do formulário e 7 de ordem/movimento/dias/próxima parada), checagem independente dos fontes Kotlin com R real e compile/link dos recursos Android passaram. A API isolada compilou/empacotou e passou em um teste de integração com H2 real cobrindo conflito, repetição, isolamento por proprietário e remoção/rollback de memos. Build Gradle completo, emulador, HTTP e API pública não foram validados nesta rodada.

## Pacote 3 — Notas e checklists: implementação de 18/11

`TripNotebookActivity` e `TripNotebookViewModel` implementam a página de organização acessível pelo painel da viagem e pelas ações da parada. Anotações da viagem, múltiplos memos da parada e nota rápida do roteiro permanecem separados. A página permite criar, abrir/editar e excluir anotações; o checklist sempre pertence à viagem inteira, inclusive quando a página é aberta em uma parada. Cada item tem rótulo e estado de conclusão próprios. Não transformar texto livre em checklist.

O editor é explícito, mantém texto em erro/rotação e no estado salvo da Activity, confirma descarte de texto e exclusões e impede mutações simultâneas. A leitura das notas da viagem percorre todas as páginas HAL. Carregamento/erro de notas e checklist são independentes; falha ou contrato ausente não apresenta sucesso nem checklist fictício. A identificação da conta é conferida ao retomar e interagir.

A API isolada recebe a tabela de itens, CRUD por UUID estável, autorização, limpeza na exclusão da viagem, notas/memos até 4000 caracteres e requestId de criação para reenvio seguro. Testes verificam que alterar uma nota da viagem não sobrescreve o memo ou a nota rápida da parada.

Foi conferido novamente o acesso à fonte API e tentada a aplicação do patch conforme autorização explícita do responsável. A gravação foi rejeitada automaticamente: a política não permite escrever fora das raízes autorizadas nem pedir ampliação. Portanto o original não foi modificado. O patch cumulativo [itinerary-notebook.patch](../backend-patches/itinerary-notebook.patch) está pronto, e seu check passou. Ele contém os pacotes 2 e 3 e substitui o patch anterior quando nenhum pacote foi aplicado. Instruções e situação em [backend-patches/README.md](../backend-patches/README.md).

Verificação: 17 testes Kotlin passaram; fontes com R real e recursos Android foram verificados. Os dois testes H2/transacionais dos pacotes 2 e 3 passaram em Maven na API isolada, incluindo independência dos tipos de nota, idempotência, propriedade e exclusão de vínculos. Não houve teste HTTP, publicação, emulador ou build Gradle completo. A aplicação da API original continua pendente, não deve ser marcada como concluída.

## Pacote 4 — Gastos e orçamento: implementação de 18/11

Android: `TripExpensesActivity`/`TripExpensesViewModel`/`ExpenseRepository` implementam criação, edição, exclusão confirmada, categoria, data e valor de despesa. Resumo apresenta total da viagem, orçamento/saldo quando definidos, categorias e reembolsos informados. Filtrar categoria afeta somente a lista. Gastos efetivos, orçamento planejado e estimativas de paradas são conceitos distintos; o app não converte um no outro. Valores são tratados em centavos inteiros e formatados por BigDecimal.

Estados de reembolso: não reembolsável, a solicitar, solicitado e recebido. São marcações manuais; não representam uma integração com empresa/banco. O total gasto não é reduzido ao marcar um reembolso recebido. Zero é permitido como orçamento, mas despesas têm valor positivo. Cada lançamento respeita o limite de centavos do formulário; somas usam Long e podem ultrapassar Int sem perda de precisão.

O formulário preserva valores e IDs estáveis em erro/rotação e no estado salvo da Activity, confirma descarte e impede envio duplicado. API usa UUID, versão da despesa e bloqueio de escrita por viagem; conflito não sobrescreve outro registro e editar registro removido não o recria. Painel da viagem exibe total e saldo e recarrega após retorno. Ausência de integração é explicitada, sem total zero fictício.

Comprovantes: anexar, abrir, substituir e remover PDF/PNG/JPEG de até 5 MiB, com propriedade conferida no servidor. Dados ficam como BLOB privado da despesa, sem link público. Download autenticado usa attachment/no-store/nosniff. Android lê o arquivo selecionado em IO com limite, envia multipart e abre cópia em cache privado via FileProvider não exportado e concessão temporária de leitura. Remover comprovante não apaga despesa; remover despesa/viagem remove o arquivo associado. A verificação de assinatura identifica o formato, sem afirmar inspeção completa do conteúdo.

O suporte da API está no patch cumulativo vigente [itinerary-notebook-expenses.patch](../backend-patches/itinerary-notebook-expenses.patch), incluindo os pacotes 2–4. `git apply --check` passou no checkout original. A escrita continua bloqueada pela política já registrada; nenhum patch foi aplicado no original nem publicado. Usar apenas o cumulativo vigente se os anteriores não tiverem sido aplicados.

Verificação: 28 testes Kotlin passaram (formulários, ordem/dias, notas, moeda/saldo e arquivos limitados por tamanho/formato), fontes com R real e compile/link AAPT2 passaram. Três integrações Maven/H2 dos pacotes 2–4 passaram; o teste financeiro cobre orçamento ausente/zero, soma acima de Int, categorias, reembolso, idempotência, edição/exclusão concorrentes, proprietário, comprovantes e limpeza de vínculos. A verificação adicional de cabeçalhos e exclusão de comprovante com viagem também passou. Gradle completo, APK, emulador, HTTP multipart e implantação pública não foram verificados; não marcar a integração original como concluída.

### Contexto imediato para o pacote 5

Primeiro verificar se o cumulativo 2–4 já foi aplicado e se a fonte API recebeu permissão de escrita. A autorização do responsável já existe; fazer check/aplicação/testes quando tecnicamente possível, preservando alterações posteriores. Não aplicar cumulativos em sequência. Enquanto a fonte estiver restrita, continuar sobre a cópia isolada com os pacotes anteriores e salvar patch cumulativo revisável.

Próximo escopo: atualizar perfil de verdade e persistir interesses. Entradas Android: `ProfileActivity`, `EditProfileActivity`, `UserRepository.update` (ainda retorna indisponibilidade), `UserViewModel`, `AuthRepository.updateStoredUser` e preferências de modo. API: `UserController`, `UserInterestController`, `InterestController`, modelos, Security/JWT e verificações de proprietário. Conferir o contrato real de atualização antes de enviar; nunca publicar senha/hash nem permitir trocar o usuário proprietário por ID no corpo.

Troca de e-mail exige estratégia explícita para JWT cujo sujeito é o e-mail: retornar nova sessão válida ou solicitar nova autenticação de forma coerente, sem manter identidade antiga em cache. Retornar ao Perfil deve recarregar nome/e-mail/interesses salvos; erro preserva formulário. Interesses devem alimentar apenas o contexto autorizado da Vi e o modo do perfil apenas preencher novas viagens, sem reescrever viagens existentes. Revisar estados de carregamento/vazio/erro e a direção visual antes de encerrar o pacote.


## Pacotes 5–9 — implementação de 18/11

Perfil/interesses, descoberta/avaliações, IA, mapa/clima e conta/portabilidade foram implementados. O backend cumulativo vigente é [all-packages.patch](../backend-patches/all-packages.patch), substituindo os cumulativos anteriores no checkout que ainda não recebeu nenhum pacote. A paleta compartilhada passou para floresta/marfim/sálvia, mantendo Kotlin/XML e Material. Há perfil optativo H2 persistente sem seed, testado após reinício.

Verificação final: 30 testes Kotlin, recursos AAPT2 vinculados e todos os fontes Kotlin checados com R real; oito testes API passaram, incluindo persistência e assinatura Google. Detalhes, contratos e limites em [remaining-packages.md](remaining-packages.md). Não houve montagem/instalação do APK, publicação ou chamada real a IA/e-mail/Google/Stripe.

## Fila operacional restante

| Ordem | Trabalho | Evidência para encerrar |
| --- | --- | --- |
| 1 | Aplicar cumulativo na fonte API quando a área estiver gravável | Check, testes e revisão das diferenças sem perder alterações locais |
| 2 | Configurar JWT, IA, e-mail e Google; manter Stripe em testes | Segredos somente no servidor, callback HTTPS e remetente verificado |
| 3 | Ativar H2 persistente em volume durável | Backup/schema revisados e reinício da implantação sem perda |
| 4 | Publicar API e montar/instalar Android | Contratos disponíveis e APK compilado pelo Gradle |
| 5 | Executar jornadas e regressões reais | Google/reset, IA/quota/conflito, mapa/clima, PDF/offline, recibos e Stripe |
| 6 | Ajustar visual/acessibilidade e concluir materiais | Capturas reais, texto ampliado/teclado, demonstração e limitações atualizadas |

Streaming, voz, anexos na conversa, colaboração, notificações e duplicação de viagem seguem adições opcionais. Não substituem a conclusão da fila operacional acima.
