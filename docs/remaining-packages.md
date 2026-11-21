# Entrega dos pacotes 5–9

18/11/2026. Complementa o [estado atual](status.md) e encerra a fila de implementação planejada; publicação e aceitação ponta a ponta permanecem abertas. Os pacotes 1–4 foram preservados e incluídos no backend cumulativo.

## Entregas

| Pacote | Resultado |
| --- | --- |
| 5 — Perfil | Editar nome/e-mail/modo e interesses de verdade; senha atual na troca de e-mail; nova sessão instalada após gravação; rascunho preservado em falha/recriação |
| 6 — Descoberta | Filtros e ordenação efetivos, paginação, busca sem diferença por acentos, fotos HTTPS disponíveis, fonte e ausência de dados explícitas; avaliações persistidas por conta/lugar |
| 7 — Vi | API OpenAI com resposta estruturada, histórico remoto, rascunho por conta, quota real, retentativa idempotente, revisão/aprovação de proposta e conflito contra alterações manuais |
| 8 — Destino | Mapa das paradas com coordenadas válidas, navegação externa e previsão identificada por fonte/data/fuso; estado de indisponibilidade sem inventar clima |
| 9 — Conta e portabilidade | Recuperação Resend, código temporário e reset; login Google pelo navegador; PDF básico/completo e leitura offline privada por conta, com benefícios aplicados no servidor |

A direção visual usa floresta, marfim e sálvia, com componentes Material e tema claro consistente. Não foi validada visualmente no APK. Dados demonstrativos do catálogo são identificados; não se transformam em informação comercial atual só porque chegam pela API.

Também foram fechados pontos de coerência: JWT criptografado no Android, invalidação de sessões após reset, revisão da propriedade de interesses, bloqueio de escrita pública de catálogo, arquivos offline fora do backup e perfil H2 persistente sem seed automático.

## Verificação executada

| Verificação | Resultado e alcance |
| --- | --- |
| Kotlin/JUnit | 30 testes aprovados; regras financeiras/roteiro/formulário, organização e HTML de mapa |
| Recursos Android | Compilação e vínculo AAPT2 com dependências locais, gerando a classe R real |
| Fontes Android | Checagem independente de todos os Kotlin de produção e testes JVM com dependências locais; não empacota APK |
| API Maven | Compilação, oito testes aprovados e empacotamento da cópia isolada |
| Roteiro/notas/gastos | Spring/H2/transações: propriedade, conflito, idempotência, limpeza e recibos privados |
| Integração 5–9 | Perfil/JWT/interesses, busca/avaliação, quota/retentativa/proposta/conflito, direitos de export/offline, recuperação e fluxo Google com gateway substituto |
| Google criptográfico | RSA/claims/audiência/nonce/expiração/algoritmo com chave de teste; sem identidade real |
| Clima | Parsing com temperaturas ausentes e rejeição de estrutura inválida; sem chamada meteorológica em dispositivo |
| Reinício | Dois contextos Spring sobre o mesmo H2 em arquivo conservam conta, viagem e Premium de fixture |
| Patch | `git apply --check` aprovado contra o checkout original; nenhuma escrita nele |

Os testes estão nos diretórios `app/src/test` e, para a API, no patch em `src/test/java`. A regressão instrumentada de sessão foi atualizada para o token criptografado, mas não executada nesta rodada. Não converter os números de testes em percentual de conclusão.

## O que continua necessário para operar

1. Aplicar o [patch cumulativo](../backend-patches/all-packages.patch) na fonte API quando gravável, configurar os serviços e publicar. [Instruções e variáveis](../backend-patches/README.md).
2. Montar e instalar APK pelo Gradle, executar jornadas com rede real e revisar teclado, fonte ampliada, retorno entre telas e expiração de sessão.
3. Validar Stripe em testes, e-mail recebido, consentimento Google, resposta real do modelo, mapa/clima, PDFs extensos, comprovantes e leitura offline após reinício/logout.
4. Configurar volume durável da hospedagem e revisar schema/backup, preparar catálogo demonstrativo coerente e concluir materiais acadêmicos com capturas reais.

A escrita na API original foi rejeitada pela política automática da sessão, apesar da autorização já concedida; o patch preserva toda a implementação. Não houve deploy, commit, cobrança ou chamadas reais aos provedores de IA/e-mail/Google nesta rodada.

## Limites funcionais explícitos

Interromper a Vi cancela a espera no app; o servidor pode concluir a geração. Propostas substituem o roteiro mediante revisão, não acrescentam alterações silenciosamente. IA não possui streaming, voz ou anexos, nem acesso autônomo para escrever notas/despesas. O modelo aceita apenas lugares do catálogo da cidade, que ainda é pequeno.

Offline é snapshot de leitura: não oferece edição/sincronização, mapa, clima, anexos de recibos ou IA sem rede. Download novo exige Premium; expiração do plano não apaga o arquivo existente. Sessão expirada preserva acesso local da conta; logout explícito bloqueia até nova autenticação. PDF completo inclui notas/memos/checklist e resumo financeiro, sem incorporar recibos.

A API padrão continua efêmera; somente o perfil `persistent` e um volume durável conservam dados. O teste de reinício não comprova webhook/renovação Stripe na hospedagem. Catálogo filtra em memória e precisa de outra estratégia se crescer. Notificações, colaboração, moderação de avaliações e compartilhamento continuam fora destes pacotes.
