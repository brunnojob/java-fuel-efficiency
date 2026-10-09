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

Contribution trailer: `Co-authored-by: nyctophile <33561761+ineedfoundmyway@users.noreply.github.com>`.
