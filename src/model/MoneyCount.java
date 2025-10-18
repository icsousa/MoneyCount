package model;

import java.util.Collection;
import utils.Serializer;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import javax.xml.crypto.Data;

public class MoneyCount implements Serializable {
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
     * Construtor de cópia.
     * 
     * @param outro Objeto MoneyCount a ser copiado.
     */
    public MoneyCount(MoneyCount outro) {
        this.registos = new HashMap<>();

        // Copiar cada registro do outro objeto
        for (Map.Entry<YearMonth, Registo> entry : outro.registos.entrySet()) {
            this.registos.put(entry.getKey(), entry.getValue().clone());
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
