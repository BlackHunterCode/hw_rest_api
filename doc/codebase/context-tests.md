# Contexto para Criação de Testes Unitários em Spring

## Objetivo
Este documento serve como contexto para orientar uma IA na criação de testes unitários para um domínio específico (`useraccount`) em uma aplicação Spring, seguindo boas práticas e uma estrutura organizada.

## Estrutura do Pacote de Testes
O pacote de testes do domínio `useraccount` segue a seguinte estrutura (quando existente):

test/
└── com/
└── example/
└── useraccount/
├── mapper/ - para testes de mapeamento (se existir)
├── repository/ - para testes de repositório (se existir)
├── service/ - para testes de serviço (se existir)
├── controller/ - para testes de controller (se existir)
├── util/ - para testes de utilitários (se existir)
└── validation/ - para testes de validação (se existir)


## Instruções para a IA

1. **Análise Prévia**:
   - Antes de escrever qualquer teste, analise o pacote `com.example.useraccount` no código fonte principal para identificar quais subpacotes (mapper, repository, service, controller, util, validation) realmente existem.
   - Só crie testes para os subpacotes que existirem na implementação.

2. **Criação dos Testes**:
   - Para cada subpacote existente, crie testes unitários adequados seguindo as melhores práticas para Spring.
   - Use JUnit 5 (Jupiter) como framework de teste.
   - Utilize Mockito para mockar dependências quando necessário.
   - Siga o padrão de nomenclatura `ClassNameTest` para os arquivos de teste.

3. **Diretrizes Específicas por Camada**:

   - **Mapper** (se existir):
     - Teste todos os métodos de mapeamento entre DTOs e entidades.
     - Verifique mapeamentos nulos e conversões de tipos.

   - **Repository** (se existir):
     - Use@DataJpaTest para testar repositórios JPA.
     - Teste queries customizadas e operações CRUD básicas.

   - **Service** (se existir):
     - Foque na lógica de negócio.
     - Mocke todas as dependências externas (repositórios, clients, etc.).
     - Teste cenários de sucesso, falha e casos limítrofes.

   - **Controller** (se existir):
     - Use@WebMvcTest para testar controllers REST.
     - Teste status HTTP, corpo da resposta e cabeçalhos.
     - Valide tratamento de erros.

   - **Util** (se existir):
     - Teste métodos utilitários com diversos cenários de entrada.
     - Foque em cobertura de casos extremos.

   - **Validation** (se existir):
     - Teste anotações de validação customizadas.
     - Verifique mensagens de erro e cenários de validação.

4. **Requisitos Gerais**:
   - Mantenha os testes isolados e independentes.
   - Use assertions descritivas (Hamcrest ou AssertJ).
   - Prefira testes claros e legíveis sobre complexidade.
   - Documente casos de teste complexos com comentários.