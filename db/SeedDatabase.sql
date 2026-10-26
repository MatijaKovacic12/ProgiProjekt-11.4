BEGIN;

INSERT INTO slika (id, putanja, alt_text) VALUES
    (1, '/images/zagreb-gornji-grad.jpg', 'Gornji grad u Zagrebu'),
    (2, '/images/plitvice-veliki-slap.jpg', 'Veliki slap na Plitvicama'),
    (3, '/images/sljeme.jpg', 'Pogled sa Sljemena'),
    (4, '/images/cetina-rafting.jpg', 'Rafting na Cetini'),
    (5, '/images/parenzana.jpg', 'Biciklistička staza Parenzana'),
    (6, '/images/elafiti.jpg', 'Elafitski otoci'),
    (7, '/images/avatars/ivana.jpg', 'Profilna slika');


INSERT INTO korisnik (id, ime, prezime, email, display_name, uloga, id_slika) VALUES
    (1,  'Marija',   'Kovač',     'marija.kovac@example.com',     'Marija Kovač',     'MANAGER',   NULL),
    (2,  'Ivan',     'Horvat',    'ivan.horvat@example.com',      'Ivan Horvat',      'ADMIN',     NULL),
    (3,  'Ana',      'Babić',     'ana.babic@example.com',        'Ana Babić',        'ORGANIZER', NULL),
    (4,  'Luka',     'Marić',     'luka.maric@example.com',       'Luka Marić',       'GUIDE',     NULL),
    (5,  'Petra',    'Novak',     'petra.novak@example.com',      'Petra Novak',      'GUIDE',     NULL),
    (6,  'Marko',    'Jurić',     'marko.juric@example.com',      'Marko Jurić',      'GUIDE',     NULL),
    (7,  'Ivana',    'Knežević',  'ivana.knezevic@example.com',   'Ivana Knežević',   'USER',      7),
    (8,  'Tomislav', 'Vuković',   'tomislav.vukovic@example.com', 'Tomislav Vuković', 'USER',      NULL),
    (9,  'Lucija',   'Pavić',     'lucija.pavic@example.com',     'Lucija Pavić',     'USER',      NULL),
    (10, 'Josip',    'Božić',     'josip.bozic@example.com',      'Josip Božić',      'USER',      NULL),
    (11, 'Ema',      'Grgić',     'ema.grgic@example.com',        'Ema Grgić',        'USER',      NULL);

INSERT INTO korisnik_vanjski (id, id_korisnik, provider, provider_user_id) VALUES
    (1, 1,  'microsoft', 'ms-sub-0001'),
    (2, 2,  'microsoft', 'ms-sub-0002'),
    (3, 3,  'microsoft', 'ms-sub-0003'),
    (4, 4,  'microsoft', 'ms-sub-0004'),
    (5, 7,  'microsoft', 'ms-sub-0007'),
    (6, 7,  'google',    'google-sub-0007'),
    (7, 8,  'google',    'google-sub-0008'),
    (8, 9,  'microsoft', 'ms-sub-0009'),
    (9, 10, 'microsoft', 'ms-sub-0010'),
    (10, 11, 'google',   'google-sub-0011');


INSERT INTO kategorija (id, ime) VALUES
    (1, 'Obilazak grada'),
    (2, 'Planinarenje'),
    (3, 'Biciklističke ture'),
    (4, 'Rafting'),
    (5, 'Izleti brodom');

INSERT INTO usluga (id, ime, opis, cijena) VALUES
    (1, 'Prijevoz autobusom', 'Organizirani prijevoz od mjesta polaska i natrag', 15.00),
    (2, 'Najam opreme',       'Kaciga, prsluk i ostala potrebna oprema',          10.00),
    (3, 'Ručak',              'Ručak u lokalnom restoranu',                       18.00),
    (4, 'Putno osiguranje',   'Osiguranje za vrijeme trajanja izleta',             5.00),
    (5, 'Fotografiranje',     'Profesionalni fotograf i digitalne fotografije',   20.00),
    (6, 'Najam bicikla',      'Treking bicikl za cijeli dan',                     25.00);

INSERT INTO izlet (id, naziv, opis, id_kategorija, trajanje_minuta, cijena, max_sudionika, broj_vodica) VALUES
    (1, 'Šetnja Gornjim gradom',
        'Vođena šetnja kroz povijesnu jezgru Zagreba uz priče o legendama i znamenitostima.',
        1, 180, 25.00, 20, 1),
    (2, 'Nacionalni park Plitvička jezera',
        'Cjelodnevni izlet na Plitvice s obilaskom Donjih i Gornjih jezera.',
        2, 600, 65.00, 30, 2),
    (3, 'Uspon na Sljeme',
        'Planinarski uspon na Medvednicu sa spustom pokraj Medvedgrada.',
        2, 300, 20.00, 15, 1),
    (4, 'Rafting na Cetini',
        'Spust kanjonom rijeke Cetine uz iskusne skipere, prikladno i za početnike.',
        4, 240, 45.00, 12, 2),
    (5, 'Parenzana biciklom',
        'Vožnja trasom stare uskotračne pruge kroz istarske gradiće na brežuljcima.',
        3, 360, 40.00, 15, 1),
    (6, 'Elafitski otoci brodom',
        'Plovidba do Koločepa, Lopuda i Šipana s kupanjem i ručkom na brodu.',
        5, 480, 55.00, 40, 2);

INSERT INTO izlet_usluga (id_izlet, id_usluga) VALUES
    (1, 3), (1, 5),
    (2, 1), (2, 3), (2, 4),
    (3, 1), (3, 2),
    (4, 1), (4, 2), (4, 4),
    (5, 6), (5, 3), (5, 4),
    (6, 3), (6, 5);

INSERT INTO lokacija (id, naziv, opis, znacaj, latitude, longitude, tip, id_slika, id_izlet) VALUES
    (1,  'Gornji grad, Zagreb', 'Povijesna jezgra Zagreba', NULL, 45.815000, 15.973000, 'MAIN', 1, 1),
    (2,  'Trg bana Jelačića', 'Glavni gradski trg', NULL, 45.813100, 15.977200, 'DEPARTURE', NULL, 1),
    (3,  'Zagrebačka katedrala', 'Neogotička katedrala s dva tornja', 'Najviša građevina u Hrvatskoj i simbol grada', 45.814400, 15.979800, 'LANDMARK', NULL, 1),
    (4,  'Crkva sv. Marka', 'Crkva s krovom od šarenih crijepova', 'Na krovu su grbovi Zagreba i Kraljevine Hrvatske, Dalmacije i Slavonije', 45.816300, 15.973700, 'LANDMARK', NULL, 1),
    (5,  'Kula Lotrščak', 'Srednjovjekovna obrambena kula', 'Iz nje svakog dana u podne puca top Grič', 45.814300, 15.973200, 'LANDMARK', NULL, 1),
    (6,  'Kamenita vrata', 'Jedina sačuvana gradska vrata', 'Svetište sa slikom Majke Božje koja je preživjela požar 1731.', 45.815500, 15.976200, 'LANDMARK', NULL, 1),

    (7,  'NP Plitvička jezera', 'Najstariji i najveći nacionalni park u Hrvatskoj', NULL, 44.865400, 15.582000, 'MAIN', 2, 2),
    (8,  'Autobusni kolodvor Zagreb', 'Polazak autobusa za Plitvice', NULL, 45.804500, 15.993000, 'DEPARTURE', NULL, 2),
    (9,  'Veliki slap', 'Najviši slap u Hrvatskoj', 'Visok 78 metara, nastao padom rijeke Plitvice', 44.902000, 15.610000, 'LANDMARK', NULL, 2),
    (10, 'Jezero Kozjak', 'Najveće plitvičko jezero', 'Povezuje Gornja i Donja jezera, preko njega vozi električni brod', 44.889000, 15.604000, 'LANDMARK', NULL, 2),
    (11, 'Galovački buk', 'Slapovi na Gornjim jezerima', 'Jedan od najljepših slapova Gornjih jezera', 44.870000, 15.606000, 'LANDMARK', NULL, 2),

    (12, 'Medvednica', 'Planina iznad Zagreba', NULL, 45.900000, 15.970000, 'MAIN', 3, 3),
    (13, 'Bliznec', 'Početak planinarske staze', NULL, 45.858000, 15.973000, 'DEPARTURE', NULL, 3),
    (14, 'Sljemenski vrh', 'Najviši vrh Medvednice', 'Na 1033 m nadmorske visine, s televizijskim tornjem', 45.900000, 15.948000, 'LANDMARK', NULL, 3),
    (15, 'Medvedgrad', 'Srednjovjekovna utvrda', 'Sagrađena u 13. stoljeću nakon tatarske provale', 45.869700, 15.940800, 'LANDMARK', NULL, 3),
    (16, 'Zrinski rudnik', 'Stari rudnik srebra i olova', 'Dio povijesti obitelji Zrinski na Medvednici', 45.896000, 15.955000, 'LANDMARK', NULL, 3),

    (17, 'Rijeka Cetina', 'Kanjon rijeke Cetine', NULL, 43.444000, 16.695000, 'MAIN', 4, 4),
    (18, 'Omiš', 'Okupljanje kod ušća Cetine', NULL, 43.444700, 16.688600, 'DEPARTURE', NULL, 4),
    (19, 'Radmanove mlinice', 'Stari mlinovi na Cetini', 'Mlinice iz 18. stoljeća, danas izletište i restoran', 43.431000, 16.730000, 'LANDMARK', NULL, 4),
    (20, 'Kanjon Cetine', 'Strme stijene uz rijeku', 'Kroz kanjon su prolazili omiški gusari', 43.440000, 16.710000, 'LANDMARK', NULL, 4),

    (21, 'Parenzana', 'Trasa stare uskotračne pruge', NULL, 45.350000, 13.750000, 'MAIN', 5, 5),
    (22, 'Motovun', 'Okupljanje ispod gradskih zidina', NULL, 45.336600, 13.828600, 'DEPARTURE', NULL, 5),
    (23, 'Grožnjan', 'Grad umjetnika', 'Srednjovjekovni gradić poznat po galerijama i glazbenoj školi', 45.379700, 13.723200, 'LANDMARK', NULL, 5),
    (24, 'Vižinada', 'Gradić na brežuljku', 'Nekadašnja postaja pruge Parenzana', 45.333300, 13.761400, 'LANDMARK', NULL, 5),

    (25, 'Elafitski otoci', 'Otočje zapadno od Dubrovnika', NULL, 42.740000, 17.950000, 'MAIN', 6, 6),
    (26, 'Luka Gruž', 'Ukrcaj na brod', NULL, 42.659000, 18.085000, 'DEPARTURE', NULL, 6),
    (27, 'Koločep', 'Najmanji naseljeni Elafit', 'Poznat po špiljama i borovim šumama', 42.675000, 18.010000, 'LANDMARK', NULL, 6),
    (28, 'Lopud', 'Otok bez automobila', 'Pješčana plaža Šunj, jedna od najljepših na jugu', 42.683000, 17.940000, 'LANDMARK', NULL, 6),
    (29, 'Šipan', 'Najveći Elafitski otok', 'Ljetnikovci dubrovačke vlastele iz 15. i 16. stoljeća', 42.728000, 17.870000, 'LANDMARK', NULL, 6);


INSERT INTO izlet_instanca (id, id_izlet, termin_pocetak, termin_kraj)
SELECT v.id, v.id_izlet, v.pocetak, v.pocetak + make_interval(mins => i.trajanje_minuta)
FROM (VALUES
    (1,  1, date_trunc('day', now()) - interval '7 days'  + interval '10 hours'),
    (2,  1, date_trunc('day', now()) + interval '2 days'  + interval '10 hours'),
    (3,  1, date_trunc('day', now()) + interval '9 days'  + interval '10 hours'),
    (4,  2, date_trunc('day', now()) - interval '14 days' + interval '7 hours'),
    (5,  2, date_trunc('day', now()) + interval '5 days'  + interval '7 hours'),
    (6,  3, date_trunc('minute', now()) - interval '1 hour'),
    (7,  3, date_trunc('day', now()) + interval '3 days'  + interval '8 hours'),
    (8,  4, date_trunc('day', now()) - interval '3 days'  + interval '9 hours'),
    (9,  4, date_trunc('day', now()) + interval '12 days' + interval '9 hours'),
    (10, 5, date_trunc('day', now()) + interval '6 days'  + interval '9 hours'),
    (11, 6, date_trunc('day', now()) - interval '20 days' + interval '8 hours'),
    (12, 6, date_trunc('day', now()) + interval '15 days' + interval '8 hours')
) AS v(id, id_izlet, pocetak)
JOIN izlet i ON i.id = v.id_izlet;


INSERT INTO izlet_instanca_vodic (id_instanca, id_vodic) VALUES
    (1, 4),
    (2, 4),
    (3, 5),
    (4, 5), (4, 6),
    (5, 4), (5, 5),
    (6, 6),
    (7, 6),
    (8, 4), (8, 5),
    (9, 4), (9, 6),
    (10, 5),
    (11, 4), (11, 6);

INSERT INTO rezervacija (id, id_korisnik, id_instanca, broj_osoba, status, ukupna_cijena) VALUES
    (1,  7,  1,  2, 'COMPLETED', 0),
    (2,  8,  1,  4, 'COMPLETED', 0),
    (3,  9,  1,  1, 'CANCELLED', 0),
    (4,  7,  2,  3, 'CONFIRMED', 0),
    (5,  10, 2,  2, 'CONFIRMED', 0),
    (6,  11, 3,  5, 'CONFIRMED', 0),
    (7,  8,  4,  6, 'COMPLETED', 0),
    (8,  9,  4,  3, 'COMPLETED', 0),
    (9,  7,  5,  4, 'CONFIRMED', 0),
    (10, 10, 5,  2, 'CANCELLED', 0),
    (11, 7,  6,  2, 'CONFIRMED', 0),
    (12, 11, 6,  3, 'CONFIRMED', 0),
    (13, 8,  7,  2, 'CONFIRMED', 0),
    (14, 9,  8,  4, 'COMPLETED', 0),
    (15, 10, 8,  6, 'COMPLETED', 0),
    (16, 11, 9,  10, 'CONFIRMED', 0),
    (17, 7,  10, 2, 'CONFIRMED', 0),
    (18, 8,  11, 8, 'COMPLETED', 0),
    (19, 9,  11, 2, 'CANCELLED', 0),
    (20, 10, 12, 4, 'CONFIRMED', 0);

INSERT INTO rezervacija_usluga (id_rezervacija, id_usluga, kolicina, cijena)
SELECT v.id_rezervacija, v.id_usluga, v.kolicina, u.cijena
FROM (VALUES
    (1, 3, 2),
    (4, 5, 1),
    (5, 3, 2),
    (7, 1, 6), (7, 3, 6),
    (8, 4, 3),
    (9, 1, 4), (9, 3, 4), (9, 4, 4),
    (10, 1, 2),
    (11, 2, 2),
    (12, 1, 3),
    (14, 2, 4), (14, 4, 4),
    (15, 1, 6),
    (16, 1, 10), (16, 2, 10),
    (17, 6, 2), (17, 3, 2),
    (18, 3, 8),
    (20, 5, 1), (20, 3, 4)
) AS v(id_rezervacija, id_usluga, kolicina)
JOIN usluga u ON u.id = v.id_usluga;

UPDATE rezervacija r
SET ukupna_cijena = r.broj_osoba * i.cijena
                  + COALESCE((SELECT SUM(ru.kolicina * ru.cijena)
                              FROM rezervacija_usluga ru
                              WHERE ru.id_rezervacija = r.id), 0),
    created_at = LEAST(now(), ii.termin_pocetak - interval '3 days'),
    updated_at = LEAST(now(), ii.termin_pocetak - interval '3 days')
FROM izlet_instanca ii
JOIN izlet i ON i.id = ii.id_izlet
WHERE ii.id = r.id_instanca;


SELECT setval(pg_get_serial_sequence('slika', 'id'),            (SELECT MAX(id) FROM slika));
SELECT setval(pg_get_serial_sequence('korisnik', 'id'),         (SELECT MAX(id) FROM korisnik));
SELECT setval(pg_get_serial_sequence('korisnik_vanjski', 'id'), (SELECT MAX(id) FROM korisnik_vanjski));
SELECT setval(pg_get_serial_sequence('kategorija', 'id'),       (SELECT MAX(id) FROM kategorija));
SELECT setval(pg_get_serial_sequence('usluga', 'id'),           (SELECT MAX(id) FROM usluga));
SELECT setval(pg_get_serial_sequence('izlet', 'id'),            (SELECT MAX(id) FROM izlet));
SELECT setval(pg_get_serial_sequence('lokacija', 'id'),         (SELECT MAX(id) FROM lokacija));
SELECT setval(pg_get_serial_sequence('izlet_instanca', 'id'),   (SELECT MAX(id) FROM izlet_instanca));
SELECT setval(pg_get_serial_sequence('rezervacija', 'id'),      (SELECT MAX(id) FROM rezervacija));

COMMIT;
