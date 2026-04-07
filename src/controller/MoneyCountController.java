package controller;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import model.Despesa;
import model.DespesaFixa;
import model.Entrada;
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

    // Devolve APENAS o rendimento base (ex: Salário)
    public Double getRendimentoAtual() {
        Registo r = getRegistoAtual();
        return r != null ? r.getRendimento() : 0.0;
    }

    // NOVO: O Saldo conta com a base + as entradas extra - as despesas
    public Double getSaldoAtual() {
        Registo r = getRegistoAtual();
        return r != null ? r.getRendimento() + r.getTotalEntradas() - r.getTotalDespesas() : 0.0;
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
            
            // Saldo real do mês: Rendimento Base + Entradas - Despesas
            double saldoMes = r.getRendimento() + r.getTotalEntradas() - r.getTotalDespesas();

            if (entry.getKey().equals(dataModelo)) {
                rendimento = r.getRendimento(); // Para o gráfico/resumo, a base mantém-se separada
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
                // A poupança antiga também tem de contar com as entradas extra!
                total += r.getRendimento() + r.getTotalEntradas() - r.getTotalDespesas();
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
    // LÓGICA DE GESTÃO DE DESPESAS, ENTRADAS E IDs
    // ==========================================

    /**
     * Procura em todos os registos qual é o ID mais elevado e devolve esse ID + 1.
     * Partilhado entre Entradas e Despesas para manter uma ordem cronológica limpa.
     */
    private int gerarProximoId() {
        int maxId = -1;
        for (Registo r : modelo.getRegistos().values()) {
            for (Integer id : r.getDespesas().keySet()) {
                if (id > maxId) maxId = id;
            }
            for (Integer id : r.getEntradas().keySet()) {
                if (id > maxId) maxId = id;
            }
        }
        return maxId + 1;
    }

    public void adicionarDespesa(String nome, double valor, boolean fixa) {
        // Formatar o nome: Primeira letra maiúscula
        if (nome != null && !nome.trim().isEmpty()) {
            nome = nome.trim();
            nome = nome.substring(0, 1).toUpperCase() + nome.substring(1);
        } else {
            nome = "Despesa sem nome";
        }

        Registo registoAtual = getRegistoAtual();
        int novoId = gerarProximoId(); // Garante um ID único!

        if (fixa) {
            DespesaFixa nova = new DespesaFixa(novoId, nome, valor, false);
            registoAtual.adicionarDespesa(nova);

            // Propaga para meses FUTUROS que já tenham sido criados na memória
            for (Map.Entry<YearMonth, Registo> entry : modelo.getRegistos().entrySet()) {
                if (entry.getKey().isAfter(dataModelo)) { 
                    int idFuturo = gerarProximoId();
                    DespesaFixa novaFutura = new DespesaFixa(idFuturo, nome, valor, false);
                    entry.getValue().adicionarDespesa(novaFutura);
                }
            }
        } else {
            Despesa nova = new Despesa(novoId, nome, valor);
            registoAtual.adicionarDespesa(nova);
        }
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

        // Se o mês ainda não existe, cria-o e copia as despesas fixas do mês anterior.
        if (registoAtual == null) {
            double rendimento = (anterior != null) ? anterior.getRendimento() : 0.0;
            registoAtual = new Registo(dataModelo, rendimento);
            modelo.getRegistos().put(dataModelo, registoAtual);

            // A cópia de despesas fixas SÓ acontece na criação do mês
            if (anterior != null) {
                for (Despesa d : anterior.getDespesas().values()) {
                    if (d instanceof DespesaFixa df) {
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

    // ==========================================
    // MÉTODOS PARA GESTÃO DE ENTRADAS
    // ==========================================

    public List<Entrada> getEntradasMesAtual() {
        Registo r = getRegistoAtual();
        return r != null ? new ArrayList<>(r.getEntradas().values()) : new ArrayList<>();
    }

    public void adicionarEntrada(String nome, double valor) {
        // Formatar o nome: Primeira letra maiúscula
        if (nome != null && !nome.trim().isEmpty()) {
            nome = nome.trim();
            nome = nome.substring(0, 1).toUpperCase() + nome.substring(1);
        } else {
            nome = "Entrada sem nome"; 
        }

        Registo registoAtual = getRegistoAtual();
        int novoId = gerarProximoId(); 

        Entrada nova = new Entrada(novoId, nome, valor);
        registoAtual.adicionarEntrada(nova);
    }

    public void removerEntrada(int idEntrada) {
        Registo registo = getRegistoAtual();
        if (registo != null) {
            registo.getEntradas().remove(idEntrada);
        }
    }

    public void editarEntrada(int idEntrada, double novoValor) {
        Registo registo = getRegistoAtual();
        if (registo != null) {
            Entrada entrada = registo.getEntradas().get(idEntrada);
            if (entrada != null) {
                entrada.setMontante(novoValor);
            }
        }
    }
}