# Compatibilidade e homologação

Compilar, selecionar um coletor em teste unitário e monitorar um banco real são evidências diferentes.

| Ambiente | Evidência atual | Situação |
| --- | --- | --- |
| Windows + Temurin 17 | Compilação, testes e empacotamento locais | Validado no escopo automatizado |
| Linux/Windows + Java 17/21 | Workflow GitHub Actions configurado | Consultar o resultado de cada execução no GitHub |
| Oracle 12c | Detecção coberta por teste unitário; usuário possui ambiente | Integração real pendente; primeiro alvo |
| Oracle 19c | Detecção coberta por teste unitário | Integração real pendente |
| Oracle 21c / 23 e posteriores | Detecção encaminhada ao coletor 11g | Integração real pendente; não assumir suporte a releases futuras |
| Oracle 8i/9i/10g/11g | Coletores históricos preservados | Sem homologação com o driver atual |
| Interface gráfica | Exportação JPEG coberta por teste | Inspeção interativa completa pendente |

## Driver escolhido para o alvo 12c

O projeto usa `com.oracle.database.jdbc:ojdbc8:19.30.0.0`. O nome `ojdbc8` identifica a linha do artefato, não uma exigência de executar com Java 8. Segundo a [FAQ oficial Oracle JDBC](https://www.oracle.com/database/technologies/faq-jdbc.html), versões 19.x recentes contemplam JDK 17/21. A matriz registra a combinação JDBC 19 / banco 12c como anteriormente suportada (`Was`), pois o banco antigo saiu das janelas de suporte ali indicadas. A linha 23 não contempla 12c; por isso não foi adotada nesta entrega.

Isso fundamenta a escolha do driver, mas não comprova a compatibilidade funcional do ASH Viewer. É necessário registrar se o ambiente é 12.1 ou 12.2, edição, patchset, serviço/PDB, privilégios e forma de coleta.

## Limites atuais

- A seleção de versões usa metadados JDBC; releases a partir de 11 usam o coletor histórico 11g.
- Views/colunas e tipos devem ser verificados no banco alvo.
- A aplicação usa `V$`, sem agregação RAC por `GV$`.
- O modelo histórico não identifica `CON_ID`; não oferece uma visão consolidada segura de múltiplas PDBs.
- O coletor SE, as versões legadas, planos e relatórios precisam de validação própria.
- Berkeley DB e SwingX permanecem versões antigas; a compilação em Java moderno não elimina todas as dívidas técnicas.
- Um banco futuro na OCI pode ampliar a matriz, mas deve ser avaliado conforme versão, serviço, conectividade e recursos efetivamente disponíveis. Nenhum recurso OCI foi criado.

Use o roteiro de [TESTING.md](TESTING.md) e registre evidências em [VALIDATION.md](VALIDATION.md), sem publicar dados do banco ou credenciais.
