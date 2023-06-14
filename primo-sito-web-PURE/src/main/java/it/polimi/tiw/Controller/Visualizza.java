package it.polimi.tiw.Controller;

import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.Bean.Utente;
import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(value="/visualizza")
public class Visualizza extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connessione;


    public void init() throws UnavailableException {
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
    	String idVisualizzatoS, queryString, percorso;
    	String[] aperti;
    	int idVisualizzato;
    	
    	// imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");

        // leggo i parametri della richiesta
        idVisualizzatoS = richiesta.getParameter("visualizzato");
        aperti = richiesta.getParameterValues("aperto");
        queryString = richiesta.getParameter("queryString");
        // se c'è stato un errore nella post del login, rispondo al client con un messaggio di errore
        if( ( idVisualizzatoS == null ) || idVisualizzatoS.isEmpty() || ( queryString == null ) || queryString.isEmpty() ){
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "QueryString e IdVisualizzato non possono essere vuoti.");
            return;
        }

        // prendo l'id del prodotto aperto
        try {
            idVisualizzato = Integer.parseInt(idVisualizzatoS);
        } catch (NumberFormatException e) {
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro Id Prodotto aperto mal formato.");
            return;
        }
        
        // controllo che l'id aperto sia valido
        if( idVisualizzato < 0 ){
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro Id Prodotto non valido.");
            return;
        }
        
        // creo un dao prodotto
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);
        
        // controllo che non ci siano prodotti aperti che non sono tra i risultati della pagina
    	try {
			if( !( daoProdotto.getProdotti(queryString).stream().filter( x -> { return x.primo().id() == idVisualizzato; } ).count() > 0 ) ) {
				risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Prodotto aperto non tra i risultati.");
				return;
			}
		} catch (SQLException e) {
			risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel recupero dei risultati dal DB.");
            return;
		}

        // controllo che tutti gli id aperti siano validi
        try {
	        if( aperti != null ){
	            for( String s : aperti ){
	                Integer.parseInt(s);
	            }
	        }
        } catch(NumberFormatException e) {
        	risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro Id Prodotto mal formato tra gli aperti.");
        	return;
        }

        Utente utente = (Utente)richiesta.getSession(false).getAttribute("utente");

        // aggiungo il prodotto aperto alla tabella delle visualizzazioni
        try{
            daoProdotto.setVisualizzato(utente, idVisualizzato);
        } catch(SQLException ex) {
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore aggiungendo la visualizzazione.");
            return;
        }

        // mando il redirect ai risultati aggiungendo il nuovo aperto e tutti quelli che erano aperti
        percorso = getServletContext().getContextPath() + "/risultati" + "?queryString=" + queryString + "&aperto=" + idVisualizzato;
        if( aperti != null )
            for( String s : aperti )
                percorso += "&aperto=" + s;
        risposta.sendRedirect(percorso);
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
