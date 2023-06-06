package it.polimi.tiw.Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.Utility.ConnectionInitializer;

@WebServlet(value="/askLogged")
public class AskLogged extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException{
    	
    	// prendo la sessione
    	HttpSession sessione = richiesta.getSession();
    	
    	// imposto la codifica
    	risposta.setCharacterEncoding("UTF-8");
    	
    	// imposto lo stato della risposta
    	risposta.setStatus(HttpServletResponse.SC_OK);
    	
    	// ritorno "yes" se è loggato, "no" altrimenti
    	risposta.getWriter().println(new Gson().toJson( ( !sessione.isNew() && ( sessione.getAttribute("utente") != null ) ) ? "yes" : "no" ));
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
