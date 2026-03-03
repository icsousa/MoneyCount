package controller;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import model.Despesa;
import model.DespesaFixa;
import model.MoneyCount;
import model.Registo;

public class MoneyCountController {
    
    private YearMonth dataModelo;
    private MoneyCount modelo;

    public MoneyCountController(MoneyCount modelo) {
        this.dataModelo = YearMonth.now();
        this.modelo = modelo;
        verificarOuCriarRegisto(); // Garante que o mês atual existe logo ao arrancar
    }

    public YearMonth getDataModelo() {
        return dataModelo;
    }

    public String getDataModeloString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.of("pt", "PT"));
        return dataModelo.format(formatter);
    }

    public void setDataModelo(YearMonth novaData) {
        this.dataModelo = novaData;
    }

    public Registo getRegistoAtual() {
        return modelo.getRegistos().get(dataModelo);
    }

    public Double getRendimentoAtual() {
        Registo r = getRegistoAtual();
        return r != null ? r.getRendimento() : 0.0;
    }

    public Double getSaldoAtual() {
        Registo r = getRegistoAtual();
        return r != null ? r.getRendimento() - r.getTotalDespesas() : 0.0;
    }

    public double getTotalDespesas() {
        Registo r = getRegistoAtual();
        if (r == null) return 0.0;

        double total = 0;
        for (Despesa d : r.getDespesas().values()) {
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

    public List<Despesa> getDespesasMesAtual() {
        Registo r = getRegistoAtual();
        return r != null ? new ArrayList<>(r.getDespesas().values()) : new ArrayList<>();
    }

    public String[] getDadosGraficoMesAtual() {
        double rendimento = 0, despesas = 0, saldo = 0, poupancaTotal = 0;

        for (Map.Entry<YearMonth, Registo> entry : modelo.getRegistos().entrySet()) {
            Registo r = entry.getValue();
            double saldoMes = r.getRendimento() - r.getTotalDespesas();

            if (entry.getKey().equals(dataModelo)) {
                rendimento = r.getRendimento();
                despesas = r.getTotalDespesas();
                saldo = saldoMes;
            }
            poupancaTotal += saldoMes;
        }

        return new String[] {
            String.valueOf(despesas),
            String.valueOf(rendimento),
            String.valueOf(saldo),
            String.valueOf(poupancaTotal)
        };
    }

    public double getPoupancaAcumulada() {
        YearMonth hoje = YearMonth.now();
        double total = 0.0;

        for (Map.Entry<YearMonth, Registo> entry : modelo.getRegistos().entrySet()) {
            if (entry.getKey().isBefore(hoje)) {
                Registo r = entry.getValue();
                total += r.getRendimento() - r.getTotalDespesas();
            }
        }
        return total;
    }

    public void avancarMes() {
        dataModelo = dataModelo.plusMonths(1);
        verificarOuCriarRegisto();
    }

    public void retrocederMes() {
        dataModelo = dataModelo.minusMonths(1);
        verificarOuCriarRegisto();
    }

    // ==========================================
    // LÓGICA DE GESTÃO DE DESPESAS E IDs
    // ==========================================

    /**
     * Procura em todos os registos qual é o ID mais elevado e devolve esse ID + 1.
     * Isto previne que despesas novas sobreponham despesas antigas ao reabrir a app.
     */
    private int gerarProximoId() {
        int maxId = -1;
        for (Registo r : modelo.getRegistos().values()) {
            for (Integer id : r.getDespesas().keySet()) {
                if (id > maxId) {
                    maxId = id;
                }
            }
        }
        return maxId + 1;
    }

    public void adicionarDespesa(String nome, double valor, boolean fixa) {
        Registo registo = getRegistoAtual();
        int novoId = gerarProximoId(); // Garante um ID único!

        Despesa nova;
        if (fixa) {
            // Usa o ID gerado, se não tiveres este construtor, tens de usar o setIdDespesa
            nova = new DespesaFixa(novoId, nome, valor, false);
        } else {
            nova = new Despesa(novoId, nome, valor);
        }

        registo.adicionarDespesa(nova);
    }

    public void removerDespesa(int idDespesa) {
        Registo registo = getRegistoAtual();
        if (registo != null) {
            registo.getDespesas().remove(idDespesa);
        }
    }

    public void editarDespesa(int idDespesa, double novoValor) {
        Registo registo = getRegistoAtual();
        if (registo != null) {
            Despesa despesa = registo.getDespesas().get(idDespesa);
            if (despesa != null) {
                despesa.setMontante(novoValor);
            }
        }
    }
    
    public void atualizarRendimento(double novoRendimento){
        Registo registo = getRegistoAtual();
        if (registo != null) registo.setRendimento(novoRendimento);
    }

    private void verificarOuCriarRegisto() {
        YearMonth mesAnterior = dataModelo.minusMonths(1);
        Registo registoAtual = modelo.getRegistos().get(dataModelo);
        Registo anterior = modelo.getRegistos().get(mesAnterior);

        if (registoAtual == null) {
            double rendimento = (anterior != null) ? anterior.getRendimento() : 0.0;
            registoAtual = new Registo(dataModelo, rendimento);
            modelo.getRegistos().put(dataModelo, registoAtual);
        }

        if (anterior != null) {
            for (Despesa d : anterior.getDespesas().values()) {
                if (d instanceof DespesaFixa df) {
                    boolean jaExiste = registoAtual.getDespesas().values().stream()
                        .anyMatch(existing -> existing.getNome().equals(df.getNome()) && existing instanceof DespesaFixa);

                    if (!jaExiste) {
                        int novoId = gerarProximoId(); // Garante um ID único para a cópia!
                        DespesaFixa nova = new DespesaFixa(novoId, df.getNome(), df.getMontante(), false);
                        registoAtual.adicionarDespesa(nova);
                    }
                }
            }
        }
    }

    public void marcarDespesaComoPaga(int idDespesa, boolean paga) {
        Registo registo = getRegistoAtual();
        if (registo != null) {
            // O(1) Lookup: Em vez de percorrer a lista toda com um 'for', vamos diretos ao ID
            Despesa d = registo.getDespesas().get(idDespesa);
            if (d instanceof DespesaFixa df) {
                df.setPago(paga);
            }
        }
    }

    public void editarDespesaFixaFuturas(int idDespesa, double novoValor) {
        Registo registoAtual = getRegistoAtual();
        if (registoAtual == null) return;

        Despesa despesaAtual = registoAtual.getDespesas().get(idDespesa);
        if (!(despesaAtual instanceof DespesaFixa)) return;

        despesaAtual.setMontante(novoValor);
        int idDespesaFixa = despesaAtual.getIdDespesa();
        String nomeDespesa = despesaAtual.getNome();

        for (Registo r : modelo.getRegistos().values()) {
            if (!r.getData().isBefore(registoAtual.getData())) {
                for (Despesa d : r.getDespesas().values()) {
                    if (d instanceof DespesaFixa df) {
                        if (df.getIdDespesa() == idDespesaFixa || df.getNome().equals(nomeDespesa)) {
                            df.setMontante(novoValor);
                        }
                    }
                }
            }
        }
    }

    public void removerDespesaFixaFuturas(int idDespesa) {
        Registo registo = getRegistoAtual();
        if (registo == null) return;

        Despesa despesaAtual = registo.getDespesas().get(idDespesa);
        if (despesaAtual == null) return;
        
        String nomeDespesa = despesaAtual.getNome();
        for (Registo r : modelo.getRegistos().values()) {
            if (!r.getData().isBefore(registo.getData())) {
                r.getDespesas().values().removeIf(d -> 
                    d instanceof DespesaFixa df && df.getNome().equals(nomeDespesa)
                );
            }
        }
    }
}