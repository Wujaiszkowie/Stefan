Product Requirements Document (PRD)
Wspiernik — MVP
Wersja: 1.0 Data: 29 listopada 2025 Status: PRD dla MVP

1. Executive Summary
   [translate:Wspiernik] to cyfrowy asystent dla opiekunów osób starszych i podopiecznych z problemami zdrowotnymi lub mentalnymi. MVP dostarczy 4 kluczowe moduły działające lokalnie w aplikacji webowej z backendem webSocket, zapewniając intuicyjne narzędzie do obsługi sytuacji kryzysowych, gromadzenia danych zdrowotnych i wsparcia emocjonalnego opiekuna.

2. Problem Statement
   Opiekunowie doświadczają kilku kluczowych wyzwań:
   Brak strukturyzowanego wsparcia w kryzysie — w sytuacji nagłej nie wiedzą, jakie informacje zebrać ani jak działać
   Rozproszenie informacji o podopiecznym — dane zdrowotne rozłożone są na karteczkach, notatkach, SMS-ach
   Obciążenie psychiczne — opiekun funkcjonuje w stresie bez dedykowanego wsparcia emocjonalnego
   Ryzyko błędnych decyzji — bez kontekstu i historii sytuacji trudniej podjąć właściwą decyzję

3. Misja i Wartość Biznesowa
   Misja
   Dostarczyć prosty, lokalny, intuicyjny asystent wspomagający opiekunów w reagowaniu na sytuacje kryzysowe, wspierający ich emocjonalnie oraz gromadzący uporządkowane informacje o stanie zdrowia podopiecznego.
   Wartość Biznesowa
   ✅ Zwiększa bezpieczeństwo podopiecznych dzięki ustrukturyzowanemu zbieraniu informacji w kryzysie
   ✅ Odciąża psychicznie opiekunów poprzez dedykowany moduł wsparcia emocjonalnego
   ✅ Zapewnia gromadzenie i destylację faktów zdrowotnych dla lepszego monitorowania
   ✅ Zachowuje pełną prywatność — wszystkie dane i modele działają lokalnie
   ✅ Prostota i lokalność zmniejszają bariery adopcji

4. Zakres MVP
   Moduły Zainkludowane
   Ankieta Wstępna (Initial Survey) — zbieranie danych startowych podopiecznego
   Interwencja (Caretaker Help) — obsługa sytuacji kryzysowej z użyciem predefiniowanych scenariuszy
   Destylacja Faktów (Facts Distiller) — ekstrakcja i tagowanie kluczowych danych z rozmów
   Wsparcie dla Opiekuna (Caretaker Support) — wsparcie emocjonalne opiekuna w stresie
   Moduły Wyłączone (Phase 2+)
   Moduł Nadzoru (Overseer) — walidacja rad
   Cykliczne Kontrole Stanu Opiekuna — proaktywne push notyfikacje
   Customizacja scenariuszy per-podopieczny
   Bez szyfrowania i multi-user authentication
   Mobile app

5. Architektura Systemu
   Stack Techniczny
   Backend: Docker container z lokalnym [translate:Bielnikiem] (LLM universal model)
   Frontend: Web app z komunikacją WebSocket
   Baza danych: SQLite (lokalna, no-sync)
   Logging: Backend logger na poziomie INFO/ERROR
   Przepływ Danych
   ┌─────────────────────────────────────────────────────────┐ │ Frontend (Web App - WebSocket) │ └────────────┬────────────────────────────────────────────┘ │ WebSocket Messages ▼ ┌─────────────────────────────────────────────────────────┐ │ Backend API (java 21, quarkus) │ │ - Conversation Manager │ │ - Facts Extraction Thread │ │ - LLM Orchestrator │ └────────────┬────────────────────────────────────────────┘ │ SQL Queries ▼ ┌─────────────────────────────────────────────────────────┐ │ SQLite Database (Local) │ │ - conversations │ │ - facts │ │ - caregiver_profile │ │ - crisis_scenarios │ │ - caregiver_support_logs │ └─────────────────────────────────────────────────────────┘ ▲ │ LLM Queries (same container) │ ┌─────────────────────────────────────────────────────────┐ │ Docker Container │ │ - Bielnik LLM Model │ │ - System Prompts Library │ └─────────────────────────────────────────────────────────┘

6. Moduły Funkcjonalne
   6.1 Ankieta Wstępna (Initial Survey)
   Cel: Zebranie danych startowych o podopiecznym
   Aktywność:
   Opiekun uruchamia Ankietę
   System zadaje sekwencję pytań (conversational, nie formularz)
   Zbiera: wiek, główne choroby, bieżące symptomy, leki, ograniczenia ruchowe
   Dane zapisywane do tabeli caregiver_profile
   Dane wyjściowe:
   Profil podopiecznego w bazie (jednokrotnie, reużywane w innych modułach)
   Te dane będą dostępne w kontekście Interwencji i Wsparcia
   Zakres MVP:
   Minimum 8-10 pytań essentialnych
   Conversational flow (nie questionnaire)
   Brak edycji na MVP

6.2 Interwencja (Caretaker Help)
Cel: Obsługa sytuacji kryzysowej poprzez predefiniotany scenariusz + zbieranie dodatkowych informacji
Przepływ:
Opiekun zgłasza sytuację kryzysową (wolny tekst lub wybór z listy scenario keywords)
System dopasuje scenariusz z bazy (3 predefiniotane na MVP)
Uruchamia conversational interview na temat sytuacji
Zbiera informacje w kontekście profilu podopiecznego
Po zakończeniu rozmowy → asynchroniczny wątek Destylacji Faktów
3 Predefiniotane Scenariusze MVP:
Upadek — czy strata przytomności, gdzie boli, czy rusza ręce/nogi
Zamieszanie Umysłowe — desorientacja, nonsensowne wypowiedzi, niepokój
Ból w Klatce Piersiowej — intensywność, irradiacja, towarzyszące symptomy
Dane wyjściowe:
Record w tabeli conversations (id, timestamp, caregiver_id, scenario_type, raw_transcript)
Asynchroniczny trigger do modułu Destylacji Faktów
Zakres MVP:
Proste keyword matching do scenariusza (brak NLP na etapie matchowania)
Conversational flow per scenariusz
Transcript zapisywany w raw_transcript

6.3 Destylacja Faktów (Facts Distiller)
Cel: Ekstrakcja kluczowych danych ze stenogramu konwersacji, tagowanie i zapis w bazie
Przepływ (asynchroniczny wątek):
Po zakończeniu rozmowy (Interwencja, Wsparcie, Ankieta) → trigger do Facts Distiller
Bielnik LLM analizuje raw_transcript
Wyciąga kluczowe fakty: {tags, person, value, context, severity, timestamp}
Porównuje z istniejącymi faktami w bazie
Jeśli nowe fakty → dodaje do tabeli facts
Jeśli duplikaty/brak zmian → nic nie dodaje
Przykłady Faktów:
{tags: ["symptom"], person:”ward”, value: "ból głowy", severity: 7, timestamp: "..."}
{tags: ["medication"], person:”ward”, value: "Aspirin 500mg", frequency: "2x dziennie"}
{tags: ["event"," danger"], person:”ward”, value: "upadek ze schodów", severity: 8, timestamp: "..."}
{tags: ["event","needs_support"], person:”caregiver”, value: "potrzeba inwerwencji", severity: 7, timestamp: "..."}
Dane wyjściowe:
Nowe rekordy w tabeli facts (tylko jeśli ekstrakcja dostarczy wartość)
Link do conversation_id
Tagging umożliwiający przeszukiwanie
Zakres MVP:
Post-conversation processing (nie real-time)
Asynchroniczny wątek, nie blokuje UI
Dedykowany system prompt dla Destylacji (jasne instrukcje dla LLM)
Brak ręcznej edycji faktów na MVP

6.4 Wsparcie dla Opiekuna (Caretaker Support)
Cel: Wspieranie emocjonalne opiekuna w momentach stresu
Przepływ:
Opiekun inicjuje moduł Wsparcia (button w UI)
Na wstępie system wczytuje fakty.
System wita opiekuna empatycznie, pytając o bieżące samopoczucie
Conversational support — uspokajanie, poradnictwo, zmotywowanie
Po rozmowie → asynchroniczny wątek Destylacji Faktów
Tone Voice:
Empatyczny, wspierający
Język prosty, bez medycznego żargonu
Potwierdzanie uczuć opiekuna ("Rozumiem że to dla Ciebie trudne...")
Zaproponowanie konkretnych kroków/porad
Dane wyjściowe:
Record w conversations (type: "support")
Potential fakty ekstrakcje: obciążenie opiekuna, stres level, potrzeby pomocy
Zapis w caregiver_support_logs (dla Phase 2 — cykliczne monitorowanie)
Zakres MVP:
Reaktywny model (opiekun inicjuje, nie push notifications)
Conversational, 5-10 min rozmowa
Brak systemowych check-insów (push proaktywne na Phase 2)

7. Schema Bazy Danych (SQLite)
   -- Profil opiekuna i podopiecznego CREATE TABLE caregiver_profile ( id INTEGER PRIMARY KEY, caregiver_id TEXT UNIQUE, ward_age INTEGER, ward_conditions TEXT, -- JSON array: ["hypertension", "diabetes", ...] ward_medications TEXT, -- JSON array: [{name, dose, frequency}, ...] ward_mobility_limits TEXT, -- JSON created_at TIMESTAMP, updated_at TIMESTAMP );
   -- Historia rozmów CREATE TABLE conversations ( id INTEGER PRIMARY KEY, caregiver_id TEXT, conversation_type TEXT, -- "survey", "intervention", "support" scenario_type TEXT, -- "fall", "confusion", "chest_pain" (nullable for support/survey) raw_transcript TEXT, started_at TIMESTAMP, ended_at TIMESTAMP, facts_extracted BOOLEAN DEFAULT FALSE, created_at TIMESTAMP );
   -- Wyekstrahowane fakty CREATE TABLE facts ( id INTEGER PRIMARY KEY, conversation_id INTEGER, fact_type TEXT, -- "symptom", "medication", "event", "condition", "limitation" fact_value TEXT, severity INTEGER, -- 1-10 (nullable) context TEXT, -- dodatkowy kontekst extracted_at TIMESTAMP, created_at TIMESTAMP, FOREIGN KEY(conversation_id) REFERENCES conversations(id) );
   -- Predefiniotane scenariusze CREATE TABLE crisis_scenarios ( id INTEGER PRIMARY KEY, scenario_key TEXT UNIQUE, -- "fall", "confusion", "chest_pain" name TEXT, trigger_keywords TEXT, -- JSON array questions_sequence TEXT, -- JSON array of questions system_prompt TEXT, -- prompt dla LLM created_at TIMESTAMP );
   -- Logi wsparcia opiekuna (dla Phase 2) CREATE TABLE caregiver_support_logs ( id INTEGER PRIMARY KEY, caregiver_id TEXT, conversation_id INTEGER, stress_level INTEGER, -- 1-10 (extracted) needs TEXT, -- JSON created_at TIMESTAMP, FOREIGN KEY(conversation_id) REFERENCES conversations(id) );
   -- Backend logs CREATE TABLE backend_logs ( id INTEGER PRIMARY KEY, timestamp TIMESTAMP, level TEXT, -- "INFO", "WARNING", "ERROR" module TEXT, message TEXT, details TEXT -- JSON );

8. API WebSocket (Specyfikacja)
   8.1 Message Format
   { "type": "string", // "survey_start", "intervention_start", "support_start", "message", "get_facts", "get_profile" "payload": {}, // dane specificzne dla typu "request_id": "string" // dla tracking i responses }
   8.2 Endpoints (WebSocket Events)
   Survey Module
   // Client → Server: Start survey { "type": "survey_start", "request_id": "req_123" }
   // Server → Client: First question { "type": "survey_question", "payload": { "question": "Ile lat ma Twój podopieczny?", "step": 1 } }
   // Client → Server: Answer { "type": "survey_message", "payload": { "text": "78 lat" }, "request_id": "req_123" }
   // Server → Client: Next question or completion { "type": "survey_question", "payload": { "question": "Jakie ma główne choroby?", "step": 2 } }
   // Client → Server: Finish survey { "type": "survey_complete", "request_id": "req_123" }
   // Server → Client: Confirmation { "type": "survey_completed", "payload": { "profile_id": "prof_001", "facts_saved": 5 } }
   Intervention Module
   // Client → Server: Start intervention { "type": "intervention_start", "payload": { "scenario_description": "Upadł z łóżka" }, "request_id": "req_456" }
   // Server → Client: Matched scenario { "type": "intervention_scenario_matched", "payload": { "scenario_key": "fall", "scenario_name": "Upadek" } }
   // Server → Client: First question { "type": "intervention_question", "payload": { "question": "Czy podopieczny stracił przytomność?", "step": 1 } }
   // Client → Server: Answer { "type": "intervention_message", "payload": { "text": "Tak, przez kilka sekund" }, "request_id": "req_456" }
   // ... more Q&A ...
   // Client → Server: Finish intervention { "type": "intervention_complete", "request_id": "req_456" }
   // Server → Client: Confirmation + Facts Extraction in Progress { "type": "intervention_completed", "payload": { "conversation_id": "conv_789", "facts_extraction": "processing" } }
   // Server → Client: Facts Extracted (async notification after extraction completes) { "type": "facts_extracted", "payload": { "conversation_id": "conv_789", "facts_count": 3, "facts": [...] } }
   Support Module
   // Client → Server: Start support { "type": "support_start", "request_id": "req_789" }
   // Server → Client: Opening message { "type": "support_message", "payload": { "text": "Wiem że teraz jest trudno. Chciałbym Ci pomóc. Jak się teraz czujesz?" } }
   // Client → Server: Response { "type": "support_message", "payload": { "text": "Jestem bardzo zmęczony i zaniepokojony" }, "request_id": "req_789" }
   // Server → Client: Empathetic response { "type": "support_message", "payload": { "text": "Rozumiem Twoje obawy. To normalne w takiej sytuacji. Czy chciałbyś powiedzieć mi więcej?" } }
   // ... more conversation ...
   // Client → Server: Finish support { "type": "support_complete", "request_id": "req_789" }
   // Server → Client: Completion { "type": "support_completed", "payload": { "conversation_id": "conv_101", "facts_extraction": "processing" } }
   Facts Query
   // Client → Server: Get all facts { "type": "get_facts", "payload": { "limit": 20 }, "request_id": "req_facts_1" }
   // Server → Client: Facts list { "type": "facts_list", "payload": { "facts": [...], "total_count": 42 } }
   Profile Query
   // Client → Server: Get profile { "type": "get_profile", "request_id": "req_prof_1" }
   // Server → Client: Profile data { "type": "profile_data", "payload": { "ward_age": 78, "ward_conditions": [...], "ward_medications": [...] } }

9. System Prompts (LLM Orchestration)
   9.1 Survey Prompt
   Ty jesteś asystentem do zbierania informacji zdrowotnych. Musisz dowiedzieć się o podopiecznym:
   Wiek
   Główne choroby
   Bieżące leki
   Ograniczenia ruchowe
   Inne istotne warunki
   Pytaj jeden temat na raz. Słuchaj uważnie i zadaj pytania follow-up jeśli odpowiedź jest niejasna. Bądź empatyczny i wspierający. Po zebraniu wszystkich infomacji, powiedz "SURVEY_COMPLETE".
   9.2 Intervention Prompt (per scenario)
   Scenario: FALL
   Ty jesteś asystentem wspomagającym opiekuna w sytuacji upadku podopiecznego. Profil podopiecznego: {profile_json}
   Zadaj pytania aby zrozumieć:
   Czy strata przytomności?
   Gdzie boli? (szczegółowo)
   Czy może ruszać ręce i nogi?
   Czy jest krwawienie?
   Stan świadomości teraz?
   Po zebraniu informacji, zadaj pytania o bieżące objawy. Bądź konkretny i rzeczowy. Po zebraniu danych zasugeruj działanie. Po zakończeniu, powiedz "INTERVENTION_COMPLETE".
   9.3 Support Prompt
   Ty jesteś wspierającym asystentem dla opiekunów. Twoja rola to wspierać emocjonalnie, uspokajać i pomagać.
   Bądź:
   Empatyczny (potwierdzaj uczucia)
   Wspierający (daj nadzieję)
   Praktyczny (zasugeruj kroki jeśli możliwe)
   Pozytywny (ale autentyczny)
   Nie udzielaj rad medycznych. Jeśli opiekun pyta o medycynę, zalecaj kontakt z lekarzem. Słuchaj i pozwól opiekunowi wyrazić emocje. Po 5-10 minutach rozmowy, zaproponuj podsumowanie.
   9.4 Facts Distiller Prompt
   Przeanalizuj poniższy zapis konwersacji i wyekstrahuj kluczowe fakty.
   Fakty powinny być w formacie JSON: [ { "type": "symptom|medication|event|condition|limitation", "value": "...", "severity": 1-10, "context": "..." }, ... ]
   Tylko wyciągaj fakty które nie są już znane (sprawdź poniższą listę znanych faktów).
   Znane fakty: {existing_facts_json}
   Nowe fakty: [wyciągnięte fakty]
   Jeśli brak nowych faktów, zwróć pusty array: []

10. Sekwencja Wzorcowych Use Cases
    Use Case 1: Opiekun zarabia się opiekunem
    Opiekun otwiera app
    Kliknie "Nowa Ankieta"
    System prosi o wiek podopiecznego
    Opiekun: "72 lata"
    System prosi o główne choroby
    Opiekun: "Cukrzyca i wysokie ciśnienie"
    System prosi o leki
    Opiekun: "Metformin i Lisinopril"
    System zbiera info o ograniczeniach
    Profil zapisany w bazie
    Opiekun widzi potwierdzenie: "Dane podopiecznego zapamiętane"

Use Case 2: Sytuacja kryzysowa — Upadek
Opiekun w panice: podopieczny upadł
Kliknie "Interwencja"
Wpisuje/mówi: "Upadek z łóżka"
System dopasowuje scenariusz "Upadek"
System pyta: "Czy stracił przytomność?"
Opiekun: "Tak, na kilka sekund"
System: "Gdzie boli teraz?"
Opiekun: "Noga i bark"
System: "Czy może ruszać nogę?"
Opiekun: "Tak, ale boli"
System: "Czy krwawi?"
Opiekun: "Nie"
System: "Podsumowując: upadek, strata przytomności, ból nogi i barku, brak krwawienia"
System kończy: "Dane zapisane. Jeśli objawy się pogorszą, wezwij pogotowie."
W tle: asynchroniczny wątek ekstrakcji faktów
Po kilku sekundach: notyfikacja "Zaktualizowałem historię zdrowia — dodano 3 nowe zdarzenia"

Use Case 3: Opiekun w stresie
Opiekun po interwencji, zestresowany
Kliknie "Wsparcie dla Mnie"
System: "Wiem że to była trudna chwila. Jak się teraz czujesz?"
Opiekun: "Jestem przerażony, nie wiem co robić"
System: "Rozumiem Twoje obawy. Zrobiłeś/aś dobry kawałek pracy zbierając informacje. Powiedzmy sobie, co się stało krok po kroku, aby lepiej zrozumieć sytuację."
Opiekun opowiada
System: "Widzę że działałeś/aś szybko i właściwie. To pokazuje Twoją gotowość do pomocy. Teraz możemy poczekać na lekarza i obserwować sytuację."
Rozmowa trwa, opiekun odzyskuje spokój
Po ~5 minut system: "Dziękuję za rozmowę. Pamiętaj, że nie jesteś sam/sama"
Rozmowa zapisana, fakty ekstrakcji w tle

11. Success Criteria (MVP)
    Funkcjonalność
    ✅ Ankieta zbiera dane podopiecznego i zapisuje w bazie
    ✅ Interwencja dopasowuje scenariusz i prowadzi rozmowę
    ✅ Destylacja ekstrakcji fakty z transktypów
    ✅ Wsparcie prowadzi empatyczną rozmowę
    ✅ Wszystkie rozmowy zapisywane w bazie
    ✅ WebSocket komunikacja działa bez przerwań
    Techniczne
    ✅ Backend uruchomiony w Docker
    ✅ Bielnik LLM integruje się z backendem
    ✅ SQLite baza działa lokalnie
    ✅ Backend loguje eventy (INFO/ERROR level)
    UX
    ✅ Frontend UI intuicyjny dla opiekuna bez IT knowledge
    ✅ Rozmowy conversational, nie formularz
    ✅ Clear visual feedback (loading, completion)

12. Ograniczenia i Założenia
    Założenia
    1:1 model — jeden opiekun, jeden podopieczny
    Użytkownik ma dostęp do komputera z przeglądarką i połączeniem internetowym (lokalnie)
    Bielnik LLM działa w Docker na tym samym hoście
    Brak multi-user authentication na MVP
    Ograniczenia
    Maksimum 3 predefiniotane scenariusze na MVP
    Brak szyfrowania danych lokalnie
    Brak backup'u — dane tylko lokalne (SQLite file)
    Brak customizacji scenariuszy per-podopieczny
    Brak push notyfikacji / cyklicznych check-insów
    Interwencja i Support są reaktywne, nie proaktywne

13. Roadmap (Phase 2+)
    Phase 2: Moduł Nadzoru (Overseer), Push Notyfikacje, Customizacja Scenariuszy
    Phase 3: Mobile App, Szyfrowanie Danych, Multi-Device Sync, Multi-User
    Phase 4: Analytics Dashboard, Integration z systemami medycznymi, Audit Trail

14. Dokumentacja i Development Notes
    Backend Logger
    Level: INFO (główne eventy), ERROR (błędy)
    Format: timestamp, level, module, message, details
    Storage: backend_logs table w SQLite
    Development Constraints
    Timeframe: Minimalistyczne podejście — focus na happy path
    Testing: Basic unit tests na ekstrakcji faktów, manual testing interfejsu
    Documentation: README (jak uruchomić Docker), API docs (WebSocket messages)

15. Definicje i Słownik
    Opiekun (Caregiver): Osoba opiekująca się podopiecznym
    Podopieczny (Ward): Osoba starsza lub z problemami zdrowotnymi/mentalnymi
    Scenariusz Kryzysowy (Crisis Scenario): Predefiniowany workflow dla typowej sytuacji (upadek, zamieszanie, etc.)
    Fakt (Fact): Ekstrakcja danych (symptom, medication, event) z rozmowy
    Interwencja (Intervention): Moduł obsługi sytuacji kryzysowej
    Destylacja Faktów (Facts Distiller): Moduł ekstrakcji informacji z transktypów
    Wsparcie (Support): Moduł wspierania emocjonalnego opiekuna

Koniec PRD
