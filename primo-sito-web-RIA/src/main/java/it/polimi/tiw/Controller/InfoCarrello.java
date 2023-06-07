package it.polimi.tiw.Controller;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import it.polimi.tiw.Bean.CarrelloFornitore;
import it.polimi.tiw.Bean.Fornitore;
import it.polimi.tiw.Bean.InfoCarrelloFornitore;
import it.polimi.tiw.Bean.InfoProdottoCarrello;
import it.polimi.tiw.Bean.ProdottoCarrello;
import it.polimi.tiw.DAO.DAO_Fornitore;
import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(value = "/infoCarrello")
@MultipartConfig
public class InfoCarrello extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws ServletException, IOException {
    	List<CarrelloFornitore> carrello;
    	
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
        Type typeToken = new TypeToken<List<CarrelloFornitore>>(){}.getType();
        // e converto da JSON
        try {
            carrello = gson.fromJson(requestBody, typeToken);
        } catch (JsonSyntaxException e) {
        	risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // creo i dao
        DAO_Fornitore daoFornitore = new DAO_Fornitore(connessione);
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);

        // leggo il carrello come ArrayList<InfoCarrelloDiUnFornitore>
        ArrayList<InfoCarrelloFornitore> info = new ArrayList<InfoCarrelloFornitore>();
        try {
            for( CarrelloFornitore carrelloFornitore : carrello ){
            	// se non ci sono prodotti ritorno un errore
                if( carrelloFornitore.prodotti().size() == 0 ){
                    risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                
                // controllo che tutti i prodotti siano forniti dal fornitore
                for( ProdottoCarrello prod : carrelloFornitore.prodotti() ){
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
                
                // prendo la lista di info sui prodotti come ArrayList<InfoProdottoCarrello>
                ArrayList<InfoProdottoCarrello> prodotti = new ArrayList<InfoProdottoCarrello>();
                for( ProdottoCarrello prodotto : carrelloFornitore.prodotti() ){
                	// prendo il prezzo a cui è venduto il prodotto corrente dal fornitore corrente
                    Double prezzo = daoProdotto.getPrezzoScontato(prodotto.idProdotto(), f.id());
                    // leggo il prodotto come InfoProdottoCarrello
                    InfoProdottoCarrello prod = new InfoProdottoCarrello(prodotto.idProdotto(), daoProdotto.getProdotto(prodotto.idProdotto()).nome(), prezzo, prodotto.quantita() );
                    prodotti.add(prod);
                }
                
                // creo le info sul carrello del fornitore
                InfoCarrelloFornitore carrelloF = new InfoCarrelloFornitore(f.id(), f.nome(), prodotti);
                
                // aggiungo le info sulla parte di carrello del fornitore corrente
                info.add(carrelloF);
            }
        } catch (SQLException e) {
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // scrivo la stringa in json
        String json = gson.toJson(info);
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
