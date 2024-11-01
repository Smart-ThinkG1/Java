import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;


public class S3Provider
{
    private final AwsSessionCredentials credentials;
    private final String bucketName = "smart-think-s3";

    // Inicializa as credenciais com as variáveis de ambiente da AWS
    public S3Provider()
    {
        this.credentials = AwsSessionCredentials.create(
                System.getenv("AWS_ACCESS_KEY_ID"),
                System.getenv("AWS_SECRET_ACCESS_KEY"),
                System.getenv("AWS_SESSION_TOKEN")
        );
    }

    // Cria e retorna um cliente S3
    public S3Client getS3Client()
    {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(() -> credentials)
                .build();
    }

    public List<S3Object> listarArquivos() {
        ListObjectsRequest listObjects = ListObjectsRequest.builder()
                .bucket(bucketName)
                .build();
        List<S3Object> objects = getS3Client().listObjects(listObjects).contents();

        // Filtrando arquivos que não contenham "logs" no nome
        List<S3Object> arquivosFiltrados = objects.stream()
                .filter(obj -> !obj.key().contains("logs"))
                .collect(Collectors.toList());

        for (S3Object object : arquivosFiltrados) {
            System.out.println(object.key());
        }
        return arquivosFiltrados;
    }

    public InputStream obterArquivo(String fileName) throws IOException
    {
        System.out.println("Obtendo arquivo do bucket: " + fileName);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        return getS3Client().getObject(getObjectRequest);
    }

    public void deletarArquivo(String fileName)
    {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        getS3Client().deleteObject(deleteObjectRequest);
    }

    public void enviarLog(String keyName, Path filePath)
    {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        getS3Client().putObject(putObjectRequest, filePath);
    }
}
