package outros;

import java.util.Date;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * Exemplos com a biblioteca Threeten Backport (https://www.threeten.org/threetenbp/).
 * 
 * Esta biblioteca é um excelente backport para as classes do Java 8, e pode ser usada com JDK 6 e 7. Ela possui a maioria das funcionalidades do java.time e
 * facilita muito uma futura migração.
 *
 * A classes possuem os mesmos nomes e métodos, com funcionalidades muito próximas (esta classe possui alguns exemplos de uso). A principal diferença é o nome
 * do pacote em que estão as classes: no JDK 8 é java.time e no backport é org.threeten.bp
 * 
 * Se você está usando JDK <=5 não é possível usar o backport, mas neste caso eu recomendaria o Joda-Time, que tem vários exemplos de uso na classe
 * {@link JodaTimeExemplos}
 */
public class ThreetenBackportExemplos {

    public static void main(String[] args) {
        funcionamentoBasico();
        converterParaDate();
    }

    static void funcionamentoBasico() {
        // dia de hoje (usa o relógio do sistema e o timezone default da JVM)
        LocalDate dataAtual = LocalDate.now();

        LocalDateTime dateTime = LocalDate
            // 20 de novembro de 2018
            .of(2018, 11, 20)
            // somar 1 dia
            .plusDays(1)
            // setar horário para meio-dia
            .atTime(LocalTime.NOON);
        System.out.println(dateTime); // 2018-11-21T12:00 <- saída no formato ISO 8601

        // O funcionamento básico é o mesmo do java.time (veja o pacote exemplos.part3 para mais detalhes). Mas há algumas diferenças para o Java 8
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // No Java 8 é possível usar method reference (DayOfWeek::from) como uma TemporalQuery (mas no JDK 6 e 7 a linha abaixo não compila)
        // DayOfWeek dow = fmt.parse("10/02/2018", DayOfWeek::from);
        // A alternativa é usar as respectivas constantes criadas para serem usadas no lugar do method reference
        DayOfWeek dow = fmt.parse("10/02/2018", DayOfWeek.FROM);
        System.out.println(dow); // SATURDAY
        // Em todas as classes que possuem um método from() foi adicionada a constante FROM
    }

    // conversões para java.util.Date, Calendar e java.sql.Date/Time/Timestamp, entre outras
    static void converterParaDate() {
        // No Java 8 a classe java.util.Date possui o método toInstant(), que converte para um java.time.Instant, e Date.from(Instant) que faz o inverso
        // No Java 6 e 7 estes métodos não existem, então as conversões são feitas pela classe org.threeten.bp.DateTimeUtils
        // cria um java.util.Date
        Date date = new Date();
        // converte para org.threeten.bp.Instant
        Instant instant = DateTimeUtils.toInstant(date);
        // converte de volta para java.util.Date
        date = DateTimeUtils.toDate(instant);

        // o detalhe é que o backport possui precisão de nanossegundos (9 casas decimais), enquanto a precisão de Date é de milissegundos (3 casas decimais)
        instant = Instant.parse("2018-01-01T10:20:30.123456789Z"); // fração de segundos com 9 casas decimais: 123456789
        System.out.println(instant); // 2018-01-01T10:20:30.123456789Z
        // converte para Date (somente os 3 primeiros dígitos da fração de segundo são mantidos)
        date = DateTimeUtils.toDate(instant);
        // converte para Instant (os dígitos 456789 já foram perdidos ao converter para Date, então eles não estarão mais aqui)
        instant = DateTimeUtils.toInstant(date);
        System.out.println(instant); // 2018-01-01T10:20:30.123Z

        /*
         * Verifique a documentação da classe (https://www.threeten.org/threetenbp/apidocs/org/threeten/bp/DateTimeUtils.html) para ver todos os métodos de
         * conversão. Há conversões de/para Calendar, java.sql.Date/Time/Timestamp, java.util.TimeZone, etc. Todas feitas para emular os respectivos métodos
         * adicionados no Java 8, que fazem as conversões destas classes de/para o java.time
         */
    }
}
