package it.polimi.tiw.Filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter({"/login"})
public class Filtro_SoloSeNonLoggato implements Filter {

        @Override
        public void doFilter(ServletRequest richiesta, ServletResponse risposta, FilterChain chain) throws IOException, ServletException {
        	// faccio il cast della richiesta e prendo la sessione
        	HttpServletRequest richiestaHTTP = (HttpServletRequest) richiesta;
            HttpSession sessione = richiestaHTTP.getSession();
            
            // se la sessione per l'utente è attiva, impedisco di andare alla pagina di login
            if( !sessione.isNew() && ( sessione.getAttribute("utente") != null ) )
                ((HttpServletResponse) risposta).sendRedirect(richiestaHTTP.getContextPath() + "/home");
            // altrimenti mando alla servlet
            else
                chain.doFilter(richiesta, risposta);
        }

}
