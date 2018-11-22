package exemplos.part3;

import static exemplos.setup.Setup.setup;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.UnsupportedTemporalTypeException;

public class Cap16AritmeticaDatas {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) {
        somaSubtracao();
        somarQualquerUnidadeDeTempo();
        testarPeriod();
        somarPeriods();
        testarDuration();
        somarDuracoesDatas();
        quantasHorasTemUmDia();
        diferencaEntreDatas();
        aritmeticaDeDatasEstranha();
    }

    static void somaSubtracao() {
        // 2018-05-04T17:00
        LocalDateTime dataHora = LocalDateTime.of(2018, 5, 4, 17, 0);

        // somar 1 dia
        LocalDateTime diaSeguinte = dataHora.plusDays(1);
        System.out.println(diaSeguinte); // 2018-05-05T17:00

        // subtrair 3 horas
        LocalDateTime tresHorasAntes = dataHora.minusHours(3);
        System.out.println(tresHorasAntes); // 2018-05-04T14:00

        // podemos somar valores negativos (o que equivale à subtração)
        System.out.println(dataHora.plusHours(-2).equals(dataHora.minusHours(2))); // true

        // podem ser feitos ajustes, dependendo do caso
        // 2018-01-31
        LocalDate jan = LocalDate.of(2018, 1, 31);
        LocalDate fev = jan.plusMonths(1);
        System.out.println(fev); // 2018-02-28
        // Ao somar 1 mês a 31 de janeiro de 2018, o resultado seria 31 de fevereiro. Mas como fevereiro não tem 31 dias, o resultado é ajustado para o último
        // dia válido deste mês. Por isso o valor final é 28 de fevereiro de 2018

        // ajustes devido ao timezone
        // 2017-10-14T10:00-03:00[America/Sao_Paulo] (um dia antes de começar o horário de verão)
        ZonedDateTime z = ZonedDateTime.of(2017, 10, 14, 10, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
        // somar 1 dia, resultado é o mesmo horário do dia seguinte (offset muda porque começou horário de verão)
        System.out.println(z.plusDays(1)); // 2017-10-15T10:00-02:00[America/Sao_Paulo]
        // somar 24 horas, resultado não é o mesmo horário, porque quando começa o horário de verão, adianta-se o relógio 1 hora (ou seja, 1 hora é "pulada")
        System.out.println(z.plusHours(24)); // 2017-10-15T11:00-02:00[America/Sao_Paulo]
    }

    static void somarQualquerUnidadeDeTempo() {
        // 10:30:00.123456789
        LocalTime hora = LocalTime.of(10, 30, 0, 123456789);
        // somar 200 milissegundos (é o mesmo que hora.plusNanos(200000000), mas sem precisar fazer a conta para converter milissegundos para nanossegundos)
        hora = hora.plus(200, ChronoUnit.MILLIS);
        System.out.println(hora); // 10:30:00.323456789

        // não some unidades que a classe não suporta
        try {
            LocalTime lt = LocalTime.now().plus(1, ChronoUnit.MONTHS);
            System.out.println(lt); // não será executado, pois LocalTime não suporta ChronoUnit.MONTHS e lança exceção
        } catch (UnsupportedTemporalTypeException e) {
            System.out.println(e.getMessage()); // Unsupported unit: Months
        }
        // é melhor verificar antes se a classe suporta uma unidade antes de somá-la (ou subtraí-la)
        hora = LocalTime.now();
        if (hora.isSupported(ChronoUnit.MONTHS)) {
            // não entra neste if, pois LocalTime não suporta ChronoUnit.MONTHS e isSupported retorna false
            hora = hora.plus(1, ChronoUnit.MONTHS);
        }
    }

    static void testarPeriod() {
        // 2 jeitos de criar um período de "1 ano, 2 meses e 20 dias"
        System.out.println(Period.parse("P1Y2M20D")); // P1Y2M20D
        System.out.println(Period.of(1, 2, 20)); // P1Y2M20D

        // semanas são automaticamente convertida em dias
        System.out.println(Period.ofWeeks(2)); // P14D

        // Period tem somente anos, meses e dias
        try {
            // período com campo de horas lança exceção
            Period.parse("P1DT1H");
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage()); // Text cannot be parsed to a Period
        }
    }

    static void somarPeriods() {
        // somar 40 dias ao periodo de 1 mês
        Period period = Period.ofMonths(1);
        period = period.plusDays(40);
        System.out.println(period); // P1M40D

        // 1 ano e 3 meses
        period = Period.parse("P1Y3M");
        // somar um Period de 5 meses e 10 dias
        period = period.plus(Period.parse("P5M10D"));
        System.out.println(period); // P1Y8M10D

        // obter os campos separadamente
        System.out.println(period.getYears()); // 1
        System.out.println(period.getMonths()); // 8
        System.out.println(period.getDays()); // 10
    }

    static void testarDuration() {
        // 2 modos de criar uma duração de 10 minutos
        System.out.println(Duration.parse("PT10M")); // PT10M
        System.out.println(Duration.ofMinutes(10)); // PT10M

        // várias formas de somar valores a um Duration
        Duration duracao = Duration
            .ofSeconds(3) // 3 segundos
            // somar 10 minutos (usando o valor numérico)
            .plusMinutes(10)
            // somar 0.5 segundos (usando outro Duration)
            .plus(Duration.ofMillis(500))
            // somar 3 horas (usando ChronoUnit)
            .plus(3, ChronoUnit.HOURS);
        System.out.println(duracao); // PT3H10M3.5S
        // Internamente, Duration só guarda segundos e nanossegundos
        System.out.println(duracao.getSeconds()); // 11403
        System.out.println(duracao.getNano()); // 500000000
        // Ao imprimir o Duration usando println, o método toString() converte para PT3H10M3.5S (mas internamente, há somente segundos e nanossegundos)

        // *****************************************************
        // Obter os valores numéricos das horas e minutos

        // jeito errado
        System.out.println(duracao.toHours()); // 3 (certo)
        System.out.println(duracao.toMinutes()); // 190 (errado)
        // toMinutes converte a duração em minutos -> PT3H10M3.5S = 3 horas, 10 minutos e 3.5 segundos = 190 minutos (arredondado, desconsidera os segundos)

        // jeito certo: é necessário fazer as contas manualmente
        // obter o total de segundos
        long segundos = duracao.getSeconds();
        // obter a quantidade de dias
        long dias = segundos / (24 * 3600);
        segundos -= dias * 24 * 3600; // descontar os dias do total
        // repetir o mesmo para horas e minutos
        long horas = segundos / 3600;
        segundos -= horas * 3600;
        long minutos = segundos / 60;
        segundos -= minutos * 60;
        // 0 dias, 3 horas, 10 minutos, 3.500000000 segundos
        System.out.format("%d dias, %d horas, %d minutos, %d.%d segundos\n", dias, horas, minutos, segundos, duracao.getNano());
        // A partir do Java 9, há os métodos toMinutesPart(), toHoursPart(), etc, e esses cálculos não são mais necessários
        // veja a documentação -> https://docs.oracle.com/javase/9/docs/api/java/time/Duration.html
    }

    static void somarDuracoesDatas() {
        // 3 horas, 10 minutos e 3.5 segundos
        Duration duracao = Duration.parse("PT3H10M3.5S");
        // 1 mês e 10 dias
        Period periodo = Period.parse("P1M10D");
        // 2018-05-04T17:00
        LocalDateTime dataHora = LocalDateTime.of(2018, 5, 4, 17, 0);
        dataHora = dataHora
            // somar o Duration -> 2018-05-04T20:10:03.500
            .plus(duracao)
            // subtrair o Period -> 2018-03-25T20:10:03.500
            .minus(periodo);
        System.out.println(dataHora); // 2018-03-25T20:10:03.500

        try {
            // LocalTime só tem campos de horário (hora, minuto, segundo e nanossegundo)
            // Period só tem anos, meses e dias (campos não suportados por LocalTime)
            LocalTime.now().plus(periodo); // por isso esta soma lança exceção
        } catch (UnsupportedTemporalTypeException e) {
            System.out.println(e.getMessage()); // Unsupported unit: Months
        }
        // verificar se uma classe suporta um Duration (também funciona com Period, pois também tem o método getUnits())
        boolean suportaDuracao = Duration
            .parse("PT1H30M4.5S")
            // obter as TemporalUnits da duracao
            .getUnits()
            // verificar se todas são suportadas pela data
            .stream()
            .allMatch(dataHora::isSupported);
        System.out.println(suportaDuracao); // true
    }

    static void quantasHorasTemUmDia() {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        // 2017-10-14T10:00-03:00[America/Sao_Paulo] (um dia antes de começar o horário de verão)
        ZonedDateTime z = ZonedDateTime.of(2017, 10, 14, 10, 0, 0, 0, zone);

        // somar Period de 1 dia é o mesmo que plusDays(1) -> resulta na mesma hora do dia seguinte (offset é ajustado)
        ZonedDateTime mais1dia = z.plus(Period.ofDays(1));
        System.out.println(mais1dia); // 2017-10-15T10:00-02:00[America/Sao_Paulo]
        // diferença em horas
        System.out.println(ChronoUnit.HOURS.between(z, mais1dia)); // 23

        // somar Duration de 1 dia é o mesmo que plusHours(24) -> o resultado não é na mesma hora devido ao horário de verão que pulou 1 hora
        ZonedDateTime mais24horas = z.plus(Duration.ofDays(1));
        System.out.println(mais24horas); // 2017-10-15T11:00-02:00[America/Sao_Paulo]
        // diferença em horas
        System.out.println(ChronoUnit.HOURS.between(z, mais24horas)); // 24
    }

    static void diferencaEntreDatas() {
        // obter diferença como um valor numérico
        LocalDate dtInicio = LocalDate.of(2018, 1, 1);
        LocalDate dtFim = LocalDate.of(2018, 1, 10);
        // quantos dias entre 1 e 10 de janeiro
        long dias = ChronoUnit.DAYS.between(dtInicio, dtFim);
        System.out.println(dias); // 9

        // between() sempre arredonda para baixo
        // 2018-01-01T11:00
        LocalDateTime inicio = LocalDateTime.of(2018, 1, 1, 11, 0);
        // 2018-01-02T10:59:59.999999999 (falta 1 nanossegundo para completar 1 dia, mesmo assim, between() retorna zero)
        LocalDateTime fim = LocalDateTime.of(2018, 1, 2, 10, 59, 59, 999999999);
        dias = ChronoUnit.DAYS.between(inicio, fim);
        System.out.println(dias); // 0

        // calcular diferença, ignorando o horário
        dias = ChronoUnit.DAYS.between(inicio.toLocalDate(), fim.toLocalDate());
        System.out.println(dias); // 1

        try {
            // LocalDate não tem minutos, então MINUTES.between lança exceção
            long minutos = ChronoUnit.MINUTES.between(dtInicio, dtFim);
            System.out.println(minutos); // não será executado
        } catch (UnsupportedTemporalTypeException e) {
            System.out.println(e.getMessage()); // Unsupported unit: Minutes
        }

        // obter diferença como Period ou Duration
        // 2018-01-01T11:00
        inicio = LocalDateTime.of(2018, 1, 1, 11, 0);
        // 2018-02-10T10:00
        fim = LocalDateTime.of(2018, 2, 10, 10, 0);
        // Period só tem anos, meses e dias, e o método between só aceita LocalDate (portanto, não considera os horários)
        Period periodo = Period.between(inicio.toLocalDate(), fim.toLocalDate());
        System.out.println(periodo); // P1M9D - 1 mês e 9 dias (diferença entre as datas, sem considerar os horários)
        // Duration considera os horários, e converte a diferença para o total de segundos e nanossegundos
        Duration duracao = Duration.between(inicio, fim);
        System.out.println(duracao); // PT959H - 959 horas (diferença considerando os horários)

        // a diferença pode ser negativa
        dtInicio = LocalDate.of(2018, 1, 1);
        dtFim = LocalDate.of(2018, 1, 10);
        // quantos dias entre 10 e 1 de janeiro
        dias = ChronoUnit.DAYS.between(dtFim, dtInicio);
        System.out.println(dias); // -9
        // período de -9 dias (P-9D)
        periodo = Period.between(dtFim, dtInicio);
        System.out.println(periodo); // P-9D

        // se você só quiser o valor absoluto, pode inverter o sinal
        if (periodo.isNegative()) {
            periodo = periodo.negated();
            System.out.println(periodo); // P9D
        }
    }

    static void aritmeticaDeDatasEstranha() {
        // 2016-02-29
        LocalDate data = LocalDate.of(2016, 2, 29);
        // somar 1 ano, o resultado seria 29 de fevereiro, mas como 2017 não é bissexto, o dia é ajustado para 2017-02-28
        LocalDate umAnoDepois = data.plusYears(1);

        // se eu somei 1 ano, as diferenças abaixo devem ser de 1 ano, certo? Errado!
        System.out.println(ChronoUnit.YEARS.between(data, umAnoDepois)); // 0
        System.out.println(Period.between(data, umAnoDepois)); // P11M30D
        // Mas o resultado não deveria ser 1 ano? Depende. Basta imaginar uma pessoa que nasceu em 29 de fevereiro de 2016. Em 28 de fevereiro de 2017 ela já
        // terá completado 1 ano? A API entende que não (e não há uma regra oficial para isso, cada API implementa de um jeito)
    }
}
