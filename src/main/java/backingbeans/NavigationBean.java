package backingbeans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;

@Named("navigationBean") // Good practice to name it explicitly
@SessionScoped
public class NavigationBean implements Serializable {

    @Inject
    private UserAccountBean userAccountBean; // Renamed from loginBean for clarity

    public void checkLogin() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext ec = context.getExternalContext();
        // Defensive: skip if no view root (first request) to avoid NPE
        if (context.getViewRoot() == null) {
            return;
        }

        String viewId = context.getViewRoot().getViewId();

        if (viewId != null && (viewId.contains("login.xhtml") || viewId.equals("/login.xhtml"))) {
            return; // Allow login page through
        }

        if (userAccountBean == null || !userAccountBean.isLoggedIn()) {
            try {
                ec.redirect(ec.getRequestContextPath() + "/login.xhtml");
                context.responseComplete(); // Important to stop further rendering
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}