CREATE TABLE `primo-sito-web`.`PRODOTTO` (
`Id` int AUTO_INCREMENT NOT NULL,
`Nome` varchar(45) NOT NULL,
`Descrizione` text NOT NULL,
`Foto` varchar(255) NOT NULL,
`Categoria` varchar(45) NOT NULL,
PRIMARY KEY (`Id`),

CONSTRAINT FOREIGN KEY (`Categoria`)
REFERENCES `CATEGORIA` (`Categoria`)
ON DELETE RESTRICT
ON UPDATE CASCADE
)