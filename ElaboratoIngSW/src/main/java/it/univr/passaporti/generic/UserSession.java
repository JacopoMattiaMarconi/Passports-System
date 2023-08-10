package it.univr.passaporti.generic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class UserSession {

    private static UserSession instance;

    private static String cf;

    private static Connection conn = null;

    private UserSession(String cf) {
        this.cf = cf;
    }

    public static UserSession getInstace(String userName) {
        if(instance == null) {
            instance = new UserSession(userName);
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/passaporti", "root", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public static String getUserName() {
        return cf;
    }

    public static void cleanUserSession() {
        cf = null;
        instance=null;
        try {
            conn.close();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "UserSession{" + "userName='" + cf + '\'' + '}';
    }
}