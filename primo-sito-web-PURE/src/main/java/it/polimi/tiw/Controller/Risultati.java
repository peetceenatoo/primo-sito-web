package it.polimi.tiw.Controller;

import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import it.polimi.tiw.Bean.Fornitore;
import it.polimi.tiw.Bean.Prodotto;
import it.polimi.tiw.Bean.Utente;
import it.polimi.tiw.DAO.DAO_Carrello;
import it.polimi.tiw.DAO.DAO_Fornitore;
import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;
import it.polimi.tiw.Utility.TemplateInitializer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(value = "/risultati")
public class Risultati extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private JavaxServletWebApplication applicazione;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.applicazione = JavaxServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = TemplateInitializer.getTemplateEngine(this.applicazione);
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
		String[] aperti;
    	Map<Integer, Map<Fornitore, Double>> prodottiAperti;
    	Map<Prodotto, Double> risultati;
    	
    	// contiene l'informazione necessaria a thymeleaf sul corrente scambio richiesta-risposta
    	final WebContext ctx = new WebContext(this.applicazione.buildExchange(richiesta, risposta), richiesta.getLocale());

    	// imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");

        // leggo i parametri della richiesta
        String queryString = richiesta.getParameter("queryString");
        
        // se la queryString è nulla o vuota rimando alla home
        if( ( queryString == null ) || queryString.isEmpty() ) {
            risposta.sendRedirect(getServletContext().getContextPath() + "/home");
            return;
        }
        
        // creo un dao fornitore
        DAO_Fornitore daoFornitore = new DAO_Fornitore(connessione);

        // prendo gli id che risultano aperti nella pagina (array di stringhe)
        aperti = richiesta.getParameterValues("aperto");
        // istanzio la mappa per i prodotti aperti
        prodottiAperti = new HashMap<>();
        
        // se l'array di id non è vuoto, metto nella mappa tutti i prodotti aperti
        if( aperti != null ){
        	for( String s : aperti ){
        		int idProdotto;
	            // prendo l'id del prodotto aperto
	        	try{
	                idProdotto = Integer.parseInt(s);
	            } catch (NumberFormatException ex) {
	                risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro Id Prodotto mal formato.");
	                return;
	            }
	        	try {
	        		prodottiAperti.put(idProdotto, daoFornitore.getFornitori(idProdotto));
	        	} catch(SQLException ex) {
	                risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel recupero dei fornitori.");
	                return;
	            }
	        }
	    }

        // prendo la sessione e quindi l'utente associato
		HttpSession sessione = richiesta.getSession(false);
        Utente utente = (Utente)sessione.getAttribute("utente");
        
        // creo un dao prodotto
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);

        // prendo i risultati
        try{
            risultati = daoProdotto.getProdotti(queryString);
        } catch (SQLException e) {
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel recupero dei risultati.");
            return;
        }
        if( risultati == null ){
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nella ricerca dei risultati.");
            return;
        }

        // creo un dao carrello
        DAO_Carrello daoCarrello = new DAO_Carrello(richiesta.getSession(false), this.connessione);

        // imposto le variabili richieste da thymeleaf nel foglio html
        ctx.setVariable("DAOcarrello", daoCarrello);
        ctx.setVariable("DAOprodotto", daoProdotto);
        ctx.setVariable("risultati", risultati);
        ctx.setVariable("prodottiAperti", prodottiAperti);
        ctx.setVariable("queryString", queryString);

        // imposto la codifica
        risposta.setCharacterEncoding("UTF-8");
        
        // mando la risposta al client (catcho un throwable perchè non conosco il codice sorgente di thymeleaf...
        try{
            this.templateEngine.process("risultati", ctx, risposta.getWriter());
        } catch(Throwable e) {
        	System.out.println(e.getMessage());
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "É stato rilevato un comportamento indesiderato durante l'elaborazione di Thymeleaf della pagina.");
            return;
        }
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
