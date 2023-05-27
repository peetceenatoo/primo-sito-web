package it.polimi.tiw.Utility;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class ConnectionInitializer {

    public static Connection getConnection(ServletContext context) throws UnavailableException {
        try {
            String dbDriver = context.getInitParameter("dbDriver");
            String dbUrl = context.getInitParameter("dbUrl");
            String dbUser = context.getInitParameter("dbUser");
            String dbPassword = context.getInitParameter("dbPassword");
            Class.forName(dbDriver);
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (ClassNotFoundException e) {
            throw new UnavailableException("Can't load database driver");
        } catch (SQLException e) {
            throw new UnavailableException("Can't connect to database");
        }
    }
}
