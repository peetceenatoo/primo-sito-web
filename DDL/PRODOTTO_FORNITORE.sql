-- Manca l'assertion che lo sconto sia
-- compreso tra 0 e 1

CREATE TABLE `primo-sito-web`.`PRODOTTO_FORNITORE` (
`IdProdotto` int NOT NULL,
`IdFornitore` int NOT NULL,
`Prezzo` DECIMAL(10,2) NOT NULL,
`Sconto` DECIMAL(3,2) DEFAULT 0 NOT NULL,

PRIMARY KEY (`IdProdotto`,`IdFornitore`),

CONSTRAINT FOREIGN KEY (`IdProdotto`) 
REFERENCES `PRODOTTO` (`Id`) 
ON DELETE RESTRICT ON UPDATE CASCADE,

CONSTRAINT FOREIGN KEY (`IdFornitore`)
REFERENCES `FORNITORE` (`Id`)
ON DELETE RESTRICT ON UPDATE CASCADE
)