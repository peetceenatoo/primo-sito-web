package it.polimi.tiw.Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import it.polimi.tiw.Bean.Prodotto;
import it.polimi.tiw.Bean.Utente;
import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;
import it.polimi.tiw.Utility.TemplateInitializer;

@WebServlet(value= {"/home"})
public class Home extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private JavaxServletWebApplication applicazione;
    private Connection connessione;
    
    @Override
    public void init() throws UnavailableException{
    	this.applicazione = JavaxServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = TemplateInitializer.getTemplateEngine(this.applicazione);
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException{
        List<Prodotto> daVisualizzare;
        
        // contiene l'informazione necessaria a thymeleaf sul corrente scambio richiesta-risposta
        final WebContext ctx = new WebContext(this.applicazione.buildExchange(richiesta, risposta), richiesta.getLocale());

        // prendo la sessione
        Utente utente = (Utente)richiesta.getSession(false).getAttribute("utente");
        
        // creo un DAO_Prodotto
        DAO_Prodotto dao = new DAO_Prodotto(connessione);

        // prendo i 5 prodotti da visualizzare
        try {
            daVisualizzare = dao.getCinqueProdottiHome(utente.email());
        } catch (SQLException e) {
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nella selezione dei prodotti da visualizzare.");
            return;
        }
        // aggiungo dinamicamente i prodotti alla pagina home
        ctx.setVariable("prodotti", daVisualizzare);

        // imposto la codifica
        risposta.setCharacterEncoding("UTF-8");

        // mando la risposta al client (catcho un throwable perchè non conosco il codice sorgente di thymeleaf...
        try {
        	this.templateEngine.process("home", ctx, risposta.getWriter());
        } catch (Throwable e) {
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
