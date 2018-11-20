package exemplos.part2.thread;

import java.text.ParsePosition;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DateTimeFormatterMultiThread {

    public static void main(String[] args) throws Exception {
        System.out.println("Start");
        ExecutorService pool = Executors.newCachedThreadPool();

        // DateTimeFormatter sempre é thread-safe, o número de erros é zero
        run(pool);

        // se você aumentar o número de threads, não esqueça de aumentar este valor
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println(ERROS);
    }

    // contador de erros
    static AtomicInteger ERROS = new AtomicInteger(0);

    static DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    static String INPUT = "10/01/2018";

    static int n = 0;

    static void run(ExecutorService pool) {
        // cria 100 threads
        for (int i = 0; i < 100; i++) {
            pool.submit(() -> {
                ParsePosition position = new ParsePosition(0);
                TemporalAccessor parsed = FMT.parseUnresolved(INPUT, position);
                if (position.getErrorIndex() >= 0) {
                    System.out.println("Erro na posição " + position.getErrorIndex());
                    ERROS.incrementAndGet();
                } else if (position.getIndex() < INPUT.length()) {
                    System.out.println("Não fez parsing da String toda, parou na posição " + position.getIndex());
                    ERROS.incrementAndGet();
                } else {
                    if (parsed.getLong(ChronoField.DAY_OF_MONTH) != 10) {
                        System.out.println(parsed);
                        ERROS.incrementAndGet();
                    }
                }
            });
        }
    }
}
