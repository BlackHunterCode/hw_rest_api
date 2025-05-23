/*
 * @(#)HttpContextInterceptor.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

/**
 * Classe protegida - Alterações somente por CODEOWNERS.
 */

package br.com.blackhunter.hunter_wallet.rest_api.config.inteceptor;

import br.com.blackhunter.hunter_wallet.rest_api.core.dto.HttpContextData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Classe <code>HttpContextInterceptor</code>.
 * <p>Interceptador responsável por gerenciar o contexto HTTP das requisições.</p>
 * <p>Implementa a interface {@link HandlerInterceptor} para interceptar requisições HTTP.</p>
 * <p>Este interceptador armazena a requisição atual no contexto e limpa após o processamento.</p>
 */
@Component
public class HttpContextInterceptor implements HandlerInterceptor {
    /**
     * @param request Requisição HTTP atual
     * @param response Resposta HTTP
     * @param handler Manipulador que processará a requisição
     * <p>Método executado antes do processamento da requisição pelo controlador.</p>
     * <p>Armazena a requisição HTTP atual no contexto para uso posterior.</p>
     * @return true se o processamento deve continuar, false caso contrário
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpContextData.setCurrentRequest(request);
        return HttpContextData.getCurrentRequest() != null;
    }

    /**
     * @param request Requisição HTTP atual
     * @param response Resposta HTTP
     * @param handler Manipulador que processou a requisição
     * @param ex Exceção lançada durante o processamento, se houver
     * <p>Método executado após a conclusão do processamento da requisição.</p>
     * <p>Limpa o contexto HTTP para evitar vazamento de memória e informações entre requisições.</p>
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HttpContextData.clear();
    }
}
