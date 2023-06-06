package it.polimi.tiw.Bean;

import java.util.Date;
import java.util.List;

public record Ordine(int id, double totaleOrdine, double speseSpedizione, Date dataSpedizione, String indirizzo, String nomeFornitore, String email, List<Coppia<DettaglioOrdine,String>> dettagli) {
	
}