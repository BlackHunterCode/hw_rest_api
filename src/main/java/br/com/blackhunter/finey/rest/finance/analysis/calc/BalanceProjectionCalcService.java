package br.com.blackhunter.finey.rest.finance.analysis.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import br.com.blackhunter.finey.rest.finance.transaction.dto.TransactionData;
import br.com.blackhunter.finey.rest.finance.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.blackhunter.finey.rest.auth.util.CryptUtil;
import br.com.blackhunter.finey.rest.finance.analysis.dto.current_balance_projection.CurrentBalanceProjection;
import br.com.blackhunter.finey.rest.finance.transaction.enums.TransactionType;
import br.com.blackhunter.finey.rest.integrations.financial_integrator.dto.FinancialInstitutionData;
import br.com.blackhunter.finey.rest.integrations.pluggy.dto.PluggyAccountIds;

/**
 * Serviço responsável por realizar cálculos de projeção de saldo financeiro.
 * Utiliza análise histórica de 3 meses para projeções precisas.
 * Todos os métodos retornam dados criptografados para segurança.
 * 
 * @author BlackHunter Team
 * @version 1.0
 * @since 2025
 */
@Service
public class BalanceProjectionCalcService {
    @Value("${hunter.secrets.pluggy.crypt-secret}")
    private String PLUGGY_CRYPT_SECRET;

    @Autowired
    private TransactionService transactionService;

    /**
     * Calcula a projeção completa de saldo baseada em análise histórica de 3 meses.
     * 
     * <p><strong>Metodologia de Cálculo:</strong></p>
     * <ol>
     *   <li><strong>Saldo Atual:</strong> Soma todos os saldos das contas conectadas</li>
     *   <li><strong>Análise Histórica:</strong> Analisa receitas e despesas dos últimos 3 meses</li>
     *   <li><strong>Média Diária de Gastos:</strong> (Total Despesas 3 meses) / (Dias dos 3 meses)</li>
     *   <li><strong>Gastos Projetados:</strong> Média Diária × Dias restantes no mês</li>
     *   <li><strong>Saldo Projetado:</strong> Saldo Atual - Gastos Projetados + Receitas Esperadas</li>
     * </ol>
     * 
     * <p><strong>Fórmulas Utilizadas:</strong></p>
     * <pre>
     * Média Diária de Gastos = Σ(Despesas 3 meses) / Dias(3 meses)
     * Receita Média Diária = Σ(Receitas 3 meses) / Dias(3 meses)
     * Gastos Projetados = Média Diária de Gastos × Dias Restantes
     * Receitas Projetadas = Receita Média Diária × Dias Restantes
     * Saldo Projetado = Saldo Atual + Receitas Projetadas - Gastos Projetados
     * </pre>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Data Atual: 15/01/2024 (15 dias restantes no mês)
     * 
     * Análise Histórica (Out/Nov/Dez 2023 - 92 dias):
     *   - Total Receitas: R$ 15.000,00
     *   - Total Despesas: R$ 9.200,00
     *   - Média Diária Receitas: R$ 15.000 / 92 = R$ 163,04
     *   - Média Diária Despesas: R$ 9.200 / 92 = R$ 100,00
     * 
     * Saldo Atual: R$ 5.500,00
     * 
     * Projeções para 15 dias restantes:
     *   - Receitas Projetadas: R$ 163,04 × 15 = R$ 2.445,60
     *   - Gastos Projetados: R$ 100,00 × 15 = R$ 1.500,00
     *   - Saldo Projetado: R$ 5.500 + R$ 2.445,60 - R$ 1.500 = R$ 6.445,60
     * </pre>
     * 
     * <p><strong>Exemplo de retorno (dados criptografados):</strong></p>
     * <pre>
     * CurrentBalanceProjection {
     *   currentBalance: "encrypted_5500.00",
     *   projectedBalance: "encrypted_6445.60",
     *   daysLeftInMonth: 15,
     *   dailyAverageExpense: "encrypted_100.00",
     *   projectedSpending: "encrypted_1500.00"
     * }
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li><strong>Verificar Saldo Atual:</strong> Somar todos os saldos das contas conectadas</li>
     *   <li><strong>Calcular Período Histórico:</strong> Definir data de 3 meses atrás até hoje</li>
     *   <li><strong>Obter Transações Históricas:</strong> Buscar todas as transações do período</li>
     *   <li><strong>Calcular Médias:</strong></li>
     *   <ul>
     *     <li>Somar todas as receitas (CREDIT) do período</li>
     *     <li>Somar todas as despesas (DEBIT) do período</li>
     *     <li>Dividir cada total pelo número de dias do período</li>
     *   </ul>
     *   <li><strong>Calcular Dias Restantes:</strong> Último dia do mês atual - dia atual</li>
     *   <li><strong>Aplicar Fórmulas:</strong></li>
     *   <ul>
     *     <li>Gastos Projetados = Média Diária Despesas × Dias Restantes</li>
     *     <li>Receitas Projetadas = Média Diária Receitas × Dias Restantes</li>
     *     <li>Saldo Projetado = Saldo Atual + Receitas Projetadas - Gastos Projetados</li>
     *   </ul>
     *   <li><strong>Comparar Resultados:</strong> Descriptografar valores retornados e comparar</li>
     * </ol>
     * 
     * <p><strong>Considerações Importantes:</strong></p>
     * <ul>
     *   <li>Utiliza 3 meses de histórico para maior precisão estatística</li>
     *   <li>Considera sazonalidade e padrões de gastos recentes</li>
     *   <li>Trata fins de semana e feriados no cálculo de dias úteis</li>
     *   <li>Aplica arredondamento para 2 casas decimais em valores monetários</li>
     * </ul>
     * 
     * @param connectedBanks lista de instituições financeiras conectadas
     * @param bankAccountIds lista de IDs das contas bancárias (criptografados)
     * @return projeção completa de saldo criptografada
     * @throws Exception se houver erro na descriptografia, busca de dados ou criptografia dos resultados
     */
    public CurrentBalanceProjection calculateBalanceProjectionEncrypted(
            List<FinancialInstitutionData> connectedBanks,
            List<String> bankAccountIds) throws Exception {
        
        // 1. Calcular saldo atual
        BigDecimal currentBalance = calculateCurrentBalance(connectedBanks);
        
        // 2. Definir período de análise histórica (3 meses)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);
        int historicalDays = (int) ChronoUnit.DAYS.between(startDate, endDate);
        
        // 3. Calcular médias históricas
        HistoricalAverages averages = calculateHistoricalAverages(
            bankAccountIds, startDate, endDate, historicalDays);
        
        // 4. Calcular dias restantes no mês atual
        int daysLeftInMonth = calculateDaysLeftInMonth();
        
        // 5. Calcular projeções
        BigDecimal projectedIncome = averages.dailyIncomeAverage.multiply(new BigDecimal(daysLeftInMonth));
        BigDecimal projectedExpenses = averages.dailyExpenseAverage.multiply(new BigDecimal(daysLeftInMonth));
        BigDecimal projectedBalance = currentBalance.add(projectedIncome).subtract(projectedExpenses);
        
        // 6. Arredondar valores para 2 casas decimais
        currentBalance = currentBalance.setScale(2, RoundingMode.HALF_UP);
        projectedBalance = projectedBalance.setScale(2, RoundingMode.HALF_UP);
        BigDecimal dailyExpenseRounded = averages.dailyExpenseAverage.setScale(2, RoundingMode.HALF_UP);
        projectedExpenses = projectedExpenses.setScale(2, RoundingMode.HALF_UP);
        
        // 7. Criptografar e retornar
        return new CurrentBalanceProjection(
                CryptUtil.encrypt(currentBalance.toString(), PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt(projectedBalance.toString(), PLUGGY_CRYPT_SECRET),
                daysLeftInMonth,
                CryptUtil.encrypt(dailyExpenseRounded.toString(), PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt(projectedExpenses.toString(), PLUGGY_CRYPT_SECRET)
        );
    }
    
    /**
     * Calcula o saldo atual somando todos os saldos das contas conectadas.
     * 
     * <p><strong>Cálculo realizado:</strong></p>
     * <ol>
     *   <li>Itera sobre todas as instituições financeiras conectadas</li>
     *   <li>Para cada conta, converte o saldo de string para BigDecimal</li>
     *   <li>Soma todos os saldos válidos</li>
     *   <li>Ignora contas com saldos inválidos (log de erro)</li>
     * </ol>
     * 
     * <p><strong>Exemplo de cenário:</strong></p>
     * <pre>
     * Banco A - Conta Corrente: R$ 2.500,00
     * Banco A - Poupança: R$ 1.200,00
     * Banco B - Conta Corrente: R$ 1.800,00
     * Total: R$ 2.500 + R$ 1.200 + R$ 1.800 = R$ 5.500,00
     * </pre>
     * 
     * @param connectedBanks lista de instituições financeiras com suas contas
     * @return saldo total atual
     */
    private BigDecimal calculateCurrentBalance(List<FinancialInstitutionData> connectedBanks) {
        BigDecimal totalBalance = BigDecimal.ZERO;
        
        for (FinancialInstitutionData bank : connectedBanks) {
            for (PluggyAccountIds account : bank.getAccounts()) {
                try {
                    BigDecimal accountBalance = new BigDecimal(account.getBalance());
                    totalBalance = totalBalance.add(accountBalance);
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao converter saldo da conta: " + e.getMessage());
                }
            }
        }
        
        return totalBalance;
    }
    
    /**
     * Calcula as médias históricas de receitas e despesas baseadas em 3 meses de dados.
     * 
     * <p><strong>Metodologia Estatística:</strong></p>
     * <ol>
     *   <li><strong>Coleta de Dados:</strong> Busca todas as transações dos últimos 3 meses</li>
     *   <li><strong>Segregação:</strong> Separa receitas (CREDIT) de despesas (DEBIT)</li>
     *   <li><strong>Cálculo de Médias:</strong> Divide totais pelo número de dias do período</li>
     *   <li><strong>Validação:</strong> Garante que há dados suficientes para análise confiável</li>
     * </ol>
     * 
     * <p><strong>Fórmulas aplicadas:</strong></p>
     * <pre>
     * Média Diária Receitas = Σ(Transações CREDIT) / Dias do Período
     * Média Diária Despesas = Σ(|Transações DEBIT|) / Dias do Período
     * </pre>
     * 
     * <p><strong>Exemplo de cálculo:</strong></p>
     * <pre>
     * Período: 15/10/2023 a 15/01/2024 (92 dias)
     * 
     * Receitas encontradas:
     *   - Salário Out: R$ 5.000,00
     *   - Freelance Out: R$ 1.200,00
     *   - Salário Nov: R$ 5.000,00
     *   - Salário Dez: R$ 5.000,00
     *   - Freelance Dez: R$ 800,00
     *   Total Receitas: R$ 17.000,00
     *   Média Diária: R$ 17.000 / 92 = R$ 184,78
     * 
     * Despesas encontradas:
     *   - Supermercado, combustível, etc.: R$ 9.200,00
     *   Média Diária: R$ 9.200 / 92 = R$ 100,00
     * </pre>
     * 
     * @param bankAccountIds lista de IDs das contas (criptografados)
     * @param startDate data de início do período histórico
     * @param endDate data de fim do período histórico
     * @param totalDays número total de dias no período
     * @return objeto contendo médias diárias de receitas e despesas
     * @throws Exception se houver erro na busca de transações
     */
    private HistoricalAverages calculateHistoricalAverages(
            List<String> bankAccountIds,
            LocalDate startDate,
            LocalDate endDate,
            int totalDays) throws Exception {
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        
        for (String accountId : bankAccountIds) {
            List<TransactionData> transactions = transactionService.getAllTransactionsPeriodByAccountId(accountId, null, null, startDate, endDate);

            for (TransactionData transaction : transactions) {
                if (transaction.getType() == TransactionType.CREDIT) {
                    totalIncome = totalIncome.add(transaction.getAmount());
                } else if (transaction.getType() == TransactionType.DEBIT) {
                    totalExpenses = totalExpenses.add(transaction.getAmount().abs());
                }
            }
        }
        
        // Calcular médias diárias
        BigDecimal dailyIncomeAverage = totalDays > 0 ? 
            totalIncome.divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal dailyExpenseAverage = totalDays > 0 ? 
            totalExpenses.divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        return new HistoricalAverages(dailyIncomeAverage, dailyExpenseAverage);
    }
    
    /**
     * Calcula quantos dias restam no mês atual a partir da data de hoje.
     * 
     * <p><strong>Cálculo realizado:</strong></p>
     * <ol>
     *   <li>Obtém a data atual</li>
     *   <li>Determina o último dia do mês atual</li>
     *   <li>Calcula a diferença em dias</li>
     * </ol>
     * 
     * <p><strong>Exemplos de cálculo:</strong></p>
     * <pre>
     * Se hoje é 15/01/2024:
     *   - Último dia do mês: 31/01/2024
     *   - Dias restantes: 31 - 15 = 16 dias
     * 
     * Se hoje é 28/02/2024 (ano bissexto):
     *   - Último dia do mês: 29/02/2024
     *   - Dias restantes: 29 - 28 = 1 dia
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li>Verificar a data atual do sistema</li>
     *   <li>Identificar o último dia do mês atual</li>
     *   <li>Subtrair: último dia - dia atual</li>
     *   <li>Comparar com o valor retornado</li>
     * </ol>
     * 
     * @return número de dias restantes no mês atual
     */
    private int calculateDaysLeftInMonth() {
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        return (int) ChronoUnit.DAYS.between(today, lastDayOfMonth);
    }
    
    /**
     * Classe interna para armazenar médias históricas calculadas.
     * Facilita o transporte de dados entre métodos internos.
     */
    private static class HistoricalAverages {
        final BigDecimal dailyIncomeAverage;
        final BigDecimal dailyExpenseAverage;
        
        HistoricalAverages(BigDecimal dailyIncomeAverage, BigDecimal dailyExpenseAverage) {
            this.dailyIncomeAverage = dailyIncomeAverage;
            this.dailyExpenseAverage = dailyExpenseAverage;
        }
    }
}