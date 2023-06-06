package it.polimi.tiw.Controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Serial;

@WebServlet(value = "/logout")
public class Logout extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {
        
    	// imposto la codifica
    	risposta.setCharacterEncoding("UTF-8");

    	// rendo la sessione non valida
        richiesta.getSession(false).invalidate();
        risposta.setStatus(HttpServletResponse.SC_OK);
    }

}
