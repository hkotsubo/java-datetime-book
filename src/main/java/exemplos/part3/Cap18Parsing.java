package exemplos.part3;

import static exemplos.setup.Setup.setup;

import java.text.ParsePosition;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import exemplos.part2.Cap08e09FormatacaoParsing;

public class Cap18Parsing {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) {
        exemplosBasicos();
        retornarQualquerTipo();
        patternOpcionalValoresPredefinidos();
        textosCustomizados();
        modosDeParsing();
        patternsParaAno();
        ignorarResolverStyle();
        parsingPodeRetornarVariosTipos();
        iso8601();
        abreviacaoTimezone();

        // ------------------------------
        bugParseDefaultYear();
        encontrarAnoParaDiaMesDiaDaSemana();
        dataHoraSemSeparadores();
        anoCom2Digitos();
        parseMicrosoftJSONDate();
        parseGmtOffset();
    }

    static void exemplosBasicos() {
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
        LocalDateTime dataHora = LocalDateTime.parse("04/05/2018 17:30", parser);
        System.out.println(dataHora); // 2018-05-04T17:30

        // a string tem data e hora, então o DateTimeFormatter precisa ter todos estes campos
        // mas ao fazer o parsing, cada tipo pega apenas os campos que precisa
        LocalDate data = LocalDate.parse("04/05/2018 17:30", parser);
        System.out.println(data); // 2018-05-04
        // mesmo que LocalDate só precise da data, o parser precisa de todos os campos para fazer o parsing da string

        // se a classe precisa de algum campo que a string não tem, dá erro
        try {
            OffsetDateTime odt = OffsetDateTime.parse("04/05/2018 17:30", parser);
            System.out.println(odt); // não será executado, pois o parse lança exceção (OffsetDateTime precisa da data, hora e offset, string de entrada possui
                                     // somente data e hora)
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage());
            // Text '04/05/2018 17:30' could not be parsed: Unable to obtain OffsetDateTime from TemporalAccessor: {},ISO resolved to 2018-05-04T17:30 of type
            // java.time.format.Parsed
        }
    }

    // SimpleDateFormat.parse só retorna Date, mas DateTimeFormatter é mais flexível
    static void retornarQualquerTipo() {
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
        // parse sem determinar o tipo, retorna um TemporalAccessor
        TemporalAccessor parsed = parser.parse("04/05/2018 17:30");
        // usado para casos em que só queremos saber o valor numérico de algum campo, sem ter que criar um objeto específico (como LocalDate ou LocalDateTime).
        System.out.println(parsed.get(ChronoField.DAY_OF_MONTH)); // 4

        // mas o melhor uso é criar um TemporalQuery e retornar qualquer coisa que quisermos
        TemporalQuery<Boolean> isFimDeSemana = temporal -> {
            // verifica se é fim de semana
            DayOfWeek diaDaSemana = DayOfWeek.from(temporal);
            return diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY;
        };
        // passando o TemporalQuery para o método parse, o resultado é o tipo retornado pela query
        Boolean fimDeSemana = parser.parse("04/05/2018 17:30", isFimDeSemana);
        System.out.println(fimDeSemana); // false

        // pode-se usar method reference como uma TemporalQuery
        parser = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        DayOfWeek dow = parser.parse("04/05/2018", DayOfWeek::from);
        System.out.println(dow); // FRIDAY
        // Assim, não preciso criar um LocalDate para depois chamar getDayOfWeek, posso criar o DayOfWeek diretamente

        // Você pode criar TemporalQuery que retorna qualquer coisa que precisar (inclusive classes do seu próprio modelo de dados, não precisa se limitar às
        // classes nativas da linguagem)
    }

    static void patternOpcionalValoresPredefinidos() {
        DateTimeFormatter parser = new DateTimeFormatterBuilder()
            // data obrigatória, hora opcional
            .appendPattern("dd/MM/uuuu[ HH:mm]")
            // valor predefinido para hora (usado quando o campo não estiver presente)
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 10)
            // valor predefinido para minuto (usado quando o campo não estiver presente)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 30)
            // valor predefinido para offset (-03:00) - este nunca estará presente, pois não foi especificado no pattern
            .parseDefaulting(ChronoField.OFFSET_SECONDS, ZoneOffset.ofHours(-3).getTotalSeconds())
            // criar DateTimeFormatter
            .toFormatter();
        // String sem horário e offset, usa os valores predefinidos (hora 10:30 e offset -03:00)
        OffsetDateTime odt = OffsetDateTime.parse("04/05/2018", parser);
        System.out.println(odt);// 2018-05-04T10:30-03:00
        // String com horário, ignora o valor predefinido (mas usa o offset predefinido, pois este campo não está presente na String)
        odt = OffsetDateTime.parse("04/05/2018 17:20", parser);
        System.out.println(odt);// 2018-05-04T17:20-03:00

        // ************************** cuidados ao usar patterns opcionais
        // 2 formatos possíveis para data
        parser = DateTimeFormatter.ofPattern("[dd/MM/uuuu][uuuu-MM-dd]");
        // ambas as linhas abaixo resultam no mesmo LocalDate
        LocalDate data1 = LocalDate.parse("2018-05-04", parser);
        LocalDate data2 = LocalDate.parse("04/05/2018", parser);
        System.out.println(data1); // 2018-05-04
        System.out.println(data2); // 2018-05-04
        // este formatter é bom para parsing (aceita 2 formatos diferentes de data), mas é ruim para formatar:
        System.out.println(data1.format(parser)); // 04/05/20182018-05-04
        // ele imprimiu a data nos 2 formatos, já que ambos os patterns (dd/MM/uuuu e uuuu-MM-dd) possuem campos que LocalDate tem
        // neste caso, o ideal é usar outro DateTimeFormatter para formatar

        // -------------------- A ordem pode fazer diferença
        // data e hora, com 3 formatos possíveis para offset
        parser = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm[X][XX][XXX]");
        try {
            odt = OffsetDateTime.parse("04/05/2018 17:30-03:00", parser);
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage()); // Text '04/05/2018 17:30-03:00' could not be parsed, unparsed text found at index 19
        }
        // Provavelmente devido a algum detalhe de implementação, este problema é facilmente resolvido mudando-se a ordem das seções opcionais, colocando as
        // maiores (com mais caracteres) primeiro:
        parser = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm[XXX][XX][X]");
        odt = OffsetDateTime.parse("04/05/2018 17:30-03:00", parser);
        System.out.println(odt); // 2018-05-04T17:30-03:00
        // lembrando que este DateTimeFormatter não é bom para formatar, pois vai mostrar o offset 3 vezes (uma para cada formato)
        System.out.println(odt.format(parser)); // 04/05/2018 17:30-03:00-0300-03
    }

    static void textosCustomizados() {
        // usar os prefixos do formato americano para os dias (1st, 2nd, 3rd, 4th, etc)
        Map<Long, String> diasComSufixo = new HashMap<>();
        // preencher o map com todos os valores válidos para o dia
        for (int dia = 1; dia <= 31; dia++) {
            String texto = Integer.toString(dia);
            switch (dia) {
                case 1:
                case 21:
                case 31:
                    texto += "st";
                    break;
                case 2:
                case 22:
                    texto += "nd";
                    break;
                case 3:
                case 23:
                    texto += "rd";
                    break;
                default:
                    texto += "th";
            }
            diasComSufixo.put((long) dia, texto);
        }
        DateTimeFormatter parser = new DateTimeFormatterBuilder()
            // ignorar maiúsculas e minúsculas (para o nome do mês poder ser "may", "May" ou "MAY", etc)
            .parseCaseInsensitive()
            // nome do mês seguido de espaço
            .appendPattern("MMMM ")
            // dia com sufixo
            .appendText(ChronoField.DAY_OF_MONTH, diasComSufixo)
            // vírgula, espaço, ano
            .appendPattern(", uuuu")
            // locale do idioma inglês (para o nome do mês)
            .toFormatter(Locale.ENGLISH);
        // 2018-05-04
        LocalDate data = LocalDate.parse("may 4th, 2018", parser);
        System.out.println(data); // 2018-05-04
        // se usar o DateTimeFormatter para formatar, o nome do mês é maiúsculo
        System.out.println(data.format(parser)); // May 4th, 2018
        // O parseCaseInsensitive(), como o nome diz, é somente para parsing. Para formatar, são usadas as strings do Locale predefinidas na JVM

        // É possível ter alguns campos case sensitive e outros não:
        parser = new DateTimeFormatterBuilder()
            // ignorar maiúsculas e minúsculas para dia da semana
            .parseCaseInsensitive()
            // nome do mês seguido de espaço
            .appendPattern("EEEE ")
            // Não ignorar maiúsculas e minúsculas para o nome do mês
            .parseCaseSensitive()
            // nome do mês seguido de espaço, seguido de dia e ano
            .appendPattern("MMMM dd uuuu")
            // locale do idioma inglês (para o nome do mês)
            .toFormatter(Locale.ENGLISH);

        // dia da semana é case insensitive, mas o mês não
        data = LocalDate.parse("friDAy May 04 2018", parser);
        System.out.println(data); // 2018-05-04
        try {
            // mês não é case insensitive, então "may" lança exceção
            LocalDate.parse("friday may 04 2018", parser);
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage()); // Text 'friday may 04 2018' could not be parsed at index 7
        }
    }

    static void modosDeParsing() {
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        try {
            // Por padrão, não aceita valores fora dos limites de cada campo
            // Como o dia do mês só pode ser de 1 a 31, o dia 33 lança exceção
            LocalDate.parse("33/01/2018", parser);
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage()); // Text '33/01/2018' could not be parsed: Invalid value for DayOfMonth (valid values 1 - 28/31): 33
        }

        // mudando para leniente, o dia 33 passa a ser aceito
        parser = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            // mudar para leniente
            .withResolverStyle(ResolverStyle.LENIENT);
        LocalDate data = LocalDate.parse("33/01/2018", parser);
        System.out.println(data); // 2018-02-02
        // O valor "33 de janeiro" é ajustado para "2 de fevereiro". O raciocínio é que 33 são 2 dias depois do último dia de janeiro (31), o que seria
        // "equivalente" a 2 de fevereiro.

        // O padrão é não ser leniente: não aceitar valores fora dos limites do campo, como dia > 31 (ou <= 0)
        // Apesar disso, o padrão pode fazer alguns ajustes para datas inválidas
        parser = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        data = LocalDate.parse("31/04/2018", parser);
        // 31 é um valor válido para o campo dia, mas a combinação "31 de abril" não é válida, então a data é ajustada para o último dia de abril (30)
        System.out.println(data); // 2018-04-30

        // para não fazer nenhum ajuste e só aceitar datas válidas, deve-se mudar o ResolverStyle para STRICT
        parser = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
        try {
            // 31 de abril é uma data inválida, lança exceção
            data = LocalDate.parse("31/04/2018", parser);
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage()); // Text '31/04/2018' could not be parsed: Invalid date 'APRIL 31'
        }

        // Apenas para registro: quando você cria um DateTimeFormatter, o ResolverStyle default é SMART. Resumindo:
        // - LENINENT: aceita valores fora dos limites do campo (dia > 31) e faz ajustes (33 de janeiro é ajustado para 2 de fevereiro)
        // - SMART: não aceita valores fora dos limites do campo mas faz ajustes (31 de abril é ajustado para 30 de abril)
        // - STRICT: não aceita valores fora dos limites do campo e nem faz ajustes (em vez disso, lança exceção para datas inválidas)

        // Mas o modo leniente não como SimpleDateFormat, que aceita qualquer coisa (ver Cap08e09FormatacaoParsing, método parseLenienteAteDemais)
        parser = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.LENIENT);
        try {
            // String com formato diferente do pattern, lança exceção mesmo sendo leniente (ao contrário de SimpleDateFormat, que não dá erro e gera uma data
            // completamente diferente)
            System.out.println(parser.parse("2018-02-01"));
        } catch (Exception e) {
            System.out.println(e.getMessage()); // Text '2018-02-01' could not be parsed at index 4
        }
    }

    // Para o campo ano, podemos usar "u" ou "y", qual a diferença? A ideia básica já é explicada na classe Cap17Formatacao, método diferencaEntreUeY
    // Aqui veremos mais alguns casos em que pode fazer diferença usar um ou outro
    static void patternsParaAno() {
        // usando yyyy no modo STRICT
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("dd/MM/yyyy").withResolverStyle(ResolverStyle.STRICT);
        try {
            System.out.println(LocalDate.parse("10/02/2018", parser));
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage()); // Text '10/02/2018' could not be parsed: Unable to obtain LocalDate from TemporalAccessor: {MonthOfYear=2,
                                                // YearOfEra=2018, DayOfMonth=10},ISO of type java.time.format.Parsed
        }
        // A mensagem da exceção informa que o campo YearOfEra foi setado para 2018, mas este campo também precisa saber qual a era (AC ou DC - Antes de Cristo
        // ou Depois de Cristo), conforme explicado em https://stackoverflow.com/a/41104034

        // ------------------------- Há 3 soluções para isso:
        // 1- mudar o ResolverStyle para SMART (o default) ou LENIENT, pois assim a era atual (DC) será usada
        parser = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // sem especificar o ResolverStyle, por padrão será SMART
        System.out.println(LocalDate.parse("10/02/2018", parser)); // 2018-02-10
        parser = DateTimeFormatter.ofPattern("dd/MM/yyyy").withResolverStyle(ResolverStyle.LENIENT); // com LENIENT também funciona
        System.out.println(LocalDate.parse("10/02/2018", parser)); // 2018-02-10

        // 2- se o ResolverStyle não pode ser mudado, use o pattern "uuuu" em vez de "yyyy":
        parser = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
        System.out.println(LocalDate.parse("10/02/2018", parser)); // 2018-02-10

        // 3- se o ResolverStyle e o pattern "yyyy" não podem ser mudados, usar DateTimeFormatterBuilder e definir um valor default para a era:
        parser = new DateTimeFormatterBuilder()
            // pattern com yyyy
            .appendPattern("dd/MM/yyyy")
            // definir valor para a era (0 para AC, 1 para DC)
            .parseDefaulting(ChronoField.ERA, 1)
            // usar modo STRICT
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);
        System.out.println(LocalDate.parse("10/02/2018", parser)); // 2018-02-10
    }

    static void ignorarResolverStyle() {
        String input = "99/00/2018";
        // Como fazer parsing desta data, supondo que a especificação seja: o mês zero deve ser automaticamente mudado para janeiro, e qualquer dia maior do que
        // o máximo permitido no mês deve ser ajustado para o último dia do mês
        // Nos modos SMART e STRICT daria erro, porque os valores estão fora do permitido para os campos (dia entre 1 e 31, mês entre 1 e 12)
        // No modo LENIENT, seria ajustada para 9 de março de 2018 (é um motivo para eu evitar usar LENIENT, pois nem sempre as conversões são óbvias):
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.LENIENT);
        System.out.println(LocalDate.parse(input, fmt)); // 2018-03-09

        // ------------------------------------------------------
        // Nenhum dos modos de parsing resolve o nosso problema, mas temos uma alternativa: usar parseUnresolved
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        // começar o parsing da posição zero (ou seja, desde o início da String)
        ParsePosition position = new ParsePosition(0);
        TemporalAccessor parsed = parser.parseUnresolved(input, position);
        // verifica se houve erro no parsing
        if (position.getErrorIndex() >= 0) {
            System.out.println("Erro na posição " + position.getErrorIndex());
        } else if (position.getIndex() < input.length()) {
            System.out.println("Não fez parsing da String toda, parou na posição " + position.getIndex());
        } else {
            // parsing feito com sucesso
            System.out.println(parsed); // {MonthOfYear=0, Year=2018, DayOfMonth=99},null

            // obter o ano
            int ano = (int) parsed.getLong(ChronoField.YEAR);
            // obter o mês
            int mes = (int) parsed.getLong(ChronoField.MONTH_OF_YEAR);
            // se mês for zero, mudar para janeiro
            if (mes == 0) {
                mes = 1;
            }
            // YearMonth representa o mês e o ano
            YearMonth ym = YearMonth.of(ano, mes);
            LocalDate data;
            // obter o dia
            int dia = (int) parsed.getLong(ChronoField.DAY_OF_MONTH);
            // verifica se o dia ultrapassa o máximo permitido no mês
            if (dia > ym.lengthOfMonth()) {
                // dia maior que o permitido, ajustar para o último dia do mês
                data = ym.atEndOfMonth();
            } else {
                // valor do dia OK, usá-lo para construir o LocalDate
                data = ym.atDay(dia);
            }
            System.out.println(data); // 2018-01-31

            // usar parseUnresolved é uma alternativa para quando os modos normais (STRICT, SMART e LENINENT) não forem o suficiente
        }
    }

    static void parsingPodeRetornarVariosTipos() {
        // data obrigatória, hora e offset opcionais
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("dd/MM/uuuu[ HH:mm][XXX]");

        TemporalAccessor parsed = parser.parseBest("04/05/2018",
                // lista de TemporalQuery a serem usados no parsing
                OffsetDateTime::from, LocalDateTime::from, LocalDate::from);
        // verifica se o resultado é OffsetDateTime
        if (parsed instanceof OffsetDateTime) { // não entra neste if
            // como a String só tem data (não tem hora nem offset), não é suficiente para obter um OffsetDateTime
            OffsetDateTime odt = (OffsetDateTime) parsed;
            // usar o OffsetDateTime ....
            System.out.println(odt);
        }
        // verifica se o resultado é LocalDateTime
        if (parsed instanceof LocalDateTime) { // não entra neste if
            // como a String só tem a data (não tem hora), não é suficiente para obter um LocalDateTime
            LocalDateTime ldt = (LocalDateTime) parsed;
            // usar o LocalDateTime ....
            System.out.println(ldt);
        }
        // verifica se o resultado é LocalDate
        if (parsed instanceof LocalDate) { // entra neste if
            LocalDate data = (LocalDate) parsed;
            // usar o LocalDate ....
            System.out.println(data); // 2018-05-04 -> a String só tem a data, é o suficiente para obter um LocalDate
        }

        // É possível passar quantas instâncias de TemporalQuery (ou method references) você quiser. O único detalhe é que o retorno do TemporalQuery deve ser
        // uma classe que implemente TemporalAccessor.

        // se a String tiver data e hora, ainda não é suficiente para criar OffsetDateTime (falta o offset), mas é o suficiente para LocalDateTime
        parsed = parser.parseBest("04/05/2018 17:30", OffsetDateTime::from, LocalDateTime::from, LocalDate::from);
        System.out.println(parsed.getClass()); // class java.time.LocalDateTime
        System.out.println(parsed); // 2018-05-04T17:30

        // A ordem é importante
        parsed = parser.parseBest("04/05/2018 17:30", LocalDate::from, LocalDateTime::from, OffsetDateTime::from);
        System.out.println(parsed.getClass()); // class java.time.LocalDate
        System.out.println(parsed); // 2018-05-04
        // Primeiro o método tenta obter um LocalDate, e como a String possui data, é o suficiente
        // Por isso o retorno é um LocalDate
    }

    static void iso8601() {
        // Formato ISO 8601 não precisa de DateTimeFormatter:

        // somente data
        LocalDate data = LocalDate.parse("2018-05-04");
        // data e hora (com segundos e nanossegundos)
        LocalDateTime dataHora = LocalDateTime.parse("2018-05-04T17:30:45.123456789");
        // data, hora e offset
        OffsetDateTime odt = OffsetDateTime.parse("2018-05-04T17:30:45-03:00");
        // data, hora, offset e timezone (atenção: o nome do timezone não faz parte da norma ISO 8601, é uma extensão adicionada pelo Java)
        ZonedDateTime zdt = ZonedDateTime.parse("2018-05-04T17:30-03:00[America/Sao_Paulo]");

        // ------------------------------------
        // Claro que há exceções: se a string tiver data e hora mas eu só quero a data (LocalDate), por exemplo, há 2 alternativas:
        // 1- usar LocalDateTime e converter para LocalDate
        data = LocalDateTime.parse("2018-05-04T17:30:45.123456789").toLocalDate();
        System.out.println(data); // 2018-05-04
        // 2- usar DateTimeFormatter
        data = LocalDate.parse("2018-05-04T17:30:45.123456789", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        System.out.println(data); // 2018-05-04

        // para offsets, somente o formato com dois pontos (-03:00) é aceito por padrão:
        try {
            // offset sem dois pontos (-0300), lança exceção
            OffsetDateTime.parse("2018-05-04T17:30:45-0300");
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage()); // Text '2018-05-04T17:30:45-0300' could not be parsed at index 19
        }
        // solução: usar seu próprio formatter
        DateTimeFormatter parser = new DateTimeFormatterBuilder()
            // data e hora no formato ISO 8601
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            // offset: -03:00, -0300 ou -03
            .appendPattern("[XXX][XX][X]")
            // criar o DateTimeFormatter
            .toFormatter();
        System.out.println(OffsetDateTime.parse("2018-05-04T10:00-03", parser)); // 2018-05-04T10:00-03:00
    }

    static void abreviacaoTimezone() {
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z uuuu", Locale.ENGLISH);
        // sigla IST é usada na Índia, Irlanda e Israel, qual desses timezones é retornado no parse?
        System.out.println(ZonedDateTime.parse("Sun Jan 07 10:00:00 IST 2018", parser)); // 2018-01-07T10:00+02:00[Asia/Jerusalem] <- timezone de Israel
        // CST é usada em Cuba, China e região central dos EUA, qual será retornado?
        System.out.println(ZonedDateTime.parse("Sun Jan 07 10:00:00 CST 2018", parser)); // 2018-01-07T10:00-06:00[America/Chicago] <- EUA

        // como a abreviação é ambígua, devemos escolher uma delas para ser usada no parsing
        // timezones a serem usados em caso de ambiguidade
        Set<ZoneId> zones = new HashSet<>();
        zones.add(ZoneId.of("Asia/Kolkata")); // Índia
        zones.add(ZoneId.of("Asia/Shanghai")); // China
        parser = new DateTimeFormatterBuilder()
            .appendPattern("EEE MMM dd HH:mm:ss ")
            // usar java.time.format.TextStyle para abreviação do timezone
            .appendZoneText(TextStyle.SHORT, zones) // usar o Set de timezones
            .appendPattern(" uuuu")
            .toFormatter(Locale.ENGLISH);
        // IST é usada na Índia, Irlanda e Israel, mas o Set acima definiu que será Índia
        System.out.println(ZonedDateTime.parse("Sun Jan 07 10:00:00 IST 2018", parser)); // 2018-01-07T10:00+05:30[Asia/Kolkata]
        // CST é usada em Cuba, China e região central dos EUA, mas o Set acima definiu que será China
        System.out.println(ZonedDateTime.parse("Sun Jan 07 10:00:00 CST 2018", parser)); // 2018-01-07T10:00+08:00[Asia/Shanghai]
    }

    // ------------------------------------------------
    // exemplos que não estão no livro

    // Ano opcional, setar valor predefinido para 2017, quando não estiver presente
    static void bugParseDefaultYear() {
        // bug 9050576 - http://bugs.java.com/view_bug.do?bug_id=8186641 <- veja se ocorre na versão de Java que vc usa

        // ------------------------------------------------
        // usar "uuuu" e ChronoField.YEAR funciona
        DateTimeFormatter parser = new DateTimeFormatterBuilder()
            // pattern com ano opcional uuuu
            .appendPattern("[uuuu-]MM-dd")
            .parseDefaulting(ChronoField.YEAR, 2017)
            // create formatter
            .toFormatter();
        System.out.println(LocalDate.parse("10-10", parser)); // 2017-10-10
        System.out.println(LocalDate.parse("2015-10-10", parser)); // 2015-10-10

        // ------------------------------------------------
        // usar "yyyy" e ChronoField.YEAR_OF_ERA funciona
        parser = new DateTimeFormatterBuilder()
            // pattern com ano opcional com yyyy
            .appendPattern("[yyyy-]MM-dd")
            .parseDefaulting(ChronoField.YEAR_OF_ERA, 2017)
            // create formatter
            .toFormatter();
        System.out.println(LocalDate.parse("10-10", parser)); // 2017-10-10
        System.out.println(LocalDate.parse("2015-10-10", parser)); // 2015-10-10

        // ------------------------------------------------
        // usar "yyyy" e ChronoField.YEAR não funciona
        parser = new DateTimeFormatterBuilder()
            // pattern com ano opcional yyyy
            .appendPattern("[yyyy-]MM-dd")
            .parseDefaulting(ChronoField.YEAR, 2017)
            // create formatter
            .toFormatter();
        System.out.println(LocalDate.parse("10-10", parser)); // 2017-10-10
        try {
            System.out.println(LocalDate.parse("2015-10-10", parser)); // erro
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage()); // Text '2015-10-10' could not be parsed: Conflict found: Year 2017 differs from Year 2015
            // A mensagem informa que Year (uuuu) é 2017, mas o outro Year (que corresponde a yyyy - YearOfEra) é 2015. Devido a este conflito, ocorre o erro (o
            // bug é justamente isso: o valor do ano deveria priorizar o que veio na String (2015) e ignorar o valor default (2017)
        }
    }

    /**
     * Dado um dia da semana, mês e dia, encontrar um ano que corresponda.
     * 
     * É interessante comparar com {@link Cap08e09FormatacaoParsing#parseDiaMesSemAno()} para ver a diferença entre as APIs
     */
    public static void encontrarAnoParaDiaMesDiaDaSemana() {
        // String com dia da semana (Tue - Terça-feira), mês e dia (mais o horário)
        String strDate = "Tue, Feb 7 03:30 PM";
        // Tentar encontrar um ano em que o dia 7 de fevereiro é uma terça-feira
        DateTimeFormatter parser = DateTimeFormatter
            // usar pattern que corresponde à String e Locale.ENGLISH porque o mês e dia da semana estão em inglês
            .ofPattern("EEE, MMM d hh:mm a", Locale.ENGLISH);

        // Não adianta tentar criar LocalDateTime, pois não temos o ano e lançaria uma exceção. Então fazemos o parse e criamos um TemporalAccessor
        TemporalAccessor parsed = parser.parse(strDate);
        // pense no TemporalAccessor como uma estrutura "intermediária", que contém todos os campos obtidos no parsing
        System.out.println(parsed); // {MonthOfYear=2, DayOfWeek=2, DayOfMonth=7},ISO resolved to 15:30

        // ------------------------------------
        // A partir do TemporalAccessor, podemos obter todos os campos que precisamos e criar as classes que nos ajudarão a encontrar o ano correto

        // obter o mês e dia
        MonthDay md = MonthDay.from(parsed);
        // dia da semana
        DayOfWeek dow = DayOfWeek.from(parsed);
        LocalDate data = null;
        // Como existem vários anos em que 7 de fevereiro caiu em uma terça, temos que escolher arbitrariamente os valores que testaremos
        // No caso, eu começo em 2018. Se quiser começar no ano atual, use Year.now().getValue()
        for (int ano = 2018; ano > 2000; ano--) {
            // obter um LocalDate que representa o dia e mês obtido no parsing e o ano que estamos verificando
            data = md.atYear(ano);
            // verifica se o dia da semana é o mesmo
            if (data.getDayOfWeek() == dow) {
                // encontrado!
                break;
            }
        }

        if (data != null) {
            // data encontrada, juntar com o horário obtido no parsing para criar o LocalDateTime
            LocalDateTime dt = LocalDateTime.of(data, LocalTime.from(parsed));
            System.out.println(dt); // 2017-02-07T15:30

            // ou, se preferir, também pode ser feito assim (as 2 formas abaixo também resultam em um LocalDateTime igual a 2017-02-07T15:30)
            dt = LocalTime.from(parsed).atDate(data);
            dt = data.atTime(LocalTime.from(parsed));
        }
    }

    static void dataHoraSemSeparadores() {
        // bug no Java 8 -> https://bugs.java.com/view_bug.do?bug_id=JDK-8031085
        String text = "20170925142051591";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

        try {
            // no Java 8 dá erro, no Java >=9 já foi corrigido e faz o parsing corretamente para 2017-09-25T14:20:51.591
            System.out.println(LocalDateTime.parse(text, formatter));
        } catch (DateTimeParseException e) {
            // exceção ocorre no Java 8
            System.out.println(e.getMessage()); // Text '20170925142051591' could not be parsed at index 0
        }

        // para Java 8, a alternativa é usar isso:
        formatter = new DateTimeFormatterBuilder()
            // date/time
            .appendPattern("yyyyMMddHHmmss")
            // milliseconds
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            // create formatter
            .toFormatter();
        LocalDateTime dateTime = LocalDateTime.parse(text, formatter);
        System.out.println(dateTime); // 2017-09-25T14:20:51.591
    }

    static void anoCom2Digitos() {
        String str = "100298"; // dia 10, mês 02, ano 98
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("ddMMyy");
        System.out.println(LocalDate.parse(str, parser)); // 2098-02-10
        // o resultado é 2098 (mas eu queria 1998, como fazer?)

        // basta usar appendValueReduced e definir um "ano base"
        parser = new DateTimeFormatterBuilder()
            // dia e mês
            .appendPattern("ddMM")
            // ano com 2 dígitos (o primeiro "2" é o tamanho mínimo, o segundo é o tamanho máximo)
            // usando 1930 como ano base, os valores do ano estarão entre 1930 e 2029
            .appendValueReduced(ChronoField.YEAR, 2, 2, 1930)
            .toFormatter();
        // ano "98" - usando 1930 como ano base, o valor resultante é 1998 (pois se fosse 2098, estaria fora do intervalo 1930 - 2029)
        System.out.println(LocalDate.parse(str, parser)); // 1998-02-10
        // ano "20" - resulta em 2020 (pois qualquer outro valor, como 1920, estaria fora do intervalo 1930 - 2029)
        System.out.println(LocalDate.parse("010120", parser)); // 2020-01-01
    }

    // parsing do formato bizarro que algumas APIs retornam: https://www.hanselman.com/blog/OnTheNightmareThatIsJSONDatesPlusJSONNETAndASPNETWebAPI.aspx
    static void parseMicrosoftJSONDate() {
        String s = "/Date(1325134800000-0500)/";

        // pegar somente a parte "1325134800000-0500" - s.substring(6, 24) também funcionaria
        s = s.replaceAll(".*/Date\\(([\\d\\+\\-]+)\\)/.*", "$1");

        // Pequeno truque para fazer o parsing do timestamp: o valor 1325134800000 é a quantidade de milissegundos desde o Unix Epoch
        // Então podemos usar o campo INSTANT_SECONDS (1325134800) seguido do campo MILLI_OF_SECOND (000)
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
            // epoch seconds
            .appendValue(ChronoField.INSTANT_SECONDS)
            // milliseconds
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            // offset (-0500)
            .appendPattern("xx")
            // create formatter
            .toFormatter();

        // criar Instant
        TemporalAccessor parsed = fmt.parse(s);
        Instant instant = Instant.from(parsed);
        System.out.println(instant); // 2011-12-29T05:00:00Z
        // converter para o offset
        OffsetDateTime odt = instant.atOffset(ZoneOffset.from(parsed));
        System.out.println(odt); // 2011-12-29T00:00-05:00

        // ------------------------------------------------
        // Outra alternativa é fazer split na String e tratar os campos separadamente. Depois do replaceAll acima, o valor de "s" é "1325134800000-0500", então
        // basta fazer o split, usando lookahead "(?=" para o sinal ("-" ou "+") fazer parte do split (assim não perdemos o sinal do offset):
        // https://www.regular-expressions.info/lookaround.html
        // Também uso "(?:" para que não se formem grupos de captura (https://www.regular-expressions.info/brackets.html#noncap), pois não é necessário capturar
        // nada (a regex serve apenas para sabermos a posição em que deve ser feito o split)
        String[] partes = s.split("(?=(?:-|\\+))");

        // partes[0] tem o timestamp
        instant = Instant.ofEpochMilli(Long.parseLong(partes[0]));
        // partes[1] tem o offset
        odt = instant.atOffset(ZoneOffset.of(partes[1]));
        System.out.println(instant); // 2011-12-29T05:00:00Z
        System.out.println(odt); // 2011-12-29T00:00-05:00
    }

    static void parseGmtOffset() {
        // bug no Java 8 -> https://bugs.openjdk.java.net/browse/JDK-8154050 (https://stackoverflow.com/q/37287103)
        String input = "08 Jul 2018 13:34:21 GMT-8";
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss O", Locale.ENGLISH);
        try {
            // segundo o link acima do bugs.openjdk, no Java 9 já foi corrigido (mas no Java 8, lança exceção)
            System.out.println(OffsetDateTime.parse(input, parser)); // 018-07-08T13:34:21-08:00
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage()); // Text '08 Jul 2018 13:34:21 GMT-8' could not be parsed: String index out of range: 26
        }

        // ---------------------------------------
        // workaround ("gambiarra" para resolver): adicionar um espaço no final (tanto na string quanto no pattern)
        input = "08 Jul 2018 13:34:21 GMT-8 ";
        parser = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss O ", Locale.ENGLISH);
        OffsetDateTime odt = OffsetDateTime.parse(input, parser);
        System.out.println(odt); // 2018-07-08T13:34:21-08:00
    }
}
