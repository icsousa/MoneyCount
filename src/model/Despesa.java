package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Despesa implements Serializable  {

    /**
     * Variaveis de instância.
     */
    private static int id = 0;
    private int idDespesa;
    private String nome;
    private double montante;
    private LocalDate dia;

    /**
     * Construtor parametrizado.
     * 
     * @param nome Nome da despesa.
     * @param montante Montante gasto na despesa.
     */
    public Despesa(String nome, double montante) {
        this.idDespesa = this.id++;
        this.nome = nome;
        this.montante = montante;
        this.dia = LocalDate.now();
    }

    /**
     * Construtor cópia.
     * 
     * @param outra Despesa copiada.
     */
    public Despesa(Despesa outra) {
        this.idDespesa = outra.idDespesa;
        this.nome = outra.nome;
        this.montante = outra.montante;
        this.dia = LocalDate.of(outra.dia.getYear(), outra.dia.getMonth(), outra.dia.getDayOfMonth());
    }


    /**
     * Método clone.
     * 
     * @return Clone da despesa.
     */
    @Override
    public Despesa clone() {
        return new Despesa(this);
    }
    
    /**
     * Get do montante da despesa.
     * 
     * @return Montante.
     */
    public double getMontante() {
        return this.montante;
    }

    /**
     * Get do id da despesa.
     * 
     * @return Id da Despesa.
     */
    public int getIdDespesa() {
        return this.idDespesa;
    }

    /**
     * Get do nome da despesa.
     * 
     * @return Nome da Despesa.
     */
    public String getNome() {
        return this.nome;
    }

    /**
     * 
     * @param montante
     */
    public void setMontante(double montante){
        this.montante = montante;
    }

}
