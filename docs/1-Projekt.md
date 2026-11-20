# 1. Opseg projekta

**Cilj:** Objasniti odobreni projektni zadatak vlastitim riječima tima. Čitatelj treba razumjeti problem, ciljane korisnike, cilj projekta i njegove granice bez prethodnog čitanja detaljnih zahtjeva.

## 1.1 Problem i cilj projekta

**Cilj:** Jasno razgraničiti problem koji se rješava od predloženog rješenja i stvarne koristi za korisnika.

Turistička agencija ponudu izleta, termine održavanja, rezervacije i raspored vodiča često vodi u odvojenim alatima ili ručno. Turist zato teško pronalazi izlet koji odgovara njegovim kriterijima i ne zna koliko je mjesta u terminu stvarno slobodno, a agencija teško prati popunjenost termina, raspoređuje vodiče i dobiva pregled poslovanja.

Cilj projekta Progi-Tours je web-aplikacija koja na jednom mjestu povezuje turiste i zaposlenike agencije. Turist bez prijave pretražuje ponudu prema datumu, lokaciji, kategoriji, trajanju, cijeni i broju osoba, a nakon prijave vanjskim računom rezervira termin uz dodatne usluge i dobiva potvrdu e-poštom. Zaposlenici agencije u istoj aplikaciji, prema svojoj ulozi, upravljaju izletima, terminima i rezervacijama, raspoređuju vodiče i pregledavaju statistiku poslovanja. Tijekom izleta turist na mobilnom uređaju dobiva digitalnog vodiča koji prikazuje znamenitosti na karti i obavještava ga kada se nađe u njihovoj blizini.

Očekivana korist je da turist vidi stvarno stanje ponude i slobodnih mjesta prije rezervacije, a agencija ima jedan izvor podataka o izletima, rezervacijama i vodičima. Sustav pri svakoj rezervaciji provjerava slobodna mjesta, pa termin ne može biti prebukiran (FR-06, FR-07, NF-04). Tim nije mjerio uštedu vremena ni povećanje broja rezervacija, pa takve koristi ne navodi brojčano.

## 1.2 Ciljani korisnici i dionici

**Cilj:** Utvrditi osobe, uloge i organizacije čije potrebe i interesi određuju projektni zadatak.

| Skupina korisnika ili dionika | Potreba ili interes relevantan za ovaj projekt |
| --- | --- |
| Neprijavljeni korisnik | Pregledati ponudu i pronaći izlet prema vlastitim kriterijima bez otvaranja korisničkog računa. |
| Turist (prijavljeni korisnik) | Rezervirati termin za željeni broj osoba uz dodatne usluge, znati ukupnu cijenu unaprijed, pregledati i otkazati vlastite rezervacije te tijekom izleta dobiti informacije o znamenitostima. |
| Administrator agencije | Objavljivati i mijenjati izlete, definirati i otkazivati termine te pregledavati i otkazivati rezervacije. |
| Organizator | Dodijeliti jednog ili više vodiča terminu izleta i pratiti koji su vodiči slobodni. |
| Vodič | Vidjeti samo termine kojima je dodijeljen, s nazivom izleta, vremenom i mjestom polaska. |
| Voditelj agencije | Pratiti broj rezervacija, popunjenost izleta, prihod i najtraženije kategorije i usluge za odabrano razdoblje, izvesti te podatke te dodjeljivati uloge zaposlenicima. |
| Turistička agencija (dionik) | Naručitelj čija ponuda i način poslovanja određuju opseg sustava; zastupljena je kroz uloge administratora, organizatora, vodiča i voditelja agencije. Ne koristi aplikaciju kao zaseban akter. |

Detaljan popis aktera, uključujući vanjske sustave (pružatelj identiteta, usluga e-pošte) i vremenski okidač, nalazi se u [odjeljku 2.3](./2-Analiza-zahtjeva.md#23-dionici-i-akteri).

## 1.3 Granice projekta

**Cilj:** Jasno definirati granice sustava: što je obuhvaćeno odobrenim zadatkom, što je svjesno izostavljeno ili odgođeno te o kojim vanjskim uslugama sustav izravno ovisi.

**Uključeno:** Pretraživanje i pregled izleta bez prijave; prijava putem vanjskog pružatelja identiteta (Microsoft, uz Google); rezervacija termina s dodatnim uslugama, izračunom cijene, provjerom slobodnih mjesta i potvrdom e-poštom; pregled i otkazivanje vlastitih rezervacija; upravljanje izletima, terminima i rezervacijama za administratora; raspoređivanje vodiča na termine i pregled dodijeljenih termina za vodiča; statistika poslovanja s izvozom u CSV za voditelja agencije; dodjela uloga zaposlenicima; automatsko zaključivanje rezervacija po završetku termina; digitalni vodič na mobilnom uređaju s kartom i obavijestima o obližnjim znamenitostima.

**Izvan opsega ili odgođeno:**

- **Plaćanje putem interneta.** Aplikacija izračunava i pamti cijenu rezervacije, ali se plaćanje obavlja izvan sustava. Integracija platnog pristupnika zahtijevala bi poseban sigurnosni i pravni okvir koji nije dio odobrenog zadatka.
- **Registracija korisničkim imenom i lozinkom.** Sustav ne pohranjuje lozinke; prijava je isključivo preko vanjskih pružatelja identiteta (C-02, ADR-02), pa registracija, promjena i oporavak lozinke nisu potrebni.
- **Rad bez internetske veze.** Digitalni vodič pretpostavlja aktivnu internetsku vezu tijekom izleta (AS-02). Predučitavanje podataka o znamenitostima za rad bez veze razmotrit će se samo ako se pretpostavka pokaže netočnom pri ispitivanju.

**Vanjske ovisnosti:**

- **Pružatelji identiteta (OAuth 2.0 / OpenID Connect):** Microsoft Entra ID i Google autentificiraju korisnike i dostavljaju ime i adresu e-pošte. Bez njih se korisnik ne može prijaviti ni rezervirati, iako katalog ostaje javno dostupan.
- **SMTP poslužitelj e-pošte (Gmail):** dostavlja potvrde rezervacija i obavijesti o otkazivanju. Neuspjeh slanja ne poništava rezervaciju (NF-10).
- **PostgreSQL baza podataka:** trajno pohranjuje korisnike, izlete, termine, lokacije, usluge i rezervacije.
- **Pružatelj kartografske podloge i Geolocation API preglednika:** potrebni za digitalnog vodiča (FR-16, FR-17). Položaj uređaja dostupan je samo preko HTTPS-a i uz dopuštenje korisnika (C-04).
- **Platforma za smještaj aplikacije (Render):** izvodi aplikaciju kao jednu instancu iza obrnutog posrednika (engl. reverse proxy) koji osigurava HTTPS.

## 1.4 Postojeći pristupi i opravdanost projekta

**Cilj:** Objasniti kako korisnici trenutačno rješavaju problem i koja preostala potreba opravdava odobreni projekt.

Turisti danas izlete često pronalaze i rezerviraju na posredničkim platformama poput GetYourGuidea i Viatora, na kojima agencija objavljuje ponudu i upravlja raspoloživošću termina, a platforma za svaku ostvarenu rezervaciju naplaćuje proviziju ([GetYourGuide Supply - FAQ](https://getyourguide.supply/components/sectionfaq), [How Viator Works](https://operatorresources.viator.com/about-us/)). Sučelje za pružatelje na takvoj platformi upravlja samo rezervacijama ostvarenima preko nje ([Ventrata - GetYourGuide Reseller Connection](https://support.ventrata.com/en/articles/9526754-getyourguide-reseller-connection)), a raspored vodiča, uloge zaposlenika i statistika cjelokupnog poslovanja ostaju izvan njega. Progi-Tours je zato zamišljen kao vlastiti sustav agencije koji na jednom mjestu objedinjuje ponudu, rezervacije, raspoređivanje vodiča i pregled poslovanja te turistu tijekom izleta nudi digitalnog vodiča. Aplikacija ne zamjenjuje posredničke platforme i ne povezuje se s njima.
