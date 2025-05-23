/*
 * @(#)WebConfig.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

/**
 * Classe protegida - Alterações somente por CODEOWNERS.
 */

package br.com.blackhunter.hunter_wallet.rest_api.config;

import br.com.blackhunter.hunter_wallet.rest_api.config.inteceptor.HttpContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Classe <code>WebConfig</code>.
 * <p>Configuração do Spring MVC para a aplicação Hunter Wallet REST API.</p>
 * <p>Implementa a interface {@link WebMvcConfigurer} para personalizar a configuração do Spring MVC.</p>
 * <p>Responsável por registrar interceptadores e outras configurações relacionadas à web.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final HttpContextInterceptor httpContextInterceptor;

    /**
     * Construtor da classe.
     * @param httpContextInterceptor Interceptador de contexto HTTP a ser registrado
     * <p>Injeta as dependências necessárias para a configuração do Spring MVC.</p>
     */
    public WebConfig(
        HttpContextInterceptor httpContextInterceptor
    ) {
        this.httpContextInterceptor = httpContextInterceptor;
    }

    /**
     * @param registry
     * <p>Método que adiciona o inteceptador de requisições HTTP.</p>
     * */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpContextInterceptor);
    }
}
