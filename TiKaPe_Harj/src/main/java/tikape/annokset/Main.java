package tikape.annokset;

import static java.lang.Integer.parseInt;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import spark.ModelAndView;
import spark.Spark;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

public class Main {

    public static void main(String[] args) throws Exception {
        
        if (System.getenv("PORT") != null) {
            Spark.port(Integer.valueOf(System.getenv("PORT")));
        }
        System.out.println("Hello world!");
        
        
        Spark.get("/home", (req, res) -> {

            List<Annos> annokset = new ArrayList<>();
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT id,nimi FROM Annos");
            ResultSet tulos_annos = stmt.executeQuery();
            while (tulos_annos.next()) {
                String nimi = tulos_annos.getString("nimi");
                Integer id = tulos_annos.getInt("id");       
                annokset.add(new Annos(id,nimi));
            }
            conn.close();
            HashMap map = new HashMap<>();
            map.put("lista", annokset);
            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());        
        
         Spark.get("/raakaainetilastot", (req, res) -> {
            List<Rtilasto> tilastot = new ArrayList<>();
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT raakaaine.nimi AS nimi, COUNT(*) AS maara FROM annosraakaaine INNER JOIN raakaaine ON annosraakaaine.raakaaine_id = raakaaine.id GROUP BY raakaaine.nimi");
           
            ResultSet tulos = stmt.executeQuery();
            while (tulos.next()) {
                String nimi = tulos.getString("nimi");
                Integer maara = tulos.getInt("maara");
                tilastot.add(new Rtilasto(nimi, maara)); 
                System.out.println(nimi);
            }
            stmt.close();
            tulos.close();
            conn.close();
            
            HashMap map = new HashMap<>();
            
            map.put("lista", tilastot);

            return new ModelAndView(map, "raakaainetilastot");
        }, new ThymeleafTemplateEngine()); 
        
        Spark.get("/annos", (req, res) -> {

            List<Annos> annokset = new ArrayList<>();
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT id,nimi FROM Annos");
            ResultSet tulos = stmt.executeQuery();
            while (tulos.next()) {
                String nimi = tulos.getString("nimi");
                Integer id = tulos.getInt("id");
                Annos a = new Annos(id, nimi);
                annokset.add(a); 
            }
            
            List<Raakaaine> raakaaineet = new ArrayList<>();
            PreparedStatement stmt2 = conn.prepareStatement("SELECT id, nimi FROM Raakaaine");
            ResultSet tulos_raakaaine = stmt2.executeQuery();
            while (tulos_raakaaine.next()) {
                Integer id = tulos_raakaaine.getInt("id");
                String nimi = tulos_raakaaine.getString("nimi");
                Raakaaine r = new Raakaaine(id, nimi);
                raakaaineet.add(r);
            }
            conn.close();
            
            HashMap map = new HashMap<>();
            map.put("lista_a", annokset);
            map.put("lista_r", raakaaineet);
            
            return new ModelAndView(map, "annos");
        }, new ThymeleafTemplateEngine());
            
        Spark.post("/annos", (req, res) -> {
            
            String annos = req.queryParams("annos");
            
            Integer check = checkAnnos(annos);
            
            if (check==0){
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Annos"
                    + " (nimi)"
                    + " VALUES (?)");
                stmt.setString(1, annos);
                stmt.executeUpdate();
                stmt.close();
                conn.close();
                res.redirect("/annos");
                return "";
            } else if (check==1){
                res.redirect("/annos");
                return "";
            } else {
                res.redirect("/annos");
                return "";
            }
            
        });            

        Spark.post("/annos/:id/delete", (req, res) -> {
            Integer id_new = parseInt(req.params(":id"));
            
            Connection conn2 = getConnection();
            List<AnnosRaakaaine> annosraakaaineet = new ArrayList<>();
            PreparedStatement stmt2 = conn2.prepareStatement("DELETE FROM AnnosRaakaaine WHERE AnnosRaakaaine.annos_id= ?");
            stmt2.setInt(1, id_new);
            stmt2.executeUpdate();
            conn2.close();
            
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Annos WHERE Annos.id = ?");
            stmt.setInt(1, id_new);
            stmt.executeUpdate();
            conn.close();

            res.redirect("/annos");
            return "";
        });
        
        
        Spark.get("/annokset/:id", (req, res) -> {
            System.out.println("In annokset");
            Integer id_new = parseInt(req.params(":id"));
            
            
            List<Raakaaine> raakaaineet = new ArrayList<>();
            Connection conn1 = getConnection();
            PreparedStatement stmt1
                    = conn1.prepareStatement("SELECT id, nimi FROM Raakaaine");
            ResultSet tulos = stmt1.executeQuery();

            while (tulos.next()) {
                Integer id = tulos.getInt("id");
                String nimi = tulos.getString("nimi");
                raakaaineet.add(new Raakaaine(id, nimi));
            }
            
            stmt1.close();
            tulos.close();
            conn1.close();            
            
            
            List<AnnosRaakaaine> annosraakaaineet= new ArrayList<>();
            
            HashMap map = new HashMap<>();
 
            
            Connection conn2 = getConnection();
            PreparedStatement stmt2 = conn2.prepareStatement("SELECT * FROM annosraakaaine WHERE annosraakaaine.annos_id = ? ORDER BY annosraakaaine.jarjestys");
            stmt2.setInt(1, id_new);
            ResultSet tulos2 = stmt2.executeQuery();
            
            while (tulos2.next()) {
                String id_temp = tulos2.getString("raakaaine_id");
                Raakaaine aine = raakaaineet.stream().filter(x -> x.getId() == Integer.parseInt(id_temp)).findFirst().get();
                annosraakaaineet.add(new AnnosRaakaaine(tulos2.getInt("id"),aine.getNimi(), tulos2.getInt("annos_id"), tulos2.getInt("jarjestys"), tulos2.getString("maara"), tulos2.getString("ohje")));
            }
            
            map.put("lista", annosraakaaineet);

            stmt2.close();
            tulos2.close();
            conn2.close();
            

            return new ModelAndView(map, "annokset");
        }, new ThymeleafTemplateEngine());
        
        
            
        Spark.get("/raakaaine", (req, res) -> {
            System.out.println("Raakaaineissa");
            List<String> raakaaineet = new ArrayList<>();
            List<Raakaaine> raakaaineet_oliot = new ArrayList<>();
            Connection conn = getConnection();
            PreparedStatement stmt
                    = conn.prepareStatement("SELECT id, nimi FROM Raakaaine");
            ResultSet tulos = stmt.executeQuery();
            
            
            while (tulos.next()) {
                Integer id = tulos.getInt("id");
                String nimi = tulos.getString("nimi");
                Raakaaine raaka_aine_joku = new Raakaaine(id, nimi);
                raakaaineet_oliot.add(raaka_aine_joku);
            }
            conn.close();

            HashMap map = new HashMap<>();

            map.put("lista", raakaaineet_oliot);

            return new ModelAndView(map, "raakaaine");
        }, new ThymeleafTemplateEngine());
        
        
        Spark.post("/raakaaine", (req, res) -> {

            String raakaaine = req.queryParams("raakaaine");
            
            Integer check = checkRaakaaine(raakaaine);
            
            if (check==0){
                System.out.println(check);
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Raakaaine"
                    + " (nimi)"
                    + " VALUES (?)");
                stmt.setString(1, raakaaine);
                stmt.executeUpdate();
                stmt.close();

                conn.close();
                res.redirect("/raakaaine");
                return "";
            } else if (check==1){
                res.redirect("/raakaaine");
                return "";
            } else {
                res.redirect("/raakaaine");
                return "";
            }
        });
            
        Spark.post("/raakaaine/:id/delete", (req, res) -> {
            Integer id_new = parseInt(req.params(":id"));
            
            Connection conn2 = getConnection();
            List<AnnosRaakaaine> annosraakaaineet = new ArrayList<>();
            PreparedStatement stmt2 = conn2.prepareStatement("DELETE FROM AnnosRaakaaine WHERE AnnosRaakaaine.raakaaine_id= ?");
            stmt2.setInt(1, id_new);
            stmt2.executeUpdate();
            conn2.close();
            
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Raakaaine WHERE Raakaaine.id = ?");
            stmt.setInt(1, id_new);
            stmt.executeUpdate();
            
            res.redirect("/raakaaine");
            
            return "";
        
        });
        
        Spark.post("/annosraakaaine",(req, res) -> {
            
            String annos = req.queryParams("annos");
            String raakaaine = req.queryParams("raakaaine");
            try {
                Integer.valueOf(parseInt(req.queryParams("jarjestys")));
                
                Integer jarjestys = parseInt(req.queryParams("jarjestys"));
                String maara = req.queryParams("maara");
                String ohje = req.queryParams("ohje");

                Connection conn = getConnection();
            
                PreparedStatement stmt = conn.prepareStatement("SELECT id FROM Annos WHERE Annos.nimi = ?");
                stmt.setString(1, annos);
                ResultSet tulos = stmt.executeQuery();
                tulos.next();
                Integer annos_id = tulos.getInt("id");
                System.out.println(annos_id);
                stmt.close();

                PreparedStatement stmt2 = conn.prepareStatement("SELECT id FROM Raakaaine WHERE Raakaaine.nimi = ?");
                stmt2.setString(1, raakaaine);
                ResultSet tulos2 = stmt2.executeQuery();
                tulos2.next();
                Integer raakaaine_id = tulos2.getInt("id");
                System.out.println(raakaaine_id);
                stmt2.close();


                PreparedStatement stmt3 = conn.prepareStatement("INSERT INTO annosraakaaine"
                    + " (annos_id, raakaaine_id, jarjestys,maara,ohje)"
                    + " VALUES (?,?,?,?,?)");
                stmt3.setInt(1, annos_id);
                stmt3.setInt(2, raakaaine_id);
                stmt3.setInt(3, jarjestys);
                stmt3.setString(4, maara);
                stmt3.setString(5, ohje);

                stmt3.executeUpdate();
                stmt3.close();

                conn.close();
            } catch (NumberFormatException e){
                System.out.println(e);
            }
            

            res.redirect("/annos");
            return "";
        });
        
        Spark.post("/raakaaineannos/:id/delete", (req, res) -> {
            System.out.println("We are deleting instructions");
            Integer id = parseInt(req.params(":id"));
            Connection conn2 = getConnection();
            PreparedStatement stmt2 = conn2.prepareStatement("DELETE FROM AnnosRaakaaine WHERE id= ?");
            stmt2.setInt(1,id);
            stmt2.executeUpdate();
            conn2.close();
            
            
            res.redirect("/home");
            
        return "";
        
        });
        
       
    }

    public static Connection getConnection() throws Exception {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        if (dbUrl != null && dbUrl.length() > 0) {
            return DriverManager.getConnection(dbUrl);
        }

        return DriverManager.getConnection("jdbc:sqlite:annokset.db");
    }

    public static Integer checkRaakaaine(String TestiNimi) throws Exception {
        Integer check = 0;
        
        List<Raakaaine> raakaaineet = new ArrayList<>();

        Connection conn = getConnection();
        PreparedStatement stmt1 = conn.prepareStatement("SELECT id, nimi FROM Raakaaine");
        ResultSet tulos = stmt1.executeQuery();
        
        while (tulos.next()) {
            Integer id = tulos.getInt("id");
            String nimi = tulos.getString("nimi");
            raakaaineet.add(new Raakaaine(id, nimi));
            }
        
        
        for (int i = 0; i < raakaaineet.size(); i++)            
            if (raakaaineet.get(i).getNimi().toUpperCase().equals(TestiNimi.toUpperCase()) || TestiNimi.trim().isEmpty()) {
        check = 1;
        }
        
        conn.close();
        
        return check;
    }
    
    public static Integer checkAnnos(String TestiNimi) throws Exception {
        Integer check = 0;
        
        List<Annos> annokset = new ArrayList<>();
        Connection conn = getConnection();
        PreparedStatement stmt1 = conn.prepareStatement("SELECT id, nimi FROM Annos");
        ResultSet tulos = stmt1.executeQuery();
        
        while (tulos.next()) {
            Integer id = tulos.getInt("id");
            String nimi = tulos.getString("nimi");
            annokset.add(new Annos(id, nimi));
        }
        
        for (int i = 0; i < annokset.size(); i++)            
            if (annokset.get(i).getNimi().toUpperCase().equals(TestiNimi.toUpperCase()) || TestiNimi.trim().isEmpty()) {
        check = 1;
        }
        
        conn.close();
        
        return check;
    }
    
}
