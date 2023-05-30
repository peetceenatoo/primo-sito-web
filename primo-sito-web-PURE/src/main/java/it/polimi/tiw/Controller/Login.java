package it.polimi.tiw.Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import it.polimi.tiw.Bean.Utente;
import it.polimi.tiw.DAO.DAO_Utente;
import it.polimi.tiw.Utility.ConnectionInitializer;
import it.polimi.tiw.Utility.TemplateInitializer;

@WebServlet(value = "/login")
public class Login extends HttpServlet {
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
        
    	// contiene l'informazione necessaria sul corrente scambio richiesta-risposta
    	final WebContext ctx = new WebContext(this.applicazione.buildExchange(richiesta, risposta), richiesta.getLocale());

    	// imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");

        // se c'è stato un errore, lo visualizzo dinamicamente
        if( richiesta.getParameter("errore") != null )
            ctx.setVariable("errore", true);

        // imposto la codifica
        risposta.setCharacterEncoding("UTF-8");
        
        // mando la risposta
        this.templateEngine.process("login", ctx, risposta.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
    	String path;
    	Utente utente;
    	
    	// imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");

        // leggo i parametri
        String email = richiesta.getParameter("email");
        String password = richiesta.getParameter("password");

        // se c'è stato un errore nella post del login, rispondo al client con un messaggio di errore
        if( ( email == null ) || email.isEmpty() || ( password == null ) || password.isEmpty() ){
            risposta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email e password non possono essere vuoti.");
            return;
        }

        // creo un dao utente
        DAO_Utente dao = new DAO_Utente(connessione);
        // metto utente a null perchè altrimenti il compilatore mi dice che non è inizializzato
        utente = null;
        
        // provo il login
        try {
        	// se la mail è valida e l'utente è registrato
            if( DAO_Utente.isValida(email) && dao.isRegistrato(email) )
            	// controllo le credenziali
                utente = dao.getUtenteConPassword(email, password);
        } catch (SQLException e) {
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il controllo delle credenziali.");
            return;
        }
        
        // se null, il login è fallito
        if( utente == null )
        	// effettuo il redirect alla pagina di login con errore==true
            path = getServletContext().getContextPath() + "/login?errore=true";
        // altrimenti è andato a buon fine
        else {
        	// ritorno l'utente
            richiesta.getSession().setAttribute("utente", utente);
            // effettuo il redirect a home
            path = getServletContext().getContextPath() + "/home";
        }
        // rispondo al client con il redirect
        risposta.sendRedirect(path);
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
