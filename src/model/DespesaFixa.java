package model;

import java.time.LocalDate;

public class DespesaFixa extends Despesa {

    private boolean pago;

    /**
     * Construtor parametrizado.
     * 
     * @param nome Nome da despesa fixa.
     * @param montante Montante da despesa fixa.
     * @param pago Indica se já foi paga.
     */
    public DespesaFixa(String nome, double montante, boolean pago) {
        super(nome, montante);
        this.pago = pago;
    }

    /**
     * Construtor cópia.
     * 
     * @param outra DespesaFixa a copiar.
     */
    public DespesaFixa(DespesaFixa outra) {
        super(outra.getNome(), outra.getMontante());
        this.pago = outra.isPago();
    }

    /**
     * Método clone.
     */
    @Override
    public DespesaFixa clone() {
        return new DespesaFixa(this);
    }

    // Getter e setter para o campo pago
    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }
}
