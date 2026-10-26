-- Datos de prueba (idempotente: se puede ejecutar en cada arranque)

INSERT INTO tipo_articulo (id_tipo, descripcion) VALUES
 (1, 'Analgesico'),
 (2, 'Antibiotico'),
 (3, 'Antihipertensivo'),
 (4, 'Antidiabetico'),
 (5, 'Vitaminas y suplementos'),
 (6, 'Material medico'),
 (7, 'Antiinflamatorio'),
 (8, 'Antiparasitario')
ON CONFLICT (id_tipo) DO NOTHING;

INSERT INTO unidad_medida (id_unidad, descripcion) VALUES
 (1, 'Tableta'),
 (2, 'Capsula'),
 (3, 'Frasco'),
 (4, 'Ampolla'),
 (5, 'Caja'),
 (6, 'Sobre'),
 (7, 'Tubo'),
 (8, 'Vial')
ON CONFLICT (id_unidad) DO NOTHING;

INSERT INTO proveedor (id_proveedor, ruc, nombre) VALUES
 (1,  '20100012345', 'Distribuidora Farmaceutica del Norte SAC'),
 (2,  '20200023456', 'Laboratorios Andinos SA'),
 (3,  '20300034567', 'Medifarma Piura EIRL'),
 (4,  '20400045678', 'Drogueria Salud Total SAC'),
 (5,  '20500056789', 'Insumos Medicos del Peru SA'),
 (6,  '20600067890', 'Farmacorp Sullana SAC'),
 (7,  '20700078901', 'BioQuimica Grau SRL'),
 (8,  '20800089012', 'Distribuidora Vida Sana SAC'),
 (9,  '20900090123', 'Laboratorio Pacifico SA'),
 (10, '20110001234', 'Suministros Hospitalarios Tallan SAC')
ON CONFLICT (id_proveedor) DO NOTHING;

INSERT INTO articulo (id_articulo, descripcion, precio, unidad_medida, id_proveedor, id_tipo)
SELECT semillas.*, 9 FROM (VALUES
 (1,  'Paracetamol 500 mg',                     0.10, 1, 1),
 (2,  'Paracetamol 500 mg',                     0.12, 1, 2),
 (3,  'Paracetamol 500 mg',                     0.09, 1, 3),
 (4,  'Paracetamol jarabe 120 mg/5 ml',         3.50, 3, 4),
 (5,  'Ibuprofeno 400 mg',                      0.15, 1, 1),
 (6,  'Ibuprofeno 400 mg',                      0.18, 1, 6),
 (7,  'Amoxicilina 500 mg',                     0.35, 2, 2),
 (8,  'Amoxicilina 500 mg',                     0.30, 2, 3),
 (9,  'Amoxicilina 500 mg',                     0.40, 2, 9),
 (10, 'Amoxicilina suspension 250 mg/5 ml',     6.80, 3, 2),
 (11, 'Azitromicina 500 mg',                    1.20, 1, 7),
 (12, 'Azitromicina 500 mg',                    1.05, 1, 9),
 (13, 'Ciprofloxacino 500 mg',                  0.45, 1, 3),
 (14, 'Ceftriaxona 1 g',                        4.50, 8, 5),
 (15, 'Ceftriaxona 1 g',                        4.20, 8, 7),
 (16, 'Enalapril 10 mg',                        0.08, 1, 1),
 (17, 'Enalapril 10 mg',                        0.07, 1, 8),
 (18, 'Losartan 50 mg',                         0.20, 1, 4),
 (19, 'Losartan 50 mg',                         0.16, 1, 9),
 (20, 'Amlodipino 5 mg',                        0.12, 1, 6),
 (21, 'Metformina 850 mg',                      0.10, 1, 2),
 (22, 'Metformina 850 mg',                      0.11, 1, 8),
 (23, 'Glibenclamida 5 mg',                     0.06, 1, 3),
 (24, 'Insulina NPH 100 UI/ml',                38.00, 8, 5),
 (25, 'Insulina NPH 100 UI/ml',                35.50, 8, 7),
 (26, 'Sulfato ferroso 300 mg',                 0.05, 1, 8),
 (27, 'Acido folico 0.5 mg',                    0.04, 1, 8),
 (28, 'Multivitaminico pediatrico',             8.90, 3, 4),
 (29, 'Sales de rehidratacion oral',            0.80, 6, 1),
 (30, 'Sales de rehidratacion oral',            0.75, 6, 10),
 (31, 'Diclofenaco 75 mg/3 ml',                 0.90, 4, 6),
 (32, 'Naproxeno 550 mg',                       0.25, 1, 7),
 (33, 'Dexametasona 4 mg/ml',                   1.10, 4, 5),
 (34, 'Albendazol 400 mg',                      0.50, 1, 3),
 (35, 'Albendazol suspension 100 mg/5 ml',      2.40, 3, 4),
 (36, 'Mebendazol 100 mg',                      0.20, 1, 9),
 (37, 'Jeringa descartable 5 ml',               0.30, 5, 10),
 (38, 'Guantes de examen talla M (caja x100)', 18.50, 5, 10),
 (39, 'Gasa esteril 10x10 cm',                  0.60, 6, 5),
 (40, 'Alcohol medicinal 70% 1 L',              9.80, 3, 10),
 (41, 'Cloruro de sodio 0.9% 1 L',              4.10, 3, 5),
 (42, 'Omeprazol 20 mg',                        0.09, 2, 1),
 (43, 'Omeprazol 20 mg',                        0.11, 2, 6),
 (44, 'Salbutamol inhalador 100 mcg',          12.50, 3, 2),
 (45, 'Clotrimazol crema 1%',                   3.20, 7, 4),
 (46, 'Loratadina 10 mg',                       0.07, 1, 8),
 (47, 'Vacuna antitetanica',                   22.00, 8, 7),
 (48, 'Oxitocina 10 UI/ml',                     2.30, 4, 5),
 (49, 'Enoxaparina 40 mg',                     28.40, 4, 9),
 (50, 'Factor VIII antihemofilico 500 UI',    950.00, 8, 9)
) AS semillas(id_articulo, descripcion, precio, unidad_medida, id_proveedor)
ON CONFLICT (id_articulo) DO NOTHING;
