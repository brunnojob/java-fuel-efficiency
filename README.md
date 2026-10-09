# Fuel Log

A persistent fuel log with decimal costs and consumption between full tanks, including partial refills.

## Run

Requirements: Java 17.

```sh
javac FuelLog.java
java FuelLog abastecimentos.csv add 2026-10-09 12000 35.500 6.190 full
java FuelLog abastecimentos.csv report > result.json
```

## Behavior

Commands: `add date odometer liters price full|partial` and `report`. Dates and odometer readings cannot decrease. `BigDecimal` preserves precision; file locking protects the local log.

## Result synchronization

The [operations archive](https://vercel-home-telemetry-api.vercel.app/laboratory.html?project=java-fuel-efficiency) stores execution results. Supabase migrations are in the [API repository](https://github.com/brunnojob/vercel-home-telemetry-api/tree/main/supabase/migrations).

```sh
python cloud/sync.py enqueue result.json --project java-fuel-efficiency
python cloud/sync.py sync
```

Set `BRUNNODEV_ACCESS_TOKEN` to your session token. The SQLite outbox retains reports until the server confirms persistence; identical content does not create duplicate records. Tokens are not stored in source code. To run the synchronization tests:

```sh
python -m unittest discover -s cloud
```
