# Credenciais e dados locais

## Política atual

O aplicativo não persiste senhas. `ProfileStore` grava apenas nome, driver, URL, usuário e edição. A senha é fornecida na interface e fica em memória durante a sessão, inclusive para reconexões. Os arrays temporários do campo de senha são limpos; APIs legadas ainda usam `String`, portanto não há garantia de apagar imediatamente todas as cópias da memória.

Foram removidos o algoritmo PBEWithMD5AndDES, a chave fixa, o salt fixo e os métodos de codificação/decodificação de `Options`. Não existe senha mestra embutida nem substituição por Base64. URLs com credenciais embutidas e campos com quebras de linha são recusados na gravação. Nomes de perfil não podem conter separadores de caminho.

## Migração de perfis

- Os campos de conexão continuam no formato de cinco linhas `.ini`, agora em UTF-8.
- Arquivos `.pwd` antigos são ignorados; a senha precisa ser digitada novamente.
- Ao salvar o mesmo perfil, o arquivo `.pwd` correspondente é removido. Perfis que não forem salvos novamente podem continuar com esse arquivo antigo em disco; remova-o manualmente após confirmar suas credenciais.
- Perfis antigos com caracteres acentuados gravados em outra codificação podem precisar de conversão para UTF-8 ou recriação.
- A gravação usa arquivo temporário e substituição atômica quando suportada pelo sistema de arquivos.

Os backups e a tag original contêm o código antigo, que mantém o comportamento de credenciais de 2009. Os backups iniciais não continham perfis locais. Não copie credenciais reais para o checkout original.

## Dados de monitoramento

SQLs, nomes de usuários, hosts e detalhes de sessões podem ser sensíveis. O histórico Berkeley DB não é criptografado pela aplicação. Proteja a pasta do projeto, seus históricos e backups com permissões do sistema operacional e, quando necessário, criptografia de disco.

`.gitignore` cobre perfis, senhas, bases Berkeley DB, locks, logs, ferramentas locais, backups e artefatos de build. Isso evita adições acidentais comuns; não substitui a revisão de `git diff --cached` antes de publicar. Dados já presentes no histórico Git não são removidos por `.gitignore`.

Use conta dedicada conforme [ORACLE_PRIVILEGES.md](ORACLE_PRIVILEGES.md). A função legada de trace exige privilégio adicional e não faz parte da conta de leitura padrão.
