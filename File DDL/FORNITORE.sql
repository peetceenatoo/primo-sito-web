CREATE TABLE `primo-sito-web`.`FORNITORE` (
`Id` int AUTO_INCREMENT NOT NULL,
`Nome` varchar(45) NOT NULL,
`Valutazione` decimal(2,1) NOT NULL,
`SogliaSpedizioneGratuita` int DEFAULT NULL,
PRIMARY KEY (`Id`)
)