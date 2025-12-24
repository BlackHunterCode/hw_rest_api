/*
 * @(#)TransactionServiceImpl.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.finey.rest.finance.transaction.service.impl;

import br.com.blackhunter.finey.rest.auth.util.CryptUtil;
import br.com.blackhunter.finey.rest.auth.util.JwtUtil;
import br.com.blackhunter.finey.rest.core.dto.TransactionPeriodDate;
import br.com.blackhunter.finey.rest.core.util.DateTimeUtil;
import br.com.blackhunter.finey.rest.finance.transaction.dto.TotalTransactionsPeriod;
import br.com.blackhunter.finey.rest.finance.transaction.dto.TransactionData;
import br.com.blackhunter.finey.rest.finance.transaction.entity.TransactionEntity;
import br.com.blackhunter.finey.rest.finance.transaction.enums.TransactionType;
import br.com.blackhunter.finey.rest.finance.transaction.mapper.TransactionMapper;
import br.com.blackhunter.finey.rest.finance.transaction.payload.TransactionPayload;
import br.com.blackhunter.finey.rest.finance.transaction.repository.TransactionRepository;
import br.com.blackhunter.finey.rest.finance.transaction.service.TransactionService;
import br.com.blackhunter.finey.rest.integrations.financial_integrator.FinancialIntegrator;
import br.com.blackhunter.finey.rest.integrations.financial_integrator.FinancialIntegratorManager;
import br.com.blackhunter.finey.rest.useraccount.entity.UserAccountEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * <p>Classe <code>TransactionServiceImpl</code>.</p>
 * <p>Essa classe implementa o processamento e as regras de negócio dos serviços de transações</p>
 * */
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    @Value("${hunter.secrets.pluggy.crypt-secret}")
    private String PLUGGY_CRYPT_SECRET;

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final JwtUtil jwtUtil;
    private final FinancialIntegratorManager financialIntegratorManager;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper,
            JwtUtil jwtUtil,
            FinancialIntegratorManager financialIntegratorManager
    ) {
        this.transactionRepository      = transactionRepository;
        this.transactionMapper          = transactionMapper;
        this.jwtUtil                    = jwtUtil;
        this.financialIntegratorManager = financialIntegratorManager;
    }

    /**
     * @param transactionPayload payload contendo os dados da transação a ser registrada.
     * <p>Esse método registra uma transação no banco de dados.</p>
     * <p>Uma transação pode ser um gasto ou um ganho.</p>
     * <p>
     *   Obs: Esse método vai ser mais usado em um evento de <code>Webhook</code>
     *   ou manualmente pelo usuário através da <code>API REST</code>.
     * </p>
     *
     * @author Victor Barberino
     * @return Retorna os dados da transação registrada.
     * */
    @Override
    public TransactionData registerTransaction(@Validated TransactionPayload transactionPayload) {
        TransactionEntity transactionEntity = transactionMapper.toEntity(transactionPayload);
        UserAccountEntity userAccountEntity =  jwtUtil.getUserAccountFromToken();
        transactionEntity.setUserAccount(userAccountEntity);
        return transactionMapper.toData(transactionRepository.save(transactionEntity));
    }

    /**
     * Calcula o total de ganhos e gastos para um período específico
     *
     * @param bankAccountIds ID da conta bancária
     * @param referenceDateMonthYear Data de referência (opcional, pode ser usado para filtros adicionais)
     * @param startDate Data inicial do período
     * @param endDate Data final do período
     * @return Objeto com os totais calculados
     */
    @Override
    @Deprecated
    public TotalTransactionsPeriod getTotalTransactionsPeriod(List<String> bankAccountIds, LocalDate referenceDateMonthYear, LocalDate startDate, LocalDate endDate) {
        // Criar um objeto TransactionPeriodDate uma única vez para evitar múltiplas chamadas com parâmetros diferentes
        TransactionPeriodDate periodDate = DateTimeUtil.getTransactionPeriodDate(referenceDateMonthYear, startDate, endDate);
        
        List<TransactionData> allTransactions = new ArrayList<>();
        for(String accountId: bankAccountIds) {
            log.debug("[DEBUG] Buscando transações para conta: {}", accountId);
            // Usar o mesmo objeto periodDate para todas as chamadas
            allTransactions.addAll(getAllTransactionsPeriodByAccountId(accountId, periodDate, null, null, null));
        }
        log.debug("[DEBUG] Total de transações acumuladas: {}", allTransactions.size());
        return calculateTotals(allTransactions);
    }

    /**
     * Método auxiliar para calcular os totais de ganhos e gastos
     */
    @Deprecated
    private TotalTransactionsPeriod calculateTotals(List<TransactionData> transactions) {
        BigDecimal totalEarnings = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (TransactionData transaction : transactions) {
            if (transaction.getType() == TransactionType.CREDIT) {
                // entrada
                totalEarnings = totalEarnings.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.DEBIT) {
                // saida
                // Garantir que valores de débito sejam positivos no total
                totalExpenses = totalExpenses.add(transaction.getAmount().abs());
            }
            // Ignora outros tipos se houver
        }

        return new TotalTransactionsPeriod(totalEarnings, totalExpenses);
    }

    /**
     * Busca todas as transações de uma conta bancária em um período específico.
     * @param accountId ID da conta bancária (criptografado)
     * @param periodDate Objeto TransactionPeriodDate
     *
     * @return lista de transações encontradas
     * */
    @Transactional
    public List<TransactionData> getAllTransactionsPeriodByAccountId(String accountId, TransactionPeriodDate periodDate) {
        return getAllTransactionsPeriodByAccountId(accountId, periodDate, null, null, null);
    }

    /**
     * Busca todas as transações de uma conta bancária em um período específico.
     * @param accountId ID da conta bancária (criptografado)
     * @param referenceDateMonthYear Data Mês/Ano de referência (opcional, pode ser usado para filtros adicionais)
     * @param startDate Data inicial do período
     * @param endDate Data final do período
     *
     * @return lista de transações encontradas
     * */
    @Override
    @Transactional
    public List<TransactionData> getAllTransactionsPeriodByAccountId(String accountId, TransactionPeriodDate periodDate, LocalDate referenceDateMonthYear, LocalDate startDate, LocalDate endDate) {
        try {
            String accountEntityId = CryptUtil.decrypt(accountId, PLUGGY_CRYPT_SECRET);
            TransactionPeriodDate transactionPeriodDate = periodDate == null ? DateTimeUtil.getTransactionPeriodDate(referenceDateMonthYear, startDate, endDate) : periodDate;

            log.debug("[DEBUG] Buscando transações para conta {} no período de {} até {}", 
                accountId, transactionPeriodDate.getStartDate(), transactionPeriodDate.getEndDate());
                
            // Verificar se o accountEntityId é um UUID válido
            UUID financialAccountId;
            try {
                financialAccountId = UUID.fromString(accountEntityId);
            } catch (IllegalArgumentException e) {
                log.error("[ERROR] ID de conta inválido após descriptografia: {}", accountEntityId);
                return new ArrayList<>();
            }

            // 1. buscar transações no período no banco de dados
            List<TransactionEntity> localTransactions = transactionRepository.findAllByFinancialAccountIdAndDateBetween(
                    financialAccountId,
                    transactionPeriodDate.getStartDate(),
                    transactionPeriodDate.getEndDate());

            log.debug("[DEBUG] Transações locais encontradas: {}", localTransactions.size());

            // 2. verfica se precisa sincronizar
            boolean needSync = hasGapsOrIsCurrentDay(localTransactions, transactionPeriodDate);

            log.debug("[DEBUG] Necessita sincronizar? {}", needSync);

            if(needSync) {
                FinancialIntegrator financialIntegrator = financialIntegratorManager.getFinancialIntegrator();
                String originalPluggyAccountId = financialIntegrator.getOriginalFinancialAccountIdByTargetId(UUID.fromString(accountEntityId));
                
                if (originalPluggyAccountId == null || originalPluggyAccountId.isEmpty()) {
                    log.warn("[WARN] ID original do Pluggy não encontrado para a conta: {}", accountId);
                    return localTransactions.stream().map(transactionMapper::toData).toList();
                }

                log.debug("[DEBUG] ID original do Pluggy encontrado: {}", originalPluggyAccountId);

                // 3. buscar transações no período na pluggy
                List<TransactionEntity> pluggyTransactions = financialIntegrator.getAllTransactionsPeriodByTargetId(
                        originalPluggyAccountId,
                        transactionPeriodDate.getStartDate(),
                        transactionPeriodDate.getEndDate()
                );

                log.debug("[DEBUG] Transações Pluggy encontradas: {}", pluggyTransactions.size());

                if (!pluggyTransactions.isEmpty()) {
                    // 4. upsert no banco de dados
                    transactionRepository.upsertAll(pluggyTransactions);
                    log.debug("[DEBUG] Transações persistidas no banco de dados");
                    
                    // 5. Merge para retorno (garante dados atualizados)
                    localTransactions = transactionRepository.findAllByFinancialAccountIdAndDateBetween(
                            UUID.fromString(accountEntityId),
                            transactionPeriodDate.getStartDate(),
                            transactionPeriodDate.getEndDate()
                    );
                    log.debug("[DEBUG] Transações atualizadas após persistência: {}", localTransactions.size());
                }
            }

            log.debug("[DEBUG] Transações finais retornadas: {}", localTransactions.size());
            return localTransactions.stream().map(transactionMapper::toData).toList();
        } catch (Exception e) {
            log.error("[ERROR] Erro ao buscar transações: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /* Métodos privados */
    private boolean hasGapsOrIsCurrentDay(List<TransactionEntity> local, TransactionPeriodDate period) {
        if (local.isEmpty()) return true;

        // 1. Se inclui o dia de hoje - verificar se já sincronizou hoje
        if (!period.getEndDate().isBefore(LocalDate.now())) {
            // Verificar se já temos transações de hoje
            boolean hasTransactionsFromToday = local.stream()
                    .anyMatch(tx -> tx.getTransactionLocalDate().equals(LocalDate.now()));
                    
            // Se já temos transações de hoje e foram atualizadas recentemente, não sincronizar novamente
            if (hasTransactionsFromToday) {
                // Opcional: verificar se as transações de hoje são recentes (últimas X horas)
                LocalDateTime recentThreshold = LocalDateTime.now().minusHours(1); // Configurável
                boolean hasRecentTransactions = local.stream()
                        .filter(tx -> tx.getTransactionLocalDate().equals(LocalDate.now()))
                        .anyMatch(tx -> tx.getCreatedAt().isAfter(recentThreshold));
                        
                // Se temos transações recentes, não precisamos sincronizar novamente
                if (hasRecentTransactions) {
                    return false;
                }
            }
            
            return true; // Sincronizar se não temos transações de hoje ou se não são recentes
        }

        // O resto da lógica permanece igual
        LocalDate minDate = local.stream()
                .map(TransactionEntity::getTransactionLocalDate)
                .min(LocalDate::compareTo)
                .orElse(period.getStartDate());

        LocalDate maxDate = local.stream()
                .map(TransactionEntity::getTransactionLocalDate)
                .max(LocalDate::compareTo)
                .orElse(period.getEndDate());

        boolean coversAll = !minDate.isAfter(period.getStartDate()) && !maxDate.isBefore(period.getEndDate());

        return !coversAll;
    }

    // Método hasInternalGaps removido pois não é mais utilizado após otimização do hasGapsOrIsCurrentDay
}
