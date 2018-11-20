package exemplos.part3;

import static exemplos.setup.Setup.setup;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import exemplos.part2.Cap12Others;
import exemplos.setup.Setup;

public class Cap19TestesOutrosCasos {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        setup();
    }

    /**
     * Para os exemplos de cálculo de idade, veja a classe {@link CalculaIdade} e os seus unit tests em {@link CalculaIdadeTest}
     */
    public static void main(String[] args) {
        usoBasicoDoClock();
        obterInformacoesTimezone();
        obterInformacoesTimezone2();
        compararTimezones();
        encontrarTimezonesPorOffsetOuAbreviacao();
        criarTemporalAdjuster();
    }

    static void usoBasicoDoClock() {
        // -------------------------------------------------
        // Usar um Clock fixo (sempre retorna o mesmo valor para o instante atual)

        // 4 de maio de 2018, às 17:00 em São Paulo
        ZonedDateTime agoraFixo = ZonedDateTime.parse("2018-05-04T17:00-03:00[America/Sao_Paulo]");
        // criar o Clock fixo
        Clock clock = Clock.fixed(
                // Instant que corresponde ao timestamp atual
                agoraFixo.toInstant(), // 2018-05-04T20:00:00Z
                // timezone usado para converter o timestamp atual em data, hora e offset
                agoraFixo.getZone()); // America/Sao_Paulo
        // o método now() obtém o instante atual do Clock e converte para data/hora/offset usando o ZoneId do Clock

        System.out.println(LocalDate.now(clock)); // 2018-05-04
        System.out.println(LocalTime.now(clock)); // 17:00
        System.out.println(OffsetDateTime.now(clock)); // 2018-05-04T17:00-03:00
        System.out.println(Instant.now(clock)); // 2018-05-04T20:00:00Z

        // para mudar o timezone do Clock
        clock = clock.withZone(ZoneId.of("Asia/Tokyo"));
        // o Instant atual é o mesmo, mas agora ele é convertido para o timezone de Tóquio, alterando os valores de data, hora e offset
        System.out.println(OffsetDateTime.now(clock)); // 2018-05-05T05:00+09:00
        System.out.println(Instant.now(clock)); // 2018-05-04T20:00:00Z <- o instante atual é o mesmo

        // usar o relógio do sistema e o timezone default da JVM
        clock = Clock.systemDefaultZone();
        System.out.println(OffsetDateTime.now(clock)); // vai mostrar a data, hora e offset atual da máquina onde a JVM está rodando

        // usar o relógio do sistema e um timezone específico (em vez do default da JVM)
        clock = Clock.system(ZoneId.of("Europe/London"));
        System.out.println(OffsetDateTime.now(clock)); // vai mostrar a data, hora e offset atual da máquina onde a JVM está rodando
        // mas a data e hora serão convertidas para o timezone indicado
    }

    static void obterInformacoesTimezone() {
        ZoneRules rules = ZoneId.of("America/Sao_Paulo").getRules();
        rules.getTransitions().forEach(System.out::println); // mostra várias linhas com as mudanças de offset. Ex:
        // Transition[Gap at 2009-10-18T00:00-03:00 to -02:00]
        // Transition[Overlap at 2010-02-21T00:00-02:00 to -03:00]
        // Estas linhas indicam quando houve um gap ou overlap

        // -------------------------------------------------
        // obtendo uma transição qualquer (apenas para vermos em detalhes)
        ZoneOffsetTransition transition = rules.getTransitions().get(30);
        // pode variar de acordo com a versão da JVM ou do TZDB que está instalado, mas nos meus testes a saída foi a transição de janeiro de 1989
        System.out.println(transition); // Transition[Overlap at 1989-01-29T00:00-02:00 to -03:00]
        System.out.println(transition.isGap()); // false
        System.out.println(transition.isOverlap()); // true
        System.out.println(transition.isValidOffset(ZoneOffset.ofHours(-3))); // true (offset válido para esta transição)
        System.out.println(transition.isValidOffset(ZoneOffset.ofHours(+7))); // false (offset inválido para esta transição)

        // -------------------------------------------------
        // aqui podemos ver que à meia-noite os relógios foram atrasados em uma hora
        // data/hora e offset antes da transição
        System.out.println(transition.getDateTimeBefore()); // 1989-01-29T00:00 (meia-noite, hora em que o relógio foi atrasado)
        System.out.println(transition.getOffsetBefore()); // -02:00
        // data/hora e offset depois da transição
        System.out.println(transition.getDateTimeAfter()); // 1989-01-28T23:00
        System.out.println(transition.getOffsetAfter()); // -03:00

        // -------------------------------------------------
        // instante (UTC) exato em que ocorre a mudança de offset
        System.out.println(transition.getInstant()); // 1989-01-29T02:00:00Z
        // duração (no caso, o relógio foi atrasado em uma hora, por isso o valor negativo)
        System.out.println(transition.getDuration()); // PT-1H (menos uma hora, indicando que o relógio foi atrasado em uma hora)
    }

    static void obterInformacoesTimezone2() {
        ZoneRules rules = ZoneId.of("America/Sao_Paulo").getRules();

        // próxima mudança de offset (ou seja, quando vai começar/terminar o horário de verão?)
        // usei o Setup.clock() para ter a data/hora simulada (4 de Maio de 2018, às 17:00 em São Paulo)
        // mas para usar o instante atual, use Instant.now()
        Instant instant = Instant.now(Setup.clock());
        ZoneOffsetTransition proxima = rules.nextTransition(instant);
        // A JVM que estou usando tem a versão 2018e do TZDB (https://www.iana.org/time-zones), que já tem a nova regra do horário de verão brasileiro (começa
        // no primeiro domingo de novembro), por isso a transição abaixo ocorre em 4 de novembro. Se sua JVM possui as regras antigas, a saída será o terceiro
        // domingo de outubro
        System.out.println(proxima); // Transition[Gap at 2018-11-04T00:00-03:00 to -02:00]
        // também é possível usar previousTransition para obter a transição anterior ao Instant

        // ------------------------------
        // outros métodos úteis de ZoneRules

        // obtém o offset usado pelo timezone no Instant indicado
        System.out.println(rules.getOffset(instant)); // -03:00
        // o Instant corresponde a um instante em que o timezone está em horário de verão?
        System.out.println(rules.isDaylightSavings(instant)); // false
        // Quando está em horário de verão, retorna um Duration com a diferença para o horário "normal" (ou seja, para a grande maioria dos lugares, é 1 hora)
        // Apesar de ter alguns lugares que adiantavam apenas meia-hora durante o horário de verão, como Montevidéu em 1968:
        // https://www.timeanddate.com/time/zone/uruguay/montevideo?year=1968
        // Como America/Sao_Paulo não está em horário de verão no Instant indicado, o retorno é uma Duration com valor igual a zero
        System.out.println(rules.getDaylightSavings(instant)); // PT0S

        // retorna os offsets válidos em determinada data/hora
        LocalDateTime dt = LocalDateTime.parse("2009-10-18T00:30");
        System.out.println(rules.getValidOffsets(dt)); // data/hora está em um gap, lista de offsets válidos é vazia
        dt = LocalDateTime.parse("2010-02-20T23:30");
        System.out.println(rules.getValidOffsets(dt)); // [-02:00, -03:00] -> data/hora está em um overlap, há dois offsets válidos
        dt = LocalDateTime.parse("2010-05-01T10:30");
        System.out.println(rules.getValidOffsets(dt)); // [-03:00] -> data/hora está em horário normal, apenas um offset válido
        dt = LocalDateTime.parse("2010-01-01T10:30");
        System.out.println(rules.getValidOffsets(dt)); // [-02:00] -> data/hora está em horário de verão, apenas um offset válido

        // verifica se o timezone tem offset fixo (a maioria não tem, pois quase todos os lugarem têm ou já tiveram horário de verão ou usavam um offset
        // diferente em algum momento da história)
        System.out.println(rules.isFixedOffset()); // false
        // os que têm offset fixo são UTC e os ... offset fixos :/
        System.out.println(ZoneOffset.UTC.getRules().isFixedOffset()); // true
        System.out.println(ZoneOffset.of("+05:00").getRules().isFixedOffset()); // true
    }

    static void compararTimezones() {
        // segundo arquivo de backward (https://github.com/eggert/tz/blob/2018e/backward#L60) America/Sao_Paulo e Brazil/East são sinônimos
        ZoneId sp = ZoneId.of("America/Sao_Paulo");
        ZoneId br = ZoneId.of("Brazil/East");
        System.out.println(sp.equals(br)); // false <- equals compara o ID (a String correspondente ao nome)

        // para saber se 2 timezones são iguais, devemos comparar o histórico de offsets (ou seja, o ZoneRules)
        System.out.println(sp.getRules().equals(br.getRules())); // true
        // Se o histórico (ZoneRules) é diferente, significa que em algum momento da história os timezones não usavam o mesmo offset
        // E se há uma diferença no histórico, por mínimo que seja, a IANA cria outro timezone (por isso ter ZoneRules iguais garante que é o mesmo timezone)
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
     * Este método gera dois Sets contendo os nomes dos timezones para determinado offset ou abreviação. É interessante comparar com a API legada em
     * {@link Cap12Others#encontrarTimezonesPorAbreviacao()} e {@link Cap12Others#encontrarTimezonesPorOffset()}
     */
    public static void encontrarTimezonesPorOffsetOuAbreviacao() {
        // instante a ser usado como referência - ou seja, vou buscar os timezones que, **neste instante específico**, usam o offset +02:00 ou a abreviação EST
        // se mudar o Instant, pode ser que a lista de timezones mude
        Instant referencia = Instant.now(Setup.clock()); // 2018-05-04T20:00:00Z
        // procurar timezones que usam offset +02:00
        ZoneOffset offset = ZoneOffset.ofHours(2);
        // procurar timezones que usam abreviação EST
        String abreviacao = "EST";

        // Sets para guardar os nomes dos timezones
        Set<String> zonesOffset2 = new HashSet<>();
        Set<String> zonesAbrevEST = new HashSet<>();

        // pattern "z" retorna a abreviação do timezone
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("z");

        // percorrer todos os timezones
        ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).forEach(zone -> {
            if (offset.equals(zone.getRules().getOffset(referencia))) {
                // timezone usa o offset +02:00 no instante de referência
                zonesOffset2.add(zone.getId());
            }

            // verificar todos os locales, pois a abreviação é locale sensitive
            for (Locale locale : Locale.getAvailableLocales()) {
                String abrev = fmt.withLocale(locale).format(referencia.atZone(zone));
                if (abreviacao.equals(abrev)) {
                    // timezone usa a abreviação EST no instante de referência
                    zonesAbrevEST.add(zone.getId());
                }
            }
        });

        // a saída pode variar conforme a versão do TZDB instalado na sua JVM
        System.out.println(zonesOffset2); // mais de 50 timezones: [Europe/Ljubljana, Africa/Lusaka, Europe/Kaliningrad, Africa/Gaborone, etc....
        System.out.println(zonesAbrevEST); // 8 timezones: [America/Coral_Harbour, SystemV/EST5, America/Jamaica, America/Cayman, America/Cancun,
                                           // America/Panama, America/Atikokan, Jamaica]
    }

    static void criarTemporalAdjuster() {
        // ajustar data para 3 meses no futuro, no primeiro dia do mês
        TemporalAdjuster adjuster = temporal -> {
            return temporal
                // 3 meses no futuro
                .plus(3, ChronoUnit.MONTHS)
                // no dia 1
                .with(ChronoField.DAY_OF_MONTH, 1);
        };
        LocalDate data = LocalDate.of(2018, 5, 4).with(adjuster);
        System.out.println(data); // 2018-08-01
    }
}
