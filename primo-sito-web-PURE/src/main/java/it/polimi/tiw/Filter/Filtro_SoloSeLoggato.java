package it.polimi.tiw.Filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter({"/home", "/risultati", "/visualizzato", "/ordini", "/carrello", "/immagine", "/logout"})
public class Filtro_SoloSeLoggato implements Filter {

    @Override
    public void doFilter(ServletRequest richiesta, ServletResponse risposta, FilterChain chain) throws IOException, ServletException {
        // faccio il cast della richiesta e prendo la sessione
    	HttpServletRequest richiestaHTTP = (HttpServletRequest) richiesta;
        HttpSession sessione = richiestaHTTP.getSession();
        
        // se la sessione per l'utente non è attiva, forzo la pagina di login
        // 		( per ora l'esistenza della sessione è strettamente equivalente ad aver fatto il login,
        // 		  quindi non serve controllare l'attributo utente perchè con il logout viene cancellata
        // 		  e senza login non viene creata )
        if( sessione.isNew() || ( sessione.getAttribute("utente") == null ) )
            ((HttpServletResponse) risposta).sendRedirect(richiestaHTTP.getContextPath() + "/login");
        // altrimenti mando alla servlet
        else 
            chain.doFilter(richiesta, risposta);
    }
    
}
