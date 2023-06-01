package it.polimi.tiw.Bean;

import java.util.Objects;

public record DettaglioOrdine(int idOrdine, int idProdotto, double prezzoProdotto, int quantita) {
	
	@Override
    public int hashCode() {
        return Objects.hash(idOrdine, idProdotto);
    }

    @Override
    public boolean equals(Object obj) {
        if( this == obj )
            return true;
        if( ( obj == null ) || !( obj instanceof DettaglioOrdine ) )
            return false;
        DettaglioOrdine other = (DettaglioOrdine) obj;
        return ( idOrdine == other.idOrdine ) && ( idProdotto == other.idProdotto );
    }
	
}