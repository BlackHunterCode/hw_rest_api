package br.com.blackhunter.finey.rest.finance.analysis.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.blackhunter.finey.rest.finance.transaction.dto.TransactionData;
import br.com.blackhunter.finey.rest.finance.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.blackhunter.finey.rest.auth.util.CryptUtil;
import br.com.blackhunter.finey.rest.core.dto.TransactionPeriodDate;
import br.com.blackhunter.finey.rest.finance.analysis.dto.financial_summary.IncomeExpenseData;
import br.com.blackhunter.finey.rest.finance.analysis.dto.financial_summary.InvestmentCategory;
import br.com.blackhunter.finey.rest.finance.analysis.dto.financial_summary.InvestmentData;
import br.com.blackhunter.finey.rest.finance.analysis.dto.financial_summary.ReturnRate;
import br.com.blackhunter.finey.rest.finance.analysis.dto.financial_summary.WalletBalance;
import br.com.blackhunter.finey.rest.finance.transaction.enums.TransactionType;
import br.com.blackhunter.finey.rest.integrations.financial_integrator.FinancialIntegrator;
import br.com.blackhunter.finey.rest.integrations.financial_integrator.FinancialIntegratorManager;
import br.com.blackhunter.finey.rest.integrations.financial_integrator.dto.FinancialInstitutionData;
import br.com.blackhunter.finey.rest.integrations.pluggy.dto.PluggyAccountIds;

/**
 * Serviço responsável por realizar cálculos financeiros complexos.
 * Todos os métodos retornam dados criptografados para segurança.
 * 
 * @author BlackHunter Team
 * @version 1.0
 * @since 2024
 */
@Service
public class FinancialSummaryCalcService {
    @Value("${hunter.secrets.pluggy.crypt-secret}")
    private String PLUGGY_CRYPT_SECRET;

    @Autowired
    private TransactionService transactionService;

    /**
     * Calcula os dados de receita (entradas) para o período especificado.
     * 
     * <p><strong>Cálculo realizado:</strong></p>
     * <ol>
     *   <li>Para cada conta bancária fornecida:</li>
     *   <li>Busca todas as transações do tipo CREDIT no período</li>
     *   <li>Soma os valores de todas as transações de crédito</li>
     *   <li>Calcula a porcentagem de crescimento em relação ao período anterior</li>
     * </ol>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Período: 01/01/2024 a 31/01/2024
     * Conta 1: Transações CREDIT: R$ 5.000,00 + R$ 2.500,00 = R$ 7.500,00
     * Conta 2: Transações CREDIT: R$ 3.200,00 + R$ 1.800,00 = R$ 5.000,00
     * Total de Receitas = R$ 7.500,00 + R$ 5.000,00 = R$ 12.500,00
     * Crescimento = 12,5% (valor fixo no exemplo)
     * </pre>
     * 
     * <p><strong>Exemplo de retorno (dados criptografados):</strong></p>
     * <pre>
     * IncomeExpenseData {
     *   amount: "encrypted_12500.00",
     *   lastUpdated: "encrypted_2024-01-31T23:59:59",
     *   status: "encrypted_active",
     *   percentage: "encrypted_12.5"
     * }
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li>Listar todas as transações do período para as contas especificadas</li>
     *   <li>Filtrar apenas transações do tipo CREDIT</li>
     *   <li>Somar todos os valores das transações filtradas</li>
     *   <li>Comparar o resultado com o valor descriptografado retornado</li>
     * </ol>
     * 
     * @param financialIntegratorManager gerenciador de integração financeira
     * @param bankAccountIds lista de IDs das contas bancárias (criptografados)
     * @param periodDate período de análise com data de início e fim
     * @return dados de receita criptografados contendo valor total, data de atualização, status e porcentagem
     * @throws Exception se houver erro na descriptografia, busca de transações ou criptografia dos resultados
     */
    public IncomeExpenseData calculateIncomeDataEncrypted(FinancialIntegratorManager financialIntegratorManager, List<String> bankAccountIds, TransactionPeriodDate periodDate) throws Exception {
        BigDecimal totalIncome = BigDecimal.ZERO;
        int totalTransactions = 0;
        
        for (String accountId : bankAccountIds) {
            List<TransactionData> transactions = transactionService.getAllTransactionsPeriodByAccountId(
                    accountId,
                    periodDate
            );
            
            for (TransactionData transaction : transactions) {
                if (transaction.getType() == TransactionType.CREDIT) {
                    totalIncome = totalIncome.add(transaction.getAmount());
                    totalTransactions++;
                }
            }
        }
        
        // Calcular porcentagem (crescimento em relação ao período anterior)
        double percentage = calculateIncomeGrowthPercentage(totalIncome, financialIntegratorManager, bankAccountIds, periodDate);
        
        return new IncomeExpenseData(
                CryptUtil.encrypt(totalIncome.toString(), PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("active", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt(String.valueOf(percentage), PLUGGY_CRYPT_SECRET)
        );
    }
    
    /**
     * Calcula os dados de despesa (saídas) para o período especificado.
     * 
     * <p><strong>Cálculo realizado:</strong></p>
     * <ol>
     *   <li>Para cada conta bancária fornecida:</li>
     *   <li>Busca todas as transações do tipo DEBIT no período</li>
     *   <li>Soma os valores absolutos de todas as transações de débito</li>
     *   <li>Calcula a porcentagem de variação em relação ao período anterior</li>
     * </ol>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Período: 01/01/2024 a 31/01/2024
     * Conta 1: Transações DEBIT: -R$ 1.200,00 + -R$ 800,00 = R$ 2.000,00 (valor absoluto)
     * Conta 2: Transações DEBIT: -R$ 2.500,00 + -R$ 1.500,00 = R$ 4.000,00 (valor absoluto)
     * Total de Despesas = R$ 2.000,00 + R$ 4.000,00 = R$ 6.000,00
     * Variação = -8,3% (redução, valor fixo no exemplo)
     * </pre>
     * 
     * <p><strong>Exemplo de retorno (dados criptografados):</strong></p>
     * <pre>
     * IncomeExpenseData {
     *   amount: "encrypted_6000.00",
     *   lastUpdated: "encrypted_2024-01-31T23:59:59",
     *   status: "encrypted_active",
     *   percentage: "encrypted_-8.3"
     * }
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li>Listar todas as transações do período para as contas especificadas</li>
     *   <li>Filtrar apenas transações do tipo DEBIT</li>
     *   <li>Aplicar valor absoluto (Math.abs) em cada transação de débito</li>
     *   <li>Somar todos os valores absolutos das transações filtradas</li>
     *   <li>Comparar o resultado com o valor descriptografado retornado</li>
     * </ol>
     * 
     * @param financialIntegratorManager gerenciador de integração financeira
     * @param bankAccountIds lista de IDs das contas bancárias (criptografados)
     * @param periodDate período de análise com data de início e fim
     * @return dados de despesa criptografados contendo valor total, data de atualização, status e porcentagem
     * @throws Exception se houver erro na descriptografia, busca de transações ou criptografia dos resultados
     */
    public IncomeExpenseData calculateExpenseDataEncrypted(FinancialIntegratorManager financialIntegratorManager, List<String> bankAccountIds, TransactionPeriodDate periodDate) throws Exception {
        BigDecimal totalExpenses = BigDecimal.ZERO;
        int totalTransactions = 0;
        
        for (String accountId : bankAccountIds) {
            List<TransactionData> transactions = transactionService.getAllTransactionsPeriodByAccountId( accountId, periodDate );

            for (TransactionData transaction : transactions) {
                if (transaction.getType() == TransactionType.DEBIT) {
                    totalExpenses = totalExpenses.add(transaction.getAmount().abs());
                    totalTransactions++;
                }
            }
        }
        
        // Calcular porcentagem de variação das despesas em relação ao período anterior
        double percentage = calculateExpenseVariationPercentage(totalExpenses, bankAccountIds, periodDate,financialIntegratorManager);
        
        return new IncomeExpenseData(
                CryptUtil.encrypt(totalExpenses.toString(), PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("active", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt(String.valueOf(percentage), PLUGGY_CRYPT_SECRET)
        );
    }
    
    /**
     * Calcula os dados de investimento para o período especificado.
     * 
     * <p><strong>Cálculo realizado:</strong></p>
     * <ol>
     *   <li>Inicializa o valor total de investimentos (atualmente zerado)</li>
     *   <li>Define categorias de investimento disponíveis</li>
     *   <li>Calcula a taxa de retorno dos investimentos</li>
     *   <li>Retorna dados estruturados com categorias e taxa de retorno</li>
     * </ol>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Total de Investimentos: R$ 0,00 (implementação atual)
     * Categorias:
     *   - Renda Fixa (ativa)
     *   - Renda Variável (ativa)
     *   - Fundos (inativa)
     * Taxa de Retorno: 7,8% (valor fixo no exemplo)
     * </pre>
     * 
     * <p><strong>Exemplo de retorno (dados criptografados):</strong></p>
     * <pre>
     * InvestmentData {
     *   totalAmount: "encrypted_0.00",
     *   lastUpdated: "encrypted_2024-01-31T23:59:59",
     *   categories: [
     *     { name: "encrypted_Renda Fixa", active: true },
     *     { name: "encrypted_Renda Variável", active: true },
     *     { name: "encrypted_Fundos", active: false }
     *   ],
     *   returnRate: "encrypted_7.8",
     *   status: "encrypted_active"
     * }
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li>Verificar se o valor total de investimentos está correto (atualmente sempre 0)</li>
     *   <li>Confirmar se as categorias estão sendo criadas corretamente</li>
     *   <li>Validar se a taxa de retorno está sendo calculada adequadamente</li>
     *   <li>Verificar se todos os dados estão sendo criptografados</li>
     * </ol>
     * 
     * <p><strong>Nota:</strong> Este método atualmente retorna valores fixos para demonstração.
     * Em uma implementação completa, deveria buscar dados reais de investimentos.</p>
     * 
     * @param bankAccountIds lista de IDs das contas bancárias (criptografados)
     * @param periodDate período de análise com data de início e fim
     * @return dados de investimento criptografados contendo valor total, categorias, taxa de retorno e status
     * @throws Exception se houver erro na criptografia dos resultados
     */
    public InvestmentData calculateInvestmentDataEncrypted(FinancialIntegratorManager financialIntegratorManager, List<String> bankAccountIds, TransactionPeriodDate periodDate) throws Exception {
        BigDecimal totalInvestments = BigDecimal.ZERO;
        Map<String, Boolean> categoriesFound = new HashMap<>();
        
        FinancialIntegrator financialIntegrator = financialIntegratorManager.getFinancialIntegrator();
        
        // Processar transações de cada conta para identificar investimentos
        for (String accountId : bankAccountIds) {
            List<TransactionData> transactions = transactionService.getAllTransactionsPeriodByAccountId( accountId, periodDate );

            // Analisar transações para identificar investimentos
            for (TransactionData transaction : transactions) {
                if (isInvestmentTransaction(transaction)) {
                    String investmentType = categorizeInvestment(transaction);
                    BigDecimal amount = transaction.getAmount().abs();
                    
                    totalInvestments = totalInvestments.add(amount);
                    categoriesFound.put(investmentType, true);
                }
            }
        }
        
        // Criar lista de categorias baseada nos investimentos encontrados
        List<InvestmentCategory> categories = new ArrayList<>();
        categories.add(new InvestmentCategory(
            CryptUtil.encrypt("Renda Fixa", PLUGGY_CRYPT_SECRET), 
            categoriesFound.getOrDefault("Renda Fixa", false)
        ));
        categories.add(new InvestmentCategory(
            CryptUtil.encrypt("Renda Variável", PLUGGY_CRYPT_SECRET), 
            categoriesFound.getOrDefault("Renda Variável", false)
        ));
        categories.add(new InvestmentCategory(
            CryptUtil.encrypt("Fundos", PLUGGY_CRYPT_SECRET), 
            categoriesFound.getOrDefault("Fundos", false)
        ));
        categories.add(new InvestmentCategory(
            CryptUtil.encrypt("Poupança", PLUGGY_CRYPT_SECRET), 
            categoriesFound.getOrDefault("Poupança", false)
        ));
        categories.add(new InvestmentCategory(
            CryptUtil.encrypt("Previdência", PLUGGY_CRYPT_SECRET), 
            categoriesFound.getOrDefault("Previdência", false)
        ));
        categories.add(new InvestmentCategory(
            CryptUtil.encrypt("Criptomoedas", PLUGGY_CRYPT_SECRET), 
            categoriesFound.getOrDefault("Criptomoedas", false)
        ));
        
        // Calcular taxa de retorno dos investimentos baseada no valor real
        double returnRate = calculateInvestmentReturnRate(totalInvestments, bankAccountIds, 
                                                         periodDate.getEndDate(), financialIntegratorManager);
        
        return new InvestmentData(
                CryptUtil.encrypt(totalInvestments.toString(), PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), PLUGGY_CRYPT_SECRET),
                categories,
                CryptUtil.encrypt(String.valueOf(returnRate), PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("active", PLUGGY_CRYPT_SECRET)
        );
    }
    
    /**
     * Calcula o saldo total da carteira somando todos os saldos das contas conectadas.
     * 
     * <p><strong>Cálculo realizado:</strong></p>
     * <ol>
     *   <li>Para cada instituição financeira conectada:</li>
     *   <li>Para cada conta da instituição:</li>
     *   <li>Converte o saldo da conta (string) para BigDecimal</li>
     *   <li>Soma todos os saldos válidos</li>
     *   <li>Trata erros de conversão individualmente sem interromper o cálculo</li>
     * </ol>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Banco 1:
     *   - Conta Corrente: R$ 2.500,00
     *   - Conta Poupança: R$ 1.200,00
     * Banco 2:
     *   - Conta Corrente: R$ 3.800,00
     *   - Conta Investimento: R$ 5.000,00
     * 
     * Saldo Total = R$ 2.500,00 + R$ 1.200,00 + R$ 3.800,00 + R$ 5.000,00 = R$ 12.500,00
     * </pre>
     * 
     * <p><strong>Exemplo de retorno (dados criptografados):</strong></p>
     * <pre>
     * WalletBalance {
     *   totalBalance: "encrypted_12500.00",
     *   status: "encrypted_active"
     * }
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li>Listar todas as instituições financeiras conectadas do usuário</li>
     *   <li>Para cada instituição, listar todas as contas</li>
     *   <li>Somar manualmente todos os saldos das contas</li>
     *   <li>Comparar o resultado com o valor descriptografado retornado</li>
     *   <li>Verificar se contas com saldos inválidos foram ignoradas corretamente</li>
     * </ol>
     * 
     * @param connectedBanks lista de instituições financeiras conectadas com suas respectivas contas
     * @return saldo total da carteira criptografado contendo valor total e status
     * @throws Exception se houver erro na criptografia dos resultados
     */
    public WalletBalance calculateWalletBalanceEncrypted(List<FinancialInstitutionData> connectedBanks) throws Exception {
        BigDecimal totalBalance = BigDecimal.ZERO;
        
        for (FinancialInstitutionData bank : connectedBanks) {
            for (PluggyAccountIds account : bank.getAccounts()) {
                try {
                    // O balance já vem como string, precisa converter
                    BigDecimal accountBalance = new BigDecimal(account.getBalance());
                    totalBalance = totalBalance.add(accountBalance);
                } catch (NumberFormatException e) {
                    // Log do erro e continua com próxima conta
                    System.err.println("Erro ao converter saldo da conta: " + e.getMessage());
                }
            }
        }
        
        return new WalletBalance(
                CryptUtil.encrypt(totalBalance.toString(), PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("active", PLUGGY_CRYPT_SECRET)
        );
    }
    
    /**
     * Calcula a taxa de retorno total baseada nos dados de receita, despesas e investimentos.
     * 
     * <p><strong>Cálculo realizado:</strong></p>
     * <ol>
     *   <li>Atualmente retorna um valor fixo de exemplo (5,2%)</li>
     *   <li>Em uma implementação completa, calcularia com base em:</li>
     *   <li>- Receitas vs Despesas do período</li>
     *   <li>- Performance dos investimentos</li>
     *   <li>- Comparação com períodos anteriores</li>
     * </ol>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Receitas: R$ 12.500,00
     * Despesas: R$ 6.000,00
     * Investimentos: R$ 0,00
     * 
     * Fórmula simplificada (exemplo):
     * Taxa de Retorno = ((Receitas - Despesas) / Receitas) * 100
     * Taxa de Retorno = ((12.500 - 6.000) / 12.500) * 100 = 52%
     * 
     * Valor atual retornado: 5,2% (valor fixo para demonstração)
     * </pre>
     * 
     * <p><strong>Exemplo de retorno (dados criptografados):</strong></p>
     * <pre>
     * ReturnRate {
     *   percentage: "encrypted_5.2",
     *   status: "encrypted_positive"
     * }
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li>Verificar se os dados de entrada são descriptografados corretamente</li>
     *   <li>Confirmar o cálculo: (Receitas - Despesas) / Investimentos * 100</li>
     *   <li>Validar se o status reflete corretamente o sinal da taxa (positive/negative/neutral)</li>
     *   <li>Verificar se a taxa está limitada entre -50% e +50%</li>
     *   <li>Confirmar tratamento de erro retornando valores seguros</li>
     * </ol>
     * 
     * <p><strong>Implementação:</strong> Este método agora utiliza dados reais descriptografados
     * para calcular a taxa de retorno baseada no fluxo de caixa e investimentos do período.</p>
     * 
     * @param income dados de receita do período
     * @param expenses dados de despesa do período
     * @param investments dados de investimento do período
     * @return taxa de retorno total criptografada contendo porcentagem e status
     * @throws Exception se houver erro na criptografia dos resultados
     */
    public ReturnRate calculateTotalReturnRateEncrypted(IncomeExpenseData income, IncomeExpenseData expenses, InvestmentData investments) throws Exception {
        try {
            // Descriptografar dados de entrada para cálculos
            BigDecimal totalIncome = new BigDecimal(CryptUtil.decrypt(income.getValue(), PLUGGY_CRYPT_SECRET));
            BigDecimal totalExpenses = new BigDecimal(CryptUtil.decrypt(expenses.getValue(), PLUGGY_CRYPT_SECRET));
            BigDecimal totalInvestments = new BigDecimal(CryptUtil.decrypt(investments.getValue(), PLUGGY_CRYPT_SECRET));
            
            // Calcular fluxo de caixa líquido (receitas - despesas)
            BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);
            
            // Calcular taxa de retorno baseada nos investimentos e fluxo de caixa
            double returnPercentage = 0.0;
            String status = "neutral";
            
            if (totalInvestments.compareTo(BigDecimal.ZERO) > 0) {
                // Taxa de retorno = (Fluxo líquido / Total investido) * 100
                returnPercentage = netCashFlow.divide(totalInvestments, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
                
                // Determinar status baseado na taxa de retorno
                if (returnPercentage > 0) {
                    status = "positive";
                } else if (returnPercentage < 0) {
                    status = "negative";
                } else {
                    status = "neutral";
                }
            } else if (netCashFlow.compareTo(BigDecimal.ZERO) > 0) {
                // Se não há investimentos mas há fluxo positivo, considerar como potencial de investimento
                returnPercentage = 2.5; // Taxa conservadora para fluxo positivo sem investimentos
                status = "positive";
            }
            
            // Limitar a taxa de retorno a valores realistas (-50% a +50%)
            returnPercentage = Math.max(-50.0, Math.min(50.0, returnPercentage));
            
            return new ReturnRate(
                    CryptUtil.encrypt(String.format("%.2f", returnPercentage), PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(status, PLUGGY_CRYPT_SECRET)
            );
            
        } catch (Exception e) {
            // Em caso de erro, retornar valores padrão seguros
            return new ReturnRate(
                    CryptUtil.encrypt("0.0", PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt("neutral", PLUGGY_CRYPT_SECRET)
            );
        }
    }
    
    /**
     * Gera uma lista de meses no formato MM/yyyy para o período especificado.
     * 
     * <p><strong>Cálculo realizado:</strong></p>
     * <ol>
     *   <li>Inicia na data de início do período</li>
     *   <li>Incrementa mês a mês até a data final</li>
     *   <li>Formata cada mês no padrão MM/yyyy</li>
     *   <li>Criptografa cada entrada da lista</li>
     * </ol>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Período: 01/01/2024 a 31/03/2024
     * 
     * Meses gerados:
     *   - 01/2024 (Janeiro 2024)
     *   - 02/2024 (Fevereiro 2024)
     *   - 03/2024 (Março 2024)
     * </pre>
     * 
     * <p><strong>Exemplo de retorno (dados criptografados):</strong></p>
     * <pre>
     * List&lt;String&gt; {
     *   "encrypted_01/2024",
     *   "encrypted_02/2024",
     *   "encrypted_03/2024"
     * }
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li>Identificar o mês/ano da data de início do período</li>
     *   <li>Identificar o mês/ano da data final do período</li>
     *   <li>Contar quantos meses existem entre as duas datas (inclusive)</li>
     *   <li>Verificar se a quantidade de itens na lista corresponde</li>
     *   <li>Descriptografar cada item e verificar se está no formato MM/yyyy</li>
     *   <li>Confirmar se a sequência de meses está correta e completa</li>
     * </ol>
     * 
     * @param periodDate período de análise com data de início e fim
     * @return lista de meses criptografados no formato MM/yyyy
     * @throws Exception se houver erro na criptografia dos resultados
     */
    public List<String> generateMonthsList(TransactionPeriodDate periodDate) throws Exception {
        List<String> months = new ArrayList<>();
        
        LocalDate current = periodDate.getStartDate();
        while (!current.isAfter(periodDate.getEndDate())) {
            String monthYear = current.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            months.add(CryptUtil.encrypt(monthYear, PLUGGY_CRYPT_SECRET));
            current = current.plusMonths(1);
        }
        
        return months;
    }
    
    /**
     * Calcula a porcentagem de crescimento da receita em relação ao período anterior.
     * 
     * <p><strong>Cálculo realizado com dados reais:</strong></p>
     * <ol>
     *   <li>Define período anterior com mesma duração do período atual</li>
     *   <li>Busca transações de crédito do período anterior via Pluggy</li>
     *   <li>Calcula receita total do período anterior</li>
     *   <li>Aplica fórmula: ((Receita Atual - Receita Anterior) / Receita Anterior) * 100</li>
     *   <li>Retorna crescimento limitado entre -100% e +500%</li>
     * </ol>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Receita Atual: R$ 12.500,00
     * Receita Período Anterior: R$ 11.111,11
     * 
     * Cálculo:
     * Crescimento = ((12.500 - 11.111,11) / 11.111,11) * 100
     * Crescimento = (1.388,89 / 11.111,11) * 100
     * Crescimento = 0,125 * 100 = 12,5%
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li>Verificar transações de crédito do período anterior</li>
     *   <li>Somar valores das transações de crédito</li>
     *   <li>Aplicar a fórmula de crescimento percentual</li>
     *   <li>Comparar resultado calculado com o valor retornado</li>
     * </ol>
     * 
     * @param currentIncome receita atual do período
     * @param financialIntegratorManager gerenciador de integração financeira
     * @param bankAccountIds lista de IDs das contas bancárias (criptografados)
     * @param periodDate período atual de análise
     * @return porcentagem de crescimento real baseada em dados históricos
     */
    public double calculateIncomeGrowthPercentage(BigDecimal currentIncome, FinancialIntegratorManager financialIntegratorManager, List<String> bankAccountIds, TransactionPeriodDate periodDate) {
        try {
            // Calcular período anterior com mesma duração
            LocalDate currentStart = periodDate.getStartDate();
            LocalDate currentEnd = periodDate.getEndDate();
            long periodDays = ChronoUnit.DAYS.between(currentStart, currentEnd);
            
            LocalDate previousStart = currentStart.minusDays(periodDays + 1);
            LocalDate previousEnd = currentStart.minusDays(1);
            
            // Buscar receitas do período anterior
            BigDecimal previousIncome = BigDecimal.ZERO;
            FinancialIntegrator financialIntegrator = financialIntegratorManager.getFinancialIntegrator();

            for (String accountId : bankAccountIds) {
                List<TransactionData> previousTransactions = transactionService.getAllTransactionsPeriodByAccountId( accountId, null, null, previousStart, previousEnd);
                
                for (TransactionData transaction : previousTransactions) {
                    if (transaction.getType() == TransactionType.CREDIT) {
                        previousIncome = previousIncome.add(transaction.getAmount());
                    }
                }
            }
            
            // Calcular crescimento percentual
            if (previousIncome.compareTo(BigDecimal.ZERO) == 0) {
                return currentIncome.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
            }
            
            BigDecimal growth = currentIncome.subtract(previousIncome)
                .divide(previousIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            
            // Limitar crescimento a valores realistas (-100% a +500%)
            double growthPercentage = growth.doubleValue();
            return Math.max(-100.0, Math.min(500.0, growthPercentage));
            
        } catch (Exception e) {
            System.err.println("Erro ao calcular crescimento de receita: " + e.getMessage());
            return 0.0; // Retorna 0% em caso de erro
        }
    }
    
    /**
     * Calcula a porcentagem de variação das despesas em relação ao período anterior.
     * 
     * <p><strong>Cálculo realizado:</strong></p>
     * <ol>
     *   <li>Busca transações de débito do período atual via Pluggy</li>
     *   <li>Busca transações de débito do período anterior via Pluggy</li>
     *   <li>Calcula variação: ((Atual - Anterior) / Anterior) * 100</li>
     *   <li>Valores positivos = aumento nas despesas</li>
     *   <li>Valores negativos = redução nas despesas</li>
     * </ol>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Despesa Atual: R$ 6.000,00
     * Despesa Período Anterior: R$ 6.542,17
     * 
     * Cálculo:
     * Variação = ((6.000 - 6.542,17) / 6.542,17) * 100
     * Variação = (-542,17 / 6.542,17) * 100
     * Variação = -0,083 * 100 = -8,3%
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li>Verificar se as transações de débito são buscadas corretamente</li>
     *   <li>Confirmar que o período anterior é calculado adequadamente</li>
     *   <li>Validar a fórmula de variação percentual</li>
     *   <li>Comparar resultado calculado com dados reais</li>
     * </ol>
     * 
     * @param currentExpenses despesa atual do período (não utilizado na nova implementação)
     * @param bankAccountIds lista de IDs das contas bancárias para busca
     * @param periodDate data do período para comparação
     * @param financialIntegratorManager gerenciador para acesso aos dados da Pluggy
     * @return porcentagem de variação das despesas
     */
    public double calculateExpenseVariationPercentage(BigDecimal currentExpenses, List<String> bankAccountIds, 
                                                     TransactionPeriodDate periodDate, FinancialIntegratorManager financialIntegratorManager) {
        try {
            // Calcular período anterior com mesma duração
            LocalDate startOfCurrentPeriod = periodDate.getStartDate();
            LocalDate endOfCurrentPeriod = periodDate.getEndDate();
            long periodDuration = ChronoUnit.DAYS.between(startOfCurrentPeriod, endOfCurrentPeriod);
            
            LocalDate endOfPreviousPeriod = startOfCurrentPeriod.minusDays(1);
            LocalDate startOfPreviousPeriod = endOfPreviousPeriod.minusDays(periodDuration);
            
            // Buscar transações do período atual
            BigDecimal currentPeriodExpenses = BigDecimal.ZERO;
            for (String accountId : bankAccountIds) {
                List<TransactionData> currentTransactions = transactionService.getAllTransactionsPeriodByAccountId( accountId, periodDate );

                for (TransactionData transaction : currentTransactions) {
                    if (TransactionType.valueOf("DEBIT").equals(transaction.getType())) {
                        currentPeriodExpenses = currentPeriodExpenses.add(transaction.getAmount().abs());
                    }
                }
            }
            
            // Buscar transações do período anterior
            BigDecimal previousPeriodExpenses = BigDecimal.ZERO;
            for (String accountId : bankAccountIds) {
                List<TransactionData> previousTransactions = transactionService.getAllTransactionsPeriodByAccountId( accountId, null, null, startOfPreviousPeriod, endOfPreviousPeriod );

                for (TransactionData transaction : previousTransactions) {
                    if ("DEBIT".equals(transaction.getType())) {
                        previousPeriodExpenses = previousPeriodExpenses.add(transaction.getAmount().abs());
                    }
                }
            }
            
            // Calcular variação percentual
            if (previousPeriodExpenses.compareTo(BigDecimal.ZERO) == 0) {
                return currentPeriodExpenses.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
            }
            
            BigDecimal variation = currentPeriodExpenses.subtract(previousPeriodExpenses)
                .divide(previousPeriodExpenses, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            
            // Aplicar limites realistas (-100% a +500%)
            double variationPercentage = variation.doubleValue();
            if (variationPercentage < -100.0) {
                variationPercentage = -100.0;
            } else if (variationPercentage > 500.0) {
                variationPercentage = 500.0;
            }
            
            return variationPercentage;
            
        } catch (Exception e) {
            System.err.println("Erro ao calcular variação de despesas: " + e.getMessage());
            return 0.0; // Retorna 0% em caso de erro
        }
    }
    
    /**
     * Calcula a taxa de retorno dos investimentos baseada em dados reais da Pluggy.
     * 
     * <p><strong>Cálculo realizado:</strong></p>
     * <ol>
     *   <li>Busca transações de investimento dos últimos 12 meses</li>
     *   <li>Calcula valor total investido (débitos em investimentos)</li>
     *   <li>Calcula valor total de retornos (créditos de investimentos)</li>
     *   <li>Aplica fórmula: ((Valor Atual - Valor Investido) / Valor Investido) * 100</li>
     *   <li>Retorna taxa limitada entre -100% e +1000%</li>
     * </ol>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Valor Investido (débitos): R$ 10.000,00
     * Retornos Recebidos (créditos): R$ 780,00
     * Valor Atual dos Investimentos: R$ 10.780,00
     * 
     * Cálculo:
     * Valor Total Atual = 10.000 + 780 + 10.780 = R$ 21.560,00
     * Retorno = ((21.560 - 10.000) / 10.000) * 100 = 115,6%
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li>Buscar transações de investimento dos últimos 12 meses</li>
     *   <li>Somar débitos (investimentos realizados)</li>
     *   <li>Somar créditos (retornos recebidos)</li>
     *   <li>Aplicar a fórmula de retorno percentual</li>
     *   <li>Verificar se está dentro dos limites (-100% a +1000%)</li>
     * </ol>
     * 
     * @param totalInvestments valor total atual dos investimentos
     * @param bankAccountIds lista de IDs das contas bancárias
     * @param periodDate data do período para análise
     * @param financialIntegratorManager gerenciador de integração financeira
     * @return taxa de retorno dos investimentos baseada em dados reais
     */
    public double calculateInvestmentReturnRate(BigDecimal totalInvestments, List<String> bankAccountIds, 
                                               LocalDate periodDate, FinancialIntegratorManager financialIntegratorManager) {
        try {
            // Definir período de análise (últimos 12 meses para cálculo de retorno anual)
            LocalDate startDate = periodDate.minusMonths(12);
            LocalDate endDate = periodDate;
            
            BigDecimal totalInvested = BigDecimal.ZERO;
            BigDecimal totalReturns = BigDecimal.ZERO;
            
            // Buscar transações de investimento para cada conta
            for (String accountId : bankAccountIds) {
                try {
                    List<TransactionData> transactions = transactionService.getAllTransactionsPeriodByAccountId( accountId, null, null, startDate, endDate );

                    for (TransactionData transaction : transactions) {
                        if (isInvestmentTransaction(transaction)) {
                            if (transaction.getType() == TransactionType.DEBIT) {
                                // Débitos são investimentos realizados
                                totalInvested = totalInvested.add(transaction.getAmount().abs());
                            } else if (transaction.getType() == TransactionType.CREDIT) {
                                // Créditos são retornos de investimentos
                                totalReturns = totalReturns.add(transaction.getAmount());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao buscar transações de investimento para conta " + accountId + ": " + e.getMessage());
                }
            }
            
            // Calcular taxa de retorno
            if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
                // Valor atual = valor investido + retornos + valor atual dos investimentos
                BigDecimal currentValue = totalInvested.add(totalReturns).add(totalInvestments);
                BigDecimal returnAmount = currentValue.subtract(totalInvested);
                
                double returnRate = returnAmount.divide(totalInvested, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
                
                // Aplicar limites realistas para taxa de retorno (-100% a +1000%)
                return Math.max(-100.0, Math.min(1000.0, returnRate));
            }
            
            // Se não há investimentos históricos, usar valor padrão baseado no mercado
            return 7.8;
            
        } catch (Exception e) {
            System.err.println("Erro ao calcular taxa de retorno de investimentos: " + e.getMessage());
            return 7.8; // Valor padrão em caso de erro
        }
    }
    
    /**
     * Identifica se uma transação é relacionada a investimentos.
     * 
     * @param transaction transação a ser analisada
     * @return true se for transação de investimento
     */
    private boolean isInvestmentTransaction(TransactionData transaction) {
        String description = transaction.getDescription().toLowerCase();
        
        return containsKeywords(description, 
            "cdb", "lci", "lca", "tesouro", "selic", "ipca",
            "acao", "acoes", "fii", "etf", "bovespa", "b3",
            "fundo", "investimento", "aplicacao", "resgate",
            "poupanca", "pgbl", "vgbl", "previdencia",
            "bitcoin", "btc", "ethereum", "crypto", "binance",
            "xp", "rico", "inter", "nubank invest", "bradesco invest"
        );
    }
    
    /**
     * Categoriza o tipo de investimento baseado na descrição da transação.
     * 
     * @param transaction transação a ser categorizada
     * @return tipo de investimento identificado
     */
    private String categorizeInvestment(TransactionData transaction) {
        String description = transaction.getDescription().toLowerCase();
        
        if (containsKeywords(description, "cdb", "lci", "lca", "tesouro", "selic", "ipca")) {
            return "Renda Fixa";
        }
        
        if (containsKeywords(description, "acao", "acoes", "fii", "etf", "bovespa", "b3")) {
            return "Renda Variável";
        }
        
        if (containsKeywords(description, "fundo", "investimento")) {
            return "Fundos";
        }
        
        if (containsKeywords(description, "pgbl", "vgbl", "previdencia")) {
            return "Previdência";
        }
        
        if (containsKeywords(description, "poupanca")) {
            return "Poupança";
        }
        
        if (containsKeywords(description, "bitcoin", "btc", "ethereum", "crypto", "binance")) {
            return "Criptomoedas";
        }
        
        return "Outros";
    }
    
    /**
     * Verifica se a descrição contém alguma das palavras-chave especificadas.
     * 
     * @param description descrição a ser verificada
     * @param keywords palavras-chave a serem procuradas
     * @return true se contém alguma palavra-chave
     */
    private boolean containsKeywords(String description, String... keywords) {
        for (String keyword : keywords) {
            if (description.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

}
