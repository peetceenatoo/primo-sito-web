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
        // faccio il cast della richiesta prendo la sessione
    	HttpServletRequest richiestaHTTP = (HttpServletRequest) richiesta;
        HttpSession sessione = richiestaHTTP.getSession();
        
        // se la sessione per l'utente non è attiva, forzo la pagina di login
        if( sessione.isNew() || ( sessione.getAttribute("utente") == null ) )
            ((HttpServletResponse) risposta).sendRedirect(richiestaHTTP.getContextPath() + "/login");
        // altrimenti proseguo
        else 
            chain.doFilter(richiesta, risposta);
    }
    
}
