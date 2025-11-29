# Dokument Wymagań Produktowych (PRD) - Wspiernik

## 1. Przegląd Produktu
Wspiernik to cyfrowy asystent zaprojektowany dla opiekunów osób starszych oraz osób z problemami zdrowotnymi lub psychicznymi. Misją projektu jest stworzenie prostego, intuicyjnego i działającego lokalnie narzędzia, które pomaga opiekunom reagować na sytuacje kryzysowe, zapewnia im wsparcie emocjonalne oraz umożliwia gromadzenie i przetwarzanie istotnych informacji o podopiecznych. Główna wartość biznesowa polega na zwiększeniu bezpieczeństwa podopiecznego poprzez triage interwencyjny, zmniejszeniu obciążenia psychicznego opiekuna, dostarczeniu ustrukturyzowanych informacji zdrowotnych dzięki destylacji faktów oraz zapewnieniu pełnej prywatności poprzez lokalne działanie. MVP dostarczy cztery kluczowe moduły w aplikacji internetowej z backendem opartym o WebSocket.

## 2. Problem Użytkownika
Opiekunowie borykają się z kilkoma kluczowymi wyzwaniami, na które odpowiada Wspiernik:
- **Brak ustrukturyzowanego wsparcia w kryzysie:** W nagłych wypadkach opiekunowie często nie wiedzą, jakie informacje zebrać ani jak działać, co prowadzi do stresu i potencjalnie nieoptymalnych decyzji.
- **Rozproszenie informacji o podopiecznym:** Dane zdrowotne, notatki o objawach i listy leków są często rozrzucone w różnych formatach fizycznych i cyfrowych, co utrudnia uzyskanie całościowego obrazu.
- **Znaczne obciążenie psychiczne i emocjonalne:** Rola opiekuna jest z natury stresująca, a opiekunom często brakuje dedykowanego, natychmiast dostępnego źródła wsparcia emocjonalnego.
- **Ryzyko błędnych decyzji:** Bez uporządkowanego kontekstu i historii stanu podopiecznego trudniej jest podjąć właściwe decyzje, zwłaszcza pod presją.

## 3. Wymagania Funkcjonalne
MVP Wspiernika będzie lokalnie hostowaną aplikacją internetową i obejmie następujące główne moduły funkcjonalne:

- **3.1. Wstępna Ankieta (Initial Survey):** Moduł konwersacyjny do przeprowadzenia wstępnego wywiadu na temat zdrowia podopiecznego. Zbiera kluczowe dane, takie jak wiek, choroby, objawy, leki i ograniczenia, poprzez dialog, a nie statyczny formularz.
- **3.2. Interwencja (Caretaker Help):** Moduł wspomagający opiekuna w sytuacjach kryzysowych. Opiekun może opisać sytuację, a system wykorzystuje predefiniowane scenariusze (np. upadek, dezorientacja, ból w klatce piersiowej), aby przeprowadzić opiekuna przez ustrukturyzowany proces zbierania informacji.
- **3.3. Destylator Faktów (Facts Distiller):** Asynchroniczny proces backendowy, który analizuje transkrypcje rozmów ze wszystkich modułów. Wyodrębnia kluczowe, otagowane fakty (np. objawy, zdarzenia, leki) i zapisuje je w lokalnej bazie danych, tworząc ustrukturyzowaną historię zdrowia.
- **3.4. Wsparcie dla Opiekuna (Caretaker Support):** Dedykowany moduł dla dobrostanu emocjonalnego opiekuna. Zapewnia przestrzeń do rozmowy, w której opiekun może wyrazić stres i otrzymać empatyczne, uspokajające wskazówki. Jest to moduł reaktywny, inicjowany przez opiekuna.

System zostanie zbudowany na lokalnym stosie technologicznym:
- **Backend:** Kontener Docker z lokalnym modelem LLM (Bielnik).
- **Frontend:** Aplikacja internetowa z komunikacją WebSocket.
- **Baza danych:** Lokalna baza SQLite do przechowywania wszystkich danych.
- **Logowanie:** Logowanie backendowe dla zdarzeń informacyjnych i błędów.

## 4. Granice Produktu
### W zakresie MVP (In Scope):
- Model 1-do-1: jeden profil opiekuna połączony z jednym profilem podopiecznego.
- Moduł Wstępnej Ankiety do zbierania danych bazowych.
- Moduł Interwencji z trzema predefiniowanymi scenariuszami kryzysowymi (upadek, dezorientacja, ból w klatce piersiowej).
- Moduł Destylatora Faktów do asynchronicznej analizy po rozmowie.
- Moduł Wsparcia Opiekuna do reaktywnego wsparcia emocjonalnego.
- Wszystkie rozmowy są logowane w lokalnej bazie danych SQLite.
- Cała aplikacja (backend, LLM, baza danych) działa lokalnie w kontenerze Docker.
- UI zapewnia jasne informacje wizualne o stanach systemu (np. przetwarzanie, zakończenie).

### Poza zakresem MVP (Przyszłe fazy):
- Moduł Nadzorcy (Overseer) do przeglądu i walidacji porad generowanych przez AI.
- Proaktywne, okresowe kontrole samopoczucia opiekuna (powiadomienia push).
- Możliwość dostosowywania lub tworzenia nowych scenariuszy interwencji przez użytkownika.
- Szyfrowanie danych w spoczynku i uwierzytelnianie wielu użytkowników.
- Natywna aplikacja mobilna.
- Funkcje kopii zapasowej, przywracania lub synchronizacji danych.
- Pulpity analityczne lub integracja z zewnętrznymi systemami medycznymi.

## 5. Historie Użytkownika (User Stories)

### Pierwsza użycie
- **ID:** US-001
- **Tytuł:** Stwierdzenie pierwszego użycia
- **Opis:** Jako nowy opiekun chcę zacząć używać aplikacji i zacząć od ankiety. Jak już korzystałem chcę zobaczyć ekran główny.
- **Kryteria Akceptacji:**
    1. Przy pierwszym uruchomieniu aplikacji sprawdzane są fakty
    2. Jak nie ma faktów użytkownik jest przekierowany na wstępną ankietę
    3. W przypadku zapisanych faktów użytkownik widzi ekran główny

### Wstępna Ankieta
- **ID:** US-002
- **Tytuł:** Przeprowadzenie wstępnej ankiety o podopiecznym
- **Opis:** Jako opiekun chcę wypełnić konwersacyjną ankietę na temat zdrowia mojego podopiecznego, aby system posiadał bazowe informacje do przyszłych interakcji.
- **Kryteria Akceptacji:**
    1. System zadaje mi sekwencję pytań jedno po drugim, dotyczących wieku, chorób, leków i ograniczeń ruchowych.
    2. Moje odpowiedzi tekstowe są poprawnie odbierane i przetwarzane przez system.
    3. Po zakończeniu system potwierdza, że profil został zapisany.
    4. Zebrane dane są zapisywane w tabeli `caregiver_profile` w bazie danych.

### Interwencja
- **ID:** US-004
- **Tytuł:** Obsługa interwencji kryzysowej
- **Opis:** Jako opiekun chcę rozpocząć sesję interwencyjną, gdy wystąpi kryzys, aby uzyskać wskazówki, co robić i mówić.
- **Kryteria Akceptacji:**
    1. Mogę rozpocząć interwencję, klikając przycisk "Interwencja" i opisując sytuację w polu tekstowym.
       1a. Do informacji od użytkownika dodawane są wszystkie fakty oraz informacje o podobiepiecznym (wiek, choroby, leki)
    2. Jeśli mój opis pasuje do słów kluczowych predefiniowanego scenariusza (np. "upadek"), system potwierdza dopasowany scenariusz.
    3. System prowadzi mnie przez scenariusz za pomocą sekwencji konkretnych pytań.
    4. Na koniec rozmowy system przedstawia krótkie podsumowanie zebranych informacji.
    5. Cały zapis rozmowy jest zapisywany w tabeli `conversations` z typem "intervention".

- **ID:** US-005
- **Tytuł:** Obsługa niedopasowanej interwencji kryzysowej
- **Opis:** Jako opiekun chcę otrzymać pomoc, nawet jeśli mój opis kryzysu nie pasuje do predefiniowanego scenariusza.
- **Kryteria Akceptacji:**
    1. Gdy mój opis nie pasuje do słów kluczowych scenariusza, system przechodzi do ogólnego procesu zbierania informacji.
    2. System zadaje szersze pytania o stan podopiecznego i opiekuna.
    3. Rozmowa jest zapisywana w tabeli `conversations`.

### Zarządzanie Faktami
- **ID:** US-006
- **Tytuł:** Otrzymanie powiadomienia o ekstrakcji faktów
- **Opis:** Jako opiekun chcę być powiadamiany po rozmowie, że system przetworzył i zapisał nowe informacje, abym wiedział, że historia podopiecznego jest aktualna.
- **Kryteria Akceptacji:**
    1. Po zakończeniu interwencji, ankiety lub sesji wsparcia, UI pokazuje, że trwa ekstrakcja faktów.
    2. Gdy asynchroniczna ekstrakcja zostanie zakończona, serwer wysyła do klienta wiadomość WebSocket `facts_extracted`.
    3. UI wyświetla powiadomienie wskazujące, ile nowych faktów znaleziono i zapisano.
    4. Jeśli nie znaleziono nowych faktów, powiadomienie wskazuje, że aktualizacja nie była konieczna.


### Wsparcie dla Opiekuna
- **ID:** US-008
- **Tytuł:** Rozpoczęcie sesji wsparcia emocjonalnego
- **Opis:** Jako opiekun czujący stres lub przytłoczenie, chcę rozpocząć rozmowę z modułem wsparcia, aby otrzymać wsparcie emocjonalne.
- **Kryteria Akceptacji:**
    1. Mogę rozpocząć sesję, klikając przycisk "Wsparcie dla Opiekuna" w UI.
       1a. Do informacji od użytkownika dodawane są wszystkie fakty oraz informacje o podobiepiecznym (wiek, choroby, leki)
    2. System odpowiada empatyczną wiadomością otwierającą, pytając o moje samopoczucie.
    3. Mogę prowadzić konwersacyjną wymianę zdań, w której system udziela wspierających i nieoceniających odpowiedzi.
    4. System nie udziela porad medycznych i w razie pytania przekierowuje do lekarza.
    5. Rozmowa jest zapisywana w tabeli `conversations` z typem "support".

## 6. Metryki Sukcesu
### Funkcjonalne
- Wstępna Ankieta skutecznie zbiera i zapisuje dane profilowe podopiecznego w bazie danych.
- Moduł Interwencji poprawnie dopasowuje dane wejściowe użytkownika do jednego z trzech predefiniowanych scenariuszy i prowadzi użytkownika przez proces konwersacyjny.
- Destylator Faktów skutecznie ekstrahuje istotne fakty z transkrypcji rozmów i dodaje je do tabeli `facts`.
- Moduł Wsparcia Opiekuna angażuje się w empatyczną, wieloetapową rozmowę.
- Wszystkie rozmowy są poprawnie logowane w tabeli `conversations`.

### Techniczne
- Aplikacja backendowa i LLM uruchamiają się pomyślnie w kontenerze Docker.
- Komunikacja WebSocket między frontendem a backendem jest stabilna i responsywna.
- Baza danych SQLite poprawnie przechowuje i odczytuje dane lokalnie.
- System logowania backendu poprawnie rejestruje zdarzenia na poziomach INFO i ERROR.

### Doświadczenie Użytkownika (UX)
- Interfejs użytkownika jest intuicyjny i może być obsługiwany przez użytkownika bez specjalistycznej wiedzy IT.
- Przepływy konwersacyjne są naturalne i preferowane względem wypełniania statycznych formularzy.
- Aplikacja zapewnia jasne informacje wizualne dla wszystkich kluczowych działań, takich jak przetwarzanie danych przez system lub zakończenie zadania.



