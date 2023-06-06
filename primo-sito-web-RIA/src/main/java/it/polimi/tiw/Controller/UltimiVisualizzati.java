package it.polimi.tiw.Controller;

import com.google.gson.Gson;

import it.polimi.tiw.Bean.Prodotto;
import it.polimi.tiw.Bean.Utente;
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
import java.util.List;

@WebServlet(value = "/ultimiVisualizzati")
public class UltimiVisualizzati extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest richiesta, HttpServletResponse risposta) throws ServletException, IOException {
        List<Prodotto> prodotti;
    	
        // creo un dao prodotto
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);
        
        // prendo i 5 prodotti da visualizzare
        try{
            prodotti = daoProdotto.getCinqueProdottiHome(((Utente)richiesta.getSession(false).getAttribute("utente")).email());
        } catch (SQLException e) {
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // scrivo la lista di prodotti come stringa in json
        Gson gson = new Gson();
        String json = gson.toJson(prodotti);
        
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
