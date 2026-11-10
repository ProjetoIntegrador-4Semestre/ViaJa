# Viajá — relatório de mudanças para a Home e o roteiro

10/11/2026 · Android em `expansion` e API em `develop`. Diagnóstico baseado no código local, na captura enviada e em consultas a uma instância isolada da API. Este relatório registra mudanças propostas. A revisão visual usa a captura e a [referência de produto](product-baseline.md). Os protótipos completos ficaram fora da revisão, pois não estavam nos checkouts examinados.

## O que existe hoje

- **Início** carrega cidades pelo catálogo e usa a busca para abrir a lista de lugares. A viagem do usuário, o roteiro e os salvos ficam em fluxos separados. [HomeActivity.kt](../app/src/main/java/com/senac/viaja/ui/main/HomeActivity.kt)
- **Viagens** já lista as viagens da conta. Ao abrir uma, o painel carrega os dados e o nome da primeira parada. O campo visual de horário recebe “Dia X • Parada Y”. A tela mostra “Clima indisponível”. Existe uma rota que entrega todas as paradas em ordem, mas ainda falta uma tela para percorrê-las. [TripRepository.kt](../app/src/main/java/com/senac/viaja/data/repository/TripRepository.kt), [TripDashboardActivity.kt](../app/src/main/java/com/senac/viaja/ui/trips/TripDashboardActivity.kt)
- **A captura é de Explorar.** O quadrado vem da imagem fixa de praia no topo. O ícone à direita é o atalho antigo para edição do perfil. “Falar com IA” é um segundo acesso à Vi. Há resíduos equivalentes na Home, na lista de lugares e no painel de viagem. O cabeçalho compartilhado já oferece o acesso principal à Vi. [activity_explore.xml](../app/src/main/res/layout/activity_explore.xml), [PageChrome.kt](../app/src/main/java/com/senac/viaja/ui/main/PageChrome.kt)

## Home proposta

A primeira dobra mostra o nome da viagem, cidade, datas e a próxima parada, com o acesso **Ver roteiro**. Abaixo, entram a nota geral, o memo da parada em destaque e um resumo de salvos. Clima e recomendações por localização podem entrar como cartões secundários à medida que houver dados reais. A navegação e o cabeçalho atuais continuam servindo de base para a tela.

| Situação | O que aparece |
| --- | --- |
| Carregando | Um cartão simples com indicador de progresso e o cabeçalho utilizável. |
| Viagem ativa | Resumo da viagem e próxima parada disponível. Se houver mais de uma ativa, aplicar um desempate consistente, como início mais recente, com acesso às demais em Viagens. |
| Apenas viagens futuras | Mostrar a primeira por data com o rótulo **Próxima viagem**. Viagens encerradas ficam em Viagens. Viagens planejadas sem data podem aparecer como planejamento, com o rótulo **Datas a definir**. |
| Viagem sem paradas | **Seu roteiro está vazio**, com acesso a **Ver roteiro**. Se houver roteiro sem data, mostrar **Primeira parada do roteiro**. |
| Conta sem viagem ou visitante | Um cartão leve com **Explorar destinos** como ação principal e texto apontando para o ícone da Vi no cabeçalho. Para o visitante, a orientação informa que é preciso entrar para ver as próprias viagens. |
| Erro de conexão | Mensagem e **Tentar novamente**. Se a viagem já estiver carregada e apenas um cartão falhar, o erro fica naquele cartão. |

Proposta de visual baseada no app atual: fundo creme, superfícies claras, verde escuro nos títulos e ações com contraste. Uma coluna rolável em telas estreitas, cartões com altura flexível, sem imagem genérica no topo. Em tela pequena ou com fonte ampliada, nome, estado da viagem e ação principal continuam legíveis. A temperatura, quando integrada, deve indicar a cidade ou localização a que se refere e o horário da atualização.

## Dados e dependências

| Parte | Reaproveitar agora | Trabalho necessário |
| --- | --- | --- |
| Seleção de viagem | Lista de viagens da conta, datas e status, via `TripRepository`. A API já retorna `cityId`. | Levar `cityId` aos modelos Android usados pela Home, resolver o nome da cidade pelo catálogo e definir uma única regra de seleção usada no painel. |
| Próxima parada e horário | `GET /trip-locals/trip/{tripId}` entrega dia, ordem, lugar e nota curta. | A parada ainda não tem horário nem estado de conclusão. Na primeira entrega, usar **Primeira parada do roteiro** e **Horário a definir** quando faltar informação para determinar o que vem a seguir. Para calcular a próxima parada real, persistir horário e conclusão e comparar dia/horário da viagem. Exibir estado específico quando todas as paradas tiverem passado ou sido concluídas. |
| Roteiro completo | Reutilizar a mesma lista de paradas e a consulta de detalhe do lugar. | Criar uma Activity dedicada, aberta por Intent com o ID da viagem, com lista por dia, nome, horário disponível e acesso ao lugar. Começar pela consulta do roteiro completo. Edição de dia, horário e ordem pode evoluir depois. |
| Nota da viagem e memos | `TripNote` guarda a anotação geral. `TripMemo` guarda uma anotação vinculada a uma parada. A nota curta da parada já vem na resposta do roteiro. | A proposta trata “nota da viagem” como anotação, pois a avaliação existente é de lugares. Antes de conectar os endpoints de notas e memos, conferir o proprietário da viagem e o vínculo do registro em leitura e escrita. Depois, tipar as respostas Retrofit e mostrar um resumo de cada conteúdo. |
| Salvos importantes | `SavedRepository` já carrega favoritos da conta. | Começar com poucos favoritos e acesso à tela Salvos. A API ainda não fornece uma marca de “importante”. Priorizar a cidade da viagem quando houver vínculo confiável com o lugar. A carga atual resolve os detalhes de todos os favoritos em sequência. Na Home, consultar apenas o necessário para a prévia. |
| Lugares próximos | Os lugares do catálogo já têm latitude e longitude quando disponíveis. | Falta pedir localização em primeiro plano e consultar candidatos por distância. Para a demonstração, calcular a distância sobre um conjunto pequeno do catálogo, com limite de resultados. Com permissão negada ou posição indisponível, oferecer Explorar ou **Sugestões no destino**, com esse rótulo. O catálogo de demonstração tem poucos locais. |
| Temperatura atual | O painel atual já tem estado textual de clima indisponível. | Deixar a integração para depois. Usar um estado discreto ou omitir o cartão até o serviço existir. Temperatura, horário da leitura e falha de atualização entram juntos quando houver dados. |

### Verificação de acesso feita nesta rodada

Usei duas contas da carga de demonstração e apenas consultas de leitura depois do login. Para a viagem da primeira conta, a segunda recebeu **403** no detalhe e na lista de paradas, mas **200** na lista de notas da viagem, nos memos da parada e na coleção geral de memos. A proteção das notas e dos memos é pré-requisito para exibi-los na Home. A verificação foi feita no backend local, sem acesso à instância hospedada.

## Outras inconsistências para revisar depois

| Área | Achado e mudança proposta |
| --- | --- |
| Cabeçalhos duplicados | Na Home, remover `btnOpenProfile`, pois Perfil já está na barra. Em Explorar, remover `ivExploreAvatar`, `btnExploreSettings` e `btnExploreAiChat` e fechar o espaço do topo, preservando os chips. Na lista de lugares, remover o ícone de configuração sem ação. No painel de viagem, retirar `btnChatVi` e manter o contexto da viagem ao abrir a Vi pelo cabeçalho. |
| Explorar e resultados | A tela mistura cartões fixos de destinos, uma imagem de praia repetida e controles com aparência de filtro. Os chips da lista de lugares ainda não filtram. Quando a Home passar a ser painel, o catálogo real de cidades deve ficar em Explorar. Exibir filtros funcionais e alinhar a busca com acentos, inclusive “São Paulo”. |
| Detalhe de lugar | O botão de site ainda simula a ação, e a praia genérica aparece como foto do lugar. Usar link e imagem quando o catálogo fornecer dados confiáveis. Sem foto, mostrar um fundo neutro. |
| Adição ao roteiro | O fluxo já grava a parada, sempre no dia 1. O roteiro dedicado pode receber a escolha de dia quando a edição entrar no escopo. O cadastro de viagem atual também deixa as datas em aberto, por isso o estado **Datas a definir** precisa existir desde a primeira versão da Home. |
| Salvos | Um erro ao buscar o detalhe de um favorito interrompe a lista inteira. Separar a falha de um item da falha de carregar a coleção, especialmente antes de levar a prévia à Home. |
| Vi e acesso | A Vi responde com textos locais. O atalho do cabeçalho funciona, mas a geração real de roteiro ainda é uma integração separada. Recuperar e redefinir senha têm telas sem fluxo de envio. |
| Telas antigas e trabalho do grupo | Há layouts `fragment_*` sem uso nas rotas atuais e uma segunda tela de início em `MainActivity`. Mapear as rotas e a autoria antes de limpar arquivos. Perfil e Premium do grupo ficam fora da próxima intervenção visual, assim como a entrada legada associada ao commit do Perfil. |

## Ordem proposta e critérios de aceite

1. **Home e roteiro de leitura.** Montar o painel com os dados protegidos que já existem, manter Explorar como destino de descoberta e abrir o roteiro por Intent. Usar Activity, XML, Retrofit e os repositórios atuais. Para a primeira versão, as chamadas existentes bastam, sem criar um endpoint exclusivo de dashboard.
2. **Horário e notas.** Fechar a regra da próxima parada, persistir horário e conclusão, proteger notas e memos e então ligar os cartões ao servidor. Testar com outra conta, troca de dia, roteiro sem hora e ausência de parada futura.
3. **Ajustes de interface e dados complementares.** Retirar os atalhos duplicados nas telas autorizadas, validar rolagem e legibilidade no celular e acrescentar salvos, proximidade e clima conforme os dados estiverem disponíveis.

A revisão da Home deve cobrir conta com viagem ativa, futura e sem data, visitante, viagem sem paradas, falha de rede e retorno do roteiro após uma alteração. Para a nova tela, verificar se todos os dias com paradas aparecem na ordem correta e se cada lugar abre o detalhe correspondente.

Referências de contexto: [estado do projeto](status.md), [arquitetura e contratos](architecture.md) e [plano de execução](recovery-plan.md).
