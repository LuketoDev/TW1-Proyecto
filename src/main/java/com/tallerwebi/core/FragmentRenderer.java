package com.tallerwebi.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class FragmentRenderer {

    private final ThymeleafViewResolver thymeleafViewResolver;
    private final SpringTemplateEngine templateEngine;

    @Autowired
    public FragmentRenderer(ThymeleafViewResolver thymeleafViewResolver, SpringTemplateEngine templateEngine) {
        this.thymeleafViewResolver = thymeleafViewResolver;
        this.templateEngine = templateEngine;
    }

    public String render(String templateFragment, Model model, HttpServletRequest request, HttpServletResponse response) {
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale());
        context.setVariables(model.asMap());
        return templateEngine.process(templateFragment, context);
    }
}


