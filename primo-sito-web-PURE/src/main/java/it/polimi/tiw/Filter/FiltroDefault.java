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

        // a meno che io stia chiedendo il css (sia loggato che non, posso vederlo), redirecto ad home
        if( richiestaHTTP.getPathInfo() != "/css/stylesheet.css" ){
        	((HttpServletResponse) risposta).sendRedirect(richiestaHTTP.getContextPath() + "/home");
        	return;
        }
        	
        // mando al file
        chain.doFilter(richiesta, risposta);
    }

}
