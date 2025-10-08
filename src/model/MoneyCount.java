package model;

import java.util.Collection;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class MoneyCount {
    /**
     * Variavel de instância.
     */
    private Map<YearMonth, Registo> registos;

    /**
     * Construtor parametrizado.
     * 
     * @param registos Parâmetro recebido pelo construtor.
     */
    public MoneyCount(Collection<Registo> registos){
        this.registos = new HashMap<>();

        for (Registo r : registos) {
            this.registos.put(r.getData(), r.clone());
        }
    }

    /**
     * Construtor Vazio.
     */
    public MoneyCount(){
        this.registos = new HashMap<>();
    }

    /**
     * Método get para obter os registos.
     * 
     * @return Devolve os registos.
     */
    public Map<YearMonth, Registo> getRegistos() {
        return this.registos;
    }

    /**
     * Adiciona registo.
     * 
     * @param r Registo adicionado.
     */
    public void adicionarRegisto(Registo r){
        this.registos.putIfAbsent(r.getData(), r.clone());
    }
}
