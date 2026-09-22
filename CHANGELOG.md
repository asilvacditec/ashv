# Alterações

## Modernização inicial — setembro de 2026

Contribuição atual: **Aparecido Silva**. Autoria original de **Alex Kardapolov** e créditos de terceiros preservados.

- Backup ZIP, bundle verificado, tag original publicada e commit inicial anterior às alterações.
- `.gitignore` para build, dados, senhas, ferramentas e backups; classes compiladas removidas do versionamento atual.
- Maven com alvo Java 17, manifesto executável, bibliotecas em `target/lib` e ponte de compatibilidade Ant.
- Scripts Windows/Unix sem caminhos de JDK específicos de máquina.
- Dependências declaradas e atualizadas; exceções Berkeley DB/SwingX documentadas.
- Fontes de terceiros separados, mantendo adaptações e cabeçalhos.
- Senhas apenas na sessão, remoção de PBEWithMD5AndDES e gravação atômica de metadados do perfil.
- Seleção de coletor por versão numérica JDBC e suporte a URLs de serviço/PDB.
- Leitura de timestamps pelo JDBC padrão no coletor 11g utilizado para versões posteriores.
- JPEG por ImageIO com retorno correto dos bytes.
- Testes automatizados, integração Oracle opcional e workflow Windows/Linux, Java 17/21.
- Documentação Markdown de instalação, backup, arquitetura, segurança, privilégios, testes, compatibilidade e créditos.

## Pendências explícitas

- Homologar Oracle 12c com o ambiente disponibilizado pelo usuário; não houve conexão real nesta etapa.
- Testar 19c e posteriores quando houver ambientes disponíveis; OCI é uma possibilidade futura, sem provisionamento realizado.
- Validar interativamente todos os gráficos, relatórios e fluxos de reconexão.
- Extrair as adaptações para desacoplar completamente JFreeChart/E-Gantt antes de substituí-los por artefatos upstream.
- Avaliar migração de Berkeley DB e SwingX em alterações próprias, com testes de dados e interface.
