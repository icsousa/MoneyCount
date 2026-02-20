package model;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class Registo implements Serializable {
    
    private YearMonth data;
    private double rendimento;
    private double saldo;
    private double poupanca;
    private Map<Integer, Despesa> despesas;
    
    public Registo(YearMonth data, double rendimento) {
        this.data = data;
        this.rendimento = rendimento;
        this.saldo = this.rendimento;
        this.poupanca = this.rendimento;
        this.despesas = new HashMap<>();
    }

    /**
     * Construtor cópia.
     */
    public Registo(Registo outro) {
        this.data = outro.data;
        this.rendimento = outro.rendimento;
        this.saldo = outro.saldo;
        this.poupanca = outro.poupanca;
        this.despesas = new HashMap<>();

        // Usamos o .clone() da despesa para garantir que se for DespesaFixa, 
        // continua a ser uma DespesaFixa.
        for (Map.Entry<Integer, Despesa> entry : outro.despesas.entrySet()) {
            this.despesas.put(entry.getKey(), entry.getValue().clone());
        }
    }

    @Override
    public Registo clone() {
        return new Registo(this);
    }

    public YearMonth getData() {
        return this.data;
    }

    public Map<Integer,Despesa> getDespesas() {
        return this.despesas;
    }

    public double getRendimento() {
        return this.rendimento;
    }

    public double getTotalDespesas() {
        double total = 0;
        for (Despesa d : despesas.values()) {
            if (d instanceof DespesaFixa df) {
                if (df.isPago()) {
                    total += df.getMontante();
                }
            } else {
                total += d.getMontante();
            }
        }
        return total;
    }

    public void adicionarDespesa(Despesa d) {
        Despesa c = d.clone();
        this.despesas.put(c.getIdDespesa(), c);
    }

    public void setRendimento(double rendimento) {
        this.rendimento = rendimento;
    } 
}