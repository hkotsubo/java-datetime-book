package exemplos.part3;

import static exemplos.setup.Setup.clock;
import static exemplos.setup.Setup.setup;
import static java.time.temporal.TemporalAdjusters.dayOfWeekInMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextYear;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.firstInMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastInMonth;
import static java.time.temporal.TemporalAdjusters.next;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Cap13PrincipiosBasicos {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) {
        criarDatas();
        obterInfo();
        mudarValores();
        usarTemporalAdjuster();
        ajustesComAsClassesLocais();
        comparar();
        ordenar();

        // --------------------------------------------
        // exemplos que não estão no livro
        outrosModosDeCriarDatas();
    }

    static void criarDatas() {
        // 4 de maio de 2018
        LocalDate data = LocalDate.of(2018, 5, 4);
        // saída no formato ISO 8601
        System.out.println(data); // 2018-05-04

        try {
            // tentando criar data com mês 13
            LocalDate mesInvalido = LocalDate.of(2018, 13, 1);
        } catch (DateTimeException e) {
            System.out.println(e.getMessage()); // Invalid value for MonthOfYear (valid values 1 - 12): 13
        }

        // 17:30 (cinco e meia da tarde)
        LocalTime horario = LocalTime.of(17, 30);
        // 2018-05-04T17:30
        LocalDateTime dataHora = LocalDateTime.of(2018, 5, 4, 17, 30);

        System.out.println(horario); // 17:30
        System.out.println(dataHora); // 2018-05-04T17:30

        // saída omite os segundos e frações de segundo, se forem zero
        System.out.println(LocalTime.of(17, 0)); // 17:00
        System.out.println(LocalTime.of(7, 0, 45)); // 07:00:45
        System.out.println(LocalTime.of(0, 0, 0, 123000000)); // 00:00:00.123

        // data/hora atual
        // usando a data/hora "atual" simulada
        LocalDate dataAtual = LocalDate.now(clock());
        LocalTime horarioAtual = LocalTime.now(clock());
        LocalDateTime dataHoraAtual = LocalDateTime.now(clock());
        System.out.println(dataAtual); // 2018-05-04
        System.out.println(horarioAtual); // 17:00
        System.out.println(dataHoraAtual); // 2018-05-04T17:00
        // now() sem parâmetros usa o relógio do sistema e o timezone padrão da JVM
        // LocalDate dataAtual = LocalDate.now();
        // LocalTime horarioAtual = LocalTime.now();
        // LocalDateTime dataHoraAtual = LocalDateTime.now();
    }

    static void obterInfo() {
        // 4 de maio de 2018
        LocalDate data = LocalDate.of(2018, 5, 4);
        int diaDoMes = data.getDayOfMonth(); // 4
        int ano = data.getYear(); // 2018
        Month mes = data.getMonth(); // Month.MAY
        int valorNumericoMes = data.getMonthValue(); // 5
        int diaDoAno = data.getDayOfYear(); // 124 (pois 4 de maio de 2018 é o centésimo vigésimo quarto dia do ano)
        DayOfWeek diaDaSemana = data.getDayOfWeek(); // DayOfWeek.FRIDAY
    }

    static void mudarValores() {
        // 4 de maio de 2018
        LocalDate data = LocalDate.of(2018, 5, 4);
        // mudar o dia do mês para 1 (a variável "data" continua sendo dia 4)
        LocalDate primeiroDeMaio = data.withDayOfMonth(1);
        System.out.println(primeiroDeMaio); // 2018-05-01
        // "data" continua sendo dia 4
        System.out.println(data); // 2018-05-04

        // 2018-05-04T17:00:35.123
        LocalDateTime dataHora = LocalDateTime.of(2018, 5, 4, 17, 0, 35, 123000000);
        // mudar para 2015-05-01T10:30:35.123
        LocalDateTime dataModificada = dataHora
            // muda o dia do mês para 1
            .withDayOfMonth(1)
            // muda o ano para 2015
            .withYear(2015)
            // mudar hora para 10
            .withHour(10)
            // mudar minuto para 30
            .withMinute(30);

        System.out.println(dataModificada); // 2015-05-01T10:30:35.123
        // "datHora" permanece inalterada
        System.out.println(dataHora); // 2018-05-04T17:00:35.123
    }

    static void usarTemporalAdjuster() {
        // 4 de maio de 2018
        LocalDate data = LocalDate.of(2018, Month.MAY, 4);
        // mudar para o último dia do mês
        LocalDate ultimoDiaDeMaio = data.with(lastDayOfMonth());
        System.out.println(ultimoDiaDeMaio); // 2018-05-31

        // -----------------------------------------------------------
        // outros adjusters
        System.out.println(data.with(firstDayOfMonth())); // 2018-05-01
        System.out.println(data.with(firstDayOfNextMonth())); // 2018-06-01
        System.out.println(data.with(firstDayOfYear())); // 2018-01-01
        System.out.println(data.with(firstDayOfNextYear())); // 2019-01-01

        // -----------------------------------------------------------
        // ajustes com dia da semana
        // próxima Sexta-feira: 2018-05-11
        LocalDate proximaSexta = data.with(next(DayOfWeek.FRIDAY));

        // terceira Quinta-feira do mês: 2018-05-17
        LocalDate terceiraQuinta = data.with(dayOfWeekInMonth(3, DayOfWeek.THURSDAY));

        // primeiro Sábado do mês: 2018-05-05
        LocalDate primeiroSabado = data.with(firstInMonth(DayOfWeek.SATURDAY));

        // último Sábado do mês: 2018-05-26
        LocalDate ultimoSabado = data.with(lastInMonth(DayOfWeek.SATURDAY));

        try {
            // LocalTime não tem dia, então ajustar para o primeiro dia do mês lança exceção
            LocalTime horario = LocalTime.now().with(firstDayOfMonth());
        } catch (UnsupportedTemporalTypeException e) {
            System.out.println(e.getMessage()); // Unsupported field: DayOfMonth
        }
    }

    static void ajustesComAsClassesLocais() {
        // 2018-05-04T10:00:35.123456
        LocalDateTime dataHora = LocalDateTime.of(2018, 5, 4, 10, 0, 35, 123456000);
        // mudar horário para 17:30 -> 2018-05-04T17:30
        dataHora = dataHora.with(LocalTime.of(17, 30)); // todos os campos de horário são mudados

        // -----------------------------------------------------------
        // 2018-05-04T00:00
        dataHora = LocalDateTime.of(2018, 5, 4, 0, 0);
        // mudar data para 1 de janeiro de 2001 -> 2001-01-01T00:00
        dataHora = dataHora.with(LocalDate.of(2001, 1, 1));
    }

    static void comparar() {
        // -----------------------------------------------------------
        // comparar datas (somente dia, mês e ano)
        // 4 de maio de 2018
        LocalDate maio = LocalDate.of(2018, 5, 4);
        // 10 de janeiro de 2018
        LocalDate janeiro = LocalDate.of(2018, 1, 10);
        boolean maioDepoisDeJaneiro = maio.isAfter(janeiro); // true
        boolean maioAntesDeJaneiro = maio.isBefore(janeiro); // false

        // -----------------------------------------------------------
        // comparar horários (somente hora, minuto, segundo, nanossegundo)
        LocalTime dezDaNoite = LocalTime.of(22, 0);
        LocalTime tresDaManha = LocalTime.of(3, 0);
        boolean antes = dezDaNoite.isBefore(tresDaManha); // false

        // -----------------------------------------------------------
        // comparar data e horário
        // 2018-05-04T22:00
        LocalDateTime dataHora1 = LocalDateTime.of(2018, 5, 4, 22, 0);
        // 2018-05-05T03:00
        LocalDateTime dataHora2 = LocalDateTime.of(2018, 5, 5, 3, 0);
        antes = dataHora1.isBefore(dataHora2); // true
    }

    static void verificarDatasIguais() {
        // somente data
        LocalDate data1 = LocalDate.of(2018, 5, 4);
        LocalDate data2 = LocalDate.of(2018, Month.MAY, 4);
        boolean datasIguais = data1.equals(data2); // true

        // -----------------------------------------------------------
        // LocalDate retorna false se a outra classe não for LocalDate
        // 2018-05-04T17:00
        LocalDateTime dataHora = data1.atTime(17, 0);
        boolean iguais = data1.equals(dataHora); // false
        // A comparação deve ser feita entre dois LocalDates
        iguais = data1.equals(dataHora.toLocalDate()); // true
    }

    static void ordenar() {
        List<LocalDate> lista = Arrays.asList(LocalDate.of(2018, 5, 4), // 2018-05-04
                LocalDate.of(1995, 5, 4), // 1995-05-04
                LocalDate.of(2018, 1, 20)); // 2018-01-20
        Collections.sort(lista);

        // datas são ordenadas em ordem alfabética
        System.out.println(lista); // [1995-05-04, 2018-01-20, 2018-05-04]
    }

    static void outrosModosDeCriarDatas() {
        // Construir 2018-05-04 usando o epoch-day (quantidade de dias desde o Unix Epoch)
        LocalDate data = LocalDate.ofEpochDay(17655);

        // centésimo dia de 2018 (2018-04-10)
        data = LocalDate.ofYearDay(2018, 100);
        // Internamente, LocalDate só possui os valores do dia, mês e ano.
        // O epochDay e yearDay são usados para calcular estes valores, mas não fazem parte do LocalDate

        // -----------------------------------------------------------
        // centésimo segundo do dia (considerando que o dia começa à meia-noite)
        LocalTime time = LocalTime.ofSecondOfDay(100); // 00:01:40

        // centésimo nanossegundo do dia
        time = LocalTime.ofNanoOfDay(100); // 00:00:00.000000100
    }
}
