package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import it.polimi.tiw.Bean.Prodotto;
import it.polimi.tiw.Bean.Utente;

public class DAO_Prodotto{
	
	private final Connection connessione;

    public DAO_Prodotto(Connection connessione) {
        this.connessione = connessione;
    }
    
    public Prodotto getProdotto(int idProdotto) throws SQLException {
        // seleziono il prodotto
    	String query = "SELECT * FROM PRODOTTO WHERE Id = ?";

        // pre-compila la query se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);
        
        // imposto il valore idProdotto come parametro #1 della query
        statement.setInt(1, idProdotto);
        
        // eseguo la query
        ResultSet resultSet = statement.executeQuery();

        // metto il risultato nella lista se presente e ritorno, altrimenti ritorno null
        if( resultSet.next() )
            return new Prodotto(resultSet.getInt("Id"), resultSet.getString("Nome"), resultSet.getString("Descrizione"), resultSet.getString("Foto"), resultSet.getString("Categoria"));
        return null;
    }
    
    public List<Prodotto> getUltimiCinqueVisualizzati(String email) throws SQLException {
    	List<Prodotto> ultimi;
    	
    	// per selezionare solo gli ultimi 5, ordino in base al timestamp e metto limit 5
        String query = "SELECT P.*, Timestamp FROM VISUALIZZAZIONE v1 INNER JOIN PRODOTTO P ON P.Id = v1.IdProdotto WHERE Timestamp = (SELECT MAX(Timestamp) FROM VISUALIZZAZIONE v2 WHERE P.Id IN (SELECT IdProdotto FROM PRODOTTO_FORNITORE) AND v2.Email = ? AND v2.IdProdotto = v1.IdProdotto) ORDER BY Timestamp DESC LIMIT 5;";
        
        // pre-compila la query se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);
        
        // imposto il valore email come parametro #1 della query
        statement.setString(1, email);
        
        // eseguo la query
        ResultSet resultSet = statement.executeQuery();
        
        // istanzio la lista da ritornare
        ultimi = new ArrayList<>();
        
        // metto i risultati nella lista
        while ( resultSet.next() )
        	ultimi.add(new Prodotto(resultSet.getInt("Id"), resultSet.getString("Nome"), resultSet.getString("Descrizione"), resultSet.getString("Foto"), resultSet.getString("Categoria")));
        
        // se non ce n'erano almeno 5, ne prendo altri a caso tramite il metodo apposito
        if( ultimi.size() < 5 ) {
            Queue<Prodotto> cinqueDefault = getCinqueDiDefault(ultimi);
           
	        // finchè non arrivo a 5 aggiungo prodotti da quelli di default
	        while( !cinqueDefault.isEmpty() && ( ultimi.size() < 5 ) ){
	            if( !ultimi.contains(cinqueDefault.peek()) )
	                ultimi.add(cinqueDefault.poll());
	        }
        }
        
        // ritorno il risultato
	    return ultimi;
    }
    
    public double getPrezzo(int idProdotto, int idFornitore) throws SQLException {

    	// calcolo il prezzo già scontato
        String query = "SELECT IdProdotto, IdFornitore, Round((Prezzo*(1-Sconto)),2) AS Prezzo FROM PRODOTTO_FORNITORE WHERE IdProdotto = ? AND IdFornitore = ?";
        
        // pre-compila la query se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);

        // imposto come parametri i rispettivi Id
        statement.setInt(1, idProdotto);
        statement.setInt(2, idFornitore);

        // eseguo la query
        ResultSet resultSet = statement.executeQuery();

        // sposto il puntatore alla prima riga e ritorno il prezzo senza controllare
        // perchè resultSet.getInt lancia SQLException se il puntatore non punta ad una riga
        resultSet.next();
        return resultSet.getDouble("Prezzo");
    }
        
    public double getSconto(int idProdotto, int idFornitore) throws SQLException {
    	// seleziono la riga associata alla coppia
        String query = "SELECT Sconto FROM PRODOTTO_FORNITORE WHERE IdProdotto = ? AND IdFornitore = ?";
        
        // pre-compila la query se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);

        // imposto come parametri i rispettivi Id
        statement.setInt(1, idProdotto);
        statement.setInt(2, idFornitore);

        // eseguo la query
        ResultSet resultSet = statement.executeQuery();

        // sposto il puntatore alla prima riga e ritorno il prezzo senza controllare
        // perchè resultSet.getDouble lancia SQLException se il puntatore non punta ad una riga
        resultSet.next();
        return resultSet.getDouble("Sconto");
    }
    
    public boolean isFornitoDaFornitore(int idProdotto, int idFornitore) throws SQLException {
    	// seleziono l'eventuale riga associata alla coppia
        String query = "SELECT * FROM PRODOTTO_FORNITORE WHERE IdProdotto = ? AND IdFornitore = ?";
        
        // pre-compila la query se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);

        // imposto come parametri i rispettivi Id
        statement.setInt(1, idProdotto);
        statement.setInt(2, idFornitore);

        // eseguo la query
        ResultSet resultSet = statement.executeQuery();

        // ritorno falso se non è fornito, vero se è fornito
        return resultSet.next();
    }
    
    public String getPercorsoFoto(int idProdotto) throws SQLException {
    	// seleziono la stringa del percorso 
        String query = "SELECT Foto FROM PRODOTTO P INNER JOIN PRODOTTO_FORNITORE PDF ON P.Id = PDF.IdProdotto WHERE Id = ?";
        
        // pre-compila la query se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);
        
        // imposto idProdotto come parametro #1 della query
        statement.setInt(1, idProdotto);
        
        // eseguo la query
        ResultSet resultSet = statement.executeQuery();

        // sposto il puntatore alla prima riga e ritorno il prezzo senza controllare
        // perchè resultSet.getString lancia SQLException se il puntatore non punta ad una riga
        resultSet.next();
        return resultSet.getString("Foto");
    }
    
    public Map<Prodotto, Integer> getProdotti(String parolaChiave) throws SQLException {
    	Map<Prodotto, Integer> prodotti;
    	
    	// cerco, tra tutti i prodotti quelli che hanno il nome o la descrizione come specificato in seguito, quelli forniti a prezzo minimo
    	String query = "SELECT P.*, Min(Round((Prezzo*(1-Sconto)))) AS PrezzoMinimo FROM PRODOTTO P INNER JOIN PRODOTTO_FORNITORE PDF on P.Id = PDF.IdProdotto WHERE P.Nome LIKE ? OR P.Descrizione LIKE ? GROUP BY IdProdotto ORDER BY PrezzoMinimo;";
        
    	// istanzio la lista da ritornare
    	prodotti = new HashMap<>();
    	
    	// pre-compila la query se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);

    	// imposto come parametri una stringa generica che contenga la parola chiave
        statement.setString(1, "%" + parolaChiave + "%");
        statement.setString(2, "%" + parolaChiave + "%");

        // eseguo la query
        ResultSet resultSet = statement.executeQuery();

        // metto i risultati nella lista e ritorno
        while( resultSet.next() )
            prodotti.put(new Prodotto(resultSet.getInt("Codice"), resultSet.getString("Nome"), resultSet.getString("Descrizione"), resultSet.getString("Foto"), resultSet.getString("Categoria")), resultSet.getInt("PrezzoMinimo"));
        return prodotti;
    }
    
    public void setVisualizzato(Utente utente, int idProdotto) throws SQLException {
    	// inserisco una nuova riga di visualizzazione
        String query = "INSERT INTO VISUALIZZAZIONE (Email, IdProdotto) VALUES(?, ?)";
        
        // pre-compila la query se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);
        
        // imposto la mail dell'utente dato come parametro #1
        statement.setString(1, utente.email());
        // imposto idProdotto come parametro #2
        statement.setInt(2, idProdotto);
        
        // eseguo la query
        statement.executeUpdate();
    }
     
    private Queue<Prodotto> getCinqueDiDefault(List<Prodotto> daEscludere) throws SQLException {
    	Queue<Prodotto> ultimi;
    	
    	// assumo che la categoria di default sia 'Tecnologia'
        String query = "SELECT * FROM PRODOTTO P WHERE P.Categoria = 'Tecnologia' AND P.Id IN (SELECT IdProdotto FROM PRODOTTO_FORNITORE WHERE Sconto > 0.00)";
        
        // escludo eventuali prodotti da escludere aggiungendo condizioni sui rispettivi Id alla query
        if( !daEscludere.isEmpty() ){
            query += " AND P.Id NOT IN (";
                for( int i=0; i<daEscludere.size(); i++ ){
                    query += " ? ";
                    if( i != daEscludere.size()-1 )
                    	query += ", ";
                }
            query += " ) ";
        }
        
        // disordino il risultato e prendo solo 5 righe
        query += " ORDER BY RAND() LIMIT 5";

        // istanzio la lista da ritornare
        ultimi = new LinkedList<>();
        
        // pre-compila la query se sintatticamente corretta
        PreparedStatement statement = connessione.prepareStatement(query);

        // imposto gli Id da escludere come parametri della query
        for( int i=1; i<=daEscludere.size(); i++ )
            statement.setInt(i, daEscludere.get(i).id());
        
        // eseguo la query
        ResultSet resultSet = statement.executeQuery();

        // aggiungo gli elementi alla coda e ritorno il risultato
        while( resultSet.next() )
            ultimi.add( new Prodotto(resultSet.getInt("Id"), resultSet.getString("Nome"), resultSet.getString("Descrizione"), resultSet.getString("Foto"), resultSet.getString("Categoria")) );
        return ultimi;
    }
	
}