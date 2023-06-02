package it.polimi.tiw.Filter;

import java.io.IOException;

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
public class FiltroDefault_SeNonLoggato implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest richiesta, ServletResponse risposta, FilterChain chain) throws IOException, ServletException {
    	// faccio il cast della richiesta prendo la sessione
    	HttpServletRequest richiestaHTTP = (HttpServletRequest) richiesta;
        HttpSession sessione = richiestaHTTP.getSession();

        // se l'utente non è loggato
        if( sessione.isNew() || ( sessione.getAttribute("utente") == null ) ){
        	// e se ha chiesto la risorsa "/"
        	if( ( richiestaHTTP.getPathInfo() == null ) || ( richiestaHTTP.getPathInfo() == "" ) || ( richiestaHTTP.getPathInfo() == "/" ) ){
        		// mando il redirect affinchè il percorso "/" rimandi al file di default login.html
        		((HttpServletResponse) risposta).sendRedirect(richiestaHTTP.getContextPath() + "/login.html");
        		return;
        	}
        }
        // altrimenti 404
        if( ( richiestaHTTP.getPathInfo() != "/css/stylesheet.css" ) && ( richiestaHTTP.getPathInfo() != "/js/utils.js" ) ){
        	((HttpServletResponse) risposta).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        	return;
        }
        
        // mando al file
        chain.doFilter(richiesta, risposta);
    }

}
