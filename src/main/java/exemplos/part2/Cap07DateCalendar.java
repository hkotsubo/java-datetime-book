package exemplos.part2;

import static exemplos.setup.Setup.clock;
import static exemplos.setup.Setup.setup;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Cap07DateCalendar {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) throws ParseException {
        dataAtual();
        mudarTimezonePadrao();
        criarDataJaneiro();
        criarDataJaneiroCalendar();
        setarHoraAmPm();
        obterCamposCalendar();
        exemploSetarCamposComParamsErrados();
        mudarTimezoneDoCalendar();
        timezoneNaoValidaNome();
        calendarLeniente();
        calendarNaoLeniente();
    }

    static void dataAtual() {
        // Date contendo o instante atual
        Date agora = new Date();
        // usando nossa "data atual" simulada
        agora = new Date(clock().millis());

        // imprime a data e hora no timezone padrão da JVM (America/Sao_Paulo)
        System.out.println(agora); // Fri May 04 17:00:00 BRT 2018
    }

    static void mudarTimezonePadrao() {
        // usando nossa "data atual" simulada
        Date agora = new Date(clock().millis());

        /*
         * Mudar o timezone padrão faz com que seja impresso um valor diferente para data/hora.
         * 
         * Mas o timestamp (getTime()) continua o mesmo.
         */
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        // 1525464000000=Fri May 04 22:00:00 CEST 2018 - Europe/Berlin
        System.out.println(agora.getTime() + "=" + agora + " - " + TimeZone.getDefault().getID());

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        // 1525464000000=Sat May 05 05:00:00 JST 2018 - Asia/Tokyo
        System.out.println(agora.getTime() + "=" + agora + " - " + TimeZone.getDefault().getID());

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        // 1525464000000=Fri May 04 20:00:00 UTC 2018 - UTC
        System.out.println(agora.getTime() + "=" + agora + " - " + TimeZone.getDefault().getID());

        // voltar config default (para não impactar os outros métodos)
        setup();
    }

    static void criarDataJaneiro() {
        // tentando criar 10 de janeiro de 2018
        Date janeiroErrado = new Date(2018, 1, 10); // construtor deprecated
        // data criada corresponde a fevereiro de 3918
        System.out.println(janeiroErrado); // Sun Feb 10 00:00:00 BRST 3918

        // ano = 2018 - 1900, mês = começa em zero (janeiro = 0, fevereiro = 1, etc)
        Date janeiro = new Date(118, 0, 10);
        // agora sim é 10 de janeiro de 2018
        System.out.println(janeiro); // Wed Jan 10 00:00:00 BRST 2018

        // criando com Calendar.JANUARY (é uma constante cujo valor é zero, apenas minimiza a chance de erro)
        janeiro = new Date(118, Calendar.JANUARY, 10);
        System.out.println(janeiro); // Wed Jan 10 00:00:00 BRST 2018
    }

    static void criarDataJaneiroCalendar() {
        // setando campos com Calendar
        // cria um Calendar
        Calendar cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (se quiser a data atual de fato, remova esta linha, pois getInstance já retorna um Calendar com o
        // timestamp atual)
        cal.setTimeInMillis(clock().millis());

        // muda o ano para 2018, o mês para janeiro e o dia do mês para 10
        cal.set(Calendar.YEAR, 2018);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 10);
        System.out.println(cal); // imprime várias linhas (java.util.GregorianCalendar[ etc etc ...])

        // obter um java.util.Date a partir do Calendar
        Date date = cal.getTime();
        // se vc não usar a "data atual" simulada, este valor poderá ser diferente, pois será usada a hora atual em vez de 17:00
        // o mesmo vale para o valor do timestamp, pois mudando o horário, também muda o timestamp
        System.out.println(date); // Wed Jan 10 17:00:00 BRST 2018

        // obter o valor do timestamp (todos serão 1515610800000)
        long timestamp1 = date.getTime();
        // outra maneira de obter o timestamp, sem precisar criar o Date
        long timestamp2 = cal.getTimeInMillis();
        // também funciona, mas fica meio confuso (prefira usar getTimeInMillis())
        long timestamp3 = cal.getTime().getTime();
        System.out.println(timestamp1);
        System.out.println(timestamp2);
        System.out.println(timestamp3);

        // se quiser, pode mudar o horário para meia-noite (isso muda o Date e o timestamp)
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // -------------------------------------------------------------
        // outras opções para mudar os dados do Calendar
        // muda somente o ano, mês e dia (o horário não é mudado)
        cal.set(2018, Calendar.JANUARY, 10);

        // muda os campos para 10 de janeiro de 2018, meia-noite
        cal.set(2018, Calendar.JANUARY, 10, 0, 0, 0);
        // não se esqueça de mudar os milissegundos também
        cal.set(Calendar.MILLISECOND, 0);
    }

    static void setarHoraAmPm() {
        // cria um Calendar
        Calendar cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (se quiser a data atual de fato, remova esta linha)
        cal.setTimeInMillis(clock().millis());

        // mudar o horário para 17:00 (5 PM)
        cal.set(Calendar.AM_PM, Calendar.PM);
        cal.set(Calendar.HOUR, 5);

        System.out.println(cal.getTime()); // Fri May 04 17:00:00 BRT 2018
    }

    static void obterCamposCalendar() {
        // cria um Calendar
        Calendar cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (se quiser a data atual de fato, remova esta linha)
        cal.setTimeInMillis(clock().millis());

        // valor numérico do dia
        int dia = cal.get(Calendar.DAY_OF_MONTH);
        // valor numérico do mês - janeiro é 0, fevereiro é 1 etc.
        int mes = cal.get(Calendar.MONTH);
        System.out.println(dia); // 4
        System.out.println(mes); // 4 ("maio")
    }

    static void exemploSetarCamposComParamsErrados() {
        // data/hora atual: 2018-05-04T17:00-03:00 (America/Sao_Paulo)
        Calendar cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (se quiser a data atual de fato, remova esta linha)
        cal.setTimeInMillis(clock().millis());

        // tentar mudar o mês para outubro: parâmetros na ordem ERRADA (estão invertidos)
        // isso compila normalmente, pois as constantes são todas int
        // mas o mês não será mudado para outubro
        cal.set(Calendar.OCTOBER, Calendar.MONTH);

        // imprime a data, para ver se fizemos tudo certo (spoiler: não fizemos)
        System.out.println(cal.getTime()); // Sat May 05 05:00:00 BRT 2018

        // obter o dia da semana: usar um valor (FRIDAY) ao invés de um campo (DAY_OF_WEEK)
        System.out.println(cal.get(Calendar.FRIDAY)); // 125
        // Calendar.FRIDAY tem o valor 6, que é o mesmo valor do campo Calendar.DAY_OF_YEAR (dia do ano)
        // como a data do Calendar é 5 de maio de 2018, este é o 125º dia do ano
    }

    static void mudarTimezoneDoCalendar() {
        // cria um Calendar
        Calendar cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (se quiser a data atual de fato, remova esta linha)
        cal.setTimeInMillis(clock().millis());

        // imprimir hora e timestamp
        System.out.println(cal.get(Calendar.HOUR_OF_DAY)); // 17
        System.out.println(cal.getTimeInMillis()); // 1525464000000

        // mudar timezone: horário muda, mas o timestamp continua o mesmo
        cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        System.out.println(cal.get(Calendar.HOUR_OF_DAY)); // 22
        System.out.println(cal.getTimeInMillis()); // 1525464000000

        // outra opção é criar o Calendar no timezone desejado
        cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
    }

    static void timezoneNaoValidaNome() {
        // nome errado ("Pualo" em vez de "Paulo")
        System.out.println(TimeZone.getTimeZone("America/Sao_Pualo"));
        // retorna um timezone que equivale a UTC:
        // sun.util.calendar.ZoneInfo[id="GMT",offset=0,dstSavings=0,useDaylight=false,transitions=0,lastRule=null]
        // ou seja, se houver erro de digitação, não dará erro na hora, e você só vai perceber quando ver que as datas/horas estão erradas

        // -----------------------------------------------------------------
        // o jeito é verificar se o nome está na lista de timezones válidos
        for (String zone : TimeZone.getAvailableIDs()) {
            if ("America/Sao_Paulo".equals(zone)) {
                // OK, timezone America/Sao_Paulo existe
                break;
            }
        }

        // outro jeito é ver se o ID é o mesmo (quando o nome é inválido, é criado um timezone com nome "GMT")
        TimeZone tz = TimeZone.getTimeZone("America/Sao_Pualo");
        if ("America/Sao_Paulo".equals(tz.getID())) {
            // OK, timezone America/Sao_Paulo criado com sucesso
        } else {
            // ID diferente, porque o nome estava errado e foi criado um timezone com nome "GMT"
        }
    }

    static void calendarLeniente() {
        // cria um Calendar
        Calendar cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (se quiser a data atual de fato, remova esta linha)
        cal.setTimeInMillis(clock().millis()); // 4 de Maio de 2018, às 17:00 em São Paulo.

        // mudar o dia do mês para 33
        cal.set(Calendar.DAY_OF_MONTH, 33); // 33 de maio é ajustado para 2 de junho
        System.out.println(cal.getTime()); // Sat Jun 02 17:00:00 BRT 2018
    }

    static void calendarNaoLeniente() {
        // cria um Calendar
        Calendar cal = Calendar.getInstance();
        // setar timestamp para nossa "data atual" simulada (se quiser a data atual de fato, remova esta linha)
        cal.setTimeInMillis(clock().millis());

        // desligar modo leniente
        cal.setLenient(false);
        try {
            // mudar o dia do mês para 33
            cal.set(Calendar.DAY_OF_MONTH, 33);
            System.out.println(cal.getTime()); // java.lang.IllegalArgumentException: DAY_OF_MONTH
        } catch (IllegalArgumentException e) {
            System.out.println("Erro ao setar dia 33: " + e);
        }
    }
}
