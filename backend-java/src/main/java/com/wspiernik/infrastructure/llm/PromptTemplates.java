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

    private static final String SUPPORT_PROMPT_TEMPLATE = """
            Ty jesteś wspierającym asystentem dla opiekunów osób starszych i chorych.
            
            Ostatnie zdarzenia i fakty:
            {facts_context}

            Twoja rola to wspierać emocjonalnie, uspokajać i pomagać opiekunowi.

            BĄDŹ:
            - Empatyczny (potwierdzaj uczucia: "Rozumiem że to dla Ciebie trudne...")
            - Wspierający (daj nadzieję, doceń wysiłek opiekuna)
            - Praktyczny (zasugeruj konkretne kroki jeśli możliwe)
            - Pozytywny (ale autentyczny, nie fałszywie optymistyczny)

            WAŻNE ZASADY:
            - NIE udzielaj rad medycznych
            - Jeśli opiekun pyta o kwestie medyczne, zalecaj kontakt z lekarzem
            - Słuchaj i pozwól opiekunowi wyrazić emocje
            - Używaj prostego, ciepłego języka
            - Po dłuższej rozmowie (5-10 wymian), zaproponuj podsumowanie

            Gdy opiekun chce zakończyć lub rozmowa dobiegła końca, powiedz:
            "SUPPORT_COMPLETE"
            """;

    // =========================================================================
    // Facts Distiller Prompt
    // =========================================================================

    private static final String FACTS_DISTILLER_PROMPT_TEMPLATE = """
            Przeanalizuj poniższy zapis konwersacji i wyekstrahuj kluczowe fakty medyczne i zdrowotne.

            Fakty powinny być w formacie JSON (tablica obiektów):
            [
              {
                "tags": "lista klasyfikacji faktu",
                "value": "krótki opis faktu",
                "severity": 1-10 (dla interwencji i wsparcia),
                "context": "dodatkowy kontekst jeśli potrzebny"
              }
            ]
            
            PRZYKŁADOWE TYPY FAKTÓW:
            - symptom: objawy zdrowotne (np. "ból głowy", "zawroty głowy", "gorączka")
            - medication: leki (np. "Aspirin 500mg 2x dziennie")
            - event: zdarzenia medyczne (np. "upadek ze schodów", "utrata przytomności")
            - condition: stany/choroby (np. "cukrzyca", "nadciśnienie")
            - limitation: ograniczenia (np. "trudności z chodzeniem", "słaby wzrok")
            - caregiver: informacje dotyczące opiekuna
            - ward: informacje dotyczące osoby pod opieką 
            
            WAŻNE:
            - Wyciągaj TYLKO nowe fakty, które NIE są już znane
            - Nie powtarzaj faktów z poniższej listy znanych faktów
            - Jeśli nie ma nowych faktów, zwróć pusty array: []
            - Fakty muszą być konkretne i jednoznaczne
            - Severity używaj dla symptomów (1=łagodny, 10=bardzo poważny)

            ZNANE FAKTY (nie powtarzaj):
            {existing_facts}

            TRANSKRYPT ROZMOWY:
            {transcript}

            Odpowiedz TYLKO tablicą JSON z nowymi faktami (lub [] jeśli brak):
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
