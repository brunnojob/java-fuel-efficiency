import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public final class FuelLog {
    record Fill(LocalDate date, long odometer, BigDecimal liters, BigDecimal unitPrice, boolean full) {
        Fill {
            if (odometer < 0 || liters.signum() <= 0 || liters.compareTo(new BigDecimal("10000")) > 0 ||
                unitPrice.signum() < 0 || unitPrice.compareTo(new BigDecimal("10000")) > 0)
                throw new IllegalArgumentException("invalid fill");
            liters = liters.setScale(3, RoundingMode.UNNECESSARY);
            unitPrice = unitPrice.setScale(3, RoundingMode.UNNECESSARY);
        }
        BigDecimal cost() { return liters.multiply(unitPrice).setScale(2, RoundingMode.HALF_EVEN); }
    }
    record Period(LocalDate date, long distanceKm, BigDecimal liters, BigDecimal cost, BigDecimal litersPer100Km) {}
    static List<Fill> read(Path file) throws Exception {
        var fills = new ArrayList<Fill>();
        if (!Files.exists(file)) return fills;
        for (String line : Files.readAllLines(file)) {
            String[] f = line.split(",", -1);
            if (f.length != 3 && f.length != 5) throw new IllegalArgumentException("invalid CSV record");
            if (f.length == 5 && !Set.of("full", "partial").contains(f[4])) throw new IllegalArgumentException("invalid tank marker");
            Fill fill = new Fill(LocalDate.parse(f[0]), Long.parseLong(f[1]), new BigDecimal(f[2]),
                f.length == 5 ? new BigDecimal(f[3]) : BigDecimal.ZERO, f.length == 3 || f[4].equals("full"));
            validateOrder(fills, fill); fills.add(fill);
        }
        return fills;
    }
    static void validateOrder(List<Fill> fills, Fill fill) {
        if (fills.isEmpty()) return;
        Fill last = fills.get(fills.size() - 1);
        if (fill.odometer() <= last.odometer() || fill.date().isBefore(last.date()))
            throw new IllegalArgumentException("odometer and date cannot regress");
    }
    static List<Period> periods(List<Fill> fills) {
        var periods = new ArrayList<Period>(); Fill anchor = null;
        BigDecimal liters = BigDecimal.ZERO, cost = BigDecimal.ZERO;
        for (Fill fill : fills) {
            if (anchor == null) { if (fill.full()) anchor = fill; continue; }
            liters = liters.add(fill.liters()); cost = cost.add(fill.cost());
            if (!fill.full()) continue;
            long distance = fill.odometer() - anchor.odometer();
            periods.add(new Period(fill.date(), distance, liters, cost,
                liters.multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(distance), 3, RoundingMode.HALF_EVEN)));
            anchor = fill; liters = BigDecimal.ZERO; cost = BigDecimal.ZERO;
        }
        return periods;
    }
    static void report(List<Fill> fills) {
        List<Period> periods = periods(fills);
        System.out.print("{\"fills\":" + fills.size() + ",\"periods\":[");
        long distance = 0; BigDecimal liters = BigDecimal.ZERO, cost = BigDecimal.ZERO;
        for (int i = 0; i < periods.size(); i++) {
            Period p = periods.get(i); if (i > 0) System.out.print(",");
            System.out.print("{\"date\":\"" + p.date() + "\",\"distanceKm\":" + p.distanceKm() +
                ",\"liters\":" + p.liters() + ",\"cost\":" + p.cost() + ",\"litersPer100Km\":" + p.litersPer100Km() + "}");
            distance = Math.addExact(distance, p.distanceKm()); liters = liters.add(p.liters()); cost = cost.add(p.cost());
        }
        System.out.println("],\"distanceKm\":" + distance + ",\"accountedLiters\":" + liters + ",\"accountedCost\":" + cost + "}");
    }
    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("FuelLog file report | add date odometer liters unit-price full|partial");
        Path file = Path.of(args[0]); Path lockPath = file.resolveSibling(file.getFileName() + ".lock");
        try (FileChannel lockChannel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             var lock = lockChannel.lock()) {
            if (!lock.isValid()) throw new IllegalStateException("file lock unavailable");
            var fills = read(file);
            if (args[1].equals("add")) {
                if (args.length != 7 || !Set.of("full", "partial").contains(args[6])) throw new IllegalArgumentException("invalid add arguments");
                Fill fill = new Fill(LocalDate.parse(args[2]), Long.parseLong(args[3]), new BigDecimal(args[4]), new BigDecimal(args[5]), args[6].equals("full"));
                validateOrder(fills, fill);
                byte[] row = (fill.date() + "," + fill.odometer() + "," + fill.liters() + "," + fill.unitPrice() + "," + args[6] + "\n").getBytes(StandardCharsets.UTF_8);
                try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                    ByteBuffer buffer = ByteBuffer.wrap(row); while (buffer.hasRemaining()) channel.write(buffer); channel.force(true);
                }
                fills.add(fill);
            } else if (!args[1].equals("report")) throw new IllegalArgumentException("unknown command");
            report(fills);
        }
    }
}
