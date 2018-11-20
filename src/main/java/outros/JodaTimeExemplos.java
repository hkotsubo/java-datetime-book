package outros;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalInstantException;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.DateTimePrinter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * Exemplos com a biblioteca Joda-Time.
 * 
 * Esta biblioteca foi a "predecessora" do java.time, com muitas de suas ideias aproveitadas no Java 8, pelo criador de ambas as APIs, Stephen Colebourne.
 * Apesar disso, elas não são iguais, como o autor informa em seu blog:
 * 
 * <ul>
 * <li>https://blog.joda.org/2014/11/converting-from-joda-time-to-javatime.html</li>
 * <li>https://blog.joda.org/2009/11/why-jsr-310-isn-joda-time_4941.html</li>
 * </ul>
 * 
 * Lembrando que o Joda-Time é um projeto "encerrado" e em seu próprio site é recomendado que se use o java.time: https://www.joda.org/joda-time/#Support
 *
 * Para JDK 6 e 7, uma alternativa é usar o ThreeTen Backport: https://www.threeten.org/threetenbp/ - um excelente backport para o java.time, com a grande
 * maioria das funcionalidades da API. Veja alguns exemplos na classe {@link ThreetenBackportExemplos}
 * 
 * Eu recomendaria o Joda-Time se há muito código legado já usa esta biblioteca (e a migração para o java.time/Threeten Backport é "inviável") ou se a versão do
 * JDK é <= 5 (pois ainda sim é uma alternativa melhor do que java.util.Date e SimpleDateFormat) e não há perspectiva de atualizar o JDK
 */
public class JodaTimeExemplos {

    public static void main(String[] args) {
        funcionamentoBasico();
        comparacaoJavaTime();
        comparacaoJavaTimeHorarioDeVerao();
        comparacaoJavaTimeFormatacao();
        comparacaoJavaTimeTests();
        formatarPeriodos();
        calculaIdadeNascido29Fev();
        converterParaDate();
    }

    static void funcionamentoBasico() {
        // usando um org.joda.time.LocalDate para criar a data 20 de fevereiro de 2018
        LocalDate vinteFev = new LocalDate(2018, 2, 20);
        // somar 10 dias
        LocalDate dezDiasDepois = vinteFev.plusDays(10);
        System.out.println(dezDiasDepois); // 2018-03-02 <- saída no formato ISO 8601
        // LocalDate só tem dia, mês e ano (sem noção de horário ou timezones, portanto não é afetado por efeitos de horário de verão
        // O Joda-Time usou a abordagem de ter vários tipos diferentes de data e hora, para situações diferentes e esta ideia foi aproveitada no java.time

        // Outra ideia que foi aproveitada no java.time foi a interface fluida e métodos específicos para somar datas, converter entre os tipos, etc
        DateTime datetime = new LocalDate() // data atual
            // mais 5 dias
            .plusDays(5)
            // às 10:00 da manhão
            .toLocalDateTime(new LocalTime(10, 0))
            // em UTC
            .toDateTime(DateTimeZone.UTC);
        System.out.println(datetime); // 2018-11-25T10:00:00.000Z
    }

    // Comparando o Joda-time com o java.time
    static void comparacaoJavaTime() {
        // Joda-time: uso do construtor para obter data e hora atual
        DateTime dataHoraAtual = new DateTime();
        // mas também é possível usar now()
        dataHoraAtual = DateTime.now();
        // uso de construtor para uma data específica
        LocalDate jodaDate = new LocalDate(2018, 2, 1);

        // java.time: uso de factory methods "now()" e "of()"
        java.time.LocalDateTime datetimeAtual = java.time.LocalDateTime.now();
        java.time.LocalDate date = java.time.LocalDate.of(2018, 2, 1);

        // mas ambos os métodos toString() usam o formato ISO 8601
        System.out.println(jodaDate); // 2018-02-01
        System.out.println(date); // 2018-02-01

        // ---------------------------------------
        // alguns métodos têm os mesmos nomes
        jodaDate = jodaDate.withDayOfMonth(10); // retorna outra data com o dia 10
        date = date.withDayOfMonth(10);
        // outros têm nomes diferentes
        jodaDate = jodaDate.withMonthOfYear(5); // muda o mês para maio
        date = date.withMonth(5);

        // ------------------------------------------
        // no Joda-time, dias da semana são valores numéricos
        int dayOfWeek = jodaDate.getDayOfWeek();
        System.out.println(dayOfWeek); // 4
        // para saber qual é o dia, compare com os valores de DateTimeConstants
        switch (dayOfWeek) {
            case DateTimeConstants.SUNDAY:
                // domingo
                break;
            case DateTimeConstants.MONDAY:
                // segunda-feira
                break;
            // ... etc
        }
        // no java.time, dias da semana são valores de um enum
        DayOfWeek dow = date.getDayOfWeek();
        System.out.println(dow); // THURSDAY

        // outra diferença fundamental é que o java.time têm precisão de nanossegundos (9 casas decimais na fração de segundos), enquanto o Joda-time suporta
        // apenas milissegundos (3 casas decimais)
    }

    // Comparando o Joda-time com o java.time, com relação ao horário de verão
    static void comparacaoJavaTimeHorarioDeVerao() {
        // Os exemplos usam o horário de verão de 2017: à meia-noite do dia 15 de outubro os relógios são adiantados em uma hora, direto para 01:00
        // Por isso, todos os minutos entre 00:00 e 00:59 não existem no timezone America/Sao_Paulo, neste dia. Se eu tentar criar uma data no dia 15 com
        // horário entre 00:00 e 00:59, cada API lida com isso de uma maneira diferente

        // java.time
        // 15 de outubro de 2017, meia-noite e meia, timezone America/Sao_Paulo
        ZonedDateTime z = ZonedDateTime.of(2017, 10, 15, 0, 30, 0, 0, ZoneId.of("America/Sao_Paulo"));
        // Devido ao horário de verão, 00:30 é ajustado para o próximo horário válido -> 01:30
        System.out.println(z); // 2017-10-15T01:30-02:00[America/Sao_Paulo]

        // Joda-time
        // 15 de outubro de 2017, meia-noite e meia, timezone America/Sao_Paulo
        try {
            // não faz ajuste como o java.time, em vez disso lança exceção
            new DateTime(2017, 10, 15, 0, 30, 0, 0, DateTimeZone.forID("America/Sao_Paulo"));
        } catch (IllegalInstantException e) {
            // Illegal instant due to time zone offset transition (daylight savings time 'gap'): 2017-10-15T00:30:00.000 (America/Sao_Paulo)
            System.out.println(e.getMessage());
        }
        // para corrigir, primeiro devemos verificar se aquela data e hora existe no timezone em questão, e caso não exista, fazemos um ajuste
        // timezone America/Sao_Paulo
        DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
        // 15 de outubro de 2017, meia-noite e meia (sem timezone)
        LocalDateTime localDt = new LocalDateTime(2017, 10, 15, 0, 30, 0, 0);
        // se o localDt está em um DST gap, ajustar para a hora seguinte
        if (zone.isLocalDateTimeGap(localDt)) {
            localDt = localDt.plusHours(1);
        }
        // criar o DateTime: 15 de outubro de 2017, à 01:30, timezone America/Sao_Paulo
        DateTime dt = localDt.toDateTime(zone);
        System.out.println(dt); // 2017-10-15T01:30:00.000-02:00
        /*
         * O código acima pode funcionar para grande parte dos casos, mas lembre-se que nem todos os DST gaps são de 1 hora. Há timezones, como por exemplo
         * Australia/Lord_Howe, que adiantam o relógio em 30 minutos durante o horário de verão
         * (https://www.timeanddate.com/time/zone/australia/lord-howe-island?year=1980). Ou ainda Asia/Kuching, que nos anos 30 adiantava o relógio em apenas 20
         * minutos (https://www.timeanddate.com/time/zone/malaysia/kuching?year=1925).
         * 
         * Além disso, nem todos os gaps são por causa do horário de verão. Há casos em que determinada região simplesmente muda seu fuso horário de forma
         * "definitiva" (entre aspas porque nada garante que não mudará novamente). Um exemplo recente é a Coreia do Norte, que no dia 5 de maio de 2018 à
         * meia-noite adiantou o relógio em 30 minutos, para alinhar seu horário com a Coreia do Sul. É um gap de 30 minutos, porém não relacionado ao horário
         * de verão. Mas por ser um gap, também pode causar um IllegalInstantException.
         * 
         * Outro exemplo notável é o caso de Samoa, que em 30 de dezembro de 2011 à meia-noite mudou seu offset de -10:00 para +14:00. Com isso, todo o dia 30
         * foi pulado, ou seja, um gap de 24 horas! Todos os horários do dia 30 de dezembro de 2011 não existem neste timezone.
         * 
         * Por isso, a solução anterior (somar 1 hora ao LocalDateTime) não funciona bem para estes casos. No caso de Samoa, por exemplo, teríamos que somar
         * pelo menos 24 horas para ter certeza que não estamos mais dentro do gap. Mas ao somar 24 horas, corremos o risco de obter uma data "adiantada"
         * demais. Um jeito de contornar isso seria ir somando 1 minuto, até encontrar um horário válido, usando o while abaixo:
         */
        // ajustar para o minuto seguinte, até encontrar uma hora que não está em um gap
        while (zone.isLocalDateTimeGap(localDt)) {
            localDt = localDt.plusMinutes(1);
        }
        // O java.time, por sua vez, faz estes ajustes internamente, sempre verificando o gap e gerando uma data e hora válidas no timezone em questão
    }

    static void comparacaoJavaTimeFormatacao() {
        // Joda-Time tem duas classes: o DateTimeFormat, que cria um DateTimeFormatter (repare no "er" no final)
        DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
        // java.time: apenas a classe DateTimeFormatter, com factory methods
        java.time.format.DateTimeFormatter javatimeFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu");

        /*
         * Lembrando que nem todos os patterns são iguais. No java.time o pattern "uuuu" serve para lidar corretamente com anos negativos, mas no Joda-time esse
         * pattern não existe e lança exceção.
         * 
         * Outro exemplo são os dias da semana. No Joda-Time, a letra "e" representa o dia da semana conforme os valores das respectivas constantes na classe
         * DateTimeConstants: Segunda-feira tem valor 1, Terça-feira é 2 etc. Porém, no java.time, a letra "e" representa o dia da semana conforme o Locale
         * utilizado (e se nenhum Locale for informado, é usado o locale padrão da JVM). De acordo com o Locale, a semana pode começar no Sábado, Domingo ou
         * Segunda-feira (veja o capítulo sobre locales e os dias da semana), então um DateTimeFormatter com a letra "e" pode retornar um valor diferente para a
         * mesma data, dependendo do Locale utilizado (algo que não ocorre no Joda-Time, que usa uma definição de semana que não depende do Locale).
         */
        // Joda-time: usa os valores de DateTimeConstants
        jodaFormatter = DateTimeFormat.forPattern("e");
        System.out.println(jodaFormatter.print(new LocalDate(2018, 5, 4))); // 5 (DateTimeConstants.FRIDAY = sexta-feira)

        // java.time: muda conforme o locale
        java.time.LocalDate d = java.time.LocalDate.of(2018, 5, 4);
        javatimeFormatter = java.time.format.DateTimeFormatter.ofPattern("e");
        System.out.println(d.format(javatimeFormatter.withLocale(Locale.UK))); // 5
        System.out.println(d.format(javatimeFormatter.withLocale(new Locale("pt", "BR")))); // 6
        /*
         * Isso acontece porque cada Locale tem uma definição diferente quanto ao primeiro dia da semana. Para Locale.UK, a semana começa na segunda, então
         * sexta-feira tem valor 5. Já para pt_BR a semana começa no domingo, então sexta-feira tem valor 6.
         */
        // neste caso, é possível simular o comportamento do Joda-time usando o pattern "ccccc"
        javatimeFormatter = java.time.format.DateTimeFormatter.ofPattern("ccccc");
        System.out.println(d.format(javatimeFormatter.withLocale(Locale.UK))); // 5
        System.out.println(d.format(javatimeFormatter.withLocale(new Locale("pt", "BR")))); // 5

        // ----------------------------- Patterns opcionais
        // java.time: pattern opcional, pode ser uuuu-MM-dd ou dd/MM/uuuu
        javatimeFormatter = java.time.format.DateTimeFormatter.ofPattern("[uuuu-MM-dd][dd/MM/uuuu]");
        // ambos funcionam
        d = java.time.LocalDate.parse("2018-02-01", javatimeFormatter);
        d = java.time.LocalDate.parse("01/02/2018", javatimeFormatter);
        // também é possíve construir o mesmo formatter com DateTimeFormatterBuilder
        javatimeFormatter = new java.time.format.DateTimeFormatterBuilder()
            .optionalStart()
            .appendPattern("uuuu-MM-dd")
            .optionalEnd()
            .optionalStart()
            .appendPattern("dd/MM/uuuu")
            .toFormatter();
        // o problema é que usar patterns opcionais com os mesmos campos só é bom para parsing, pois para formatação ele mostra os campos nos 2 formatos
        System.out.println(d.format(javatimeFormatter)); // 2018-02-0101/02/2018 <- a mesma data nos 2 formatos
        // se quiser formatar a data em outro formato, você precisa criar outro DateTimeFormatter

        // no Joda-time é possível definir tudo de uma vez (formatos opcionais para parsing e outro separado para formatação)
        // Joda-Time: patterns opcionais para parsing
        DateTimeParser[] parsers = new DateTimeParser[] {
                // pattern opcional 1
                DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
                // pattern opcional 2
                DateTimeFormat.forPattern("dd/MM/yyyy").getParser() };
        // printer, usado para formatar a data
        DateTimePrinter printer = DateTimeFormat.forPattern("dd MM yyyy").getPrinter();
        jodaFormatter = new DateTimeFormatterBuilder()
            // usar o printer e os parsers acima
            .append(printer, parsers)
            .toFormatter();
        // faz o parse usando o array de parsers acima (tenta cada um deles até dar certo)
        LocalDate d1 = jodaFormatter.parseLocalDate("2018-02-01");
        LocalDate d2 = jodaFormatter.parseLocalDate("01/02/2018");
        // para formatar, usa o printer
        System.out.println(jodaFormatter.print(d1)); // 01 02 2018
        System.out.println(jodaFormatter.print(d2)); // 01 02 2018

        // ------------------------------ Formato ISO 8601
        // Joda-time tem a classe ISODateTimeFormat, com alguns parsers predefinidos
        // parser de data, com hora opcional
        DateTimeFormatter jodaParser = ISODateTimeFormat.dateOptionalTimeParser();
        // informar apenas a data (horário é setado para meia-noite e timezone default da JVM é usado para obter o offset)
        // repare no método parseDateTime, que retorna um objeto DateTime (há outros que retornam tipos específicos, como parseLocalDate, parseLocalTime, etc)
        DateTime dt = jodaParser.parseDateTime("2018-02-01");
        System.out.println(dt); // 2018-02-01T00:00:00.000-02:00
        // se informar o horário, ele é usado
        dt = jodaParser.parseDateTime("2018-02-01T10:30");
        System.out.println(dt); // 2018-02-01T10:30:00.000-02:00

        // o java.time é mais restrito e todos os campos que não estiverem presentes devem ter algum valor predefinido
        java.time.format.DateTimeFormatter javatimeParser = new java.time.format.DateTimeFormatterBuilder()
            // data (dia, mês e ano)
            .append(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            // hora opcional
            .optionalStart()
            .appendLiteral('T')
            .append(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)
            .optionalEnd()
            // valor predefinido para a hora
            .parseDefaulting(java.time.temporal.ChronoField.HOUR_OF_DAY, 0)
            // valor predefinido para o minuto
            .parseDefaulting(java.time.temporal.ChronoField.MINUTE_OF_HOUR, 0)
            // criar o formatter
            .toFormatter();
        // usar os métodos parse da classe LocalDateTime
        java.time.LocalDateTime dtSemHora = java.time.LocalDateTime.parse("2018-02-01", javatimeParser);
        System.out.println(dtSemHora); // 2018-02-01T00:00
        java.time.LocalDateTime dtComHora = java.time.LocalDateTime.parse("2018-02-01T10:30", javatimeParser);
        System.out.println(dtComHora); // 2018-02-01T10:30
    }

    static void comparacaoJavaTimeTests() {
        // Em testes é comum verificarmos condições que dependem da data/hora atual.

        // O java.time permite simularmos o valor que quisermos com um Clock
        // relogio que sempre retorna 1 de janeiro de 2018 à meia-noite em São Paulo
        ZonedDateTime z = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
        Clock clock = Clock.fixed(z.toInstant(), z.getZone()); // somente quem usar este Clock terá a data/hora atual fixa (com este valor)
        System.out.println(java.time.LocalDate.now(clock)); // 2018-01-01
        System.out.println(java.time.LocalDateTime.now(clock)); // 2018-01-01T00:00
        System.out.println(java.time.Instant.now(clock)); // 2018-01-01T02:00:00Z

        // O Joda-time usa uma classe estática que é usada internamente por todas as classes da API
        // data/hora atual vai ser 1 de janeiro de 2018 à meia-noite em São Paulo
        DateTime dt = new DateTime(2018, 1, 1, 0, 0, 0, 0, DateTimeZone.forID("America/Sao_Paulo"));
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis()); // data/hora atual será fixa (sempre retorna este valor)
        System.out.println(new LocalDate()); // 2018-01-01
        System.out.println(new DateTime()); // 2018-01-01T00:00:00.000-02:00

        // como é uma classe estática, é importante voltar a configuração "normal" depois (senão todas as aplicações rodando na mesma JVM serão afetadas)
        DateTimeUtils.setCurrentMillisSystem(); // usar relógio do sistema para obter data/hora atual
    }

    static void formatarPeriodos() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            // horas, com um sufixo para singular e outro para plural
            .appendHours()
            .appendSuffix(" hora", " horas")
            // espaço opcional, caso hajam mais campos
            .appendSeparatorIfFieldsBefore(" ")
            // minutos
            .appendMinutes()
            .appendSuffix(" minuto", " minutos")
            // espaço opcional, caso hajam mais campos
            .appendSeparatorIfFieldsBefore(" ")
            // segundos
            .appendSeconds()
            .appendSuffix(" segundo", " segundos")
            .toFormatter();
        // período de 10 horas, 20 minutos e 1 segundo
        Period period = new Period(10, 20, 1, 0);
        System.out.println(formatter.print(period)); // 10 horas 20 minutos 1 segundo

        // infelizmente, esta ideia não foi usada no java.time (onde a formatação de períodos deve ser feita manualmente)
    }

    /**
     * Entre 29/02/2000 e 28/02/2001 já se passou um ano? O Joda-Time entende que sim, enquanto o java.time entende que não.
     * 
     * Compare o resultado deste método com {@link exemplos.part3.CalculaIdadeTest#nascido29DeFevereiro()}
     */
    public static void calculaIdadeNascido29Fev() {
        // data de nascimento: 29 de fevereiro de 2000
        LocalDate dataNasc = new LocalDate(2000, 2, 29);

        // em 28 de fevereiro de 2001, a idade será 1 ano?
        // usar DateTimeUtils para simular a data/hora atual
        long fixedMillis = Instant.parse("2001-02-28T10:00-03:00").getMillis();
        DateTimeUtils.setCurrentMillisFixed(fixedMillis);

        int idade = Years.yearsBetween(dataNasc, new LocalDate()).getYears();
        // a API java.time entende que em 28/02/2001 já foi completado 1 ano
        System.out.println(idade); // 1

        // voltar a data/hora atual para o normal (usar relógio do sistema)
        DateTimeUtils.setCurrentMillisSystem();
    }

    // converões de/para java.util.Date e Calendar
    static void converterParaDate() {
        // java.util.Date
        Date date = new Date();
        // converter para org.joda.time.DateTime
        DateTime dateTime = new DateTime(date);
        // converter de volta para Date
        date = dateTime.toDate();
        // Atenção para o fato de que DateTime usa o instante representado por Date, mas também possui um timezone — no caso, será o timezone padrão da JVM.

        // java.util.Calendar
        Calendar calendar = Calendar.getInstance();
        // converter para org.joda.time.DateTime
        dateTime = new DateTime(calendar);
        // converter de volta para Calendar
        calendar = dateTime.toGregorianCalendar();
    }
}
