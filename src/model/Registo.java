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
    private Map<Integer, Entrada> entradas;
    
    public Registo(YearMonth data, double rendimento) {
        this.data = data;
        this.rendimento = rendimento;
        this.saldo = this.rendimento;
        this.poupanca = this.rendimento;
        this.despesas = new HashMap<>();
        this.entradas = new HashMap<>();
    }

    public Registo(Registo outro) {
        this.data = outro.data;
        this.rendimento = outro.rendimento;
        this.saldo = outro.saldo;
        this.poupanca = outro.poupanca;
        this.despesas = new HashMap<>();
        this.entradas = new HashMap<>();

        for (Map.Entry<Integer, Despesa> entry : outro.despesas.entrySet()) {
            this.despesas.put(entry.getKey(), entry.getValue().clone());
        }

        for (Map.Entry<Integer, Entrada> entry : outro.entradas.entrySet()) {
            this.entradas.put(entry.getKey(), entry.getValue().clone());
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

    public Map<Integer, Entrada> getEntradas() {
        return this.entradas;
    }

    // Devolve estritamente o valor do salário/base, sem somar as entradas
    public double getRendimento() {
        return this.rendimento; 
    }

    // NOVO: Soma das entradas para ser calculada no Saldo Disponível
    public double getTotalEntradas() {
        double total = 0;
        for (Entrada e : entradas.values()) {
            total += e.getMontante();
        }
        return total;
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

    public void adicionarEntrada(Entrada e) {
        Entrada c = e.clone();
        this.entradas.put(c.getIdEntrada(), c);
    }

    public void setRendimento(double rendimento) {
        this.rendimento = rendimento;
    } 
}