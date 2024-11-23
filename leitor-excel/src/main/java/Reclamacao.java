public class Reclamacao extends Feedback
{
    private String titulo;

    public Reclamacao() {}

    public Reclamacao(Integer codEmpresa, String comentario, String titulo)
    {
        super(codEmpresa, comentario);
        this.titulo = titulo;
    }

    public String getTitulo()
    {
        return titulo;
    }

    public void setTitulo(String titulo)
    {
        this.titulo = titulo;
    }

    @Override
    public String toString()
    {
        return "Reclamacao{" +
                "titulo='" + titulo + '\'' +
                ", " + super.toString() +
                '}';
    }
}
