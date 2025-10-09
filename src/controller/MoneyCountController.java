package controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import model.Despesa;
import model.DespesaFixa;
import model.MoneyCount;
import model.Registo;

public class MoneyCountController {
    
    /**
     * Variaveis de instância.
     */
    private YearMonth dataModelo;
    private MoneyCount modelo;

    /**
     * Construtor parametrizado.
     * 
     * @param modelo Modelo.
     */
    public MoneyCountController(MoneyCount modelo) {
        this.dataModelo = YearMonth.now();
        this.modelo = modelo;
    }

    /**
     * Get da data.
     * 
     * @return Data do modelo.
     */
    public YearMonth getDataModelo() {
        return dataModelo;
    }

    /**
     * Get da data em formato string.
     * 
     * @return Data do modelo formatada como string.
     */
    public String getDataModeloString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.of("pt", "PT"));
        return dataModelo.format(formatter);
    }

    /**
     * Set da data.
     * 
     * @param novaData Data do modelo.
     */
    public void setDataModelo(YearMonth novaData) {
        this.dataModelo = novaData;
    }

    /**
     * Get do registo atual.
     * 
     * @return Registo atual.
     */
    private Registo getRegistoAtual() {
        return (modelo.getRegistos().get(dataModelo));
    }

    /**
     * Get do rendimento atual.
     * 
     * @return Rendimento atual.
     */
    public Double getRendimentoAtual() {
        Registo r = getRegistoAtual();
        return r.getRendimento();
    }

    /**
     * Get do saldo atual.
     * 
     * @return saldo atual.
     */
    public Double getSaldoAtual() {
        Registo r = getRegistoAtual();
        return r.getRendimento() - r.getTotalDespesas();
    }

    /**
     * Get do rendimento atual.
     * 
     * @return Rendimento atual.
     */
    public Double getTotalDespesas() {
        Registo r = getRegistoAtual();
        return r.getTotalDespesas();
    }

    /**
     * Obter todas as despesas do mês.
     * 
     * @return Devolve uma lista das despesas do mês. 
     */
    public List<Despesa> getDespesasMesAtual() {
        Registo r = getRegistoAtual();
        return new ArrayList<>(r.getDespesas().values());
    }

    /**
     * Obter os dados para o gráfico.
     * 
     * @return Dados do gráfico.
     */
    public String[] getDadosGraficoMesAtual() {
        double rendimento = 0;
        double despesas = 0;
        double saldo = 0;
        double poupancaTotal = 0;

        for (Map.Entry<YearMonth, Registo> entry : modelo.getRegistos().entrySet()) {
            Registo r = entry.getValue();
            double saldoMes = r.getRendimento() - r.getTotalDespesas();

            if (entry.getKey().equals(dataModelo)) {
                rendimento = r.getRendimento();
                despesas = r.getTotalDespesas();
                saldo = saldoMes;
            }

            poupancaTotal += saldoMes; // soma de saldos de todos os meses
        }

        return new String[] {
            String.valueOf(despesas),
            String.valueOf(rendimento),
            String.valueOf(saldo),
            String.valueOf(poupancaTotal)
        };
    }

    /**
     * Calculo da poupança.
     * 
     * @return Retorna o valor total da poupança até à data atual.
     */
    public double getPoupancaAcumulada() {
        YearMonth hoje = YearMonth.now();
        double total = 0.0;

        for (Map.Entry<YearMonth, Registo> entry : modelo.getRegistos().entrySet()) {
            YearMonth data = entry.getKey();

            if (data.isBefore(hoje)) {
                Registo r = entry.getValue();
                total += r.getRendimento() - r.getTotalDespesas();
            }
        }

        return total;
    }

    /**
     * Avançar mês.
     */
    public void avancarMes() {
        dataModelo = dataModelo.plusMonths(1);
        verificarOuCriarRegisto();
    }

    /**
     * Retroceder mês.
     */
    public void retrocederMes() {
        dataModelo = dataModelo.minusMonths(1);
        verificarOuCriarRegisto();
    }

    /**
     * 
     * @param nome
     * @param valor
     * @param fixa
     */
    public void adicionarDespesa(String nome, double valor, boolean fixa) {
        Registo registo = getRegistoAtual();

        Despesa nova;
        if (fixa) {
            nova = new DespesaFixa(nome, valor, false);
        } else {
            nova = new Despesa(nome, valor);
        }

        registo.adicionarDespesa(nova);
    }

    /**
     * 
     * @param idDespesa
     */
    public void removerDespesa(int idDespesa) {
        Registo registo = getRegistoAtual();
        registo.getDespesas().remove(idDespesa);
    }

    /**
     * 
     * @param idDespesa
     * @param novoValor
     */
    public void editarDespesa(int idDespesa, double novoValor) {
        Registo registo = getRegistoAtual();
        Despesa despesa = registo.getDespesas().get(idDespesa);
        if (despesa != null) {
            despesa.setMontante(novoValor);
        }
    }
    
    /**
     * 
     * @param novoRendimento
     */
    public void atualizarRendimento(double novoRendimento){
        Registo registo = getRegistoAtual();
        registo.setRendimento(novoRendimento);
    }


    /**
     * Verificar se existe registo, se não existir, cria um.
     */
    private void verificarOuCriarRegisto() {
        if (!modelo.getRegistos().containsKey(dataModelo)) {
            modelo.getRegistos().put(dataModelo, new Registo(dataModelo, 0));
        }
    }
}




