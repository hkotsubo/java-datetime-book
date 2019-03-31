package mysql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import exemplos.setup.Setup;

/**
 * Teste feito no MySQL 5.7.21
 * 
 * Para configurar os timezones no MySQL, veja em https://dev.mysql.com/doc/refman/5.5/en/time-zone-support.html
 * 
 * Para criar a tabela de exemplo usada neste teste, veja o arquivo scripts/mysql/criar_tabela.sql
 * 
 * Este é um código que não tem no livro, e serve para exemplificar como os valores de data e hora podem ser influenciados pela configuração de timezones e
 * retornar resultados confusos.
 * 
 * No caso, o tipo TIMESTAMP do MySQL converte a data/hora recebida do timezone atual para UTC na hora de inserir, e ao consultar converte de volta de UTC para
 * o timezone atual. O que ele chama de "timezone atual" é o que está configurado no servidor, ou o timezone que está setado na conexão. Para mais detalhes,
 * veja a documentação: https://dev.mysql.com/doc/refman/5.6/en/datetime.html
 * 
 * Neste exemplo eu seto o timezone na conexão (uso um no INSERT e outro no SELECT) para simular a situação na qual o timezone do servidor é alterado. Com isso
 * eu mostro como os valores podem mudar dependendo dessas configurações.
 * 
 * Isso é algo específico do MySQL. Outros bancos de dados podem ou não ter problemas similares, dependendo de como cada um implementa seus tipos de data.
 * Sempre leia a documentação antes de decidir qual tipo usar (DATE, DATETIME, TIMESTAMP, TIMESTAMP WITH TIMEZONE, etc). Apesar de terem os mesmos nomes, cada
 * tipo pode ter comportamentos diferentes dependendo do banco, e as configurações de timezone (tanto do servidor quanto da conexão) podem influenciar no
 * resultado final (na data que você grava e na que você obtém quando consulta).
 */
public class MysqlTests {

    static {
        // setar configurações, ver javadoc da classe Setup para mais detalhes
        Setup.setup();
    }

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();

        apagaTudo(); // só pra não poluir a tabela de testes, pode comentar essa linha se quiser

        // insere 4 de maio de 2018, 17:30, usando o timezone America/Sao_Paulo na conexão
        insertDate(1, 4, 5, 2018, 17, 30, "America/Sao_Paulo");
        // faz o select usando o timezone Asia/Tokyo na conexão
        select("Asia/Tokyo");
        /*
         * O timestamp retorna 2018-05-04 05:30:00.0, pois o valor do banco é 2018-05-04T17:30 (mas foi gravado com America/Sao_Paulo), e ao ler usando
         * Asia/Tokyo, ele é interpretado como 2018-05-04T17:30+09:00 (17:30 em Tóquio), que no timezone default da JVM (America/Sao_Paulo) equivale a 05:30 da
         * manhã. Eu usei timezones diferentes na conexão de propósito, mas o mesmo aconteceria se o MySQL estivesse rodando em um timezone na hora do INSERT,
         * mas mudasse para outro timezone e depois vc fizesse o SELECT (e não importa se a mudança de configuração do timezone foi sem querer ou de propósito,
         * o fato é que esta é uma situação fora do seu controle e podem começar a retornar datas erradas "do nada").
         */
        // O campo DATE, por sua vez, retorna 2018-05-04

        /*
         * Pelo mesmo motivo já explicado acima, esse SELECT mostra o timestamp como 2018-05-04 21:30:00.0. E repare que o respectivo valor do timestamp
         * (retornado por getTime()) é diferente do SELECT acima. O timezone usado na conexão (ou o configurado no servidor) muda completamente o valor
         * retornado
         */
        select("America/Los_Angeles");

        // ----------------------------------------------------
        // outro exemplo
        apagaTudo();
        insertDate(2, 4, 5, 2018, 23, 30, "Europe/London");
        select("Europe/London"); // timestamp retorna 2018-05-04 23:30:00.0
        select("America/Sao_Paulo"); // timestamp retorna 2018-05-05 03:30:00.0
    }

    static void insertDate(long id, int dia, int mes, int ano, int hora, int minuto, String timezone) throws Exception {
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = connect(timezone);
            st = conn.prepareStatement("insert into exemplo (id, data_ts, data) values (? , ? , ? )");
            st.setLong(1, id);
            st.setTimestamp(2, createTimestamp(dia, mes, ano, hora, minuto));
            st.setDate(3, createDate(dia, mes, ano));
            st.executeUpdate();
        } finally {
            if (st != null) {
                st.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    static void select(String timezone) throws Exception {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = connect(timezone);
            st = conn.prepareStatement("select * from exemplo");
            rs = st.executeQuery();
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("data_ts");
                Date data = rs.getDate("data");
                System.out.printf("id=%s, data_ts=%s (%d), data=%s (%d)\n", rs.getLong("id"), timestamp, timestamp.getTime(), data, data.getTime());
            }
        } finally {
            if (st != null) {
                st.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    static void apagaTudo() throws Exception {
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = connect(TimeZone.getDefault().getID());
            st = conn.prepareStatement("delete from exemplo");
            st.executeUpdate();
        } finally {
            if (st != null) {
                st.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    static Connection connect(String timezone) throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/test?user=root&password=password&useTimezone=true&serverTimezone=" + timezone);
    }

    static Timestamp createTimestamp(int dia, int mes, int ano, int hora, int minuto) {
        Calendar cal = Calendar.getInstance();
        cal.set(ano, mes - 1, dia, hora, minuto, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Timestamp(cal.getTimeInMillis());
    }

    static Date createDate(int dia, int mes, int ano) {
        Calendar cal = Calendar.getInstance();
        cal.set(ano, mes - 1, dia, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Date(cal.getTimeInMillis());
    }
}
