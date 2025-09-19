package com.voxcompose.cli;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningProfile {
    @JsonProperty("wordCorrections")
    private Map<String, String> wordCorrections = new LinkedHashMap<>();

    @JsonProperty("capitalizations")
    private Map<String, String> capitalizations = new LinkedHashMap<>();

    @JsonProperty("technicalVocabulary")
    private List<String> technicalVocabulary = new ArrayList<>();

    @JsonProperty("phrasePatterns")
    private Map<String, String> phrasePatterns = new LinkedHashMap<>();

    public Map<String, String> getWordCorrections() {
        if (wordCorrections == null) wordCorrections = new LinkedHashMap<>();
        return wordCorrections;
    }

    public void setWordCorrections(Map<String, String> wordCorrections) {
        this.wordCorrections = wordCorrections;
    }

    public Map<String, String> getCapitalizations() {
        if (capitalizations == null) capitalizations = new LinkedHashMap<>();
        return capitalizations;
    }

    public void setCapitalizations(Map<String, String> capitalizations) {
        this.capitalizations = capitalizations;
    }

    public List<String> getTechnicalVocabulary() {
        if (technicalVocabulary == null) technicalVocabulary = new ArrayList<>();
        return technicalVocabulary;
    }

    public void setTechnicalVocabulary(List<String> technicalVocabulary) {
        this.technicalVocabulary = technicalVocabulary;
    }

    public Map<String, String> getPhrasePatterns() {
        if (phrasePatterns == null) phrasePatterns = new LinkedHashMap<>();
        return phrasePatterns;
    }

    public void setPhrasePatterns(Map<String, String> phrasePatterns) {
        this.phrasePatterns = phrasePatterns;
    }
}
