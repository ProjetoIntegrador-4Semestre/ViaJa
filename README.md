# Viajá

Aplicativo Android de planejamento de viagens de turismo e trabalho, com Kotlin/XML e API própria em Java/Spring Boot. Projeto Integrador do SENAC; entrega confirmada em 02/12/2026. O desenvolvimento Android desta recuperação permanece na branch `expansion`.

## Documentação

Comece pelo [índice](docs/README.md). Ele separa [produto](docs/product-baseline.md), [arquitetura/dados](docs/architecture.md), [estado real](docs/status.md), [plano](docs/recovery-plan.md) e [entrega acadêmica](docs/academic.md). A [revisão documental](docs/documentation-audit.md) explica o destino das especificações e relatórios anteriores.

O app tem fluxos reais de login/sessão, busca/detalhe, salvos e criação/listagem de viagens com inclusão inicial de lugares no roteiro. Isso não significa que todo o produto esteja pronto. Perfil, integrações, editor completo, cobrança e demais lacunas estão descritos no estado da implementação.

Stripe em testes com Checkout/webhook reais é a direção confirmada, ainda não implementada. As telas precisam de revisão visual. Protótipos e documentos antigos não são prova de funcionamento.

## Execução local

A API está no repositório `api-viaja`; seu README descreve a inicialização com H2/LoadDatabase e as verificações locais. Com a API na porta 18080 e um emulador Android conectado:

```sh
./gradlew :app:assembleDebug -PapiBaseUrl=http://127.0.0.1:18080/ --offline
adb reverse tcp:18080 tcp:18080
```

O modo offline do Gradle exige dependências previamente presentes no cache; ele não implementa o modo offline do produto. Instalar o APK gerado pelo Android Studio ou ferramenta Android. A configuração hospedada é distinta e seus contratos ainda precisam de deploy/verificação.

Existem mudanças locais anteriores à recuperação. Não descartá-las nem assumir que o diretório testado equivale a um checkout limpo. Requisitos e evidências devem acompanhar cada alteração.
