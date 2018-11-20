package exemplos.part3;

import static exemplos.setup.Setup.setup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.zone.ZoneRulesException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import exemplos.setup.Setup;

public class Cap20Migracao {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) throws ParseException {
        dateVsInstant();
        dateVsLocalDate();
        conversoesCalendar();
        timezoneVsZoneId();
        javaSql();
        quandoNaoConverter();
        ultimoInstanteDoDia();
        simpleDateFormatVsDateTimeFormatter();
    }

    static void dateVsInstant() {
        Date date = new Date();
        // converter para Instant
        Instant instant = date.toInstant();
        // converter de volta para Date
        date = Date.from(instant);

        // detalhe: Date tem precisão de milissegundos e Instant, de nanossegundos. Portanto, a conversão pode ocasionar perda de precisão
        // valor com nanossegundos (9 casas decimais)
        instant = Instant.parse("2018-01-01T10:00:00.123456789Z");
        System.out.println(instant); // 2018-01-01T10:00:00.123456789Z
        // converter para Date (os 6 últimos dígitos são perdidos)
        date = Date.from(instant);
        System.out.println(date); // Mon Jan 01 08:00:00 BRST 2018 (saída pode variar conforme o timezone default da sua JVM)
        // date.toString() não mostra os milissegundos, então vou usar SimpleDateFormat
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // usando UTC, para mostrar mesma data/hora do Instant
        System.out.println(sdf.format(date)); // 2018-01-01T10:00:00.123Z (os dígitos 456789 foram perdidos)
        // converter de volta para Instant
        instant = date.toInstant();
        System.out.println(instant); // 2018-01-01T10:00:00.123Z

        // Se vc está pensando em colocar 9 letras "S" no SimpleDateFormat só para ver se Date não tem os dígitos 456789, dê uma olhada na classe
        // exemplos.part2.Cap08e09FormatacaoParsing, método parseFracaoSegundos() e veja que isso não funciona (o máximo de letras "S" que funciona corretamente
        // é 3)

        // ---------------------------------------------
        // Se vc quiser ter todos os dígitos dos nanossegundos no Instant, deve mantê-los separadamente:
        // valor com nanossegundos (9 casas decimais)
        instant = Instant.parse("2018-01-01T10:00:00.123456789Z");
        // converter para Date
        date = Date.from(instant);
        // guardar o valor dos nanossegundos
        int nano = instant.getNano();
        // converter de volta para Instant
        Instant instant2 = date
            // Date perdeu os dígitos 456789, então o Instant só vai ter .123
            .toInstant()
            // restaurar o valor original dos nanossegundos (.123456789)
            .with(ChronoField.NANO_OF_SECOND, nano);
        System.out.println(instant2); // 2018-01-01T10:00:00.123456789Z
    }

    static void dateVsLocalDate() {
        // usar data/hora atual simulada
        Date date = new Date(Setup.clock().millis());
        // Date só guarda o valor do timestamp. Só que dependendo do timezone, o timestamp pode corresponder a um dia e horário diferentes
        // Por isso, para converter para LocalDate é necessário escolher um timezone
        LocalDate localDate = date
            // converter para Instant
            .toInstant()
            // converter para um timezone
            .atZone(ZoneId.of("America/Sao_Paulo"))
            // obter o LocalDate
            .toLocalDate();
        System.out.println(localDate); // 2018-05-04

        // se mudar o timezone, podemos obter uma data diferente
        localDate = date.toInstant().atZone(ZoneId.of("Asia/Tokyo")).toLocalDate();
        System.out.println(localDate); // 2018-05-05
        // tudo porque Date não representa uma única data, e sim um timestamp (um valor que pode corresponder a uma data/hora diferente em cada timezone)

        // ---------------------------------------------------
        // O oposto também pode mudar, dependendo do timezone escolhido

        localDate = LocalDate.of(2018, 5, 4); // 4 de maio de 2018
        // para ter um Date (ou seja, um timestamp), precisamos saber o horário e o offset (sem isso, não tem como ter um valor de timestamp)
        date = Date.from(
                // setar algum horário
                localDate
                    .atTime(10, 0)
                    // setar timezone
                    .atZone(ZoneId.of("America/Sao_Paulo"))
                    // converter para Instant
                    .toInstant());
        System.out.println(date); // Fri May 04 10:00:00 BRT 2018
        System.out.println(date.getTime()); // 1525438800000

        // se eu mudar o horário e o timezone, o resultado é completamente diferente. Ex: usar meia-noite em UTC
        date = Date.from(localDate.atStartOfDay(ZoneOffset.UTC).toInstant());
        System.out.println(date); // Thu May 03 21:00:00 BRT 2018 (meia-noite em UTC convertido para o timezone default da JVM - no caso, America/Sao_Paulo)
        System.out.println(date.getTime()); // 1525392000000

        // --------------------------------
        // para usar o timezone default da JVM, use ZoneId.systemDefault()

        // A partir do Java 9: LocalDate.ofInstant(date.toInstant(), ZoneId.of("..."));
        // https://docs.oracle.com/javase/9/docs/api/java/time/LocalDate.html#ofInstant-java.time.Instant-java.time.ZoneId-
    }

    static void conversoesCalendar() {
        // As conversões têm a mesma perda de precisão de date.toInstant(), conforme método dateVsInstant()

        // Calendar com data/hora "atual" simulada
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Setup.clock().millis());

        // converter para Instant
        Instant instant = cal.toInstant();

        // converter para Calendar
        cal = Calendar.getInstance();
        // setar o valor do timestamp
        cal.setTimeInMillis(instant.toEpochMilli());

        // ------------------------------------------
        // conversões de/para ZonedDateTime

        // data/hora atual simulada: 4 de Maio de 2018, às 17:00 em São Paulo
        ZonedDateTime zdt = ZonedDateTime.now(Setup.clock());
        System.out.println(zdt); // 2018-05-04T17:00-03:00[America/Sao_Paulo]
        // converter para GregorianCalendar
        GregorianCalendar gcal = GregorianCalendar.from(zdt);
        // converter de volta para ZonedDateTime
        zdt = gcal.toZonedDateTime(); // possui mesma data/hora e timezone do Calendar

        // GregorianCalendar é uma subclasse de Calendar. Quando criamos um Calendar com getInstance(), o Locale default da JVM determina qual a subclasse que é
        // criada. Para a grande maioria dos locales, GregorianCalendar é retornado. A linha abaixo provavelmente imprime "class java.util.GregorianCalendar"
        // a menos que o Locale default da sua JVM seja th_TH ou ja_JP_JP
        System.out.println(Calendar.getInstance().getClass()); // grandes chances de ser class java.util.GregorianCalendar
    }

    static void timezoneVsZoneId() {
        // identificador da IANA, conversão OK
        TimeZone timeZone = TimeZone.getTimeZone("America/Sao_Paulo");
        // converter para ZoneId
        ZoneId zoneId = timeZone.toZoneId();
        System.out.println(zoneId); // America/Sao_Paulo
        // converter de volta para TimeZone
        timeZone = TimeZone.getTimeZone(zoneId);
        System.out.println(timeZone); // sun.util.calendar.ZoneInfo[id="America/Sao_Paulo",offset=-10800000....

        // -------------------------------------------
        // abreviações: TimeZone aceita, mas ao converter para ZoneId, os resultados são inesperados

        // Retorna timezone da Índia, mas IST também é usada em Israel e Irlanda (quem definiu este "default"?)
        System.out.println(TimeZone.getTimeZone("IST").toZoneId()); // Asia/Kolkata
        // Retorna o offset -05:00 (ou seja, nem sequer retornou um timezone)
        System.out.println(TimeZone.getTimeZone("EST").toZoneId()); // -05:00

        // Lembrando que tentar criar um ZoneId com abreviação diretamente dá erro
        try {
            ZoneId.of("EST");
        } catch (ZoneRulesException e) {
            System.out.println(e.getMessage()); // Unknown time-zone ID: EST
        }
    }

    static void javaSql() {
        // java.sql.Date possui um timestamp, então dependendo do timezone default, ele pode corresponder a uma data diferente
        // timestamp 1525464000000 -> 2018-05-04T17:00-03:00
        java.sql.Date date = new java.sql.Date(1525464000000L);
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        System.out.println(date.toLocalDate()); // 2018-05-04
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        System.out.println(date.toLocalDate()); // 2018-05-05

        // converter LocalDate para java.sql.Date tem esse mesmo problema
        LocalDate localDate = LocalDate.of(2018, 5, 4); // 4 de maio de 2018
        // valueOf usa o timezone default da JVM e usa meia-noite como horário
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        date = java.sql.Date.valueOf(localDate);
        System.out.println(date); // 2018-05-04
        System.out.println(date.getTime()); // 1525402800000
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        date = java.sql.Date.valueOf(localDate);
        System.out.println(date); // 2018-05-04
        System.out.println(date.getTime()); // 1525359600000
        // Apesar das datas impressas serem as mesmas (2018-05-04), o timestamp é completamente diferente. Isso acontece porque o valor do timestamp equivale à
        // 4 de maio de 2018, à meia-noite no timezone default que estava setado na JVM no momento em que valueOf foi chamado

        // java.sql.Time e java.sql.Timestamp sofrem do mesmo problema ao converter de/para as classes do java.time

        // voltar config default (para não impactar os outros métodos)
        setup();
    }

    static void quandoNaoConverter() {
        // não precisa fazer isso para obter o Instant atual
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // getInstance já pega a data/hora atual, então esta linha já é redundante
        Instant instant = Instant.ofEpochMilli(cal.getTimeInMillis()); // troque tudo isso por Instant.now() e pronto!

        // "Ah, mas talvez fizeram assim porque `Instant` tem precisão de nanossegundos e eles só precisam dos milissegundos"
        // neste caso, você pode usar truncatedTo
        instant = Instant.now().truncatedTo(ChronoUnit.MILLIS); // trunca o Instant, mantendo apenas os milissegundos
        // se o valor do Instant original fosse 2018-08-17T12:15:10.123456789Z, o resultado de truncatedTo(ChronoUnit.MILLIS) seria 2018-08-17T12:15:10.123Z

        // outra opcão é criar um Clock que ignora frações de segundo menores que 1 milissegundo
        Clock clock = Clock.tick(Clock.systemDefaultZone(), Duration.ofMillis(1));
        instant = Instant.now(clock);

        // ---------------------------------------------------
        // não misture as APIs desnecessariamente
        ZoneId defaultJvm = TimeZone.getDefault().toZoneId(); // não
        defaultJvm = ZoneId.systemDefault(); // sim

        // ---------------------------------------------------
        // E não esqueça que, apesar de ter métodos de conversão (Date para Instant, etc), as APIs são incompatíveis entre si
        try {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            // parse retorna um TemporalAccessor, que não é compatível com Date (por isso lança exceção)
            Date date = (Date) formato.parse("10/02/2018 17:30:00");
        } catch (ClassCastException e) {
            System.out.println(e.getMessage()); // java.time.format.Parsed cannot be cast to java.util.Date
        }

        /*
         * Particularmente, um dos casos em que misturo a API legada com java.time é quando estou usando bibliotecas que ainda precisam de Date ou Calendar e
         * tenho que fazer alguma manipulação nestes objetos. Por exemplo, suponha que estou usando uma API que retorna um Date, que eu preciso passar para
         * outra API, só que o valor recebido por esta deve ter o horário setado para o fim do dia:
         * 
         * Date date = algumaAPI.getDate(); // API que retorna java.util.Date
         * 
         * date = ... // manipular o Date (setar horário para fim do dia)
         * 
         * outraAPI.fazAlgo(date); // passar o Date para outra API
         * 
         * É possível fazer estes cálculos usando Calendar, mas neste caso eu prefiro converter o Date para o java.time (seja para LocalDate, Instant, ou
         * qualquer outra classe que seja mais fácil de trabalhar, dependendo do que preciso fazer), efetuar os cálculos e converter o resultado de volta para
         * Date.
         */
    }

    static void ultimoInstanteDoDia() {
        // tentar obter o último instante do dia 19/06/2009, em algum timezone
        LocalDate dia = LocalDate.of(2009, 6, 19);

        // não basta setar o horário para 23:59:59.999999999 e pronto, graças às bizarrices dos timezones
        ZonedDateTime fimDoDiaErrado = dia
            // setar horário para 23:59:59.999999999
            .atTime(LocalTime.MAX)
            // converter para o timezone de Bangladesh
            .atZone(ZoneId.of("Asia/Dhaka"));
        // O resultado é 00:59 do dia 20 (ou seja, nada parecido com "o último instante do dia 19")
        System.out.println(fimDoDiaErrado); // 2009-06-20T00:59:59.999999999+07:00[Asia/Dhaka]

        // Em Bangladesh, houve uma mudança de offset neste dia: às 23h do dia 19 o relógio foi adiantado em 1 hora, direto para meia-noite do dia 20
        // Por isso, 23:59 faz parte de um gap e ZonedDateTime fez o ajuste para a próxima hora válida (00:59)

        // O modo correto de se obter o último instante do dia é: primeiro obter o início do dia seguinte e depois subtrair 1 nanossegundo. Com isso eu
        // garanto que peguei o último instante daquele dia:
        ZonedDateTime fimDoDia = dia
            // início do dia seguinte no timezone
            .plusDays(1)
            .atStartOfDay(ZoneId.of("Asia/Dhaka"))
            // subtrair 1 nanossegundo
            .minusNanos(1);
        System.out.println(fimDoDia); // 2009-06-19T22:59:59.999999999+06:00[Asia/Dhaka]
        // para ter certeza que este é o último instante do dia 19, basta somar 1 nanossegundo e ver se o resultado é o início do dia 20
        System.out.println(fimDoDia.plusNanos(1)); // 2009-06-20T00:00+07:00[Asia/Dhaka]
    }

    static void simpleDateFormatVsDateTimeFormatter() throws ParseException {
        // alguns patterns funcionam do mesmo jeito
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Date date = new Date(Setup.clock().millis());
        System.out.println(sdf.format(date)); // 04/05/2018 17:00
        System.out.println(OffsetDateTime.now(Setup.clock()).format(fmt)); // 04/05/2018 17:00

        // ----------------------------------------------------
        // mas nem todos os patterns são assim
        ZonedDateTime zdt = ZonedDateTime.now(Setup.clock());

        // alguns são diferentes
        System.out.println(zdt.format(DateTimeFormatter.ofPattern("uuuu"))); // 2018 (ano)
        System.out.println(new SimpleDateFormat("uuuu").format(date)); // 0005 (dia da semana -> 5 é sexta-feira)

        // outros não existem na API antiga
        // dia da semana, trimestre e nome do timezone
        fmt = DateTimeFormatter.ofPattern("eeee QQQQ VV", new Locale("pt", "BR"));
        System.out.println(zdt.format(fmt)); // Sexta-feira 2º trimestre America/Sao_Paulo
        // esses patterns não funcionam com SimpleDateFormat
        try {
            new SimpleDateFormat("eeee");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()); // Illegal pattern character 'e'
        }
        try {
            new SimpleDateFormat("QQQQ");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()); // Illegal pattern character 'Q'
        }
        try {
            new SimpleDateFormat("VV");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()); // Illegal pattern character 'V'
        }

        // ----------------------------------------------------
        // outro exemplo: pattern "h" (hour-of-am-pm, com valores de 1 a 12)
        // este campo é ambíguo se não tivermos o identificador AM/PM
        sdf = new SimpleDateFormat("hh:mm");
        // faz parsing de 17:00, mesmo que o campo "h" tenha valores de 1 a 12
        System.out.println(sdf.parse("17:00")); // Thu Jan 01 17:00:00 BRT 1970
        // DateTimeFormatter, por sua vez, não aceita, nem se estiver em modo leniente
        fmt = DateTimeFormatter.ofPattern("hh:mm").withResolverStyle(ResolverStyle.LENIENT);
        try {
            // mesmo se o valor for válido (o campo aceita valores de 1 a 12), sem a informação AM/PM não é possível resolver o horário
            LocalTime.parse("10:00", fmt);
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage()); // Text '10:00' could not be parsed: Unable to obtain LocalTime from TemporalAccessor: {HourOfAmPm=10,
                                                // MinuteOfHour=0},ISO of type java.time.format.Parsed
        }
        // para resolver o parsing acima, há algumas alternativas:

        // 1- A String de entrada deve ter a informação se é AM ou PM
        fmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH); // usar Locale, porque o campo AM/PM é locale sensitive
        System.out.println(LocalTime.parse("10:00 PM", fmt)); // 22:00

        // 2- setar valor predefinido para o campo AM/PM
        fmt = new DateTimeFormatterBuilder()
            // hora e minuto
            .appendPattern("hh:mm")
            // valor default para AM/PM -> 0 para AM e 1 para PM
            .parseDefaulting(ChronoField.AMPM_OF_DAY, 1L)
            .toFormatter();
        System.out.println(LocalTime.parse("10:00", fmt)); // 22:00

        // 3- usar "HH" (nesse caso, o campo tem valores de 0 a 23, ou seja, não preciso saber se é AM ou PM)
        fmt = DateTimeFormatter.ofPattern("HH:mm");
        System.out.println(LocalTime.parse("10:00", fmt)); // 10:00 <- se quiser 10 da noite, a string deve ser "22:00"

        // resumindo, o que funcionava com SimpleDateFormat não necessariamente vai funcionar com DateTimeFormatter (e vice-versa)
    }
}
