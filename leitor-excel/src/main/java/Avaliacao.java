//Avaliações de usuários do arquivo de Reviews
public class Avaliacao
{
    private String descricao;
    private Integer estrelas;
    private Integer codEmpresa;

    public Avaliacao() {}

    public Avaliacao(String descricao, Integer estrelas)
    {
        this.descricao = descricao;
        this.estrelas = estrelas;
    }

    // Getters e Setters
    public String getDescricao()
    {
        return descricao;
    }
    public void setDescricao(String descricao)
    {
        this.descricao = descricao;
    }

    public Integer getEstrelas()
    {
        return estrelas;
    }
    public void setEstrelas(Integer estrelas)
    {
        this.estrelas = estrelas;
    }

    public Integer getCodEmpresa()
    {
        return codEmpresa;
    }
    public void setCodEmpresa(Integer codEmpresa)
    {
        this.codEmpresa = codEmpresa;
    }

    @Override
    public String toString()
    {
        return "Avaliacao{" +
                ", descricao='" + descricao + '\'' +
                ", estrelas=" + estrelas + '\'' +
                ", Código da empresa=" + codEmpresa +
                '}';
    }
}
