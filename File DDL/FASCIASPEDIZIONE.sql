-- Manca l'asserzione che le fasce di spedizione non
-- si sovrappongano

CREATE TABLE `primo-sito-web`.`FASCIASPEDIZIONE` (
`Id` int AUTO_INCREMENT NOT NULL,
`IdFornitore` int NOT NULL,
`Min` int NOT NULL,
`Max` int DEFAULT NULL,
`Prezzo` DECIMAL(10,2) NOT NULL,
PRIMARY KEY (`Id`),

CONSTRAINT FOREIGN KEY (`IdFornitore`)
REFERENCES `FORNITORE` (`Id`)
ON DELETE RESTRICT
ON UPDATE CASCADE
)