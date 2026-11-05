# 4. Arhitektura i oblikovanje

**Cilj:** Definirati arhitekturni stil, dekompoziciju programa na podsustave i module, organizaciju podataka te dinamičko ponašanje tijekom izvođenja. Arhitektura predstavlja temelj za implementaciju i ažurira se pri svakoj važnijoj promjeni projektnih odluka.

| Arhitekturni pogled | Ključno inženjersko pitanje | Primijenjeni model ili dijagram |
| :--- | :--- | :--- |
| **Strategija rješenja** | Koji stil i temeljne odluke određuju program? | Tablica odluka (odjeljak 4.1) |
| **Struktura podsustava** | Koji su glavni dijelovi i kako komuniciraju? | Dijagram komponenata (4.2) |
| **Model podataka** | Kako su strukturirani trajni podaci programa? | Relacijski ER dijagram (4.3.1) |
| **Struktura koda** | Kako su dodijeljene odgovornosti i ključne klase? | Dijagram razreda — rezervacije (4.3.2) |
| **Dinamičko ponašanje** | Kako moduli surađuju pri obradi ključnih transakcija? | Sekvencijski dijagram — rezervacija termina (4.4.1) |
| **Stanja sustava** | Kako ključni domenski objekt mijenja svoja stanja? | Dijagram stanja rezervacije (4.4.2) |
| **Fizički razmještaj** | Na kojim se izvršnim čvorovima program izvodi? | Dijagram razmještaja (poglavlje 6) |

---

## 4.1 Ključne arhitekturne odluke

| Odluka | Razmatrana alternativa | Projektno specifično obrazloženje i kompromis |
| --- | --- | --- |
| Web-klijent u pregledniku i jedna Spring Boot poslužiteljska aplikacija (modularni monolit) | Zasebno postavljene usluge za izlete, rezervacije i korisnike | Izleti, termini i rezervacije dijele iste podatke i transakcije (provjera slobodnih mjesta pri rezervaciji). Jedna aplikacija omogućuje jednu lokalnu transakciju i jednostavno postavljanje; kvar aplikacije prekida sve funkcionalnosti istodobno. Vidi ADR-01. |
| Slojevi kontroler → usluga → repozitorij; usluge su konkretni razredi bez zasebnih sučelja | Sučelje za svaku uslugu (`ITripService`, `IReservationService` …), kako je bilo skicirano u početnom oblikovanju | Svaka usluga ima jednu implementaciju, pa sučelja ne bi donosila zamjenjivost. Ovisnosti se primaju kroz konstruktor, pa se usluge i dalje mogu zamijeniti testnim dvojnicima (Mockito). Ako se pojavi druga implementacija, sučelje se uvodi tada. |
| Prijava isključivo preko vanjskih OAuth2 pružatelja; identitet u tablici `korisnik_vanjski` | Vlastita registracija s korisničkim imenom i lozinkom | Sustav ne pohranjuje lozinke, a jedan korisnički račun može imati više vanjskih identiteta (npr. Google i Microsoft). Kompromis: prijava ovisi o dostupnosti pružatelja identiteta. Vidi ADR-02. |
| Slobodna mjesta računaju se iz potvrđenih rezervacija (`izlet.max_sudionika − Σ rezervacija.broj_osoba`) | Denormalizirani brojač slobodnih mjesta na `izlet_instanca` | Nema dvostrukog izvora istine koji se može razići s rezervacijama. Kompromis: istodobne rezervacije zahtijevaju zaključavanje termina unutar transakcije (vidi 4.7). |
| Slanje e-pošte kroz Spring aplikacijske događaje (`events` + `ReservationMailListener` → `MailService`) | Zaseban red poruka i proces za slanje e-pošte (Redis/RabbitMQ + worker) | Kreiranje rezervacije ne čeka SMTP poslužitelj i ne ovisi o njegovoj dostupnosti, bez dodatne infrastrukture. Kompromis: nema trajnog reda ni ponovnog pokušaja — poruka se gubi ako slanje ne uspije ili aplikacija padne. Vidi ADR-03. |
| Logičko brisanje izleta i termina (`is_active`) | Fizičko brisanje redaka | Rezervacije ostaju povezane s izletom i terminom pa povijest i statistika ostaju točne. Kompromis: svi upiti za javni prikaz moraju filtrirati neaktivne zapise. |
| API vraća DTO objekte kroz mapere (`dto`, `mappers`) | Izravno serijaliziranje JPA entiteta | Klijent ne ovisi o shemi baze, nema problema s lijenim učitavanjem i cikličnim vezama, a administratorski prikaz (`AdminReservationResponse`) može sadržavati više podataka od korisničkog. Kompromis: dodatni razredi i kod mapiranja. |

## 4.2 Gradivni blokovi i UML dijagram komponenata

**Dijagram komponenata:**

![Dijagram komponenata sustava TouristAgency](./assets/diagrams/4-1-touristagency-system-CMP.svg)

**Slika 4.1. Dijagram komponenata sustava TouristAgency**

**PlantUML izvor:** [izvor](./puml/4-1-touristagency-system-CMP.puml)

| Komponenta | Odgovornost | Razlog za ovu granicu |
| --- | --- | --- |
| Web-klijent | Pretraga i prikaz izleta, odabir termina, izračun cijene, rezervacija, pregled vlastitih rezervacija, administratorski prikazi. | Korisnička interakcija odvojena je od poslovnih pravila; klijent komunicira samo preko REST API-ja i ne zna za bazu. |
| Sigurnost (`SecurityConfig`, Spring Security) | OAuth2 prijava, sesija, zaštita ruta (`/api/admin/**` samo za administratora). | Provjera identiteta i ovlasti izvodi se prije kontrolera, na jednom mjestu, za sve zahtjeve. |
| API i usluga izleta (`TripController`, `TripService`) | Pretraga i detalji izleta, CRUD izleta, dodavanje i otkazivanje termina, znamenitosti, izračun cijene. | Katalog izleta i termina mijenja organizator/administrator, a čitaju ga svi; neovisan je o tome tko rezervira. |
| API i usluga rezervacija (`ReservationController`, `AdminReservationController`, `ReservationService`) | Kreiranje rezervacije uz provjeru slobodnih mjesta, pregled i otkazivanje vlastitih rezervacija, administratorska pretraga i otkazivanje. | Najkritičnije poslovno pravilo (nema prebukiranja) ima jedno mjesto odgovornosti; korisnički i administratorski API dijele istu uslugu. |
| Korisnici i uloge (`AuthController`, `AuthService`, `CurrentUserService`, `RoleManagementController`, `RoleManagementService`) | Povezivanje OAuth2 identiteta s računom, podaci o prijavljenom korisniku, dodjela uloga zaposlenicima. | `CurrentUserService` je jedini izvor ID-a prijavljenog korisnika, pa ga kontroleri nikad ne čitaju iz tijela zahtjeva. |
| Obavijesti e-poštom (`ReservationMailListener`, `MailService`) | Reagiranje na događaje rezervacije i slanje e-pošte preko SMTP-a. | Slanje e-pošte odvojeno je od transakcije rezervacije; kvar SMTP-a ne poništava rezervaciju. |
| Repozitoriji (Spring Data JPA) | Pristup tablicama i upiti (pretraga, filtriranje, straničenje). | Usluge ne pišu SQL izravno; upiti su na jednom mjestu. |
| PostgreSQL | Trajna pohrana korisnika, izleta, termina, lokacija, usluga i rezervacija. | Unutar granice sustava jer je baza aplikacije. |
| Vanjski: pružatelj identiteta, SMTP, kartografske pločice | Autentikacija, dostava e-pošte, prikaz karte. | Usluge trećih strana izvan granice sustava. |

Sve komponente unutar pravokutnika „Spring Boot aplikacija” moduli su jedne aplikacije koja se postavlja kao cjelina, a ne zasebne usluge. Paketi u kodu: `controllers`, `services`, `repositories`, `models`, `dto`, `mappers`, `events`, `exceptions`, `config`.

## 4.3 Model podataka i oblikovanje razreda

### 4.3.1 ER dijagram i model podataka

**ER dijagram:**

![ER dijagram podataka sustava TouristAgency](./assets/diagrams/4-2-touristagency-data-ERD.svg)

**Slika 4.2. ER dijagram podataka sustava TouristAgency**

**Izvor dijagrama:** [PlantUML izvor](./puml/4-2-touristagency-data-ERD.puml) (izvorna skica izrađena u dbdiagram.io)  
**Implementacija:** JPA entiteti u paketu `touristAgency.models`, repozitoriji u `touristAgency.repositories`. **[PROVJERITI: dodati poveznicu na migracije / `schema.sql` ako postoje.]**

Izlet pripada jednoj kategoriji i nudi više dodatnih usluga (`izlet_usluga`). Svaki izlet ima više termina (`izlet_instanca`) i više lokacija (mjesto okupljanja i znamenitosti, razlikuju se stupcem `tip`). Korisnik rezervira određeni termin; rezervacija pamti broj osoba, status i ukupnu cijenu, a odabrane dodatne usluge s količinom i cijenom pohranjuju se u `rezervacija_usluga`. Jedan korisnik može imati više vanjskih identiteta (`korisnik_vanjski`), a vodiči se terminima dodjeljuju preko `izlet_instanca_vodic`.

Odluke vidljive u modelu:

- `rezervacija_usluga.cijena` i `rezervacija.ukupna_cijena` čuvaju cijenu u trenutku rezervacije, pa kasnija promjena cjenika ne mijenja postojeće rezervacije.
- `izlet_instanca` nema stupac slobodnih mjesta; broj slobodnih mjesta izvodi se iz `izlet.max_sudionika` i potvrđenih rezervacija (odluka u 4.1).
- Uloga je pohranjena kao jedan stupac `korisnik.uloga` (jedna uloga po korisniku), a ne kao zasebna tablica uloga.
- Stupci `created_at` i `updated_at` postoje u gotovo svim tablicama i izostavljeni su s dijagrama radi čitljivosti.
- Tablica `izlet_instanca_vodic` postoji u shemi, ali trenutačno nema API ni uslugu koja je koristi (vidi 4.7).
- Obavijesti unutar aplikacije nemaju tablicu — korisnici se obavještavaju samo e-poštom.

### 4.3.2 Reprezentativni UML dijagram razreda

**Područje:** rezervacija termina i otkazivanje rezervacije.

![Dijagram razreda za obradu rezervacija](./assets/diagrams/4-3-touristagency-reservation-CD.svg)

**Slika 4.3. Dijagram razreda za obradu rezervacija**

**PlantUML izvor:** [izvor](./puml/4-3-touristagency-reservation-CD.puml)  
**Referenca na kod:** `controllers/ReservationController.java`, `controllers/AdminReservationController.java`, `services/ReservationService.java`, `services/CurrentUserService.java`, `mappers/ReservationMapper.java`, `services/ReservationMailListener.java`

`ReservationService` jedino je mjesto na kojem se kreira rezervacija i mijenja njezin status — i korisnički (`ReservationController`) i administratorski (`AdminReservationController`) API delegiraju na nju, pa se pravila otkazivanja ne dupliciraju. Kontroleri sadrže samo validaciju ulaza (`@Valid`, granice straničenja), dohvat ID-a prijavljenog korisnika preko `CurrentUserService` i mapiranje u DTO preko `ReservationMapper`. Provjera vlasništva provodi se u usluzi (`getReservationDetails(reservationId, userId)`, `cancelReservation(reservationId, userId)`), a ne u klijentu. `ReservationService` ne poziva `MailService` izravno nego objavljuje događaj; `ReservationMailListener` na njega reagira, pa usluga rezervacija ne ovisi o načinu slanja obavijesti.

Dijagram namjerno izostavlja stupce baze podataka i izlete/kategorije — ER model opisuje pohranjene podatke, a ovaj dijagram dodjelu odgovornosti i operacija.

## 4.4 Ponašanje tijekom izvođenja

### 4.4.1 Reprezentativni UML sekvencijski dijagram

**Scenarij:** Rezervacija termina izleta (UC-03), uključujući izračun cijene, provjeru slobodnih mjesta i asinkronu potvrdu e-poštom.

![Sekvencijski dijagram rezervacije termina](./assets/diagrams/4-4-touristagency-reservation-SD.svg)

**Slika 4.4. Sekvencijski dijagram rezervacije termina**

**PlantUML izvor:** [izvor](./puml/4-4-touristagency-reservation-SD.puml)

Prije potvrde klijent dohvaća cijenu preko `GET /api/trips/{id}/calculate-price`, ali konačnu cijenu računa i pohranjuje poslužitelj unutar `book(...)`, pa klijent ne može poslati vlastitu cijenu. Provjera aktivnosti termina, provjera slobodnih mjesta i spremanje rezervacije izvode se u jednoj transakciji. Prikazane su tri alternative: neprijavljen korisnik (401), nevaljan zahtjev (400 preko `GlobalExceptionHandler`) i nedovoljan broj mjesta ili neaktivan termin (409). E-pošta se šalje nakon što je rezervacija spremljena; neuspjeh slanja ne poništava rezervaciju.

### 4.4.2 Reprezentativni UML dijagram stanja

**Vlasnik stanja i razlog odabira:** Rezervacija (`Reservation`, stupac `rezervacija.status`, enum `ReservationStatus`). Status određuje računa li se rezervacija u zauzeta mjesta termina i smije li je korisnik ili administrator otkazati, pa su prijelazi izravno povezani s pravilom o prebukiranju.

![Dijagram stanja rezervacije](./assets/diagrams/4-5-touristagency-reservation-SM.svg)

**Slika 4.5. Dijagram stanja rezervacije**

**PlantUML izvor:** [izvor](./puml/4-5-touristagency-reservation-SM.puml)

Rezervacija nastaje izravno u stanju `CONFIRMED` (nema plaćanja ni ručnog odobravanja). U `CANCELLED` prelazi otkazivanjem od strane vlasnika, administratora ili otkazivanjem cijelog termina; otkazana rezervacija ne može se ponovno potvrditi. Prijelaz u `COMPLETED` nakon završetka termina prikazan je isprekidano jer u kodu ne postoji periodički zadatak koji ga izvodi. **[PROVJERITI: točne vrijednosti enuma `ReservationStatus`; otkazuje li `TripService.cancelSession` i rezervacije termina; postoji li rok za otkazivanje od strane korisnika.]**

## 4.5 Zapisi arhitekturnih odluka

| ID | Datum | Odluka | Razmatrana alternativa | Obrazloženje i kompromis | Status |
| --- | --- | --- | --- | --- | --- |
| ADR-01 | [datum] | Jedna Spring Boot aplikacija s modulima po domeni | Zasebne usluge za izlete, rezervacije i korisnike | Jedna lokalna transakcija za rezervaciju i jednostavno postavljanje; kvar aplikacije prekida sve funkcionalnosti. | Prihvaćeno |
| ADR-02 | [datum] | Prijava samo preko OAuth2 pružatelja, više identiteta po računu | Vlastite lozinke | Bez pohrane lozinki; ovisnost o vanjskom pružatelju. | Prihvaćeno |
| ADR-03 | [datum] | E-pošta preko Spring aplikacijskih događaja | Red poruka i zaseban worker | Bez dodatne infrastrukture; nema ponovnog pokušaja slanja. | Prihvaćeno (zamjenjuje početnu skicu s `EmailQueue`/`EmailWorker`) |

### ADR-01: Jedna poslužiteljska aplikacija s modulima po domeni

| Polje | Zapis odluke |
| --- | --- |
| Kontekst i cilj | Tim tijekom semestra mora izraditi i postaviti katalog izleta, rezervacije, prijavu i administraciju. Rezervacija mora atomarno provjeriti slobodna mjesta termina i spremiti rezervaciju s dodatnim uslugama. |
| Kriteriji odluke | Atomarnost rezervacije, mogućnost izrade i postavljanja u roku, jasno razdvojene odgovornosti u kodu. |
| Razmatrane alternative | **Jedna aplikacija:** moduli (`TripService`, `ReservationService`, `AuthService` …) dijele bazu i transakciju. **Zasebne usluge:** rezervacije bi morale preko mreže pitati uslugu izleta za slobodna mjesta, što zahtijeva distribuiranu koordinaciju. |
| Odluka i obrazloženje | Jedna Spring Boot aplikacija s paketima po sloju i uslugama po domeni. Rezervacija se izvodi u jednoj `@Transactional` metodi. Prihvaćamo da se aplikacija skalira i postavlja kao cjelina. |
| Status | Prihvaćeno. |

### ADR-02: Prijava samo preko vanjskih OAuth2 pružatelja

| Polje | Zapis odluke |
| --- | --- |
| Kontekst i cilj | Korisnici se moraju prijaviti radi rezervacije, a zaposlenici radi administracije. Tim ne želi odgovornost za pohranu i oporavak lozinki. |
| Kriteriji odluke | Sigurnost, jednostavnost za korisnika, mogućnost prijave s više pružatelja na isti račun. |
| Razmatrane alternative | **Vlastite lozinke:** puna kontrola, ali hashiranje, resetiranje i zaštita od napada. **OAuth2 (Spring Security `oauth2Login`):** pružatelj provjerava identitet, aplikacija pamti samo `provider` i `provider_user_id`. |
| Odluka i obrazloženje | OAuth2 prijava; tablica `korisnik_vanjski` omogućuje više identiteta po računu. Novi korisnik dobiva osnovnu ulogu, a uloge zaposlenika dodjeljuje administrator (`RoleManagementController`). Prijava ne radi ako je pružatelj nedostupan. |
| Status | Prihvaćeno. |

### ADR-03: Slanje e-pošte preko aplikacijskih događaja

| Polje | Zapis odluke |
| --- | --- |
| Kontekst i cilj | Korisnik treba dobiti potvrdu rezervacije e-poštom, ali spori ili nedostupni SMTP poslužitelj ne smije usporiti ni poništiti rezervaciju. |
| Kriteriji odluke | Odziv API-ja, rezervacija ostaje valjana i kad e-pošta ne uspije, bez dodatne infrastrukture. |
| Razmatrane alternative | **Red poruka i zaseban worker** (početna skica s `EmailQueue`/`EmailWorker`): trajnost i ponovni pokušaji, ali dodatni kontejner i konfiguracija. **Sinkroni poziv `MailService` iz usluge:** najjednostavnije, ali vezuje odziv i ishod transakcije uz SMTP. **Spring događaj + listener:** usluga objavljuje događaj, listener šalje e-poštu nakon spremanja. |
| Odluka i obrazloženje | `ReservationService` objavljuje događaj (paket `events`), `ReservationMailListener` poziva `MailService`. Prihvaćamo da se poruka ne šalje ponovno ako slanje ne uspije. **[PROVJERITI: je li listener `@TransactionalEventListener(phase = AFTER_COMMIT)` i `@Async`; ako nije, e-pošta se šalje unutar transakcije.]** |
| Status | Prihvaćeno. |

## 4.6 Zajednička pravila oblikovanja

| Aspekt | Mehanizam | Pravilo | Mjesto primjene |
| --- | --- | --- | --- |
| Sigurnost — autentikacija | Spring Security OAuth2 login + sesijski kolačić | Svi zaštićeni endpointi zahtijevaju prijavu; `GET /api/auth/me` vraća 401 ako korisnik nije prijavljen. | `config/SecurityConfig`, `AuthController`, `AuthService` |
| Sigurnost — identitet korisnika | `CurrentUserService.requireUserId()` | ID korisnika uvijek se uzima iz sigurnosnog konteksta, nikad iz tijela zahtjeva, pa korisnik ne može rezervirati ili otkazati u tuđe ime. | `ReservationController`, `ReservationService` (provjera vlasništva) |
| Sigurnost — autorizacija | Pravila ruta po ulozi | `/api/admin/**` dostupan je samo administratoru; skrivanje gumba u klijentu nije zaštita. **[PROVJERITI u `SecurityConfig`: pravila za `/api/admin/**` i za izmjenu izleta (`POST/PUT/DELETE /api/trips`).]** | `config/SecurityConfig` |
| Validacija podataka | Jakarta Bean Validation (`@Valid`) | Tijela zahtjeva provjeravaju se prije ulaska u uslugu; granice straničenja (`page ≥ 0`, `1 ≤ size ≤ 100`) provjeravaju se u kontroleru. | DTO u paketu `dto`, `TripController`, `ReservationController`, `AdminReservationController`, `RoleManagementController` |
| Obrada pogrešaka | `@RestControllerAdvice` | Iznimke iz usluga (paket `exceptions`) pretvaraju se u dosljedne HTTP odgovore (400, 403, 404, 409) na jednom mjestu. | `controllers/GlobalExceptionHandler` |
| Transakcije | `@Transactional` | Provjera slobodnih mjesta i spremanje rezervacije s uslugama izvode se u jednoj transakciji. | `ReservationService.book` |
| Upravljanje ovisnostima | Injektiranje kroz konstruktor | Kontroleri i usluge primaju ovisnosti kroz konstruktor, bez `@Autowired` na poljima. | Svi kontroleri i usluge |
| Granica API-ja | DTO + maperi | Entiteti se ne vraćaju klijentu; korisnički i administratorski prikaz rezervacije različiti su DTO-i. | `dto`, `mappers/ReservationMapper` |
| Asinkrone obavijesti | Aplikacijski događaji | Usluge objavljuju događaje, slanje e-pošte odvija se u listeneru. | `events`, `ReservationMailListener`, `MailService` |

## 4.7 Arhitekturni rizici i tehnički dug

**Tehnički rizici i dug**

| Rizik ili kompromis | Posljedica | Odgovor na rizik |
| --- | --- | --- |
| Istodobne rezervacije istog termina (slobodna mjesta računaju se zbrajanjem) | Bez zaključavanja dvije transakcije mogu istodobno vidjeti dovoljno mjesta i prebukirati termin. | **[PROVJERITI]** Termin se dohvaća s pesimističnim zaključavanjem (`@Lock(PESSIMISTIC_WRITE)`) unutar `book`; ako se to ne radi, dodati zaključavanje ili optimističnu verziju na `izlet_instanca`. |
| E-pošta bez trajnog reda i ponovnog pokušaja | Ako SMTP ne odgovori ili aplikacija padne, korisnik ne dobije potvrdu. | Prihvaćeno (ADR-03); rezervacija je vidljiva u „Moje rezervacije”, greška se bilježi u log. |
| `@CrossOrigin(origins = "*")` na `TripController` | Bilo koja domena može pozivati API izleta iz preglednika, uključujući metode izmjene. | Ograničiti CORS na domenu klijenta centralno u `SecurityConfig` i ukloniti anotaciju s kontrolera. |
| `GET /api/trips/{id}/landmarks?lat=&lon=` prima položaj korisnika | Položaj uređaja šalje se na poslužitelj, što je u suprotnosti s početnom namjerom da se položaj obrađuje samo u pregledniku. | Svjesni kompromis radi jednostavnijeg klijenta; položaj se ne pohranjuje. Alternativa: klijent dohvaća sve znamenitosti i sam računa udaljenost. **[Odlučiti i uskladiti s nefunkcionalnim zahtjevom o privatnosti.]** |
| Nema periodičkog zadatka za završene termine | Rezervacije završenih termina ostaju u stanju `CONFIRMED`. | Dodati `@Scheduled` zadatak ili izvoditi status iz `termin_kraj` pri čitanju. |
| `TestController` u produkcijskom kodu | Nepotrebni endpointi dostupni u postavljenoj aplikaciji. | Ukloniti ili uključiti samo u razvojnom profilu (`@Profile("dev")`). |

**Poslovni ili domenski rizici**

| Rizik | Posljedica | Odgovor na rizik |
| --- | --- | --- |
| Jedna instanca aplikacije obrađuje sve zahtjeve | Kvar poslužitelja privremeno onemogućuje pregled izleta i rezervacije. | Prihvaćeno u okviru ograničenja projekta; nema automatskog prebacivanja na pričuvni sustav. |
| Nedostupnost pružatelja identiteta | Korisnici se ne mogu prijaviti ni rezervirati, iako je katalog izleta javno dostupan. | Prihvaćeno (ADR-02); podržano je više pružatelja. |
| Nedostupnost kartografske usluge | Karta se ne prikazuje. | Znamenitosti i mjesto okupljanja ostaju dostupni kao tekst u detaljima izleta. |

---

> **Završna provjera:** Usporediti dijagrame s kodom (posebno sve oznake **[PROVJERITI]**), ažurirati zastarjele opise i objasniti preostale razlike.
