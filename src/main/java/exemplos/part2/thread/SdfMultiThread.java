package exemplos.part2.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Teste mostrando que SimpleDateFormat não é thread-safe e como contornar esse problema
public class SdfMultiThread {

    public static void main(String[] args) throws Exception {
        System.out.println("Start");
        ExecutorService pool = Executors.newCachedThreadPool();

        // versão não-thread-safe, o número de erros varia a cada execução, mas quase nunca é zero
        runNotThreadSafe(pool);

        // versão thread-safe com synchronized, sem erros
        // runThreadSafeSynch(pool);

        // criar um novo SimpleDateFormat ao invés de usar o estático, sem erros
        // runThreadSafeNotStatic(pool);

        // usar ThreadLocal, que cria somente um SimpleDateFormat para cada thread, sem erros
        // runThreadLocal(pool);

        // usar clone(), sem erros
        // runThreadSafeClone(pool);

        // se você aumentar o número de threads, não esqueça de aumentar este valor
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("Fim");
    }

    static SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    // usa o SimpleDateFormat estático, não é thread safe
    static void runNotThreadSafe(ExecutorService pool) {
        String entrada = "01/02/2018";
        // criar 100 threads
        for (int i = 0; i < 100; i++) {
            // cada thread faz parsing da String e em seguida formata
            pool.submit(() -> {
                try {
                    Date date = SDF.parse(entrada);
                    String dataFormatada = SDF.format(date);
                    // se as Strings forem diferentes, imprime ambas
                    if (!entrada.equals(dataFormatada)) {
                        System.out.println(entrada + " diferente de " + dataFormatada);
                    }
                } catch (Exception e) {
                    // imprimir exceções
                    System.out.println(e);
                }
            });
        }
    }

    static final ThreadLocal<SimpleDateFormat> TL = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy");
        }
    };

    // usa o ThreadLocal
    static void runThreadLocal(ExecutorService pool) {
        String entrada = "01/02/2018";
        // criar 100 threads
        for (int i = 0; i < 100; i++) {
            // cada thread faz parsing da String e em seguida formata
            pool.submit(() -> {
                try {
                    SimpleDateFormat sdf = TL.get();
                    Date date = sdf.parse(entrada);
                    String dataFormatada = sdf.format(date);
                    // se as Strings forem diferentes, imprime ambas
                    if (!entrada.equals(dataFormatada)) {
                        System.out.println(entrada + " diferente de " + dataFormatada);
                    }
                } catch (Exception e) {
                    // imprimir exceções
                    System.out.println(e);
                }
            });
        }
    }

    // usa synchronized para não ter problemas de várias threads modificando o mesmo Calendar
    static void runThreadSafeSynch(ExecutorService pool) {
        String entrada = "01/02/2018";
        // criar 100 threads
        for (int i = 0; i < 100; i++) {
            // cada thread faz parsing da String e em seguida formata
            pool.submit(() -> {
                try {
                    synchronized (SDF) {
                        Date date = SDF.parse(entrada);
                        String dataFormatada = SDF.format(date);
                        // se as Strings forem diferentes, imprime ambas
                        if (!entrada.equals(dataFormatada)) {
                            System.out.println(entrada + " diferente de " + dataFormatada);
                        }
                    }
                } catch (Exception e) {
                    // imprimir exceções
                    System.out.println(e);
                }
            });
        }
    }

    // cria um novo SimpleDateFormat ao invés de usar o estático
    static void runThreadSafeNotStatic(ExecutorService pool) {
        String entrada = "01/02/2018";
        // criar 100 threads
        for (int i = 0; i < 100; i++) {
            // cada thread faz parsing da String e em seguida formata
            pool.submit(() -> {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = sdf.parse(entrada);
                    String dataFormatada = sdf.format(date);
                    // se as Strings forem diferentes, imprime ambas
                    if (!entrada.equals(dataFormatada)) {
                        System.out.println(entrada + " diferente de " + dataFormatada);
                    }
                } catch (Exception e) {
                    // imprimir exceções
                    System.out.println(e);
                }
            });
        }
    }

    // cria um clone do SimpleDateFormat
    static void runThreadSafeClone(ExecutorService pool) {
        String entrada = "01/02/2018";
        // criar 100 threads
        for (int i = 0; i < 100; i++) {
            // cada thread faz parsing da String e em seguida formata
            pool.submit(() -> {
                try {
                    SimpleDateFormat sdf = (SimpleDateFormat) SDF.clone();
                    Date date = sdf.parse(entrada);
                    String dataFormatada = sdf.format(date);
                    // se as Strings forem diferentes, imprime ambas
                    if (!entrada.equals(dataFormatada)) {
                        System.out.println(entrada + " diferente de " + dataFormatada);
                    }
                } catch (Exception e) {
                    // imprimir exceções
                    System.out.println(e);
                }
            });
        }
    }
}
