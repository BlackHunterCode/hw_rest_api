package br.com.blackhunter.finey.rest.finance.analysis.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
import br.com.blackhunter.finey.rest.finance.analysis.dto.income.IncomeBreakdown;
import br.com.blackhunter.finey.rest.finance.analysis.dto.income.IncomeSource;
import br.com.blackhunter.finey.rest.finance.transaction.enums.TransactionType;

/**
 * Serviço responsável por calcular e categorizar fontes de renda financeira.
 * 
 * <p>Este serviço analisa transações de crédito e as categoriza automaticamente
 * em diferentes fontes de renda, calculando valores totais, percentuais de participação
 * e identificando receitas recorrentes vs. variáveis.</p>
 * 
 * <p><strong>Funcionalidades:</strong></p>
 * <ul>
 *   <li>Categorização automática de receitas baseada em descrições</li>
 *   <li>Identificação de receitas recorrentes vs. variáveis</li>
 *   <li>Cálculo de percentuais de participação por fonte</li>
 *   <li>Análise de padrões de recebimento</li>
 *   <li>Criptografia de todos os dados retornados</li>
 * </ul>
 * 
 * @author BlackHunter Team
 * @version 1.0
 * @since 2024
 */
@Service
public class IncomeBreakdownCalcService {
    
    @Value("${hunter.secrets.pluggy.crypt-secret}")
    private String PLUGGY_CRYPT_SECRET;

    @Autowired
    private TransactionService transactionService;

    /**
     * Calcula o detalhamento de receitas por fonte para o período especificado.
     * 
     * <p><strong>Metodologia de Cálculo:</strong></p>
     * <ol>
     *   <li><strong>Coleta de Dados:</strong> Busca todas as transações CREDIT do período</li>
     *   <li><strong>Categorização:</strong> Classifica receitas por fonte baseada na descrição</li>
     *   <li><strong>Análise de Recorrência:</strong> Identifica padrões de recebimento mensal</li>
     *   <li><strong>Cálculo de Percentuais:</strong> Determina participação de cada fonte no total</li>
     *   <li><strong>Criptografia:</strong> Protege todos os dados sensíveis</li>
     * </ol>
     * 
     * <p><strong>Categorias de Receita Suportadas:</strong></p>
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
     * <p><strong>Exemplo de cenário para validação manual:</strong></p>
     * <pre>
     * Período: Janeiro 2024
     * 
     * Transações encontradas:
     *   - "Salário Empresa XYZ" - R$ 5.000,00 (Recorrente)
     *   - "Freelance Projeto ABC" - R$ 2.500,00 (Variável)
     *   - "Dividendos Ações" - R$ 300,00 (Recorrente)
     *   - "PIX Recebido" - R$ 200,00 (Variável)
     * 
     * Resultado esperado:
     *   - Total: R$ 8.000,00
     *   - Recorrente: R$ 5.300,00 (66,25%)
     *   - Variável: R$ 2.700,00 (33,75%)
     * </pre>
     * 
     * <p><strong>Exemplo de retorno (dados criptografados):</strong></p>
     * <pre>
     * IncomeBreakdown {
     *   incomeSources: [
     *     IncomeSource {
     *       name: "encrypted_Salário",
     *       amount: "encrypted_5000.00",
     *       percentage: "encrypted_62.5",
     *       isRecurring: "encrypted_true",
     *       icon: "encrypted_💼"
     *     },
     *     IncomeSource {
     *       name: "encrypted_Freelance",
     *       amount: "encrypted_2500.00",
     *       percentage: "encrypted_31.25",
     *       isRecurring: "encrypted_false",
     *       icon: "encrypted_💻"
     *     }
     *   ],
     *   totalIncome: "encrypted_8000.00",
     *   recurringIncome: "encrypted_5300.00",
     *   variableIncome: "encrypted_2700.00"
     * }
     * </pre>
     * 
     * <p><strong>Como validar manualmente (QA):</strong></p>
     * <ol>
     *   <li><strong>Verificar Dados:</strong> Confirmar que todas as transações CREDIT do período foram incluídas</li>
     *   <li><strong>Validar Categorização:</strong></li>
     *   <ul>
     *     <li>Verificar se descrições foram categorizadas corretamente</li>
     *     <li>Confirmar identificação de receitas recorrentes vs. variáveis</li>
     *   </ul>
     *   <li><strong>Conferir Cálculos:</strong></li>
     *   <ul>
     *     <li>Somar valores por categoria e comparar com totais</li>
     *     <li>Verificar se percentuais somam 100%</li>
     *     <li>Confirmar separação entre receita recorrente e variável</li>
     *   </ul>
     *   <li><strong>Testar Criptografia:</strong> Descriptografar valores e comparar com dados originais</li>
     * </ol>
     * 
     * @param bankAccountIds lista de IDs das contas bancárias (criptografados)
     * @param periodDate período de análise com data de início e fim
     * @return detalhamento de receitas por fonte com todos os dados criptografados
     * @throws Exception se houver erro na descriptografia, busca de transações ou criptografia dos resultados
     */
    public IncomeBreakdown calculateIncomeBreakdownEncrypted(
            List<String> bankAccountIds,
            TransactionPeriodDate periodDate) throws Exception {
        
        // Mapa para armazenar receitas por categoria
        Map<String, IncomeSourceData> incomeByCategory = new HashMap<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        
        // Buscar transações de todas as contas
        for (String accountId : bankAccountIds) {
            List<TransactionData> transactions = transactionService.getAllTransactionsPeriodByAccountId(accountId, periodDate);
            
            // Processar apenas transações de crédito (receitas)
            for (TransactionData transaction : transactions) {
                if (transaction.getType() == TransactionType.CREDIT) {
                    String category = categorizeIncomeTransaction(transaction);
                    String icon = getIconForCategory(category);
                    boolean isRecurring = isRecurringIncome(transaction, category);
                    
                    IncomeSourceData sourceData = incomeByCategory.getOrDefault(category, 
                        new IncomeSourceData(category, icon, BigDecimal.ZERO, isRecurring));
                    
                    sourceData.amount = sourceData.amount.add(transaction.getAmount());
                    incomeByCategory.put(category, sourceData);
                    
                    totalIncome = totalIncome.add(transaction.getAmount());
                }
            }
        }
        
        // Converter para lista de IncomeSource e calcular percentuais
        List<IncomeSource> incomeSources = new ArrayList<>();
        BigDecimal recurringTotal = BigDecimal.ZERO;
        BigDecimal variableTotal = BigDecimal.ZERO;
        
        for (IncomeSourceData sourceData : incomeByCategory.values()) {
            double percentage = totalIncome.compareTo(BigDecimal.ZERO) > 0 ? 
                sourceData.amount.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue() : 0.0;
            
            // Criptografar todos os campos
            IncomeSource source = new IncomeSource(
                CryptUtil.encrypt(sourceData.category, PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt(sourceData.amount.toString(), PLUGGY_CRYPT_SECRET),
                CryptUtil.encrypt(String.valueOf(percentage), PLUGGY_CRYPT_SECRET),
                Boolean.parseBoolean(CryptUtil.encrypt(String.valueOf(sourceData.isRecurring), PLUGGY_CRYPT_SECRET)),
                CryptUtil.encrypt(sourceData.icon, PLUGGY_CRYPT_SECRET)
            );
            
            incomeSources.add(source);
            
            // Somar para totais de recorrente/variável
            if (sourceData.isRecurring) {
                recurringTotal = recurringTotal.add(sourceData.amount);
            } else {
                variableTotal = variableTotal.add(sourceData.amount);
            }
        }
        
        // Ordenar por valor (maior para menor)
        incomeSources.sort((a, b) -> {
                try {
                    return Double.compare(
                       Double.parseDouble(CryptUtil.decrypt(b.getAmount(), PLUGGY_CRYPT_SECRET)),
                       Double.parseDouble(CryptUtil.decrypt(a.getAmount(), PLUGGY_CRYPT_SECRET))
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        );
        
        // Retornar resultado criptografado
        return new IncomeBreakdown(
            incomeSources,
            CryptUtil.encrypt(totalIncome.toString(), PLUGGY_CRYPT_SECRET),
            CryptUtil.encrypt(recurringTotal.toString(), PLUGGY_CRYPT_SECRET),
            CryptUtil.encrypt(variableTotal.toString(), PLUGGY_CRYPT_SECRET)
        );
    }
    
    /**
     * Categoriza uma transação de receita baseada na descrição.
     * 
     * @param transaction transação a ser categorizada
     * @return categoria da receita
     */
    private String categorizeIncomeTransaction(TransactionData transaction) {
        String description = transaction.getDescription().toLowerCase();
        
        // Verificar se já tem categoria definida na transação
        if (transaction.getCategory() != null && !transaction.getCategory().trim().isEmpty()) {
            return transaction.getCategory();
        }
        
        // Categorização baseada em palavras-chave na descrição
        if (description.contains("salario") || description.contains("salário") || 
            description.contains("ordenado") || description.contains("vencimento")) {
            return "Salário";
        }
        
        if (description.contains("freelance") || description.contains("projeto") || 
            description.contains("consultoria") || description.contains("servico")) {
            return "Freelance";
        }
        
        if (description.contains("dividendo") || description.contains("juros") || 
            description.contains("rendimento") || description.contains("investimento")) {
            return "Investimentos";
        }
        
        if (description.contains("aluguel") || description.contains("locacao") || 
            description.contains("locação") || description.contains("imovel")) {
            return "Aluguel";
        }
        
        if (description.contains("venda") || description.contains("produto") || 
            description.contains("mercadoria") || description.contains("comercio")) {
            return "Vendas";
        }
        
        if (description.contains("pix") || description.contains("ted") || 
            description.contains("doc") || description.contains("transferencia")) {
            return "Transferências";
        }
        
        return "Outros";
    }
    
    /**
     * Retorna o ícone correspondente à categoria.
     * 
     * @param category categoria da receita
     * @return ícone da categoria
     */
    private String getIconForCategory(String category) {
        switch (category) {
            case "Salário": return "💼";
            case "Freelance": return "💻";
            case "Investimentos": return "📈";
            case "Aluguel": return "🏠";
            case "Vendas": return "🛒";
            case "Transferências": return "💸";
            default: return "💰";
        }
    }
    
    /**
     * Determina se uma receita é recorrente baseada na categoria e padrões.
     * 
     * @param transaction transação a ser analisada
     * @param category categoria da receita
     * @return true se for receita recorrente
     */
    private boolean isRecurringIncome(TransactionData transaction, String category) {
        // Categorias tipicamente recorrentes
        if (category.equals("Salário") || category.equals("Aluguel") || 
            category.equals("Investimentos")) {
            return true;
        }
        
        // Categorias tipicamente variáveis
        if (category.equals("Freelance") || category.equals("Vendas") || 
            category.equals("Transferências")) {
            return false;
        }
        
        // Para "Outros", analisar valor (valores altos tendem a ser recorrentes)
        return transaction.getAmount().compareTo(new BigDecimal("1000.00")) >= 0;
    }
    
    /**
     * Classe interna para armazenar dados temporários de fonte de receita.
     */
    private static class IncomeSourceData {
        String category;
        String icon;
        BigDecimal amount;
        boolean isRecurring;
        
        IncomeSourceData(String category, String icon, BigDecimal amount, boolean isRecurring) {
            this.category = category;
            this.icon = icon;
            this.amount = amount;
            this.isRecurring = isRecurring;
        }
    }
}