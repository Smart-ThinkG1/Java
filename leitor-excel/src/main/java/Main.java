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
        //Formatando a data atual
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Iniciando execução do programa");


        //Criando instâncias S3
        S3Provider s3Provider = new S3Provider();
        S3Service s3Service = new S3Service(s3Provider, "smart-think-s3");

        Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Iniciando listagem de arquivos do bucket");

        List<S3Object> arquivos = s3Service.listarArquivos();

        //Verificando se existem arquivos encontrados no S2
        if (arquivos.isEmpty())
        {
            Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Nenhum arquivo encontrado no bucket.");
        }
        else
        {
            for (S3Object arquivo : arquivos)
            {
                Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Arquivo encontrado: " + arquivo.key());

                try (InputStream inputStream = s3Service.obterArquivo(arquivo.key()))
                {
                    DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
                    SlackNotifier slackNotifier = new SlackNotifier();
                    LeitorExcel leitorExcel = new LeitorExcel(dbConnectionProvider, slackNotifier);

                    Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Iniciando a leitura do arquivo: " + arquivo.key());
                    leitorExcel.lerArquivo(arquivo.key(), inputStream);
                    Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Arquivo processado com sucesso: " + arquivo.key());

                    s3Service.deletarArquivo(arquivo.key());
                    Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Arquivo deletado do bucket: " + arquivo.key());
                }
                catch (Exception e)
                {
                    Log.inserirNoLog("Erro ao ler o arquivo " + arquivo.key() + ": " + e.getMessage());
                }
            }
        }

        Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Processamento completo.");

        File logFile = Log.colocarNaPasta();
        try
        {
            //Enviando logs para o S3
            s3Service.enviarArquivo("logs/" + logFile.getName(), logFile);
            Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Log enviado para S3 com sucesso.");
        }
        catch (Exception e)
        {
            Log.inserirNoLog("[" + LocalDateTime.now().format(formatter) + "] Erro ao enviar log para S3: " + e.getMessage());
        }
        System.out.println("Arquivo de log enviado para S3.");
    }
}