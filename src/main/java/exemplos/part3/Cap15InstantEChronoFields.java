package exemplos.part3;

import static exemplos.setup.Setup.setup;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import exemplos.setup.Setup;

// trata da classe java.time.Instant e como usar java.time.temporal.TemporalField e java.time.temporal.ChronoField
public class Cap15InstantEChronoFields {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) {
        testInstant();
        conversoes();
        comparacaoOrdenacao();
        usarChronoField();
        getVSgetLong();
        camposDoInstant();
        mudarCampos();
        criarTemporalQuery();
        usarMethodReferenceComoTemporalQuery();
        outrosTipos();
        verificarAnoBissexto();
        outrosExemplosTemporalQuery();
    }

    static void testInstant() {
        // instante (timestamp) atual
        Instant agora = Instant.now();
        // usando data/hora atual simulada
        agora = Instant.now(Setup.clock());
        System.out.println(agora); // 2018-05-04T20:00:00Z

        // obter o valor do timestamp
        long timestamp = agora.toEpochMilli(); // valor em milissegundos
        System.out.println(timestamp); // 1525464000000

        // ou
        long timestampSegundos = agora.getEpochSecond(); // valor em segundos
        int nanossegundos = agora.getNano(); // nanossegundos
        System.out.println(timestampSegundos + "." + nanossegundos); // 1525464000.0

        // obter um Instant a partir do timestamp
        // usar timestamp em milissegundos
        Instant instant = Instant.ofEpochMilli(1525464000000L);
        // usar timestamp em segundos
        Instant instant2 = Instant.ofEpochSecond(1525464000L);
        System.out.println(instant); // 2018-05-04T20:00:00Z
        System.out.println(instant2); // 2018-05-04T20:00:00Z

        // usar timestamp em segundos, mais os nanossegundos
        instant = Instant.ofEpochSecond(1525464000L, 123456789);
        System.out.println(instant); // 2018-05-04T20:00:00.123456789Z
        // mas toEpochMilli trunca o valor dos nanossegundos
        System.out.println(instant.toEpochMilli()); // 1525464000123
    }

    static void conversoes() {
        // 4 de maio de 2018
        LocalDate data = LocalDate.of(2018, 5, 4);

        // LocalDate só tem o dia, mês e ano. Para converter para Instant, precisamos saber o horário e o offset

        // usando um ZonedDateTime, o offset é calculado usando as regras do timezone
        // atStartOfDay seta o horário para meia-noite: 2018-05-04T00:00-03:00[America/Sao_Paulo]
        ZonedDateTime zdt = data.atStartOfDay(ZoneId.of("America/Sao_Paulo"));
        System.out.println(zdt.toInstant()); // 2018-05-04T03:00:00Z
        // usar um offset específico (em vez de um timezone): 2018-05-04T00:00+05:00
        OffsetDateTime odt = data.atTime(0, 0).atOffset(ZoneOffset.of("+05:00"));
        System.out.println(odt.toInstant()); // 2018-05-03T19:00:00Z
    }

    static void comparacaoOrdenacao() {
        Instant maio = OffsetDateTime.of(2018, 5, 1, 10, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant dezembro = OffsetDateTime.of(2018, 12, 1, 10, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant janeiro = OffsetDateTime.of(2018, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC).toInstant();

        // isBefore e isAfter considera a ordem cronológica (internamente, compara o valor do timestamp)
        System.out.println(janeiro.isBefore(maio)); // true
        System.out.println(dezembro.isAfter(maio)); // true

        // ZonedDateTime que corresponde ao mesmo instante (ao mesmo timestamp)
        ZonedDateTime zdt = ZonedDateTime.of(2018, 5, 1, 7, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
        System.out.println(maio.equals(zdt.toInstant())); // true

        // ordenação considera a ordem cronológica
        List<Instant> list = Arrays.asList(dezembro, maio, janeiro);
        Collections.sort(list);
        System.out.println(list); // [2018-01-01T10:00:00Z, 2018-05-01T10:00:00Z, 2018-12-01T10:00:00Z]
    }

    static void usarChronoField() {
        // 2018-05-04T17:00
        LocalDateTime data = LocalDateTime.of(2018, 5, 4, 17, 0);
        int mes = data.get(ChronoField.MONTH_OF_YEAR);
        System.out.println(mes); // 5 -> o mesmo que data.getMonthValue()
        int minuto = data.get(ChronoField.MINUTE_OF_HOUR);
        System.out.println(minuto); // 0 -> o mesmo que data.getMinute()

        // obter o minuto do dia (este campo não tem um getXXX equivalente, o único jeito de obtê-lo é usar ChronoField)
        int minutoDoDia = data.get(ChronoField.MINUTE_OF_DAY);
        System.out.println(minutoDoDia); // 1020 -> desde o início do dia (meia-noite), passaram-se 1020 minutos

        Instant instant = Instant.ofEpochSecond(1525464000L, 123456789);
        // ChronoField.MILLI_OF_SECOND retorna a fração de segundos em milissegundos
        int milissegundos = instant.get(ChronoField.MILLI_OF_SECOND);
        System.out.println(milissegundos); // 123 -> o mesmo que instant.getNano() / 1000000 (mas sem precisar fazer a conta)
    }

    static void getVSgetLong() {
        // 2018-05-04T17:00
        LocalDateTime dataHora = LocalDateTime.of(2018, 5, 4, 17, 0);

        try {
            // quantos nanossegundos se passaram desde a meia-noite, usar get() lança exceção
            int nanossegundosDoDia = dataHora.get(ChronoField.NANO_OF_DAY);
            System.out.println(nanossegundosDoDia); // não será executado, pois get() vai lançar exceção
        } catch (UnsupportedTemporalTypeException e) {
            System.out.println(e.getMessage()); // Invalid field 'NanoOfDay' for get() method, use getLong() instead
        }

        // obter os limites para os valores de NANO_OF_DAY
        ValueRange limites = dataHora.range(ChronoField.NANO_OF_DAY);
        System.out.println(limites); // 0 - 86399999999999
        /*
         * O valor máximo do campo é 86399999999999, que é mais do que um int suporta (por isso get() lança UnsupportedTemporalTypeException)
         * 
         * O maior valor que um int pode ter é 2147483647 (valor da constante Integer.MAX_VALUE)
         * 
         * Em vez de usar get() (que retorna um int), usamos getLong(), que retorna um long
         */
        long nanossegundosDoDia = dataHora.getLong(ChronoField.NANO_OF_DAY);
        System.out.println(nanossegundosDoDia); // 61200000000000

        // O método range ajusta os valores mínimo e máximo de acordo com o contexto. Ex:
        // verificar dia do mês para datas em fevereiro
        System.out.println(LocalDate.of(2018, 2, 1).range(ChronoField.DAY_OF_MONTH)); // 1 - 28
        System.out.println(LocalDate.of(2020, 2, 1).range(ChronoField.DAY_OF_MONTH)); // 1 - 29
        // Repare que o valor máximo para o dia do mês pode ser 28 ou 29 (pois depende se o ano é bissexto ou não)
    }

    static void camposDoInstant() {
        // 2018-05-04T20:00:00Z
        Instant instant = Instant.ofEpochSecond(1525464000L);
        try {
            // tentar obter a hora
            System.out.println(instant.get(ChronoField.HOUR_OF_DAY));
        } catch (UnsupportedTemporalTypeException e) {
            System.out.println(e.getMessage()); // Unsupported field: HourOfDay
            /*
             * Isso acontece porque a classe Instant não representa uma data e hora específicas, e sim um timestamp: um valor que corresponde a uma data e hora
             * diferentes em cada timezone. Por isso não é possível obter um valor para as horas, e nem para qualquer outro campo de data ou hora, como o dia,
             * mês, minutos etc. Para obter estes valores, temos que converter o Instant para um timezone ou offset, usando os métodos atZone(ZoneId) e
             * atOffset(ZoneOffset), conforme abaixo
             */
        }

        // converter para um timezone
        ZonedDateTime sp = instant.atZone(ZoneId.of("America/Sao_Paulo"));
        System.out.println(sp); // 2018-05-04T17:00-03:00[America/Sao_Paulo]
        // converter para um offset
        OffsetDateTime odt = instant.atOffset(ZoneOffset.of("+05:00"));
        System.out.println(odt); // 2018-05-05T01:00+05:00
        // agora sim é possível usar o ChronoField
        System.out.println(sp.get(ChronoField.HOUR_OF_DAY)); // 17
        System.out.println(odt.get(ChronoField.HOUR_OF_DAY)); // 1

        // se quiser a data e hora em UTC, use ZoneOffset.UTC
        OffsetDateTime utcDateTime = instant.atOffset(ZoneOffset.UTC);
        // use getHour() para obter a hora (ou .get(ChronoField.HOUR_OF_DAY))
        System.out.println(utcDateTime.getHour()); // 20

        // antes de usar get() ou getLong(), verifique se o campo é suportado
        if (instant.isSupported(ChronoField.HOUR_OF_DAY)) {
            // nesse caso, não entra no if, porque Instant não suporta o campo HOUR_OF_DAY
            System.out.println(instant.get(ChronoField.HOUR_OF_DAY));
            // Lembrando que mesmo que o campo seja suportado, o método get() ainda pode lançar uma exceção, caso o valor exceda os limites de um int.
        }
    }

    static void mudarCampos() {
        // 2018-05-04T20:00:00Z
        Instant instant = Instant.ofEpochSecond(1525464000L);
        // mudar os milissegundos
        instant = instant.with(ChronoField.MILLI_OF_SECOND, 123);
        // with sempre retorna outra instância, então não esqueça de atribuir o retorno do método em alguma variável
        System.out.println(instant); // 2018-05-04T20:00:00.123Z

        // 17:00
        LocalTime hora = LocalTime.of(17, 0);
        // mudar para o centésimo segundo do dia
        hora = hora.with(ChronoField.SECOND_OF_DAY, 100);
        System.out.println(hora); // 00:01:40
    }

    static void criarTemporalQuery() {
        // criar TemporalQuery ("temporal" é um TemporalAccessor)
        TemporalQuery<Boolean> fimDeSemana = temporal -> {
            // valor numérico do dia da semana
            int diaDaSemana = temporal.get(ChronoField.DAY_OF_WEEK);
            // comparar com o valor numérico de sábado e domingo
            return diaDaSemana == DayOfWeek.SATURDAY.getValue() || diaDaSemana == DayOfWeek.SUNDAY.getValue();
        };
        // 4 de maio de 2018
        LocalDate data = LocalDate.of(2018, 5, 4);
        System.out.println(data.query(fimDeSemana)); // false

        // TemporalQuery funciona com qualquer classe que implemente TemporalAccessor. Ou seja, eu posso usá-lo com qualquer tipo de data e hora da API, desde
        // que os campos sendo usados sejam suportados:
        // 6 de maio de 2018 (domingo)
        LocalDateTime dataHora = LocalDateTime.of(2018, 5, 6, 17, 0);
        System.out.println(dataHora.query(fimDeSemana)); // true
        // converte para um timezone, data continua a mesma
        ZonedDateTime dataHoraSP = dataHora.atZone(ZoneId.of("America/Sao_Paulo"));
        System.out.println(dataHoraSP.query(fimDeSemana)); // true
        try {
            // tentar usar com um LocalTime
            LocalTime hora = LocalTime.now();
            System.out.println(hora.query(fimDeSemana)); // exceção
        } catch (UnsupportedTemporalTypeException e) {
            // LocalTime não tem campos de data, então DAY_OF_WEEK não é suportado
            System.out.println(e.getMessage()); // Unsupported field: DayOfWeek
        }
    }

    static void usarMethodReferenceComoTemporalQuery() {
        // 4 de maio de 2018
        LocalDate data = LocalDate.of(2018, 5, 4);
        // usar method reference como um TemporalQuery
        // (útil se você já tiver essa classe no seu código legado, assim não precisa duplicar o código em uma TemporalQuery)
        boolean fds = data.query(DateUtils::isFimDeSemana);
        System.out.println(fds); // false
    }

    static class DateUtils {
        // Como DayOfWeek é um enum, posso usá-lo em um EnumSet
        private static final EnumSet<DayOfWeek> FIM_DE_SEMANA = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

        public static boolean isFimDeSemana(TemporalAccessor temporal) {
            // obter um DayOfWeek ao invés do valor numérico do campo
            DayOfWeek diaDaSemana = DayOfWeek.from(temporal);
            // verificar se é sábado ou domingo
            return FIM_DE_SEMANA.contains(diaDaSemana);
        }
    }

    // -------------------------------------------------
    // Outros tipos de data e hora
    static void outrosTipos() {
        // maio de 2018 (apenas o mês e ano, sem nenhuma informação sobre o dia)
        // Um uso para YearMonth é a data de validade de cartão de crédito (só possui o mês e ano)
        YearMonth maio = YearMonth.of(2018, 5);
        LocalDate dt = maio.atDay(1); // 1 de maio de 2018
        System.out.println(dt); // 2018-05-01
        dt = maio.atEndOfMonth(); // último dia do mês (já considerando regras do ano bissexto se o mês for fevereiro)
        System.out.println(dt); // 2018-05-31

        // janeiro de 2020
        YearMonth jan2020 = maio
            // mudar o mês para janeiro
            .withMonth(1)
            // somar 2 anos
            .plusYears(2);
        System.out.println(jan2020); // 2020-01 (formato ISO 8601 para "ano e mês sem o dia")

        // MonthDay possui apenas dia e mês (sem nenhuma informação sobre o ano)
        // É útil para datas recorrentes. Ex:
        MonthDay natal = MonthDay.of(12, 25); // 25 de dezembro
        System.out.println(natal); // --12-25 (formato ISO 8601 para "mês e dia sem o ano")
        // Os "--" na frente é para evitar ambiguidade com ano-mês: se fosse "12-25" poderia ser confundido com "ano 12 e mês 25" (inválido)

        // Natal deste ano -> usando a classe java.time.Year, que representa somente um ano (sem nenhuma informação sobre dia ou mês)
        int anoAtual = Year.now().getValue();
        // mas para este exemplo, vou usar a data/hora atual simulada
        anoAtual = Year.now(Setup.clock()).getValue(); // 2018
        LocalDate natalDesteAno = natal.atYear(anoAtual);
        System.out.println(natalDesteAno); // 2018-12-25

        // todas as classes possuem um método from() para serem criadas
        ZonedDateTime zdt = ZonedDateTime.now(Setup.clock());
        YearMonth ym = YearMonth.from(zdt); // extrai apenas o ano e mês do ZonedDateTime
        System.out.println(ym); // 2018-05

        MonthDay md = MonthDay.from(zdt);// extrai apenas o dia e mês do ZonedDateTime
        System.out.println(md); // --05-04

        DayOfWeek dow = DayOfWeek.from(zdt); // seria uma alternativa se ZonedDateTime não tivesse o método getDayOfWeek()
        System.out.println(dow); // FRIDAY
    }

    // várias formas de verificar se um ano é bissexto
    static void verificarAnoBissexto() {
        int ano = 2020;
        // Se você tiver apenas o valor numérico do ano e só quiser saber se é bissexto ou não, o jeito mais simples é:
        boolean bissexto = Year.isLeap(ano);
        System.out.println(bissexto); // true

        // PS: vou usar o mesmo ano nos próximos exemplos, então o resultado também será "true"

        // outra forma é criar um Year (vale a pena se vc vai usar este objeto para outras coisas depois, senão use o método acima)
        Year year = Year.of(ano);
        bissexto = year.isLeap(); // true
        /*
         * Este método vale a pena, por exemplo, se você quiser verificar o ano atual: Year.now().isLeap() verifica se o ano atual é bissexto
         * 
         * É melhor do que fazer Year.isLeap(Year.now().getValue()), por exemplo.
         * 
         * Mas se você já tiver o valor numérico do ano (em um int ou long), aí o melhor é usar Year.isLeap(ano) mesmo
         */

        // se você já tiver um ZonedDateTime, OffsetDateTime ou LocalDateTime, basta converter para LocalDate e usar o método isLeapYear()
        ZonedDateTime zdt = ZonedDateTime.of(ano, 5, 1, 10, 0, 0, 0, ZoneId.systemDefault());
        bissexto = zdt.toLocalDate().isLeapYear(); // true
        // OU passar o valor do ano diretamente para Year
        bissexto = Year.isLeap(zdt.getYear()); // true
        // PS: se vc já tiver uma instância de LocalDate, use isLeapYear() diretamente -> não precisa fazer Year.isLeap(localdate.getYear())

        // se você já tiver um YearMonth, basta usar o método isLeapYear()
        YearMonth ym = YearMonth.of(ano, 1);
        bissexto = ym.isLeapYear(); // true

        // ****************************************************************************************************
        // Os exemplos acima são as formas mais simples e as que eu recomendo usar (escolha a mais adequada para a sua situação)

        // Ainda há outras mais "ortodoxas" e deixo aqui apenas como curiosidade (não recomendo usar em produção, já que os exemplos acima são bem melhores)

        // tentar criar "29 de fevereiro" no ano em questão
        try {
            LocalDate.of(ano, 2, 29);
            bissexto = true;
            // se o ano for bissexto, a exceção não é lançada
        } catch (DateTimeException e) {
            bissexto = false;
        }

        // criar YearMonth com mês igual a fevereiro e verificar se a quantidade de dias deste mês é 29
        bissexto = YearMonth.of(ano, 2).lengthOfMonth() == 29; // lengthOfMonth() leva em conta se o ano é bissexto
        System.out.println(bissexto); // true

        // criar um MonthDay para 29 de fevereiro e verificar o ano
        MonthDay md = MonthDay.of(2, 29);
        bissexto = md.isValidYear(ano); // true
    }

    // mais alguns exemplos de uso de TemporalQuery
    static void outrosExemplosTemporalQuery() {
        // saudação baseada no horário
        TemporalQuery<String> saudacao = temporal -> {
            int hora = temporal.get(ChronoField.HOUR_OF_DAY);
            if (hora < 12) { // considera que de madrugada também é "Bom dia
                return "Bom dia";
            }

            if (12 <= hora && hora < 18) {
                return "Boa tarde";
            }

            return "Boa noite";
        };
        // usar data/hora atual simulada: 4 de Maio de 2018, às 17:00 em São Paulo
        ZonedDateTime dataHoraSP = ZonedDateTime.now(Setup.clock());
        System.out.println(dataHoraSP.query(saudacao)); // Boa tarde
        // em Tóquio, corresponde a 5 de maio, às 05:00 da manhã
        ZonedDateTime dataHoraTokyo = dataHoraSP.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
        System.out.println(dataHoraTokyo.query(saudacao)); // Bom dia

        // reaproveitar a query anterior para incrementar a mensagem
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(", 'agora são' HH:mm:ss");
        TemporalQuery<String> saudacao2 = temporal -> {
            String msg = temporal.query(saudacao); // obter a saudação
            return msg + formatter.format(temporal); // mostrar o horário, de acordo com pattern definido por DateTimeFormatter
        };
        System.out.println(dataHoraSP.query(saudacao2)); // Boa tarde, agora são 17:00:00
        System.out.println(dataHoraTokyo.query(saudacao2)); // Bom dia, agora são 05:00:00

        // Lembrando que TemporalQuery recebe um TemporalAccessor (ou seja, qualquer classe de data e hora) e pode retornar qualquer coisa que você precisar
        // (não precisa se limitar às classes nativas do Java). O único detalhe é que, como TemporalAccessor é uma interface-base para todos os tipos, nem
        // sempre todos os campos estarão disponíveis. Por exemplo, LocalDate não tem campos de horário, então não funciona com as queries acima.
        // Se quiser, você pode usar isSupported() para verificar se o campo é suportado, e lançar exceção ou mostrar uma mensagem de erro quando não for.
    }
}
