/*
 * @(#)JavaClassExample.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package yourpackage;

/**
 * Classe <code>JavaClassExample</code>
 * <p>Exemplo de classe base para demonstração de padrões de documentação.</p>
 * <p>Extende: {@link Father}</p>
 * <p>Implementa: [{@link Contract}, {@link Contract2}]</p>
 * 
 * @author Black Hunter
 * @since 2025
 */
public class JavaClassExample extends Father implements Contract, Contract2 {
    // Atributo de exemplo 1
    private Boject attributeNameInCamelCase1;
    // Atributo de exemplo 2
    private Boject attributeNameInCamelCase2;
    // Atributo de exemplo 3
    private Boject attributeNameInCamelCase3;

    /**
     * Construtor padrão da classe JavaClassExample.
     */
    public JavaClassExample() {

    }

    /**
     * Construtor da classe JavaClassExample.
     * @param param1 parâmetro de inicialização
     */
    public JavaClassExample(Object param1) {

    }

    /**
     * Exemplo de método void.
     * @param param1 parâmetro de entrada
     */
    public void method1(Object param1) {

    }

    /**
     * Exemplo de método que retorna um objeto.
     * @param param1 parâmetro de entrada
     * @return objeto resultante ou null
     */
    public Object method2(Object param1) {
        return null;
    }

}
