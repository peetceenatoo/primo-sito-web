package it.polimi.tiw.Bean;

import java.util.List;
import java.util.Objects;

public record Fornitore(int id, String nome, double valutazione, Double soglia, List<FasciaDiSpedizione> fasceDiSpedizione) {
	
	@Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if( this == obj )
            return true;
        if( ( obj == null ) || !( obj instanceof Fornitore ) )
            return false;
        Fornitore other = (Fornitore) obj;
        return id == other.id;
    }
	
}