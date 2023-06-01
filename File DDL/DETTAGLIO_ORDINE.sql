CREATE TABLE `dettaglio_ordine` (
	`IdOrdine` int NOT NULL,
	`IdProdotto` int NOT NULL,
	`PrezzoProdotto` decimal(10,2) NOT NULL,
	`Quantita` int NOT NULL,
	
	PRIMARY KEY (`IdOrdine`,`IdProdotto`),

  	CONSTRAINT FOREIGN KEY (`IdOrdine`)
	REFERENCES `ORDINE` (`Id`)
	ON DELETE RESTRICT
	ON UPDATE CASCADE,
	
  	CONSTRAINT FOREIGN KEY (`IdProdotto`)
	REFERENCES `PRODOTTO` (`Id`)
	ON DELETE RESTRICT
	ON UPDATE CASCADE
)