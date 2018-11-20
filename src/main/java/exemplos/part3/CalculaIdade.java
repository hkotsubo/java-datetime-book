package exemplos.part3;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Classe feita apenas com fins didáticos, por isso não há verificações que um sistema real teria (verificar se a data de nascimento está no futuro, etc)
 *
 */
public class CalculaIdade {
    // Clock a ser usado para obter a data atual
    private Clock clock;

    // em produção, você pode usar Clock.systemDefaultZone() para usar o relógio do sistema e o timezone default da JVM
    // nos testes, pode ser usado um Clock fixo para simular diversas situações
    public CalculaIdade(Clock clock) {
        this.clock = clock;
    }

    // retorna a idade (quantidade de anos)
    public long getIdade(LocalDate dataNasc) {
        return ChronoUnit.YEARS.between(dataNasc, LocalDate.now(clock));
    }
}
