package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polimi.tiw.Bean.FasciaDiSpedizione;
import it.polimi.tiw.Bean.Fornitore;

public class DAO_Fornitore {

    private final Connection connessione;

    public DAO_Fornitore(Connection connessione) {
        this.connessione = connessione;
    }

    public Fornitore getFornitore(int idFornitore) throws SQLException {
    	Double sogliaSpedizione;
    	
    	// prendo il fornitore
    	String query = "SELECT * FROM FORNITORE WHERE Id = ?";
    	
    	// pre-compila la query 1 se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);
        // imposto il parametro della query
        statement.setInt(1,idFornitore);
        ResultSet resultSet = statement.executeQuery();

        // se non l'ho trovato, ritorno null
        if( !resultSet.next() ) return null;

        // gli attributi che possono essere null vanno trattati separatamente
        sogliaSpedizione = resultSet.getDouble("SogliaSpedizioneGratuita");
        if( resultSet.wasNull() )
            sogliaSpedizione = null;
        
        // ritorno il fornitore
        return new Fornitore(resultSet.getInt("Id"), resultSet.getString("Nome"), resultSet.getDouble("Valutazione"), sogliaSpedizione, this.getFasceDiSpedizione(resultSet.getInt("Id")));
    }

	public List<FasciaDiSpedizione> getFasceDiSpedizione(int idFornitore) throws SQLException {
		List<FasciaDiSpedizione> fasceDiSpedizione;
		
		// prendo le fasce di spedizione del fornitore
		String query = "SELECT * FROM FASCIASPEDIZIONE WHERE IdFornitore = ?";
	
		// pre-compila la query 1 se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);
        // imposto il parametro della query
        statement.setInt(1, idFornitore);
        // eseguo la query
        ResultSet resultSet = statement.executeQuery();

        // istanzio la lista da ritornare
        fasceDiSpedizione = new ArrayList<>();

        // metto tutte le fasce nel risultato
        while( resultSet.next() ){
        	FasciaDiSpedizione fasciaDiSpedizione;
        	
        	// gli attributi che possono essere null vanno trattati separatamente
            Integer numeroMassimoArticoli = resultSet.getInt("Max");
            if( resultSet.wasNull() )
                numeroMassimoArticoli = null;

            // aggiungo la fascia corrente alla lista
            fasceDiSpedizione.add(new FasciaDiSpedizione(resultSet.getInt("id"), resultSet.getInt("IdFornitore"), resultSet.getInt("Min"), numeroMassimoArticoli, resultSet.getDouble("Prezzo")));
        }
        // ritorno il risultato
        return fasceDiSpedizione;
	}
	
	public Map<Fornitore, Double> getFornitori(int idProdotto) throws SQLException {
		Map<Fornitore, Double> fornitori;
		
		// prendo fornitore e prezzo relativo per il prodotto richiesto
        String query = "SELECT F.*, Round((Prezzo*(1-Sconto)),2) AS Prezzo FROM PRODOTTO_FORNITORE PDF INNER JOIN FORNITORE F ON PDF.IdFornitore = F.Id WHERE IdProdotto = ?";
        
        // pre-compila la query 1 se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);
        // imposto il parametro della query
        statement.setInt(1, idProdotto);
        // eseguo la query
        ResultSet resultSet = statement.executeQuery();
        
        // istanzio la lista da ritornare
        fornitori = new HashMap<>();

        // aggiungo alla mappa tutti i fornitori-prezzi
        while( resultSet.next() ){
        	
        	// gli attributi che possono essere null vanno trattati separatamente
            Double sogliaSpedizione = resultSet.getDouble("SogliaSpedizioneGratuita");
            if( resultSet.wasNull() )
                sogliaSpedizione = null;

            // aggiungo fornitore e prezzo alla mappa
            fornitori.put(new Fornitore(resultSet.getInt("Id"), resultSet.getString("Nome"), resultSet.getDouble("Valutazione"), sogliaSpedizione, this.getFasceDiSpedizione(resultSet.getInt("Id"))), resultSet.getDouble("Prezzo"));
        }
        // ritorno il risultato
        return fornitori;
    }
	
	public Integer getCostoSpedizione(int idFornitore, int numArticoli) throws SQLException {
		
		// cerco l'UNICA (si assumono fasce disgiunte) fascia di spedizione definita per il dato numero di articoli
        String query = "SELECT Prezzo FROM FASCIASPEDIZIONE WHERE IdFornitore = ? AND Min <= ? AND (Max IS NULL OR Max >= ?);";
        
        // pre-compila la query 1 se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);
        // imposto i parametri della query
        statement.setInt(1, idFornitore);
        statement.setInt(2, numArticoli);
        statement.setInt(3, numArticoli);
        // eseguo la query
        ResultSet rs = statement.executeQuery();

        // se non ho trovato la fascia ritorno null, altrimenti ritorno il prezzo
        if( !rs.next() )
        	return null;
        else
        	return rs.getInt("Prezzo");
    }

}