package exemplos.setup;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.TimeZone;

import exemplos.part3.Cap19TestesOutrosCasos;

public class Setup {

    /**
     * Seta configurações default, para que você não precise mudar a sua JVM.
     * 
     * Se não quiser usar este método, configure sua JVM com as opções abaixo:
     * 
     * <pre>
     * -Duser.country=BR -Duser.language=pt -Duser.timezone=America/Sao_Paulo
     * </pre>
     */
    public static void setup() {
        Locale.setDefault(new Locale("pt", "BR"));
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }

    private static Clock DEFAULT_CLOCK = null;

    /**
     * Retorna um {@link Clock} para simular a data atual.
     * 
     * O instante retornado pelo {@link Clock} corresponde a 4 de Maio de 2018, às 17:00 em São Paulo.
     * 
     * O funcionamento do {@link Clock} é explicado em detalhes na classe {@link Cap19TestesOutrosCasos}
     */
    public static Clock clock() {
        if (DEFAULT_CLOCK == null) {
            ZonedDateTime z = ZonedDateTime.of(2018, 5, 4, 17, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
            DEFAULT_CLOCK = Clock.fixed(z.toInstant(), z.getZone());
        }
        return DEFAULT_CLOCK;
    }
}
