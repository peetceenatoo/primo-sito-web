package it.polimi.tiw.Utility;                                                            
                                                                                                          
import org.thymeleaf.TemplateEngine;                                                                      
import org.thymeleaf.templatemode.TemplateMode;                                                           
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;                                     
import org.thymeleaf.web.servlet.JavaxServletWebApplication;                                              
                                                                                                          
public abstract class TemplateInitializer {                                                                   
                                                                                                          
    public static TemplateEngine getTemplateEngine(JavaxServletWebApplication application){               
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);
        templateResolver.setTemplateMode(TemplateMode.HTML);                                              
        templateResolver.setPrefix("WEB-INF/");                                                           
        templateResolver.setSuffix(".html");                                                              
        templateResolver.setCharacterEncoding("UTF-8");                                                   
        TemplateEngine templateEngine = new TemplateEngine();                                             
        templateEngine.setTemplateResolver(templateResolver);                                             
                                                                                                          
        return templateEngine;                                                                            
    }                                                                                                     
}                                                                                                         
                                                                                                          