/*
 * @(#)BaseController.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

/**
 * Classe protegida - Alterações somente por CODEOWNERS.
 */

package br.com.blackhunter.hunter_wallet.rest_api.core.controller;

import br.com.blackhunter.hunter_wallet.rest_api.core.dto.ApiResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * <p>Classe <code>BaseController</code>.</p>
 * <p>Controller base com métodos genéricos para operações CRUD padrão.</p>
 * <p>Todas as exceções são propagadas para o GlobalExceptionHandler.</p>
 *
 * @param <T> Tipo da Payload/DTO
 * @param <ID> Tipo do ID (Long, UUID, etc.)
 */
@Getter
public abstract class BaseController<T, ID> {

    protected static final String SUCCESS = "success";
    protected static final String CREATED = "created";
    protected static final String UPDATED = "updated";
    protected static final String DELETED = "deleted";

    protected static final int SUCCESS_CODE = HttpStatus.OK.value();
    protected static final int CREATED_CODE = HttpStatus.CREATED.value();
    protected static final int NO_CONTENT_CODE = HttpStatus.NO_CONTENT.value();

    /**
     * Retorna padrão de resposta para sucesso.
     * 
     * @param data Dados a serem retornados na resposta
     * @return ResponseEntity com ApiResponse contendo os dados e status de sucesso
     */
    public <R> ResponseEntity<ApiResponse<R>> ok(R data) {
        return ResponseEntity.ok(new ApiResponse<>(SUCCESS, SUCCESS_CODE, data));
    }

    /**
     * Retorna padrão de resposta para recurso criado com sucesso.
     * 
     * @param data Dados do recurso criado
     * @return ResponseEntity com ApiResponse contendo os dados e status de criação
     */
    public <R> ResponseEntity<ApiResponse<R>> created(R data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(CREATED, CREATED_CODE, data));
    }

    /**
     * Retorna padrão de resposta para operações bem-sucedidas que não retornam conteúdo.
     * 
     * @return ResponseEntity com ApiResponse sem conteúdo e status NO_CONTENT
     */
    public ResponseEntity<ApiResponse<Void>> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>(DELETED, NO_CONTENT_CODE, null));
    }

    // -------------------------------------------------------------------------
    // Métodos CRUD padrão
    // -------------------------------------------------------------------------

    /**
     * Busca um recurso pelo seu identificador.
     * 
     * @param id Identificador único do recurso
     * @return ResponseEntity com ApiResponse contendo o recurso encontrado
     */
    @GetMapping("/{id}")
    public abstract ResponseEntity<ApiResponse<T>> findById(@PathVariable("id") ID id);

    /**
     * Busca todos os recursos disponíveis.
     * 
     * @return ResponseEntity com ApiResponse contendo a lista de recursos
     */
    @GetMapping
    public abstract ResponseEntity<ApiResponse<Iterable<T>>> findAll();

    /**
     * Cria um novo recurso.
     * 
     * @param payload Dados do recurso a ser criado
     * @return ResponseEntity com ApiResponse contendo o recurso criado
     */
    @PostMapping
    public abstract ResponseEntity<ApiResponse<T>> create(@RequestBody T payload);

    /**
     * Atualiza um recurso existente.
     * 
     * @param id Identificador único do recurso a ser atualizado
     * @param payload Novos dados do recurso
     * @return ResponseEntity com ApiResponse contendo o recurso atualizado
     */
    @PutMapping("/{id}")
    public abstract ResponseEntity<ApiResponse<T>> update(
            @PathVariable("id") ID id,
            @RequestBody T payload);

    /**
     * Remove um recurso existente.
     * 
     * @param id Identificador único do recurso a ser removido
     * @return ResponseEntity com ApiResponse sem conteúdo
     */
    @DeleteMapping("/{id}")
    public abstract ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") ID id);

    // -------------------------------------------------------------------------
    // Métodos opcionais
    // -------------------------------------------------------------------------

    /**
     * Verifica se um recurso existe pelo seu identificador.
     * 
     * @param id Identificador único do recurso
     * @return ResponseEntity com ApiResponse contendo true se o recurso existe, false caso contrário
     */
    @GetMapping("/exists/{id}")
    public abstract ResponseEntity<ApiResponse<Boolean>> existsById(@PathVariable("id") ID id);

    /**
     * Conta o número total de recursos disponíveis.
     * 
     * @return ResponseEntity com ApiResponse contendo o número total de recursos
     */
    @GetMapping("/count")
    public abstract ResponseEntity<ApiResponse<Long>> count();
}