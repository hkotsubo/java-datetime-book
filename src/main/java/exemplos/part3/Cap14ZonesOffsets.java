package exemplos.part3;

import static exemplos.setup.Setup.clock;
import static exemplos.setup.Setup.setup;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import exemplos.setup.Setup;

public class Cap14ZonesOffsets {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) {
        criarZonedDateTime();
        timezonePadrao();
        criarDatasEmOutroTimezone();
        timezones();
        gaps();
        overlaps();
        usandoZonedDateTime();
        comparacao();
        converterParaOutroTimezone();
        combinarComLocalDateTime();
        combinarComLocalDate();
        offsetDateTime();

        // --------------------------------------------
        // exemplos que não estão no livro
        zoneIdVsZoneOffset();
        zoneIdVsZoneOffset2();
        testOffsetTime();
    }

    static void criarZonedDateTime() {
        // data/hora atual
        // usando a data/hora "atual" simulada
        ZonedDateTime agora = ZonedDateTime.now(clock());
        System.out.println(agora); // 2018-05-04T17:00-03:00[America/Sao_Paulo]
        // now() sem parâmetros usa o relógio do sistema e o timezone padrão da JVM
        // ZonedDateTime agora = ZonedDateTime.now();

        // data/hora atual em outro timezone
        // agora = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        // usando a data/hora "atual" simulada
        agora = ZonedDateTime.now(clock().withZone(ZoneId.of("Asia/Tokyo")));
        System.out.println(agora); // 2018-05-05T05:00+09:00[Asia/Tokyo]
    }

    static void timezonePadrao() {
        // ZoneId.systemDefault() sempre usa o timezone padrão que estiver configurado no momento em que ele é chamado
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles")); // America/Los_Angeles
        System.out.println(ZoneId.systemDefault());

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
        System.out.println(ZoneId.systemDefault()); // Europe/London

        // voltar config default (para não impactar os outros métodos)
        setup();
    }

    static void criarDatasEmOutroTimezone() {
        // LocalDate não possui timezone, mas usa o timezone para saber qual é o dia, mês e ano atual

        // usando a data/hora "atual" simulada
        // data de hoje no timezone America/Sao_Paulo: 2018-05-04
        LocalDate hojeSP = LocalDate.now(clock().withZone(ZoneId.of("America/Sao_Paulo")));
        // data de hoje no timezone Asia/Tokyo: 2018-05-05
        LocalDate hojeTokyo = LocalDate.now(clock().withZone(ZoneId.of("Asia/Tokyo")));
        // passando o ZoneId, o relógio do sistema é usado
        // LocalDate hojeSP = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        // LocalDate hojeTokyo = LocalDate.now(ZoneId.of("Asia/Tokyo"));

        System.out.println(hojeSP); // 2018-05-04
        System.out.println(hojeTokyo); // 2018-05-05

        // usando a data/hora "atual" simulada
        System.out.println(LocalTime.now(clock().withZone(ZoneId.of("Asia/Tokyo")))); // 05:00
        System.out.println(LocalDateTime.now(clock().withZone(ZoneId.of("Asia/Tokyo")))); // 2018-05-05T05:00
        // passar somente ZoneId para usar o relógio do sistema
        // System.out.println(LocalTime.now(ZoneId.of("Asia/Tokyo")));
        // System.out.println(LocalDateTime.now(ZoneId.of("Asia/Tokyo")));
    }

    static void timezones() {
        // ID inválido, retorna UTC
        TimeZone timeZone = TimeZone.getTimeZone("Id invalido");
        System.out.println(timeZone.getID()); // GMT
        try {
            // ID inválido, lança exceção
            ZoneId.of("Id invalido");
        } catch (DateTimeException e) {
            System.out.println(e.getMessage()); // Invalid ID for region-based ZoneId, invalid format: Id invalido
        }

        // -------------------------------------
        // IST é uma abreviação de timezone usada na Irlanda, Índia e Israel, qual desses TimeZone retorna?
        System.out.println(TimeZone.getTimeZone("IST"));
        // sun.util.calendar.ZoneInfo[id="IST",offset=19800000,dstSavings=0,useDaylight=false,transitions=7,lastRule=null]
        // o retorno é um timezone que usa o offset da Índia, mas o nome é a própria abreviação (que não é um timezone de fato)
        // A Índia usa o timezone Asia/Kolkata

        try {
            // ZoneId não aceita abreviações, justamente por serem ambíguas
            ZoneId.of("IST");
        } catch (DateTimeException e) {
            System.out.println(e.getMessage()); // Unknown time-zone ID: IST
        }

        // mas é possível escolher qual timezone retornar quando for usada uma abreviação
        // minhas escolhas arbitrárias para as abreviações
        Map<String, String> abreviacoes = new HashMap<>();
        // EST é mapeado para New York
        abreviacoes.put("EST", "America/New_York");
        // IST é mapeado para Índia
        abreviacoes.put("IST", "Asia/Kolkata");
        // ... coloque no Map quantos valores você precisar
        // usar o map para obter o timezone - Asia/Kolkata
        ZoneId zone = ZoneId.of("IST", abreviacoes);
        System.out.println(zone); // Asia/Kolkata

        // retorna o próprio timezone America/Sao_Paulo, pois este valor não está no map
        System.out.println(ZoneId.of("America/Sao_Paulo", abreviacoes)); // America/Sao_Paulo

        try {
            // abreviação BRT não está no map, por isso é inválida e lança exceção
            ZoneId.of("BRT", abreviacoes);
        } catch (DateTimeException e) {
            System.out.println(e.getMessage()); // Unknown time-zone ID: BRT
        }
    }

    /**
     * Em São Paulo, 15 de outubo de 2017 começa o horário de verão: à meia-noite, o relógio é adiantado em 1 hora, diretamente para 01:00
     * 
     * Ou seja, todos os minutos entre 00:00 e 00:59 são pulados e não existem neste dia (isso é chamado de "gap").
     * 
     * Por isso criar esta data e hora neste timezone causa um ajuste para o próximo horário válido.
     * 
     * O mesmo acontece para outros timezones, quando tentamos criar uma data e hora que "não existe" devido à mudança de horário
     */
    static void gaps() {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        // 15 de outubo de 2017, meia-noite, horário de verão começa em São Paulo
        ZonedDateTime z = ZonedDateTime.of(2017, 10, 15, 0, 0, 0, 0, zone);
        // horário é ajustado para 01:00
        System.out.println(z); // 2017-10-15T01:00-02:00[America/Sao_Paulo]

        // nem sempre o ajuste é de uma hora
        zone = ZoneId.of("Australia/Lord_Howe");
        // neste timezone, o horário de verão adianta o relógio em meia hora
        z = ZonedDateTime.of(2018, 10, 7, 2, 10, 0, 0, zone);
        // 02:10 é ajustado para meia hora depois (02:40)
        System.out.println(z); // 2018-10-07T02:40+11:00[Australia/Lord_Howe]
    }

    /**
     * Em São Paulo, 17 de fevereiro de 2018 termina o horário de verão: à meia-noite, o relógio é atrasado em 1 hora, de volta para 23:00
     * 
     * Ou seja, todos os minutos entre 23:00 e 23:59 existem duas vezes neste dia (uma no horário de verão, outra no horário "normal" - isso é chamado de
     * "overlap").
     * 
     * Por isso criar esta data e hora neste timezone é ambíguo, pois pode se referir à primeira ou segunda ocorrência.
     */
    static void overlaps() {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        // 17 de fevereiro de 2018, às 23:00
        ZonedDateTime zdt = ZonedDateTime.of(2018, 2, 17, 23, 0, 0, 0, zone);
        // por padrão, é usada a primeira ocorrência das 23:00 (em horário de verão, por isso o offset é -02:00)
        System.out.println(zdt); // 2018-02-17T23:00-02:00[America/Sao_Paulo]

        // é possível obter a segunda ocorrência com withLaterOffsetAtOverlap
        // depois de terminar o horário de verão (o offset agora é -03:00)
        ZonedDateTime depoisHv = zdt.withLaterOffsetAtOverlap();
        System.out.println(depoisHv); // 2018-02-17T23:00-03:00[America/Sao_Paulo]

        // obter a primeira ocorrência
        zdt = depoisHv.withEarlierOffsetAtOverlap();
        System.out.println(zdt); // 2018-02-17T23:00-02:00[America/Sao_Paulo]

        // os métodos withXXXAtOverlap são mais seguros do que simplesmente somar 1 hora, pois nem sempre o overlap é de 1 hora (veja o caso do gap no timezone
        // Australia/Lord_Howe, por exemplo: neste timezone, o overlap também é de meia hora)
    }

    static void usandoZonedDateTime() {
        // usando a data/hora "atual" simulada
        // 2018-05-04T17:00-03:00[America/Sao_Paulo]
        ZonedDateTime dataHoraSp = ZonedDateTime.now(clock());
        // now() sem parâmetros usa o relógio do sistema e o timezone padrão da JVM
        // ZonedDateTime dataHoraSp = ZonedDateTime.now();
        System.out.println("antes :" + dataHoraSp); // antes :2018-05-04T17:00-03:00[America/Sao_Paulo]

        // toda vez que um campo é mudado, o offset é verificado e pode mudar
        dataHoraSp = dataHoraSp
            // mudar a data para 12 de janeiro de 2017 (está em horário de verão, offest muda para -02:00)
            .with(LocalDate.of(2017, 1, 12))
            // mudar a hora para meio-dia (continua em horário de verão, offest permanece -02:00)
            .with(LocalTime.NOON);
        System.out.println("depois:" + dataHoraSp); // depois:2017-01-12T12:00-02:00[America/Sao_Paulo]

        // obter o timezone
        ZoneId zone = dataHoraSp.getZone();
        System.out.println(zone); // America/Sao_Paulo
        // obter o offset
        ZoneOffset offset = dataHoraSp.getOffset();
        System.out.println(offset); // -02:00
    }

    static void comparacao() {
        // 2018-05-04T17:00-03:00[America/Sao_Paulo]
        ZoneId spZone = ZoneId.of("America/Sao_Paulo");
        ZonedDateTime dataSP = ZonedDateTime.of(2018, 5, 4, 17, 0, 0, 0, spZone);
        // 2018-05-05T05:00+09:00[Asia/Tokyo]
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        ZonedDateTime dataTokyo = ZonedDateTime.of(2018, 5, 5, 5, 0, 0, 0, tokyoZone);

        // false - valores numéricos da data/hora/offset e/ou timezone diferentes
        System.out.println(dataSP.equals(dataTokyo)); // false
        // true - correspondem ao mesmo instante: em UTC, ambos correspondem a 2018-05-04T20:00:00Z
        System.out.println(dataSP.isEqual(dataTokyo)); // true

        // ----------------------------------
        // 2018-05-04T17:00-03:00[America/Sao_Paulo]
        dataSP = ZonedDateTime.of(2018, 5, 4, 17, 0, 0, 0, spZone);
        // 2018-05-04T23:00+09:00[Asia/Tokyo]
        dataTokyo = ZonedDateTime.of(2018, 5, 4, 23, 0, 0, 0, tokyoZone);
        System.out.println(dataSP.isBefore(dataTokyo)); // false
        System.out.println(dataSP.isAfter(dataTokyo)); // true
    }

    static void converterParaOutroTimezone() {
        // 2018-05-04T17:00-03:00[America/Sao_Paulo]
        ZoneId spZone = ZoneId.of("America/Sao_Paulo");
        ZonedDateTime dataSP = ZonedDateTime.of(2018, 5, 4, 17, 0, 0, 0, spZone);

        // converter para Asia/Tokyo
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        ZonedDateTime dataTokyo = dataSP.withZoneSameInstant(tokyoZone);
        System.out.println(dataTokyo); // 2018-05-05T05:00+09:00[Asia/Tokyo]
        // dataTokyo corresponde ao mesmo instante de dataSP
        System.out.println(dataTokyo.isEqual(dataSP)); // true
        // mas os valores dos campos (dia, mês, ano, hora, offset, etc) não são iguais
        System.out.println(dataTokyo.equals(dataSP)); // false

        // -------------------------------------------------------
        // manter o mesmo dia e hora, e só mudar o timezone (o offset é ajustado automaticamente)
        dataTokyo = dataSP.withZoneSameLocal(tokyoZone);
        System.out.println(dataTokyo); // 2018-05-04T17:00+09:00[Asia/Tokyo]
        // dataTokyo não corresponde ao mesmo instante de dataSP
        System.out.println(dataTokyo.isEqual(dataSP)); // false
        // os valores da data e hora são os mesmos, mas o offset e timezone não
        System.out.println(dataTokyo.equals(dataSP)); // false
    }

    static void combinarComLocalDateTime() {
        // 2018-05-04T17:00
        LocalDateTime dataHora = LocalDateTime.of(2018, 5, 4, 17, 0);
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        // as duas formas abaixo são equivalentes
        ZonedDateTime dataHoraSP = dataHora.atZone(zone);
        dataHoraSP = ZonedDateTime.of(dataHora, zone);
        System.out.println(dataHoraSP); // 2018-05-04T17:00-03:00[America/Sao_Paulo]

        // -----------------------------------------------------------------
        // efeitos de gaps e overlaps também podem acontecer
        dataHora = LocalDateTime.of(2017, 10, 15, 0, 0);
        // dataHora corresponde a um gap do timezone America/Sao_Paulo
        // (quando o horário de verão começa e à meia-noite o relógio é automaticamente adiantado para 01:00)
        dataHoraSP = dataHora.atZone(zone);
        // o horário é automaticamente ajustado para 01:00
        System.out.println(dataHoraSP); // 2017-10-15T01:00-02:00[America/Sao_Paulo]
        // o mesmo vale para overlaps (ver os respectivos métodos gaps() e overlaps() para mais exemplos)
    }

    static void combinarComLocalDate() {
        // 2018-05-04
        LocalDate data = LocalDate.of(2018, 5, 4);
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        // setar horário para 10:00 e juntar com o ZoneId
        ZonedDateTime dataHoraSP = data.atTime(10, 0).atZone(zone);
        // offset é calculado automaticamente, com os devidos ajustes em casos de gap e overlap
        // (ver os respectivos métodos gaps() e overlaps() para mais exemplos)
        System.out.println(dataHoraSP); // 2018-05-04T10:00-03:00[America/Sao_Paulo]

        // ----------------------------------------------------------------
        // outra maneira: usando um LocalTime para o horário
        // 2018-05-04
        data = LocalDate.of(2018, 5, 4);
        // 10:00
        LocalTime hora = LocalTime.of(10, 0);
        // juntar data, hora e timezone
        dataHoraSP = ZonedDateTime.of(data, hora, zone);
        System.out.println(dataHoraSP); // 2018-05-04T10:00-03:00[America/Sao_Paulo]

        // ----------------------------------------------------------------
        // atalho para meia-noite (início do dia)
        // sem ZoneId, retorna LocalDateTime: 2018-05-04T00:00
        LocalDateTime inicioDoDia = data.atStartOfDay();
        System.out.println(inicioDoDia); // 2018-05-04T00:00
        // com ZoneId, retorna ZonedDateTime: 2018-05-04T00:00-03:00[America/Sao_Paulo]
        ZonedDateTime inicioDoDiaSP = data.atStartOfDay(zone);
        System.out.println(inicioDoDiaSP); // 2018-05-04T00:00-03:00[America/Sao_Paulo]

        // atenção para os gaps
        // 2017-10-15 - dia que começa o horário de verão em São Paulo
        data = LocalDate.of(2017, 10, 15);
        inicioDoDiaSP = data.atStartOfDay(zone);
        // horário ajustado para 01:00
        System.out.println(inicioDoDiaSP); // 2017-10-15T01:00-02:00[America/Sao_Paulo]
    }

    static void offsetDateTime() {
        // data/hora atual
        // usando a data/hora "atual" simulada
        ZonedDateTime agora = ZonedDateTime.now(clock());
        // obter OffsetDateTime (data, hora e offset, sem o timezone)
        OffsetDateTime odt = agora.toOffsetDateTime();
        System.out.println(agora); // 2018-05-04T17:00-03:00[America/Sao_Paulo]
        System.out.println(odt); // 2018-05-04T17:00-03:00

        // ------------------------------------------------
        // A partir do OffsetDateTime, não tem como obter o ZonedDateTime original. Há vários timezones que usam o mesmo offset na mesma data e hora, então o
        // melhor que podemos fazer é obter uma lista de timezones e escolher um arbitrariamente

        // verificar todos os timezones disponíveis
        ZoneId.getAvailableZoneIds().forEach(zoneName -> {
            // obter o offset usado neste timezone, neste mesmo instante
            ZoneId zone = ZoneId.of(zoneName);
            // atZoneSameInstant obtém o ZonedDateTime que corresponde ao mesmo instante do OffsetDateTime
            // ou seja, ambos correspondem ao mesmo instante UTC (mas em cada timezone, um offset diferente pode ser usado)
            ZoneOffset offset = odt.atZoneSameInstant(zone).getOffset();

            // verifica se os offsets são iguais
            if (offset.equals(odt.getOffset())) {
                System.out.println(zoneName); // serão impressos vários timezones (vai variar de acordo com a versão do TZDB da sua JVM)
            }
        });
    }

    // ************************************************************
    // exemplos que não estão no livro
    static void zoneIdVsZoneOffset() {
        // ZoneOffset é uma subclasse de ZoneId
        ZoneOffset offset = ZoneOffset.of("+03:00");
        System.out.println(offset instanceof ZoneId); // true

        // now recebe um ZoneId, mas podemos passar um ZoneOffset
        ZonedDateTime now = ZonedDateTime.now(offset);
        // usando o Clock para termos a nossa data atual simulada (que também recebe um ZoneId, mas podemos passar o ZoneOffset)
        now = ZonedDateTime.now(Setup.clock().withZone(offset));
        // o resultado é o instante atual (4 de Maio de 2018, às 17:00 em São Paulo), convertido para o offset +03:00
        System.out.println(now); // 2018-05-04T23:00+03:00 <- repare que agora o ZonedDateTime não tem um timezone, somente um offset

        // nesse caso, o ZonedDateTime se comporta como um OffsetDateTime (tem somente um offset, sem referência a um timezone)
        System.out.println(now.getZone() == now.getOffset()); // true
        System.out.println(now.getZone()); // +03:00
        System.out.println(now.getZone().getClass()); // class java.time.ZoneOffset
        /*
         * Resumindo, quando o ZonedDateTime é criado com um ZoneOffset, ele se comporta como um OffsetDateTime (sempre tem o mesmo offset).
         * 
         * Quando recebe um ZoneId com identificador da IANA (nomes de timezone, como America/Sao_Paulo), ele passa a considerar o timezone, isto é, passa a
         * verificar o histórico de offsets daquele timezone, ajustando-o conforme a data e hora.
         */

        // ---------------------------------------------------------------------------
        // ZoneId.of pode retornar um ZoneOffset, dependendo da String usada
        // usando identificador da IANA
        System.out.println(ZoneId.of("America/Sao_Paulo").getClass()); // class java.time.ZoneRegion
        // ZoneRegion é outra subclasse de ZoneId, mas não usamos diretamente porque não é pública
        // usando offset
        System.out.println(ZoneId.of("+03:00").getClass()); // class java.time.ZoneOffset

        // se for usar UTC (offset "Z"), use a constante ZoneOffset.UTC diretamente
        System.out.println(ZoneId.of("Z") == ZoneOffset.UTC); // true

        // ---------------------------------------------------------------------------
        // ZoneOffset.of só aceita offsets
        System.out.println(ZoneOffset.of("+03:00")); // +03:00
        try {
            // identificador da IANA lança exceção
            ZoneOffset.of("America/Sao_Paulo");
        } catch (DateTimeException e) {
            System.out.println(e.getMessage()); // Invalid ID for ZoneOffset, invalid format: America/Sao_Paulo
        }

        // ---------------------------------------------------------------------------
        // É possível obter o timezone padrão usando ZoneId.systemDefault();
        // Mas será que é possível obter o "offset padrão"?
        // o código abaixo não compila (Type mismatch: cannot convert from ZoneId to ZoneOffset)
        // ZoneOffset padraoJVM = ZoneOffset.systemDefault();
        // Isso acontece porque a classe ZoneOffset não possui o método systemDefault(). O código ZoneOffset.systemDefault() está na verdade chamando o
        // método da superclasse (ZoneId). E de qualquer forma, o retorno é um ZoneRegion:
        ZoneId padraoJVM = ZoneOffset.systemDefault();
        System.out.println(padraoJVM.getClass()); // class java.time.ZoneRegion
        System.out.println(padraoJVM); // America/Sao_Paulo (ou outro valor, depedendo da configuração da sua JVM)

        // A classe ZoneOffset não possui um método systemDefault() porque não faz sentido ter um "offset padrão da JVM".
        // O que existe é um timezone padrão, que possui uma lista de offsets usados ao longo da história.
        // Como o offset pode variar com o tempo, não há como decidir qual é o valor padrão.
        // O que você pode fazer então é, dado o timezone padrão, descobrir qual é o offset usado na data e hora atual:

        // offset usado atualmente pelo timezone padrão
        ZoneOffset offsetAtual = OffsetDateTime.now().getOffset();
        System.out.println(offsetAtual); // o valor vai depender do timezone padrão configurado na JVM
        // Internamente, OffsetDateTime.now() usa o timezone padrão da JVM para calcular o offset usado atualmente.
        // E getOffset() retorna o ZoneOffset correspondente.
    }

    static void zoneIdVsZoneOffset2() {
        // Não use ZoneId quando precisar de ZoneOffset e vice-versa
        // 4 de maio de 2018, 17:00
        LocalDateTime dateTime = LocalDateTime.of(2018, 5, 4, 17, 0);
        // usando um ZoneId (apesar do valor ser um offset)
        ZoneId offset = ZoneId.of("-03:00");
        // a linha abaixo não compila
        // The method atOffset(ZoneOffset) in the type LocalDateTime is not applicable for the arguments (ZoneId)
        // OffsetDateTime odt = dateTime.atOffset(offset);
        // Neste caso específico, como sabemos que ZoneId.of("-03:00") retorna um ZoneOffset, podemos fazer o cast da variável:
        OffsetDateTime odt = dateTime.atOffset((ZoneOffset) offset);
        System.out.println(odt); // 2018-05-04T17:00-03:00

        // ---------------------------------------------------------------------------
        // Mas se o ZoneId fosse um timezone (ou seja, um ZoneRegion), o código daria um erro durante a execução. Por exemplo:
        // identificador da IANA, retorno é um ZoneRegion
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        try {
            // cast dá erro ao executar, pois zoneId foi criado com identificador da IANA, portanto não é um ZoneOffset
            odt = dateTime.atOffset((ZoneOffset) zoneId);
        } catch (ClassCastException e) {
            System.out.println(e.getMessage()); // java.time.ZoneRegion cannot be cast to java.time.ZoneOffset
        }

        // ---------------------------------------------------------------------------
        // usando atZone (retorna um ZonedDateTime)
        ZonedDateTime zdt = dateTime.atZone(ZoneId.of("-03:00"));
        System.out.println(zdt); // 2018-05-04T17:00-03:00
        // ZoneId.of("-03:00") retorna um ZoneOffset (conforme visto em zoneIdVsZoneOffset())
        // O resultado é um ZonedDateTime que só tem offset (sem timezone)
        // É o mesmo efeito de criar um ZonedDateTime com um ZoneOffset, visto em zoneIdVsZoneOffset()
    }

    static void testOffsetTime() {
        // A classe java.time.OffsetTime não foi abordada no livro, por ser menos usada e funcionar de maneira bem similar às demais
        // Basicamente, ela é um OffsetDateTime sem a data (possui apenas hora e offset)

        // hora e offset atual, no timezone padrão da JVM (usando nossa data/hora atual simulada)
        OffsetTime agora = OffsetTime.now(Setup.clock());
        System.out.println(agora); // 17:00-03:00

        // hora atual em UTC
        OffsetTime agoraUtc = OffsetTime.now(ZoneOffset.UTC);
        // usando data/hora atual simulada
        agoraUtc = OffsetTime.now(Setup.clock().withZone(ZoneOffset.UTC));
        System.out.println(agoraUtc); // 20:00Z

        // criando uma hora específica
        OffsetTime ot = OffsetTime.of(15, 30, 40, 123456789, ZoneOffset.of("+07:30"));
        System.out.println(ot); // 15:30:40.123456789+07:30

        // ---------------------------------------------------------------------------
        // getters e métodos withXXX para retornar outra instância com o valor modificado:
        // mudar hora para 10 -> 10:00-03:00
        OffsetTime modificado = agora.withHour(10);
        System.out.println(modificado); // 10:00-03:00
        // zero
        int minutos = agora.getMinute();
        System.out.println(minutos); // 0

        // ---------------------------------------------------------------------------
        // juntar com LocalDate para obter um OffsetDateTime
        LocalDate data = LocalDate.of(2018, 5, 4);
        OffsetDateTime odt = agora.atDate(data);
        System.out.println(odt); // 2018-05-04T17:00-03:00

        // outra maneira de juntar com um LocalDate
        odt = data.atTime(agora); // 2018-05-04T17:00-03:00

        // converter OffsetDateTime para OffsetTime
        OffsetTime offsetTime = odt.toOffsetTime();
        System.out.println(offsetTime); // 17:00-03:00

        // obter somente o horário: 17:00
        LocalTime hora = offsetTime.toLocalTime();
        // juntar LocalTime com offset: 17:00-03:00
        offsetTime = hora.atOffset(ZoneOffset.ofHours(-3));
    }
}
