package model;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MoneyCount implements Serializable {
    
    private Map<YearMonth, Registo> registos;

    public MoneyCount(Collection<Registo> registos){
        this.registos = new HashMap<>();
        for (Registo r : registos) {
            this.registos.put(r.getData(), r.clone());
        }
    }

    public MoneyCount(MoneyCount outro) {
        this.registos = new HashMap<>();
        for (Map.Entry<YearMonth, Registo> entry : outro.registos.entrySet()) {
            this.registos.put(entry.getKey(), entry.getValue().clone());
        }
    }

    public MoneyCount(){
        this.registos = new HashMap<>();
    }

    public Map<YearMonth, Registo> getRegistos() {
        return this.registos;
    }

    public void adicionarRegisto(Registo r){
        this.registos.putIfAbsent(r.getData(), r.clone());
    }
}