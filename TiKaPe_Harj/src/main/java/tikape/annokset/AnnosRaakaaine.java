
package tikape.annokset;

public class AnnosRaakaaine {
    
    private String raakaaine;
    private Integer annos_id;
    private Integer jarjestys;
    private String maara;
    private String ohje;
    private Integer id;


    public AnnosRaakaaine(Integer id,String raakaaine, Integer annos_id, Integer jarjestys, String maara, String ohje) {
        this.id = id;
        this.raakaaine = raakaaine;
        this.annos_id = annos_id;
        this.jarjestys = jarjestys;
        this.maara = maara;
        this.ohje = ohje;
 
    }
    
    public String getRaakaaine() {
        return raakaaine;
    }

    public Integer getAnnosid() {
        return annos_id;
    }
    
    public Integer getJarjestys() {
        return jarjestys;
    }
    
    public String getMaara() {
        return maara;
    }
    
    public String getOhje() {
        return ohje;
    }
    
    public Integer getId() {
        return id;
    }
    
}
