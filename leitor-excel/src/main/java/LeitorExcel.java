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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeitorExcel
{
    private final DBConnectionProvider dbConnectionProvider;
    private final SlackNotifier slackNotifier;

    // Construtor
    public LeitorExcel(DBConnectionProvider dbConnectionProvider, SlackNotifier slackNotifier)
    {
        this.dbConnectionProvider = dbConnectionProvider;
        this.slackNotifier = slackNotifier;
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

            // Armazenando a primeira página para acesso
            Sheet sheet = workbook.getSheetAt(0);
            System.out.println("Processando a planilha: " + sheet.getSheetName());

            // Armazenando o id da empresa vinculada ao arquivo
            Integer idEmpresa = extrairIdEmpresa(nomeArquivo);
            System.out.println("ID da empresa extraído: " + idEmpresa);

            // Obtém os emails dos usuários do Slack
            Map<String, String> usuariosSlack = slackNotifier.listarUsuariosSlack();

            // Busca o email do usuário
            String emailUsuario = dbConnectionProvider.buscarEmailUsuario(idEmpresa);
            System.out.println("Email do usuário encontrado: " + emailUsuario);

            // Envia notificação se o email estiver presente
            if (usuariosSlack.containsKey(emailUsuario))
            {
                //Guarda o id do slack vinculado ao e-mail
                String userId = usuariosSlack.get(emailUsuario);

                //Formatação da data do processamento
                String dataProcessamento = new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date());
                slackNotifier.enviarNotificacao(userId,
                        "Um novo conjunto de dados sobre sua empresa foi processado em " + dataProcessamento +
                                ". Acesse sua Dashboard e confira as atualizações.");

                System.out.println("Notificação enviada para o usuário: " + emailUsuario);
            }
            else
            {
                System.out.println("E-mail do usuário não encontrado na lista do Slack.");
            }

            processarArquivo(tipoArquivo, sheet, idEmpresa);

            workbook.close();

            System.out.println("Workbook fechado com sucesso.");
        }
        catch (IOException e)
        {
            System.err.println("Erro de ao ler o arquivo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String identificarTipoArquivo(String tipoArquivo)
    {
        if (tipoArquivo.contains("Reclamacoes"))
        {
            return "Reclamacoes";
        }
        else if (tipoArquivo.contains("Avaliacoes"))
        {
            return "Avaliacoes";
        }
        else if (tipoArquivo.contains("DemandaPesquisa"))
        {
            return "DemandaPesquisa";
        }
        return "Não foi possível identificar o arquivo!";
    }

    private void processarArquivo(String tipoArquivo, Sheet sheet, Integer idEmpresa)
    {
        int lastRowNum = sheet.getLastRowNum();
        System.out.println("Número de linhas do arquivo: " + lastRowNum);

        for (Row row : sheet)
        {
            System.out.println("Processando linha: " + row.getRowNum());

            // Ignora a primeira linha (cabeçalho)
            if (row.getRowNum() == 0)
            {
                System.out.println("Ignorando linha de cabeçalho.");
                continue;
            }

            // Se a linha for maior que a última linha com dados, para a leitura
            if (row.getRowNum() > lastRowNum)
            {
                System.out.println("Linha além da última linha com dados. Parando a leitura.");
                break;
            }

            // Verifica se todas as células estão nulas ou vazias
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
                System.out.println("Linha vazia encontrada, pulando para a próxima.");
                continue;
            }

            // Processa a linha com base no tipo de arquivo
            switch (tipoArquivo)
            {
                case "Reclamacoes":
                    processarReclamacoes(row, idEmpresa);
                    break;
                case "Avaliacoes":
                    processarAvaliacoes(row, idEmpresa);
                    break;
                case "DemandaPesquisa":
                    processarDemandaPesquisa(row, idEmpresa);
                    break;
                default:
                    System.out.println("Tipo de arquivo desconhecido: " + tipoArquivo);
                    break;
            }
        }
    }

    private void processarReclamacoes(Row row, Integer idEmpresa)
    {
        Reclamacao reclamacao = new Reclamacao();

        //Valida a célula da planilha e ceta o valor na classe
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
        }
        else
        {
            reclamacao.setComentario(null);
            System.out.println("Corpo: VALOR NULO");
        }

        reclamacao.setCodEmpresa(idEmpresa);
        dbConnectionProvider.insertReclamacao(reclamacao);
    }

    private void processarAvaliacoes(Row row, Integer idEmpresa)
    {
        Avaliacao avaliacao = new Avaliacao();

        Cell cell0 = row.getCell(0);
        //Valida a célula da planilha e ceta o valor na classe
        if (cell0 == null || cell0.getCellType() != CellType.STRING)
        {
            avaliacao.setComentario(null);
            System.out.println("Nota: VALOR NULO ou CÉLULA INVÁLIDA");
        }
        else
        {
            String descricao = cell0.getStringCellValue();
            avaliacao.setComentario(descricao.replaceAll("[^\\p{ASCII}]", ""));
            System.out.println("Nota: " + avaliacao.getComentario());
        }

        Cell cell1 = row.getCell(1);
        if (cell1 == null || cell1.getCellType() != CellType.NUMERIC)
        {
            avaliacao.setEstrelas(0);
            System.out.println("Estrelas: VALOR NULO");
        }
        else
        {
            avaliacao.setEstrelas((int) cell1.getNumericCellValue());
            System.out.println("Estrelas: " + avaliacao.getEstrelas());
        }

        avaliacao.setCodEmpresa(idEmpresa);
        dbConnectionProvider.insertAvaliacao(avaliacao);
    }

    private void processarDemandaPesquisa(Row row, Integer idEmpresa)
    {
        DemandaPesquisa demandaPesquisa = new DemandaPesquisa();

        // Formatar a data no padrão yyyy-MM-dd
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        //Valida a célula da planilha e ceta o valor na classe
        if (row.getCell(0) != null && row.getCell(0).getCellType() == CellType.NUMERIC)
        {
            Date dataExcel = row.getCell(0).getDateCellValue();
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
        if (matcher.find())
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