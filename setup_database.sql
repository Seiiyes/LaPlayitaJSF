-- Script para inicializar/verificar la tabla 'rol' en la base de datos 'laplayita'.
-- Este script es seguro de ejecutar múltiples veces.

-- 1. Asegurarse de que la tabla 'rol' existe con la estructura correcta.
CREATE TABLE IF NOT EXISTS `rol` (
  `idRol` INT NOT NULL AUTO_INCREMENT,
  `descripcionRol` VARCHAR(35) NOT NULL,
  PRIMARY KEY (`idRol`),
  UNIQUE INDEX `descripcionRol_UNIQUE` (`descripcionRol` ASC)
) ENGINE=InnoDB;

-- 2. Insertar los roles estándar si no existen.
-- Usamos ON DUPLICATE KEY UPDATE para insertar si no existe, o actualizar si el ID ya existe.
-- Esto garantiza que la descripción del rol sea siempre la correcta.
INSERT INTO `rol` (`idRol`, `descripcionRol`) VALUES
(1, 'ADMIN'),
(2, 'VENDEDOR'),
(3, 'CLIENTE')
ON DUPLICATE KEY UPDATE `descripcionRol` = VALUES(`descripcionRol`);

-- 3. (Opcional, buena práctica) Reajustar el contador AUTO_INCREMENT.
-- Esto evita posibles colisiones si se insertaron IDs manualmente.
SET @max_id = (SELECT MAX(idRol) FROM `rol`);
SET @sql = CONCAT('ALTER TABLE `rol` AUTO_INCREMENT = ', IFNULL(@max_id, 0) + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. Verificar el contenido de la tabla.
SELECT * FROM `rol`;