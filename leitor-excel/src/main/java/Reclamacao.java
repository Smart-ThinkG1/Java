//Reclamações vindas do reclame aqui
public class Reclamacao
{
    private String titulo;
    private String corpo;
    private Integer codEmpresa;

    public Reclamacao() {}

    public Reclamacao(String titulo, String corpo)
    {
        this.titulo = titulo;
        this.corpo = corpo;
    }

    // Getters e Setters
    public String getTitulo()
    {
        return titulo;
    }
    public void setTitulo(String titulo)
    {
        this.titulo = titulo;
    }

    public String getCorpo()
    {
        return corpo;
    }
    public void setCorpo(String corpo)
    {
        this.corpo = corpo;
    }

    public Integer getCodEmpresa()
    {
        return codEmpresa;
    }
    public void setCodEmpresa(Integer codEmpresa)
    {
        this.codEmpresa = codEmpresa;
    }

    // Metodo toString para exibir informações da Avaliação
    @Override
    public String toString()
    {
        return "Reclamacao{" +
                ", titulo='" + titulo + '\'' +
                ", corpo='" + corpo + '\'' +
                ", Código da empresa=" + codEmpresa +
                '}';
    }
}
