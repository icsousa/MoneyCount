package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Entrada implements Serializable {

    private int idEntrada;
    private String nome;
    private double montante;
    private LocalDate dia;

    /**
     * Construtor parametrizado.
     * @param idEntrada Id único da entrada.
     * @param nome Nome da fonte de rendimento (ex: "Salário", "Venda de algo").
     * @param montante Valor da entrada de dinheiro.
     */
    public Entrada(int idEntrada, String nome, double montante) {
        this.idEntrada = idEntrada;
        this.nome = nome;
        this.montante = montante;
        this.dia = LocalDate.now();
    }

    /**
     * Construtor cópia.
     * @param outra Entrada copiada.
     */
    public Entrada(Entrada outra) {
        this.idEntrada = outra.idEntrada;
        this.nome = outra.nome;
        this.montante = outra.montante;
        this.dia = LocalDate.of(outra.dia.getYear(), outra.dia.getMonth(), outra.dia.getDayOfMonth());
    }

    /**
     * Método clone.
     * @return Clone da entrada.
     */
    @Override
    public Entrada clone() {
        return new Entrada(this);
    }
    
    public double getMontante() {
        return this.montante;
    }

    public int getIdEntrada() {
        return this.idEntrada;
    }

    public String getNome() {
        return this.nome;
    }

    public void setMontante(double montante){
        this.montante = montante;
    }
}