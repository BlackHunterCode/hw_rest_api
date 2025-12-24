package br.com.blackhunter.finey.rest.finance.financial_commitments.service;

import br.com.blackhunter.finey.rest.finance.financial_commitments.dto.FinancialCommitmentData;
import br.com.blackhunter.finey.rest.finance.financial_commitments.dto.FinancialCommitmentPayload;

import java.util.List;

public interface FinancialCommitmentService {
    /**
     * Persiste um compromisso financeiro no banco de dados.
     * Caso seja uma inserção, após a persistência, o método tentará criar a notificação agendada para o compromisso financeiro.
     * Caso seja uma atualização, apenas atualizará os dados do compromisso financeiro e tentará atualizar a notificação agendada associada.
     *
     * @param payload Os dados do compromisso financeiro a serem persistidos.
     * @param update Indica se a operação é uma atualização (true) ou uma inserção (false).
     * @return Dados do compromisso financeiro persistido.
     * */
    FinancialCommitmentData persist(FinancialCommitmentPayload payload, boolean update);

    List<FinancialCommitmentData> listAllUserCommitments();
    List<FinancialCommitmentData> listUpCommingUserCommitments();
}
