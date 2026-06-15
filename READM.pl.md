# Ochroniarz symulator

## Opis
Trójwymiarowa gra first-person, w której wcielasz się w ochroniarza sklepu. Gra została stworzona w silniku Godot 4.5 z użyciem wtyczki Godot Kotlin JVM. Gracz patroluje sklep, wypatruje złodziei kradnących piwo, walczy z nimi wręcz (z systemem kondycji), przeciąga obezwładnionych sprawców do strefy zgłoszeń, aby rozwiązywać przestępstwa, a wszystko to w systemie zmianowym.

## Wymagania wstępne
- **Java 21** (JDK 21) — wymagana przez toolchain wtyczki Godot Kotlin JVM
- **Godot 4.5** — wersja standardowa (bez .NET), **nie** Mono
- **Godot Kotlin JVM plugin v0.14.3-4.5.1** — rozwiązywany automatycznie przez Gradle
- **Gradle 8.14.4** — dołączony przez Gradle Wrapper (`gradlew` / `gradlew.bat`)

## Instalacja i konfiguracja

### 1. Konfiguracja środowiska

Ustaw zmienną środowiskową `JAVA_PATH` tak, aby wskazywała na instalację JDK 21. Dzięki temu Godot będzie wiedział, której maszyny JVM użyć do wczytania wtyczki Kotlin JVM.

**Windows (PowerShell):**
```powershell
$env:JAVA_PATH = "C:\Program Files\Java\jdk-21\bin\java.exe"
```

**Windows (Wiersz polecenia):**
```cmd
set JAVA_PATH=C:\Program Files\Java\jdk-21\bin\java.exe
```

**Linux / macOS:**
```bash
export JAVA_PATH=/usr/lib/jvm/jdk-21/bin/java
```

> **Uwaga:** Sprawdź wersję Javy poleceniem `java -version`. Musi to być wersja **21** (np. `openjdk version "21"`).

### 2. Konfiguracja projektu

```bash
# Sklonuj repozytorium
git clone <adres-repozytorium>
cd ochroniarz-symulator

# Wygeneruj pliki rejestracyjne skryptów (.gdj)
./gradlew generateGodotRegistration
```

W systemie Windows użyj `gradlew.bat`:
```cmd
gradlew.bat generateGodotRegistration
```

> Pliki `.gdj` są generowane automatycznie z kodu źródłowego Java i informują Godota o dostępnych klasach skryptów. Uruchom zadanie generowania po dodaniu lub przeniesieniu klas Java.

### 3. Uruchamianie projektu

1. Otwórz Godot 4.5 i kliknij **Import**.
2. Wskaż folder projektu i wybierz plik `project.godot`.
3. Po otwarciu projektu wtyczka JVM automatycznie wykryje `JAVA_PATH` i uruchomi maszynę JVM.
4. Kliknij przycisk **Play** (lub naciśnij **F5**), aby uruchomić grę.

**Oczekiwany rezultat:** Pojawia się menu główne. Możesz rozpocząć nową grę, wczytać zapis, dostosować ustawienia lub wyjść.

> **Pierwsze uruchomienie może być wolniejsze** — Gradle pobiera zależności, a wtyczka Godot Kotlin JVM przetwarza kod źródłowy Java.

## Struktura projektu

```
├── source/                    # Kod źródłowy Java (główna logika gry)
│   ├── Civilian/              # Cywilny NPC (neutralny)
│   ├── Enemy/                 # Złodziej / wróg NPC (agresywny, kradnie piwo)
│   ├── Game/                  # Pętla gry, menedżery, system zapisu, obiekty świata
│   │   └── Rzeczy/            # Strefa zgłoszeń, kałuża
│   ├── Level/                 # Ładowanie poziomów, obiekty interaktywne (drzwi, PC)
│   │   └── Rzeczy/            # Aktywa 3D i sceny dla poziomu sklepu
│   ├── Menu/                  # Menu główne, pauzy, ustawienia (audio, wideo)
│   ├── NPC/                   # Bazowa klasa NPC z maszyną stanów
│   ├── Player/                # Kontroler pierwszoosobowy, HUD, kondycja, raycast
│   ├── Spawner/               # Generator NPC
│   └── Transition/            # Efekty przejść między scenami
├── scripts/                   # Automatycznie generowane pliki rejestracyjne (.gdj)
├── scenes/                    # Dodatkowe sceny Godot (kałuże, strefa zgłoszeń)
├── gradle/wrapper/            # Gradle Wrapper (gradlew, gradlew.bat)
├── build.gradle.kts           # Skrypt budowania Gradle (wtyczka Godot Kotlin JVM)
├── settings.gradle.kts        # Ustawienia projektu Gradle
├── gradle.properties          # Argumenty JVM dla Gradle
├── project.godot              # Plik konfiguracyjny projektu Godot
├── godot_kotlin_configuration.json  # Ustawienia debugowania/konfiguracji JVM
└── export_presets.cfg         # Predefiniowane ustawienia eksportu (Linux x86_64)
```

## Główne cechy

- **Ruch pierwszoosobowy 3D** — WASD + mysz, sprint z zarządzaniem kondycją
- **Walka wręcz** — Atakuj złodziei ciosami zależnymi od kondycji; obezwładniaj ich
- **SI NPC z maszyną stanów** — Cywile (70%) i złodzieje (30%) z pełną nawigacją: kolejka, patrolowanie półek, płacenie przy kasie, wyjście; złodzieje mogą kraść i uciekać
- **Przeciąganie i zgłaszanie** — Przeciągnij obezwładnionych wrogów do strefy zgłoszeń, aby rozwiązać przestępstwo
- **System zmian** — 8 godzin w grze (3 minuty rzeczywiste na godzinę) z liczeniem dni
- **Zapis / Odczyt** — Trwały stan gry zapisywany w formacie JSON przez Godot `FileAccess`
- **System menu** — Menu główne, pauza, ustawienia (suwaki głośności, przełącznik pełnego ekranu)
- **Shader rozmycia** — Rozmycie gaussowskie w czasie rzeczywistym dla tła menu
- **Fizyka poślizgu** — Kałuże powodują poślizg gracza podczas sprintu

## Rozwiązywanie problemów

| Problem | Rozwiązanie |
|---|---|
| **Godot nie znajduje JVM** | Sprawdź, czy `JAVA_PATH` wskazuje na plik wykonywalny `java` z JDK 21. Uruchom ponownie Godota po zmianie zmiennej. |
| **Błąd budowania — nieodpowiednia wersja Javy** | Upewnij się, że `java -version` zwraca **21**. Zainstaluj JDK 21 ze strony [Adoptium](https://adoptium.net/) lub menedżera pakietów. |
| **Gradle — brak pamięci** | Zwiększ `org.gradle.jvmargs` w pliku `gradle.properties` (domyślnie `-Xmx3G`). |
| **Brak plików .gdj** | Uruchom `./gradlew generateGodotRegistration`, aby ponownie wygenerować pliki rejestracyjne skryptów. |
| **Niezgodność wersji wtyczki Godot Kotlin JVM** | Wersja wtyczki jest zdefiniowana w `build.gradle.kts`. Sprawdź [dokumentację wtyczki](https://godot-kotl.in/) w celu dobrania kompatybilnej wersji. |
| **Antywirus blokuje Gradle w systemie Windows** | Dodaj wyjątek dla folderu projektu lub katalogu cache Gradle (`%USERPROFILE%\.gradle`). |

## Wkład / Rozwój

1. Projekt używa **Java 21** z wtyczką **Godot Kotlin JVM**. Pliki źródłowe Java znajdują się w katalogu `source/`.
2. Po dodaniu lub zmianie nazwy klas Java wygeneruj ponownie pliki `.gdj`: `./gradlew generateGodotRegistration`.
3. Przechowuj katalog `scripts/` w systemie kontroli wersji, aby inni programiści mogli uruchomić projekt bez Gradle.
4. Komentarze w kodzie są obecnie w **języku polskim** — zachowaj ten styl podczas wnoszenia wkładu.
5. Przetestuj swoje zmiany, uruchamiając grę z edytora Godot.

## Licencja

Ten projekt jest obecnie **bez licencji**. Nie dołączono pliku licencyjnego.
