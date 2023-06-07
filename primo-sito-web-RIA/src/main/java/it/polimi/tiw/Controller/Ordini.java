package it.polimi.tiw.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import it.polimi.tiw.Bean.CarrelloFornitore;
import it.polimi.tiw.Bean.Fornitore;
import it.polimi.tiw.Bean.Ordine;
import it.polimi.tiw.Bean.ProdottoDiUnFornitore;
import it.polimi.tiw.Bean.ProdottoCarrello;
import it.polimi.tiw.Bean.Utente;
import it.polimi.tiw.DAO.DAO_Fornitore;
import it.polimi.tiw.DAO.DAO_Ordine;
import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;

@WebServlet(value = "/ordini")
@MultipartConfig
public class Ordini extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest richiesta, HttpServletResponse risposta) throws ServletException, IOException {
    	List<Ordine> ordini;

        // creo un dao ordine
        DAO_Ordine daoOrdine = new DAO_Ordine(connessione);

        // prendo l'utente associato alla sessione
        Utente utente = (Utente)richiesta.getSession(false).getAttribute("utente");

        // prendo gli ordini
        try {
            ordini = daoOrdine.getOrdini(utente.email());
        } catch (SQLException e) {
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // creo un oggetto gson
        Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
        // scrivo la stringa in json
        String json = gson.toJson(ordini);
        // ritorno il risultato
        risposta.setStatus(HttpServletResponse.SC_OK);
        risposta.setContentType("application/json");
        risposta.setCharacterEncoding("UTF-8");
        risposta.getWriter().write(json);
    }
    
    @Override
    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
    	CarrelloFornitore data;
    	
    	// imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");
    	
    	// leggo la stringa in input
    	BufferedReader reader = richiesta.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while( ( line = reader.readLine() ) != null )
            sb.append(line);
        String corpo = sb.toString();

        // creo un oggetto gson
        Gson gson = new Gson();
        // converto da JSON
        try {
           data = gson.fromJson(corpo, CarrelloFornitore.class);
        } catch (JsonSyntaxException e) {
            risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // controllo che l'id sia almeno 1
        if( data.idFornitore() <= 0 ){
            risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // controllo che ci sia almeno un prodotto
        if( data.prodotti().stream().map(o -> o.quantita()).reduce(0, Integer::sum) == 0 ){
            risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // creo i dao
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);
        DAO_Fornitore daoFornitore = new DAO_Fornitore(connessione);    
        DAO_Ordine daoOrdine = new DAO_Ordine(connessione);
        
        // controllo che tutti i prodotti siano forniti dal fornitore
        for( ProdottoCarrello prod : data.prodotti() ){
            try {
                if( ( prod == null ) || !daoProdotto.isFornitoDaFornitore(prod.idProdotto(), data.idFornitore()) ){
                    risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } catch(SQLException e) {
                risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
        		
        // prendo il fornitore
        Fornitore fornitore;
        try {
			fornitore = daoFornitore.getFornitore(data.idFornitore());
        } catch (SQLException ex) {
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if( fornitore == null ){
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // calcolo il totale e il numero di articoli nel carrello
        double totale = 0;
        for( ProdottoCarrello prod : data.prodotti() ){
        	try {
        		totale += daoProdotto.getPrezzoScontato(prod.idProdotto(), data.idFornitore());
        	} catch (SQLException e) {
        		risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel calcolo del totale.");
                return;
        	}
        }

        // se non si è superata la soglia per la spedizione gratuita, la calcolo
        double speseSpedizione = 0;
        if( ( fornitore.soglia() == null ) ||  ( totale < fornitore.soglia() ) ){
        	int quantita = 0;
        	for( ProdottoCarrello prod : data.prodotti() )
        		quantita += prod.quantita();
            try{
                speseSpedizione = daoFornitore.getCostoSpedizione(data.idFornitore(), quantita);
            } catch (SQLException e) {
                risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }

        // prendo l'utente dalla sessione
        Utente utente = (Utente)richiesta.getSession(false).getAttribute("utente");

        // creo la mappa con cui creare l'ordine
        Map<ProdottoDiUnFornitore,Integer> mappa = new HashMap<>();
        for( ProdottoCarrello prod : data.prodotti() ){
        	try {
				mappa.put(new ProdottoDiUnFornitore(prod.idProdotto(), fornitore.id(), daoProdotto.getPrezzoScontato(prod.idProdotto(), fornitore.id()), daoProdotto.getSconto(prod.idProdotto(), fornitore.id())), prod.quantita());
			} catch (SQLException e) {
				risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
			}
        }
        	
        // creo l'ordine
        try{
            daoOrdine.creaOrdine(utente, fornitore.nome(), speseSpedizione, totale, mappa);
        } catch (SQLException e) {
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // mando la risposta ok
        risposta.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    public void destroy() {
        try {
            if( connessione != null )
                connessione.close();
        } catch (SQLException e) {
        }
    }
}
