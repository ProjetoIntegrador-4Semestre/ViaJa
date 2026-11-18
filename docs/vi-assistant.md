# Vi — assistente de viagens

Premissa autorizada em 17/11; implementação atualizada em 18/11/2026. A imagem do protótipo orienta hierarquia e conversa, sem exigir cópia literal. A Vi combina contexto autorizado, dados do catálogo e propostas revisáveis; sua capacidade depende das integrações e da qualidade desses dados.

## Composição

A conversa ocupa o centro; usuário à direita, Vi à esquerda. Cabeçalho e contexto são compactos; quota e ações aparecem quando úteis. Sem barras promocionais, amenidades fictícias ou botões de voz/anexo sem implementação. O teclado deve preservar o compositor; validação visual no APK ainda está pendente.

| Token | Cor | Uso |
| --- | --- | --- |
| vi_canvas | #F6F7F3 | Fundo marfim |
| vi_ink | #183C34 | Texto e identidade |
| vi_primary | #176B54 | Balão do usuário e ação principal |
| vi_muted | #576B63 | Texto auxiliar |
| vi_sage | #E7EFE8 | Contexto e superfícies |
| vi_border | #D6E1D8 | Contornos |
| vi_warning | #805520 | Avisos |

A direção floresta/marfim/sálvia agora é compartilhada com as demais jornadas. Fonte Plus Jakarta Sans, superfícies claras, alvos de toque de 48 dp e estados por texto além de cor. O tema permanece claro também no modo noturno do sistema; uma variante escura completa continua futura.

## Sessões e contexto

Sessão tem UUID, título, vínculo imutável opcional à viagem e mensagens remotas por proprietário. O Android mantém rascunho, modo/ritmo e histórico local separados por conta, fora do backup. Nova conversa, retomada, consulta de histórico e exclusão remota estão implementadas; a lista local só é removida após confirmação do servidor. Visitante não recebe acesso a chamadas privadas.

Entrar pela viagem vincula o ID, não apenas o nome. Modo/ritmo da conversa não alteram silenciosamente o cadastro da viagem. O servidor monta contexto com viagem própria, orçamento, roteiro atual, interesses e até 100 lugares da cidade, incluindo fonte. Usa as últimas oito interações como contexto limitado. O catálogo seed é demonstrativo, não comprova preços ou disponibilidade reais.

## Integração e ações

Android → API Viajá → OpenAI Responses, com saída JSON estruturada e `store=false`. Chave/modelo são obrigatórios no servidor. Sem configuração, a API retorna indisponibilidade; não há respostas fixas fingindo execução.

Mensagem livre responde em texto. “Gerar/revisar roteiro” produz proposta com IDs de lugares, dias, ordem, horário opcional e notas; o servidor valida estrutura, propriedade e catálogo. Viagem sem datas exige duração explícita; proposta aceita de 1 a 30 dias e até 150 paradas, sem repetição do mesmo lugar pela chave composta atual. Horários propostos são sugestões, não confirmação de funcionamento.

A revisão mostra a proposta antes de aplicar. Aprovação substitui o roteiro inteiro e pode remover memos das paradas excluídas, conforme aviso na tela. O snapshot capturado antes da geração impede sobrescrever alterações manuais posteriores. Repetir envio ou aplicação mantém uma única ação; só resposta confirmada permite mostrar aplicação concluída. Não há escrita autônoma de notas, gastos, reservas ou serviços externos.

## Consumo e falhas

A quota é consultada no servidor: 2 planos concluídos por mês civil no gratuito e 20 por ciclo pago no Premium, com data de renovação. Existe teto operacional separado de 40/200 interações diárias (gratuito/Premium); conversa livre não consome plano. Cada plano também ocupa uma interação diária. Falha de provedor/validação não consome; falha ao aplicar não devolve a geração já concluída. Edição manual e consulta permanecem disponíveis após esgotar quota.

Entrada até 2000 caracteres; saída textual até 8000; chamada ao provedor limitada por timeout. “Interromper” cancela a espera no Android, preservando pedido e chave de retentativa. Isso não garante cancelamento ou estorno no provedor; sincronizar/repetir a mesma solicitação recupera o resultado que o servidor já concluiu, sem nova geração confirmada.

Solicitações são salvas com UUID e conteúdo; reutilizar chave para conteúdo diferente é conflito. Perda de rede preserva o pedido sem afirmar falha definitiva ou sucesso. Propostas e consumo só chegam de resposta real do servidor.

## Limites e evidência

Implementados em código: sessões remotas, provedor, quota, retentativa, propostas e aplicação. Ainda opcionais: streaming de tokens, voz, anexos, comparação visual de lugares e ferramentas de escrita para notas/despesas. Clima/transporte/hotel não entram como fatos presumidos no texto do modelo.

Testes da API com gateway substituto verificam geração, quota, repetição, falha sem consumo, aplicação, conflito e isolamento. Os 30 testes Kotlin e o compile/link AAPT2 passaram no conjunto do app. Não houve chamada real ao modelo, deploy, montagem de APK ou validação visual em dispositivo. A API original depende de aplicação do [patch cumulativo](../backend-patches/README.md).
