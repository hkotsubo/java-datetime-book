package outros;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.threeten.extra.Interval;
import org.threeten.extra.PeriodDuration;
import org.threeten.extra.Temporals;

import exemplos.part3.Cap16AritmeticaDatas;
import exemplos.setup.Setup;

/**
 * Durante o desenvolvimento do java.time, algumas ideias da JSR 310 foram rejeitadas e acabaram não sendo incorporadas ao JDK 8. Porém, isto não impediu seu
 * autor (Stephen Colebourne) de adicionar estas funcionalidades em um projeto à parte. Daí surgiu o ThreeTen-Extra (http://www.threeten.org/threeten-extra).
 * 
 * Funciona somente em Java >= 8
 */
public class ThreetenExtraExemplos {

    public static void main(String[] args) {
        periodDurationJuntos();
        intervalos();
        parseVariosFormatters();
        outros();
    }

    /**
     * No java.time há duas classes para durações: {@link java.time.Period} e {@link java.time.Duration} - ver exemplos em {@link Cap16AritmeticaDatas}
     * 
     * No Threeten Extra foi criada uma classe que junta um Period com um Duration
     */
    static void periodDurationJuntos() {
        // 1 de maio de 2018, 10:30:45
        LocalDateTime inicio = LocalDateTime.of(2018, 5, 1, 10, 30, 45);
        // 3 de maio de 2018, 15:25:55
        LocalDateTime fim = LocalDateTime.of(2018, 5, 3, 15, 25, 55);
        // Usando Period, eu tenho que converter as datas para LocalDate, pois o método Period.between() não aceita parâmetros de outro tipo
        System.out.println(Period.between(inicio.toLocalDate(), fim.toLocalDate())); // P2D (2 dias)

        // Já o método Duration.between aceita qualquer classe que implemente Temporal, então eu posso usar LocalDateTime sem problemas:
        System.out.println(Duration.between(inicio, fim)); // PT52H55M10S (52 horas, 55 minutos e 10 segundos)

        // juntando Period com Duration, com a classe org.threeten.extra.PeriodDuration
        System.out.println(PeriodDuration.between(inicio, fim)); // P2DT4H55M10S (2 dias, 4 horas, 55 minutos e 10 segundos)

        // duração de 3 dias, 5 horas e 10 minutos
        PeriodDuration pd = PeriodDuration.parse("P3DT5H10M");
        // pode ser somado à datas
        LocalDateTime dt = inicio.plus(pd);
        System.out.println(dt); // 2018-05-04T15:40:45

        // -------------------------------------------------
        // PeriodDuration pode ter campos negativos
        // 1 de maio de 2018, 10:00
        inicio = LocalDateTime.of(2018, 5, 1, 10, 0, 0);
        // 3 de maio de 2018, 09:00
        fim = LocalDateTime.of(2018, 5, 3, 9, 0, 0);
        System.out.println(Period.between(inicio.toLocalDate(), fim.toLocalDate())); // P2D (2 dias)
        System.out.println(Duration.between(inicio, fim)); // PT47HS (47 horas)
        System.out.println(PeriodDuration.between(inicio, fim)); // P2DT-1H (2 dias e menos 1 hora)
        // é possível normalizar o PeriodDuration
        pd = PeriodDuration.between(inicio, fim).normalizedStandardDays();
        System.out.println(pd); // P1DT23H (1 dia e 23 horas)

        // mas a normalização nem sempre deixa os campos positivos
        pd = PeriodDuration.parse("P1DT-49H"); // 1 dia e menos 49 horas
        System.out.println(pd.normalizedStandardDays()); // P-1DT-1H (menos 1 dia e menos 1 hora)
        /*
         * O resultado é esse porque:
         * 
         * - a duração original é de 1 dia e menos 49 horas
         * 
         * - 1 dia é igual a 24 horas, e subtraindo-se 49 horas, temos menos 25 horas (`PT-25H`)
         * 
         * - menos 25 horas é o mesmo que menos 1 dia e menos 1 hora (basta pensar que subtrair 25 horas é o mesmo que subtrair 1 dia e depois subtrair 1 hora)
         */
    }

    /**
     * Ideia que existia no Joda-time (ver {@link JodaTimeExemplos#intervalos()}), mas ficou de fora do java.time
     */
    public static void intervalos() {
        // instante atual simulado: 4 de Maio de 2018, às 17:00 em São Paulo
        Instant inicio = Instant.now(Setup.clock());
        Instant fim = inicio.plusSeconds(589344593);
        // intervalo entre os instantes
        Interval intervalo = Interval.of(inicio, fim);
        // imprime no format ISO 8601: https://en.wikipedia.org/wiki/ISO_8601#Time_intervals
        System.out.println(intervalo); // 2018-05-04T20:00:00Z/2037-01-05T22:49:53Z
        System.out.println(intervalo.getStart()); // 2018-05-04T20:00:00Z
        System.out.println(intervalo.getEnd()); // 2037-01-05T22:49:53Z

        // duração do intervalo
        System.out.println(intervalo.toDuration()); // PT163706H49M53S (163706 horas, 49 minutos e 53 segundos)
        // verificar se um Instant pertence ao intervalo
        System.out.println(intervalo.contains(Instant.parse("2019-01-01T10:30:00Z"))); // true

        // ---------------------------------------
        // outro modo de criar um intervalo
        Duration duracao = Duration.ofHours(10); // duração de 10 horas
        // instante final do intervalo é 10 horas depois do inicial
        intervalo = Interval.of(inicio, duracao); // Duration é somada ao Instant inicial, para gerar o Instant final
        // o código abaixo é equivalente: o instante final também é 10 horas depois do inicial
        intervalo = Interval.of(inicio, inicio.plus(10, ChronoUnit.HOURS));

        // também é possível simular um intervalo só com instante inicial
        intervalo = Interval.of(inicio, Instant.MAX);
        System.out.println(intervalo.isUnboundedEnd()); // true (não tem instante final)
        // ou um intervalo sem início (só com o instante final)
        intervalo = Interval.of(Instant.MIN, fim);
        System.out.println(intervalo.isUnboundedStart()); // true (não tem instante inicial)
    }

    static void parseVariosFormatters() {
        // formatos possíveis para a data
        DateTimeFormatter fmt1 = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("MMM, dd uuuu", Locale.ENGLISH);
        DateTimeFormatter fmt3 = new DateTimeFormatterBuilder()
            // ano-mês, com dia opcional
            .appendPattern("uuuu-MM[-dd]")
            // valor default para o dia = 1 (quando a string não tiver o dia)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            // criar o DateTimeFormatter
            .toFormatter();

        // string pode estar em qualquer um dos 3 formatos
        LocalDate dt = Temporals.parseFirstMatching("May, 04 2018", LocalDate::from, fmt1, fmt2, fmt3);
        // O método tenta fazer o parsing com fmt1, fmt2 e fmt3 (ele para no primeiro que der certo)
        // O resultado é determinado pelo segundo parâmetro, que é um TemporalQuery (no caso, usei o method reference LocalDate::from)
        System.out.println(dt); // 2018-05-04
    }

    static void outros() {
        // conversões entre ChronoUnit e TimeUnit
        ChronoUnit chronoUnit = Temporals.chronoUnit(TimeUnit.HOURS);
        System.out.println(chronoUnit); // Hours
        try {
            // lança exceção, pois TimeUnit não possui equivalente para YEARS
            TimeUnit timeUnit = Temporals.timeUnit(ChronoUnit.YEARS);
            System.out.println(timeUnit);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()); // ChronoUnit cannot be converted to TimeUnit: Years
        }

        // TemporalAdjuster para dias úteis (considerando que dias úteis são: de segunda a sexta)
        LocalDate date = LocalDate.of(2018, 5, 4); // 2018-05-04
        // próximo dia útil
        System.out.println(date.with(Temporals.nextWorkingDay())); // 2018-05-07
        // dia útil anterior
        System.out.println(date.with(Temporals.previousWorkingDay())); // 2018-05-03
    }
}
