ALTER TABLE izlet          ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE izlet_instanca ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE izlet_instanca DROP CONSTRAINT ux_termin_izlet_pocetak;
CREATE UNIQUE INDEX ux_termin_izlet_pocetak ON izlet_instanca (id_izlet, termin_pocetak) WHERE is_active;

CREATE INDEX ix_izlet_aktivni  ON izlet (id_kategorija) WHERE is_active;
CREATE INDEX ix_termin_aktivni ON izlet_instanca (id_izlet, termin_pocetak) WHERE is_active;