package com.stockdual.ui.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.component.UI;

@Route("login")
@PageTitle("Login — StockDual")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        // Enforce dark mode directly on this view to prevent white backgrounds and
        // default light form
        getElement().getThemeList().add(Lumo.DARK);

        addClassName("login-view");
        setSizeFull();
        setPadding(false);
        setMargin(false);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);


        // Animated Background Blobs
        Div blob1 = new Div();
        blob1.addClassName("login-blob-1");
        Div blob2 = new Div();
        blob2.addClassName("login-blob-2");
        Div blob3 = new Div();
        blob3.addClassName("login-blob-3");

        // Header
        H2 title = new H2("StockDual");
        title.addClassName("login-title");

        Paragraph subtitle = new Paragraph("Control de Stock Multi-ubicación");
        subtitle.addClassName("login-subtitle");

        // Login form configuration
        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.Form form = i18n.getForm();
        form.setTitle("Iniciar Sesión");
        form.setUsername("Usuario");
        form.setPassword("Contraseña");
        form.setSubmit("Ingresar");
        form.setForgotPassword("");
        i18n.setForm(form);

        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle("Error de autenticación");
        errorMessage.setMessage("Usuario o contraseña incorrectos. Intente nuevamente.");
        i18n.setErrorMessage(errorMessage);

        loginForm.setI18n(i18n);
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        // Layout
        Div loginInnerContent = new Div(title, subtitle, loginForm);
        loginInnerContent.getStyle().set("width", "100%");
        loginInnerContent.getStyle().set("display", "flex");
        loginInnerContent.getStyle().set("flex-direction", "column");
        loginInnerContent.getStyle().set("align-items", "stretch");

        Div glassCard = new Div(loginInnerContent);
        glassCard.addClassName("login-glass-card");

        add(blob1, blob2, blob3, glassCard);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
