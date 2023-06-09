package it.polimi.tiw.Controller;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.DAO.DAO_Prodotto;
import it.polimi.tiw.Utility.ConnectionInitializer;

@WebServlet(value="/immagine")
@MultipartConfig
public class Immagine extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private String percorsoBase;
    private Connection connessione;

    public void init() throws UnavailableException {
        this.connessione = ConnectionInitializer.getConnection(getServletContext());
        this.percorsoBase = getServletContext().getInitParameter("percorsoImmagini");
    }

    protected void doGet(HttpServletRequest richiesta, HttpServletResponse risposta) throws ServletException, IOException {
    	String percorsoRelativo;
    	File file;
    	
		// imposto la codifica per leggere i parametri, coerentemente all'HTML
        richiesta.setCharacterEncoding("UTF-8");

        // prendo l'id del prodotto 
        int idProdotto;
        try{
            idProdotto = Integer.parseInt(richiesta.getParameter("idProdotto"));
        } catch (NumberFormatException e) {
            risposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // creo un dao prodotto
        DAO_Prodotto daoProdotto = new DAO_Prodotto(connessione);
        
        // prendo il percorso relativo
        try{
            percorsoRelativo = daoProdotto.getPercorsoFoto(idProdotto);
        } catch (SQLException e) {
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if( percorsoRelativo == null ){
            risposta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // imposto la codifica
        risposta.setCharacterEncoding("UTF-8");

        // recupero il file
        file = new File(this.percorsoBase, percorsoRelativo);
        // se il file non esiste o se risulta essere una directory
        if ( !file.exists() || file.isDirectory() ) {
        	// creo uno stream per aprire l'icona "non trovato" in lettura
            InputStream inputStream = getServletContext().getResourceAsStream("/WEB-INF/imgs/non-trovato.png");
            // creo un output stream di byte su cui scrivere con cui poi riempirò quello della risposta
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // copio a blocchi di byte l'icona nell'output stream usando un buffer da 4096 byte
            byte[] buffer = new byte[4096];
            int tempNumDiBytes;
            while( ( tempNumDiBytes = inputStream.read(buffer)) != -1 )
                outputStream.write(buffer, 0, tempNumDiBytes);
            
            // imposto il tipo della risposta
            risposta.setContentType("image/png");
            // imposto la dimensinone della risposta
            risposta.setContentLength(outputStream.size());
            // imposto Content-Disposition come inline (e scelgo un nome di default per il file)
            risposta.setHeader("Content-Disposition", "inline");
            // scrivo sullo stream della risposta
            risposta.getOutputStream().write(outputStream.toByteArray());

            return;
        }
        // altrimenti imposto la dimensione del file di risposta
        risposta.setContentLength((int)file.length());
        
        // imposto il tipo della risposta, in generale non conoscendo il formato della foto 
        risposta.setHeader("Content-Type", getServletContext().getMimeType(percorsoRelativo));
        // imposto Content-Disposition come inline (e scelgo un nome di default per il file)
        risposta.setHeader("Content-Disposition", "inline");
        // scrivo l'immagine sullo stream della risposta
        Files.copy(file.toPath(), risposta.getOutputStream());
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
