package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import it.polimi.tiw.Bean.Coppia;
import it.polimi.tiw.Bean.DettaglioOrdine;
import it.polimi.tiw.Bean.Ordine;
import it.polimi.tiw.Bean.ProdottoDiUnFornitore;
import it.polimi.tiw.Bean.Utente;

public class DAO_Ordine{

	private final Connection connessione;

    public DAO_Ordine(Connection connessione) {
        this.connessione = connessione;
    }
    
    public List<Ordine> getOrdini(String email) throws SQLException {
    	List<Ordine> ordini;
    	
    	// prendo tutti gli ordini dell'utente e li ordino per data di spedizione
        String query1 = "SELECT * FROM ORDINE WHERE Email = ? ORDER BY DataSpedizione DESC";
        // prendo tutti i dettagli dell'ordine per un ordine parametrico
        String query2 = "SELECT * FROM DETTAGLIO_ORDINE WHERE IdOrdine = ?";
        
        // pre-compila la query 1 se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query1);
        // imposto il parametro della query
        statement.setString(1, email);
        // eseguo la query
        ResultSet resultSet = statement.executeQuery();
        // pre-compila la query 1 se sintatticamente corretta
        PreparedStatement statement2 = connessione.prepareStatement(query2);
        
        // istanzio la lista da ritornare
        ordini = new ArrayList<>();
        // metto tutti gli ordini nel risultato
        while( resultSet.next() ){
        	List<Coppia<DettaglioOrdine, String>> dettagli;
        	
        	// imposto l'id dell'ordine corrente come parametro #1 della query 2
            statement2.setInt(1, resultSet.getInt("Id"));
            // pre-compila la query 1 se sintatticamente corretta
            ResultSet resultSet2 = statement2.executeQuery();

            // istanzio la lista con i dettagli
            dettagli = new ArrayList<>();
            
            DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);

            // aggiungo tutti i dettagli dell'ordine corrente 
            while( resultSet2.next() )
            	dettagli.add(new Coppia<DettaglioOrdine,String>(new DettaglioOrdine(resultSet2.getInt("IdOrdine"), resultSet2.getInt("IdProdotto"), resultSet2.getDouble("PrezzoProdotto"), resultSet2.getInt("Quantita")), daoProdotto.getProdotto(resultSet2.getInt("IdProdotto")).nome()));
            
            // salvo la data di spedizione dell'ordine corrente
            Date data = resultSet.getDate("DataSpedizione");
            // se l'oggetto che ho appena letto era null (purtroppo funziona così...) la metto a null
            if( resultSet.wasNull() )
                data = null;
            
            // aggiungo l'ordine corrente alla lista da ritornare
            ordini.add(new Ordine(resultSet.getInt("Id"), resultSet.getDouble("TotaleOrdine"), resultSet.getDouble("SpeseSpedizione"), data, resultSet.getString("Indirizzo"), resultSet.getString("NomeFornitore"), resultSet.getString("Email"), dettagli));
        }
        // ritorno il risultato
        return ordini;
    }
    
    public void creaOrdine(Utente utente, String nomeFornitore, double speseSpedizione, double totaleOrdine, Map<ProdottoDiUnFornitore,Integer> quantitaProdotti) throws SQLException {
    	int id_ordine;
    	
    	// per far fallire tutto un blocco di operazioni e non solo quella patologica
        connessione.setAutoCommit(false);
        
        // inserisco un nuovo ordine, senza specificare Id (autoincrement) e dataSpedizione (non nota al momento della creazione)
        String query = "INSERT INTO ORDINE (TotaleOrdine, SpeseSpedizione, Indirizzo, NomeFornitore, Email) VALUES (?,?,?,?,?)";
        // pre-compila la query se sintatticamente corretta, inoltre specifico che l'execute deve ritornare le chiavi generate
        // (se si prova ad eseguire getGeneratedKeys su uno statement non adatto, lancerà un'eccezione)
        PreparedStatement statement1 = connessione.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        try {
            // imposto i parametri della query
            statement1.setDouble(1, totaleOrdine);
            statement1.setDouble(2, speseSpedizione);
            statement1.setString(3, utente.indirizzo());
            statement1.setString(4, nomeFornitore);
            statement1.setString(5, utente.email());

            // se il numero di righe interessate è pari a zero, lancia un'eccezione
            if( statement1.executeUpdate() == 0 )
                throw new SQLException("Creazione dell'ordine fallita, nessuna riga inserita.");

            // ottengo tutte le righe create in un ResultSet
            ResultSet chiavi = statement1.getGeneratedKeys();

            // se c'è almeno una chiave la prendo perchè è l'unica
            if( chiavi.next() )
                id_ordine = chiavi.getInt(1);
            // altrimenti è successo qualcosa di strano
            else
                throw new SQLException("Errore: l'operazione ha apportato modifiche ma non è possibile ottenere.");

            // query generica per inserire un dettaglio_prodotto
            query = "INSERT INTO DETTAGLIO_ORDINE (IdOrdine, IdProdotto, PrezzoProdotto, Quantita) VALUES (?,?,?,?);";
            // pre-compila la query se sintatticamente corretta
            PreparedStatement statement2 = connessione.prepareStatement(query);
            
            // inserisco il dettaglio per ogni ProdottoDiUnFornitore
            for( ProdottoDiUnFornitore pdf : quantitaProdotti.keySet() ){
            	// imposto i parametri della query
                statement2.setInt(1, id_ordine);
                statement2.setInt(2, pdf.idProdotto());
                statement2.setDouble(3, pdf.prezzoScontato());
                statement2.setInt(4, quantitaProdotti.get(pdf));

                // se il numero di righe interessate è pari a zero, lancia un'eccezione
                if ( statement2.executeUpdate() == 0 ) 
                    throw new SQLException("Creazione del dettaglio dell'ordine fallita, nessuna riga inserita.");
            }

            // se siamo arrivati qui, tutto è andato liscio: posso fare la commit
            connessione.commit();
            
        } catch (SQLException e) {
        	// se sono state lanciate eccezioni nel blocco, faccio il rollback (se la connessione non è null)
            if( connessione != null )
            	connessione.rollback();
            // lancio nuovamente la stessa eccezione
            throw e;
            
        } finally {
        	// e ri-attivo l'autocommit
            connessione.setAutoCommit(true);
        }
    }

}