package it.polimi.tiw.DAO;

import it.polimi.tiw.Bean.Utente;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

public class DAO_Utente {

    private final Connection connessione;

    public DAO_Utente(Connection connessione) {
        this.connessione = connessione;
    }


    public boolean isRegistrato(String email) throws SQLException {
        // prendo la mail
    	String query = "SELECT Email FROM UTENTE WHERE BINARY Email = ?";
    	
    	// pre-compila la query 1 se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);
        // imposto il parametro della query
        statement.setString(1, email);
        // eseguo la query
        ResultSet resultSet = statement.executeQuery();
        
        // ritorno se ho trovato l' email nel database
        return resultSet.next();
    }

    public Utente getUtenteConPassword(String email, String password) throws SQLException {
    	
    	// prendo l'utente dal database
        String query = "SELECT * FROM UTENTE WHERE BINARY Email = ? AND BINARY Password = ?";
        
        // pre-compila la query 1 se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);
        // imposto i parametri della query
        statement.setString(1, email);
        statement.setString(2, password);
        // eseguo la query
        ResultSet resultSet = statement.executeQuery();
        
        // se la coppia email-password non è corretta
        if ( !resultSet.next() )
            return null;
        
        // altrimenti ritorno l'utente
        return new Utente(resultSet.getString("Email"), resultSet.getString("Nome"), resultSet.getString("Cognome"), resultSet.getString("Indirizzo"));
    }

    // controlla che la parte prima della chiocciola sia valida (compresa o meno tra virgolette) (([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))
    // controlla che segua una chiocciola @
    // controlla che la seconda parte sia valida (ip o dominio) ((\\[\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}])|(([a-zA-Z\\-\\d]+\\.)+[a-zA-Z]{2,}))
    public static boolean isValida(String email) {
        return ( email != null ) && ( email.matches("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}])|(([a-zA-Z\\-\\d]+\\.)+[a-zA-Z]{2,}))$") ) && ( email.length() <= 50 );
    }



}
