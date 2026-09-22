# Preservação da versão original

A versão anterior às mudanças está preservada em:

- Commit original: `56bdef0ded8f5dffaed7380b6a7fa679248f2c37`.
- Tag anotada enviada ao GitHub: `original-2009-preserved-2026-09-21`.
- Commit inicial de preservação, sem alteração de arquivos: `089e4ce`.

A tag aponta diretamente ao commit original. Classes, JARs, scripts, fontes e avisos daquele estado continuam recuperáveis no histórico Git.

## Cópias versionadas no GitHub

A pasta [`.local-backups/`](../.local-backups/) está versionada no Git e disponível no GitHub. Ela contém as cópias originais abaixo e o diretório `original-lib/`, com os seis JARs preservados da instalação original:

| Arquivo | SHA-256 |
| --- | --- |
| [ashv-original-2026-09-21.zip](../.local-backups/ashv-original-2026-09-21.zip) | `8ce10a650f5ea4d7eadf5d8ef94e14e0fd6fbdfad809be35f63d88ba97610557` |
| [ashv-original-2026-09-21.bundle](../.local-backups/ashv-original-2026-09-21.bundle) | `dc326d9d2e6036d123547a51c4ecda3acc90931da595415a58e8ee7b8d087328` |

O ZIP contém os arquivos versionados originais. O bundle contém o histórico e as referências presentes no momento da cópia, anterior à criação da nova tag. Sua integridade foi verificada com `git bundle verify`. Não havia mudanças locais no início. Essas cópias não incluem dados futuros, perfis locais ou configuração/chaves SSH. Copie-as para outro dispositivo se desejar redundância física.

## Abrir o original sem alterar a versão atual

Execute na raiz do repositório:

```sh
git worktree add --detach ../ashv-original original-2009-preserved-2026-09-21
```

Ou restaure um clone pelo backup local:

```sh
git clone .local-backups/ashv-original-2026-09-21.bundle ../ashv-original-backup
```

O original mantém os requisitos e limitações de 2009, inclusive Java 5, scripts específicos e dependências antigas. A preservação exata não garante execução com o Java moderno. Use um ambiente legado isolado para comparação; não compartilhe diretórios de histórico ou credenciais entre versões.

Para comparar as alterações:

```sh
git diff --stat original-2009-preserved-2026-09-21..master
git log --oneline original-2009-preserved-2026-09-21..master
```

Não é necessário executar `reset --hard` ou sobrescrever o trabalho atual.
