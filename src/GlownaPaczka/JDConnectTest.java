package GlownaPaczka;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class JDConnectTest {
    public static void main(String[] args) {
        System.out.println("-------- PostgreSQL "+ "JDBC Connection Testing ------------");
        try{
            Class.forName("org.postgresql.Driver");
        }catch(ClassNotFoundException e){
            System.out.println("Where is your PostgreSQL JDBC Driver? "
                    + "Include in your library path!");
            e.printStackTrace();
            return;
        }
        System.out.println("PostgreSQL JDBC Driver Registered!");
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/Zaliczenie", "Admin", "proba123");
        }catch(SQLException e){
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }
        if(connection != null){
            System.out.println("Udalo sie polaczyc");
        }
        else{
            System.out.println("Nie udalo sie polaczyc");
        }
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            String sql = "";
            stmt.close();
            connection.close();
        }catch(Exception e ){
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }
        System.out.println("Poprawnie dodano rekordy");
    }
}
