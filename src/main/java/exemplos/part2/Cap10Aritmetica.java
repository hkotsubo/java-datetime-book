package exemplos.part2;

import static exemplos.setup.Setup.clock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Cap10Aritmetica {

    public static void main(String[] args) throws ParseException {
        somarDuasHorasDate();
        somarDuasHorasCalendar();
        somarDiaHorarioDeVerao();
        somarUmMes();
        subtrairMes();
        parseDuracao();

        // --------------------------------------------
        // exemplos que não estão no livro
        duracaoISO8601();
    }

    static void somarDuasHorasDate() {
        // usando nossa "data atual" simulada
        Date dataAtual = new Date(clock().millis());
        System.out.println(dataAtual); // Fri May 04 17:00:00 BRT 2018

        // 2 horas em milissegundos
        long duasHorasEmMs = TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS);
        // somar 2 horas ao valor do timestamp
        dataAtual.setTime(dataAtual.getTime() + duasHorasEmMs);
        System.out.println(dataAtual); // Fri May 04 19:00:00 BRT 2018
    }

    static void somarDuasHorasCalendar() {
        Calendar dataAtual = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (se quiser a data atual de fato, remova esta linha)
        dataAtual.setTimeInMillis(clock().millis());
        System.out.println(dataAtual.getTime()); // Fri May 04 17:00:00 BRT 2018

        // somar 2 horas
        dataAtual.add(Calendar.HOUR_OF_DAY, 2);
        System.out.println(dataAtual.getTime()); // Fri May 04 19:00:00 BRT 2018
    }

    static void somarDiaHorarioDeVerao() {
        // cria um Calendar
        Calendar cal = Calendar.getInstance();

        // Um dia antes do horário de verão: 2017-10-14T10:00-03:00 (America/Sao_Paulo)
        cal.set(2017, Calendar.OCTOBER, 14, 10, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // antes de somar 1 dia
        long timestampAntes = cal.getTimeInMillis();
        System.out.println("antes: " + cal.getTime()); // antes: Sat Oct 14 10:00:00 BRT 2017

        // somar 1 dia
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long timestampDepois = cal.getTimeInMillis();
        System.out.println("depois:" + cal.getTime()); // depois:Sun Oct 15 10:00:00 BRST 2017

        // calcula a diferenca em horas
        long diferenca = timestampDepois - timestampAntes;
        // a diferenca está em milissegundos, converter para horas
        System.out.println(TimeUnit.HOURS.convert(diferenca, TimeUnit.MILLISECONDS)); // 23
    }

    static void somarUmMes() {
        // cria um Calendar
        Calendar cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (se quiser a data atual de fato, remova esta linha)
        cal.setTimeInMillis(clock().millis());

        // 1 de janeiro de 2018
        cal.set(2018, Calendar.JANUARY, 1);
        // somar 1 mês
        cal.add(Calendar.MONTH, 1);
        // 1 de fevereiro
        System.out.println(cal.getTime()); // Thu Feb 01 17:00:00 BRST 2018

        // ------------------------------------------------------
        cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (se quiser a data atual de fato, remova esta linha)
        cal.setTimeInMillis(clock().millis());

        // 31 de janeiro de 2018
        cal.set(2018, Calendar.JANUARY, 31);
        // somar 1 mês
        cal.add(Calendar.MONTH, 1);
        System.out.println(cal.getTime()); // Wed Feb 28 17:00:00 BRT 2018
    }

    static void subtrairMes() {
        Calendar cal = Calendar.getInstance();
        // testar dos dias 28 a 31 de janeiro
        for (int dia = 28; dia <= 31; dia++) {
            cal.set(2018, Calendar.JANUARY, dia);

            // somar 1 mês
            cal.add(Calendar.MONTH, 1);
            System.out.println(cal.getTime()); // 28 de fevereiro

            // subtrair 1 mês
            cal.add(Calendar.MONTH, -1);
            System.out.println(cal.getTime()); // 28 de janeiro
            // mesmo com o dia inicial sendo 29, 30, ou 31, o resultado de somar 1 mês e depois subtrair 1 mês é 28 de janeiro
        }
    }

    // Parsing de duração, às vezes até funciona, mas não é o jeito certo
    // Nas APIs nativas do Java, somente o java.time tem classes específicas para tratar durações
    static void parseDuracao() throws ParseException {
        // ERRADO: fazer parsing de uma duração, tratando-a como uma data
        SimpleDateFormat parser = new SimpleDateFormat("'PT'H'H'm'M'");
        // usar UTC, para evitar problemas com horário de verão
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        // parse de uma duração (10 horas e 20 minutos)
        Date date = parser.parse("PT10H20M");
        // "duração" em milissegundos
        long duracao = date.getTime();
        System.out.println(duracao); // 37200000 - por coincidência, está correto, mas nem sempre funciona

        // ---------------------------------------------------------------------
        // ERRADO: tentando fazer parsing de uma duração, tratando-a como uma data
        parser = new SimpleDateFormat("'P'M'M'");
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        // duração de 1 mês
        date = parser.parse("P1M");
        duracao = date.getTime(); // qual será o valor da duração?
        System.out.println(duracao); // zero (totalmente errado, já que a entrada corresponde a uma duração de 1 mês)

        // Isso acontece porque SimpleDateFormat só sabe lidar com datas. Tratar os dados como uma duração pode até funcionar às vezes por coincidência, mas nem
        // sempre funcionará. A solução é fazer o parsing manualmente (String.split/substring/regex) ou usar uma API com suporte a durações, como o java.time
    }

    // converter uma duração em milissegundos para o formato ISO8601
    static void duracaoISO8601() {
        // duracao total em milisegundos
        long millis = 21878400000L;

        // quantidade total de segundos
        long secs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
        // descontar a quantidade de segundos do valor dos milissegundos
        millis -= TimeUnit.MILLISECONDS.convert(secs, TimeUnit.SECONDS);

        // quantidade total de minutos
        long mins = TimeUnit.MINUTES.convert(secs, TimeUnit.SECONDS);
        // descontar a quantidade de minutos do valor dos segundos
        secs -= TimeUnit.SECONDS.convert(mins, TimeUnit.MINUTES);

        // quantidade total de horas
        long horas = TimeUnit.HOURS.convert(mins, TimeUnit.MINUTES);
        // descontar a quantidade de horas do valor dos minutos
        mins -= TimeUnit.MINUTES.convert(horas, TimeUnit.HOURS);

        // quantidade total de dias
        long dias = TimeUnit.DAYS.convert(horas, TimeUnit.HOURS);
        // descontar a quantidade de dias do total de horas
        horas -= TimeUnit.HOURS.convert(dias, TimeUnit.DAYS);

        // quantidade total de meses
        long meses = dias / 30; // considerar todo mês com 30 dias
        // descontar a quantidade de meses do total de dias
        dias -= (meses * 30);

        System.out.printf("P%dM%dDT%dH%dM%d.%dS", meses, dias, horas, mins, secs, millis); // P8M13DT5H20M0.0S
        // o problema é que eu considerei todos os meses com 30 dias, o que é uma aproximação arbitrária
        // dependendo das datas envolvidas, 1 mês pode ter de 28 a 31 dias, porém a API não fornece um mecanismo mais preciso para calcularmos corretamente
    }
}
