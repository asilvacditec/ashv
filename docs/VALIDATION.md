# Registro de validação

## Execução local — 22 de setembro de 2026

- Sistema: Windows, arquitetura x64.
- JDK: Eclipse Temurin 17.0.20.1+1, extraído localmente; checksum do download conferido com o publicado pela API Adoptium.
- Maven: 3.9.16, extraído localmente; checksum SHA-512 do download conferido.
- Comando: `mvn --batch-mode --no-transfer-progress clean verify`.
- Driver final: Oracle `ojdbc8` 19.30.0.0.
- Resultado: **23 testes, zero falhas, zero erros, zero ignorados** na suíte padrão.
- Artefato: `target/ash-viewer.jar` e dependências em `target/lib/`.

Os relatórios locais detalhados ficam em `target/surefire-reports` (ignorados pelo Git). O build inclui os testes de integração na compilação, mas eles só são executados com o perfil `oracle-integration`.

## Verificações adicionais

- Bundle original verificado antes das alterações.
- Tag original e commit inicial enviados ao GitHub antes de editar os fontes.
- JAR contém `org.ash.MainApp`, recursos de localização e as classes novas.
- Licença, NOTICE e créditos incluídos em `META-INF` do JAR.
- Java 5, criptografia PBE fixa e encoder `com.sun` removidos do caminho de build/credenciais atualizado.
- Artefatos `bin` retirados do índice Git; cópia original preservada pela tag e backups.
- Revisão de whitespace e caminhos ignorados realizada antes do commit.
- Build offline repetido sobre uma cópia limpa exportada do índice Git, sem `bin` ou arquivos locais não versionados: 23 testes passaram.
- Sintaxe do `run.sh` verificada com Bash; movimentação de 906 arquivos confirmada sem alteração de conteúdo, além da adaptação do encoder JPEG.

## Integração contínua

O commit de código `bc88ea22775312caab18323c6c76cdd1005a21c4` passou nas quatro combinações: Ubuntu/Windows e Java 17/21. Evidência: [execução 35682523994 do GitHub Actions](https://github.com/asilvacditec/ashv/actions/runs/35682523994). Esses jobs executaram o build e a suíte local; não conectaram ao Oracle.

## O que ainda não foi validado

- Não houve conexão com Oracle real. O usuário informou possuir Oracle 12c; subversão, edição, serviço e grants ainda precisam ser definidos para a execução do roteiro.
- Oracle 19c e posteriores não estão disponíveis nesta etapa.
- Não foi feita inspeção interativa completa da aplicação Swing.
- Os avisos de depreciação e tipos genéricos do código legado permanecem.

## Modelo para homologação futura

Registre versão/patchset Oracle, edição, JDK, driver, modo ASH ou sessões, escopo de container/instância, casos executados, falhas e resultado dos relatórios Maven. Identifique explicitamente testes ignorados. Não inclua senhas, hosts privados, dados pessoais ou SQLs sensíveis neste arquivo público.
