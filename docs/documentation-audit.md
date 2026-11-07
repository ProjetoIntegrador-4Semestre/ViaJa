# Revisão documental — Viajá

Revisão em 05/11/2026. Escopo de entrada: 30 Markdown, sendo 25 no projeto do Obsidian, 3 no Android e 2 na API. As imagens foram inventariadas como referências históricas; esta revisão não certifica todos os protótipos nem consulta os serviços externos vinculados a cada fonte antiga.

## Resultado da avaliação

### Aproveitado

- Jornada descobrir → planejar → organizar → viajar; turismo e trabalho no mesmo produto.
- Salvos separados do roteiro, modo por viagem, datas flexíveis, centavos e proposta de rascunho aprovado pelo usuário.
- Requisitos das ADOs e exemplos de fluxo entre API, modelos, Adapter, RecyclerView, XML e Intent.
- Material do grupo como origem de requisitos e perguntas, sem transformar o desenho em prova de implementação.
- Simplicidade dos exemplos acadêmicos: Retrofit/callbacks e controllers/repositories/assemblers, sem arquitetura especulativa.

### Retirado da referência ativa

- Repetições do mesmo produto/contrato em vários compêndios e duas cópias importadas de contexto/sitemap.
- Catálogos de classes e nomes de layouts que mandavam criar dezenas de arquivos antes de definir os fluxos.
- Contagens “27 views” e “tudo praticamente funcional”, sem correspondência demonstrada entre código e comportamento.
- Compose, Fragments/NavGraph obrigatórios, pacote `com.viaja.app`, Postgres/Supabase/RLS/Flyway como premissas do projeto atual.
- Catálogo Google/OSM/Gemini com preços, quotas e versões antigos; troca de provedor “em uma linha” sem integração existente.
- Premium por flag, IA ilimitada, preços/ciclos conflitantes, falsas telas de cartão e obrigações herdadas de uma persona visual.

### Contradições corrigidas

1. API já é Spring/Java/H2; “confirmar para começar a migração” deixou de ser próxima ação.
2. Código usa IDs Long, não a migração UUID afirmada pelo registro de 02/11. O registro também nomeia Expense/BoardingCard/Itinerary controllers ausentes. Essas alegações foram classificadas como não comprovadas, não como funcionalidades que certamente existiram.
3. Banco de entrega não é o DDL PostgreSQL do documento anterior. A FK histórica de uma coluna para chave composta era inválida; o TripMemo atual já declara associação JPA composta. Seu ciclo de exclusão ainda precisa de correção.
4. Rotas reais não têm prefixo `/api`; login atual recebe `senha` e devolve o JWT em header, contrariando exemplos que colocavam `password`/token no corpo.
5. Preço do lugar em centavos não é priceLevel; o catálogo paginado/escrita ainda difere da leitura PlaceResponse.
6. Compilar, listar classes e existir um arquivo não comprovam Google/reset, clima, mapa, IA, pagamentos ou perfil funcional.
7. HATEOAS/paginação existem, mas não em todas as listas; nem toda declaração `@Valid` possui restrições efetivas.
8. Cópias de relatório e prompts podem dizer “fonte única de verdade”; essa expressão dentro de material histórico não lhes dá autoridade atual.

## Inventário e destino dos 30 documentos de entrada

“Vault” significa `/Users/rafa/Library/Mobile Documents/iCloud~md~obsidian/Documents/second-brain/05_projects/active/viaja`. Originais das nove notas de raiz e do índice storage foram preservados em `storage/review-2026-11-05/`. As demais fontes em storage permanecem intactas.

| Origem | Documento | Avaliação e destino |
| --- | --- | --- |
| Android | README.md | Introdução útil, insuficiente como guia; substituído por entrada para índice/documentos vigentes |
| Android | docs/product-baseline.md | Direção recente válida, concentrada em Stripe; ampliada/reorganizada para todas as jornadas, limites e telas |
| Android | docs/recovery-plan.md | Evidências úteis misturadas com cronograma e baselines obsoletos; original em history, plano atual separado do status |
| API | README.md | Execução/testes e contratos úteis; mantidos, com mapa de autoridade para evitar nova duplicação de produto |
| API | HELP.md | Template Spring gerado e ignorado pelo Git; não é requisito nem documentação de produto, deixado como referência auxiliar |
| Vault | viaja.md | Hub útil com estado/próxima ação antigos; refeito como entrada para documentos atuais |
| Vault | product.md | Regras aproveitáveis, escopo ainda comandado por protótipo; substituído por referência à especificação atual |
| Vault | decisions.md | Decisões e perguntas misturadas; refeito para distinguir confirmado, decidido sob delegação, pendente e substituído |
| Vault | architecture.md | Mistura PHP atual/Spring futuro e inventário prematuro de classes; original arquivado, referência técnica aponta para código/arquitetura atuais |
| Vault | database.md | Modelo conceitual útil, DDL/enum/RLS/FK incompatíveis com código atual; original arquivado, entrada aponta ao inventário JPA e lacunas reais |
| Vault | academic.md | Critérios úteis com nota de ausência de listas desatualizada; conteúdo atualizado centralizado em docs/academic.md |
| Vault | plan.md | Próxima ação de migração e estado de 21/10 superados; entrada aponta a plano e status separados |
| Vault | progress.md | Relatório com alegações não comprovadas e instruções antigas; cópia exata arquivada, entrada aponta a evidências atuais |
| Vault | api-changes-2026-11-02.md | Afirma UUID e controladores inexistentes; arquivado como relato não comprovado, sem valor de validação atual |
| Vault/storage | README.md | Boa intenção de separar fontes, mas diz que raiz não tem conflitos e descreve PHP como atual; substituído por índice histórico explícito |
| Vault/storage | Viaja Context.md | Fonte de visão/terminologia, mas mistura Compose, Postgres, preços e Premium mock; manter somente como histórico |
| Vault/storage | viaja-context-ae8a71b2973720db.md | Corpo duplicado de Viaja Context; diferenças apenas de importação/metadados. Não consolidar novamente nem ler como outra decisão |
| Vault/storage | Viaja Sitemap - Views.md | Relações de navegação úteis como pesquisa; contagem e classes prematuras/conflitantes. Sem ordem de criar arquivos |
| Vault/storage | viaja-sitemap-views-f8f85462096e1ffc.md | Corpo duplicado do sitemap; diferenças de importação/metadados. Histórico apenas |
| Vault/storage | Viaja Decisões Consolidadas.md | Decisões históricas relevantes, mas “status final” de setembro não prevalece sobre o responsável/código atuais |
| Vault/storage | Viajá.md | Bom registro do baseline de 21/10 e fontes do grupo; preservar sem tratar PHP/ausências daquele dia como estado atual |
| Vault/storage | Viajá - Funcionalidades e Fluxos de Usuário.md | Fonte mais útil para jornadas; resgatar notas, gastos e aprovação da IA. Separar opcionais e substituir plano/Premium por decisão vigente |
| Vault/storage | Viajá - Especificação Produto e Banco.md | Mapeamento conceitual útil; UUID, /api, tabelas futuras e matriz “Sim” não provam implementação. Histórico apenas |
| Vault/storage | Viajá - ERD do Banco.md | Diagrama original e lacunas são úteis; não é schema atual nem SQL executável |
| Vault/storage | Viajá - Mind Map do Grupo.md | Preserva intenções e perguntas; não fixa provedor, API paga, automação ou reservas |
| Vault/storage | Viajá - ADOs.md | Fonte acadêmica útil; afirmação de que não existem listas já superada. Requisitos consolidados no documento acadêmico atual |
| Vault/storage | Viajá - Migração para Spring Boot.md | Registro de decisão histórica; não manter a migração como trabalho futuro nem inferir formalidade acadêmica não documentada |
| Vault/storage | prompt-mapa-mental-figma-ai.md | Prompt instrui citar PHP e negar existência de listas; arquivado, não reutilizar para gerar evidência atual |
| Vault/storage | prompt-mapa-mental-get-fluxo.md | Estrutura de mapa aproveitável, endpoint/baseURL antigos e proibição de distinguir status; não usar como contrato nem fonte factual |
| Vault/storage | prompt-viaja-product-stitch-b3810ee12f306b50.md | Princípios de UX aproveitáveis; instruções de gerador, exemplos e telas não são requisitos aprovados. Sem nova geração automática a partir dele |

## Nova responsabilidade de cada documento

- `docs/product-baseline.md`: produto, regras, planos, cobrança e estados de tela; não contém status de conclusão.
- `docs/architecture.md`: implementação observada, contratos, dados e limitações; sem DDL imaginado nem inventário de classes futuras.
- `docs/status.md`: matriz atual e evidência, incluindo falhas de segurança e verificações locais.
- `docs/recovery-plan.md`: ordem por critérios de saída, sem cronograma fictício de funcionalidades prontas.
- `docs/academic.md`: critérios do curso, código que pode ser demonstrado e artefatos ainda pendentes.
- Este arquivo: justificativa da reorganização e rastreabilidade das fontes. Não deve crescer como outro backlog.

README/índice e notas de entrada do Obsidian apenas encaminham para esses papéis. O README da API continua dono das instruções de execução/teste e descrições dos recursos existentes. Não há necessidade de criar uma documentação por classe ou manter uma cópia de toda especificação no vault.

## Preservação e verificações

- Antes da alteração, foram inventariados caminho, linhas e SHA-256 dos 25 Markdown do vault. `inventory.json` acompanha os snapshots.
- As nove notas de raiz e o README de storage têm cópias exatas; 15 fontes históricas restantes e os assets não foram reescritos.
- O plano Android anterior foi preservado integralmente em `docs/history/recovery-2026-11-05.md`.
- Links atuais, arquivos citados e destinos do índice devem ser verificados após a gravação. Links e afirmações dentro das cópias históricas permanecem como parte do registro; não são promessa de validade atual.
- Esta rodada altera documentação, não a implementação. Não reexecutar uma build e chamá-la de validação de texto; verificar coerência, navegação e preservação dos originais.

O protocolo de captura do vault também recebeu um registro curto na daily de 05/11/2026, ligado ao projeto. Não foi copiada a conversa nem alterado conteúdo pessoal anterior. As datas de revisão permanecem reais, independentemente das datas configuradas nos commits.
