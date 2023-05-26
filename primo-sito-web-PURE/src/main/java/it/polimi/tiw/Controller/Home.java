package it.polimi.tiw.Controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value= {"/home"})
public class Home extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void init(){
    	
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response){

    }

    @Override
    public void destroy() {
    	
    }

}
