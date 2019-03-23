package exemplos.part2;

import static exemplos.setup.Setup.setup;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Cap11JavaSql {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    // "normaliza" um timestamp, setando a hora para 00:00:00.000 ou o dia para 1970-01-01
    static long normalizar(long timestamp, boolean mudarHorario) {
        // criar Calendar com o valor do timestamp
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        if (mudarHorario) {
            // setar horário para meia-noite
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        } else {
            cal.set(Calendar.YEAR, 1970);
            cal.set(Calendar.MONDAY, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }

        // ambos os casos ignoram os milissegundos
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    public static void main(String[] args) throws ParseException {
        dataHoraMudaConformeTimezone();
        dataPossuiHorario();
        comparar();
        compararNormalizado();
        parse();
        parseTimestamp();
        parseTimestampFracaoSegundosVariavel();
    }

    static void dataHoraMudaConformeTimezone() {
        // timestamp 1525464000000 = 2018-05-04T17:00-03:00 (America/Sao_Paulo)
        java.sql.Date date = new java.sql.Date(1525464000000L);
        Time time = new Time(1525464000000L);
        // 2018-05-04T17:00-03:00
        Timestamp ts = new Timestamp(1525464000000L);

        // imprimir a data e hora usando o timezone padrão
        System.out.println(date); // 2018-05-04
        System.out.println(time); // 17:00:00
        System.out.println(ts); // 2018-05-04 17:00:00.0
        // mudar o timezone padrão
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        System.out.println(date); // 2018-05-05
        System.out.println(time); // 05:00:00
        System.out.println(ts); // 2018-05-05 05:00:00.0

        // voltar config default (para não impactar os outros métodos)
        setup();
    }

    static void dataPossuiHorario() {
        // 1525464000000 = 2018-05-04T17:00-03:00 (America/Sao_Paulo)
        java.sql.Date sqlDate = new java.sql.Date(1525464000000L);

        try {
            sqlDate.getHours(); // método deprecated e lança exceção
        } catch (IllegalArgumentException e) {
            System.out.println("Não pode obter horas de java.sql.Date");
        }

        // na verdade, há como obter as horas
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        System.out.println(formatter.format(sqlDate)); // 04/05/2018 17:00

        // ------------------------------------------------------------
        // o mesmo vale para java.sql.Time
        Time time = new Time(1525464000000L);
        try {
            time.getMonth(); // método deprecated e lança exceção
        } catch (IllegalArgumentException e) {
            System.out.println("Não pode obter mês de java.sql.Time");
        }

        // na verdade, há como obter o mês (e demais campos de data)
        System.out.println(formatter.format(sqlDate)); // 04/05/2018 17:00

        // ou seja, os getters escondem artificialmente as informações - uma pequena "gambiarra", necessária porque estas classes herdam de java.util.Date
    }

    static void comparar() {
        // 1525464000000 = 2018-05-04T17:00-03:00
        java.sql.Date sqlDate = new java.sql.Date(1525464000000L);
        // 1525464000001 = 2018-05-04T17:00:00.001-03:00
        java.sql.Date sqlDate2 = new java.sql.Date(1525464000001L);
        System.out.println(sqlDate); // 2018-05-04
        System.out.println(sqlDate2); // 2018-05-04
        System.out.println("iguais? " + sqlDate.equals(sqlDate2)); // iguais? false
        System.out.println("sqlDate antes de sqlDate2? " + sqlDate.before(sqlDate2)); // sqlDate antes de sqlDate2? true

        // 1525464000000 = 2018-05-04T17:00-03:00
        Time time1 = new Time(1525464000000L);
        // 1525464000001 = 2018-05-04T17:00:00.001-03:00
        Time time2 = new Time(1525464000001L);
        System.out.println(time1); // 17:00:00
        System.out.println(time2); // 17:00:00
        System.out.println(time1.equals(time2)); // false
        System.out.println(time1.before(time2)); // true
    }

    static void compararNormalizado() {
        // 1525464000000 = 2018-05-04T17:00-03:00 (normalizar para 2018-05-04T00:00-03:00)
        java.sql.Date sqlDate = new java.sql.Date(normalizar(1525464000000L, true));
        // 1525464000001 = 2018-05-04T17:00:00.001-03:00 (normalizar para 2018-05-04T00:00-03:00)
        java.sql.Date sqlDate2 = new java.sql.Date(normalizar(1525464000001L, true));
        System.out.println(sqlDate); // 2018-05-04
        System.out.println(sqlDate2); // 2018-05-04
        System.out.println("iguais? " + sqlDate.equals(sqlDate2)); // iguais? true
        System.out.println("sqlDate antes de sqlDate2? " + sqlDate.before(sqlDate2)); // sqlDate antes de sqlDate2? false

        // 1525464000001 = 2018-05-04T17:00:00.001-03:00 (normalizar para 1970-01-01T17:00:00-03:00)
        Time time1 = new Time(normalizar(1525464000000L, false));
        // 1525464000001 = 2018-05-04T17:00:00.001-03:00 (normalizar para 1970-01-01T17:00:00.001-03:00)
        Time time2 = new Time(normalizar(1525464000001L, false));
        System.out.println(time1); // 17:00:00
        System.out.println(time2); // 17:00:00
        System.out.println(time1.equals(time2)); // true
        System.out.println(time1.before(time2)); // false
    }

    static void parse() throws ParseException {
        // valueOf recebe Strings no formato ISO 8601
        java.sql.Date sqlDate = java.sql.Date.valueOf("2018-05-04");
        Time time = Time.valueOf("10:00:00");
        System.out.println(sqlDate); // 2018-05-04
        System.out.println(time); // 10:00:00
        // lembrando que os valores são normalizados: sqlDate tem horário igual a meia-noite e time tem data igual a 1970-01-01 (ambos no timezone padrão da
        // JVM)

        // parsing de String em formato diferente do ISO 8601
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
        // parse() retorna sempre um java.util.Date
        java.util.Date date = parser.parse("04/05/2018");
        // passar o timestamp para java.sql.Date
        sqlDate = new java.sql.Date(date.getTime());
        System.out.println(sqlDate); // 2018-05-04
        // timestamp já está normalizado no timezone padrão da JVM
        // porém, é possível mudar o timezone usando parser.setTimeZone()
    }

    /**
     * As frações de segundo são tratadas separadamente, já que SimpleDateFormat não consegue fazer o parsing corretamente quando há mais de 3 casas decimais
     * (conforme visto em {@link Cap08e09FormatacaoParsing#parseFracaoSegundos()})
     */
    static void parseTimestamp() throws ParseException {
        Timestamp ts = Timestamp.valueOf("2018-05-04 10:30:45.123456789");
        System.out.println(ts); // 2018-05-04 10:30:45.123456789
        System.out.println(ts.getTime()); // 1525397445123
        System.out.println(ts.getNanos()); // 123456789

        String input = "04/05/2018 10:30:45.123456789";
        // separar frações de segundo do restante da data/hora
        String[] dadosSeparados = input.split("\\.");
        // fazer parsing da data/hora (sem os nanossegundos)
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = parser.parse(dadosSeparados[0]);
        // usar o valor do timestamp no construtor
        ts = new Timestamp(date.getTime());
        // setar os nanossegundos
        ts.setNanos(Integer.parseInt(dadosSeparados[1]));
        System.out.println(ts); // 2018-05-04 10:30:45.123456789
        System.out.println(ts.getTime()); // 1525440645123
        System.out.println(ts.getNanos()); // 123456789
    }

    // exemplo que não tem no livro, trata fração de segundos com qualquer quantidade de dígitos
    static void parseTimestampFracaoSegundosVariavel() throws ParseException {
        String input = "04/05/2018 10:30:45.12345";
        // separar frações de segundo do restante da data/hora
        String[] dadosSeparados = input.split("\\.");
        // fazer parsing da data/hora (sem os nanossegundos)
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = parser.parse(dadosSeparados[0]);
        // usar o valor do timestamp no construtor
        Timestamp ts = new Timestamp(date.getTime());
        // setar os nanossegundos (preencher a string com zeros à direita para completar os 9 dígitos)
        ts.setNanos(Integer.parseInt(String.format("%-9s", dadosSeparados[1]).replaceAll(" ", "0")));
        System.out.println(ts); // 2018-05-04 10:30:45.12345
        System.out.println(ts.getTime()); // 1525440645123
        System.out.println(ts.getNanos()); // 123450000
    }
}