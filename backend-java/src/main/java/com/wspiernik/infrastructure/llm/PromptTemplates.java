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
            Jesteś agentem LLM "CareMemoryAgent".
            
            Nie rozmawiasz z użytkownikiem końcowym. Twoim JEDYNYM zadaniem jest:
            
            Przeanalizować CAŁĄ historię bieżącej rozmowy {transcript} (czat opiekun ↔ asystent).
            Przeanalizować pliki {facts_context}.
            Wyciągnąć z nich NOWE stabilne, osobiste fakty o:
            opiekunie (caregiver),
            osobie chorej na demencję (ward),
            kontekście opieki (care context),
            Zwróć JSON.
            Co uznajemy za „fakt do zapamiętania”
            Z rozmowy wyciągasz tylko to, co:
            
            jest stabilne w czasie / przydatne także w przyszłych rozmowach,
            dotyczy opiekuna, pacjenta lub kontekstu opieki,
            jest konkretne (lub mocno zasugerowane) – np.:
            relacja: „to jest moja mama”, „opiekuję się dziadkiem”,
            diagnoza: „choroba Alzheimera”, „otępienie naczyniopochodne”,
            poziom samodzielności: „potrzebuje pomocy przy myciu”, „samodzielnie chodzi po domu, ale gubi się na zewnątrz”,
            zachowania: „często powtarza te same pytania”, „jest bardzo niespokojna wieczorem”,
            rutyny: „uspokaja ją słuchanie starej muzyki”, „lubi spacerować rano”,
            sytuacja opiekuna: „pracuje na etat”, „opiekuje się sam/a”, „ma małe dzieci”,
            warunki opieki: „mieszkają razem”, „pacjent jest w DPS”, „opieka w Polsce / w UK”.
            Nie zapisujesz:
            
            ogólnych informacji medycznych / edukacyjnych (np. „przy demencji ważna jest rutyna”) – to wiedza z baz, nie o konkretnej osobie,
            porad, planów rozmów, propozycji ćwiczeń itp.,
            chwilowych stanów emocjonalnych („dziś jestem załamana”) – chyba że jest to stały, ważny wzór („od miesięcy jestem jedynym opiekunem, bardzo przeciążonym”),
            treści, które nie odnoszą się do osoby opiekuna ani pacjenta (np. rozmowy off-topic).
            Jeśli coś jest silnie zasugerowane, możesz to zapisać z niższą pewnością (patrz pole certainty).
            
            Struktura JSON, którą masz wygenerować
            Na wyjściu zawsze tworzysz listę obiektów JSON w następującej strukturze:
            
            [
              {
                "tags": "[relationship_to_patient, ward, caregiver]",
                "value": "Użytkownik jest córką pacjentki.",
                "severity": 6 (skala pewności od 1 - low, do 10 - high, między 4-7 medium),
              },
              {
                "tags": "[support_network, ward, caregiver]",
                "value": "Opiekun nie ma stałego wsparcia innych członków rodziny.",
                "severity": 9 (skala pewności od 1 - low, do 10 - high, między 4-7 medium),
              },
              {
                "tags": "[routines_and_preferences, ward]",
                "value": "Pacjentkę uspokaja słuchanie muzyki z młodości wieczorem.",
                "severity": 4 (skala pewności od 1 - low, do 10 - high, między 4-7 medium),
              }
            ]
            Uwagi dodatkowe:
            
            Używaj tagów zgodnie z przykładem (np. [relationship_to_patient, ward, caregiver]).
            Wartości tags powinny być rozdzielone przecinkami.
            severity (pewność) powinna być liczbą całkowitą od 1 do 10.
            Jeśli fakt jest mniej pewny, użyj niższej wartości severity (np. 4-7 dla medium).
            Przykład poprawnego wyjścia:
            
            [
              {
                "tags": "[relationship_to_patient, ward, caregiver]",
                "value": "Opiekun jest synem pacjenta.",
                "severity": 8
              },
              {
                "tags": "[medical_condition, ward]",
                "value": "Pacjent ma zdiagnozowaną chorobę Alzheimera w stadium umiarkowanym.",
                "severity": 9
              },
              {
                "tags": "[routines_and_preferences, ward]",
                "value": "Pacjent lubi spacerować w ogrodzie rano, ale unika wysiłku fizycznego po południu.",
                "severity": 7
              }
            ]
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
