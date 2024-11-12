import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

//Conexão e interações com o banco de dados
public class DBConnectionProvider
{
    private final DataSource dataSource;

    // Configura o datasource MySQL usando variáveis de ambiente
    public DBConnectionProvider()
    {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();

        mysqlDataSource.setServerName(System.getenv("DB_HOST")); // Endereço do servidor
        mysqlDataSource.setPort(Integer.parseInt(System.getenv("DB_PORT"))); // Porta
        mysqlDataSource.setDatabaseName(System.getenv("DB_NAME")); // Nome do banco de dados
        mysqlDataSource.setUser(System.getenv("DB_USER")); // Usuário do banco de dados
        mysqlDataSource.setPassword(System.getenv("DB_PASSWORD")); // Senha do banco de dados

        this.dataSource = mysqlDataSource;
    }

    //Inserts de acordo com o arquivo lido
    public void insertAvaliacao(Avaliacao avaliacao)
    {
        DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
        JdbcTemplate connection = dbConnectionProvider.getConnection();

        connection.update("INSERT INTO avaliacao (qtdEstrela,descricaoComentario,dataExtracao,fkEmpresa) VALUES (?, ?, NOW(), ?)",
                avaliacao.getEstrelas(),avaliacao.getDescricao(),avaliacao.getCodEmpresa());

        System.out.println("Dados de avaliações inseridos no banco de dados!");

    }

    public void insertReclamacao(Reclamacao reclamacao)
    {
        DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
        JdbcTemplate connection = dbConnectionProvider.getConnection();

        connection.update("INSERT INTO reclamacao (titulo,descricao,dataExtracao,fkEmpresa) VALUES (?, ?, NOW(), ?)",
                reclamacao.getTitulo(),reclamacao.getCorpo(),reclamacao.getCodEmpresa());

        System.out.println("Dados de reclamações inseridos no banco de dados!");
    }

    public void insertDemandaPesquisa(DemandaPesquisa demandaPesquisa)
    {
        DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
        JdbcTemplate connection = dbConnectionProvider.getConnection();

        connection.update("INSERT INTO demandaPesquisa (nivelInteresse, dataLeitura, dataExtracao, fkEmpresa) VALUES (?, ?, NOW(), ?)",
                demandaPesquisa.getDemanda(),demandaPesquisa.getData(),demandaPesquisa.getCodEmpresa());

        System.out.println("Dados de demanda de pesquisa inseridos no banco de dados!");
    }

    // Retorna um JdbcTemplate para executar operações no banco de dados
    public JdbcTemplate getConnection()
    {
        return new JdbcTemplate(dataSource);
    }
}
