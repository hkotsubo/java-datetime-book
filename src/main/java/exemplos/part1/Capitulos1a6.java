package exemplos.part1;

import static exemplos.setup.Setup.clock;
import static exemplos.setup.Setup.setup;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.Years;

/**
 * A primeira parte do livro não possui código propriamente dito, mas fiz alguns códigos em Java para ilustrar os exemplos do livro.
 * 
 * Se não está familiarizado com a API java.time não se preocupe em entender os códigos agora, pois estas classes são explicadas na parte 3. Os códigos que
 * estão aqui servem apenas para demonstrar alguns conceitos citados no livro.
 * 
 * Como a primeira parte do livro é mais conceitual, não há tanto código associado, então esta classe cobre os capítulos 1 a 6.
 */
public class Capitulos1a6 {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) throws ParseException {
        queDiaEhHojeQueHorasSao();
        inicioHorarioDeVeraoSP();
        fimHorarioDeVeraoSP();
        samoaPulaUmDia();
        dadoOffsetQualTimezone();
        mesmoInstantEmVariosTimezones();
        duracaoNegativa();
        somaDataDuracao();
        periodoComSemanas();
        somar2DiasA30DeMaio();
        somar24HorasVs1Dia();
        somarUmMes();
        calcularIdadeLocalDate();
        calcularIdadeLocalDateTime();
        algunsTimezonesComOffset0200();
    }

    static void queDiaEhHojeQueHorasSao() {
        // 4 de Maio de 2018, 17h em São Paulo
        ZonedDateTime z = ZonedDateTime.of(2018, 5, 4, 17, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
        System.out.println(z);

        // converter para fuso horário da California
        System.out.println(z.withZoneSameInstant(ZoneId.of("America/Los_Angeles")));
        // converter para fuso horário do Japão
        System.out.println(z.withZoneSameInstant(ZoneId.of("Asia/Tokyo")));

        // obter o número gigante 1525464000000 (que será explicado posteriormente)
        System.out.println(z.toInstant().toEpochMilli());

        // converter para o calendário islâmico
        HijrahDate hd = HijrahDate.from(z);
        System.out.println(hd);

        // quantas vezes a data/hora "4 de Maio de 2018, as 17 horas" ocorre no mundo?
        LocalDateTime dt = LocalDateTime.of(2018, 5, 4, 17, 0);
        Set<Instant> set = new HashSet<>();
        // percorrer todos os timezones e adicionar o Instant correspondente no set
        ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).map(dt::atZone).forEach(zdt -> {
            set.add(zdt.toInstant());
        });
        System.out.println(set.size()); // 38 - o resultado pode variar, pois as informações de timezone mudam o tempo todo
        set.stream().sorted().forEach(System.out::println);
    }

    static void inicioHorarioDeVeraoSP() {
        // timezone de São Paulo
        ZoneId spZone = ZoneId.of("America/Sao_Paulo");
        // 14 de Outubro de 2017, 23:59h (um minuto antes de começar o horário de verão)
        ZonedDateTime umMinutoAntesHV = ZonedDateTime.of(2017, 10, 14, 23, 59, 0, 0, spZone);
        // 1 minuto depois, começa o horário de verão
        ZonedDateTime inicioHV = umMinutoAntesHV.plusMinutes(1);

        // imprimir as datas no fuso de SP e o instante correspondente em UTC
        System.out.println(umMinutoAntesHV + ", UTC=" + umMinutoAntesHV.toInstant());
        System.out.println(inicioHV + ", UTC=" + inicioHV.toInstant());

        /*
         * A saida será:
         * 
         * 2017-10-14T23:59-03:00[America/Sao_Paulo], UTC=2017-10-15T02:59:00Z
         * 
         * 2017-10-15T01:00-02:00[America/Sao_Paulo], UTC=2017-10-15T03:00:00Z
         * 
         * Repare que o horário de início foi ajustado automaticamente para 1 da manhã, pois à meia-noite o relógio é adiantado em 1 hora.
         * 
         * Porém os instantes em UTC são contínuos, graças a mudança de offset.
         * 
         */

        // comparando com o mesmo horário em Recife
        ZoneId recifeZone = ZoneId.of("America/Recife");
        ZonedDateTime recifeUmMinutoAntesHV = umMinutoAntesHV.withZoneSameInstant(recifeZone);
        ZonedDateTime recifeInicioHV = inicioHV.withZoneSameInstant(recifeZone);
        System.out.println(recifeUmMinutoAntesHV + ", UTC=" + recifeUmMinutoAntesHV.toInstant());
        System.out.println(recifeInicioHV + ", UTC=" + recifeInicioHV.toInstant());
        /*
         * A saída será:
         * 
         * 2017-10-14T23:59-03:00[America/Recife], UTC=2017-10-15T02:59:00Z
         * 
         * 2017-10-15T00:00-03:00[America/Recife], UTC=2017-10-15T03:00:00Z
         * 
         * Repare que o relógio não foi adiantado e o offset permaneceu o mesmo (-03:00), pois Recife não tem horário de verão, e os instantes UTC são os mesmos
         * de São Paulo
         */
    }

    static void fimHorarioDeVeraoSP() {
        // timezone de São Paulo
        ZoneId spZone = ZoneId.of("America/Sao_Paulo");
        // 17 de Fevereiro de 2018, 23h (um minuto antes de acabar o horário de verão)
        ZonedDateTime umMinutoAntesFimHV = ZonedDateTime.of(2018, 2, 17, 23, 59, 0, 0, spZone);
        // 1 hora depois, acaba o horário de verão
        ZonedDateTime fimHV = umMinutoAntesFimHV.plusMinutes(1);

        // imprimir as datas no fuso de SP e o instante correspondente em UTC
        System.out.println(umMinutoAntesFimHV + ", UTC=" + umMinutoAntesFimHV.toInstant());
        System.out.println(fimHV + ", UTC=" + fimHV.toInstant());

        /*
         * A saida será:
         * 
         * 2018-02-17T23:59-02:00[America/Sao_Paulo], UTC=2018-02-18T01:59:00Z
         * 
         * 2018-02-17T23:00-03:00[America/Sao_Paulo], UTC=2018-02-18T02:00:00Z
         * 
         * Repare que um minuto depois deveria resultar em meia-noite, porém como o horário de verão acabou, o relógio é atrasado em 1 hora e volta para 23h.
         * 
         * Porém os instantes em UTC são contínuos, graças a mudança de offset.
         * 
         */

        // comparando com o mesmo horário em Recife
        ZoneId recifeZone = ZoneId.of("America/Recife");
        ZonedDateTime recifeUmMinutoAntesFimHV = umMinutoAntesFimHV.withZoneSameInstant(recifeZone);
        ZonedDateTime recifeFimHV = fimHV.withZoneSameInstant(recifeZone);
        System.out.println(recifeUmMinutoAntesFimHV + ", UTC=" + recifeUmMinutoAntesFimHV.toInstant());
        System.out.println(recifeFimHV + ", UTC=" + recifeFimHV.toInstant());
        /*
         * A saída será:
         * 
         * 2018-02-17T22:59-03:00[America/Recife], UTC=2018-02-18T01:59:00Z
         * 
         * 2018-02-17T23:00-03:00[America/Recife], UTC=2018-02-18T02:00:00Z
         * 
         * Repare que o relógio não foi atrasado e o offset permaneceu o mesmo (-03:00), pois Recife não tem horário de verão, e os instantes UTC são os mesmos
         * de São Paulo
         */
    }

    static void samoaPulaUmDia() {
        // timezone de Samoa
        ZoneId zone = ZoneId.of("Pacific/Apia");

        // 29 de Dezembro de 2011, 23:59 (1 minuto antes da mudança para o outro lado da Linha Internacional de Data)
        ZonedDateTime antesMudanca = ZonedDateTime.of(2011, 12, 29, 23, 59, 0, 0, zone);
        System.out.println(antesMudanca); // 2011-12-29T23:59-10:00[Pacific/Apia]

        // 1 minuto depois, offset muda de -10 para +14 e a data local muda de 29 para 31
        // todo o dia 31 de Dezembro foi pulado localmente
        System.out.println(antesMudanca.plusMinutes(1)); // 2011-12-31T00:00+14:00[Pacific/Apia]

        // mostrar que os instantes UTC sao continuos
        System.out.println(antesMudanca.toInstant()); // 2011-12-30T09:59:00Z
        System.out.println(antesMudanca.plusMinutes(1).toInstant()); // 2011-12-30T10:00:00Z
    }

    /*
     * Usei a versão 2018e do TZBD (https://data.iana.org/time-zones/releases/tzdata2018e.tar.gz)
     * 
     * Se sua JVM está com uma versão diferente, pode ser que os resultados não sejam os mesmos.
     * 
     * Para verificar a versão, você pode usar ZoneRulesProvider.getVersions("America/Sao_Paulo"), que retorna um map cujas chaves são as versões usadas. No meu
     * caso, a chave é "2018e", confirmando que estou usando a versão 2018e.
     */
    static void dadoOffsetQualTimezone() {
        Instant i = Instant.now(clock());
        // procurar timezones que usam o offset +02:0
        ZoneOffset offset = ZoneOffset.ofHours(2);

        // procurar timezones para 4 de Maio de 2018, às 17 horas em São Paulo (total: 57)
        getZonesByOffset(i, offset);

        // procurar timezones para 1 de Outubro de 2018, às 17 horas em São Paulo (total: 44)
        getZonesByOffset(i.plus(Duration.ofDays(180)), offset);
    }

    // dado um Instant e um offset, verifica quais os timezones que usam este offset naquele instante
    static void getZonesByOffset(Instant i, ZoneOffset offset) {
        Set<String> set = new HashSet<>();
        ZoneId.getAvailableZoneIds().forEach(s -> {
            if (offset.equals(i.atZone(ZoneId.of(s)).getOffset())) {
                set.add(s);
            }
        });
        System.out.println("\n********\n" + i + "(" + i.atZone(ZoneId.of("America/Sao_Paulo")) + ")" + " with " + offset);
        System.out.println(set.size() + ": " + set.stream().sorted().collect(Collectors.joining(", ")));
    }

    static void mesmoInstantEmVariosTimezones() {
        // Instant representa um timestamp
        Instant instant = clock().instant();

        // o mesmo timestamp corresponde a uma data e hora diferentes, dependendo do timezone
        System.out.println(instant.atZone(ZoneId.of("America/Sao_Paulo")));
        System.out.println(instant.atZone(ZoneId.of("Europe/Berlin")));
        System.out.println(instant.atZone(ZoneId.of("Asia/Tokyo")));
        System.out.println(instant.atZone(ZoneId.of("Pacific/Honolulu")));
        /*
         * Saída:
         * 
         * 2018-05-04T17:00-03:00[America/Sao_Paulo]
         * 
         * 2018-05-04T22:00+02:00[Europe/Berlin]
         * 
         * 2018-05-05T05:00+09:00[Asia/Tokyo]
         * 
         * 2018-05-04T10:00-10:00[Pacific/Honolulu]
         */
    }

    static void duracaoNegativa() {
        // -1 hora e +3 minutos, o equivalente a -57 minutos
        System.out.println(Duration.parse("PT-1H3M").getSeconds());
        System.out.println(Duration.parse("PT57M").getSeconds());
    }

    static void somaDataDuracao() {
        // 4 de Maio de 2018 somada a uma duração de 2 dias resulta em 6 de Maio de 2018
        LocalDate quatroDeMaio2018 = LocalDate.of(2018, 5, 4);
        Period doisDias = Period.parse("P2D");
        LocalDate seisDeMaio2018 = quatroDeMaio2018.plus(doisDias);
        System.out.println(seisDeMaio2018); // 2018-05-06

        // Entre 4 de Maio de 2018 e 6 de Maio de 2018, há uma diferença (ou uma duração) de 2 dias
        Period diff = Period.between(quatroDeMaio2018, seisDeMaio2018);
        System.out.println(diff); // P2D
        System.out.println(diff.getDays()); // 2
    }

    /*
     * Períodos com semanas (W) misturadas com outras unidades (como dias, meses, etc) não são permitidas pela ISO 8601
     * 
     * Porém, algumas APIs aceitam mesmo assim. O teste abaixo mostra como diferentes APIs tratam P1W1D (1 semana e 1 dia)
     */
    static void periodoComSemanas() {
        // java.time, ajusta para 8 dias
        System.out.println(Period.parse("P1W1D")); // P8D

        // Joda-Time, mantém as semanas separadas dos dias
        System.out.println(org.joda.time.Period.parse("P1W1D")); // P1W1D
    }

    static void somar2DiasA30DeMaio() {
        // 30 de maio de 2018
        LocalDate dt = LocalDate.of(2018, 5, 30);
        // somar 2 dias
        System.out.println(dt.plusDays(2)); // 2018-06-01 (1 de Junho de 2018)
    }

    static void somar24HorasVs1Dia() {
        // 4 de Maio de 2018, às 5 da tarde
        LocalDateTime dt = LocalDateTime.parse("2018-05-04T17:00");

        // somar 24 horas
        System.out.println(dt.plusHours(24)); // 2018-05-05T17:00
        // somar 1 dia
        System.out.println(dt.plusDays(1)); // 2018-05-05T17:00

        // timezone de São Paulo
        ZoneId spZone = ZoneId.of("America/Sao_Paulo");
        // 14 de Outubro de 2017, 10h (um dia antes de começar o horário de verão)
        ZonedDateTime antesHv = ZonedDateTime.of(2017, 10, 14, 10, 0, 0, 0, spZone);
        System.out.println(antesHv); // 2017-10-14T10:00-03:00[America/Sao_Paulo]
        // o mesmo dia, em UTC
        System.out.println(antesHv.toInstant()); // 2017-10-14T13:00:00Z

        // somar 1 dia
        System.out.println(antesHv.plusDays(1)); // 2017-10-15T10:00-02:00[America/Sao_Paulo]
        // em UTC
        System.out.println(antesHv.plusDays(1).toInstant()); // 2017-10-15T12:00:00Z

        // somar 24 horas
        System.out.println(antesHv.plusHours(24)); // 2017-10-15T11:00-02:00[America/Sao_Paulo]
        // em UTC
        System.out.println(antesHv.plusHours(24).toInstant()); // 2017-10-15T13:00:00Z

        // 17 de Fevereiro de 2018, 10h (um dia antes de acabar o horário de verão)
        ZonedDateTime antesFimHV = ZonedDateTime.of(2018, 2, 17, 10, 0, 0, 0, spZone);
        System.out.println(antesFimHV); // 2018-02-17T10:00-02:00[America/Sao_Paulo]
        // o mesmo dia, em UTC
        System.out.println(antesFimHV.toInstant()); // 2018-02-17T12:00:00Z

        // somar 1 dia
        System.out.println(antesFimHV.plusDays(1)); // 2018-02-18T10:00-03:00[America/Sao_Paulo]
        // em UTC
        System.out.println(antesFimHV.plusDays(1).toInstant()); // 2018-02-18T13:00:00Z

        // somar 24 horas
        System.out.println(antesFimHV.plusHours(24)); // 2018-02-18T09:00-03:00[America/Sao_Paulo]
        // em UTC
        System.out.println(antesFimHV.plusHours(24).toInstant()); // 2018-02-18T12:00:00Z
    }

    static void somarUmMes() {
        // 1 de Janeiro de 2018
        LocalDate jan = LocalDate.of(2018, 1, 1);

        // somar 1 mês = 1 de Fevereiro de 2018
        LocalDate fev = jan.plusMonths(1);
        System.out.println(fev); // 2018-02-01

        // diferença em dias
        long dias = ChronoUnit.DAYS.between(jan, fev);
        System.out.println(dias); // 31

        // somar 1 mês = 1 de Março de 2018
        LocalDate mar = fev.plusMonths(1);
        System.out.println(mar); // 2018-03-01

        // diferença em dias
        dias = ChronoUnit.DAYS.between(fev, mar);
        System.out.println(dias); // 28
    }

    public DateTimeFormatter criaFormatter(boolean incluirHorasOpcional, boolean incluirFracaoSegundos) {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        // usar formatador predefinido para data (yyyy-MM-dd)
        builder.append(DateTimeFormatter.ISO_LOCAL_DATE);

        if (incluirHorasOpcional) {
            // seção opcional com as horas
            builder.optionalStart().appendPattern(" HH:mm:ss");
            if (incluirFracaoSegundos) {
                builder.appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true);
            }
            // encerrar seção opcional
            builder.optionalEnd();
        }
        // criar o DateTimeFormatter
        return builder.toFormatter();
    }

    static Temporal ajusta(Temporal temporal) {
        return temporal
            // 3 meses no futuro
            .plus(3, ChronoUnit.MONTHS)
            // no dia 1
            .with(ChronoField.DAY_OF_MONTH, 1);
    }

    static void calcularIdadeLocalDate() {
        // data de nascimento: 1 de Janeiro de 2000
        LocalDate dataNascimento = LocalDate.of(2000, 1, 1);

        // no dia 31 de Dezembro de 2006, a idade é 6 anos
        long idade = ChronoUnit.YEARS.between(dataNascimento, LocalDate.of(2006, 12, 31));
        System.out.println(idade); // 6

        // somente em 1 de Janeiro de 2007, a idade é 7 anos
        idade = ChronoUnit.YEARS.between(dataNascimento, LocalDate.of(2007, 1, 1));
        System.out.println(idade); // 7

        // *********************************
        // data de nascimento: 29 de Fevereiro de 2000
        dataNascimento = LocalDate.of(2000, 2, 29);

        // em 28 de Fevereiro de 2007, não completou 7 anos
        idade = ChronoUnit.YEARS.between(dataNascimento, LocalDate.of(2007, 2, 28));
        System.out.println(idade); // 6

        // em 1 de Março de 2007, completou 7 anos
        idade = ChronoUnit.YEARS.between(dataNascimento, LocalDate.of(2007, 3, 1));
        System.out.println(idade); // 7

        // em 29 de Fevereiro de 2008, completou 8 anos
        idade = ChronoUnit.YEARS.between(dataNascimento, LocalDate.of(2008, 2, 29));
        System.out.println(idade); // 8

        // ***************************** teste com Joda-Time
        org.joda.time.LocalDate dtnasc = new org.joda.time.LocalDate(2000, 2, 29);
        // Joda-Time considera que em 28 de Fevereiro de 2007, completou 7 anos (diferente do java.time, que considera 6)
        System.out.println(Years.yearsBetween(dtnasc, new org.joda.time.LocalDate(2007, 2, 28)).getYears()); // 7
        System.out.println(Years.yearsBetween(dtnasc, new org.joda.time.LocalDate(2008, 2, 29)).getYears()); // 8
    }

    static void calcularIdadeLocalDateTime() {
        // data de nascimento: 1 de Janeiro de 2000, às 10h
        LocalDateTime dataNascimento = LocalDateTime.of(2000, 1, 1, 10, 0);

        // em 1 de Janeiro de 2007, às 09:59, ainda não completou 7 anos
        long idade = ChronoUnit.YEARS.between(dataNascimento, LocalDateTime.of(2007, 1, 1, 9, 59));
        System.out.println(idade); // 6

        // somente em 1 de Janeiro de 2007, às 10h, completa 7 anos
        idade = ChronoUnit.YEARS.between(dataNascimento, LocalDateTime.of(2007, 1, 1, 10, 0));
        System.out.println(idade); // 7
    }

    static void algunsTimezonesComOffset0200() {
        // 4 de Maio de 2018, às 17:00 em São Paulo
        ZonedDateTime maio = ZonedDateTime.now(clock());
        // 31 de Outubro de 2018, às 17:00 em São Paulo
        ZonedDateTime outubro = ZonedDateTime.of(2018, 10, 31, 17, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
        // 10 de Agosto de 2014, às 17:00 em São Paulo
        ZonedDateTime agosto2014 = ZonedDateTime.of(2014, 8, 10, 17, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));

        List<ZonedDateTime> datas = Arrays.asList(agosto2014, maio, outubro);

        Stream.of("Africa/Ceuta", "Asia/Amman", "Africa/Cairo").map(ZoneId::of).forEach(zone -> {
            System.out.println("\n" + zone);
            datas.forEach(zdt -> {
                System.out.println("Em " + zdt + ", o offset é " + zdt.withZoneSameInstant(zone).getOffset() + " - DST="
                        + zone.getRules().isDaylightSavings(zdt.toInstant()));
            });
        });
    }
}
