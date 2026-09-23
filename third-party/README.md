# Componentes de terceiros

Os avisos e cabeçalhos originais foram mantidos. Consulte [AUTHORS.md](../AUTHORS.md), [notice.txt](../notice.txt) e os próprios fontes para autores e termos específicos.

## Fontes incorporados

`src/` contém JFreeChart, E-Gantt, jEdit Syntax e Blanco SQL Formatter anteriormente misturados em `src/` na raiz. JFreeChart e E-Gantt contêm adaptações acopladas a `org.ash`; por isso ainda são compilados junto com a aplicação. Não foram substituídos silenciosamente por versões upstream incompatíveis.

Alteração feita nesta etapa no E-Gantt: `BasicJPEGEncoder` usa `javax.imageio.ImageIO`, disponível no Java padrão, e retorna os bytes do JPEG gerado. Foi removida a dependência de `com.sun.image.codec.jpeg`, inexistente em JDKs atuais.

## Dependências do Maven

| Dependência | Versão | Decisão |
| --- | --- | --- |
| Oracle JDBC `ojdbc8` | 19.30.0.0 | Linha escolhida para o alvo Oracle 12c e Java 17; requer homologação |
| Microsoft JDBC `mssql-jdbc` | 12.8.2.jre11 | Leitor SQL Server experimental em Java 17+; conexão com SQL Server 2012+ requer homologação |
| Joda-Time | 2.14.0 | Atualizada e resolvida no Maven Central |
| JCommon | 1.0.24 | Atualizada mantendo API do JFreeChart incorporado |
| Commons Logging | 1.3.5 | Atualizada e resolvida no Maven Central |
| SwingX | 1.0 | Mantida para compatibilidade com a interface legada |
| Berkeley DB JE | 3.3.75 | Preservada para evitar migração implícita dos históricos |

SwingX também resolve dependências transitivas `swing-worker` e `filters`. JUnit Jupiter e Mockito são exclusivos de teste. `mvn dependency:tree` exibe a árvore efetiva.

## Berkeley DB preservado

O JAR original está em `maven/com/sleepycat/je/3.3.75/`, com POM mínimo e checksums SHA-1 exigidos pelo repositório Maven local. Foi mantido um único binário de terceiro porque essa versão não está no Maven Central; não é um artefato gerado pelo projeto.

SHA-256 do JAR original: `3496c3485f3def3b79dcfc19e3d14d7ae28d71e8159c326b109ad87420f0d016`.

Uma futura atualização precisa revisar os termos da versão escolhida, compatibilidade de API, formato em disco, migração de uma cópia do histórico e possibilidade de retorno ao original. Os testes desta entrega usam a versão preservada e não demonstram migração para outra versão.

Todos os outros JARs antigos foram removidos da árvore atual; continuam disponíveis na tag original. Os termos das dependências Maven continuam nos respectivos artefatos e projetos. A licença principal do ASH Viewer não substitui as licenças de terceiros.
