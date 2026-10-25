# 2. Analiza zahtjeva

**Cilj:** Iz odobrenog opsega projekta izvesti prepoznatljive, prioritetizirane i provjerljive zahtjeve. Zahtjevi se mogu mijenjati; pri završnoj predaji moraju odražavati dogovorenu aplikaciju.

## 2.1 Funkcijski zahtjevi

**Cilj:** Navesti što aplikacija mora omogućiti korisnicima, odakle pojedina mogućnost proizlazi i koji se opažljivi rezultat smatra prihvaćanjem.

| ID | Opis | Prioritet | Izvor | Kriteriji prihvaćanja |
| --- | --- | --- | --- | --- |
| F-001 | Sustav omogućuje registraciju korisničkog računa pomoću adrese e-pošte. | Visok | Odobreni zadatak (više vrsta korisnika s ovlastima) | Korisnik unosi ime, prezime, e-poštu i lozinku te nakon uspješne registracije može se prijaviti u sustav. Registracija s već korištenom e-poštom se odbija uz poruku o pogrešci. |
| F-002 | Sustav omogućuje prijavu i odjavu korisnika. | Visok | Odobreni zadatak | Korisnik s ispravnim podacima prijavljuje se i vidi funkcije svoje uloge; s neispravnim podacima prijava se odbija. Nakon odjave zaštićene stranice nisu dostupne. |
| F-003 | Sustav razlikuje uloge korisnik, organizator i voditelj agencije te svakoj omogućuje samo pripadajuće radnje. | Visok | Odobreni zadatak | Korisnik bez uloge organizatora ne može dodavati ni mijenjati izlete; korisnik bez uloge voditelja ne može pristupiti statistici (ni preko izravne poveznice ni preko API-ja). |
| F-004 | Organizator može dodati novi izlet s nazivom, opisom, kategorijom, trajanjem, lokacijom, mjestom polaska, cijenom, maksimalnim brojem sudionika, potrebnim brojem vodiča i dodatnim uslugama. | Visok | Odobreni zadatak | Nakon spremanja izlet se pojavljuje u popisu sa svim unesenim podacima. Izlet bez obaveznih podataka (naziv, kategorija, cijena, kapacitet) ne sprema se i prikazuje se poruka o nedostajućim poljima. |
| F-005 | Organizator može izmijeniti i obrisati (deaktivirati) postojeći izlet. | Visok | Odobreni zadatak | Izmijenjeni podaci vidljivi su korisnicima odmah nakon spremanja. Deaktivirani izlet ne prikazuje se u pretrazi; postojeće rezervacije za njega ostaju vidljive organizatoru. |
| F-006 | Izlet pripada jednoj kategoriji aktivnosti (obilazak gradova i kulturnih znamenitosti, planinarenje, biciklistička tura, rafting, izlet brodom ili druga aktivnost). | Visok | Odobreni zadatak | Pri unosu izleta organizator bira kategoriju s popisa; kategorija se prikazuje u detaljima izleta i može se koristiti kao filtar. |
| F-007 | Organizator može za izlet definirati jedan ili više termina održavanja. | Visok | Odobreni zadatak | Organizator dodaje termin (datum i vrijeme početka); izlet s više termina prikazuje sve buduće termine i broj slobodnih mjesta za svaki. |
| F-008 | Organizator može definirati dodatne usluge (npr. organizirani prijevoz, najam opreme) s nazivom i cijenom te ih povezati s izletom. | Visok | Odobreni zadatak | Dodatna usluga povezana s izletom prikazuje se u detaljima izleta i nudi se pri izradi rezervacije. |
| F-009 | Korisnik može pregledati popis dostupnih izleta s cijenom i brojem slobodnih mjesta po terminu. | Visok | Odobreni zadatak | Popis prikazuje samo aktivne izlete s budućim terminima; broj slobodnih mjesta jednak je kapacitetu umanjenom za broj osoba u aktivnim (zahtjev ili potvrđena) rezervacijama. |
| F-010 | Korisnik može pretraživati izlete prema terminu, lokaciji, vrsti aktivnosti, trajanju, cijeni, broju sudionika i dodatnim uslugama. | Visok | Odobreni zadatak | Za svaki kriterij, i za njihovu kombinaciju, prikazuju se samo izleti koji ga zadovoljavaju. Filtar „broj sudionika = N” prikazuje samo termine s barem N slobodnih mjesta. |
| F-011 | Korisnik može otvoriti detalje izleta: opis, termine, cijenu, slobodna mjesta, mjesto polaska, dodatne usluge i informacije o lokaciji održavanja. | Visok | Odobreni zadatak | Odabirom izleta s popisa prikazuju se svi navedeni podaci. |
| F-012 | Prijavljeni korisnik može izraditi rezervaciju za odabrani termin, određeni broj osoba i odabrane dodatne usluge. | Visok | Odobreni zadatak | Nakon slanja rezervacija se sprema sa statusom „zahtjev za rezervaciju”; prije slanja prikazuje se ukupna cijena (cijena × broj osoba + dodatne usluge). |
| F-013 | Sustav onemogućuje rezervaciju koja bi premašila maksimalan broj sudionika termina. | Visok | Odobreni zadatak | Rezervacija za više osoba od broja slobodnih mjesta se odbija uz poruku; popunjeni termin označen je kao „popunjeno” i ne nudi rezervaciju. |
| F-014 | Sustav za svaku rezervaciju vodi status: zahtjev za rezervaciju, potvrđena, otkazana, realizirana. | Visok | Odobreni zadatak | Rezervacija u svakom trenutku ima točno jedan status; organizator i korisnik vide trenutačni status. Otkazivanjem se mjesta vraćaju u slobodan kapacitet termina. |
| F-015 | Organizator upravlja rezervacijama: pregledava, potvrđuje, otkazuje i označava ih realiziranima. | Visok | Odobreni zadatak | Organizator može promijeniti status rezervacije; korisnik bez uloge organizatora ne može mijenjati tuđe rezervacije. |
| F-016 | Korisnik može otkazati vlastitu rezervaciju. | Srednji | Procjena tima | Korisnik otkazuje rezervaciju u statusu „zahtjev” ili „potvrđena” prije termina; status postaje „otkazana”. |
| F-017 | Nakon potvrde rezervacije korisniku se šalje potvrda e-poštom. | Visok | Odobreni zadatak | Promjenom statusa u „potvrđena” na korisnikovu adresu stiže poruka s izletom, terminom, mjestom polaska, brojem osoba, dodatnim uslugama i ukupnom cijenom. |
| F-018 | Korisnik može pregledati sve svoje rezervacije i sve relevantne podatke potvrđene rezervacije. | Visok | Odobreni zadatak | Na stranici „Moje rezervacije” prikazuju se sve korisnikove rezervacije sa statusom; otvaranjem potvrđene rezervacije vide se svi podaci navedeni u F-017. |
| F-019 | Organizator vodi evidenciju vodiča i dodjeljuje vodiče pojedinim terminima izleta. | Visok | Odobreni zadatak | Organizator dodaje vodiča i dodjeljuje ga terminu; termin s manje dodijeljenih vodiča od potrebnog broja označen je upozorenjem. |
| F-020 | Sustav sprječava dodjelu istog vodiča na termine koji se vremenski preklapaju. | Srednji | Procjena tima | Pokušaj dodjele vodiča na preklapajući termin se odbija uz poruku. |
| F-021 | Organizator vodi evidenciju raspoloživosti ostalih resursa (prijevoz, oprema) i njihovu dodjelu terminima. | Srednji | Odobreni zadatak | Organizator unosi resurs s količinom/kapacitetom, dodjeljuje ga terminu i vidi koliko je resursa slobodno u odabranom razdoblju. |
| F-022 | Voditelj agencije pregledava statistiku: broj rezervacija, popunjenost izleta, ostvareni prihod, najtraženije aktivnosti i dodatne usluge. | Visok | Odobreni zadatak | Za odabrano razdoblje prikazane vrijednosti odgovaraju ručno izračunatim vrijednostima nad oglednim podacima. |
| F-023 | Voditelj agencije može izvesti odabrane podatke i izvještaje u standardnom formatu (CSV). | Visok | Odobreni zadatak | Preuzeta CSV datoteka otvara se u programu za tablične izračune (npr. Excel) i sadrži iste podatke kao prikazani izvještaj. |
| F-024 | Organizator definira točke interesa izleta (naziv, kratak opis, značaj, zanimljivosti, GPS koordinate). | Visok | Odobreni zadatak | Spremljena točka interesa povezana je s izletom i dostupna u mobilnom vodiču tog izleta. |
| F-025 | Mobilni vodič na temelju GPS lokacije korisnika određuje nalazi li se korisnik u blizini točke interesa izleta. | Visok | Odobreni zadatak | Kada je udaljenost korisnika od točke interesa manja od zadanog radijusa (npr. 100 m), točka se označava kao „u blizini”. |
| F-026 | Kada se korisnik približi točki interesa, aplikacija prikazuje njezine osnovne informacije. | Visok | Odobreni zadatak | Ulaskom u radijus točke automatski se prikazuju naziv, kratak opis, značaj i zanimljivosti; ista točka ne prikazuje se ponovno dok korisnik ne izađe i ponovno uđe u radijus. |
| F-027 | Aplikacija prikazuje položaj korisnika i točke interesa izleta na karti. | Visok | Odobreni zadatak | Karta prikazuje oznaku trenutačnog položaja korisnika i oznake svih točaka interesa izleta; odabirom oznake otvaraju se informacije o točki. Ako korisnik odbije pristup lokaciji, karta i dalje prikazuje točke interesa. |

## 2.2 Nefunkcijski zahtjevi

**Cilj:** Odrediti važna svojstva kvalitete i uvjete rada odobrene aplikacije na način koji tim može razumno provjeriti.

| ID | Opis | Prioritet | Izvor | Način provjere |
| --- | --- | --- | --- | --- |
| NF-01 | Korisničko sučelje responzivno je i upotrebljivo na računalu, tabletu i mobilnom uređaju (širine zaslona od 360 px naviše). | Visok | Odobreni zadatak | Pregled ključnih tijekova (pretraga, rezervacija, mobilni vodič, statistika) na širinama 360 px, 768 px i 1366 px; bez horizontalnog pomicanja i skrivenih kontrola. |
| NF-02 | Sustav podržava istovremeni rad većeg broja korisnika (najmanje 50 istodobnih korisnika) uz vrijeme odziva pretrage kraće od 2 s. | Visok | Odobreni zadatak; vrijednost procjena tima | Test opterećenja (npr. k6 ili JMeter) s 50 virtualnih korisnika i mjerenje vremena odziva. |
| NF-03 | Kapacitet termina ne smije se premašiti ni pri istodobnim rezervacijama istog termina. | Visok | Odobreni zadatak | Testni scenarij: istodobno poslati više rezervacija za posljednja slobodna mjesta; provjeriti da je ukupan broj rezerviranih osoba ≤ kapaciteta. |
| NF-04 | Pristup funkcijama ograničen je ulogom na strani poslužitelja, ne samo skrivanjem elemenata sučelja. | Visok | Odobreni zadatak | Testni scenarij: pozvati zaštićene API rute s tokenom korisnika bez ovlasti i provjeriti odgovor 403. |
| NF-05 | Lozinke se pohranjuju isključivo kao hash (npr. bcrypt). | Visok | Procjena tima (sigurnost) | Pregled sheme baze i koda za registraciju. |
| NF-06 | Komunikacija klijenta i poslužitelja odvija se preko HTTPS-a. | Visok | Procjena tima (sigurnost) | Pregled konfiguracije postavljene aplikacije; HTTP zahtjev preusmjerava se na HTTPS. |
| NF-07 | Lokacija korisnika koristi se samo uz njegovo dopuštenje, samo tijekom korištenja vodiča i ne pohranjuje se na poslužitelju. | Visok | Procjena tima (privatnost, GDPR) | Pregled koda i mrežnog prometa vodiča; provjera da se koordinate ne šalju poslužitelju. |
| NF-08 | Neuspjelo slanje e-pošte ne poništava potvrdu rezervacije; greška se bilježi i poruku je moguće ponovno poslati. | Srednji | Procjena tima | Testni scenarij s nedostupnim SMTP poslužiteljem: rezervacija ostaje potvrđena, greška je zabilježena. |
| NF-09 | Aplikacija radi u aktualnim verzijama preglednika Chrome, Firefox, Safari i Edge, a mobilni vodič u mobilnom pregledniku na Androidu i iOS-u. | Srednji | Odobreni zadatak | Ručni pregled ključnih tijekova u navedenim preglednicima. |
| NF-10 | Sučelje je na hrvatskom jeziku. | Srednji | Procjena tima | Pregled svih zaslona. |

## 2.3 Dionici i akteri

**Cilj:** Utvrditi uloge koje utječu na zahtjeve i razlikovati dionike od aktera koji komuniciraju s aplikacijom.

| ID uloge ili aktera | Odnos prema sustavu | Relevantni zahtjevi |
| --- | --- | --- |
| A-01 Posjetitelj (neprijavljeni korisnik) | Akter; pregledava i pretražuje ponudu izleta. | F-001, F-009, F-010, F-011 |
| A-02 Korisnik (turist) | Akter; rezervira izlete, prati rezervacije i koristi mobilni vodič tijekom izleta. | F-002, F-012, F-016, F-017, F-018, F-025, F-026, F-027 |
| A-03 Organizator | Akter; upravlja izletima, terminima, cijenama, kapacitetima, dodatnim uslugama, rezervacijama, vodičima, resursima i točkama interesa. | F-004 – F-008, F-015, F-019 – F-021, F-024 |
| A-04 Voditelj turističke agencije | Akter; pregledava statistiku i izvozi izvještaje. | F-022, F-023 |
| A-05 Sustav e-pošte | Vanjski akter; dostavlja potvrde rezervacija. | F-017, NF-08 |
| A-06 GPS / geolokacija uređaja | Vanjski akter; daje trenutačnu lokaciju korisnika. | F-025, F-027, NF-07 |
| D-01 Vodič | Dionik; dodjeljuje mu se termin, ali u ovom opsegu ne koristi sustav izravno. | F-019, F-020 |
| D-02 Nastavnici kolegija | Dionici; ocjenjuju projekt, nisu akteri aplikacije. | — |

---

## 2.4 Ograničenja i pretpostavke

**Cilj:** Zabilježiti projektno specifične granice i pretpostavke koje utječu na zahtjeve ili oblikovanje, zajedno s njihovim izvorom i praktičnom posljedicom.

| ID | Tip | Opis ograničenja ili pretpostavke | Izvor | Inženjerska posljedica / Provjera |
| --- | --- | --- | --- | --- |
| C-01 | Ograničenje | Aplikacija mora biti responzivna i raditi na računalu, tabletu i mobilnom uređaju. | Odobreni zadatak | Web-aplikacija s responzivnim sučeljem; mobilni vodič izveden kao dio iste web-aplikacije (ili PWA) umjesto zasebne nativne aplikacije. |
| C-02 | Ograničenje | Potvrda rezervacije dostavlja se e-poštom. | Odobreni zadatak | Potrebna integracija s SMTP poslužiteljem ili uslugom za slanje e-pošte. |
| C-03 | Ograničenje | Mobilni vodič koristi GPS lokaciju i prikazuje kartu. | Odobreni zadatak | Korištenje Geolocation API-ja preglednika (zahtijeva HTTPS) i kartografske usluge (npr. OpenStreetMap + Leaflet). |
| C-04 | Ograničenje | Podaci se moraju moći izvesti u standardnom formatu. | Odobreni zadatak | Izvoz u CSV na strani poslužitelja. |
| AS-01 | Pretpostavka | Korisnici na izletu imaju pametni telefon s GPS-om i mobilnim internetom. | Procjena tima | Provjeriti na terenskom testu; ako internet nije dostupan, razmotriti predučitavanje točaka interesa prije polaska. |
| AS-02 | Pretpostavka | Točnost GPS-a na otvorenom (oko 10–20 m) dovoljna je za radijus približavanja od oko 100 m. | Procjena tima | Terenski test na nekoliko točaka interesa; po potrebi prilagoditi radijus po točki. |
| AS-03 | Pretpostavka | Rezervaciju potvrđuje organizator ručno (nema online plaćanja u opsegu projekta). | Procjena tima | Potvrditi s nastavnikom na prvoj kontrolnoj točki. |
| AS-04 | Pretpostavka | Prihod se računa iz potvrđenih i realiziranih rezervacija (otkazane se ne računaju). | Procjena tima | Potvrditi s nastavnikom; provjeriti izračunom nad oglednim podacima. |

---

## 2.5 Značajne promjene zahtjeva

**Cilj:** Sačuvati razlog promjena koje mijenjaju dogovoreni opseg, prioritet ili kriterije prihvaćanja, uz istodobno održavanje prethodnih zahtjeva ažurnima.

| Datum | ID zahtjeva | Promjena i razlog | Dogovoreno s / dokaz |
| --- | --- | --- | --- |
|  |  |  |  |

---

## 2.6 Sljedivost zahtjeva

**Cilj:** Osigurati dvosmjernu sljedivost — povezati zahtjeve s opisanim ponašanjem, dijelovima sustava koji ih ostvaruju te ispitivanjem ili drugom provjerom.

| ID zahtjeva | Obrazac uporabe / modul | Arhitekturna realizacija | Ispitivanje ili provjera |
| --- | --- | --- | --- |
| F-001, F-002 | UC-001 Registracija i prijava | Web-klijent; API za autentifikaciju | Na čekanju (ispitni slučajevi nisu definirani) |
| F-003 | Upravljanje ovlastima | API — provjera uloga | Na čekanju |
| F-004 – F-008 | UC-002 Upravljanje izletima i terminima | Web-klijent (admin dio); API za izlete | Na čekanju |
| F-009 – F-011 | UC-003 Pretraživanje i pregled izleta | Web-klijent; API za izlete | Na čekanju |
| F-012 – F-014 | UC-004 Izrada rezervacije | Web-klijent; API za rezervacije | Na čekanju |
| F-015, F-016 | UC-005 Upravljanje rezervacijama | Web-klijent; API za rezervacije | Na čekanju |
| F-017 | Slanje potvrde rezervacije | Modul za slanje e-pošte | Na čekanju |
| F-018 | UC-006 Pregled vlastitih rezervacija | Web-klijent; API za rezervacije | Na čekanju |
| F-019 – F-021 | UC-007 Upravljanje vodičima i resursima | Web-klijent (admin dio); API za resurse | Na čekanju |
| F-022, F-023 | UC-008 Pregled statistike i izvoz | Web-klijent; API za statistiku | Na čekanju |
| F-024 | UC-009 Definiranje točaka interesa | Web-klijent (admin dio); API za točke interesa | Na čekanju |
| F-025 – F-027 | UC-010 Korištenje mobilnog vodiča | Mobilni vodič (web/PWA); kartografska usluga | Na čekanju |

---

## 2.7 Pregled zahtjeva

**Cilj:** Zabilježiti važne povratne informacije o zahtjevima i odluke koje iz njih proizlaze na dvije formalne kontrolne točke projekta, u 7. i 14. tjednu.

| Kontrolna točka | Povratna informacija ili nalaz | Odluka o zahtjevu ili opsegu |
| --- | --- | --- |
| Iteracija 1 |  |  |
| Demo 2 |  |  |
| Završni pregled |  |  |

---

>**Završna provjera:** Zahtjevi su aktualni, provjerljivi i povezani. Svaki važan zahtjev moguće je pratiti od njegova opisa i ponašanja preko mjesta realizacije u sustavu do odgovarajućeg ispitivanja ili druge provjere. Poveznice koriste stvarne identifikatore i nazive iz projektne dokumentacije.
