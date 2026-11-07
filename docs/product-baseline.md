# Viajá — produto e experiência

Referência vigente, revisada em 05/11/2026. Este documento descreve o produto desejado. Consulte [status.md](status.md) para saber o que já foi implementado.

## Proposta e limites

Organizar uma viagem da descoberta ao uso do roteiro: encontrar lugares, planejar dias, registrar informações e gastos e consultar o plano durante a viagem. Turismo e Trabalho são contextos do mesmo produto, não aplicativos diferentes.

A entrega de 02/12/2026 será demonstrada por APK/emulador e API própria. A possível publicação na Google Play pertence à etapa posterior. Kotlin/XML, Retrofit, RecyclerView, Intents, Java/Spring Boot, H2 e LoadDatabase são restrições confirmadas. Não substituir a integração real por mensagens de sucesso ou dados locais disfarçados.

Permanecem no escopo as áreas confirmadas: acesso, descoberta, lugares, viagens/roteiro, salvos, perfil/interesses, IA, notas/checklists, despesas/recibos, mapas/clima, avaliações, histórico, assinatura, PDF e leitura offline. Ordem de implementação não significa corte de escopo. Reservas são encaminhadas a serviços externos; o Viajá não emite passagem, conclui reserva ou processa pagamento de hotel.

## Jornadas e regras

### Acesso e identidade

Visitante pode consultar o catálogo e detalhes. Ações que persistem dados pessoais pedem login; não criar uma conta fictícia nem usar o ID de outra pessoa. Cadastro por e-mail cria a conta e encaminha ao login. Login Google deve autenticar uma identidade verificável no servidor, sem aceitar um e-mail enviado pelo app como prova.

A sessão deve sobreviver ao reinício, expirar corretamente e ser encerrada por logout. Tela privada aberta com sessão expirada deve oferecer entrada novamente sem mostrar dados de outra conta. Recuperação usa link/token temporário e resposta que não revela se o e-mail existe; não anunciar envio sem o serviço funcionar. Mudança de e-mail precisa de tratamento da sessão, pois o JWT atual identifica o usuário pelo e-mail.

### Descoberta e lugares

Buscar por cidade ou texto e filtrar apenas atributos realmente disponíveis: categoria, preço com unidade, avaliação e outros campos sustentados pela fonte. Pesquisa deve tratar espaços e acentos de forma coerente. Não prometer disponibilidade de hospedagem ou abertura em tempo real sem fonte que as sustente.

Lugar exibe nome, endereço, categoria, descrição e, quando existentes, fotos, coordenadas, horários, comodidades, preço e avaliações. Ausência de dado não vira valor inventado. Sem foto, usar estado neutro; sem nota, “Sem avaliações”. Preço nulo significa desconhecido; zero pode significar gratuito. Não apresentar ingresso, diária e gasto estimado como o mesmo preço.

Salvar e adicionar ao roteiro são ações independentes. Abrir site ou navegação exige destino válido. O catálogo curado do servidor é válido para a demonstração se seus dados forem identificados como demonstrativos; consumir esse catálogo por Retrofit é integração real, não informação comercial em tempo real. Provedor externo e catálogo precisam de procedência/licença e revisão de dados.

### Viagens, dias e paradas

Uma viagem tem nome, uma cidade, modo, datas opcionais, orçamento opcional e proprietário autenticado. Datas são de calendário; fim não antecede início. Datas ausentes não autorizam a IA a inventar duração: o usuário informa quantos dias quer planejar. Valores em BRL são armazenados em centavos.

O modo pertence à viagem. A preferência do perfil apenas preenche o modo inicial de novas viagens. Turismo prioriza atrações e ritmo escolhido; Trabalho destaca compromissos, praticidade e despesas reembolsáveis. Isso deve produzir comportamento observável sem duplicar telas.

Roteiro mostra todos os dias e paradas, permite adicionar/remover, escolher dia/horário e alterar ordem. A inclusão atual no primeiro dia é um passo parcial, não a regra final. Reordenar precisa persistir. Atualizar cidade/datas deve resolver paradas incompatíveis antes de salvar. Excluir viagem/parada exige confirmação e tratamento dos vínculos existentes.

“Próxima parada” precisa considerar o dia/horário e conclusão/visita, não simplesmente o primeiro registro. Sem datas, mostrar “Primeira parada do roteiro”. Histórico mantém viagens concluídas, lugares e resumo de gastos. Status canônicos adotados para a entrega: `planned`, `active`, `completed`, `cancelled`; migrar o termo histórico `ongoing` na documentação, não alterar silenciosamente dados.

A repetição do mesmo lugar em diferentes paradas é uma limitação do modelo composto atual, a resolver com o editor. Ainda não foi aprovada uma migração de chave apenas por antecipação.

### Notas, checklists, gastos e avaliações

Notas gerais pertencem à viagem. Notas de parada pertencem à associação daquela viagem com o lugar, nunca ao catálogo global. Checklists precisam de itens e estado de conclusão; um campo de texto não implementa checklist.

Despesas têm categoria, valor em centavos, data e viagem. Recibo é anexo privado opcional. Resumo soma despesas e compara com o orçamento informado; reembolso tem estado próprio, sem fingir integração com uma empresa. BRL é a moeda inicial; conversão cambial não está confirmada.

Avaliações de lugar permanecem sem autor público e com nota de 1 a 5. A regra de repetição/moderação e a atualização de média/contagem precisam ser definidas antes de expor a escrita. Não transformar essa decisão em avaliação de viagem automaticamente. Estatísticas e badges só aparecem quando calculados de dados reais.

### IA, PDF e offline

A Vi gera ou revisa um rascunho usando contexto da viagem, interesses e lugares disponíveis. Deve validar IDs e estrutura da resposta, guardar a proposta e solicitar aprovação antes de inserir paradas. Não inventar reservas, horários ou preços. Falha deve preservar o pedido; nenhuma resposta fixa é apresentada como chamada real ao provedor.

A quota conta uma geração ou revisão substancial concluída, não cada mensagem. Falhas do provedor não consomem cota. Limitar entrada/saída e duração do roteiro; modelo e preços precisam de verificação na integração, sem versões herdadas de prompts antigos. Histórico bruto de conversa e personalização automática por viagens antigas não estão confirmados para a entrega.

PDF básico contém o itinerário. O pacote completo inclui roteiro, notas/checklists e resumo de gastos. Offline é leitura de uma viagem baixada explicitamente; não inclui sincronização colaborativa, mapas offline, IA ou clima sem conexão. Arquivos já exportados/baixados não são apagados por cancelamento; produzir/atualizar novos pacotes Premium exige acesso vigente.

## Gratuito e Premium

| Recurso | Gratuito | Premium |
| --- | --- | --- |
| Explorar, detalhes, salvos e planejamento manual | Incluído, sem limites artificiais de quantidade | Incluído |
| Notas/checklists e gastos básicos | Incluído | Incluído |
| Mapas, clima disponível, perfil e histórico | Incluído | Incluído |
| Pedidos de geração/revisão à Vi | 2 concluídos por mês de calendário | 20 concluídos por ciclo mensal da assinatura |
| PDF básico do itinerário | Incluído | Incluído |
| PDF completo da viagem | Não | Incluído |
| Viagem baixada para leitura offline | Não | Incluído |

Decisão de trabalho delegada pelo responsável: um plano Premium de R$ 19,90/mês. É preço inicial de demonstração e hipótese comercial, não preço validado por pesquisa. Sem plano anual, teste que vira cobrança, recargas de quota, anúncios ou descontos inventados nesta entrega. Limites e data de renovação da quota devem ser visíveis. Quota esgotada não bloqueia consulta nem edição manual.

A justificativa comercial é cobrar conveniência, portabilidade e trabalho poupado, preservando um produto gratuito útil. A viabilidade depende de receita menos processamento/Billing, IA, tributos aplicáveis, reembolsos e infraestrutura/suporte. Medir consumo médio e uso máximo da quota, além do custo dos usuários gratuitos. Não afirmar lucro com base apenas na mensalidade ou em cota gratuita de um provedor. Tarifas externas devem ser verificadas quando forem usadas; números antigos não são orçamento.

## Stripe: compra, retorno e acesso

Confirmado: Stripe em testes, com Checkout e webhook reais; não haverá cobrança real na banca. A decisão substitui a flag Premium simulada. Abrir Checkout hospedado em Android Custom Tabs, com navegador como alternativa. Não reconstruir campos de cartão em XML ou WebView. Usar Customer Portal para cobrança, método de pagamento e cancelamento.

1. Usuário autenticado escolhe o plano e vê preço, limites e cancelamento.
2. A API cria a sessão para o cliente Stripe associado ao usuário e um Price ID autorizado no servidor. App não define valor, dono ou URL arbitrária.
3. Android abre a URL do Checkout. Stripe coleta dados e conduz autenticações do pagamento.
4. A volta passa por uma URL controlada e abre o app. Preferir HTTPS App Links quando houver domínio; prever retorno explícito na demonstração. Não transportar JWT/segredos na URL.
5. App consulta a própria assinatura na API. O redirecionamento não libera Premium.
6. Webhook assinado confirma e atualiza estado, inclusive se o usuário não retornar.

Confirmar assinatura pode demorar. Mostrar “Confirmando sua assinatura”, opção de atualizar e continuar no gratuito. Fechar Checkout não significa falha de pagamento. Pagamento pendente não concede acesso. Renovação confirmada estende o período; falha não estende além do que já foi pago. Cancelamento é agendado para o fim do período e informa a data de encerramento.

O servidor aplica benefícios; CRUD genérico de assinatura não pode concedê-los. Distinguir status Stripe, período pago e cancelamento do acesso efetivo. Evitar compras ativas duplicadas. Verificar assinatura do webhook com corpo original, persistir IDs de eventos e aplicar atualização transacional; reentregas e eventos fora de ordem não duplicam benefícios. Reconciliar estado com o provedor. Sem fila, microsserviço ou framework de eventos para esse fluxo.

H2 em memória/create-drop não preserva clientes, assinaturas e eventos ao reiniciar. Manter H2 conforme a disciplina, preparando persistência em arquivo e carga inicial controlada para a demonstração de cobrança. Isso é requisito pendente, não configuração já aplicada. Chaves de teste/segredo do webhook permanecem no servidor; usar Stripe CLI no desenvolvimento. Não semear um usuário pago fictício como se tivesse assinatura verificada.

Publicação futura na Play exige revisar as regras de benefícios digitais e os programas aplicáveis ao mercado. Usar Stripe ou uma aba de navegador não resolve essa elegibilidade por si só.

## Direção visual e contrato das telas

Uma família tipográfica, componentes Material com cores explícitas, rótulos em português e uma única marcação de seleção. Paleta clara inicial: verde `#087A49`, creme `#F7F5EF`, superfície `#FFFFFF`, texto `#19382A`, secundário `#53675A`. Contrastes calculados: branco/verde 5,40:1; texto/creme 11,74:1; secundário/branco 6,08:1. Isso não é certificação integral de acessibilidade.

Espaçamento 8/16/24dp, margem de tela 16dp, alvo de toque mínimo 48dp, título de tela aproximadamente 24sp, seção 18sp, corpo 16sp e secundário 14sp. Altura acompanha conteúdo; suportar fonte ampliada, teclado, tela menor e modo escuro. Sem coordenadas absolutas copiadas de capturas. A família final e os tokens escuros serão validados na implementação, aproveitando as fontes já existentes.

| Área | Ação principal e conteúdo | Estados e restrições |
| --- | --- | --- |
| Entrada e acesso | Entrar, cadastrar ou visitar | Validação, envio, falha; Google/reset só anunciam sucesso real |
| Home | Próxima viagem ou criar primeira; busca secundária | Sem cartão gigante de saudação, estatísticas fictícias ou catálogo silenciosamente vazio |
| Explorar/lista | Pesquisa, filtros explícitos, resultados | Carregando, vazio, erro com tentativa; imagem ausente neutra |
| Lugar | Detalhe, favorito, adicionar à viagem, mapa/site | Sem galeria, comodidades ou disponibilidade inventadas |
| Viagens | Lista e formulário de criação/edição | Cidade obrigatória; datas/orçamento opcionais; confirmação ao excluir |
| Viagem/roteiro | Dias, paradas, notas e gastos | Edição deliberada; conflitos tratados; próxima parada real |
| Salvos | Lugares reais e acesso ao detalhe | Vazio leva à exploração; salvar não inclui no roteiro |
| Perfil | Identidade real, interesses, assinatura e sair | Sem pessoa fictícia, países inventados ou cartões de pagamento falsos |
| Vi | Contexto, quota, pedido, rascunho e aprovação | Limite, falha recuperável, revisão e aplicação explícita |
| Premium | Um plano, comparação com gratuito e preço | “Continuar no Stripe”; aviso de testes na demonstração |
| Retorno | Confirmando, ativo, pendência ou compra não concluída | Estado do servidor; não ativar por URL |
| Assinatura | Acesso, quota, renovação/fim e gestão | Portal Stripe; não duplicar telas de cartão/cobrança |
| Notas/gastos | Entrada simples no contexto da viagem/parada | Salvar real, falha sem apagar edição, confirmação de exclusão |

A revisão do emulador encontrou texto branco em fundo claro, roxo herdado do tema, seleção duplicada no menu, chips cortados, cartões muito altos e identidade fictícia. A prévia apresentada na conversa é uma proposta ilustrativa, não uma tela implementada. Protótipos antigos podem ajudar a entender intenções, mas não decidem regras nem sustentam contagens de páginas.

## Pontos que não devem ser inventados na implementação

- Escolha e condições de uso do mapa/clima; Open-Meteo é candidato histórico, não integração comprovada.
- Modelo Gemini, custos, limites e políticas atuais do provedor.
- Repetição/moderação de avaliações e retenção de rascunhos da IA.
- Anexos de notas, lembretes, cartões de embarque, agenda de reuniões e lugares personalizados: aparecem nas fontes antigas e precisam de escopo/aceitação antes de gerar tabelas ou páginas. Não estão sendo declarados concluídos nem descartados.
- Garantia de disponibilidade/preços de hospedagem, personalização automática de perfil e múltiplos destinos por viagem não foram aprovados para esta entrega.

## Referências de integração

Consultadas em 05/11/2026; verificar novamente quando da implementação/publicação:

- [Stripe Checkout](https://docs.stripe.com/payments/checkout), [confirmação e retorno](https://docs.stripe.com/checkout/fulfillment?ui=full-page), [eventos de assinatura](https://docs.stripe.com/billing/subscriptions/webhooks), [webhooks](https://docs.stripe.com/webhooks), [portal](https://docs.stripe.com/customer-management).
- [Android Custom Tabs](https://developer.chrome.com/docs/android/custom-tabs).
- [Política de pagamentos da Play](https://support.google.com/googleplay/android-developer/answer/9858738).
