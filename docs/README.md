# Documentação do Viajá

Revisão: 05/11/2026. Entrega confirmada: 02/12/2026. Android: branch `expansion`.

## Por onde começar

| Pergunta | Documento que responde |
| --- | --- |
| O que o produto deve fazer e quais são suas regras? | [Produto e experiência](product-baseline.md) |
| O que existe no código, como se comunica e quais dados usa? | [Arquitetura e dados](architecture.md) |
| O que funciona, o que está parcial e o que falta? | [Estado da implementação](status.md) |
| Em que ordem trabalhar e como concluir cada etapa? | [Plano de execução](recovery-plan.md) |
| O que demonstrar e entregar à faculdade? | [Entrega acadêmica](academic.md) |
| O que foi aproveitado, corrigido ou arquivado nos documentos antigos? | [Revisão documental](documentation-audit.md) |

Os três primeiros documentos têm papéis diferentes: requisito, estrutura e evidência. Uma decisão de produto não comprova implementação; um arquivo de tela não comprova um fluxo; uma compilação não comprova comportamento.

## Autoridade e manutenção

- Conversa e decisões explícitas do responsável prevalecem sobre material antigo.
- O produto vigente está em `product-baseline.md`; não replicar sua matriz de planos em outros arquivos.
- O estado atual está em `status.md`. Evidências históricas ficam em [history](history/README.md).
- O README da API explica execução e contratos existentes. Swagger deve ser conferido com o código: ainda há exemplos incorretos.
- O Obsidian funciona como entrada para estas referências e preserva fontes do grupo. Seus documentos antigos não competem com este índice.
- Uma funcionalidade muda de estado somente com evidência do fluxo correspondente. Registrar a limitação quando a verificação for apenas local, da API ou de uma tela.
- Adicionar arquivos de documentação apenas quando houver outro público ou outra pergunta recorrente. Não criar uma especificação por classe nem copiar contratos do Swagger manualmente para vários lugares.

## Convenções

Documentação e interface podem usar português claro; código e identificadores permanecem em inglês. Não adicionar comentários ao código. Criar commits semânticos e preservar mudanças anteriores do usuário. As datas de commits configuradas a pedido do responsável não são evidência da data de execução ou da duração do trabalho; a revisão documental usa a data da sessão e evidências descritas.

Valores de demonstração, hipóteses comerciais e decisões ainda abertas devem aparecer como tais. Protótipos, prompts e preços de serviços externos antigos são referências históricas, não critérios de aceitação.
