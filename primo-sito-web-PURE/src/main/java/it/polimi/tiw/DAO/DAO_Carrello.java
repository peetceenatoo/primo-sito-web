package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;

public class DAO_Carrello {

    private final HttpSession sessione;
    private final Connection connessione;

    public DAO_Carrello(HttpSession sessione, Connection connessione) {
    	// il carrello è salvato nella sessione, non nel database
        this.sessione = sessione;
        this.connessione = connessione;
    }
    
    public void aggiungiProdottoAlCarrello(int idProdotto, int idFornitore, int quantita){
    	int prec;
        Map<Integer, Map<Integer, Integer>> carrello;
        
        // se già presente nella sessione, prendo il carrello
        carrello = (Map<Integer, Map<Integer, Integer>>) sessione.getAttribute("carrello");
        
        // altrimenti lo creo
        if( carrello == null ){
            carrello = new HashMap<>();
            sessione.setAttribute("carrello", carrello);
        }

        // se il carrello non conteneva prodotti del fornitore corrente, aggiungo una mappa per il fornitore
        if( !carrello.containsKey(idFornitore) )
            carrello.put(idFornitore, new HashMap<>());

        // metto in old la quantità del prodotto del fornitore attuale
        if( carrello.get(idFornitore).containsKey(idProdotto) )
        	prec = carrello.get(idFornitore).get(idProdotto);
        else
        	prec = 0;
     
        // aggiungo la quantita specificata per il prodotto
        carrello.get(idFornitore).put(idProdotto, prec + quantita);
    }

    public Map<Integer, Map<Integer, Integer>> getCarrello(){
    	Map<Integer, Map<Integer, Integer>> carrello;
    	Map<Integer, Map<Integer, Integer>> carrelloUnmodifiable;
        
        // se già presente nella sessione, prendo il carrello
        carrello = (Map<Integer, Map<Integer, Integer>>) sessione.getAttribute("carrello");
        
        // altrimenti lo creo
        if( carrello == null ){
            carrello = new HashMap<>();
            sessione.setAttribute("carrello", carrello);
        }
        
        // istanzio il carrello da ritornare
        carrelloUnmodifiable = new HashMap<>();
        
        // prendo una copia unmodifiable della mappa di ogni fornitore
        for( Integer i : carrello.keySet() )
        	carrelloUnmodifiable.put(i, Collections.unmodifiableMap(carrello.get(i)));
        	
        // ritorno una copia unmodifiable della mappa di fornitori
        return Collections.unmodifiableMap(carrelloUnmodifiable);
    }
    
    public void rimuoviProdottiDelFornitore(int idFornitore) {
    	Map<Integer, Map<Integer, Integer>> carrello;
        
        // se già presente nella sessione, prendo il carrello
        carrello = (Map<Integer, Map<Integer, Integer>>) sessione.getAttribute("carrello");
        
        // altrimenti lo creo
        if( carrello == null ){
            carrello = new HashMap<>();
            sessione.setAttribute("carrello", carrello);
        }
        
        // tolgo il fornitore
        carrello.remove(idFornitore);
    }
  
}