/*
 * @(#)HttpContextData.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

/**
 * Classe protegida - Alterações somente por CODEOWNERS.
 */

package br.com.blackhunter.hunter_wallet.rest_api.core.dto;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Classe <code>HttpContextData</code>.
 * <p>Classe utilitária para gerenciar o contexto HTTP da requisição atual.</p>
 * <p>Utiliza ThreadLocal para armazenar a requisição HTTP atual de forma isolada por thread,
 * permitindo acesso ao contexto da requisição em qualquer ponto da aplicação.</p>
 */
public class HttpContextData {
    /**
     * Armazena a requisição HTTP atual isolada por thread.
     */
    private static final ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<>();

    /**
     * Obtém a requisição HTTP atual associada à thread em execução.
     * 
     * @return A requisição HTTP atual ou null se não houver requisição associada
     */
    public static HttpServletRequest getCurrentRequest() {
        return currentRequest.get();
    }

    /**
     * Define a requisição HTTP atual para a thread em execução.
     * 
     * @param request A requisição HTTP a ser associada à thread atual
     */
    public static void setCurrentRequest(HttpServletRequest request) {
        currentRequest.set(request);
    }

    /**
     * Limpa a requisição HTTP associada à thread atual.
     * <p>Este método deve ser chamado após o processamento da requisição para evitar
     * vazamento de memória e informações entre requisições.</p>
     */
    public static void clear() {
        currentRequest.remove();
    }
}
