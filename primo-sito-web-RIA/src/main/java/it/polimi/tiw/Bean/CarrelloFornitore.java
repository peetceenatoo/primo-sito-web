package it.polimi.tiw.Bean;

import java.util.List;

public record CarrelloFornitore(Integer idFornitore, List<ProdottoCarrello> prodotti) {
}