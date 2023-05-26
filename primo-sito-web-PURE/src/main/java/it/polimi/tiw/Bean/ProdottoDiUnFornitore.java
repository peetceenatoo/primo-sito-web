package it.polimi.tiw.Bean;

import java.util.Objects;

public record ProdottoDiUnFornitore(int idProdotto, int idFornitore, double prezzoScontato, double sconto) {
	
	@Override
    public int hashCode() {
        return Objects.hash(idProdotto, idFornitore);
    }

    @Override
    public boolean equals(Object obj) {
        if( this == obj )
            return true;
        if( ( obj == null ) || !( obj instanceof ProdottoDiUnFornitore ) )
            return false;
        ProdottoDiUnFornitore other = (ProdottoDiUnFornitore) obj;
        return ( idProdotto == other.idProdotto ) && ( idFornitore == other.idFornitore );
    }
	
}