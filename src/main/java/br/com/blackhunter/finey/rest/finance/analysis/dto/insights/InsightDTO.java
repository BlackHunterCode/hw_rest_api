package br.com.blackhunter.finey.rest.finance.analysis.dto.insights;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsightDTO {
    @JsonProperty(value = "title")
    private String titleEncrypted;
    @JsonProperty(value = "subtitle")
    private String subtitleEncrypted;
    @JsonProperty(value = "icon")
    private String iconEncrypted;
    private boolean isItActionable;
    @JsonProperty(value = "actionableText")
    private String actionableTextEncrypted;
    @JsonProperty(value = "actionableLink")
    private String actionableLinkEncrypted;
}
