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
import utils.Serializer;

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
    public Registo getRegistoAtual() {
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
    public double getTotalDespesas() {
        double total = 0;
        for (Despesa d : getRegistoAtual().getDespesas().values()) {
            if (d instanceof DespesaFixa df) {
                if (df.isPago()) {
                    total += df.getMontante(); // só soma se estiver paga
                }
            } else {
                total += d.getMontante(); // despesa normal conta sempre
            }
        }
        return total;
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

            if (!data.isAfter(hoje)) {
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
     * Verifica se já existe registo para o mês atual.
     * - Se não existir: cria novo e copia despesas fixas do mês anterior.
     * - Se já existir: atualiza as despesas fixas (adiciona novas que não existam).
     */
    private void verificarOuCriarRegisto() {
        YearMonth mesAnterior = dataModelo.minusMonths(1);
        Registo registoAtual = modelo.getRegistos().get(dataModelo);
        Registo anterior = modelo.getRegistos().get(mesAnterior);

        // Caso não exista o registo atual, cria-o
        if (registoAtual == null) {
            double rendimento = (anterior != null) ? anterior.getRendimento() : 0.0;
            registoAtual = new Registo(dataModelo, rendimento);
            modelo.getRegistos().put(dataModelo, registoAtual);
        }

        // Se houver registo anterior, copia/adiciona despesas fixas que ainda não existam
        if (anterior != null) {
            for (Despesa d : anterior.getDespesas().values()) {
                if (d instanceof DespesaFixa df) {
                    boolean jaExiste = registoAtual.getDespesas().values().stream()
                        .anyMatch(existing -> existing.getNome().equals(df.getNome()) && existing instanceof DespesaFixa);

                    if (!jaExiste) {
                        DespesaFixa nova = new DespesaFixa(df.getNome(), df.getMontante(), false);
                        registoAtual.adicionarDespesa(nova);
                    }
                }
            }
        }
    }

    /**
     * 
     * @param idDespesa
     * @param paga
     */
    public void marcarDespesaComoPaga(int idDespesa, boolean paga) {
        List<Despesa> lista = this.getDespesasMesAtual(); // garantees que isto devolve a lista "viva", não cópia
        for (Despesa d : lista) {
            if (d.getIdDespesa() == idDespesa) {
                if (d instanceof DespesaFixa) {
                    DespesaFixa df = (DespesaFixa) d;
                    df.setPago(paga); // precisa existir setter
                    // Se tiveres persistência, grava aqui (ex.: salvar no ficheiro/bd)
                } 
            }
        }
    }

    // Editar despesa fixa
    public void editarDespesaFixaFuturas(int idDespesa, double novoValor) {
        Registo registoAtual = getRegistoAtual();
        if (registoAtual == null) return;

        Despesa despesaAtual = registoAtual.getDespesas().get(idDespesa);
        if (!(despesaAtual instanceof DespesaFixa)) return;

        // Atualiza o valor da despesa do mês atual diretamente
        despesaAtual.setMontante(novoValor);

        // Identificador único (caso exista)
        int idDespesaFixa = despesaAtual.getIdDespesa();
        String nomeDespesa = despesaAtual.getNome();

        // Atualiza todos os registos a partir do mês atual
        for (Registo r : modelo.getRegistos().values()) {
            if (!r.getData().isBefore(registoAtual.getData())) {
                for (Despesa d : r.getDespesas().values()) {
                    if (d instanceof DespesaFixa df) {
                        boolean mesmaDespesa = (df.getIdDespesa() == idDespesaFixa)
                            || df.getNome().equals(nomeDespesa);
                        if (mesmaDespesa) {
                            df.setMontante(novoValor);
                        }
                    }
                }
            }
        }
    }




    public void removerDespesaFixaFuturas(int idDespesa) {
        Registo registo = getRegistoAtual();
        Despesa despesaAtual = registo.getDespesas().get(idDespesa);
        String nomeDespesa = despesaAtual.getNome();
        for (Registo r : modelo.getRegistos().values()) {
            if (!r.getData().isBefore(registo.getData())) {
                r.getDespesas().values().removeIf(d -> {
                    boolean cond = d instanceof DespesaFixa df && df.getNome().equals(nomeDespesa);
                    return cond;
                });
            }
        }
    }


}




