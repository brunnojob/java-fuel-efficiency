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

## Optional report archive

Use the [native C operations archive client](https://github.com/brunnojob/vercel-home-telemetry-api/tree/main/clients/c) to queue `result.json` under project `java-fuel-efficiency`. The client uses `BRUNNODEV_ACCESS_TOKEN` and retains unacknowledged reports locally.

## License

Original source and documentation are MIT licensed; see [LICENSE](LICENSE). Third-party dependencies and media retain their respective terms. Maintained by [Brunno Dev](https://brunnodev.store).

## Implementation update

Reports include weighted consumption across complete fill-up periods and a cost-per-kilometer value for each period. Weighting uses summed liters and distance rather than averaging period ratios.

Contribution trailer: `Co-authored-by: nyctophile <329826984+ineedfoundmyway@users.noreply.github.com>`.

## Execution proof

[![Executable proof](https://github.com/brunnojob/java-fuel-efficiency/actions/workflows/proof.yml/badge.svg)](https://github.com/brunnojob/java-fuel-efficiency/actions/workflows/proof.yml)

[Recorded execution and downloadable evidence](https://github.com/brunnojob/java-fuel-efficiency/actions/workflows/proof.yml)

Run `python .proof/record.py` after installing the prerequisites above. The scenarios execute repository code and verify exit codes and expected output. CI publishes `execution-proof` with the transcript, input fingerprints and source commit. The downloadable report identifies the exact tested version; the workflow badge tracks the latest run.
