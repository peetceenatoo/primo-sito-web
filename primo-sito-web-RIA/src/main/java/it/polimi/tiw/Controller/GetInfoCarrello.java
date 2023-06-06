package it.polimi.tiw.Controller;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import it.polimi.tiw.Bean.Fornitore;
import it.polimi.tiw.Bean.InfoCarrello;
import it.polimi.tiw.Bean.ProdottoInfoCarrello;
import it.polimi.tiw.DAO.DAO_Fornitore;
import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;

import java.awt.print.PageFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(value = "/infoCarrello")
@MultipartConfig
public class GetInfoCarrello extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws ServletException, IOException {
    	List<InfoCarrello> carrello;
    	
    	// imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");
    	
        // leggo la stringa in input
        BufferedReader reader = richiesta.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while( (line = reader.readLine()) != null )
            sb.append(line);
        String requestBody = sb.toString();

        // creo un oggetto gson
        Gson gson = new Gson();
        
        // prendo il token della classe da ritornare
        Type typeToken = new TypeToken<List<InfoCarrello>>(){}.getType();

        // converto da JSON
        try {
            carrello = gson.fromJson(requestBody, typeToken);
        } catch (JsonSyntaxException e) {
        	risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // creo i dao
        DAO_Fornitore daoFornitore = new DAO_Fornitore(connessione);
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);

        // leggo il carrello come JsonArray
        JsonArray risultato = new JsonArray();
        try {
            for( InfoCarrello carrelloFornitore : carrello ){
            	// se non ci sono prodotti ritorno un errore
                if( carrelloFornitore.prodotti().size() == 0 ){
                    risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                
                // controllo che tutti i prodotti siano forniti dal fornitore
                for( ProdottoInfoCarrello prod : carrelloFornitore.prodotti() ){
                    try {
                        if( ( prod == null ) || !daoProdotto.isFornitoDaFornitore(prod.idProdotto(), carrelloFornitore.idFornitore()) ){
                            risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            return;
                        }
                    } catch(SQLException e) {
                        risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return;
                    }
                }
            	
            	// leggo il fornitore
                Fornitore f = daoFornitore.getFornitore(carrelloFornitore.idFornitore());
                if( f == null ){
                    risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
                
                // prendo l'InfoCarrello come JsonObject
                JsonObject temp = new JsonObject();
                temp.addProperty("id", f.id());
                temp.addProperty("nome", f.nome());

                // prendo i prodotti come JsonArray
                JsonArray prodotti = new JsonArray();
                for( ProdottoInfoCarrello prodotto : carrelloFornitore.prodotti() ){
                	// prendo il prezzo a cui è venduto il prodotto corrente dal fornitore corrente
                    Double prezzo = daoProdotto.getPrezzoScontato(prodotto.idProdotto(), f.id());
                    // leggo il prodotto come JsonObject
                    JsonObject prod = new JsonObject();
                    prod.addProperty("id", prodotto.idProdotto());
                    prod.addProperty("nome", daoProdotto.getProdotto(prodotto.idProdotto()).nome());
                    prod.addProperty("prezzo", prezzo);
                    prod.addProperty("quantita", prodotto.quantita());
                    prodotti.add(prod);
                }
                // aggiungo il JsonArray di prodotti al JsonArray da ritornare
                temp.add("prodotti", prodotti);
                risultato.add(temp);
            }
        } catch (SQLException e) {
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // scrivo la stringa in json
        String json = gson.toJson(risultato);
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
