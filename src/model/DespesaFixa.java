package model;

import java.io.Serializable;

public class DespesaFixa extends Despesa implements Serializable {

    private boolean pago;

    /**
     * Construtor parametrizado.
     * * @param idDespesa Id único da despesa.
     * @param nome Nome da despesa fixa.
     * @param montante Montante da despesa fixa.
     * @param pago Indica se já foi paga.
     */
    public DespesaFixa(int idDespesa, String nome, double montante, boolean pago) {
        super(idDespesa, nome, montante); // Passa o ID para a superclasse
        this.pago = pago;
    }

    /**
     * Construtor cópia.
     * * @param outra DespesaFixa a copiar.
     */
    public DespesaFixa(DespesaFixa outra) {
        super(outra); // Usa o construtor cópia da superclasse
        this.pago = outra.isPago();
    }

    /**
     * Método clone.
     */
    @Override
    public DespesaFixa clone() {
        return new DespesaFixa(this);
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }
}