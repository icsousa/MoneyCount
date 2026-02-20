package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Despesa implements Serializable {

    private int idDespesa;
    private String nome;
    private double montante;
    private LocalDate dia;

    /**
     * Construtor parametrizado (Agora recebe o ID externamente).
     * * @param idDespesa Id único da despesa.
     * @param nome Nome da despesa.
     * @param montante Montante gasto na despesa.
     */
    public Despesa(int idDespesa, String nome, double montante) {
        this.idDespesa = idDespesa;
        this.nome = nome;
        this.montante = montante;
        this.dia = LocalDate.now();
    }

    /**
     * Construtor cópia.
     * * @param outra Despesa copiada.
     */
    public Despesa(Despesa outra) {
        this.idDespesa = outra.idDespesa;
        this.nome = outra.nome;
        this.montante = outra.montante;
        this.dia = LocalDate.of(outra.dia.getYear(), outra.dia.getMonth(), outra.dia.getDayOfMonth());
    }

    /**
     * Método clone.
     * * @return Clone da despesa.
     */
    @Override
    public Despesa clone() {
        return new Despesa(this);
    }
    
    public double getMontante() {
        return this.montante;
    }

    public int getIdDespesa() {
        return this.idDespesa;
    }

    public String getNome() {
        return this.nome;
    }

    public void setMontante(double montante){
        this.montante = montante;
    }
}