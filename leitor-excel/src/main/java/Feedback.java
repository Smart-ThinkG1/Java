public abstract class Feedback
{
    private Integer codEmpresa;
    private String comentario;

    public Feedback() {}

    public Feedback(Integer codEmpresa, String comentario)
    {
        this.codEmpresa = codEmpresa;
        this.comentario = comentario;
    }

    public Integer getCodEmpresa()
    {
        return codEmpresa;
    }

    public void setCodEmpresa(Integer codEmpresa)
    {
        this.codEmpresa = codEmpresa;
    }

    public String getComentario()
    {
        return comentario;
    }

    public void setComentario(String comentario)
    {
        this.comentario = comentario;
    }

    @Override
    public String toString()
    {
        return "Feedback{" +
                "codEmpresa=" + codEmpresa +
                ", comentario='" + comentario + '\'' +
                '}';
    }
}
