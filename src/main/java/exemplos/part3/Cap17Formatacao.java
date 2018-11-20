package exemplos.part3;

import static exemplos.setup.Setup.setup;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import exemplos.setup.Setup;

// trata da classe java.time.Instant e como usar java.time.temporal.TemporalField e java.time.temporal.ChronoField
public class Cap17Formatacao {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    public static void main(String[] args) {
        exemplosBasicos();
        diferencaEntreUeY();
        mudarIdioma();
        naoDependeDoTimezoneDefault();
        formatarInstant();
        formatarISO8601();
        patternsOpcionais();
        qtdDigitosVariavel();
        textoCustomizado();

        textoCustomizadoAmPm();
        formatosPorLocaleETipo();
    }

    static void exemplosBasicos() {
        LocalDate data = LocalDate.of(2018, 5, 4);
        // usar pattern para "dia/mês/ano"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        // as duas formas abaixo são equivalentes
        System.out.println(formatter.format(data)); // 04/05/2018
        System.out.println(data.format(formatter)); // 04/05/2018

        // verifique se o objeto tem todos os campos que estão no pattern
        formatter = DateTimeFormatter.ofPattern("HH:mm"); // HH (hora) e mm (minuto)
        try {
            System.out.println(formatter.format(LocalDate.now()));
            // LocalDate não tem campos de horário, por isso lança exceção
        } catch (UnsupportedTemporalTypeException e) {
            System.out.println(e.getMessage()); // Unsupported field: HourOfDay
        }
    }

    // Na API legada, SimpleDateFormat só possui o pattern "y" para o ano, mas o java.time possui "u" e "y"
    static void diferencaEntreUeY() {
        // para anos positivos (DC - Depois de Cristo), não faz diferença
        LocalDate data = LocalDate.of(2010, 1, 1);
        // mostra o ano com uuuu e yyyy, e em seguida o nome da era (GGGG) - será usado o Locale default da JVM (pt_BR - Portugês do Brasi)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("uuuu yyyy GGGG");
        System.out.println(fmt.format(data)); // 2010 2010 Ano do Senhor

        // para anos AC (Antes de Cristo), faz diferença
        data = LocalDate.of(-10, 1, 1);
        System.out.println(fmt.format(data)); // -0010 0011 Antes de Cristo
        /*
         * Esta diferença acontece porque "y" considera que não houve ano zero: o primeiro ano da era em que estamos atualmente é o "ano 1 Depois de Cristo", e
         * o ano anterior a este é o "ano 1 Antes de Cristo". Já o pattern "u" considera que antes do ano 1 vem o ano zero e antes disso os anos são negativos.
         */
        // exemplo (com vários anos)
        fmt = DateTimeFormatter.ofPattern("uuuu yyyy G");
        // do ano 2 ao -2
        for (int ano = 2; ano >= -2; ano--) {
            System.out.println(fmt.format(LocalDate.of(ano, 1, 1)));
        }
        /*
         * A saída é:
         * 
         * 0002 0002 d.C.
         * 
         * 0001 0001 d.C.
         * 
         * 0000 0001 a.C.
         * 
         * -0001 0002 a.C.
         * 
         * -0002 0003 a.C.
         */
    }

    static void mudarIdioma() {
        LocalDate data = LocalDate.of(2018, 5, 4);
        DateTimeFormatter formatter = DateTimeFormatter
            // definir o pattern e usar locale pt_BR
            .ofPattern("dd 'de' MMMM 'de' uuuu", new Locale("pt", "BR"));
        System.out.println(data.format(formatter)); // 04 de Maio de 2018

        // criar formatter para português
        formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' uuuu", new Locale("pt", "BR"));
        // criar outro DateTimeFormatter com o locale inglês (o pattern é o mesmo)
        DateTimeFormatter fmtIngles = formatter.withLocale(Locale.ENGLISH);
        data = LocalDate.of(2018, 5, 4);
        System.out.println(fmtIngles.format(data)); // 04 de May de 2018
        // Repare que o texto literal "de" não foi traduzido, somente os campos que são afetados pelo locale (no caso, o mês)

        /*
         * Se você não especificar um Locale, será usado o default da JVM (Locale.getDefault()). O problema é que este pode ser mudado por outras aplicações
         * rodando na mesma JVM (qualquer um pode chamar Locale.setDefault). Ou esta config pode ser mudada sem o seu conhecimento (a equipe responsável pelo
         * servidor muda o locale - seja sem querer ou de propósito - e de repente as datas começam a ser formatadas em outro idioma, sem você ter mudado o
         * código). Então se você já sabe em qual idioma deve estar as strings formatadas, passe o Locale explicitamente e não dependa do default.
         */
    }

    static void naoDependeDoTimezoneDefault() {
        // diferente do que acontece com SimpleDateFormat, a saída do DateTimeFormatter não depende do timezone padrão da JVM
        // mudar timezone padrão para Asia/Tokyo
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        // 2018-05-04T17:00-03:00[America/Sao_Paulo] (usando a data/hora "atual" simulada )
        ZonedDateTime agora = ZonedDateTime.now(Setup.clock().withZone(ZoneId.of("America/Sao_Paulo")));
        // usar pattern para "dia/mês/ano hora:minuto"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
        // agora é 4 de maio, 17h em São Paulo - mesmo com o timezone padrão setado para Tóquio, o valor do ZonedDateTime não é afetado
        // (se fosse SimpleDateFormat, os valores de data e hora seriam convertidos para Asia/Tokyo)
        System.out.println(formatter.format(agora)); // 04/05/2018 17:00

        // voltar config default (para não impactar os outros métodos)
        setup();
    }

    static void formatarInstant() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
        Instant instant = Instant.now(Setup.clock()); // Instante atual (usando valor "simulado")
        try {
            System.out.println(formatter.format(instant));
        } catch (UnsupportedTemporalTypeException e) {
            System.out.println(e.getMessage()); // Unsupported field: DayOfMonth
        }
        // Instant representa um timestamp e portanto pode corresponder a uma data e hora diferentes em cada timezone
        // Por isso tentar formatá-lo lança exceção, pois ele não possui os campos de data e hora
        // Para obter tais campos, deve-se converter o Instant para um timezone ou offset

        // converter para UTC
        System.out.println(formatter.format(instant.atOffset(ZoneOffset.UTC))); // 04/05/2018 20:00
        // converter para algum timezone
        System.out.println(formatter.format(instant.atZone(ZoneId.of("Asia/Tokyo")))); // 05/05/2018 05:00

        // outra opção é setar o timezone no DateTimeFormatter
        formatter = DateTimeFormatter
            .ofPattern("dd/MM/uuuu HH:mm")
            // usar timezone Asia/Tokyo
            .withZone(ZoneId.of("Asia/Tokyo"));
        // instant corresponde a 2018-05-04T20:00:00Z (mas ele é convertido para o timezone do DateTimeFormatter e o resultado é a data e hora de Tóquio)
        System.out.println(formatter.format(instant)); // 05/05/2018 05:00

        // objetos que podem ser convertidos para Instant (como ZonedDateTime e OffsetDateTime) também são convertidos para o timezone do DateTimeFormatter
        // 2018-05-04T17:00-03:00[America/Sao_Paulo]
        ZonedDateTime agora = ZonedDateTime.now(Setup.clock().withZone(ZoneId.of("America/Sao_Paulo")));
        System.out.println(formatter.format(agora)); // 05/05/2018 05:00
        // LocalDateTime não possui timezone (e não pode ser convertido diretamente para Instant) -> o timezone do DateTimeFormatter não tem efeito
        // 2018-05-04T17:00
        LocalDateTime dataHora = LocalDateTime.now(Setup.clock());
        System.out.println(formatter.format(dataHora)); // 04/05/2018 17:00
    }

    static void formatarISO8601() {
        // 2018-05-04T17:00-03:00[America/Sao_Paulo]
        ZonedDateTime agora = ZonedDateTime.now(Setup.clock().withZone(ZoneId.of("America/Sao_Paulo")));

        // existem várias constantes predefinidas para o formato ISO 8601
        System.out.println(agora.format(DateTimeFormatter.ISO_LOCAL_DATE)); // 2018-05-04
        System.out.println(agora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // 2018-05-04T17:00:00
        System.out.println(agora.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)); // 2018-05-04T17:00:00-03:00

        // vários formatos para o offset
        DateTimeFormatter formatter = DateTimeFormatter
            // formato ISO 8601, offset com os dois pontos (igual a ISO_OFFSET_DATE_TIME)
            .ofPattern("uuuu-MM-dd'T'HH:mm:ssXXX");
        System.out.println(agora.format(formatter)); // 2018-05-04T17:00:00-03:00
        formatter = DateTimeFormatter
            // formato ISO 8601, offset sem os dois pontos
            .ofPattern("uuuu-MM-dd'T'HH:mm:ssXX");
        System.out.println(agora.format(formatter)); // 2018-05-04T17:00:00-0300
        formatter = DateTimeFormatter
            // formato ISO 8601, offset sem o valor dos minutos
            .ofPattern("uuuu-MM-dd'T'HH:mm:ssX");
        // Este formato pode omitir informações importante caso o offset tenha um valor para os minutos (a Índia, por exemplo, usa o offset +05:30, mas este
        // formato só mostraria +05)
        System.out.println(agora.format(formatter)); // 2018-05-04T17:00:00-03

        // a diferença para os formatadores predefinidos se dá nas frações de segundo
        LocalDateTime dateTime = LocalDateTime.of(2018, 1, 1, 10, 30, 20, 123000000);
        formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSS");
        System.out.println(dateTime.format(formatter)); // 2018-01-01T10:30:20.123000000 (imprime todos as 9 casas decimais)
        System.out.println(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // 2018-01-01T10:30:20.123 (os zeros no final são omitidos)
    }

    static void patternsOpcionais() {
        // data obrigatória, hora opcional
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu[ HH:mm:ss]");
        // 2018-05-04
        LocalDate data = LocalDate.of(2018, 5, 4);
        // 2018-05-04T17:30
        LocalDateTime dataHora = data.atTime(17, 30);
        System.out.println(formatter.format(data)); // 04/05/2018
        System.out.println(formatter.format(dataHora)); // 04/05/2018 17:30:00
    }

    static void qtdDigitosVariavel() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            // hora:minuto:segundo
            .appendPattern("HH:mm:ss")
            // nanossegundos, com 6 casas decimais no máximo
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true)
            // criar o DateTimeFormatter
            .toFormatter();
        LocalTime hora = LocalTime.of(10, 30, 15, 123400000);
        System.out.println(formatter.format(hora)); // 10:30:15.1234 (usa de zero a 6 casas decimais, omitindo os zeros no final)
    }

    static void textoCustomizado() {
        Map<Long, String> nomesDosDias = new HashMap<>();
        // valor 1 (escrito como "1L", já que os valores devem ser long)
        nomesDosDias.put(1L, "Primeiro"); // dia 1 será formatado como "Primeiro" (os demais valores serão o próprio número)
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            // nomes customizados para o dia do mês
            .appendText(ChronoField.DAY_OF_MONTH, nomesDosDias)
            // nome do mês
            .appendPattern(" 'de' MMMM")
            // criar o DateTimeFormatter com locale pt_BR
            .toFormatter(new Locale("pt", "BR"));
        // formatar 4 de maio de 2018
        System.out.println(LocalDate.of(2018, 5, 4).format(formatter)); // 4 de Maio
        // formatar 1 de maio de 2018
        System.out.println(LocalDate.of(2018, 5, 1).format(formatter)); // Primeiro de Maio
    }

    // ------------------------------------------------
    // exemplos que não estão no livro
    static void textoCustomizadoAmPm() {
        // mudar o valor de AM e PM para "manhã" e "tarde"
        Map<Long, String> textosCustomizados = new HashMap<>();
        textosCustomizados.put(0L, "manhã");
        textosCustomizados.put(1L, "tarde");
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            // horário (hh é a hora-AM-PM, com valores de 1 a 12 - ou seja, é ambíguo a menos que se tenha também o campo AM/PM)
            .appendPattern("hh:mm 'da' ")
            // nomes customizados para o dia do mês
            .appendText(ChronoField.AMPM_OF_DAY, textosCustomizados)
            // criar o DateTimeFormatter
            .toFormatter();
        System.out.println(formatter.format(LocalTime.of(10, 30))); // 10:30 da manhã
        System.out.println(formatter.format(LocalTime.of(17, 50))); // 05:50 da tarde
    }

    // existem formatos predefinidos para cada Locale
    static void formatosPorLocaleETipo() {
        // data/hora atual simulada: 4 de Maio de 2018, às 17:00 em São Paulo
        ZonedDateTime zdt = ZonedDateTime.now(Setup.clock());
        DateTimeFormatter formatter;
        // percorrer todos os locales
        for (Locale locale : Locale.getAvailableLocales()) {
            // percorrer todos os estilos
            for (FormatStyle style : FormatStyle.values()) {
                formatter = DateTimeFormatter.ofLocalizedDateTime(style).withLocale(locale);
                System.out.format("Formato %s e locale %s: %s\n", style.toString(), locale.toString(), zdt.format(formatter));
            }
        }

        // Atenção quando for usar estes formatos predefinidos. Dependendo do Locale e do FormatStyle, o formato retornado pode ter ou não determinados campos
        // (como só a data ou só a hora, ou data, hora e offset, etc), e se o tipo de data/hora sendo formatado não tiver esses campos, lançará exceção
    }
}
