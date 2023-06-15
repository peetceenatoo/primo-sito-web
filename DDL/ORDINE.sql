CREATE TABLE `primo-sito-web`.`ORDINE` (
`Id` int AUTO_INCREMENT NOT NULL,
`TotaleOrdine` int NOT NULL,
`SpeseSpedizione` int NOT NULL,
`DataSpedizione` date,
`Indirizzo` varchar(200) NOT NULL,
`NomeFornitore` varchar(45) NOT NULL,
`Email` varchar(45) NOT NULL,
PRIMARY KEY (`Id`),

CONSTRAINT FOREIGN KEY (`Email`) 
REFERENCES `UTENTE` (`Email`)
ON DELETE RESTRICT ON UPDATE CASCADE
)