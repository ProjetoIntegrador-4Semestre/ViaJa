# Viajá — estado da implementação

Revisado em 05/11/2026 a partir do código local e das verificações da sessão. Referência de produto: [product-baseline.md](product-baseline.md). Esta é a única matriz atual; relatórios em history/storage são snapshots.

**Verificado** significa um fluxo específico executado. **Parcial** significa integração incompleta ou validação insuficiente. **Simulado** significa resposta local/fixa. **Ausente** significa que não foi encontrado um fluxo implementado. Nenhum desses rótulos é percentual de conclusão.

## Matriz

| Área | Android | API/dados | O que falta para concluir |
| --- | --- | --- | --- |
| Login/sessão | Login real, token persistido e retorno à Home verificados | JWT/login funcionam no teste local | Logout na UI, expiração em tela aberta, armazenamento protegido, JWT inválido tratado corretamente |
| Cadastro | Callback real; não reexecutado integralmente na última rodada | Rota existente | Validação de campos/senha, duplicidade e regressão ponta a ponta |
| Google | Botão “em breve” | Ausente | OAuth/configuração e verificação da identidade |
| Recuperar/redefinir | Telas sem ação | Ausente | E-mail, token temporário, submissão e links |
| Home/cidades | Consulta real de cidades; visual problemático | HAL e seed existentes | Estado de erro/retentativa, próxima viagem e revisão visual |
| Explore/filtros | Pesquisa e cards; filtros sem efeito real | Busca textual simples | Filtros, acentos, catálogo suficiente e ordenação |
| Lugar | Leitura/detalhe verificados; favorito e inclusão real | PlaceResponse alinhado | Fotos/mapa/horários/amenidades reais, ação do site; remover ilustrações enganosas |
| Salvos | Salvar/remover/lista/retorno vazio verificados | Proprietário e mutações testados | Falhas de consulta/retentativa no favorito, paginação Android em volume e visita sem sessão |
| Viagens | Criar/listar e abrir detalhe verificados | CRUD e proprietário testados | Edição/exclusão na UI, conflitos de cidade/datas, finalização/histórico completo |
| Roteiro | Adiciona ao dia 1 e mostra primeira parada | CRUD/ordenação por dia e acesso testados | Editor completo, horários, dia escolhido, ordem sem colisões e memos na exclusão |
| Perfil/interesses | Usuário simulado e sucesso apenas local | CRUD sem proteção suficiente | Identidade correta, gravação real, campos e sessão após edição |
| Notas/checklists | Sem fluxo | Texto de viagem/parada parcial | Autorização, telas e itens de checklist |
| Despesas/recibos | Ausente | Ausente | Registro, totais, anexos privados, orçamento/reembolso |
| Mapas/localização | Imagem ilustrativa | Coordenadas no catálogo | Mapa/renderização, permissões e navegação real |
| Clima | “Indisponível” no dashboard | Ausente | Provedor, consulta contextual e estados de falha |
| Avaliações | Apenas exibe nota do catálogo | CRUD parcial | Regra de repetição, validação uniforme, média/contagem e interface |
| Vi/IA | Respostas fixas por palavras | Ausente | Provedor, quota, rascunho validado e aprovação/aplicação |
| Premium/Stripe | Telas sobrepostas/simuladas | CRUD Subscription; sem Stripe | Checkout/webhook/retorno/Portal, persistência e benefícios no servidor |
| PDF/offline | Ausente | Ausente | Exportação e download conforme os planos |
| Navegação/visual | Fragmentos/Activities sobrepostos e estados incompletos | Não se aplica | Um caminho coerente por área, botões reais, acessibilidade e revisão das telas |
| Publicação | APK local verificado em fluxos selecionados | Atualizações locais | Checkout limpo reproduzível, deploy validado e demonstração completa |

## Falhas prioritárias confirmadas na auditoria

1. Com a conta Rafael, `GET /users/2` retornou 200 com `passwordHash`; os controladores de usuários não conferem proprietário nas mutações.
2. `GET /trips/2/notes`, `/trip-memos` e `/users/2/interests` retornaram 200 com a mesma conta. A proteção de TripController não protege esses caminhos automaticamente.
3. Regras públicas de places/cities/interests/medias abrangem métodos de escrita, não só leitura.
4. JWT padrão no código, filtro sem tratamento explícito de falhas e RuntimeException → 404. Sem token, recurso privado retornou 403; JWT malformado prejudicou consulta pública com 403.
5. Editar perfil anuncia sucesso sem gravar na API; UserRepository pode substituir a identidade da sessão por dados do mock.
6. Busca local confirmou “São Paulo” com zero resultados e “Sao Paulo” com um.
7. Falta tratar memos na remoção de uma parada. Como existe FK composta, isso pode impedir a exclusão; não é correto afirmar que sempre produz órfãos.
8. Há alterações Android pré-existentes, inclusive arquivos fora do versionamento. Compilar o diretório atual não prova compilação e execução de um checkout limpo.

Não foram feitas alterações de produção durante a revisão documental. As falhas acima não foram corrigidas só por serem descritas aqui.

## Evidência disponível e seus limites

- Compilação Android debug e Maven verify passaram nas rodadas de implementação anteriores.
- `scripts/smoke_test.py` da API possui nove grupos de verificações: catálogo/paginação, erros, contratos, login, salvos, viagens e paradas. Esses grupos contêm várias assertivas, não equivalem a nove funcionalidades completas nem a uma suíte integral.
- `AuthRepositoryTest` passou diretamente com AndroidJUnitRunner: sessão simulada/expirada, restauração, IDs longos e rejeição do token correspondente. O launcher de testes Gradle offline não tinha uma dependência no cache.
- Emulador: login/restauração, busca→detalhe, ciclo de salvos, criar viagem e incluir lugar na viagem foram demonstrados. Não houve regressão de todas as telas, modo escuro, rotação, rede lenta ou troca de conta.
- Capturas atuais de Home/Perfil mostram contraste ruim, roxo herdado, seleção duplicada, chips cortados e persona fictícia. A prévia visual da conversa não foi implementada no APK.
- Checkout Stripe, webhook e novos planos são decisões, não funcionalidades existentes. H2 persistente ainda não foi configurado.
- Não houve envio dos commits nem publicação dos contratos atuais no remoto durante estas rodadas.

## Como atualizar

Trocar um estado exige registrar o fluxo executado, ambiente, resultado e limite relevante. Não usar quantidade de arquivos, presença de classes, data configurada de commit ou compilação como prova de funcionalidade. Guardar histórico fora desta matriz e substituir informações superadas, em vez de adicionar relatórios contraditórios no fim do arquivo.
