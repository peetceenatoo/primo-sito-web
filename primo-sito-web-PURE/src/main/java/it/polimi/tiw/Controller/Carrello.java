package it.polimi.tiw.Controller;

import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import it.polimi.tiw.Bean.Utente;
import it.polimi.tiw.DAO.DAO_Carrello;
import it.polimi.tiw.DAO.DAO_Fornitore;
import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;
import it.polimi.tiw.Utility.TemplateInitializer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(value = "/carrello")
public class Carrello extends HttpServlet {
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
    	// contiene l'informazione necessaria a thymeleaf sul corrente scambio richiesta-risposta
    	final WebContext ctx = new WebContext(this.applicazione.buildExchange(richiesta, risposta), richiesta.getLocale());

    	// prendo la sessione e quindi l'utente associato
        HttpSession sessione = richiesta.getSession(false);
        Utente utente = (Utente)sessione.getAttribute("utente");
        
        // creo i dao necessari
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);
        DAO_Carrello daoCarrello = new DAO_Carrello(sessione,connessione);
        DAO_Fornitore daoFornitore = new DAO_Fornitore(connessione);

        // imposto le variabili richieste da thymeleaf nel foglio html
        ctx.setVariable("carrello", daoCarrello.getCarrello());
        ctx.setVariable("DAOfornitore", daoFornitore);
        ctx.setVariable("DAOprodotto", daoProdotto);

        // imposto la codifica
        risposta.setCharacterEncoding("UTF-8");
        
        // mando la risposta al client (catcho un throwable perchè non conosco il codice sorgente di thymeleaf...
        try{
            this.templateEngine.process("carrello", ctx, risposta.getWriter());
        } catch(Throwable e) {
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "É stato rilevato un comportamento indesiderato durante l'elaborazione di Thymeleaf della pagina.");
            return;
        }

    }

    @Override
    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
    	String percorso;
    	String idFornitoreS, idProdottoS, quantS;
    	int idFornitore, idProdotto, quant;
    	
    	// imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");

        // leggo i parametri della richiesta
        idFornitoreS = richiesta.getParameter("idFornitore");
        idProdottoS = richiesta.getParameter("idProdotto");
        quantS = richiesta.getParameter("quantita");

        // se c'è stato un errore, lo visualizzo dinamicamente
        if( ( idFornitoreS == null ) || idFornitoreS.isEmpty() || ( idProdottoS == null ) || idProdottoS.isEmpty() || ( quantS == null ) || quantS.isEmpty()){
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro mancante.");
            return;
        }

        // se fallisce la conversione a intero mando un messaggio di errore
        try{
            idFornitore = Integer.parseInt(idFornitoreS);
            idProdotto = Integer.parseInt(idProdottoS);
            quant = Integer.parseInt(quantS);
        } catch (NumberFormatException e) {
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro mal formato.");
            return;
        }
        
        // se la quantità non è valida mando un messaggio di errore
        if( quant <= 0 ){
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro quant non valido.");
            return;
        }

        // creao un dao prodotto
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);

        try{
        	// se il prodotto non è fornito da quel fornitore mando un messaggio di errore
            if( !daoProdotto.isFornitoDaFornitore(idProdotto,idFornitore) ){
            	risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il prodotto scelto come parametro non è fornito dal fornitore scelto come parametro.");
                return;
            }
        } catch (SQLException e) {
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il controllo della disponibilità nel DB.");
            return;
        }

        // creo un dao carrello
        DAO_Carrello daoCarrello = new DAO_Carrello(richiesta.getSession(false), this.connessione);
        // aggiungo il prodotto al carrello
        daoCarrello.aggiungiProdottoAlCarrello(idProdotto, idFornitore, quant);

        // mando il redirect al carrello
        percorso = getServletContext().getContextPath() + "/carrello";
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
