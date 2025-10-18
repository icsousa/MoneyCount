package model;

import utils.Serializer;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class Registo implements Serializable {
    
    /**
     * Variáveis de instância.
     */
    private YearMonth data;
    private double rendimento;
    private double saldo;
    private double poupanca;
    private Map<Integer, Despesa> despesas;
    
    /**
     * Construtor parametrizado.
     * 
     * @param rendimento Parâmetro do construtor.
     */
    public Registo(YearMonth data,double rendimento) {
        this.data = data;
        this.rendimento = rendimento;
        this.saldo = this.rendimento;
        this.poupanca = this.rendimento;
        this.despesas = new HashMap<>();
    }

    /**
     * Construtor cópia.
     * 
     * @param outro Resgito a copiar.
     */
    public Registo(Registo outro) {
        this.data = outro.data;
        this.rendimento = outro.rendimento;
        this.saldo = outro.saldo;
        this.poupanca = outro.poupanca;
        this.despesas = new HashMap<>();

        for (Map.Entry<Integer, Despesa> entry : outro.despesas.entrySet()) {
            this.despesas.put(entry.getKey(), new Despesa(entry.getValue()));
        }
    }

    /**
     * Método clone.
     */
    @Override
    public Registo clone() {
        return new Registo(this);
    }

    /**
     * Método get para obter a data do registo.
     * 
     * @return Devolve a data do registo das despesas do mês.
     */
    public YearMonth getData() {
        return this.data;
    }

    /**
     * Método get para obter as despesas do registo.
     * 
     * @return Devolve as despesas do mês.
     */
    public Map<Integer,Despesa> getDespesas() {
        return this.despesas;
    }

    /**
     * Método get para obter o rendimento do registo.
     * 
     * @return Devolve o rendimento.
     */
    public double getRendimento() {
        return this.rendimento;
    }

    /**
     * Método get para obter o total de despesas.
     * 
     * @return Devolve o valor total gasto em despesas.
     */
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


    /**
     * Adiciona uma despesa ao registo.
     * 
     * @param d Despesa adicionada.
     */
    public void adicionarDespesa(Despesa d) {
        Despesa c = d.clone();
        this.despesas.put(c.getIdDespesa(), c);
    }

    /**
     * 
     * @param rendimento
     */
    public void setRendimento(double rendimento) {
        this.rendimento = rendimento;
    } 
}