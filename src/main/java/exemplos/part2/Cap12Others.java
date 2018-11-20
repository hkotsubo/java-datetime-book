package exemplos.part2;

import static exemplos.setup.Setup.clock;
import static exemplos.setup.Setup.setup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import exemplos.part3.Cap19TestesOutrosCasos;
import exemplos.setup.Setup;

public class Cap12Others {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) throws ParseException {
        getInfoTimezone();
        encontrarTimezonesPorOffset();
        encontrarTimezonesPorAbreviacao();
        proximaSexta();
        ultimoDiaDoMes();
        terceiraSegundaFeiraDoMes();
    }

    static void getInfoTimezone() {
        TimeZone zone = TimeZone.getTimeZone("America/Sao_Paulo");
        System.out.println(zone.getID()); // America/Sao_Paulo
        // mostra o DisplayName no locale padrão da JVM (pt_BR)
        System.out.println(zone.getDisplayName()); // Fuso horário de Brasília
        // mostra o DisplayName em inglês
        System.out.println(zone.getDisplayName(Locale.US)); // Brasilia Time

        // obter o offset no instante "atual" (usar nosso timestamp simulado)
        System.out.println(zone.getOffset(clock().millis())); // -10800000 (milissegundos, que corresponde a -3 horas -> offset -03:00)

        // -------------------------------------------------------------
        // usando nossa "data atual" simulada
        Date dataAtual = new Date(clock().millis());
        // verificar se a data atual está em horário de verão
        System.out.println(zone.inDaylightTime(dataAtual)); // false

        // quantidade de milissegundos adicionado à hora local quando é horário de verão
        System.out.println(zone.getDSTSavings()); // 3600000 (corresponde a uma hora)
    }

    /**
     * Vários timezones usam o mesmo offset ou a mesma abreviação, e estes podem mudar dependendo da data e hora. Por exemplo, America/Sao_Paulo usa o offset
     * -03:00 e abreviação BRT, mas durante o horário de verão muda para -02:00 e BRST. Mas ele não é o único timezone que usa os offsets -03:00 e -02:00, então
     * apenas com a informação do offset não temos como afirmar com certeza que se trata deste timezone.
     * 
     * Abreviações também são problemáticas. IST, por exemplo, é usada na Índia, Irlanda e Israel, cada um com um offset diferente (e na Irlanda é só durante o
     * horário de verão).
     * 
     * Por isso, dado um offset ou abreviação, não há como obter um único timezone. O melhor que podemos obter é uma lista de timezones, e mesmo esta lista pode
     * variar conforme a data e hora que escolhemos como referência. Se o offset for -02:00 e a data for em dezembro, provavelmente America/Sao_Paulo estará na
     * lista, já que em dezembro este timezone está em horário de verão (exceto se consultarmos em um ano que não teve horário de verão - como nos anos 70, por
     * exemplo - e portanto o offset era -03:00).
     * 
     * Os 2 métodos abaixo (encontrarTimezonesPorOffset() e encontrarTimezonesPorAbreviacao()) usam como referência a data "atual" simulada da classe
     * {@link Setup} e verificam quais os timezones que nesta data usam determinado offset ou abreviação. Compare com a forma que o java.time faz a mesma coisa
     * em {@link Cap19TestesOutrosCasos#encontrarTimezonesPorOffsetOuAbreviacao()}
     */
    public static void encontrarTimezonesPorOffset() {
        // offset +02:00 em milissegundos
        long offset = TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS);
        // timestamp a ser usado como referência (equivalente a 2018-05-04T17:00-03:00 - nosso instante "atual" simulado)
        long referencia = clock().millis();
        // Set para guardar os resultados
        Set<String> timezones = new HashSet<>();
        // procurar todos os timezones que usam o offset na data de referência
        for (String id : TimeZone.getAvailableIDs()) {
            // obtém o offset usado neste timezone, na data de referência
            int offsetUsado = TimeZone.getTimeZone(id).getOffset(referencia);
            if (offset == offsetUsado) {
                timezones.add(id);
            }
        }
        // saída depende da versão do TZDB instalada na JVM, mas deve imprimir pelo menos uns 50 timezones
        System.out.println(timezones);
    }

    public static void encontrarTimezonesPorAbreviacao() {
        // abreviação
        String abrev = "EST";
        // data a ser usada como referência (equivalente a 2018-05-04T17:00-03:00 - nosso instante "atual" simulado)
        Date referencia = new Date(clock().millis());
        // Set para guardar os resultados
        Set<String> timezones = new HashSet<>();
        // verificar todos os locales (pois a abreviação é locale sensitive)
        for (Locale locale : Locale.getAvailableLocales()) {
            // pattern "z" para a abreviação do timezone
            SimpleDateFormat formatter = new SimpleDateFormat("z", locale);
            // verificar todos os timezones
            for (String id : TimeZone.getAvailableIDs()) {
                // obtém a abreviação usada por este timezone, na data de referência
                formatter.setTimeZone(TimeZone.getTimeZone(id));
                if (abrev.equals(formatter.format(referencia))) {
                    timezones.add(id);
                }
            }
        }
        // saída depende da versão do TZDB instalada na JVM, mas com certeza terá mais de um
        System.out.println(timezones);
    }

    static void proximaSexta() {
        Calendar cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (2018-05-04T17:00-03:00)
        cal.setTimeInMillis(clock().millis());
        // 27 de dezembro de 2011
        cal.set(2011, Calendar.DECEMBER, 27);

        // vou somando um dia, até encontrar a próxima Sexta-feira
        do {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY);
        System.out.println(cal.getTime()); // Fri Dec 30 17:00:00 BRST 2011 <- o horário original (17:00) é preservado

        // -------------------------------------------------------------
        // rodar de novo, mas mudando o timezone padrão
        // No timezone Pacific/Apia (Samoa), o dia 30/12/2011 foi "pulado" e a próxima sexta-feira será 6 de janeiro de 2012
        // https://www.telegraph.co.uk/news/worldnews/australiaandthepacific/samoa/8980665/Samoa-prepares-to-skip-Dec-30.html
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Apia"));
        cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (2018-05-04T17:00-03:00)
        cal.setTimeInMillis(clock().millis());
        // 27 de dezembro de 2011
        cal.set(2011, Calendar.DECEMBER, 27);

        // vou somando um dia, até encontrar a próxima Sexta-feira
        do {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY);
        System.out.println(cal.getTime()); // Fri Jan 06 09:00:00 WSDT 2012

        // -------------------------------------------------------------
        // rodar de novo, mas mudando o timezone padrão
        // voltar para America/Sao_Paulo e ver o que acontece quando há mudança de horário de verão
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        // 14 de outubro de 2017, à meia-noite - timezone padrão é America/Sao_Paulo
        cal = Calendar.getInstance();
        cal.set(2017, Calendar.OCTOBER, 14, 0, 0, 0);
        // vou somando um dia, até encontrar a próxima Sexta-feira
        do {
            // quando começa o horário de verão, à meia-noite o relógio é adiantado para 01:00
            // e essa mudança de horário permanece nas demais datas
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY);
        // o resultado tem horário igual a 01:00
        System.out.println(cal.getTime()); // Fri Oct 20 01:00:00 BRST 2017

        // voltar config default (para não impactar os outros métodos)
        setup();
    }

    static void ultimoDiaDoMes() {
        // criar uma data em fevereiro de 2018
        Calendar cal = Calendar.getInstance();
        cal.set(2018, Calendar.FEBRUARY, 16, 0, 0, 0);

        // mudar para o último dia do mês (28 de fevereiro de 2018)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        System.out.println(cal.getTime()); // Wed Feb 28 00:00:00 BRT 2018
    }

    static void terceiraSegundaFeiraDoMes() {
        Calendar cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (2018-05-04T17:00-03:00)
        cal.setTimeInMillis(clock().millis());

        // primeiro mudamos para a terceira semana
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 3);
        // depois mudamos para a Segunda-feira desta semana
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        System.out.println(cal.getTime()); // Mon May 21 17:00:00 BRT 2018
    }
}