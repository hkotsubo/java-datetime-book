# Datas e horas: Conceitos fundamentais e as APIs do Java

Neste repositório estão alguns códigos de exemplo usados no livro [Datas e horas: Conceitos fundamentais e as APIs do Java](https://www.casadocodigo.com.br/products/livro-datas-e-horas). Como muitos códigos são explicados em mais detalhes no texto, e muitos são apenas didáticos, não houve uma preocupação muito grande com *design patterns*, "boas práticas" e coisas do tipo.

Algumas classes específicas são citadas no livro, mas a maioria são os mesmos códigos utilizados no texto, para servir como referência. Há um pacote para cada parte do livro, e uma classe para capítulo (exceto um ou outro que cita mais de uma classe específica), sendo que os exemplos seguem a ordem em que aparecem no texto (sendo que os exemplos foram divididos em métodos, assim você pode chamar apenas os que lhe interessarem).

Há também alguns exemplos que não estão no livro, seja por estarem fora do escopo da discussão, seja por falta de espaço mesmo.

Os pacotes seguem a mesma divisão dos capítulos:

- `exemplos.part1`: como esta parte é mais conceitual, os códigos são apenas para ilustrar alguns conceitos
- `exemplos.part2`: mostra a API legada (`java.util.Date`, `Calendar`, `SimpleDateFormat` etc)
- `exemplos.part3`: foca na API `java.time`

Todos os exemplos usam o pacote `exemplos.setup` para fazer as configurações iniciais.

Existem ainda pacotes adicionais, com códigos que não estão no livro:

- `outros`: Joda-time e Threeten Backport, duas bibliotecas que são apenas mencionadas no livro. Este pacote possui alguns exemplos de uso destas APIs.
- `mysql`: contém exemplos com o pacote `java.sql`, usando o banco MySQL.

---
### Configurações

É importante ressaltar que todos os códigos usam a classe `exemplos.setup.Setup`, que ajusta algumas configurações, de forma a não ter uma variação tão grande nos resultados gerados. Estas configurações são:

- setar o *locale default* da JVM para pt_BR (português do Brasil)
- setar o *timezone default* para `America/Sao_Paulo`
- usar como data/hora atual "4 de Maio de 2018, às 17:00 em São Paulo"

O terceiro item é feito usando-se um `java.time.Clock` (que é explicado em detalhes no capítulo 19). Sendo assim, nos códigos deste projeto, para obter a data/hora "atual" você terá algo assim:

```java
Date dataAtual = new Date(clock().millis());
LocalDateTime dataHoraAtual = LocalDateTime.now(clock());
```

Sendo que no livro estará assim:

```java
Date dataAtual = new Date();
LocalDateTime dataHoraAtual = LocalDateTime.now();
```

Se eu não usasse o `Clock`, o resultado seria a data/hora atual no momento em que você rodar o código, e obviamente os resultados mudarão a cada execução (além de ficarem totalmente diferentes do livro). Para deixar as coisas menos confusas e manter os mesmos resultados, optei por simular a data/hora atual. Se não souber como o `Clock` funciona, por ora basta saber que ele serve para obter este valor simulado.
