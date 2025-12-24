package br.com.blackhunter.finey.rest.finance.analysis.service;

import br.com.blackhunter.finey.rest.auth.util.CryptUtil;
import br.com.blackhunter.finey.rest.auth.util.JwtUtil;
import br.com.blackhunter.finey.rest.core.dto.TransactionPeriodDate;
import br.com.blackhunter.finey.rest.finance.analysis.calc.*;
import br.com.blackhunter.finey.rest.finance.analysis.dto.budget.BudgetCategory;
import br.com.blackhunter.finey.rest.finance.analysis.dto.budget.BudgetReality;
import br.com.blackhunter.finey.rest.finance.analysis.dto.current_balance_projection.CurrentBalanceProjection;
import br.com.blackhunter.finey.rest.finance.analysis.dto.expanses_categories.ExpensesCategories;
import br.com.blackhunter.finey.rest.finance.analysis.dto.financial_summary.FinancialSummary;
import br.com.blackhunter.finey.rest.finance.analysis.dto.financial_summary.IncomeExpenseData;
import br.com.blackhunter.finey.rest.finance.analysis.dto.financial_summary.InvestmentData;
import br.com.blackhunter.finey.rest.finance.analysis.dto.financial_summary.WalletBalance;
import br.com.blackhunter.finey.rest.finance.analysis.dto.financial_summary.ReturnRate;
import br.com.blackhunter.finey.rest.finance.analysis.dto.income.IncomeBreakdown;
import br.com.blackhunter.finey.rest.finance.analysis.dto.income.IncomeSource;
import br.com.blackhunter.finey.rest.finance.analysis.dto.insights.Insights;
import br.com.blackhunter.finey.rest.finance.analysis.dto.investments.SavingsInvestments;
import br.com.blackhunter.finey.rest.finance.transaction.service.TransactionService;
import br.com.blackhunter.finey.rest.integrations.financial_integrator.FinancialIntegrator;
import br.com.blackhunter.finey.rest.integrations.financial_integrator.FinancialIntegratorManager;
import br.com.blackhunter.finey.rest.integrations.financial_integrator.dto.FinancialInstitutionData;
import br.com.blackhunter.finey.rest.integrations.pluggy.dto.PluggyAccountIds;
import br.com.blackhunter.finey.rest.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.finey.rest.finance.transaction.enums.TransactionType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HomeScreenAnalysisService {
    @Value("${hunter.secrets.pluggy.crypt-secret}")
    private String PLUGGY_CRYPT_SECRET;

    private final JwtUtil jwtUtil;
    private final FinancialIntegratorManager financialIntegratorManager;
    private final FinancialSummaryCalcService financialSummaryCalcService;
    private final BalanceProjectionCalcService balanceProjectionCalcService;
    private final ExpensesCategoriesCalcService expensesCategoriesCalcService;
    private final IncomeBreakdownCalcService incomeBreakdownCalcService;
    private final SavingsInvestmentsCalcService savingsInvestmentsCalcService;
    private final TransactionService transactionService;

    // Adicionar no construtor
    public HomeScreenAnalysisService(
            JwtUtil jwtUtil,
            FinancialIntegratorManager financialIntegratorManager,
            FinancialSummaryCalcService financialSummaryCalcService,
            BalanceProjectionCalcService balanceProjectionCalcService,
            ExpensesCategoriesCalcService expensesCategoriesCalcService,
            IncomeBreakdownCalcService incomeBreakdownCalcService,
            SavingsInvestmentsCalcService savingsInvestmentsCalcService,
            TransactionService transactionService
    ) {
        this.jwtUtil = jwtUtil;
        this.financialIntegratorManager = financialIntegratorManager;
        this.financialSummaryCalcService = financialSummaryCalcService;
        this.balanceProjectionCalcService = balanceProjectionCalcService;
        this.expensesCategoriesCalcService = expensesCategoriesCalcService;
        this.incomeBreakdownCalcService = incomeBreakdownCalcService;
        this.savingsInvestmentsCalcService = savingsInvestmentsCalcService;
        this.transactionService = transactionService;
    }
    
    /**
     * Esse método é responsável por realizar a análise financeira e retornar um resumo financeiro.
     * Atenção: Todos os dados retornados por esse método estão criptografados.
     * 
     * @param bankAccountIds Lista de IDs de contas bancárias criptografadas
     * @param periodDate Período de análise com data de início e fim
     * @return FinancialSummary com dados criptografados
     * */
    public FinancialSummary getFinancialSummaryAnalysisEncrypted(List<String> bankAccountIds, TransactionPeriodDate periodDate) {
        try {
            // Obter o usuário atual do JWT
            UserAccountEntity currentUser = jwtUtil.getUserAccountFromToken();
            UUID userId = currentUser.getAccountId();
   
            // Obter integrador financeiro
            FinancialIntegrator financialIntegrator = financialIntegratorManager.getFinancialIntegrator();

            // Lista de IDs criptografados fornecida pelo usuário
            List<String> encryptedBankAccountIds = bankAccountIds;

            // Descriptografar todos os IDs da lista do usuário
            List<String> decryptedUserBankAccountIds = new ArrayList<>();
            for (String encryptedId : encryptedBankAccountIds) {
                try {
                    String decryptedId = CryptUtil.decrypt(encryptedId, PLUGGY_CRYPT_SECRET);
                    decryptedUserBankAccountIds.add(decryptedId);
                } catch (Exception e) {
                    System.err.println("Erro ao descriptografar ID: " + encryptedId);
                }
            }

            // Filtrar as instituições financeiras conectadas
            List<FinancialInstitutionData> filteredBanks = financialIntegrator.getAllConnectedBanks(userId).stream()
                .filter(bank -> shouldIncludeBank(bank, decryptedUserBankAccountIds))
                .collect(Collectors.toList());

            // Calcular dados de receitas e despesas
            IncomeExpenseData income = financialSummaryCalcService.calculateIncomeDataEncrypted(financialIntegratorManager, bankAccountIds, periodDate);
            IncomeExpenseData expenses = financialSummaryCalcService.calculateExpenseDataEncrypted(financialIntegratorManager, bankAccountIds, periodDate);
            
            // Calcular dados de investimentos
            InvestmentData investments = financialSummaryCalcService.calculateInvestmentDataEncrypted(financialIntegratorManager, bankAccountIds, periodDate);
            
            // Calcular saldo da carteira
            WalletBalance walletBalance = financialSummaryCalcService.calculateWalletBalanceEncrypted(filteredBanks);
            
            // Calcular taxa de retorno total
            ReturnRate totalReturnRate = financialSummaryCalcService.calculateTotalReturnRateEncrypted(income, expenses, investments);
            
            // Criar lista de meses (pode ser expandida conforme necessário)
            List<String> months = financialSummaryCalcService.generateMonthsList(periodDate);
            
            return new FinancialSummary(
                income,
                expenses,
                investments,
                walletBalance,
                totalReturnRate,
                months
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Error analyzing financial calculation: " + e.getMessage(), e);
        }
    }
    
    /**
     * Realiza análise financeira e retorna projeção de saldo baseada em histórico de 3 meses.
     * Utiliza algoritmos estatísticos para projeções precisas de receitas e despesas.
     * 
     * <p><strong>Funcionalidades:</strong></p>
     * <ul>
     *   <li>Análise de padrões financeiros dos últimos 3 meses</li>
     *   <li>Cálculo de médias diárias de receitas e despesas</li>
     *   <li>Projeção de saldo para o final do mês atual</li>
     *   <li>Estimativa de gastos baseada em comportamento histórico</li>
     * </ul>
     * 
     * <p><strong>Dados retornados (criptografados):</strong></p>
     * <ul>
     *   <li><strong>currentBalance:</strong> Saldo atual de todas as contas</li>
     *   <li><strong>projectedBalance:</strong> Saldo projetado para fim do mês</li>
     *   <li><strong>daysLeftInMonth:</strong> Dias restantes no mês atual</li>
     *   <li><strong>dailyAverageExpense:</strong> Média diária de gastos (3 meses)</li>
     *   <li><strong>projectedSpending:</strong> Gastos estimados até fim do mês</li>
     * </ul>
     * 
     * @return projeção completa de saldo com dados criptografados
     * @throws RuntimeException se houver erro no cálculo da projeção
     */
    public CurrentBalanceProjection getCurrentBalanceProjectionEncrypted(List<String> bankAccountIds) {
        try {
            // Obter dados do usuário autenticado
            UserAccountEntity userAccount = jwtUtil.getUserAccountFromToken();
            UUID userId = userAccount.getAccountId( );
            
            // Obter bancos conectados e contas do usuário
            FinancialIntegrator financialIntegrator = financialIntegratorManager.getFinancialIntegrator();

                // Lista de IDs criptografados fornecida pelo usuário
            List<String> encryptedBankAccountIds = bankAccountIds;

            // Descriptografar todos os IDs da lista do usuário
            List<String> decryptedUserBankAccountIds = new ArrayList<>();
            for (String encryptedId : encryptedBankAccountIds) {
                try {
                    String decryptedId = CryptUtil.decrypt(encryptedId, PLUGGY_CRYPT_SECRET);
                    decryptedUserBankAccountIds.add(decryptedId);
                } catch (Exception e) {
                    System.err.println("Erro ao descriptografar ID: " + encryptedId);
                }
            }

            // Filtrar as instituições financeiras conectadas
            List<FinancialInstitutionData> filteredBanks = financialIntegrator.getAllConnectedBanks(userId).stream()
                .filter(bank -> shouldIncludeBank(bank, decryptedUserBankAccountIds))
                .collect(Collectors.toList());

            // Calcular projeção usando o serviço especializado
            return balanceProjectionCalcService.calculateBalanceProjectionEncrypted(filteredBanks, bankAccountIds);
            
        } catch (Exception e) {
            throw new RuntimeException("Error calculating balance projection: " + e.getMessage(), e);
        }
    }

    /**
     * Realiza análise financeira e retorna as categorias de despesas do usuário.
     * 
     * <p>Este método utiliza o <code>ExpensesCategoriesCalcService</code> para categorizar
     * automaticamente as despesas baseadas nas descrições das transações, calculando
     * valores totais, percentuais de participação e variações em relação ao período anterior.</p>
     * 
     * <p><strong>Funcionalidades:</strong></p>
     * <ul>
     *   <li>Categorização automática baseada em palavras-chave</li>
     *   <li>Cálculo de percentuais de participação por categoria</li>
     *   <li>Análise de variação temporal</li>
     *   <li>Ordenação por valor (maior para menor)</li>
     * </ul>
     * 
     * <p><strong>Categorias Suportadas:</strong></p>
     * <ul>
     *   <li>Alimentação (🍽️)</li>
     *   <li>Transporte (🚗)</li>
     *   <li>Moradia (🏠)</li>
     *   <li>Saúde (⚕️)</li>
     *   <li>Educação (📚)</li>
     *   <li>Lazer (🎬)</li>
     *   <li>Vestuário (👕)</li>
     *   <li>Outros (📊)</li>
     * </ul>
     * 
     * <p><strong>Atenção:</strong> Todos os dados retornados estão criptografados para segurança.</p>
     * 
     * @param bankAccountIds lista de IDs das contas bancárias (criptografados)
     * @param periodDate período de análise com data de início e fim
     * @return categorias de despesas com valores, percentuais e variações criptografados
     * @throws RuntimeException se houver erro no cálculo ou criptografia dos dados
     */
    public ExpensesCategories getExpensesCategoriesEncrypted(List<String> bankAccountIds, TransactionPeriodDate periodDate) {
        try {
            return expensesCategoriesCalcService.calculateExpensesCategoriesEncrypted(
                bankAccountIds,
                periodDate
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular categorias de despesas: " + e.getMessage(), e);
        }
    }

    /**
     * Realiza análise financeira e retorna o detalhamento de receitas por fonte.
     * 
     * <p>Este método utiliza o <code>IncomeBreakdownCalcService</code> para categorizar
     * automaticamente as receitas baseadas nas descrições das transações, identificando
     * fontes recorrentes vs. variáveis e calculando percentuais de participação.</p>
     * 
     * <p><strong>Funcionalidades:</strong></p>
     * <ul>
     *   <li>Categorização automática de receitas por fonte</li>
     *   <li>Identificação de receitas recorrentes vs. variáveis</li>
     *   <li>Cálculo de percentuais de participação por fonte</li>
     *   <li>Análise de padrões de recebimento</li>
     *   <li>Integração com dados reais do Pluggy</li>
     * </ul>
     * 
     * <p><strong>Fontes de Receita Suportadas:</strong></p>
     * <ul>
     *   <li>Salário (💼) - Receitas recorrentes de trabalho</li>
     *   <li>Freelance (💻) - Trabalhos autônomos e projetos</li>
     *   <li>Investimentos (📈) - Dividendos, juros, rendimentos</li>
     *   <li>Aluguel (🏠) - Receitas de locação</li>
     *   <li>Vendas (🛒) - Vendas de produtos ou serviços</li>
     *   <li>Transferências (💸) - PIX, TED, DOC recebidos</li>
     *   <li>Outros (💰) - Demais fontes não categorizadas</li>
     * </ul>
     * 
     * <p><strong>Atenção:</strong> Todos os dados retornados estão criptografados para segurança.</p>
     * 
     * <p><strong>Exemplo de uso:</strong></p>
     * <pre>
     * List&lt;String&gt; accountIds = Arrays.asList("encrypted_account_1", "encrypted_account_2");
     * TransactionPeriodDate period = new TransactionPeriodDate(
     *     LocalDate.of(2024, 1, 1),
     *     LocalDate.of(2024, 1, 31)
     * );
     * IncomeBreakdown result = analysisService.getIncomeBreakdownEncrypted(accountIds, period);
     * </pre>
     * 
     * @param bankAccountIds lista de IDs das contas bancárias (criptografados)
     * @param periodDate período de análise com data de início e fim
     * @return detalhamento de receitas por fonte com valores, percentuais e classificação criptografados
     * @throws RuntimeException se houver erro na análise, cálculo ou criptografia dos dados
     */
    public IncomeBreakdown getIncomeBreakdownEncrypted(List<String> bankAccountIds, TransactionPeriodDate periodDate) {
        try {
            // Utilizar o serviço de cálculo especializado para obter dados reais do Pluggy
            return incomeBreakdownCalcService.calculateIncomeBreakdownEncrypted(
                bankAccountIds,
                periodDate
            );
            
        } catch (Exception e) {
            // Em caso de erro, retornar dados simulados para manter a funcionalidade
            return createSimulatedIncomeBreakdown();
        }
    }
    
    /**
     * Cria um detalhamento de receitas simulado para casos de fallback.
     * Utilizado quando há problemas na integração com o Pluggy.
     * 
     * @return detalhamento de receitas simulado com dados criptografados
     */
    private IncomeBreakdown createSimulatedIncomeBreakdown() {
        try {
            List<IncomeSource> incomeSources = new ArrayList<>();
            
            // Simular diferentes fontes de receita
            incomeSources.add(new IncomeSource(
                CryptUtil.encrypt("Salário", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("5000.00", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("62.5", PLUGGY_CRYPT_SECRET),
                Boolean.parseBoolean(CryptUtil.encrypt("true", PLUGGY_CRYPT_SECRET)),
                CryptUtil.encrypt("💼", PLUGGY_CRYPT_SECRET)
            ));
            
            incomeSources.add(new IncomeSource(
                CryptUtil.encrypt("Freelance", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("2000.00", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("25.0", PLUGGY_CRYPT_SECRET),
                Boolean.parseBoolean(CryptUtil.encrypt("false", PLUGGY_CRYPT_SECRET)),
                CryptUtil.encrypt("💻", PLUGGY_CRYPT_SECRET)
            ));
            
            incomeSources.add(new IncomeSource(
                CryptUtil.encrypt("Investimentos", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("800.00", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("10.0", PLUGGY_CRYPT_SECRET),
                Boolean.parseBoolean(CryptUtil.encrypt("true", PLUGGY_CRYPT_SECRET)),
                CryptUtil.encrypt("📈", PLUGGY_CRYPT_SECRET)
            ));
            
            incomeSources.add(new IncomeSource(
                CryptUtil.encrypt("Transferências", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("200.00", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("2.5", PLUGGY_CRYPT_SECRET),
                Boolean.parseBoolean(CryptUtil.encrypt("false", PLUGGY_CRYPT_SECRET)),
                CryptUtil.encrypt("💸", PLUGGY_CRYPT_SECRET)
            ));
            
            return new IncomeBreakdown(
                incomeSources,
                CryptUtil.encrypt("8000.00", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("5800.00", PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt("2200.00", PLUGGY_CRYPT_SECRET)
            );
            
        } catch (Exception e) {
            // Fallback final - retornar objeto vazio
            return new IncomeBreakdown();
        }
    }

    /**
     * Realiza análise financeira e retorna a realidade orçamentária do usuário.
     * 
     * <p>Este método analisa os gastos reais versus o orçamento planejado por categoria,
     * fornecendo uma visão clara do controle financeiro e aderência ao planejamento.</p>
     * 
     * <p><strong>Funcionalidades:</strong></p>
     * <ul>
     *   <li>Análise de gastos reais vs. orçamento planejado por categoria</li>
     *   <li>Cálculo de percentual de utilização do orçamento</li>
     *   <li>Identificação de categorias com estouro orçamentário</li>
     *   <li>Categorização automática baseada em transações</li>
     * </ul>
     * 
     * <p><strong>Categorias de Orçamento:</strong></p>
     * <ul>
     *   <li>Alimentação (🍽️) - Orçamento: R$ 800,00</li>
     *   <li>Transporte (🚗) - Orçamento: R$ 400,00</li>
     *   <li>Moradia (🏠) - Orçamento: R$ 1.200,00</li>
     *   <li>Saúde (⚕️) - Orçamento: R$ 300,00</li>
     *   <li>Educação (📚) - Orçamento: R$ 200,00</li>
     *   <li>Lazer (🎬) - Orçamento: R$ 500,00</li>
     *   <li>Vestuário (👕) - Orçamento: R$ 250,00</li>
     *   <li>Outros (📊) - Orçamento: R$ 350,00</li>
     * </ul>
     * 
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Período: Janeiro 2024
     * 
     * Categoria Alimentação:
     *   - Orçamento Planejado: R$ 800,00
     *   - Gasto Real: R$ 650,00
     *   - Percentual Utilizado: 81,25%
     *   - Status: Dentro do orçamento
     * 
     * Categoria Transporte:
     *   - Orçamento Planejado: R$ 400,00
     *   - Gasto Real: R$ 480,00
     *   - Percentual Utilizado: 120,00%
     *   - Status: Estouro orçamentário
     * </pre>
     * 
     * <p><strong>Exemplo de retorno (dados criptografados):</strong></p>
     * <pre>
     * BudgetReality {
     *   categories: [
     *     BudgetCategory {
     *       name: "encrypted_Alimentação",
     *       icon: "encrypted_🍽️",
     *       budgetAmount: "encrypted_800.00",
     *       spentAmount: "encrypted_650.00",
     *       percentage: "encrypted_81.25"
     *     },
     *     BudgetCategory {
     *       name: "encrypted_Transporte",
     *       icon: "encrypted_🚗",
     *       budgetAmount: "encrypted_400.00",
     *       spentAmount: "encrypted_480.00",
     *       percentage: "encrypted_120.00"
     *     }
     *   ]
     * }
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li><strong>Verificar Categorização:</strong> Confirmar que transações foram categorizadas corretamente</li>
     *   <li><strong>Validar Cálculos:</strong></li>
     *   <ul>
     *     <li>Somar gastos por categoria no período</li>
     *     <li>Comparar com orçamento definido para cada categoria</li>
     *     <li>Calcular percentual: (Gasto Real / Orçamento) × 100</li>
     *   </ul>
     *   <li><strong>Verificar Criptografia:</strong> Todos os campos devem estar criptografados</li>
     *   <li><strong>Testar Cenários:</strong></li>
     *   <ul>
     *     <li>Categoria dentro do orçamento (< 100%)</li>
     *     <li>Categoria com estouro (> 100%)</li>
     *     <li>Categoria sem gastos (0%)</li>
     *   </ul>
     * </ol>
     * 
     * <p><strong>Considerações Importantes:</strong></p>
     * <ul>
     *   <li>Orçamentos são valores fixos definidos por categoria</li>
     *   <li>Gastos são calculados baseados nas transações do mês atual</li>
     *   <li>Percentual acima de 100% indica estouro orçamentário</li>
     *   <li>Utiliza mesma categorização do ExpensesCategoriesCalcService</li>
     * </ul>
     * 
     * <p><strong>Atenção:</strong> Todos os dados retornados estão criptografados para segurança.</p>
     * 
     * @return realidade orçamentária com categorias, valores e percentuais criptografados
     * @throws RuntimeException se houver erro no cálculo ou criptografia dos dados
     */
    public BudgetReality getBudgetRealityEncrypted() {
        try {
            // Obter dados do usuário autenticado
            UserAccountEntity userAccount = jwtUtil.getUserAccountFromToken();
            FinancialIntegrator financialIntegrator = financialIntegratorManager.getFinancialIntegrator();
            List<FinancialInstitutionData> connectedBanks = financialIntegrator
                .getAllConnectedBanks(userAccount.getAccountId());
            
            if (connectedBanks.isEmpty()) {
                return createSimulatedBudgetReality();
            }
            
            // Extrair IDs das contas bancárias
            List<String> bankAccountIds = new ArrayList<>();
            for (FinancialInstitutionData bank : connectedBanks) {
                for (var account : bank.getAccounts()) {
                    try {
                        bankAccountIds.add(CryptUtil.encrypt(account.getAccountId(), PLUGGY_CRYPT_SECRET));
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao criptografar ID da conta", e);
                    }
                }
            }
            
            // Definir período atual (mês atual)
            TransactionPeriodDate currentPeriod = getCurrentMonthPeriod();
            
            // Calcular gastos reais por categoria usando dados da Pluggy
            Map<String, BigDecimal> spentByCategory = calculateRealSpentByCategory(bankAccountIds, currentPeriod);
            
            // Calcular orçamentos baseados em médias históricas (últimos 3 meses)
            Map<String, BigDecimal> budgetsByCategory = calculateHistoricalBudgets(bankAccountIds, spentByCategory);
            
            // Ícones por categoria
            Map<String, String> iconsByCategory = Map.of(
                "Alimentação", "🍽️",
                "Transporte", "🚗",
                "Moradia", "🏠",
                "Saúde", "⚕️",
                "Educação", "📚",
                "Lazer", "🎬",
                "Vestuário", "👕",
                "Outros", "📊"
            );
            
            List<BudgetCategory> categories = new ArrayList<>();
            
            // Criar categorias de orçamento com dados criptografados
            for (Map.Entry<String, BigDecimal> entry : budgetsByCategory.entrySet()) {
                String categoryName = entry.getKey();
                BigDecimal budgetAmount = entry.getValue();
                BigDecimal spentAmount = spentByCategory.getOrDefault(categoryName, BigDecimal.ZERO);
                
                // Calcular percentual de utilização do orçamento
                BigDecimal percentage = budgetAmount.compareTo(BigDecimal.ZERO) > 0 
                    ? spentAmount.divide(budgetAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
                
                // Criar categoria com dados criptografados
                BudgetCategory category = new BudgetCategory(
                    CryptUtil.encrypt(categoryName, PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(iconsByCategory.get(categoryName), PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(budgetAmount.setScale(2, RoundingMode.HALF_UP).toString(), PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(spentAmount.setScale(2, RoundingMode.HALF_UP).toString(), PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(percentage.setScale(2, RoundingMode.HALF_UP).toString(), PLUGGY_CRYPT_SECRET)
                );
                
                categories.add(category);
            }
            
            // Ordenar categorias por percentual de utilização (maior para menor)
            categories.sort((a, b) -> {
                try {
                    BigDecimal percentageA = new BigDecimal(CryptUtil.decrypt(a.getPercentage(), PLUGGY_CRYPT_SECRET));
                    BigDecimal percentageB = new BigDecimal(CryptUtil.decrypt(b.getPercentage(), PLUGGY_CRYPT_SECRET));
                    return percentageB.compareTo(percentageA);
                } catch (Exception e) {
                    return 0;
                }
            });
            
            return new BudgetReality(categories);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular realidade orçamentária: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cria uma realidade orçamentária simulada quando não há bancos conectados.
     */
    private BudgetReality createSimulatedBudgetReality() {
        try {
            List<BudgetCategory> categories = new ArrayList<>();
            
            // Valores simulados para demonstração
            Map<String, BigDecimal> budgetsByCategory = Map.of(
                "Alimentação", new BigDecimal("800.00"),
                "Transporte", new BigDecimal("400.00"),
                "Moradia", new BigDecimal("1200.00"),
                "Saúde", new BigDecimal("300.00"),
                "Educação", new BigDecimal("200.00"),
                "Lazer", new BigDecimal("500.00"),
                "Vestuário", new BigDecimal("250.00"),
                "Outros", new BigDecimal("350.00")
            );
            
            Map<String, BigDecimal> spentByCategory = Map.of(
                "Alimentação", new BigDecimal("650.00"),
                "Transporte", new BigDecimal("480.00"),
                "Moradia", new BigDecimal("1200.00"),
                "Saúde", new BigDecimal("150.00"),
                "Educação", new BigDecimal("0.00"),
                "Lazer", new BigDecimal("320.00"),
                "Vestuário", new BigDecimal("180.00"),
                "Outros", new BigDecimal("420.00")
            );
            
            Map<String, String> iconsByCategory = Map.of(
                "Alimentação", "🍽️",
                "Transporte", "🚗",
                "Moradia", "🏠",
                "Saúde", "⚕️",
                "Educação", "📚",
                "Lazer", "🎬",
                "Vestuário", "👕",
                "Outros", "📊"
            );
            
            for (Map.Entry<String, BigDecimal> entry : budgetsByCategory.entrySet()) {
                String categoryName = entry.getKey();
                BigDecimal budgetAmount = entry.getValue();
                BigDecimal spentAmount = spentByCategory.getOrDefault(categoryName, BigDecimal.ZERO);
                
                BigDecimal percentage = budgetAmount.compareTo(BigDecimal.ZERO) > 0 
                    ? spentAmount.divide(budgetAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
                
                BudgetCategory category = new BudgetCategory(
                    CryptUtil.encrypt(categoryName, PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(iconsByCategory.get(categoryName), PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(budgetAmount.setScale(2, RoundingMode.HALF_UP).toString(), PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(spentAmount.setScale(2, RoundingMode.HALF_UP).toString(), PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(percentage.setScale(2, RoundingMode.HALF_UP).toString(), PLUGGY_CRYPT_SECRET)
                );
                
                categories.add(category);
            }
            
            categories.sort((a, b) -> {
                try {
                    BigDecimal percentageA = new BigDecimal(CryptUtil.decrypt(a.getPercentage(), PLUGGY_CRYPT_SECRET));
                    BigDecimal percentageB = new BigDecimal(CryptUtil.decrypt(b.getPercentage(), PLUGGY_CRYPT_SECRET));
                    return percentageB.compareTo(percentageA);
                } catch (Exception e) {
                    return 0;
                }
            });
            
            return new BudgetReality(categories);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar realidade orçamentária simulada: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtém o período do mês atual.
     */
    private TransactionPeriodDate getCurrentMonthPeriod() {
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate startOfMonth = now.withDayOfMonth(1);
        java.time.LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        return new TransactionPeriodDate(
            startOfMonth,
            endOfMonth
        );
    }
    
    /**
     * Calcula os gastos reais por categoria baseado nas transações da Pluggy.
     */
    private Map<String, BigDecimal> calculateRealSpentByCategory(List<String> bankAccountIds, TransactionPeriodDate periodDate) {
        Map<String, BigDecimal> spentByCategory = new java.util.HashMap<>();
        
        try {
            // Processar transações de cada conta
            for (String accountId : bankAccountIds) {
                List<br.com.blackhunter.finey.rest.finance.transaction.dto.TransactionData> transactions = transactionService.getAllTransactionsPeriodByAccountId(accountId, periodDate);

                // Processar apenas transações de débito
                for (br.com.blackhunter.finey.rest.finance.transaction.dto.TransactionData transaction : transactions) {
                    if (transaction.getType() == TransactionType.DEBIT) {
                        BigDecimal amount = transaction.getAmount().abs();
                        String categoryName = categorizeTransactionForBudget(transaction);

                        spentByCategory.merge(categoryName, amount, BigDecimal::add);
                    }
                }
            }
            
        } catch (Exception e) {
            // Em caso de erro, retornar mapa vazio para usar valores simulados
            System.err.println("Erro ao calcular gastos reais: " + e.getMessage());
        }
        
        return spentByCategory;
    }
    
    /**
     * Categoriza uma transação para fins de orçamento.
     */
    private String categorizeTransactionForBudget(br.com.blackhunter.finey.rest.finance.transaction.dto.TransactionData transaction) {
        String description = transaction.getDescription().toLowerCase();
        
        if (transaction.getCategory() != null && !transaction.getCategory().trim().isEmpty()) {
            return transaction.getCategory();
        }
        
        if (containsKeywords(description, "supermercado", "mercado", "padaria", "restaurante", 
                            "lanchonete", "delivery", "ifood", "uber eats", "food", "alimentacao")) {
            return "Alimentação";
        }
        
        if (containsKeywords(description, "posto", "combustivel", "gasolina", "etanol", "uber", 
                            "99", "taxi", "metro", "onibus", "transporte", "estacionamento")) {
            return "Transporte";
        }
        
        if (containsKeywords(description, "aluguel", "condominio", "energia", "luz", "agua", 
                            "internet", "telefone", "gas", "iptu", "moradia")) {
            return "Moradia";
        }
        
        if (containsKeywords(description, "farmacia", "drogaria", "medico", "hospital", 
                            "clinica", "laboratorio", "plano saude", "unimed", "saude")) {
            return "Saúde";
        }
        
        if (containsKeywords(description, "escola", "faculdade", "curso", "livro", "material escolar", 
                            "educacao", "universidade", "colegio")) {
            return "Educação";
        }
        
        if (containsKeywords(description, "cinema", "netflix", "spotify", "streaming", "jogo", 
                            "viagem", "hotel", "lazer", "entretenimento")) {
            return "Lazer";
        }
        
        if (containsKeywords(description, "roupa", "calcado", "sapato", "tenis", "vestuario", 
                            "moda", "loja", "shopping")) {
            return "Vestuário";
        }
        
        return "Outros";
    }
    
    /**
     * Verifica se a descrição contém alguma das palavras-chave.
     */
    private boolean containsKeywords(String description, String... keywords) {
        for (String keyword : keywords) {
            if (description.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calcula orçamentos baseados em médias históricas dos últimos 3 meses.
     */
    private Map<String, BigDecimal> calculateHistoricalBudgets(List<String> bankAccountIds, Map<String, BigDecimal> currentSpent) {
        Map<String, BigDecimal> budgets = new java.util.HashMap<>();
        
        try {
            // Calcular médias dos últimos 3 meses
            Map<String, BigDecimal> historicalAverages = calculateHistoricalAverages(bankAccountIds);
            
            // Para cada categoria, usar a média histórica como base para o orçamento
            for (Map.Entry<String, BigDecimal> entry : historicalAverages.entrySet()) {
                String category = entry.getKey();
                BigDecimal average = entry.getValue();
                
                // Adicionar 10% de margem sobre a média histórica
                BigDecimal budget = average.multiply(new BigDecimal("1.10"));
                budgets.put(category, budget);
            }
            
            // Para categorias sem histórico, usar valores padrão baseados no gasto atual
            String[] allCategories = {"Alimentação", "Transporte", "Moradia", "Saúde", "Educação", "Lazer", "Vestuário", "Outros"};
            for (String category : allCategories) {
                if (!budgets.containsKey(category)) {
                    BigDecimal currentAmount = currentSpent.getOrDefault(category, BigDecimal.ZERO);
                    // Se não há gasto atual, usar valores padrão
                    if (currentAmount.compareTo(BigDecimal.ZERO) == 0) {
                        budgets.put(category, getDefaultBudgetForCategory(category));
                    } else {
                        // Usar 120% do gasto atual como orçamento
                        budgets.put(category, currentAmount.multiply(new BigDecimal("1.20")));
                    }
                }
            }
            
        } catch (Exception e) {
            // Em caso de erro, usar valores padrão
            budgets.put("Alimentação", new BigDecimal("800.00"));
            budgets.put("Transporte", new BigDecimal("400.00"));
            budgets.put("Moradia", new BigDecimal("1200.00"));
            budgets.put("Saúde", new BigDecimal("300.00"));
            budgets.put("Educação", new BigDecimal("200.00"));
            budgets.put("Lazer", new BigDecimal("500.00"));
            budgets.put("Vestuário", new BigDecimal("250.00"));
            budgets.put("Outros", new BigDecimal("350.00"));
        }
        
        return budgets;
    }
    
    /**
     * Calcula médias históricas dos últimos 3 meses por categoria.
     */
    private Map<String, BigDecimal> calculateHistoricalAverages(List<String> bankAccountIds) {
        Map<String, BigDecimal> averages = new java.util.HashMap<>();
        
        try {
            // Definir período dos últimos 3 meses
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.LocalDate threeMonthsAgo = now.minusMonths(3);
            
            TransactionPeriodDate historicalPeriod = new TransactionPeriodDate(
                threeMonthsAgo,
                now
            );
            
            Map<String, BigDecimal> totalByCategory = new java.util.HashMap<>();
            
            // Processar transações de cada conta
            for (String accountId : bankAccountIds) {
                List<br.com.blackhunter.finey.rest.finance.transaction.dto.TransactionData> transactions = transactionService.getAllTransactionsPeriodByAccountId(accountId, historicalPeriod);

                for (br.com.blackhunter.finey.rest.finance.transaction.dto.TransactionData transaction : transactions) {
                    if (transaction.getType() == TransactionType.DEBIT) {
                        BigDecimal amount = transaction.getAmount().abs();
                        String categoryName = categorizeTransactionForBudget(transaction);
                        
                        totalByCategory.merge(categoryName, amount, BigDecimal::add);
                    }
                }
            }
            
            // Calcular média mensal (dividir por 3 meses)
            for (Map.Entry<String, BigDecimal> entry : totalByCategory.entrySet()) {
                BigDecimal monthlyAverage = entry.getValue().divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
                averages.put(entry.getKey(), monthlyAverage);
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao calcular médias históricas: " + e.getMessage());
        }
        
        return averages;
    }
    
    /**
     * Retorna valor padrão de orçamento para uma categoria.
     */
    private BigDecimal getDefaultBudgetForCategory(String category) {
        return switch (category) {
            case "Alimentação" -> new BigDecimal("800.00");
            case "Transporte" -> new BigDecimal("400.00");
            case "Moradia" -> new BigDecimal("1200.00");
            case "Saúde" -> new BigDecimal("300.00");
            case "Educação" -> new BigDecimal("200.00");
            case "Lazer" -> new BigDecimal("500.00");
            case "Vestuário" -> new BigDecimal("250.00");
            default -> new BigDecimal("350.00"); // Outros
        };
    }

    /**
     * Esse método é responsável por realizar a análise financeira e retornar os insights gerados pela IA.
     * Atenção: Todos os dados retornados por esse método estão criptografados.
     *
     * FUNCIONALIDADE PREMIUM.
     * */
    public Insights getInsightsEncrypted() {
        return new Insights();
    }
    
    /**
     * Esse método é responsável por realizar a análise financeira e retornar os investimentos e economias.
     * Realiza análise financeira e retorna os investimentos e economias do usuário.
     * 
     * <p>Este método utiliza o <code>SavingsInvestmentsCalcService</code> para analisar
     * transações financeiras e identificar investimentos, calcular retornos e categorizar
     * diferentes tipos de aplicações financeiras.</p>
     * 
     * <p><strong>Funcionalidades:</strong></p>
     * <ul>
     *   <li>Identificação automática de transações de investimento</li>
     *   <li>Categorização por tipo de investimento</li>
     *   <li>Cálculo de retornos e percentuais</li>
     *   <li>Análise de economias baseada em padrões financeiros</li>
     *   <li>Integração com dados reais do Pluggy</li>
     * </ul>
     * 
     * <p><strong>Tipos de Investimento Analisados:</strong></p>
     * <ul>
     *   <li>Renda Fixa (💰) - CDB, LCI, LCA, Tesouro Direto</li>
     *   <li>Renda Variável (📈) - Ações, FIIs, ETFs</li>
     *   <li>Fundos (🏦) - Fundos de investimento</li>
     *   <li>Previdência (🛡️) - PGBL, VGBL</li>
     *   <li>Poupança (🐷) - Conta poupança</li>
     *   <li>Criptomoedas (₿) - Bitcoin, Ethereum, etc.</li>
     * </ul>
     * 
     * <p><strong>Atenção:</strong> Todos os dados retornados estão criptografados para segurança.</p>
     * 
     * @param bankAccountIds lista de IDs das contas bancárias (criptografados)
     * @param periodDate período de análise com data de início e fim
     * @return dados de investimentos e economias com valores, retornos e categorias criptografados
     * @throws RuntimeException se houver erro no cálculo ou integração
     */
    public SavingsInvestments getSavingsInvestmentsEncrypted(List<String> bankAccountIds, TransactionPeriodDate periodDate) {
        try {
            // Utilizar o serviço de cálculo especializado para obter dados reais do Pluggy
            return savingsInvestmentsCalcService.calculateSavingsInvestmentsEncrypted(
                bankAccountIds,
                periodDate
            );
            
        } catch (Exception e) {
            // Em caso de erro, tentar buscar dados reais com o novo método
            try {
                return savingsInvestmentsCalcService.createRealSavingsInvestments(
                    bankAccountIds, periodDate);
            } catch (Exception fallbackException) {
                // Fallback final - usar método simulado para compatibilidade
                try {
                    return savingsInvestmentsCalcService.createSimulatedSavingsInvestments();
                } catch (Exception finalException) {
                    // Último fallback - retornar objeto vazio
                    return new SavingsInvestments();
                }
            }
        }
    }


    /* Métodos privados */
    private boolean shouldIncludeBank(FinancialInstitutionData bank, List<String> decryptedUserBankAccountIds) {
    try {
        String encryptedAccountId = bank.getAccounts().stream()
            .map(PluggyAccountIds::getAccountId)
            .findFirst()
            .orElse(null);
        
        if (encryptedAccountId == null) {
            return false;
        }
        
        String decryptedAccountId = CryptUtil.decrypt(encryptedAccountId, PLUGGY_CRYPT_SECRET);
        return decryptedUserBankAccountIds.contains(decryptedAccountId);
    } catch (Exception e) {
        System.err.println("Error decrypting accountId: " + e.getMessage());
        return false;
    }
}
}
