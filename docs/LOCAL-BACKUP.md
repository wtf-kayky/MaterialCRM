# Backup local do MaterialCRM

Com os containers em execução, na raiz do projeto:

```powershell
.\scripts\backup.ps1
```

O arquivo será criado na pasta `backups` com data e hora no nome.

Para restaurar um backup:

```powershell
.\scripts\restore.ps1 -BackupFile .\backups\materialcrm-AAAAMMDD-HHMMSS.sql
```

A restauração exige a confirmação `RESTAURAR` e substitui os dados atuais do banco. Guarde os backups fora do computador servidor também.
