package it.polimi.tiw.Controller;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
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
import it.polimi.tiw.Bean.Ordine;
import it.polimi.tiw.Bean.ProdottoDiUnFornitore;
import it.polimi.tiw.Bean.Utente;
import it.polimi.tiw.DAO.DAO_Carrello;
import it.polimi.tiw.DAO.DAO_Fornitore;
import it.polimi.tiw.DAO.DAO_Ordine;
import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;
import it.polimi.tiw.Utility.TemplateInitializer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(value = "/ordini")
public class Ordini extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private JavaxServletWebApplication applicazione;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.applicazione = JavaxServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = TemplateInitializer.getTemplateEngine(this.applicazione);
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
    	List<Ordine> ordini;
    	
    	// contiene l'informazione necessaria a thymeleaf sul corrente scambio richiesta-risposta
    	final WebContext ctx = new WebContext(this.applicazione.buildExchange(richiesta, risposta), richiesta.getLocale());

    	// prendo l'utente associato alla sessione
		Utente utente = (Utente)richiesta.getSession(false).getAttribute("utente");
		
		// creo un dao ordine
        DAO_Ordine dao = new DAO_Ordine(connessione);

        // prendo gli ordini dell'utente
        try {
            ordini = dao.getOrdini(utente.email());
        } catch (SQLException ex) {
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel recupero degli ordini dal DB.");
            return;
        }
        
        // creo un dao prodotto
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);

        // imposto le variabili richieste da thymeleaf nel foglio html
        ctx.setVariable("ordini", ordini);
        ctx.setVariable("DAOprodotto", daoProdotto);

        // imposto la codifica
        risposta.setCharacterEncoding("UTF-8");
        
        // mando la risposta al client (catcho un throwable perchè non conosco il codice sorgente di thymeleaf...
        try {
            this.templateEngine.process("ordini", ctx, risposta.getWriter());
        } catch (Exception e) {
        	System.out.println(e.getMessage());
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "É stato rilevato un comportamento indesiderato durante l'elaborazione di Thymeleaf della pagina.");
            return;
        }

    }

    @Override
    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
    	Map<Integer, Map<Integer, Integer>> carrello;
    	Map<Integer,Integer> quantitaPerProdotto;
    	String idFornitoreS;
    	int idFornitore;
    	double speseSpedizione;
        Fornitore fornitore;
    	
    	// imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");

        // leggo i parametri della richiesta
        idFornitoreS = richiesta.getParameter("idFornitore");

        // se c'è stato un errore nella post del login, rispondo al client con un messaggio di errore
        if( ( idFornitoreS == null ) || idFornitoreS.isEmpty() ){
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro IdFornitore non può essere vuoto.");
            return;
        }

        // prendo l'id del fornitore
        try{
            idFornitore = Integer.parseInt(idFornitoreS);
        }catch (NumberFormatException ex){
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro IdFornitore mal formato.");
            return;
        }

        // controllo che sia valido
        if( idFornitore <= 0 ){
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro IdFornitore non valido.");
            return;
        }

        // creo un dao carrello
        DAO_Carrello daoCarrello = new DAO_Carrello(richiesta.getSession(false), connessione);
        // creo un dao prodotto
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);
        // creo un dao fornitore
        DAO_Fornitore daoFornitore = new DAO_Fornitore(connessione);

        // prendo il fornitore
        try {
            fornitore = daoFornitore.getFornitore(idFornitore);
        } catch (SQLException e) {
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel recupero del fornitore.");
            return;
        }
        if( fornitore == null ){
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Il fornitore risulta essere null.");
            return;
        }
        
        // prendo il carrello
        carrello = daoCarrello.getCarrello();

        // prendo la mappa di prodotti del carrello associata al fornitore
        quantitaPerProdotto = carrello.get(idFornitore);
        
        // controllo che ci sia almeno un prodotto nel carrello del fornitore richiesto
        if( !carrello.containsKey(idFornitore) || ( quantitaPerProdotto.values().stream().reduce(0, Integer::sum) == 0 ) ) {
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro IdFornitore si riferisce ad un fornitore di cui non è presente alcun prodotto da ordinare.");
            return;
        }
        
        // calcolo il costo totale dell'ordine
        double totale = 0;
        for( Integer i : quantitaPerProdotto.keySet() ) {
			try {
				totale += daoProdotto.getPrezzoScontato(i, fornitore.id()) * quantitaPerProdotto.get(i);
			} catch (SQLException e1) {
				risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel calcolo del totale.");
                return;
			}
        }

        // calcolo l'eventuale fascia di spedizione il totale non supera la soglia per la spedizione gratuita
        speseSpedizione = 0;
        if( ( fornitore.soglia() == null ) || ( totale < fornitore.soglia() ) ) {
            try {
                speseSpedizione = daoFornitore.getCostoSpedizione(idFornitore, quantitaPerProdotto.entrySet().stream().map(x -> {
                    return x.getValue();
                }).reduce(0, Integer::sum));
            } catch (SQLException e) {
                risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel recupero della fascia di spedizione.");
                return;
            }
        }

        // creo un dao ordine
        DAO_Ordine daoOrdine = new DAO_Ordine(connessione);
        
        // prendo l'utente associato alla sessione
     	Utente utente = (Utente)richiesta.getSession(false).getAttribute("utente");
     		
     	// creo una mappa <ProdottoDiUnFornitore, Integer> invece che <Integer, Integer>
     	Map<ProdottoDiUnFornitore, Integer> temp = new HashMap();
     	for( Integer i : quantitaPerProdotto.keySet() ){
			try {
				temp.put(new ProdottoDiUnFornitore(i, fornitore.id(), daoProdotto.getPrezzoScontato(i, fornitore.id()), daoProdotto.getSconto(i, fornitore.id())), quantitaPerProdotto.get(i));
			} catch (SQLException e) {
				risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel recupero di informazioni su un ProdottoDiUnFornitore.");
                return;
			}
     	}
     		
     	// creo un ordine (con la collect converto la mappa in una mappa che ha un ProdottoDiUnFornitore come chiave)
        try {
			daoOrdine.creaOrdine(utente, fornitore.nome(), speseSpedizione, totale, temp);
		} catch (SQLException e) {
			risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nella creazione di un ordine.");
            return;
		}

        // tolgo i prodotti ordinati dal carrello
        daoCarrello.rimuoviProdottiDelFornitore(fornitore.id());

        // mando il redirect a ordini
        risposta.sendRedirect(getServletContext().getContextPath() + "/ordini");
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
