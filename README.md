# Progi-Tours - upravljanje izletima i turističkim aktivnostima

Web-aplikacija u kojoj turisti pretražuju i rezerviraju izlete, a turistička agencija upravlja ponudom, terminima, rezervacijama i zaposlenicima.

**Projekt na kolegiju:** Ovu je aplikaciju razvio studentski tim u sklopu kolegija [Programsko inženjerstvo](https://www.fer.unizg.hr/predmet/proinz) na Fakultetu elektrotehnike i računarstva Sveučilišta u Zagrebu (FER).

## Opis aplikacije

Turističke agencije ponudu izleta, termine i rezervacije često vode ručno ili u odvojenim alatima, pa korisnik teško vidi koliko je mjesta stvarno slobodno, a agencija teško prati popunjenost. Progi-Tours na jednom mjestu povezuje turiste, koji pretražuju ponudu i rezerviraju termine, i zaposlenike agencije (administratore, organizatore, vodiče i voditelja agencije), koji upravljaju izletima, terminima, rezervacijama i ulogama. Sustav pri svakoj rezervaciji provjerava slobodna mjesta, pa termin ne može biti prebukiran.

**Ključne funkcionalnosti (implementirano)**

- Pretraživanje izleta bez prijave prema datumu, lokaciji, kategoriji, trajanju, cijeni i broju osoba te pregled detalja izleta sa znamenitostima i dodatnim uslugama.
- Prijava putem vanjskog pružatelja identiteta (OAuth 2.0) bez pohrane lozinki.
- Rezervacija termina za odabrani broj osoba uz dodatne usluge, izračun ukupne cijene, provjeru slobodnih mjesta i potvrdu e-poštom.
- Pregled i otkazivanje vlastitih rezervacija.
- Administracija: unos i izmjena izleta, dodavanje i otkazivanje termina, pretraživanje i otkazivanje rezervacija. Otkazivanjem termina ili izleta otkazuju se i njegove potvrđene rezervacije, a korisnici dobivaju obavijest e-poštom.
- Raspoređivanje vodiča na termine uz provjeru preklapanja i pregled dodijeljenih termina za vodiče.
- Statistika poslovanja za odabrano razdoblje s izvozom u CSV.
- Automatsko zaključivanje rezervacija po završetku termina.
- Dodjela uloga zaposlenicima (administrator, organizator, vodič) prema adresi e-pošte.

**U razvoju:** digitalni vodič na mobilnom uređaju, odnosno karta s položajem korisnika i obavijesti o obližnjim znamenitostima (FR-16, FR-17), te obavijesti o otkazivanju unutar aplikacije (UC-13). Poslužiteljski dio za znamenitosti izleta već postoji.

Potpuni opseg, zahtjevi i oblikovanje opisani su u mapi `docs`.

<a id="deployment"></a>
## Postavljanje (engl. deployment)

**Javno dostupna aplikacija:** [https://progi-tours.onrender.com](https://progi-tours.onrender.com)

**Isprobajte:** Bez prijave otvorite ponudu izleta, zadajte kriterij (npr. kategoriju i raspon cijene) i otvorite detalje izleta. Nakon prijave odaberite termin, broj osoba i dodatne usluge, potvrdite rezervaciju i provjerite je u popisu "Moje rezervacije".

**Demonstracija:** Pretraživanje izleta ne zahtijeva korisnički račun. Za rezervaciju se prijavite vlastitim računom. Pristup administratorskim funkcijama za ocjenjivanje dodjeljuje tim na zahtjev, prema adresi e-pošte kojom ste se prijavili.

**Ograničenja:** Funkcionalnosti navedene pod "U razvoju" još nisu dostupne. Aplikacija se izvodi na besplatnom planu platforme Render, koji je uspavljuje nakon razdoblja neaktivnosti, pa prvo učitavanje nakon toga može trajati i do minute. Aplikacija se izvodi kao jedna instanca, a ako slanje e-pošte ne uspije, poruka se ne šalje ponovno (rezervacija ostaje valjana). Popis poznatih ograničenja nalazi se u [arhitekturnim rizicima](docs/4-Arhitektura-i-oblikovanje.md#47-arhitekturni-rizici-i-tehnički-dug).

<a id="quick-start"></a>
## Brzi početak / instalacija

**Preduvjeti:** Java 25, PostgreSQL 17 (lokalno ili u Dockeru), Node.js 20 i npm (za web-klijent), OAuth klijent registriran kod Microsofta (Microsoft Entra ID) i Googlea te pristupni podaci SMTP poslužitelja.

```bash
git clone https://github.com/MatijaKovacic12/ProgiProjekt-11.4.git
cd ProgiProjekt-11.4

# poslužitelj (Spring Boot, Maven)
cd backend
cp .env.example .env        # upišite vlastite vrijednosti
./mvnw spring-boot:run

# web-klijent (u drugom terminalu, iz korijena repozitorija)
cd frontend
npm install
npm run dev
```

**Adresa:** API na `http://localhost:8080`, web-klijent na `http://localhost:5173` (Vite razvojni poslužitelj prosljeđuje zahtjeve na `/api` poslužitelju).

**Konfiguracija:** Sve tajne učitavaju se iz varijabli okruženja i nikad se ne spremaju u repozitorij (NF-06). Lokalno se upisuju u datoteku `backend/.env` (kopija predloška `backend/.env.example`; `.env` je u `.gitignore`), a na Renderu kao varijable okruženja, koje imaju prednost. Varijable: `DB_URL`, `DB_TEST_URL` (baza za testove), `DB_USERNAME`, `DB_PASSWORD`; `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `MICROSOFT_CLIENT_ID`, `MICROSOFT_CLIENT_SECRET` (dobivaju se registracijom aplikacije kod pružatelja s redirect URI-jem `http://localhost:8080/login/oauth2/code/{provider}` lokalno, odnosno `https://progi-tours.onrender.com/login/oauth2/code/{provider}` na Renderu); `MAIL_USERNAME`, `MAIL_PASSWORD` (SMTP); `FRONTEND_URL` (lokalno `http://localhost:5173`). Zadanu vrijednost imaju samo `MICROSOFT_CLIENT_ID` i `MICROSOFT_CLIENT_SECRET`, pa bez njih prijava Microsoft računom ne radi; ostale varijable su obavezne.

Provjera: otvorite `http://localhost:8080/api/trips` - odgovor je JSON popis izleta (prazan popis `[]` ako baza nema podataka). Ogledni podaci mogu se učitati skriptom `db/SeedDatabase.sql`.

## Tehnologije

| Tehnologija ili alat | Uloga u ovom projektu | Razlog uporabe ili važno ograničenje |
| --- | --- | --- |
| Spring Boot 4 (Java 25) | Poslužiteljska aplikacija: REST API, poslovna pravila izleta, termina i rezervacija. | Jedna aplikacija s jednom transakcijom za rezervaciju (provjera slobodnih mjesta i spremanje), potrebno da termin ne bude prebukiran (NF-04). |
| Spring Security, OAuth 2.0 / OpenID Connect | Prijava putem Microsoft i Google računa, sjednica i provjera uloga. | Sustav ne pohranjuje lozinke (C-02); prijava ovisi o dostupnosti vanjskog pružatelja identiteta. |
| PostgreSQL 17 + Spring Data JPA, Flyway | Pohrana korisnika, izleta, termina, lokacija, usluga i rezervacija; shema se stvara migracijama. | Rezervacije su vezane uz korisnike, termine i usluge; cijena se pamti u trenutku rezervacije pa promjena cjenika ne mijenja postojeće rezervacije. |
| SMTP usluga e-pošte (Gmail) | Slanje potvrda rezervacije i otkazivanja (FR-10). | E-pošta se šalje nakon spremanja rezervacije; neuspjeh slanja ne poništava rezervaciju (NF-10). |
| React 19 + Vite | Responzivno web-sučelje za računalo, tablet i mobitel (NF-01). | Klijent se gradi u statičke datoteke koje poslužuje Spring Boot aplikacija, pa klijent i API dijele domenu i OAuth sesijski kolačić radi bez CORS-a (ADR-04). |
| Leaflet i Geolocation API preglednika _(planirano)_ | Prikaz znamenitosti izleta i položaja korisnika na karti. | Karta ovisi o vanjskoj usluzi kartografskih pločica; geolokacija u pregledniku radi samo preko HTTPS-a (C-04), a položaj se ne šalje na poslužitelj (ADR-05). |
| Docker, Render | Javno postavljena aplikacija (jedna Docker slika s klijentom i API-jem) i baza. | Besplatni plan uspavljuje aplikaciju nakon neaktivnosti, pa je prvi zahtjev nakon toga sporiji; izvodi se jedna instanca. |

Kompromisi oblikovanja objašnjeni su u [arhitekturi i oblikovanju](docs/4-Arhitektura-i-oblikovanje.md).

<a id="team-members"></a>
## Članovi tima

| Član | Glavna uloga ili doprinos |
| --- | --- |
| Matija Kovačić | voditelj tima |
| Leon Bačanović | - |
| Luka Kranjčec | - |
| Dorijan Ferenec | - |
| Roko Babić | - |
| Lovro Gale | - |
| Marko Zelenović | - |

Uloge se tijekom projekta mogu mijenjati. Repozitorij i dogovoreni alat za praćenje zadataka (engl. task tracker) služe kao dodatni dokazi timskog rada.

<a id="ai-usage"></a>
**Izjava:** Tim je koristio alate umjetne inteligencije Claude i ChatGPT kao pomoć pri generiranju koda poslužiteljskog i klijentskog dijela, jediničnih testova i konfiguracije (OAuth, Spring Security, Docker, Render), izradi početnih skica UML dijagrama (PlantUML), strukturiranju projektne dokumentacije, istraživanju postojećih rješenja, objašnjavanju pogrešaka u kodu i pregledu koda. Sav generirani kod i tekst tim je usporedio sa stvarnim zahtjevima i pokrenutom aplikacijom te ga ispravio tamo gdje se nisu podudarali; svaki član može objasniti dio koda i dokumentacije za koji je odgovoran. U alate nisu uneseni pristupni podaci ni osobni podaci korisnika. Detaljan zapis nalazi se u [projektnoj dokumentaciji](docs/Home.md#izjava-o-uporabi-umjetne-inteligencije).

<a id="code-of-conduct-and-support"></a>
## Kodeks ponašanja i podrška

Od članova tima očekuje se pridržavanje **Kodeksa ponašanja studenata** Fakulteta elektrotehnike i računarstva Sveučilišta u Zagrebu, pravila timskog rada na kolegiju i [Etičkog kodeksa IEEE-a](https://www.ieee.org/about/corporate/governance/p7-8.html).

---

Struktura dokumentacije prilagođena je predlošku kolegija Programsko inženjerstvo FER-a autora [Vlade Sruka](https://www.fer.unizg.hr/vlado.sruk), licenciranom pod [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/). Aplikaciju i njezin sadržaj izradio je gore navedeni studentski tim.
