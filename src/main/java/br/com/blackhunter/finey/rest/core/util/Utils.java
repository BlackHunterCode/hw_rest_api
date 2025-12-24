package br.com.blackhunter.finey.rest.core.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static boolean isEmptyOrNull(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static Double safeParseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static String formatAmountInReal(String amount) {
        return String.format("R$ %.2f", safeParseDouble(amount));
    }

    public static String formatAmountInReal(Double amount) {
        return String.format("R$ %.2f", amount);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }
}
