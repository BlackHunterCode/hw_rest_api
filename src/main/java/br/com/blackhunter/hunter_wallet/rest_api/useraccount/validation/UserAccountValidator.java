/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 *
 * Classe protegida - Aletrações somente por CODEOWNERS.
 * */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.validation;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.exception.UserAccountCreationException;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.repository.UserAccountRepository;
import org.springframework.stereotype.Component;

/**
 * <p>Classe <code>UserAccountValidator</code>.</p>
 * <p>Classe de validações complexas para a conta de usuários.</p>
 * */
@Component
public class UserAccountValidator {
    private final UserAccountRepository userAccountRepository;

    /**
     * Injeção de dependências:
     * @param userAccountRepository
     * */
    public UserAccountValidator(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * <p>Verifica se o e-mail já está cadastrado no sistema</p>
     * <p>A exception lançada por esse método será tratada pelo handler global.</p>
     * @param email E-mail a ser verificado
     * @throws UserAccountCreationException Caso o e-mail já esteja cadastrado
     */
    public void validateEmailUniqueness(String email) {
        if (userAccountRepository.existsByEmail(email)) {
            throw new UserAccountCreationException("O e-mail informado já está cadastrado no sistema");
        }
    }
}
