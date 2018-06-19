
package tikape.annokset;


public class Rtilasto {
    
    private String nimi;
    private Integer maara;
    
    
    public Rtilasto(String nimi, Integer maara) {
        this.nimi = nimi;
        this.maara = maara;
    }
    
    public String getNimi() {
        return nimi;
    }
    
    public Integer getMaara() {
        return maara;
    }
    
}
