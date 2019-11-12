package server;

import java.sql.*;


public class AuthService {
    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:mainDB.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Integer getUserIdByLoginAndPass (String login, String password) {
        String sql = String.format("SELECT id FROM User\n" +
                "WHERE login = '%s'\n" +
                "AND password = '%s'", login, password);

        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean renameUser(String oldLogin, String newLogin) {
        String sql = "SELECT count(id) FROM User WHERE login = '" + newLogin + "'";

        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.getInt(1) != 0) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "UPDATE User SET login = '" + newLogin + "' WHERE login = '" + oldLogin + "'";

        try {
            return (stmt.executeUpdate(sql) > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
