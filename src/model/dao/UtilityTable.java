package model.dao;

import model.database.dbConnection;
import model.entity.Utility;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class UtilityTable implements Table<Utility>{
    
    Connection conn = dbConnection.establishConnection();
    ArrayList<Utility> utilitiesList = new ArrayList<Utility>();
    private final String UTILITY_COST_PER_MONTH_QUERY = "select  strftime('%m-%Y', u.date) as date,\n" +
            "sum(u.total) as sum_for_month\n" +
            "from utility u\n" +
            "where u.date between datetime('now', '-1 year') and datetime('now')\n" +
            "group by strftime('%m-%Y', u.date)\n" +
            "order by strftime('%Y', u.date) asc , strftime('%m', u.date) asc;";

    @Override
    public ArrayList<Utility> getAll() {
        
            ArrayList<Utility> resList = new ArrayList<Utility>();
            String sql = "SELECT * FROM utility";
            try {
                Statement stm = conn.createStatement();
                ResultSet resultSet = stm.executeQuery(sql);

                while (resultSet.next()) {
                    Utility u= new Utility(resultSet.getInt("numberId"),resultSet.getInt("total"), resultSet.getString("type"),resultSet.getDate("date").toLocalDate());
                    resList.add(u);
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
            utilitiesList = resList;
            return utilitiesList;
    }

    @Override
    public boolean save(Utility u) {
        //lo inserisce nella lista e nel db
        //se il dipendente è nella lista significa che è stato già inserito nel db
        
        boolean res = false;
        if(utilitiesList.indexOf(u)<0){
            
            String sql= "INSERT INTO Utility (numberId, total, type, date) VALUES (?,?,?,?)";
            try {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, u.getNumberId());
                ps.setInt(2, (int)u.getTotal());
                ps.setString(3, u.getType());
                ps.setDate(4, u.getDate());
                ps.execute();
                
                res = true;
            } catch (SQLException ex) {
                
                ex.printStackTrace();
            }

            utilitiesList.add(u); 
        }
        return res;
    }

    @Override
    public boolean update (Utility u) {
        //t deve essere un istanza di Product con lo stesso identificativo 
        //dell'istanza che si vuole modificare
        boolean res = false;
         if(utilitiesList.indexOf(u)>0){ //seleziono istanza corretta
            
            String sql= "UPDATE Utility  SET numberId = ?, total = ? , type =? , date = ? WHERE numberId = ?";
            try {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, u.getNumberId());
                ps.setInt(2, (int)u.getTotal());
                ps.setString(3, u.getType());
		ps.setInt(4, u.getNumberId());
                ps.setDate(5, u.getDate());
                ps.execute();
                
                res = true;
            } catch (SQLException ex) {
                
                ex.printStackTrace();
            }

         }  
         return res;
    }
  

    @Override
    public boolean delete(Utility u) {
        
        boolean res = false;
	if(utilitiesList.indexOf(u)>0){
            
            String sql= "DELETE FROM Utility WHERE numberId = ?";
            try {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, u.getNumberId());
                //ps.execute();
                
                res = true;

            } catch (SQLException ex) {
                
                ex.printStackTrace();
            }
        }
        this.utilitiesList.remove(u);
        return res;
    }
    
    
    public ArrayList<Utility> getFrom (Object searchParam, String paramName) { 
        
        ArrayList<Utility> resList = new ArrayList<Utility>();
        if(searchParam instanceof String){   //passso un oggetto di ricerca textinput
         
        String sql= "SELECT * FROM Utility WHERE type =? ORDER BY date";
        try {
                PreparedStatement ps = conn.prepareStatement(sql); 
                ps.setString(1, (String) searchParam);
                ps.execute();
                ResultSet resultSet = ps.executeQuery();
                
                while (resultSet.next()) {
                    Utility u = new Utility(resultSet.getInt("numberId"), resultSet.getInt("total"),resultSet.getString("type"),resultSet.getDate("date").toLocalDate());
                    resList.add(u);
                }
            }
            catch (SQLException ex) {
        
                ex.printStackTrace();
            }   
        } 
        
        else if (searchParam instanceof Integer) {
            
        String sql=  "SELECT * FROM Utility WHERE numberId=? OR total = ? ORDER BY date"; 
       
        
            try {
                PreparedStatement ps = conn.prepareStatement(sql); 
                ps.setInt(1, (int) searchParam);
                ps.setInt(2, (int) searchParam);
                ps.execute();
                ResultSet resultSet = ps.executeQuery();
                
                while (resultSet.next()) {
                    Utility u = new Utility(resultSet.getInt("numberId"), resultSet.getInt("total"),resultSet.getString("type"),resultSet.getDate("date").toLocalDate());
                    resList.add(u);
                }
            }
            catch (SQLException ex) {
        
                ex.printStackTrace();
            }  
        }
            
        else if (searchParam instanceof LocalDate) {
            
        String sql=  "SELECT * FROM Utility WHERE date =? ORDER BY date"; 
       
        
            try {
                PreparedStatement ps = conn.prepareStatement(sql); 
                ps.setDate(1, (Date) searchParam);
                ps.execute();
                ResultSet resultSet = ps.executeQuery();
                
                while (resultSet.next()) {
                    Utility u = new Utility(resultSet.getInt("numberId"), resultSet.getInt("total"),resultSet.getString("type"),resultSet.getDate("date").toLocalDate());
                    resList.add(u);
                }
            }
            catch (SQLException ex) {
        
                ex.printStackTrace();
            }
        }     
         return resList;
    }

    public LinkedHashMap<String, Double> getUtilityCostPerMonth(){
        LinkedHashMap<String, Double> res = null;
        try{
            Statement stm = conn.createStatement();
            ResultSet resultSet = stm.executeQuery(this.UTILITY_COST_PER_MONTH_QUERY);
            res = new LinkedHashMap<String, Double>();
            while (resultSet.next()) {
                String month = resultSet.getString("date");
                Double cost = resultSet.getDouble("sum_for_month");
                res.put(month, cost);
            }
        }catch (SQLException ex){
            System.out.println(ex.toString());
        }
        return res;
    }
    
    @Override
    public Utility constructEntityFromMap(HashMap<String, Object> map) {
        int numberId =(int) map.get("numberId");
        int total =(int) map.get("total");
        String type =(String) map.get("type");
        LocalDate date =(LocalDate) map.get("date");
        
        return new Utility(numberId, total, type, date);
    }
    
}
