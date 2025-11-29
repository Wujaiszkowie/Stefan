package com.wspiernik.infrastructure.lifecycle;

import com.wspiernik.infrastructure.persistence.entity.CrisisScenario;
import com.wspiernik.infrastructure.persistence.repository.BackendLogRepository;
import com.wspiernik.infrastructure.persistence.repository.CrisisScenarioRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes database with seed data on application startup.
 * Seeds the 3 MVP crisis scenarios if they don't already exist.
 */
@ApplicationScoped
public class DataInitializer {

    private static final Logger LOG = Logger.getLogger(DataInitializer.class);
    private static final String MODULE = "STARTUP";

    @Inject
    CrisisScenarioRepository scenarioRepository;

    @Inject
    BackendLogRepository logRepository;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        LOG.info("DataInitializer: Starting database initialization...");

        // Collect all scenarios to create
        List<CrisisScenario> scenariosToCreate = new ArrayList<>();

        if (!scenarioRepository.existsByScenarioKey("fall")) {
            scenariosToCreate.add(createFallScenario());
        }

        if (!scenarioRepository.existsByScenarioKey("confusion")) {
            scenariosToCreate.add(createConfusionScenario());
        }

        if (!scenarioRepository.existsByScenarioKey("chest_pain")) {
            scenariosToCreate.add(createChestPainScenario());
        }

        // Persist all at once to avoid SQLite locking issues
        if (!scenariosToCreate.isEmpty()) {
            for (CrisisScenario scenario : scenariosToCreate) {
                scenarioRepository.persist(scenario);
                LOG.infof("Created scenario: %s (%s)", scenario.name, scenario.scenarioKey);
            }
            LOG.infof("DataInitializer: Created %d crisis scenarios", scenariosToCreate.size());
        } else {
            LOG.info("DataInitializer: All scenarios already exist, skipping initialization");
        }

        LOG.infof("DataInitializer: Total scenarios in database: %d", scenarioRepository.count());
    }

    private CrisisScenario createFallScenario() {
        CrisisScenario scenario = new CrisisScenario();
        scenario.scenarioKey = "fall";
        scenario.name = "Upadek";
        scenario.triggerKeywords = """
                ["upadek", "upadł", "upadła", "przewrócił", "przewróciła", "spadł", "spadła", "potknął", "potknęła", "wywrócił", "leży na ziemi", "leży na podłodze"]
                """.trim();
        scenario.questionsSequence = """
                ["Czy podopieczny stracił przytomność?", "Gdzie dokładnie boli?", "Czy może ruszać rękami i nogami?", "Czy jest krwawienie?", "Jaki jest teraz stan świadomości?"]
                """.trim();
        scenario.systemPrompt = """
                Ty jesteś asystentem wspomagającym opiekuna w sytuacji upadku podopiecznego.

                Profil podopiecznego: {profile_json}
                Znane fakty: {facts_json}

                Zadaj pytania aby zrozumieć:
                1. Czy strata przytomności?
                2. Gdzie boli? (szczegółowo)
                3. Czy może ruszać ręce i nogi?
                4. Czy jest krwawienie?
                5. Stan świadomości teraz?

                Po zebraniu informacji, zadaj pytania o bieżące objawy. Bądź konkretny i rzeczowy.
                Po zebraniu danych zasugeruj działanie.

                WAŻNE:
                - Zadawaj jedno pytanie na raz
                - Słuchaj uważnie odpowiedzi
                - Jeśli coś jest niejasne, dopytaj
                - Bądź spokojny i wspierający
                - Po zakończeniu zbierania informacji, powiedz "INTERVENTION_COMPLETE" i przedstaw podsumowanie
                """.trim();
        scenario.createdAt = LocalDateTime.now();
        return scenario;
    }

    private CrisisScenario createConfusionScenario() {
        CrisisScenario scenario = new CrisisScenario();
        scenario.scenarioKey = "confusion";
        scenario.name = "Zamieszanie Umysłowe";
        scenario.triggerKeywords = """
                ["zamieszanie", "dezorientacja", "zdezorientowany", "zdezorientowana", "nie poznaje", "mówi bez sensu", "nonsens", "majaczenie", "majaczy", "nie wie gdzie jest", "nie wie kim jest", "zagubiony", "zagubiona", "splątany", "splątana"]
                """.trim();
        scenario.questionsSequence = """
                ["Od kiedy zauważyłeś/aś zmianę?", "Czy podopieczny poznaje Cię?", "Czy mówi rzeczy bez sensu?", "Czy jest niespokojny lub agresywny?", "Czy miał dzisiaj gorączkę?"]
                """.trim();
        scenario.systemPrompt = """
                Ty jesteś asystentem wspomagającym opiekuna w sytuacji zamieszania umysłowego podopiecznego.

                Profil podopiecznego: {profile_json}
                Znane fakty: {facts_json}

                Zadaj pytania aby zrozumieć:
                1. Od kiedy zauważono zmianę?
                2. Czy podopieczny poznaje opiekuna?
                3. Czy mówi rzeczy bez sensu?
                4. Czy jest niespokojny lub agresywny?
                5. Czy miał gorączkę?

                Pamiętaj o kontekście medycznym podopiecznego. Bądź konkretny i rzeczowy.

                WAŻNE:
                - Zadawaj jedno pytanie na raz
                - Słuchaj uważnie odpowiedzi
                - Jeśli coś jest niejasne, dopytaj
                - Bądź spokojny i wspierający
                - Zamieszanie umysłowe może być oznaką poważnego stanu - sugeruj kontakt z lekarzem
                - Po zakończeniu zbierania informacji, powiedz "INTERVENTION_COMPLETE" i przedstaw podsumowanie
                """.trim();
        scenario.createdAt = LocalDateTime.now();
        return scenario;
    }

    private CrisisScenario createChestPainScenario() {
        CrisisScenario scenario = new CrisisScenario();
        scenario.scenarioKey = "chest_pain";
        scenario.name = "Ból w Klatce Piersiowej";
        scenario.triggerKeywords = """
                ["ból w klatce", "ból serca", "boli serce", "klatka piersiowa", "duszność", "dusi się", "nie może oddychać", "ściska w klatce", "ucisk w klatce", "boli przy oddychaniu", "kłucie w sercu"]
                """.trim();
        scenario.questionsSequence = """
                ["Jak silny jest ból w skali 1-10?", "Czy ból promieniuje do ramienia lub szczęki?", "Czy występuje duszność?", "Czy są nudności lub wymioty?", "Czy podopieczny jest spocony?"]
                """.trim();
        scenario.systemPrompt = """
                Ty jesteś asystentem wspomagającym opiekuna w sytuacji bólu w klatce piersiowej podopiecznego.

                UWAGA: Ból w klatce piersiowej może być objawem zawału serca. To jest sytuacja potencjalnie zagrażająca życiu!

                Profil podopiecznego: {profile_json}
                Znane fakty: {facts_json}

                Zadaj pytania aby zrozumieć:
                1. Jak silny jest ból (skala 1-10)?
                2. Czy ból promieniuje do ramienia, szczęki lub pleców?
                3. Czy występuje duszność?
                4. Czy są nudności lub wymioty?
                5. Czy podopieczny jest spocony?

                WAŻNE:
                - Zadawaj jedno pytanie na raz
                - Słuchaj uważnie odpowiedzi
                - Jeśli objawy wskazują na zawał (silny ból + promieniowanie + duszność + poty), NATYCHMIAST zalecaj wezwanie pogotowia (112)
                - Bądź spokojny ale stanowczy
                - Po zakończeniu zbierania informacji, powiedz "INTERVENTION_COMPLETE" i przedstaw podsumowanie z zaleceniami
                """.trim();
        scenario.createdAt = LocalDateTime.now();
        return scenario;
    }
}
