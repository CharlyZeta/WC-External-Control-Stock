package com.stockdual;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme("stockdual")
@Push
public class StockDualApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(StockDualApplication.class, args);
    }
}
