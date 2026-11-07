# Viajá — entrega acadêmica

Revisão: 05/11/2026. Requisitos abaixo foram recuperados dos documentos do grupo; não substituem novos enunciados do professor.

## Identificação

Projeto Integrador do 4º semestre, Sistemas para a Internet, SENAC. Disciplina registrada: 2603 — Projeto Integrador: Sistema Web Modularizável. Equipe registrada: Maria Clara De Jesus Estevam, Rafael Reis e Victor Curtis. Orientador registrado: Alexandre Lopes Santos. Período registrado: 12/08 a 02/12/2026; entrega final confirmada pelo responsável para 02/12/2026.

Tecnologias que o responsável confirmou manter: Kotlin, XML Views, Retrofit, RecyclerView, Intents e backend Java/Spring Boot com H2/LoadDatabase. A antiga pergunta sobre iniciar a migração para Spring não é mais bloqueio: o backend já existe. Não transformar isso em prova de uma exigência formal do orientador que ainda não foi apresentada.

## ADO 2

O mapa mental precisa mostrar API → Model → Adapter/ViewHolder → RecyclerView/tela, com endpoint, método HTTP, conversão de JSON, `onCreateViewHolder`, `onBindViewHolder`, `getItemCount`, layout da lista e layout do item. Usar setas/legenda e trechos reais do código do grupo.

O fluxo atual de lugares é candidato demonstrável:

- `ApiService.searchPlaces`: `GET /places/search`, não `/api/places/search` nem PHP.
- `Place` em `data/model/Model.kt`: modelo de leitura.
- `PlaceRepository` e `PlaceViewModel`: solicitação e estado existentes.
- `PlaceAdapter`: criação, vínculo de views e contagem.
- `PlaceListActivity`, `activity_place_list.xml` e `item_place_card.xml`: lista real.

Não copiar trechos de um prompt antigo nem inventar assinaturas. Conferir os arquivos e o commit selecionado no momento de montar o material.

## ADO 3

Inclui o fluxo anterior e acrescenta Intents, dados transmitidos e recebidos e ligação entre XML e Kotlin. Exemplo já demonstrado: `PlaceListActivity` envia `place_id` para `PlaceDetailsActivity`, que lê o identificador e consulta o detalhe. Outro exemplo é `trip_id` para o dashboard da viagem.

A tradução do exemplo de comércio para o domínio Viajá é explicativa: lugar corresponde ao item da lista, detalhe ao item escolhido e roteiro à composição da viagem. Não descrever o app como um carrinho de compras.

## Formato e pendências

Fontes registram mapa em PDF no Blackboard, com padrão `ADO2_NomeSobrenome` e `ADO3_NomeSobrenome`. Ferramenta de desenho é livre. Os prazos individuais das ADOs e seu estado de entrega não foram confirmados; não inventar datas nem afirmar que foram entregues.

O Diário de Bordo antigo contém o nome de template “Plataforma Integrada de Compras em Supermercados”. A correção desse documento externo precisa ser verificada antes de entregar; esta revisão não alterou o arquivo fora da pasta do projeto.

## Demonstração à banca

Separar o que funciona do que é hipótese: jornada Android/API; HATEOAS/paginação e respostas; dados iniciais identificados como demonstração; decisões de produto e custos ainda não medidos. Stripe será em testes e IA precisa ser chamada real quando apresentada como integração. Não chamar respostas fixas ou retorno de tela de processamento real.

Mostrar contribuição do grupo por evidências efetivas. Datas configuradas de commits não comprovam dia de execução, duração de trabalho ou autoria de cada trecho. Não usar os relatórios antigos “praticamente funcionais” como evidência de conclusão.

## Fontes preservadas

No Obsidian do projeto: `storage/Viajá - ADOs.md`, `storage/Viajá.md`, enunciados/diário citados e as cópias anteriores em `storage/review-2026-11-05/`. Os mapas/arquivos PDF finais ainda precisam ser localizados ou produzidos e conferidos; documentação de critérios não equivale à entrega do artefato.
