# Viajá — estado da implementação

Revisão: 18/11/2026. Requisito: [produto](product-baseline.md). Planejamento: [plano de execução](recovery-plan.md). **Implementado** descreve código; **verificado** exige indicar o ambiente e o comportamento executado. A API original e a hospedada não receberam o patch desta rodada.

## Matriz atual

| Área | Código Android e API preparada | Validação ainda necessária |
| --- | --- | --- |
| Acesso e sessão | Login/cadastro existentes; JWT protegido pelo Keystore; sessão por conta; servidor exige segredo configurado e revoga sessões após reset | Instalação, reinício do Android, expiração e troca de conta em dispositivo |
| Google | Navegador do sistema, PKCE/nonce/state, assinatura RSA e claims verificados no servidor; resultado de uso único por segredo separado | Credenciais/redirect HTTPS, consentimento real e retorno ao app |
| Recuperação | Serviço Resend, token temporário com hash, resposta genérica e reset de uso único; telas enviam dados reais | Remetente verificado, entrega real e fluxo Android completo |
| Home/cidades | Consultas existentes, navegação para viagens/Vi e paleta compartilhada | Revisão visual, erros e retorno entre telas no APK |
| Descoberta | Filtros por cidade/tipo/preço/nota, acentos, ordenação e paginação; fallback textual para API antiga | Catálogo suficiente e revisado, desempenho em volume |
| Lugar e avaliações | Fotos HTTPS quando disponíveis, fonte visível, dados desconhecidos sem amenidades fictícias; avaliação 1–5 por conta/lugar, com atualização da média | Catálogo seed continua demonstrativo; imagens e avaliações no dispositivo |
| Salvos | Fluxo já existente, separado da inclusão no roteiro | Regressão de paginação, falhas, retentativa e visitantes |
| Viagens e painel | Pacote 1: criar/editar/excluir, status, datas/orçamento, resumos e navegação | HTTP e regressão no Android; proteção cidade/duração reforçada no patch |
| Roteiro | Pacote 2: editor por dia, mover/reordenar, horário/conclusão, notas, exclusão e PUT atômico com snapshot | Aplicar/publicar API e executar conflito/retorno no app |
| Notas e checklist | Pacote 3: notas gerais, memos e itens individuais; propriedade/idempotência/limpeza | HTTP e preservação dos editores em dispositivo |
| Despesas e comprovantes | Pacote 4: centavos, categorias, datas, reembolso manual, versão e anexos privados | Multipart, seletor de documentos e FileProvider no APK |
| Perfil/interesses | Pacote 5: gravação real, seleção persistida, senha atual para alterar e-mail e JWT renovado | Reabrir/rotacionar, falha de rede e mudança de e-mail no app |
| Vi/IA | Pacote 7: OpenAI Responses, sessões/histórico remotos, quota, contexto autorizado, proposta revisável e aplicação explícita/idempotente | Configurar chave/modelo, publicar API e validar resposta real; sem streaming, voz ou imagens |
| Mapa e clima | Pacote 8: Leaflet/OSM, navegação externa, Open-Meteo com fonte/data/fuso e cache | Rede/WebView real, atribuição, falha de tiles e previsão no dispositivo |
| PDF e offline | Pacote 9: PDF básico/completo, download privado por conta e leitura local; servidor exige Premium para novos pacotes completos/offline | Arquivos extensos, seletor de destino, expiração/logout e leitura sem rede |
| Premium/Stripe | Integração já existente; benefícios novos usam período pago no servidor, nunca flag do app | Checkout/webhook/Portal em testes, renovação e eventos fora de ordem |
| Persistência | Perfil optativo `persistent`, H2 em arquivo, seed desativado; reinício testado com conta/viagem/direito de acesso | Configurar volume durável e verificar schema existente sem perda de dados |
| Visual e publicação | Tokens floresta/marfim/sálvia e tema claro consistente; novas páginas usam componentes Material | Acessibilidade, teclado/fonte ampliada, APK Gradle, deploy e demonstração |

## Evidências de 18/11

- **30 testes Kotlin/JUnit passaram**, incluindo moeda/datas/roteiro, notas/despesas e proteção do HTML do mapa. Todos os fontes Kotlin foram checados com dependências locais e classe R real gerada por AAPT2. Recursos Android compilaram e vincularam.
- **8 testes da API passaram**, em sete classes: roteiro, notas, despesas, integração dos pacotes 5–9, parsing meteorológico, verificação criptográfica Google e persistência após reinício. Integrações usam Spring/H2 real e provedores substituídos por dublês; não fazem chamadas pagas.
- Maven empacotou a API isolada. O patch cumulativo passa no `git apply --check` contra a fonte original.
- A checagem Kotlin/AAPT2 não monta nem instala um APK. Gradle completo, emulador, HTTP público, Stripe e provedores reais **não foram validados nesta rodada**.

A aplicação na API original foi rejeitada automaticamente pela política de escrita da sessão, mesmo com autorização do responsável. O código completo está preservado em [all-packages.patch](../backend-patches/all-packages.patch); configuração e aplicação em [backend-patches/README.md](../backend-patches/README.md). Nenhum deploy ou commit foi realizado.

## Histórico e próxima verificação

As verificações antigas de login, catálogo e salvos continuam registradas no histórico; não comprovam regressão das alterações atuais. [Entrega dos pacotes 5–9](remaining-packages.md) separa o código entregue da fila de integração operacional. Atualizar esta matriz ao executar cada fluxo, registrando ambiente e limite da evidência.
