package backingbeans;



import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;

@Named
@RequestScoped
public class NavigationBean {

    @Inject
    private UserAccountBean loginBean;

    public void checkLogin() {
        if (!loginBean.isLoggedIn()) {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            try {
                ec.redirect(ec.getRequestContextPath() + "/login.xhtml");
            } catch (IOException e) {
                e.printStackTrace(); // Handle error
            }
        }
    }
}
