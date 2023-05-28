package it.polimi.tiw.Controller;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/logout")
public class Logout extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest richiesta, HttpServletResponse risposta) throws IOException {

    	// basta porre l'attributo utente = null perchè il filtro controlla entrambe le cose...
    	// in generale, così, la sessione è strettamente equivalente ad avere fatto il login
        richiesta.getSession(false).invalidate();

        // imposto la codifica
        risposta.setCharacterEncoding("UTF-8");
        // effettuo il redirect alla pagina di login
        risposta.sendRedirect(getServletContext().getContextPath() + "/login");
    }


}
