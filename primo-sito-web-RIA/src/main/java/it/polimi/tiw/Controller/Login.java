package it.polimi.tiw.Controller;

import com.google.gson.Gson;

import it.polimi.tiw.Bean.Utente;
import it.polimi.tiw.DAO.DAO_Utente;
import it.polimi.tiw.Utility.ConnectionInitializer;

import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(value="/login")
public class Login extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
        String email, password;
        Utente utente;

        // imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");

        // leggo i parametri
        email = richiesta.getParameter("email");
        password = richiesta.getParameter("password");

        // se c'è stato un errore nella post del login, rispondo al client con un messaggio di errore
        if( ( email == null ) || ( email.isEmpty() ) || ( password == null ) || ( password.isEmpty() ) ){
            risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // provo il login
        utente = null;
        DAO_Utente dao = new DAO_Utente(connessione);
        try {
        	// se la mail è valida e l'utente è registrato
            if( DAO_Utente.isValida(email) && dao.isRegistrato(email) )
            	// controllo le credenziali
                utente = dao.getUtenteConPassword(email, password);
            else {
            	risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (SQLException e) {
            risposta.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il controllo delle credenziali.");
            return;
        }

        // se null, il login è fallito
        if( utente == null ){
            risposta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // altrimenti è andato a buon fine
        else {
        	// ritorno l'utente
            richiesta.getSession(true).setAttribute("utente", utente);
            risposta.setStatus(HttpServletResponse.SC_OK);
            risposta.setContentType("application/json");
            // imposto la codifica
            risposta.setCharacterEncoding("UTF-8");
            // mando la risposta
            risposta.getWriter().println(new Gson().toJson(utente.email()));
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
