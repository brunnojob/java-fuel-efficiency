import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class FuelLog {
    record Fill(LocalDate date, long odometer, double liters) {}
    static List<Fill> read(Path path) throws Exception {
        var fills = new ArrayList<Fill>();
        if (Files.exists(path)) for (String line : Files.readAllLines(path)) {
            var p = line.split(",", -1);
            if (p.length != 3) throw new IllegalArgumentException("bad row: " + line);
            fills.add(new Fill(LocalDate.parse(p[0]), Long.parseLong(p[1]), Double.parseDouble(p[2])));
        }
        return fills;
    }
    public static void main(String[] args) throws Exception {
        if (args.length < 2) { System.err.println("usage: java FuelLog <file> report | add <date> <odometer-km> <liters>"); System.exit(2); }
        Path file = Path.of(args[0]); var fills = read(file);
        if (args[1].equals("add")) {
            if (args.length != 5) throw new IllegalArgumentException("add needs date, odometer and liters");
            var f = new Fill(LocalDate.parse(args[2]), Long.parseLong(args[3]), Double.parseDouble(args[4]));
            if (f.odometer() < 0 || f.liters() <= 0 || (!fills.isEmpty() && f.odometer() <= fills.get(fills.size()-1).odometer()))
                throw new IllegalArgumentException("odometer must increase and liters must be positive");
            Files.writeString(file, f.date()+","+f.odometer()+","+f.liters()+"\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } else if (args[1].equals("report")) {
            for (int i=1; i<fills.size(); i++) {
                var a=fills.get(i-1); var b=fills.get(i); long km=b.odometer()-a.odometer();
                System.out.printf(Locale.ROOT,"%s %d km %.2f L/100km%n",b.date(),km,b.liters()*100.0/km);
            }
        } else throw new IllegalArgumentException("unknown command");
    }
}