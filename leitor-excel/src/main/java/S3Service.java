import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class S3Service
{
    private final S3Provider s3Provider;

    public S3Service(S3Provider s3Provider)
    {
        this.s3Provider = s3Provider;
    }

    public List<S3Object> listarArquivos()
    {
        ListObjectsRequest listObjects = ListObjectsRequest.builder()
                .bucket("smart-think-s3")
                .build();
        return s3Provider.getS3Client().listObjects(listObjects).contents()
                .stream()
                .filter(obj -> !obj.key().contains("logs")) // Filtrando arquivos que não contenham "logs"
                .collect(Collectors.toList());
    }

    public InputStream obterArquivo(String fileName) throws IOException
    {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("smart-think-s3")
                .key(fileName)
                .build();
        return s3Provider.getS3Client().getObject(getObjectRequest);
    }

    public void deletarArquivo(String fileName)
    {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket("smart-think-s3")
                .key(fileName)
                .build();
        s3Provider.getS3Client().deleteObject(deleteObjectRequest);
    }

    public void enviarArquivo(String key, File file)
    {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("smart-think-s3")
                .key(key)
                .build();
        s3Provider.getS3Client().putObject(putObjectRequest, RequestBody.fromFile(file));
    }
}