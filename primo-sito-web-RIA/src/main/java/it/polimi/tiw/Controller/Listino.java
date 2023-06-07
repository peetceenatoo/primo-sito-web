package it.polimi.tiw.Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.Bean.Coppia;
import it.polimi.tiw.Bean.ProdottoDiUnFornitore;
import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;

@WebServlet(value = "/listino")
public class Listino extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest richiesta, HttpServletResponse risposta) throws ServletException, IOException {

        // creo un dao prodotto
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);
        
        // prendo il listino
        List<ProdottoDiUnFornitore> listino;
        try{
            listino = daoProdotto.getListino();
        } catch (SQLException e ) {
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // scrivo il listino come stringa in JS
        Gson gson = new Gson();
        String json = gson.toJson(listino);
        
        // invio il listino
        risposta.setCharacterEncoding("UTF-8");
        risposta.setStatus(HttpServletResponse.SC_OK);
        risposta.setContentType("application/json");
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
