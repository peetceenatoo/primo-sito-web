package it.polimi.tiw.Controller;

import com.google.gson.Gson;

import it.polimi.tiw.Bean.Utente;
import it.polimi.tiw.Bean.Coppia;
import it.polimi.tiw.Bean.Fornitore;
import it.polimi.tiw.Bean.Prodotto;
import it.polimi.tiw.DAO.DAO_Fornitore;
import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(value="/visualizza")
public class Visualizza extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest richiesta, HttpServletResponse risposta) throws ServletException, IOException {
    	Coppia<Prodotto,List<Coppia<Fornitore,Coppia<Double,Double>>>> info;
    	
    	// imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");

        // leggo i parametri della richiesta
        String idProdottoS = richiesta.getParameter("idProdotto");
        if( idProdottoS == null || idProdottoS.isEmpty() ){
            risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // prendo l'id del prodotto aperto
        int idProdotto;
        try{
            idProdotto = Integer.parseInt(idProdottoS);
        } catch (NumberFormatException e) {
            risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if( idProdotto < 0 ){
            risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // creo i dao
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);
        DAO_Fornitore daoFornitore = new DAO_Fornitore(connessione);
        
        // creo la lista di appoggio con cui costruire le info
        List<Coppia<Fornitore,Coppia<Double,Double>>> tempCoppie;
        // creo una mappa di appoggio per riempire la lista
        Map<Fornitore,Coppia<Double,Double>> tempMap;
        // prendo il prodotto e la mappa da associarvi
        try {
        	// inserisco la nuova visualizzazione
            daoProdotto.setVisualizzato(((Utente)richiesta.getSession(false).getAttribute("utente")).email(),idProdotto);
            // creo la lista di appoggio con cui costruire le info
            tempCoppie = new ArrayList<Coppia<Fornitore,Coppia<Double,Double>>>();
            // creo una mappa di appoggio per riempire la lista
            tempMap = daoFornitore.getFornitoriConPrezzi(idProdotto);
        } catch (SQLException e) {
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        // riempio la lista
        for( Fornitore f : tempMap.keySet() )
        	tempCoppie.add(new Coppia<Fornitore, Coppia<Double,Double>>(f, tempMap.get(f)));
        // creo info
        try {
        	info = new Coppia<Prodotto,List<Coppia<Fornitore,Coppia<Double,Double>>>>(daoProdotto.getProdotto(idProdotto), tempCoppie);
        } catch (SQLException e) {
        	risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // se info è null vuol dire che l'id prodotto non è stato trovato
        if( ( info == null ) || ( info.primo() == null ) || ( info.secondo() == null ) ){
            risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // scrivo la mappa come stringa in json
        Gson gson = new Gson();
        String json = gson.toJson(info);
        // ritorno il risultato
        risposta.setStatus(HttpServletResponse.SC_OK);
        risposta.setContentType("application/json");
        risposta.setCharacterEncoding("UTF-8");
        risposta.getWriter().write(json);
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
