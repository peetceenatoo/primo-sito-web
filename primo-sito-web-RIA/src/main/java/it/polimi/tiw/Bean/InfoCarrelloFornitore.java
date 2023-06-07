package it.polimi.tiw.Bean;

import java.util.List;

public record InfoCarrelloFornitore(Integer idFornitore, String nome, List<InfoProdottoCarrello> prodotti) {
}