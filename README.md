# Fuel Log

Registro persistente de abastecimentos, custos decimais e consumo entre tanques completos, incluindo abastecimentos parciais.

## Executar

Requisitos: Java 17.

```sh
javac FuelLog.java
java FuelLog abastecimentos.csv add 2026-10-09 12000 35.500 6.190 full
java FuelLog abastecimentos.csv report > resultado.json
```

## Funcionamento

Comandos: `add data odômetro litros preço full|partial` e `report`. Datas e odômetro não podem regredir. `BigDecimal` conserva precisão; arquivo e bloqueio protegem o registro local.

## Persistência de resultados

O arquivo de operações está em [vercel-home-telemetry-api.vercel.app](https://vercel-home-telemetry-api.vercel.app/laboratory.html?project=java-fuel-efficiency). As migrações Supabase estão no [repositório da API](https://github.com/brunnojob/vercel-home-telemetry-api/tree/main/supabase/migrations).

```sh
python cloud/sync.py enqueue resultado.json --project java-fuel-efficiency
python cloud/sync.py sync
```

Defina `BRUNNODEV_ACCESS_TOKEN` com sua sessão. A fila SQLite conserva os relatórios até confirmação do servidor; o mesmo conteúdo não gera registros duplicados. Tokens não são gravados no código.
