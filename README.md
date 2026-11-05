# [Naziv aplikacije] — upravljanje izletima i turističkim aktivnostima

Web-aplikacija u kojoj turisti pretražuju i rezerviraju izlete, a turistička agencija upravlja ponudom, terminima, rezervacijama i zaposlenicima.

**Projekt na kolegiju:** Ovu je aplikaciju razvio studentski tim u sklopu kolegija [Programsko inženjerstvo](https://www.fer.unizg.hr/predmet/proinz) na Fakultetu elektrotehnike i računarstva Sveučilišta u Zagrebu (FER).

## Opis aplikacije

Turističke agencije ponudu izleta, termine i rezervacije često vode ručno ili u odvojenim alatima, pa korisnik teško vidi koliko je mjesta stvarno slobodno, a agencija teško prati popunjenost. Aplikacija na jednom mjestu povezuje turiste, koji pretražuju ponudu i rezerviraju termine, i zaposlenike agencije (administratore, organizatore, vodiče i voditelja agencije), koji upravljaju izletima, terminima, rezervacijama i ulogama. Sustav pri svakoj rezervaciji provjerava slobodna mjesta, pa termin ne može biti prebukiran.

**Ključne funkcionalnosti (implementirano)**

- Pretraživanje izleta bez prijave prema datumu, lokaciji, kategoriji, trajanju, cijeni i broju osoba te pregled detalja izleta sa znamenitostima i dodatnim uslugama.
- Prijava putem vanjskog pružatelja identiteta (OAuth 2.0) bez pohrane lozinki.
- Rezervacija termina za odabrani broj osoba uz dodatne usluge, izračun ukupne cijene, provjeru slobodnih mjesta i potvrdu e-poštom.
- Pregled i otkazivanje vlastitih rezervacija.
- Administracija: unos i izmjena izleta, dodavanje i otkazivanje termina, pretraživanje i otkazivanje rezervacija.
- Dodjela uloga zaposlenicima (administrator, organizator, vodič) prema adresi e-pošte.

**U razvoju:** raspoređivanje vodiča na termine i pregled termina za vodiče (FR-14, FR-19), statistika poslovanja s izvozom u CSV (FR-15, FR-16), automatsko zaključivanje završenih termina (FR-22) i obavijesti o otkazivanju unutar aplikacije (UC-13). **[PROVJERITI: stanje digitalnog vodiča na mobilnom uređaju (FR-17, FR-18) — poslužiteljski dio za znamenitosti u blizini postoji.]**

Za potpuni opseg, zahtjeve, oblikovanje i dokaze ispitivanja pogledajte [docs](docs/Home.md).

<a id="deployment"></a>
## Postavljanje (engl. deployment)

**Javno dostupna aplikacija:** [Javni URL]

**Isprobajte:** Bez prijave otvorite ponudu izleta, zadajte kriterij (npr. kategoriju i raspon cijene) i otvorite detalje izleta. Nakon prijave odaberite termin, broj osoba i dodatne usluge, potvrdite rezervaciju i provjerite je u popisu „Moje rezervacije”.

**Demonstracija:** Pretraživanje izleta ne zahtijeva korisnički račun. Za rezervaciju se prijavite vlastitim [Microsoft / Google] računom. Pristup administratorskim funkcijama za ocjenjivanje dodjeljuje tim na zahtjev — javite se [kontakt tima] s adresom e-pošte kojom ste se prijavili.

**Ograničenja:** Funkcionalnosti navedene pod „U razvoju” još nisu dostupne. Aplikacija se izvodi kao jedna instanca, a ako slanje e-pošte ne uspije, poruka se ne šalje ponovno (rezervacija ostaje valjana). Popis poznatih ograničenja nalazi se u [arhitekturnim rizicima](docs/4-Arhitektura-i-oblikovanje.md#47-arhitekturni-rizici-i-tehnički-dug).

[Vodič za postavljanje i pokretanje](docs/6-Postavljanje-instalacija-i-konfiguracija.md) opisuje smještaj aplikacije (engl. hosting) koji tim stvarno koristi.

<a id="quick-start"></a>
## Brzi početak / instalacija

**Preduvjeti:** Java [17/21], PostgreSQL [verzija] (lokalno ili u Dockeru), [Node.js verzija i npm — za web-klijent], OAuth klijent registriran kod Microsofta (Microsoft Entra ID) i pristupni podaci SMTP poslužitelja.

```bash
git clone [repository URL]
cd [repository directory]

# poslužitelj (Spring Boot)
cd [backend direktorij]
cp .env.example .env        # upišite vlastite vrijednosti
./mvnw spring-boot:run      # [PROVJERITI: Maven ili Gradle]

# web-klijent (u drugom terminalu)
cd [frontend direktorij]
npm install
npm run dev
```

**Adresa:** API na `http://localhost:8080` [PROVJERITI], web-klijent na `[adresa koju ispiše razvojni poslužitelj]`.

**Konfiguracija:** Sve tajne učitavaju se iz varijabli okruženja i nikad se ne spremaju u repozitorij (NF-06). Popis varijabli nalazi se u `.env.example`: pristup bazi (URL, korisnik, lozinka), OAuth klijenti (client ID i secret za Microsoft, po želji Google — dobivaju se registracijom aplikacije kod pružatelja s redirect URI-jem `[…]/login/oauth2/code/{provider}`) i SMTP pristupni podaci. **[PROVJERITI točne nazive varijabli prema `application.properties`/`application.yml`.]**

Provjera: otvorite `http://localhost:8080/api/trips` — odgovor je JSON popis izleta (prazan popis `[]` ako baza nema podataka). Za detaljno postavljanje pogledajte [projektnu dokumentaciju](docs/Home.md).

## Tehnologije

| Tehnologija ili alat | Uloga u ovom projektu | Razlog uporabe ili važno ograničenje |
| --- | --- | --- |
| Spring Boot (Java) | Poslužiteljska aplikacija: REST API, poslovna pravila izleta, termina i rezervacija. | Jedna aplikacija s jednom transakcijom za rezervaciju (provjera slobodnih mjesta i spremanje), potrebno da termin ne bude prebukiran (NF-04). |
| Spring Security, OAuth 2.0 / OpenID Connect | Prijava putem Microsoft (i po želji Google) računa, sjednica i provjera uloga. | Sustav ne pohranjuje lozinke (C-02); prijava ovisi o dostupnosti vanjskog pružatelja identiteta. |
| PostgreSQL + Spring Data JPA | Pohrana korisnika, izleta, termina, lokacija, usluga i rezervacija. | Rezervacije su vezane uz korisnike, termine i usluge; cijena se pamti u trenutku rezervacije pa promjena cjenika ne mijenja postojeće rezervacije. |
| SMTP usluga e-pošte | Slanje potvrda rezervacije i otkazivanja (FR-11). | E-pošta se šalje nakon spremanja rezervacije; neuspjeh slanja ne poništava rezervaciju (NF-10). |
| [Frontend tehnologija, npr. React] | Responzivno web-sučelje za računalo, tablet i mobitel (NF-01). | [Razlog specifičan za projekt] |
| [Kartografska usluga, npr. OpenStreetMap / Leaflet] | Prikaz znamenitosti izleta na karti. | Karta ovisi o vanjskoj usluzi; geolokacija u pregledniku radi samo preko HTTPS-a (C-04). |
| [Platforma za smještaj, npr. Render] | Javno postavljena aplikacija i baza. | [Ograničenje, npr. besplatni plan uspavljuje aplikaciju nakon neaktivnosti] |

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

**Izjava:** Tim je koristio alate umjetne inteligencije [DeepSeek, Claude, …] kao pomoć pri izradi početnih skica UML dijagrama (PlantUML), strukturiranju projektne dokumentacije i [navedite ostalo, npr. objašnjenja pogrešaka u kodu]. Sve generirane skice tim je usporedio sa stvarnim kodom i zahtjevima te ih ispravio tamo gdje se nisu podudarale; svaki član može objasniti dio koda i dokumentacije za koji je odgovoran. U alate nisu uneseni pristupni podaci ni osobni podaci korisnika. [Uskladite s timom prije predaje.]

<a id="code-of-conduct-and-support"></a>
## Kodeks ponašanja i podrška [![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md)

Od članova tima očekuje se pridržavanje **Kodeksa ponašanja studenata** Fakulteta elektrotehnike i računarstva Sveučilišta u Zagrebu, pravila timskog rada na kolegiju i [Etičkog kodeksa IEEE-a](https://www.ieee.org/about/corporate/governance/p7-8.html). [Postavite poveznicu na `CODE_OF_CONDUCT.md` ako se ta datoteka koristi u repozitoriju.]

---

Struktura dokumentacije prilagođena je predlošku kolegija Programsko inženjerstvo FER-a autora [Vlade Sruka](https://www.fer.unizg.hr/vlado.sruk), licenciranom pod [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/). Aplikaciju i njezin sadržaj izradio je gore navedeni studentski tim.
