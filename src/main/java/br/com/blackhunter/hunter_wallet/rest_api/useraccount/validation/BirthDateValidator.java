/*
 * @(#)BirthDateValidator.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

/**
 * Classe protegida - Alterações somente por CODEOWNERS.
 */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.validation;

import br.com.blackhunter.hunter_wallet.rest_api.core.annotations.MinAdultAge;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

/**
 * Classe <code>BirthDateValidator</code>
 * <p>Validador para a anotação {@link MinAdultAge} que verifica se uma data de nascimento
 * corresponde a uma idade mínima de adulto.</p>
 * <p>Implementa: {@link ConstraintValidator}</p>
 * 
 * @author Black Hunter
 * @since 2025
 */
public class BirthDateValidator implements ConstraintValidator<MinAdultAge, LocalDate> {

    private static final int MIN_AGE = 18;

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return false;
        }
        return Period.between(birthDate, LocalDate.now()).getYears() >= MIN_AGE;
    }
}