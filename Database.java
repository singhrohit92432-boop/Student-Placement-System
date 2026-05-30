package placement;

import java.sql.Connection;
import java.sql.DriverManager;

class Database {

    public static Connection connect() {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            String url =
                "jdbc:mysql://bohxxds7qm1j2synq7nu-mysql.services.clever-cloud.com:3306/bohxxds7qm1j2synq7nu?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

            String user =
                "ujaxoeww3d0blqwi";

            String password =
                "qFqN09LdHDdG7k0v7hbV";

            Connection con =
                DriverManager.getConnection(url, user, password);

            System.out.println("Connected to Clever Cloud MySQL");

            return con;

        } catch (Exception e) {

            System.out.println("Database connection failed");
            e.printStackTrace();
            return null;
        }
    }
}