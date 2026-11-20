# Progi-Tours - Projektna dokumentacija

**Kolegij:** [Programsko inženjerstvo](https://www.fer.unizg.hr/predmet/proinz), Fakultet elektrotehnike i računarstva Sveučilišta u Zagrebu  
**Akademska godina:** 2026./2027.  
**Tim:** Prompt and pray  
**Nastavnik:** Vlado Sruk  
**Asistent / demonstrator:** Miljenko Krhen (asistent)

## Pregled projekta

**Cilj:** Omogućiti čitatelju koji prvi put vidi projekt da u dvije ili tri rečenice prepozna aplikaciju, njezine ciljane korisnike i glavnu svrhu.

Progi-Tours je web-aplikacija turističke agencije u kojoj turisti pretražuju ponudu izleta i rezerviraju termine uz dodatne usluge, a zaposlenici agencije (administratori, organizatori, vodiči i voditelj agencije) prema svojoj ulozi upravljaju izletima, terminima, rezervacijama, rasporedom vodiča i pregledom poslovanja. Cilj je da turist prije rezervacije vidi stvarno stanje ponude i slobodnih mjesta, a agencija ima jedan izvor podataka o izletima, rezervacijama i vodičima.

**Aplikacija i osnovno postavljanje:** [README repozitorija](../README.md#quick-start)  
**Javno dostupna aplikacija:** pogledajte [README - Postavljanje](../README.md#deployment)

## Članovi tima i uloge

Ažurirani popis članova, njihovih GitHub profila i glavnih uloga ili doprinosa nalazi se u [README-u repozitoriju](../README.md#team-members).

## Kazalo dokumentacije

| Stranica | Svrha |
| --- | --- |
| [1. Opseg projekta](1-Projekt.md) | Problem, cilj i granice projekta |
| [2. Analiza zahtjeva](2-Analiza-zahtjeva.md) | Zahtjevi, ograničenja i sljedivost |
| [3. Obrasci uporabe](3-Obrasci-uporabe.md) | Akteri i reprezentativne interakcije korisnika |
| [4. Arhitektura i oblikovanje](4-Arhitektura-i-oblikovanje.md) | Glavne odluke oblikovanja, struktura, podaci i ponašanje |
| [A. Pregled aktivnosti grupe](A-Pregled-aktivnosti-grupe.md) | Aktivnosti tima, doprinosi i izazovi |

## Ključne točke projekta

| Kontrolna točka | Demonstracija | Video |
| --- | --- | --- |
| Iteracija 1 | [https://progi-tours.onrender.com](https://progi-tours.onrender.com) | Nije snimljen |
| Završni pregled | [Poveznica na završnu inačicu projekta] | [Poveznica na video završne demonstracije, ako je snimljen] |

Dokumentacija se kontinuirano ažurira kako se aplikacija razvija.

## Organizacija projekta i razvojni proces

**Način rada:** Tim radi u tjednim iteracijama. Svakog ponedjeljka od 17:00 do 18:30 održava se sastanak na kojem se pregledava stanje, otvaraju novi zadaci kao GitHub Issues i svakom se zadatku dodjeljuje odgovorni član. Svaki zadatak razvija se na zasebnoj grani (`feature/...`, `fix/...`) i u granu `main` uključuje kroz zahtjev za uključivanje promjena (PR). Zapisi sastanaka i plan rada nalaze se u [A. Pregled aktivnosti grupe](A-Pregled-aktivnosti-grupe.md).  
**Praćenje trenutačnih zadataka i nedostataka:** [GitHub Issues](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/issues)

## Pravila dokumentacije

Dokumentacija se sastoji od međusobno povezanih Markdown stranica u mapi `docs`. UML dijagrami izrađuju se u PlantUML-u; izvori se nalaze u `docs/puml`, a iscrtane slike u `docs/assets/diagrams`. Kada se promijeni ponašanje sustava, ažuriraju se i izvor i slika dijagrama.

## Akademski integritet, ponašanje i licenciranje

README repozitorija sadrži projektnu [izjavu o uporabi umjetne inteligencije](../README.md#ai-usage) te [informacije o kodeksu ponašanja i podršci](../README.md#code-of-conduct-and-support). Pristupni podaci i druge tajne nikada se ne objavljuju u dokumentaciji niti dijele u običnim porukama; lokalno se čuvaju u datoteci `backend/.env` koja nije dio repozitorija.

## Izjava o uporabi umjetne inteligencije

Kratak sažetak: [README - Uporaba umjetne inteligencije](../README.md#ai-usage). Ovaj odjeljak bilježi koji su alati korišteni, za koju dokumentaciju ili kod te kako je tim provjerio rezultat. Slijedi FER-ova [pravila o primjerenoj uporabi umjetne inteligencije](https://www.fer.unizg.hr/_download/repository/Policy%20on%20the%20appropriate%20use%20of%20artificial%20intelligence%20at%20the%20faculty%20of%20electrical%20engineering%20and%20computing%5B1%5D.pdf).

### Korišteni alati

| Alat | Inačica ili model | Koristio/la | Glavna svrha |
| --- | --- | --- | --- |
| Claude | Claude (Anthropic) | MK, MZ, LB, RB, DF, LG, LK | Generiranje koda poslužiteljskog i klijentskog dijela, jedinični testovi, konfiguracija (OAuth, Spring Security, Docker, Render, `.env`), izrada početnih skica UML dijagrama, strukturiranje dokumentacije, istraživanje postojećih rješenja, objašnjavanje pogrešaka i pregled koda |
| ChatGPT | ChatGPT (OpenAI) | MK, MZ, LB, RB, DF, LG, LK | Generiranje koda poslužiteljskog i klijentskog dijela, jedinični testovi, konfiguracija (OAuth, Spring Security, Docker, Render, `.env`), izrada početnih skica UML dijagrama, strukturiranje dokumentacije, istraživanje postojećih rješenja, objašnjavanje pogrešaka i pregled koda |

Kratice članova definirane su u [A.1 Evidencija sastanaka](A-Pregled-aktivnosti-grupe.md#a1-evidencija-sastanaka).

### Opseg doprinosa umjetne inteligencije

| Stranica, datoteka ili artefakt | Vrsta pomoći | Što je alat proizveo | Što je tim zadao i promijenio | Lokacija |
| --- | --- | --- | --- | --- |
| 4. Arhitektura i oblikovanje, UML dijagrami | Izvor dijagrama | Početne skice PlantUML dijagrama komponenti, ERD-a, razreda, sekvencijskog dijagrama i dijagrama stanja | Tim je zadao stvarne entitete, komponente i tijek rezervacije te uskladio nazive i veze s kodom | `docs/puml/*.puml`, `docs/assets/diagrams/*.svg` |
| Dokumentacija u mapi `docs` | Strukturiranje | Prijedlog strukture i formulacija odjeljaka | Tim je unio činjenice o projektu i uskladio ih s kodom i zahtjevima | `docs/*.md` |
| A. Pregled aktivnosti grupe, Home | Izrada nacrta | Tablice sastanaka, plana rada i doprinosa izvedene iz GitHub Issuesa i PR-ova | Tim je zadao raspored sastanaka i raspodjelu sati te provjerio odgovorne osobe | `docs/A-Pregled-aktivnosti-grupe.md`, `docs/Home.md` |
| Poslužiteljski dio (Spring Boot) | Generiranje koda | Nacrte kontrolera, servisa, DTO modela i repozitorija za izlete, termine, rezervacije, vodiče, statistiku, uloge i e-poštu | Član odgovoran za zadatak zadao je model podataka i poslovna pravila (provjera slobodnih mjesta, statusi rezervacija, otkazivanje), prilagodio kod postojećoj strukturi i provjerio ga pokretanjem aplikacije | `backend/src/main`; npr. [PR #21](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/pull/21), [PR #32](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/pull/32), [PR #39](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/pull/39) |
| Klijentski dio (React) | Generiranje koda | Nacrte komponenti, stranica, stilova i poziva API-ja | Tim je zadao izgled i tijek stranica, povezao komponente sa stvarnim API-jem i ispravio prikaz na mobilnim uređajima | `frontend/src`; npr. [PR #6](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/pull/6), [PR #38](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/pull/38), [PR #40](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/pull/40) |
| Jedinični testovi | Generiranje koda | Nacrte testova repozitorija i servisa | Tim je zadao slučajeve koje treba ispitati i provjerio da se testovi izvode nad stvarnom shemom baze | `backend/src/test`; [PR #8](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/pull/8) |
| Konfiguracija i postavljanje | Generiranje koda, objašnjenje pogrešaka | Prijedloge konfiguracije OAuth 2.0 i Spring Securityja, Dockerfilea, postavljanja na Render i učitavanja `.env` datoteke | Tim je unio vlastite vrijednosti i redirect URI-je izvan alata (tajne nisu unesene u upite) i provjerio prijavu lokalno i na Renderu | [PR #20](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/pull/20), [PR #53](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/pull/53), [Zadatak #23](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/issues/23) |
| Oblikovanje sustava i tumačenje zadatka | Pomoć pri donošenju odluka | Prijedloge tumačenja projektnog zadatka, arhitekture sustava, modela podataka, korisničkog sučelja (UI/UX) i tijeka korištenja aplikacije za pojedine uloge | Tim je prijedloge raspravio na tjednim sastancima, odbacio one koji nisu odgovarali zadatku ili su bili logički slabiji od vlastitih ideja i sam donio konačne odluke | [2. Analiza zahtjeva](2-Analiza-zahtjeva.md), [4. Arhitektura i oblikovanje](4-Arhitektura-i-oblikovanje.md), [A.1 Evidencija sastanaka](A-Pregled-aktivnosti-grupe.md#a1-evidencija-sastanaka) |
| 1. Opseg projekta, odjeljak 1.4 | Istraživanje | Pregled postojećih platformi za rezervaciju izleta (GetYourGuide, Viator) | Tim je otvorio i provjerio navedene izvore te zadržao samo tvrdnje koje izvori potvrđuju | [1.4 Postojeći pristupi](1-Projekt.md#14-postojeći-pristupi-i-opravdanost-projekta) |
| Poslužiteljski i klijentski kod | Objašnjenje pogrešaka, pregled koda | Objašnjenja pogrešaka i prijedloge izmjena | Član odgovoran za zadatak primijenio je samo izmjene koje razumije i provjerio ih pokretanjem aplikacije | `backend/`, `frontend/` |

Stranice i artefakti koji nisu navedeni iznad izrađeni su bez pomoći generativne umjetne inteligencije.

### Provjera generiranog sadržaja

| Provjera | Kako je provedena | Dokaz |
| --- | --- | --- |
| Činjenična točnost | Tvrdnje o funkcionalnostima, ponašanju i arhitekturi uspoređene su s kodom i pokrenutom aplikacijom | Pregled PR-ova na [GitHubu](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/pulls?q=is%3Apr) |
| Nema tvrdnji o neostvarenim funkcionalnostima | Funkcionalnosti koje nisu dovršene označene su kao "U razvoju" ili "planirano" | [README - Opis aplikacije](../README.md#opis-aplikacije) |
| Dosljednost s drugim stranicama | Nazivi i ID-ovi (FR, NF, UC, ADR) provjereni su između Analize zahtjeva, Obrazaca uporabe i Arhitekture i oblikovanja | [2. Analiza zahtjeva](2-Analiza-zahtjeva.md), [3. Obrasci uporabe](3-Obrasci-uporabe.md), [4. Arhitektura i oblikovanje](4-Arhitektura-i-oblikovanje.md) |
| Izvori dijagrama | Generirani PlantUML je iscrtan, ispravljen i uspoređen s implementiranim sustavom | `docs/puml/*.puml` i `docs/assets/diagrams/*.svg` |
| Povjerljivi sadržaj | U alate nisu uneseni pristupni podaci ni osobni podaci korisnika; tajne se čuvaju isključivo u `backend/.env` | [Zadatak #43](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/issues/43) |

### Pogreške pronađene u izlazu umjetne inteligencije

| Problem | Kako je pronađen | Rješenje |
| --- | --- | --- |
| Općenito je primijećeno da alati ponekad predlažu logički slabija rješenja oblikovanja sustava, korisničkog sučelja (UI/UX) i tijeka korištenja aplikacije te pogrešno ili preopćenito tumače projektni zadatak | Usporedba prijedloga s odobrenim zadatkom i rasprava na tjednim sastancima | Takvi prijedlozi nisu preuzeti; odluke o oblikovanju, sučelju i tijeku korištenja donio je tim, a AI je služio samo kao jedan od izvora ideja |
| Nacrt dokumentacije opisivao je digitalnog vodiča (karta s položajem korisnika i obavijesti o obližnjim znamenitostima) i obavijesti o otkazivanju unutar aplikacije kao implementirane funkcionalnosti | Usporedba opisa s pokrenutom aplikacijom i kodom: klijentski dio za kartu i obavijesti ne postoji, a servis za karte i lokacije još je otvoren zadatak | Funkcionalnosti su u README-u premještene pod "U razvoju" (FR-16, FR-17, UC-13), a Leaflet je u tablici tehnologija označen kao planiran; [Zadatak #25](https://github.com/MatijaKovacic12/ProgiProjekt-11.4/issues/25) |
