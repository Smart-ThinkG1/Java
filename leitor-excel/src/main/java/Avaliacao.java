public class Avaliacao extends Feedback
{
    private Integer estrelas;

    public Avaliacao() {}

    public Avaliacao(Integer codEmpresa, String comentario, Integer estrelas)
    {
        super(codEmpresa, comentario);
        this.estrelas = estrelas;
    }

    public Integer getEstrelas()
    {
        return estrelas;
    }

    public void setEstrelas(Integer estrelas)
    {
        this.estrelas = estrelas;
    }

    @Override
    public String toString()
    {
        return "Avaliacao{" +
                "estrelas=" + estrelas +
                ", " + super.toString() +
                '}';
    }
}
