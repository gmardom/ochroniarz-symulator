# Ochroniarz symulator

## Opis
Trójwymiarowa gra first-person, w której wcielasz się w ochroniarza sklepu. Gra została stworzona w silniku Godot 4.5 z użyciem wtyczki **Godot Kotlin/JVM**. Patroluj sklep, wypatruj złodziei kradnących piwo, walcz z nimi wręcz (z systemem kondycji) i przeciągaj obezwładnionych sprawców do strefy zgłoszeń. Rozgrywka toczy się w systemie zmianowym z możliwością zapisu i odczytu stanu gry.

## Szybki start

```bash
# 1. Sklonuj repozytorium
git clone <adres-repozytorium>
cd ochroniarz-symulator

# 2. Ustaw JAVA_PATH na JDK 21 (patrz "Konfiguracja środowiska" poniżej)

# 3. Wygeneruj pliki rejestracyjne skryptów
./gradlew generateGodotRegistration

# 4. Pobierz edytor Godot Kotlin/JVM z GitHub Releases (patrz "Edytor Godot" poniżej)
# 5. Otwórz plik project.godot w niestandardowym edytorze i naciśnij F5
```

## Wymagania wstępne

### Java 21 (JDK)
Wymagana jest dystrybucja **JDK 21**. Toolchain Gradle używa jej do kompilacji kodu źródłowego Java, a wtyczka Godot Kotlin/JVM uruchamia na niej JVM.

| Dystrybucja | Pobieranie |
|---|---|
| **Eclipse Temurin** (zalecana) | [adoptium.net](https://adoptium.net/temurin/releases/?version=21) |
| **Oracle JDK** | [oracle.com/java](https://www.oracle.com/java/technologies/downloads/#java21) |
| **Amazon Corretto** | [aws.amazon.com/corretto](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html) |

Zweryfikuj instalację:
```bash
java -version
# Oczekiwane: openjdk version "21" ... 2025-XX-XX
```

### Wtyczka Godot Kotlin/JVM — niestandardowy edytor Godot
> **WAŻNE:** Ten projekt wymaga **niestandardowej kompilacji Godota** z projektu `godot-kotlin-jvm`. Oficjalny edytor Godot ze strony godotengine.org **nie będzie działać**.

| Komponent | Wersja |
|---|---|
| **Edytor Godot (niestandardowa kompilacja)** | `0.14.3-4.5.1` — pobierz z [GitHub Releases](https://github.com/utopia-rise/godot-kotlin-jvm/releases/tag/0.14.3-4.5.1) |
| **Wtyczka Gradle** | `com.utopia-rise.godot-kotlin-jvm` wersja `0.14.3-4.5.1` (rozwiazywana automatycznie) |
| **Gradle** | 8.14.4 (dołączony przez `gradlew` / `gradlew.bat`) |

Na stronie wydania pobierz archiwum odpowiadające twojemu systemowi — np. `godot-linux.x86_64-release.zip`, `godot-windows.x86_64-release.zip` lub `godot-macos.zip`.

> **Brak wtyczki IDE:** Ze względu na zmianę polityki JetBrains dotyczącej wtyczek, wersja 0.14.x nie zawiera wtyczki IDE. Wszystkie zadania Gradle należy uruchamiać z terminala.

### Obsługiwane systemy operacyjne
Wtyczka Godot Kotlin/JVM obsługuje następujące platformy jako edytor i cel eksportu:

| Platforma | Edytor | Eksport |
|---|---|---|
| Windows x86_64 | Tak | Tak |
| Linux x86_64 | Tak | Tak |
| macOS x86_64 / arm64 | Tak | Tak |
| Android (arm64-v8a, x86_64) | — | Tak |
| iOS (arm64) | — | Tak |

## Instalacja i konfiguracja

### 1. Konfiguracja środowiska

Ustaw zmienną środowiskową `JAVA_PATH` tak, aby wskazywała na plik wykonywalny `java` z JDK 21. Dzięki temu Godot wie, której JVM użyć do wczytania wtyczki Kotlin/JVM.

**Windows (PowerShell) — sesja bieżąca:**
```powershell
$env:JAVA_PATH = "C:\Program Files\Java\jdk-21\bin\java.exe"
```

**Windows (PowerShell) — trwale (dla całego systemu):**
```powershell
[System.Environment]::SetEnvironmentVariable('JAVA_PATH','C:\Program Files\Java\jdk-21\bin\java.exe','Machine')
```

**Windows (Wiersz polecenia):**
```cmd
set JAVA_PATH=C:\Program Files\Java\jdk-21\bin\java.exe
```

**Linux (bash):**
```bash
export JAVA_PATH=/usr/lib/jvm/jdk-21/bin/java
```

**macOS (zsh):**
```zsh
export JAVA_PATH=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin/java
```

Aby ustawić zmienną trwale w systemach Linux/macOS, dodaj linię `export` do pliku `~/.bashrc`, `~/.zshrc` lub `~/.profile`.

> **Weryfikacja:** Uruchom ponownie terminal i wykonaj `echo $JAVA_PATH` (macOS/Linux) lub `echo %JAVA_PATH%` (Windows CMD), aby potwierdzić ustawienie zmiennej.

### 2. Konfiguracja projektu

#### Krok 1 — Generowanie plików rejestracyjnych skryptów
```bash
./gradlew generateGodotRegistration
```
W systemie Windows:
```cmd
gradlew.bat generateGodotRegistration
```

Polecenie to generuje pliki `.gdj` w katalogu `scripts/`. Informują one Godota, które klasy Java są dostępne jako skrypty. Ponownie uruchom to zadanie po dodaniu, zmianie nazwy lub usunięciu klas Java.

#### Krok 2 — (Opcjonalnie) Kompilacja projektu
Gradle kompiluje źródła Java i pakuje je automatycznie podczas uruchamiania Godota. Aby skompilować ręcznie:
```bash
./gradlew build
```

### 3. Uruchamianie projektu

1. **Uruchom niestandardowy edytor Godot Kotlin/JVM** (kompilację pobraną z GitHub Releases, **nie** oficjalny plik wykonywalny Godota).
2. Kliknij **Import** i wybierz plik `project.godot` z folderu projektu.
3. Poczekaj na inicjalizację wtyczki JVM — w dzienniku wyjściowym edytora pojawią się komunikaty z wtyczki.
4. Naciśnij **F5** lub kliknij przycisk **Play**.

**Oczekiwany rezultat:** Pojawia się menu główne. Możesz rozpocząć nową grę, wczytać zapis, dostosować ustawienia lub wyjść.

> **Pierwsze uruchomienie** jest wolniejsze, ponieważ Gradle pobiera zależności, a wtyczka przetwarza wszystkie klasy Java. Kolejne uruchomienia są szybsze dzięki pamięci podręcznej.

> **Problem z uruchomieniem?** Jeśli okno gry się nie pojawia lub Godot zawiesza się, sprawdź plik `source/godot_kotlin_configuration.json` — jeśli `wait_for_debugger` ma wartość `true` (domyślnie), JVM czeka na debugger na porcie 5005 przed uruchomieniem. Ustaw wartość `false` lub dołącz debugger (patrz sekcja rozwiązywania problemów).

### 4. Eksportowanie gry

Predefiniowany preset eksportu dla **Linux x86_64** znajduje się w pliku `export_presets.cfg`. Aby wyeksportować:
1. W edytorze Godot przejdź do **Project → Export**.
2. Wybierz preset **Linux** i kliknij **Export Project**.
3. Wybierz ścieżkę wyjściową — domyślnie jest to `exports/Ochroniarz symulator.x86_64`.

Szablony eksportu dla kompilacji Kotlin/JVM należy pobrać osobno ze strony [GitHub Releases](https://github.com/utopia-rise/godot-kotlin-jvm/releases/tag/0.14.3-4.5.1). Szukaj plików o nazwie `godot-export-templates-...`.

Dla innych platform (Windows, macOS, Android, iOS) wymagana jest dodatkowa konfiguracja w plikach `build.gradle.kts` i `export_presets.cfg`.

## Struktura projektu

```
├── source/                        # Kod źródłowy Java (główna logika gry)
│   ├── Civilian/                  #   Cywilny NPC (neutralny, 70% spawnów)
│   ├── Enemy/                     #   Złodziej NPC (agresywny, 30% spawnów)
│   ├── Game/                      #   GameLoop, menedżery, zapis/odczyt, obiekty
│   │   └── Rzeczy/                #     Strefa zgłoszeń, kałuża
│   ├── Level/                     #   Ładowanie poziomów, obiekty interaktywne
│   │   └── Rzeczy/                #     Aktywa 3D i sceny sklepu
│   ├── Menu/                      #   Menu główne, pauza, ustawienia
│   ├── NPC/                       #   Bazowa klasa NPC z maszyną stanów
│   ├── Player/                    #   Kontroler pierwszoosobowy, HUD, kondycja
│   ├── Spawner/                   #   Generator okresowy NPC
│   └── Transition/                #   Przejścia między scenami (zanikanie)
├── scripts/                       # Automatycznie generowane pliki .gdj
├── scenes/                        # Dodatkowe sceny Godot (kałuże, strefa zgłoszeń)
├── gradle/wrapper/                # Gradle Wrapper (gradlew + gradlew.bat)
├── source/Menu/Blur.gdshader      # Shader rozmycia gaussowskiego dla menu
├── build.gradle.kts               # Skrypt budowania Gradle (wtyczka Godot Kotlin/JVM)
├── settings.gradle.kts            # Nazwa projektu i wtyczki Gradle
├── gradle.properties              # Argumenty JVM dla Gradle (-Xmx3G)
├── project.godot                  # Plik konfiguracyjny projektu Godot
├── godot_kotlin_configuration.json# Ustawienia uruchomieniowe i debugowania JVM
└── export_presets.cfg             # Predefiniowany preset eksportu Linux x86_64
```

## Główne cechy

- **Ruch pierwszoosobowy 3D** — WASD + mysz, sprint (Shift) z wyczerpywaniem kondycji, skok (Spacja)
- **Walka wręcz** — Atak lewym przyciskiem myszy kosztem kondycji; obezwładnianie złodziei
- **SI NPC z maszyną stanów** — Cywile (70%) i złodzieje (30%) nawigują po sklepie: kolejka przy wejściu, patrolowanie półek, płacenie przy kasie, wyjście. Złodzieje mogą omijać kasę i uciekać
- **Przeciąganie i zgłaszanie** — Przeciągnij obezwładnionych wrogów do strefy zgłoszeń, aby zarejestrować przestępstwo
- **System zmian** — 8 godzin w grze (3 minuty rzeczywiste na godzinę), licznik dni, limit ucieczek (game over przy 5)
- **Zapis / Odczyt** — Trwały stan gry w formacie JSON przez Godot `FileAccess`
- **Pełny system menu** — Menu główne, pauza, ustawienia (suwaki głośności dla każdego busa, przełącznik pełnego ekranu)
- **Shader rozmycia gaussowskiego** — Efekt rozmycia w czasie rzeczywistym za panelami menu
- **Fizyka poślizgu** — Kałuże powodują poślizg gracza podczas sprintu
- **Obiekty interaktywne** — Drzwi (otwieranie/zamykanie z czasem odnowienia), terminal PC (rozpoczęcie zmiany), strefa zgłoszeń

## Informacje o konfiguracji

### `godot_kotlin_configuration.json`

| Klucz | Wartość | Opis |
|---|---|---|
| `wait_for_debugger` | `true` | JVM wstrzymuje się przy starcie i czeka na debugger na porcie 5005. **Ustaw `false` do normalnej gry.** |
| `debug_port` | `5005` | Port JDWP do zdalnego debugowania z IntelliJ / VS Code. |
| `debug_address` | `*` | Adres nasłuchu debuggera (wszystkie interfejsy). |
| `vm_type` | `auto` | Strategia wyboru JVM (`auto`, `jdk` lub `jre`). |
| `use_debug` | `false` | Użyj debugowej kompilacji JVM, jeśli `true`. |
| `disable_gc` | `false` | Wyłącz garbage collection obiektów Kotlin (niezalecane). |
| `max_string_size` | `-1` | Maksymalny rozmiar stringu przekazywanego między Godot a JVM (`-1` = bez ograniczenia). |

### Sterowanie (z `project.godot`)

| Akcja | Klawisz |
|---|---|
| Idź do przodu | **W** |
| Idź do tyłu | **S** |
| Idź w lewo | **A** |
| Idź w prawo | **D** |
| Skok | **Spacja** |
| Sprint | **Shift** |
| Interakcja | **F** |
| Atak | **Lewy przycisk myszy** |

## Rozwiązywanie problemów

| Problem | Prawdopodobna przyczyna | Rozwiązanie |
|---|---|---|
| **Godot nie uruchamia się / brak JVM** | `JAVA_PATH` nie jest ustawione lub wskazuje złą ścieżkę | Ustaw `JAVA_PATH` na pełną ścieżkę do `java` w JDK 21. Uruchom ponownie Godota. |
| **Błąd "Java 21 required" podczas buildowania** | `JAVA_HOME` lub toolchain wskazuje starsze JDK | Zainstaluj JDK 21 i upewnij się, że `java -version` zwraca wersję 21. |
| **Okno gry wisi przy starcie** | `wait_for_debugger` ma wartość `true` w `godot_kotlin_configuration.json` | Ustaw `"wait_for_debugger": false` i uruchom ponownie Godota. |
| **Błąd ".gdj file not found" w edytorze** | Pliki rejestracyjne brakują lub są nieaktualne | Uruchom `./gradlew generateGodotRegistration`, aby je wygenerować ponownie. |
| **Gradle — brak pamięci** | Domyślny sterta jest niewystarczająca dla dużych projektów | Zwiększ `org.gradle.jvmargs=-Xmx4G` lub więcej w `gradle.properties`. |
| **Błąd "Kotlin compiler plugin"** | Niezgodność wersji Kotlin | Wersja wtyczki w `build.gradle.kts` definiuje wersję Kotlin. Sprawdź [gradle-plugin-configuration](https://godot-kotl.in/en/user-guide/advanced/gradle-plugin-configuration/). |
| **Brak metody / klasa nie znaleziona w czasie wykonania** | Pliki `.gdj` są nieaktualne po zmianie nazwy/przeniesieniu klas Java | Wygeneruj ponownie pliki rejestracyjne i uruchom ponownie Godota. |
| **ClassCastException między typami Godota** | Niezgodność typów w moście JNI | Upewnij się, że importujesz typy `godot.*` i nie mieszasz błędnie wrapperów Kotlin/Java. |
| **Antywirus w Windows blokuje Gradle** | Skanowanie w czasie rzeczywistym zakłóca działanie JVM | Dodaj wyjątek dla folderu projektu i `%USERPROFILE%\.gradle\`. |
| **Błąd eksportu — brak szablonów eksportu** | Szablony eksportu Kotlin/JVM nie są zainstalowane | Pobierz odpowiednie szablony ze strony [GitHub Releases](https://github.com/utopia-rise/godot-kotlin-jvm/releases/tag/0.14.3-4.5.1). |

## FAQ

**P: Czy mogę użyć oficjalnego edytora Godot?**
Nie. Wtyczka Godot Kotlin/JVM wymaga **niestandardowej kompilacji silnika** dostępnej na stronie [GitHub Releases](https://github.com/utopia-rise/godot-kotlin-jvm/releases). Oficjalny edytor Godot nie zawiera modułu JVM.

**P: Czy Java jest w pełni obsługiwana przez Godot Kotlin/JVM?**
Obsługa Javy jest **eksperymentalna**, ale działa. Podstawowym językiem wtyczki jest Kotlin; klasy Java są obsługiwane przez ten sam system rejestracji. Zobacz [oficjalną dokumentację](https://godot-kotl.in/en/contribution/support-for-other-jvm-based-languages/).

**P: Jak debugować skrypty Java?**
Dołącz zdalny debugger do portu `5005` (ustawiony w `godot_kotlin_configuration.json`). W IntelliJ utwórz konfigurację **Remote JVM Debug** z `-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005`. Ustaw `wait_for_debugger` na `true` podczas debugowania i na `false` przy normalnym uruchomieniu.

**P: Jak dodać nową klasę Java i udostępnić ją Godotowi?**
1. Utwórz plik `.java` w katalogu `source/` (np. `source/MyClass.java`).
2. Dodaj adnotację `@RegisterClass` do swojej klasy.
3. Uruchom `./gradlew generateGodotRegistration`, aby zaktualizować pliki `.gdj`.
4. Uruchom ponownie Godota — nowa klasa pojawi się na liście skryptów.

**P: Pojawił się ekran game over — jak zrestartować grę?**
Ekran game over pojawia się, gdy 5 złodziei ucieknie. Naciśnij przycisk w HUD, aby wrócić do menu głównego i rozpocząć nową grę.

## Przykład kodu Java

Minimalny skrypt zarejestrowany w Godocie wygląda następująco:

```java
package mygame;

import godot.*;
import godot.annotation.RegisterClass;
import godot.annotation.RegisterFunction;

@RegisterClass
public class MyNode extends Node2D {

    @RegisterFunction
    public void _ready() {
        GD.print("Witaj z Javy!");
    }

    @RegisterFunction
    @Override
    public void _process(double delta) {
        // Wywoływane w każdej klatce
    }
}
```

> **Uwaga:** Tylko domyślne konstruktory (bezargumentowe) mogą być rejestrowane. Logikę inicjalizacyjną umieszczaj w metodzie `_ready()`.

## Wkład / Rozwój

1. Projekt używa **Java 21** z wtyczką **Godot Kotlin/JVM** `0.14.3-4.5.1`. Pliki źródłowe znajdują się w katalogu `source/`.
2. Po dodaniu lub zmianie nazwy klas Java wygeneruj ponownie pliki `.gdj`:
   ```bash
   ./gradlew generateGodotRegistration
   ```
3. Przechowuj katalog `scripts/` w repozytorium, aby członkowie zespołu mogli uruchomić projekt bez pełnej kompilacji Gradle.
4. Komentarze w kodzie są obecnie w **języku polskim** — zachowaj tę konwencję.
5. Testuj zmiany, uruchamiając grę z edytora Godot. Sprawdzaj dziennik wyjściowy edytora w poszukiwaniu błędów JVM.
6. W razie problemów z wtyczką zajrzyj do [oficjalnej dokumentacji](https://godot-kotl.in/) lub zapytaj na [serwerze Discord](https://discord.gg/zpb5Ru7v9x).

## Licencja

Ten projekt jest obecnie **bez licencji**. Nie dołączono pliku licencyjnego.
