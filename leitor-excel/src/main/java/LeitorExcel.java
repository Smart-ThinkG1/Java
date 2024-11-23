import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeitorExcel
{
    private final DBConnectionProvider dbConnectionProvider;

    public LeitorExcel(DBConnectionProvider dbConnectionProvider)
    {
        this.dbConnectionProvider = dbConnectionProvider;
    }

    public void lerArquivo(String nomeArquivo, InputStream arquivo)
    {
        try
        {
            String tipoArquivo = identificarTipoArquivo(nomeArquivo);
            Workbook workbook;

            // Criando o Workbook baseado no tipo de arquivo
            if (nomeArquivo.endsWith(".xlsx"))
            {
                workbook = new XSSFWorkbook(arquivo);
            }
            else
            {
                workbook = new HSSFWorkbook(arquivo);
            }

            //Armazenando a primeira página para acesso
            Sheet sheet = workbook.getSheetAt(0);

            //Armazenando o id da empresa vínculada ao arquivo
            Integer idEmpresa = extrairIdEmpresa(nomeArquivo);

            processarArquivo(tipoArquivo, sheet,idEmpresa);
            System.out.println(idEmpresa);

            workbook.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String identificarTipoArquivo(String tipoArquivo)
    {
        if (tipoArquivo.contains("Reclamacoes"))
        {
            return "Reclamacoes";
        } else if (tipoArquivo.contains("Avaliacoes"))
        {
            return "Avaliacoes";
        }else if (tipoArquivo.contains("DemandaPesquisa"))
        {
            return "DemandaPesquisa";
        }
        return "Não foi possível identificar o arquivo!";
    }


    private void processarArquivo(String tipoArquivo, Sheet sheet,Integer idEmpresa)
    {
        // Obtém o número da última linha com dados reais
        int lastRowNum = sheet.getLastRowNum();
        System.out.println("Número de linhas do arquivo: "+lastRowNum);

        for (Row row : sheet)
        {
            // Ignora a primeira linha (cabeçalho)
            if (row.getRowNum() == 0)
            {
                continue;
            }

            // Se a linha for maior que a última linha com dados, para a leitura
            if (row.getRowNum() > lastRowNum)
            {
                break;
            }

            // Verifica se todas as células que você espera na linha estão nulas ou vazias
            boolean linhaVazia = true;
            for (int cn = 0; cn < row.getLastCellNum(); cn++)
            {
                Cell cell = row.getCell(cn);
                if (cell != null && cell.getCellType() != CellType.BLANK)
                {
                    linhaVazia = false;
                    break;
                }
            }

            // Se a linha está vazia, pula para a próxima
            if (linhaVazia)
            {
                continue;
            }

            // Processa a linha com base no tipo de arquivo
            switch (tipoArquivo)
            {
                case "Reclamacoes":
                    processarReclamacoes(row,idEmpresa);
                    break;
                case "Avaliacoes":
                    processarAvaliacoes(row,idEmpresa);
                    break;
                case "DemandaPesquisa":
                    processarDemandaPesquisa(row,idEmpresa);
                    break;
                default:
                    System.out.println("Tipo de arquivo desconhecido");
                    break;
            }
        }
    }


    //Métodos de processamento específico para cada tipo de arquivo
    private void processarReclamacoes(Row row,Integer idEmpresa)
    {
        Reclamacao reclamacao = new Reclamacao();

        if (row.getCell(0) != null && !row.getCell(0).getStringCellValue().trim().isEmpty())
        {
            reclamacao.setTitulo(row.getCell(0).getStringCellValue());
            System.out.println("Título: " + reclamacao.getTitulo());
        }
        else
        {
            reclamacao.setTitulo(null);
            System.out.println("Título: VALOR NULO");
        }

        if (row.getCell(1) != null && !row.getCell(1).getStringCellValue().trim().isEmpty())
        {
            reclamacao.setComentario(row.getCell(1).getStringCellValue());
            System.out.println("Corpo: " + reclamacao.getComentario());
        } else
        {
            reclamacao.setComentario(null);
            System.out.println("Corpo: VALOR NULO");
        }

        reclamacao.setCodEmpresa(idEmpresa);

        dbConnectionProvider.insertReclamacao(reclamacao);
    }

    private void processarAvaliacoes(Row row, Integer idEmpresa) {
        Avaliacao avaliacao = new Avaliacao();

        Cell cell0 = row.getCell(0);

        // Expressão regular para caracteres indesejados
        String padraoIndesejado = "[^\\p{ASCII}]|[ÂÃ]";
        Pattern pattern = Pattern.compile(padraoIndesejado);

        if (cell0.getCellType() == CellType.STRING && !cell0.getStringCellValue().trim().isEmpty()) {
            String descricao = cell0.getStringCellValue();

            // Verifica se a descrição contém caracteres indesejados
            Matcher matcher = pattern.matcher(descricao);
            if (matcher.find()) {
                avaliacao.setComentario(null);
                System.out.println("Nota: CARACTERES INVÁLIDOS DETECTADOS");
            } else {
                avaliacao.setComentario(descricao);
                System.out.println("Nota: " + avaliacao.getComentario());
            }
        } else {
            avaliacao.setComentario(null);
            System.out.println("Nota: VALOR NULO");
        }

        Cell cell1 = row.getCell(1);

        if (cell1.getCellType() == CellType.NUMERIC) {
            avaliacao.setEstrelas((int) cell1.getNumericCellValue());
            System.out.println("Estrelas: " + avaliacao.getEstrelas());
        } else {
            avaliacao.setEstrelas(0);
            System.out.println("Estrelas: VALOR NULO");
        }

        avaliacao.setCodEmpresa(idEmpresa);

        dbConnectionProvider.insertAvaliacao(avaliacao);
    }

    private void processarDemandaPesquisa(Row row, Integer idEmpresa)
    {
        DemandaPesquisa demandaPesquisa = new DemandaPesquisa();

        // Formatar a data no padrão yyyy-MM-dd
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        if (row.getCell(0) != null && row.getCell(0).getCellType() == CellType.NUMERIC)
        {
            Date dataExcel = row.getCell(0).getDateCellValue();
            // Formatar a data para o formato que você deseja
            String dataFormatada = dateFormatter.format(dataExcel);
            demandaPesquisa.setData(java.sql.Date.valueOf(dataFormatada));
            System.out.println("Data: " + dataFormatada);
        }
        else
        {
            demandaPesquisa.setData(null);
            System.out.println("Data: VALOR NULO");
        }


        if (row.getCell(1) != null && row.getCell(1).getCellType() == CellType.NUMERIC)
        {
            demandaPesquisa.setDemanda((int) row.getCell(1).getNumericCellValue());
            System.out.println("Demanda: " + demandaPesquisa.getDemanda());
        }
        else
        {
            demandaPesquisa.setDemanda(0);
            System.out.println("Demanda: VALOR NULO");
        }

        demandaPesquisa.setCodEmpresa(idEmpresa);
        dbConnectionProvider.insertDemandaPesquisa(demandaPesquisa);
    }

    private static Integer extrairIdEmpresa(String nomeArquivo)
    {
        Pattern pattern = Pattern.compile(".*id(\\d+)");
        Matcher matcher = pattern.matcher(nomeArquivo);
        if (matcher.find()) // Usa find() em vez de matches()
        {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private LocalDate converterDate(Date data)
    {
        return data.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
