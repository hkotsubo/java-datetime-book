package exemplos.part2.thread;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CalendarMultiThread {

    public static void main(String[] args) throws Exception {
        System.out.println("Start");
        ExecutorService pool = Executors.newCachedThreadPool();

        // versão não-thread-safe, o número de erros varia a cada execução, mas quase nunca é zero
        runNotThreadSafe(pool);

        // versão thread-safe com synchronized, ERROS deve ser zero
        // runThreadSafeSynch(pool);

        // criar um novo Calendar ao invés de usar o estático, ERROS deve ser zero
        // runThreadSafeNotStatic(pool);

        // usar ThreadLocal, que cria somente um Calendar para cada thread, ERROS deve ser zero
        // runTheadLocal(pool);

        // se você aumentar o número de threads, não esqueça de aumentar este valor
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println(ERROS);
    }

    // contador de erros
    static AtomicInteger ERROS = new AtomicInteger(0);

    static Calendar CAL = Calendar.getInstance();

    static int n = 0;

    // usa o Calendar estático, não é thread safe
    static void runNotThreadSafe(ExecutorService pool) {
        // cria 100 threads
        for (int i = 0; i < 100; i++) {
            final int month = i % 12;
            pool.submit(() -> {
                CAL.set(Calendar.MONTH, month);
                int m = CAL.get(Calendar.MONTH);
                if (m != month) {
                    ERROS.incrementAndGet();
                }
            });
        }
    }

    static ThreadLocal<Calendar> TL = new ThreadLocal<Calendar>() {
        protected Calendar initialValue() {
            return Calendar.getInstance();
        };
    };

    // usa o Calendar estático, não é thread safe
    static void runTheadLocal(ExecutorService pool) {
        // cria 100 threads
        for (int i = 0; i < 100; i++) {
            final int month = i % 12;
            pool.submit(() -> {
                Calendar cal = TL.get();
                cal.set(Calendar.MONTH, month);
                int m = cal.get(Calendar.MONTH);
                if (m != month) {
                    ERROS.incrementAndGet();
                }
            });
        }
    }

    // usa synchronized para não ter problemas de várias threads modificando o mesmo Calendar
    static void runThreadSafeSynch(ExecutorService pool) {
        // cria 100 threads
        for (int i = 0; i < 100; i++) {
            final int month = i % 12;
            pool.submit(() -> {
                synchronized (CAL) {
                    CAL.set(Calendar.MONTH, month);
                    int m = CAL.get(Calendar.MONTH);
                    if (m != month) {
                        ERROS.incrementAndGet();
                    }
                }
            });
        }
    }

    // cria um novo Calendar ao invés de usar o estático
    static void runThreadSafeNotStatic(ExecutorService pool) {
        // cria 100 threads
        for (int i = 0; i < 100; i++) {
            final int month = i % 12;
            pool.submit(() -> {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, month);
                int m = cal.get(Calendar.MONTH);
                if (m != month) {
                    ERROS.incrementAndGet();
                }
            });
        }
    }
}
