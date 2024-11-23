import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Iniciando execução do programa");

        S3Provider s3Provider = new S3Provider();

        Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Iniciando listagem de arquivos do bucket");

        // Armazenando a lista de arquivos no bucket S3
        List<S3Object> arquivos = s3Provider.listarArquivos();

        // Verificando se existem arquivos no bucket S3
        if (arquivos.isEmpty())
        {
            Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Nenhum arquivo encontrado no bucket.");
        }
        else
        {
            // Listando arquivos no bucket S3
            for (S3Object arquivo : arquivos)
            {
                Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Arquivo encontrado: " + arquivo.key());

                // Lendo o arquivo do S3 e inserindo no banco de dados
                try (InputStream inputStream = s3Provider.obterArquivo(arquivo.key()))
                {
                    DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
                    LeitorExcel leitorExcel = new LeitorExcel(dbConnectionProvider);

                    Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Iniciando a leitura do arquivo: " + arquivo.key());
                    leitorExcel.lerArquivo(arquivo.key(), inputStream);
                    Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Arquivo processado com sucesso: " + arquivo.key());

                    s3Provider.deletarArquivo(arquivo.key());
                    Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Arquivo deletado do bucket: " + arquivo.key());
                }
                catch (Exception e)
                {
                    Log.inserirNoLog("Erro ao ler o arquivo " + arquivo.key() + ": " + e.getMessage());
                }
            }
        }

        Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Processamento completo.");

        // Enviar log para o S3
        File logFile = Log.colocarNaPasta();
        try
        {
            s3Provider.enviarArquivo("logs/" + logFile.getName(), logFile);
            Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Log enviado para S3 com sucesso.");
        }
        catch (Exception e)
        {
            Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Erro ao enviar log para S3: " + e.getMessage());
        }

        System.out.println("Arquivo de log enviado para S3.");
    }
}