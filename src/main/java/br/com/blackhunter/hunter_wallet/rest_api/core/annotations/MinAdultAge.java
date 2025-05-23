/*
 * @(#)MinAdultAge.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

/**
 * Classe protegida - Alterações somente por CODEOWNERS.
 */

package br.com.blackhunter.hunter_wallet.rest_api.core.annotations;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.validation.BirthDateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotação <code>MinAdultAge</code>
 * <p>Valida se uma data de nascimento corresponde a uma idade mínima de adulto.</p>
 * <p>Esta anotação é utilizada para validar campos de data de nascimento em DTOs e entidades,
 * garantindo que o usuário tenha a idade mínima legal para utilizar o sistema.</p>
 * 
 * @author Black Hunter
 * @since 2025
 */
@Documented
@Constraint(validatedBy = BirthDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinAdultAge {
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
