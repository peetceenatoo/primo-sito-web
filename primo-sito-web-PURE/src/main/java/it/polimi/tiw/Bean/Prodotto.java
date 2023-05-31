package it.polimi.tiw.Bean;

import java.util.Objects;

public record Prodotto(int id, String nome, String descrizione, String foto, String categoria) {
	
	@Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if( this == obj )
            return true;
        if( ( obj == null ) || !( obj instanceof Prodotto ) )
            return false;
        Prodotto other = (Prodotto) obj;
        return id == other.id;
    }
	
}