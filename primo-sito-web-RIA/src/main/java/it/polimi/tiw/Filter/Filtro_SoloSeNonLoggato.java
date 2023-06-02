package it.polimi.tiw.Filter;


import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter({"/login", "/login.html", "js/login.js"})
public class Filtro_SoloSeNonLoggato implements Filter {

        @Override
        public void init(FilterConfig filterConfig) {

        }

        @Override
        public void doFilter(ServletRequest richiesta, ServletResponse risposta, FilterChain chain) throws IOException, ServletException {
        	// faccio il cast della richiesta prendo la sessione
        	HttpServletRequest richiestaHTTP = (HttpServletRequest) richiesta;
            HttpSession sessione = richiestaHTTP.getSession();
            
            // se la sessione per l'utente non è attiva, ritorno un errore
            if ( !sessione.isNew() && ( sessione.getAttribute("utente") != null ) ){
                ((HttpServletResponse)risposta).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // mando alla servlet/file
            chain.doFilter(richiesta, risposta);
        }

}
