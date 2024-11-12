import java.sql.Time;
import java.util.Date;

//Demanda de pesquisas do Google Trends
public class DemandaPesquisa
{
    private Date data;
    private Integer demanda;
    private Integer codEmpresa;

    public DemandaPesquisa() {}

    public DemandaPesquisa(Date data, Time time, Integer demanda)
    {
        this.data = data;
        this.demanda = demanda;
    }

    // Getters e Setters
    public Date getData()
    {
        return data;
    }
    public void setData(Date data)
    {
        this.data = data;
    }

    public Integer getDemanda()
    {
        return demanda;
    }
    public void setDemanda(Integer demanda)
    {
        this.demanda = demanda;
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
        return "DemandaPesquisa{" +
                "data=" + data +
                ", demanda=" + demanda + '\'' +
                ", Código da empresa=" + codEmpresa +
                '}';
    }
}