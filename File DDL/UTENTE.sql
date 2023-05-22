CREATE TABLE `primo-sito-web`.`UTENTE` (
`Nome` varchar(45) NOT NULL,
`Cognome` varchar(45) NOT NULL,
`Email` varchar(45) NOT NULL,
`Password` varchar(45) NOT NULL,
`Via` varchar(45) NOT NULL,
`Civico` varchar(45) NOT NULL,
`CAP` varchar(45) NOT NULL,
`Citta` varchar(45) NOT NULL,
`Stato` varchar(45) NOT NULL,
`Provincia` varchar(45) NOT NULL,
PRIMARY KEY (`Email`)
)