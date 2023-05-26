package it.polimi.tiw.Bean;

import java.util.List;

public record Fornitore(int id, String nome, double valutazione, double soglia, List<FasciaDiSpedizione> fasceDiSpedizione) {
	
}