# Viajá — plano de execução

Revisão: 05/11/2026. Entrega: 02/12/2026. Prioridade atual confirmada: coerência de produto/documentação e direção visual antes de retomar a sequência técnica.

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
