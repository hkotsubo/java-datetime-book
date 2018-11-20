package exemplos.part2.thread;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalDateMultiThread {

    public static void main(String[] args) throws Exception {
        System.out.println("Start");
        ExecutorService pool = Executors.newCachedThreadPool();

        // LocalDate sempre é thread-safe, o número de erros é zero
        run(pool);

        // se você aumentar o número de threads, não esqueça de aumentar este valor
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println(ERROS);
    }

    // contador de erros
    static AtomicInteger ERROS = new AtomicInteger(0);

    static LocalDate DT = LocalDate.now();

    static int n = 0;

    // LocalDate é sempre thread-safe
    static void run(ExecutorService pool) {
        // cria 100 threads
        for (int i = 0; i < 100; i++) {
            final int month = i % 2;
            pool.submit(() -> {
                // não tem setMonth, somente withMonth que retorna outro LocalDate
                LocalDate other = DT.withMonth(month);
                int m = other.getMonthValue();
                if (m != month) {
                    // nunca vai entrar nesse if
                    ERROS.incrementAndGet();
                }
            });
        }
    }
}
