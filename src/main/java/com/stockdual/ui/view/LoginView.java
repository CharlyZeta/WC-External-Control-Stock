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

        // Injecting CSS dynamically guarantees it renders without Vite compilation
        // issues
        String styles = """
                    .login-view {
                        background-color: #0b0f19 !important;
                        position: relative;
                        overflow: hidden;
                    }
                    .login-blob-1, .login-blob-2, .login-blob-3 {
                        position: absolute;
                        border-radius: 50%;
                        filter: blur(80px);
                        z-index: 0;
                        opacity: 0.5;
                        animation: floatBlob 20s infinite alternate ease-in-out;
                    }
                    .login-blob-1 {
                        width: 60vw; height: 60vw; max-width: 600px; max-height: 600px;
                        background: radial-gradient(circle, #135bec, #0a3d9e);
                        top: -10%; left: -10%; animation-delay: 0s;
                    }
                    .login-blob-2 {
                        width: 50vw; height: 50vw; max-width: 500px; max-height: 500px;
                        background: radial-gradient(circle, #8b5cf6, #5b21b6);
                        bottom: -10%; right: -5%; animation-delay: -5s;
                    }
                    .login-blob-3 {
                        width: 40vw; height: 40vw; max-width: 400px; max-height: 400px;
                        background: radial-gradient(circle, #0ea5e9, #0369a1);
                        top: 40%; left: 40%; animation-delay: -10s;
                    }
                    @keyframes floatBlob {
                        0% { transform: translate(0, 0) scale(1); }
                        33% { transform: translate(30px, -50px) scale(1.1); }
                        66% { transform: translate(-30px, 20px) scale(0.9); }
                        100% { transform: translate(20px, 40px) scale(1.05); }
                    }
                    .login-glass-card {
                        position: relative;
                        z-index: 1;
                        background: rgba(16, 22, 34, 0.45);
                        backdrop-filter: blur(28px) saturate(180%);
                        -webkit-backdrop-filter: blur(28px) saturate(180%);
                        border: 1px solid rgba(255, 255, 255, 0.1);
                        border-radius: 36px;
                        padding: 48px 40px;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        max-width: 420px;
                        width: 90%;
                        box-shadow: 0 30px 60px -12px rgba(0, 0, 0, 0.6), inset 0 1px 0 rgba(255, 255, 255, 0.15);
                        animation: cardEntrance 1s cubic-bezier(0.16, 1, 0.3, 1) forwards;
                    }
                    @keyframes cardEntrance {
                        from { opacity: 0; transform: translateY(40px) scale(0.95); }
                        to { opacity: 1; transform: translateY(0) scale(1); }
                    }
                    .login-title {
                        font-size: 2.2rem;
                        font-weight: 800;
                        background: linear-gradient(135deg, #ffffff 0%, #a5b4cb 100%);
                        background-clip: text;
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        text-align: center;
                        margin: 0 0 8px 0;
                    }
                    .login-subtitle {
                        font-size: 0.95rem;
                        color: #92a4c9;
                        text-align: center;
                        margin: 0 0 32px 0;
                        line-height: 1.5;
                    }
                    vaadin-login-form::part(form) {
                        background: transparent !important;
                        box-shadow: none !important;
                        padding: 0 !important;
                    }
                """;
        getElement().appendChild(new com.vaadin.flow.dom.Element("style").setText(styles));

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
