package exemplos.part2;

import static exemplos.setup.Setup.clock;
import static exemplos.setup.Setup.setup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import exemplos.part3.Cap18Parsing;

public class Cap08e09FormatacaoParsing {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) throws ParseException {
        formatarData();
        formatarDataMudarTimezonePadrao();
        formatarDataMudarTimezoneFormatter();
        naoFormataCalendar();
        formatarMesDiaDaSemana();
        formatarMesDiaDaSemanaLocaleDefault();
        formatarMesDiaDaSemanaLocale();
        formatarLocaleVsTimezone();
        formatarISO8601();
        parseData();
        parseDataSetarHorario();
        parseHorarioDeVerao();
        parseOverlap();
        converterFormatos();
        parseComLocale();
        parseISO8601();
        parseLeniente();
        parseLenienteAteDemais();
        parseFracaoSegundos();

        // --------------------------------------------
        // exemplos que não estão no livro
        parseDataSamoa();
        parseAnoDoisDigitos();
        parseDiaMesSemAno();
        parseISO8601Java6();
    }

    static void formatarData() {
        // usando nossa "data atual" simulada
        Date dataAtual = new Date(clock().millis());

        // usar o formato dia/mês/ano
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        // formatando a data atual
        String dataFormatada = formatter.format(dataAtual);
        System.out.println(dataFormatada); // 04/05/2018
    }

    static void formatarDataMudarTimezonePadrao() {
        // usando nossa "data atual" simulada
        Date dataAtual = new Date(clock().millis());

        // mudar o timezone padrão
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        // usar o formato dia/mês/ano
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        // formatando a data atual (2018-05-04T17:00-03:00)
        System.out.println(formatter.format(dataAtual)); // 05/05/2018

        // voltar config default (para não impactar os outros métodos)
        setup();
    }

    static void formatarDataMudarTimezoneFormatter() {
        // usando nossa "data atual" simulada
        Date dataAtual = new Date(clock().millis());

        // usar o formato dia/mês/ano
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        // usar um timezone específico (não importa qual o timezone padrão da JVM, será usado o timezone abaixo)
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        // formatando a data atual (2018-05-04T17:00-03:00)
        System.out.println(formatter.format(dataAtual)); // 05/05/2018

        // setar o timezone no SimpleDateFormat é melhor porque TimeZone.setDefault muda a config para toda a JVM
    }

    static void naoFormataCalendar() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        try {
            formatter.format(cal); // java.lang.IllegalArgumentException: Cannot format given Object as a Date
        } catch (IllegalArgumentException e) {
            System.out.println("SimpleDateForma não formata Calendar: " + e);
        }
        // para formatar, temos que passar o Date
        System.out.println(formatter.format(cal.getTime()));
    }

    static void formatarMesDiaDaSemana() {
        // usando nossa "data atual" simulada
        Date dataAtual = new Date(clock().millis());

        SimpleDateFormat formatter = new SimpleDateFormat("EEE dd/MMM/yyyy");
        System.out.println(formatter.format(dataAtual)); // Sex 04/mai/2018

        formatter = new SimpleDateFormat("EEEE dd/MMMM/yyyy");
        System.out.println(formatter.format(dataAtual)); // Sexta-feira 04/Maio/2018
    }

    static void formatarMesDiaDaSemanaLocaleDefault() {
        // usando nossa "data atual" simulada
        Date dataAtual = new Date(clock().millis());

        // mudar o locale padrão para inglês
        Locale.setDefault(Locale.ENGLISH);

        SimpleDateFormat formatter = new SimpleDateFormat("EEEE dd/MMMM/yyyy");
        System.out.println(formatter.format(dataAtual)); // Friday 04/May/2018

        // voltar config default (para não impactar os outros métodos)
        setup();
    }

    static void formatarMesDiaDaSemanaLocale() {
        // usando nossa "data atual" simulada
        Date dataAtual = new Date(clock().millis());

        SimpleDateFormat formatter = new SimpleDateFormat("EEEE dd/MMMM/yyyy", Locale.ENGLISH);
        System.out.println(formatter.format(dataAtual)); // Friday 04/May/2018
    }

    static void formatarLocaleVsTimezone() {
        // usando nossa "data atual" simulada
        Date dataAtual = new Date(clock().millis());

        // formatter em inglês
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
        // converter a data para o timezone do Japão
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));

        // saída em inglês, mas data corresponde ao timezone do Japão
        System.out.println(formatter.format(dataAtual)); // 05/May/2018

        // o Locale não interfere no timezone, e vice-versa
    }

    static void formatarISO8601() {
        // usando nossa "data atual" simulada
        Date dataAtual = new Date(clock().millis());

        SimpleDateFormat iso8601Format;
        try {
            // dá erro porque "T" não é um pattern válido
            iso8601Format = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.SSSXXX");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro porque T não é um pattern válido: " + e); // java.lang.IllegalArgumentException: Illegal pattern character 'T'
        }

        // "T" deve estar entre aspas simples
        iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        System.out.println(iso8601Format.format(dataAtual)); // 2018-05-04T17:00:00.000-03:00

        // há a opção de setar um timezone no formatter. Exemplo: usar UTC
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.out.println(iso8601Format.format(dataAtual)); // 2018-05-04T20:00:00.000Z
    }

    static void parseData() {
        // usar o formato dia/mês/ano
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
        try {
            // parsing: converte a String para java.util.Date
            Date data = parser.parse("04/05/2018");
            System.out.println(data); // Fri May 04 00:00:00 BRT 2018
            System.out.println(data.getTime()); // 1525402800000
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    static void parseDataSetarHorario() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
        // setar o timezone para não depender da configuração padrão
        parser.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        Date data = parser.parse("04/05/2018");
        // String não tem campos de horário, então é setado para meia-noite

        // criar um Calendar e usar o Date obtido pelo parsing
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        // mudar o horário para 17:00
        cal.set(Calendar.HOUR_OF_DAY, 17);
        // obter o Date com o novo horário
        data = cal.getTime();
        System.out.println(data); // Fri May 04 17:00:00 BRT 2018
        System.out.println(data.getTime()); // 1525464000000
    }

    static void parseHorarioDeVerao() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
        Date date = parser.parse("15/10/2017");
        // horário é setado para meia-noite, mas devido ao horário de verão, à meia-noite, os relógios são adiantados para 01:00, e portanto meia-noite não
        // existe neste dia, neste timezone
        System.out.println(date); // Sun Oct 15 01:00:00 BRST 2017
    }

    static void parseOverlap() throws ParseException {
        // formato com dia/mês/ano e hora:minuto
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = parser.parse("17/02/2018 23:00");
        // no término do horário de verão, à meia-noite o relógio é atrasado em uma hora, de volta para 23:00 (ou seja, os minutos entre 23:00 e 23:59 existem
        // duas vezes) - o parse, por padrão, retorna a segunda ocorrência (depois que acabou o horário de verão)
        System.out.println(date); // Sat Feb 17 23:00:00 BRT 2018
    }

    static void converterFormatos() throws ParseException {
        // parsing do formato com dia/mês/ano e hora:minuto
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = parser.parse("17/02/2018 23:00");

        // formatar para ISO 8601
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        System.out.println(iso8601Format.format(date)); // 2018-02-17T23:00:00.000-03:00
    }

    static void parseComLocale() throws ParseException {
        // formato com dia/mês/ano e locale inglês
        SimpleDateFormat parser = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
        System.out.println(parser.parse("01/Oct/2017")); // Sun Oct 01 00:00:00 BRT 2017
    }

    static void parseISO8601() throws ParseException {
        // jeito errado de se fazer parsing de UTC (Z entre aspas simples)
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        // 4 de maio de 2018, à 01:00 em UTC
        String entrada = "2018-05-04T01:00Z";
        // resultado é 1 da manhã no timezone padrão (America/Sao_Paulo), mas a entrada está em UTC, então o resultado está errado, pois 1 da manhã do dia 4 de
        // maio no timezone America/Sao_Paulo equivale a 4 da manhã em UTC
        System.out.println(iso8601Format.parse(entrada)); // Fri May 04 01:00:00 BRT 2018

        // segunda tentativa: retirar as aspas simples do Z
        iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        try {
            System.out.println(iso8601Format.parse(entrada)); // java.text.ParseException: Unparseable date: "2018-05-04T01:00Z"
        } catch (ParseException e) {
            System.out.println("Não deu: " + e);
        }

        // jeito certo: usar o pattern XXX (offset)
        iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");
        System.out.println(iso8601Format.parse(entrada)); // Thu May 03 22:00:00 BRT 2018
        // agora sim, 3 de maio às 22:00 no timezone America/Sao_Paulo equivale a 4 de maio às 01:00 em UTC (que é exatamente o que a String representa)
    }

    static void parseLeniente() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
        // 31 de fevereiro de 2018 - parsing resulta em 3 de março
        System.out.println(parser.parse("31/02/2018")); // Sat Mar 03 00:00:00 BRT 2018

        // cancelar o comportamento leniente
        parser.setLenient(false);
        try {
            System.out.println(parser.parse("31/02/2018")); // java.text.ParseException: Unparseable date: "31/02/2018"
        } catch (ParseException e) {
            System.out.println("Modo leniente desligado, só aceito datas válidas: " + e);
        }
    }

    static void parseLenienteAteDemais() throws ParseException {
        // formato ano, mês e dia, sem separadores
        SimpleDateFormat parser = new SimpleDateFormat("yyyyMMdd");
        // 1 de fevereiro de 2018, em formato ISO 8601 (ou seja, formato diferente do pattern acima)
        System.out.println(parser.parse("2018-02-01")); // Sat Dec 02 00:00:00 BRST 2017
        // resultado foi 2 de dezembro de 2017, uma data completamente diferente

        // formato dia/mês/ano
        parser = new SimpleDateFormat("dd/MM/yyyy");
        parser.setLenient(false);
        // 1 de fevereiro de 2018, às 10:30
        System.out.println(parser.parse("01/02/2018 10:30")); // Thu Feb 01 00:00:00 BRST 2018
        // resultado ignorou o horário (10:30 foi ignorado, o resultado tem horário igual a meia-noite)
    }

    static void parseFracaoSegundos() throws ParseException {
        // pattern com 6 dígitos na fração de segundos
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        // parsing da data (horário estará diferente da entrada)
        Date date = parser.parse("2018-02-01T10:20:30.123456");
        System.out.println(date); // Thu Feb 01 10:22:33 BRST 2018
        // O trecho "123456" foi interpretado como 123456 milissegundos, que correspondem a "2 minutos, 3 segundos e 456 milissegundos". E este valor é somado
        // ao horário que já havia sido obtido anteriormente (10:20:30). Por isso o resultado é 10:22:33.456 (mas repare que Date.toString() não mostra os
        // milissegundos - mas eles estão lá).

        // formatar o Date usando o mesmo SimpleDateFormat (com 6 casas decimais)
        System.out.println(parser.format(date)); // 2018-02-01T10:22:33.000456 <-- errado (no final deveria ser ".456")
    }

    // --------------------------------------------
    // exemplos que não estão no livro

    // No timezone Pacific/Apia (Samoa) não houve o dia 30/12/2011 (foi pulado quando eles trocaram de lado da Linha Internacional de Data)
    static void parseDataSamoa() throws ParseException {
        // formato dia/mês/ano
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        // usar timezone Pacific/Apia
        sdf.setTimeZone(TimeZone.getTimeZone("Pacific/Apia"));

        // parse de uma data (30 de dezembro de 2011)
        Date date = sdf.parse("30/12/2011");

        // formatar a data
        System.out.println(sdf.format(date)); // 31/12/2011
    }

    static void parseAnoDoisDigitos() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yy");
        Date date = parser.parse("04/05/18");
        System.out.println(date); // Fri May 04 00:00:00 BRT 2018

        // --------------------------------------------------------------------------
        parser = new SimpleDateFormat("dd/MM/yy HH:mm:ss.SSS");
        // setar data de referência para parse de ano com 2 dígitos
        // usar timestamp -999144000000, que corresponde a 1938-05-04T17:00-03:00
        parser.set2DigitYearStart(new Date(-999144000000L));
        // todas as datas devem estar entre 2DigitYearStart e (2DigitYearStart + 100 anos)
        // ou seja, entre 1938-05-04T17:00-03:00 (inclusive) e 2038-05-04T17:00-03:00 (exclusive)

        // único ano que faz esta data e hora estar no intervalo válido é 2038
        System.out.println(parser.parse("04/05/38 16:59:59.999")); // Tue May 04 16:59:59 BRT 2038

        // único ano que faz esta data e hora estar no intervalo válido é 1938
        System.out.println(parser.parse("04/05/38 17:00:00.000")); // Wed May 04 17:00:00 BRT 1938
        // quando não usamos set2DigitYearStart, é usado "a data atual menos 80 anos"
        // sendo que "data atual" é o instante em que o SimpleDateFormat é criado
        // por isso é recomendado setar o 2DigitYearStart para algum valor fixo, em vez de depender da data atual, que muda o tempo todo
    }

    /**
     * Data uma string com data (sem o ano) e dia da semana, encontrar um ano correspondente.
     * 
     * Compare com {@link Cap18Parsing#encontrarAnoParaDiaMesDiaDaSemana()} para ver a diferença entre as APIs
     */
    public static void parseDiaMesSemAno() throws ParseException {
        // locale português do Brasil
        Locale locale = new Locale("pt", "BR");

        // parser com dia da semana, dia/mês
        SimpleDateFormat parser = new SimpleDateFormat("EEE, dd/MM", locale);
        Date date = parser.parse("Qua, 04/05");
        // o ano é setado para 1970, e a data resultante corresponde a uma Segunda-feira - ignorando o dia da semana que estava na String de entrada ("Qua")
        System.out.println(date); // Mon May 04 00:00:00 BRT 1970

        // -----------------------------------------------------------------------------
        // a solução é fazer o parse do dia da semana separadamente e tentar encontrar um ano em que o dia 4 de maio seja uma Quarta-feira
        // quebrar a String em duas partes (separar o dia da semana do restante)
        String[] entrada = "Qua, 04/05".split(", ");

        // locale português do Brasil
        locale = new Locale("pt", "BR");
        // faz parsing do dia da semana (primeira parte da String original)
        date = new SimpleDateFormat("EEE", locale).parse(entrada[0]);
        // cria um Calendar para pegar o valor do dia da semana correspondente
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int diaDaSemana = cal.get(Calendar.DAY_OF_WEEK);

        // faz parsing do dia e mês (segunda parte da String original)
        parser = new SimpleDateFormat("dd/MM");
        date = parser.parse(entrada[1]);
        cal.setTime(date);

        // começar no ano atual (2018)
        int ano = Calendar.getInstance().get(Calendar.YEAR);
        do {
            // avançar o ano, até encontrar um 4 de maio que seja Quarta-feira
            cal.set(Calendar.YEAR, ano);
            ano++;
        } while (cal.get(Calendar.DAY_OF_WEEK) != diaDaSemana);
        System.out.println(cal.getTime()); // Wed May 04 00:00:00 BRT 2022
    }

    static void parseISO8601Java6() throws ParseException {
        String input = "2018-05-04T01:00Z";

        // A partir do Java 7 é possível fazer parsing da String acima usando new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");
        // Mas no Java <= 6 não existe o pattern X, então o jeito é quebrar a String
        // 2018-05-04T01:00
        String dataHora = input.substring(0, 16);
        // Z
        String offset = input.substring(16);

        // formato ISO 8601 (data e hora, sem offset)
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        // se o offset for numérico (+05:30, -03:00, etc), deve ter o prefixo "GMT"
        // se o offset for "Z", a String "GMTZ" é inválida, mas getTimeZone() não valida e retorna UTC
        // (um caso que "funciona" aproveitando o comportamento da API, que não valida o nome do timezone)
        iso8601Format.setTimeZone(TimeZone.getTimeZone("GMT" + offset));

        System.out.println(iso8601Format.parse(dataHora)); // Thu May 03 22:00:00 BRT 2018
        // a saída é diferente porque a entrada está em UTC, mas a data é impressa usando o timezone padrão da JVM
    }
}
