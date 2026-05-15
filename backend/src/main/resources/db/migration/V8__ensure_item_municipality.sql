ALTER TABLE items ADD COLUMN municipality VARCHAR(120) NOT NULL DEFAULT 'Madrid';

UPDATE items SET city = 'Madrid', municipality = 'Madrid'
WHERE title = 'Taladro Bosch' OR city IN ('Madrid - Chamberi', 'Madrid - Retiro', 'Madrid - Moncloa');

UPDATE items SET city = 'Madrid', municipality = 'Madrid'
WHERE title LIKE 'C%mara GoPro';

UPDATE items SET city = 'Valencia/València', municipality = 'València'
WHERE title = 'Proyector Epson Full HD' OR city = 'Valencia - Ruzafa';

UPDATE items SET city = 'Barcelona', municipality = 'Barcelona'
WHERE title = 'Bicicleta urbana' OR city = 'Barcelona - Gracia';

UPDATE items SET city = 'Sevilla', municipality = 'Sevilla'
WHERE title = 'Altavoz JBL PartyBox' OR city = 'Sevilla - Triana';

UPDATE items SET city = 'Málaga', municipality = 'Málaga'
WHERE title LIKE 'Carrito de beb% plegable' OR city = 'Malaga - Centro';
