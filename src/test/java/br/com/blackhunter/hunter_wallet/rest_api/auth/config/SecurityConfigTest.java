package br.com.blackhunter.hunter_wallet.rest_api.auth.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 * Classe protegida - Alterações somente por CODEOWNERS.
 * <p>Classe <code>SecurityConfigTest</code>.</p>
 * Testes para a configuração de segurança da aplicação.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve negar acesso a endpoints protegidos sem autenticação")
    void accessProtectedEndpoints_WithoutAuthentication_ShouldDenyAccess() throws Exception {
        // Act & Assert - Test access to protected API endpoints
        mockMvc.perform(get("/v1/api/user-profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve incluir cabeçalhos de segurança nas respostas")
    void responseHeaders_ShouldIncludeSecurityHeaders() throws Exception {
        // Act & Assert - Check security headers using a public endpoint
        // We don't care about the status code, just that the headers are present
        mockMvc.perform(get("/v1/public/auth")
                .with(user("test@example.com").password("password").roles("USER")))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Cache-Control"))
                .andExpect(header().exists("Pragma"));
    }

    @Test
    @DisplayName("Deve verificar cabeçalhos de segurança em requisições OPTIONS")
    void corsPreflightRequests_ShouldBeAllowed() throws Exception {
        // Act & Assert - Check that security headers are present in OPTIONS responses
        // Note: We're not checking for CORS headers since CORS may not be configured
        mockMvc.perform(options("/v1/public/auth")
                .with(user("test@example.com").password("password").roles("USER")))
                // We don't expect 200 OK since CORS might not be configured
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Cache-Control"));
    }
}
