package exemplos.part3;

import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

public class CalculaIdadeTest {

    private CalculaIdade calc;

    @Before
    public void setup() {
        // usa um Clock que retorna a data atual -> 9 de janeiro de 2010, meia-noite, em São Paulo
        ZonedDateTime z = ZonedDateTime.parse("2010-01-09T00:00-03:00[America/Sao_Paulo]");
        calc = new CalculaIdade(Clock.fixed(z.toInstant(), z.getZone()));
    }

    @Test
    public void umDiaAntesDoDecimoAniversario() {
        LocalDate dataNasc = LocalDate.of(2000, 1, 10);
        assertEquals(calc.getIdade(dataNasc), 9); // um dia antes do décimo aniversário, a idade ainda é 9 anos
    }

    @Test
    public void umDiaDepoisDoDecimoAniversario() {
        LocalDate dataNasc = LocalDate.of(2000, 1, 8);
        assertEquals(calc.getIdade(dataNasc), 10); // um dia depois do décimo aniversário, a idade é 10
    }

    @Test
    public void noDiaDoAniversario() {
        LocalDate dataNasc = LocalDate.of(2000, 1, 9);
        assertEquals(calc.getIdade(dataNasc), 10); // no dia do aniversário, a idade é 10
    }

    // -------------------------------------------------------------------------
    // é possível modificar o Clock para as várias situações que você precisar

    /**
     * Este exemplo mostra como o java.time calcula quantos anos há entre duas datas, quando uma delas é 29 de fevereiro.
     * 
     * Você pode ver como este comportamento varia conforme a API. Neste caso, o resultado é zero, mas no Joda-Time é 1 (veja em
     * {@link outros.JodaTimeExemplos#calculaIdadeNascido29Fev()})
     */
    @Test
    public void nascido29DeFevereiro() {
        // data de nascimento: 29 de fevereiro de 2000
        LocalDate dataNasc = LocalDate.of(2000, 2, 29);

        // em 28 de fevereiro de 2001, a idade será 1 ano?
        ZonedDateTime z = ZonedDateTime.parse("2001-02-28T10:00-03:00[America/Sao_Paulo]");
        calc = new CalculaIdade(Clock.fixed(z.toInstant(), z.getZone()));

        // a API java.time entende que em 28/02/2001 ainda não foi completado 1 ano
        assertEquals(calc.getIdade(dataNasc), 0);

        // --------------------------------------------
        // somente a partir de 1 de março de 2001, a idade será 1 ano
        z = ZonedDateTime.parse("2001-03-01T10:00-03:00[America/Sao_Paulo]");
        calc = new CalculaIdade(Clock.fixed(z.toInstant(), z.getZone()));
        assertEquals(calc.getIdade(dataNasc), 1);
    }
}
