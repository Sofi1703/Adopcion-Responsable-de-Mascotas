-- tres mascotas de ejemplo para probar la API (ejecutar despues de schema.sql)

INSERT INTO mascota (nombre, especie, raza, descripcion, estado, imagen_url) VALUES
('Luna', 'Perro', 'Criolla', 'Juguetona y sociable', 'DISPONIBLE', 'https://ejemplo.com/luna.jpg'),
('Michi', 'Gato', NULL, 'Tranquilo, ideal para apartamento', 'DISPONIBLE', NULL),
('Rocky', 'Perro', 'Labrador', 'Adulto y obediente', 'ADOPTADA', NULL);
