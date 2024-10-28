import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main
{
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    // Configuração do logger
    static
    {
        try
        {

            FileHandler fileHandler = new FileHandler("logs.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException
    {
        S3Provider s3Provider = new S3Provider();

        logger.info("Iniciando listagem de arquivos do bucket");

        // Armazenando a lista de arquivos no bucket S3
        List<S3Object> arquivos = s3Provider.listarArquivos();

        // Verificando se existem arquivos no bucket S3
        if (arquivos.isEmpty())
        {
            logger.warning("Nenhum arquivo encontrado no bucket S3.");
        }
        else
        {
            // Listando arquivos no bucket S3
            for (S3Object arquivo : arquivos)
            {
                logger.info("Arquivo encontrado: " + arquivo.key());

                // Lendo o arquivo do S3 e inserindo no banco de dados
                try (InputStream inputStream = s3Provider.obterArquivo(arquivo.key()))
                {
                    DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();

                    LeitorExcel leitorExcel = new LeitorExcel(dbConnectionProvider);

                    logger.info("Iniciando a leitura do arquivo: " + arquivo.key());

                    leitorExcel.lerArquivo(arquivo.key(), inputStream);

                    logger.info("Arquivo processado com sucesso: " + arquivo.key());

                    s3Provider.deletarArquivo(arquivo.key());

                    logger.info("Arquivo deletado do bucket: " + arquivo.key());

                } catch (Exception e)
                {
                    logger.log(Level.SEVERE, "Erro ao ler o arquivo " + arquivo.key(), e);
                }
            }
        }
        logger.info("Processamento completo.");

        // Enviar o arquivo de log para o bucket S3
        Path logFilePath = Paths.get("logs.txt");
        s3Provider.enviarLog("logs.txt", logFilePath);

        System.out.println("Arquivo de log enviado para S3.");
    }
}
