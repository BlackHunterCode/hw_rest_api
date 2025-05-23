/*
 * @(#)MultipartFileUtil.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

/**
 * Classe protegida - Alterações somente por CODEOWNERS.
 */

package br.com.blackhunter.hunter_wallet.rest_api.core.util;

import br.com.blackhunter.hunter_wallet.rest_api.core.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * Classe <code>MultipartFileUtil</code>
 * <p>Classe utilitária para manipulação e validação de arquivos enviados via multipart/form-data.</p>
 * <p>Fornece métodos para validação de imagens, tipos de arquivos e conversão de formatos.</p>
 * 
 * @author Black Hunter
 * @since 2025
 */
public class MultipartFileUtil {
    /**
     * Valida se o arquivo é uma imagem válida e retorna seus bytes.
     * 
     * @param multipartFile O arquivo enviado via multipart/form-data
     * @return Array de bytes do arquivo validado
     * @throws BusinessException Se o arquivo não for uma imagem válida ou ocorrer erro na leitura
     */
    public static byte[] validateAndGetMultipartFileBytes(MultipartFile multipartFile) {
        if(!validateMultipartFileImage(multipartFile))
            throw new BusinessException("Invalid format! The uploaded file must be an image.");
        try {
            return multipartFile.getBytes();
        } catch (IOException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Valida se o arquivo é uma imagem válida verificando extensão, tipo de conteúdo e estrutura.
     * 
     * @param multipartFile O arquivo enviado via multipart/form-data
     * @return true se o arquivo for uma imagem válida, false caso contrário
     */
    public static boolean validateMultipartFileImage(MultipartFile multipartFile) {
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp");
        validateExtension(multipartFile, allowedExtensions);
        return validateContentType(multipartFile, "image/")
               && validateImageStructure(multipartFile);
    }

    /**
     * Valida se a extensão do arquivo está entre as extensões permitidas.
     * 
     * @param multipartFile O arquivo enviado via multipart/form-data
     * @param allowedExtensions Lista de extensões permitidas (sem o ponto)
     * @throws BusinessException Se a extensão do arquivo não estiver na lista de permitidas
     */
    public static void validateExtension(MultipartFile multipartFile, List<String> allowedExtensions) {
        String fileName = multipartFile.getOriginalFilename();

        if (fileName == null || !isValidExtension(fileName, allowedExtensions)) {
            throw new BusinessException("Invalid format! Allowed types are: " + String.join(", ", allowedExtensions));
        }
    }

    /**
     * Valida se o tipo de conteúdo do arquivo começa com o prefixo especificado.
     * 
     * @param multipartFile O arquivo enviado via multipart/form-data
     * @param contentType Prefixo do tipo de conteúdo a ser validado (ex: "image/")
     * @return true se o tipo de conteúdo for válido, false caso contrário
     */
    public static boolean validateContentType(MultipartFile multipartFile, String contentType) {
        return  multipartFile.getContentType() != null &&  multipartFile.getContentType().startsWith(contentType);
    }

    /**
     * Valida se o arquivo possui uma estrutura de imagem válida que pode ser lida.
     * 
     * @param multipartFile O arquivo enviado via multipart/form-data
     * @return true se a estrutura da imagem for válida, false caso contrário
     */
    public static boolean validateImageStructure(MultipartFile multipartFile) {
        try {
            BufferedImage image = ImageIO.read(multipartFile.getInputStream());
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Verifica se o nome do arquivo termina com uma das extensões permitidas.
     * 
     * @param fileName Nome do arquivo a ser validado
     * @param allowedExtensions Lista de extensões permitidas (sem o ponto)
     * @return true se a extensão for válida, false caso contrário
     */
    private static boolean isValidExtension(String fileName, List<String> allowedExtensions) {
        return allowedExtensions.stream()
                .anyMatch(ext -> fileName.toLowerCase().endsWith("." + ext.toLowerCase()));
    }
}
