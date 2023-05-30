package it.polimi.tiw.Filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("/")
public class FiltroDefault implements Filter {

    private ServletContext context;

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.context = config.getServletContext();
    }
    
    @Override
    public void doFilter(ServletRequest richiesta, ServletResponse risposta, FilterChain chain) throws IOException, ServletException {
    	// faccio il cast della richiesta prendo la sessione
    	HttpServletRequest richiestaHTTP = (HttpServletRequest) richiesta;
        HttpSession session = richiestaHTTP.getSession();

        // se sto scaricando il css, il quale non è mappato su una servlet, lo devo servire
        if( richiestaHTTP.getPathInfo() == "/css/stylesheet.css" ){
        	// creo uno stream per aprire lo stylesheet in lettura
        	InputStream inputStream = Files.newInputStream(Paths.get(context.getRealPath("/"), "/css/stylesheet.css.css"));
        	// prendo  su cui scrivere
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	
        	// Copio a blocchi di byte l'icona nell'output stream usando un buffer da 4096 byte
        	byte[] buffer = new byte[4096];
            int tempNumDiBytes;
            while( ( tempNumDiBytes = inputStream.read(buffer)) != -1 )
                outputStream.write(buffer, 0, tempNumDiBytes);
            
            // imposto il tipo del contenuto
            risposta.setContentType("text/css");
            // scrivo sullo stream della risposta
            risposta.getOutputStream().write(outputStream.toByteArray());
 
            return;
        }
        // altrimenti mando il redirect alla home (e non continuo sulla chain: se questo filtro di default è stato chiamato è perchè nessun'altra risorsa dev'essere raggiunta
        ((HttpServletResponse) risposta).sendRedirect(richiestaHTTP.getContextPath() + "/home");
    }

}
