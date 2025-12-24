package br.com.blackhunter.finey.rest.finance.analysis.service;

import br.com.blackhunter.finey.rest.finance.analysis.dto.score.FinancialScorePeriodDTO;

import java.time.LocalDate;
import java.util.List;

public interface FinancialScorePeriodService {
    FinancialScorePeriodDTO getFinancialScorePeriod(List<String> bankAccountIds, LocalDate referenceDateMonthYear, LocalDate startDate, LocalDate endDate);
}
