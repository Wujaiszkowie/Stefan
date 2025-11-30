package com.wspiernik.infrastructure.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wspiernik.domain.facts.Fact;
import com.wspiernik.infrastructure.persistence.entity.CrisisScenario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

/**
 * Centralized management of system prompts for all modules.
 * Provides methods to build prompts with context injection.
 */
@ApplicationScoped
public class PromptTemplates {

    @Inject
    ObjectMapper objectMapper;

    // =========================================================================
    // Survey Prompt
    // =========================================================================

    private static final String SURVEY_PROMPT = """
            Ty jesteś asystentem do zbierania informacji zdrowotnych o podopiecznym.

            Twoim zadaniem jest przeprowadzenie przyjaznego wywiadu w celu zebrania podstawowych informacji.
            Musisz dowiedzieć się o podopiecznym:
            1. Wiek
            2. Główne choroby (np. cukrzyca, nadciśnienie, choroba serca)
            3. Bieżące leki (nazwa, dawka, częstotliwość)
            4. Ograniczenia ruchowe (np. trudności z chodzeniem, używa laski)
            5. Inne istotne warunki zdrowotne

            ZASADY:
            - Pytaj jeden temat na raz
            - Słuchaj uważnie i zadaj pytania follow-up jeśli odpowiedź jest niejasna
            - Bądź empatyczny i wspierający
            - Używaj prostego języka, unikaj żargonu medycznego
            - Potwierdzaj informacje przed przejściem dalej

            Gdy zbierzesz WSZYSTKIE wymagane informacje, zakończ rozmowę słowami:
            "SURVEY_COMPLETE"

            Następnie przedstaw krótkie podsumowanie zebranych informacji.
            """;

    // =========================================================================
    // Support Prompt
    // =========================================================================

    private static final String SUPPORT_PROMPT_TEMPLATE =
            """
            Jesteś asystentem wspierającym opiekunów osób z demencją.
            ## Kontekst opiekuna i pacjenta
                {facts_context}
            ## Zasady odpowiedzi
            1. **Odpowiadaj BEZPOŚREDNIO na pytanie** - nie udzielaj ogólnych rad gdy pytanie jest konkretne
            2. **Wykorzystuj kontekst RAG** - bazuj na dostarczonych fragmentach dokumentów
            3. **Bądź praktyczny** - dawaj konkretne kroki do wykonania
            4. **Język prosty** - tłumacz terminy medyczne
            5. **Empatia** - rozumiej, że opiekun może być zmęczony
            ## Czego NIE robisz
            - Nie stawiasz diagnoz ("to Alzheimer")
            - Nie zmieniasz leków/dawek
            - Nie dajesz spersonalizowanych zaleceń medycznych
            ## Gdy sytuacja pilna
                Jeśli jest zagrożenie życia (drgawki, ból w klatce, utrata przytomności) - zalecaj NATYCHMIASTOWY kontakt z pogotowiem.
            ## Struktura odpowiedzi
            1. Krótka odpowiedź na pytanie
            2. Praktyczne kroki (1-2-3)
            3. Kiedy zgłosić się do lekarza (jeśli istotne)
                Mów PO POLSKU. Bądź konkretny i pomocny.""";

    // =========================================================================
    // Facts Distiller Prompt
    // =========================================================================

    private static final String FACTS_DISTILLER_PROMPT_TEMPLATE = """
           [SYSTEM]
                                                               Jesteś parserem JSON. Zwracasz WYŁĄCZNIE tablicę JSON. Nigdy nie piszesz tekstu poza JSON.
            
                                                               [ROLA]
                                                               CareMemoryAgent - wyciągasz fakty o opiece nad osobą z demencją.
            
                                                               [WEJŚCIE]
                                                               Rozmowa: {transcript}
                                                               Znane fakty: {facts_context}
            
                                                               [WYCIĄGAJ FAKTY O WARTOŚCI BIZNESOWEJ]
                                                               - Relacja opiekun-pacjent → personalizacja rozmów
                                                               - Diagnoza, stadium choroby → dopasowanie porad
                                                               - Samodzielność pacjenta → planowanie opieki
                                                               - Zachowania problemowe → skuteczne interwencje
                                                               - Co uspokaja pacjenta → sprawdzone strategie
                                                               - Obciążenie opiekuna → zapobieganie wypaleniu
                                                               - Sieć wsparcia → wykrywanie izolacji
                                                               - Warunki mieszkaniowe → ocena bezpieczeństwa
            
                                                               [NIE WYCIĄGAJ]
                                                               - Ogólnych porad medycznych
                                                               - Planów i propozycji
                                                               - Chwilowych emocji
                                                               - Tematów niezwiązanych z opieką
            
                                                               [TAGI]
                                                               relationship_to_patient, medical_condition, daily_functioning, routines_and_preferences, support_network, living_situation, caregiver_situation, behavioral_issues, ward, caregiver
            
                                                               [FORMAT WYJŚCIA]
                                                               Tablica JSON. Pierwszy znak: [ Ostatni znak: ]
                                                               Każdy element: {"tags":"[tag1, tag2]","value":"fakt"}
            
                                                               [PRZYKŁAD A]
                                                               Rozmowa: "Mama ma Alzheimera, mieszkamy razem. Wieczorem jest niespokojna, muzyka ją uspokaja. Jestem jedyną opiekunką."
                                                               [{"tags":"[relationship_to_patient, caregiver]","value":"Opiekun jest córką pacjentki"},{"tags":"[medical_condition, ward]","value":"Pacjentka choruje na Alzheimera"},{"tags":"[living_situation]","value":"Opiekun mieszka z pacjentką"},{"tags":"[behavioral_issues, ward]","value":"Pacjentka jest niespokojna wieczorami"},{"tags":"[routines_and_preferences, ward]","value":"Muzyka uspokaja pacjentkę"},{"tags":"[support_network, caregiver]","value":"Opiekun nie ma wsparcia, opiekuje się sam"}]
            
                                                               [PRZYKŁAD B]
                                                               Rozmowa: "Tata nie poznaje mnie od miesiąca. Pracuję i mam dwójkę dzieci."
                                                               [{"tags":"[relationship_to_patient, caregiver]","value":"Opiekun jest dzieckiem pacjenta"},{"tags":"[daily_functioning, ward]","value":"Pacjent nie rozpoznaje bliskich od miesiąca"},{"tags":"[caregiver_situation]","value":"Opiekun pracuje zawodowo"},{"tags":"[caregiver_situation]","value":"Opiekun ma dwoje dzieci"}]
            
                                                               [PRZYKŁAD C]
                                                               Rozmowa: "Dziękuję za informacje."
                                                               []
            
                                                               [ODPOWIEDŹ - TYLKO JSON ARRAY]
 """;
    // =========================================================================
    // Generic Intervention Prompt (when no scenario matched)
    // =========================================================================

    private static final String GENERIC_INTERVENTION_PROMPT_TEMPLATE =
            """
            Jesteś asystentem „Wsparcie Psychiczne Opiekuna" - dbasz o dobrostan psychiczny opiekuna osoby z demencją.
            ## Twój priorytet
    Koncentrujesz się WYŁĄCZNIE na tym, jak czuje się opiekun, z czym się zmaga i jak może zadbać o siebie psychicznie.
            ## Dostępny kontekst
    {facts_context}
## Kluczowe zasady
1. **Wykorzystuj RAG** - bazuj na sprawdzonych metodach psychologicznych z bazy wiedzy (psychoedukacja, CBT, ACT, techniki regulacji emocji, profilaktyka wypalenia)
2. **Normalizuj uczucia** - zmęczenie, złość, bezradność, smutek, wina są normalne w roli opiekuna
3. **Psychoedukacja**:
            - Przeciążenie/wypalenie opiekuna
   - Żałoba antycypacyjna
   - Wpływ stresu przewlekłego
4. **Dawaj mikrokroki**:
            - Konkretne, małe sposoby dbania o siebie
   - Budowanie sieci wsparcia
   - Planowanie odpoczynku
5. **Wzmacniaj samoempatię**:
            - "Dobry opiekun" ≠ "idealny opiekun"
            - Prawo do błędów i granic
## BEZPIECZEŃSTWO - sytuacje alarmowe
    Jeśli opiekun mówi o:
            - Myślach samobójczych
- "Życie nie ma sensu"
        - Konkretnych planach krzywdzenia siebie
- Skrajnym wyczerpaniu
    NATYCHMIAST:
            1. Uznaj empatycznie ("to brzmi bardzo poważnie")
2. Wskaż potrzebę PILNEGO kontaktu ze specjalistą (psycholog/psychiatra/pogotowie)
3. Zachęć do zwrócenia się do zaufanej osoby
    NIE bagatelizuj sygnałów samobójczych.
            ## Czego NIE robisz
- Nie diagnozujesz klinicznie ("masz depresję")
- Nie prowadzisz psychoterapii
- Nie doradzasz w leczeniu pacjenta (to rola innych agentów)
## Styl wypowiedzi
- **Polski**, ciepły, autentyczny
- Unikaj: "powinieneś", "musisz"
            - Używaj: "Możesz spróbować...", "Wielu opiekunom pomaga..."
            - Konkret zamiast ogólników
## Struktura odpowiedzi
1. **Odzwierciedlenie uczuć** ("Słyszę dużo zmęczenia...")
            2. **Psychoedukacja z RAG** ("W materiałach dla opiekunów podkreśla się...")
            3. **2-4 konkretne kroki** - co może zrobić dziś/ten tydzień
4. **Przypomnienie** - opiekun też ma prawo dbać o siebie
5. **(Jeśli potrzeba)** Sygnał o kontakcie ze specjalistą
    Mów PO POLSKU. Bądź konkretny, empatyczny i oparty na sprawdzonych metodach.""";

    // =========================================================================
    // Public Methods
    // =========================================================================

    /**
     * Get the survey system prompt.
     */
    public String buildSurveyPrompt() {
        return SURVEY_PROMPT;
    }

    /**
     * Build the support module prompt with profile and facts context.
     */
    public String buildSupportPrompt(List<Fact> facts) {
        String factsContext = formatFactsContext(facts);

        return SUPPORT_PROMPT_TEMPLATE
                .replace("{facts_context}", factsContext);
    }

    /**
     * Build the intervention prompt using scenario's system prompt.
     */
    public String buildInterventionPrompt(List<Fact> facts, CrisisScenario scenario) {
        String factsJson = formatFactsAsJson(facts);

        return scenario.systemPrompt
                .replace("{facts_json}", factsJson);
    }

    /**
     * Build a generic intervention prompt when no scenario matched.
     */
    public String buildGenericInterventionPrompt(List<Fact> facts, String situationDescription) {
        String factsContext = formatFactsContext(facts);

        return GENERIC_INTERVENTION_PROMPT_TEMPLATE
                .replace("{facts_context}", factsContext)
                .replace("{situation_description}", situationDescription);
    }

    /**
     * Build the facts distiller prompt.
     */
    public String buildFactsDistillerPrompt(String transcript, List<Fact> existingFacts) {
        String existingFactsStr = formatFactsForDistiller(existingFacts);

        return FACTS_DISTILLER_PROMPT_TEMPLATE
                .replace("{existing_facts}", existingFactsStr)
                .replace("{transcript}", transcript);
    }

    private String formatFactsContext(List<Fact> facts) {
        if (facts == null || facts.isEmpty()) {
            return "Brak zarejestrowanych faktów";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Fact fact : facts) {
            if (count >= 10) { // Limit to 10 most recent facts
                sb.append("... i ").append(facts.size() - 10).append(" więcej\n");
                break;
            }
            sb.append("- [").append(fact.tags).append("] ").append(fact.factValue);
            if (fact.severity != null) {
                sb.append(" (poziom: ").append(fact.severity).append("/10)");
            }
            sb.append("\n");
            count++;
        }

        return sb.toString();
    }

    private String formatFactsAsJson(List<Fact> facts) {
        if (facts == null || facts.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(
                    facts.stream()
                            .map(f -> Map.of(
                                    "type", f.tags != null ? f.tags : "",
                                    "value", f.factValue != null ? f.factValue : "",
                                    "severity", f.severity != null ? f.severity : 0
                            ))
                            .toList()
            );
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String formatFactsForDistiller(List<Fact> facts) {
        if (facts == null || facts.isEmpty()) {
            return "Brak wcześniejszych faktów";
        }

        StringBuilder sb = new StringBuilder();
        for (Fact fact : facts) {
            sb.append("- [").append(fact.tags).append("] ").append(fact.factValue).append("\n");
        }
        return sb.toString();
    }
}
