-- Manca l'asserzione che accerti che tutti i fornitori
-- abbiano un prezzo di spedizione definito per ogni
-- numero di prodotti, oppure spedizione sempre gratuita

CREATE TABLE `primo-sito-web`.`FORNITORE` (
`Id` int AUTO_INCREMENT NOT NULL,
`Nome` varchar(45) NOT NULL,
`Valutazione`DECIMAL(2,1) NOT NULL,
`SogliaSpedizioneGratuita` DECIMAL(10,2) DEFAULT NULL,
PRIMARY KEY (`Id`)
)