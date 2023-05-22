CREATE TABLE `primo-sito-web`.`DETTAGLIO_ORDINE` (
`IdOrdine` int NOT NULL,
`IdProdotto` int NOT NULL,
`IdFornitore` int NOT NULL,
`Quantita` int NOT NULL,
`Prezzo` decimal(10,2) NOT NULL,
PRIMARY KEY (`IdOrdine`,`IdProdotto`,`IdFornitore`),

CONSTRAINT FOREIGN KEY (`IdOrdine`)
REFERENCES `ORDINE` (`Id`)
ON DELETE RESTRICT ON UPDATE CASCADE,

CONSTRAINT FOREIGN KEY (`IdProdotto`, `IdFornitore`)
REFERENCES `PRODOTTO_FORNITORE` (`IdProdotto`, `IdFornitore`) 
ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Cambio delimiter perchè ho bisogno
-- di utilizzare ";" internamente
DELIMITER //
CREATE TRIGGER `Unico_IdFornitore`

-- Per ogni riga che sto cercando di inserire,
-- prima di inserirla...
BEFORE INSERT ON `DETTAGLIO_ORDINE`
FOR EACH ROW

BEGIN
  -- Seleziono l'IdFornitore attualmente associato
  -- a tutti i dettagli dell'ordine a cui appartiene
  -- il dettaglio che sto aggiungendo, e lo metto in
  -- IDFornitore_Temp
  DECLARE `IdFornitore_Temp` int;
  SELECT DISTINCT `IdFornitore` INTO `IdFornitore_Temp`
  FROM `ORDINE`
  WHERE `Id` = NEW.`IdOrdine`;
  
  -- Se l'IdFornitore della riga che sto aggiungendo
  -- è diverso dall'IdFornitore che compare nelle righe
  -- relative all'ordine di interesse: eccezione
  IF NEW.`IdFornitore` != `IdFornitore_Temp` THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'IdFornitore deve essere comune a tutte le righe associate allo stesso IdOrdine';
  END IF;
END //
DELIMITER ;