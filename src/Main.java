import controller.MoneyCountController;
import model.Despesa;
import model.MoneyCount;
import model.Registo;
import view.Janela;

import javax.swing.*;
import java.time.YearMonth;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Criar modelo e registo de exemplo
            MoneyCount modelo = new MoneyCount();

            YearMonth mes = YearMonth.of(2025, 5); // Maio de 2025
            Registo registo = new Registo(mes, 2000.0); // Rendimento: 2000€

            // Adicionar despesas
            Despesa desp1 = new Despesa("Renda", 700.0);
            Despesa desp2 = new Despesa("Supermercado", 250.0);
            Despesa desp3 = new Despesa("Netflix", 700.0);
            Despesa desp4 = new Despesa("Spotify", 250.0);
            Despesa desp5 = new Despesa("Uber", 700.0);
            Despesa desp6 = new Despesa("Supermercado", 250.0);
            Despesa desp7 = new Despesa("Renda", 700.0);
            Despesa desp8 = new Despesa("Supermercado", 250.0);
            Despesa desp9 = new Despesa("Renda", 700.0);
            Despesa desp10 = new Despesa("Supermercado", 250.0);

            registo.adicionarDespesa(desp1);
            registo.adicionarDespesa(desp2);
            registo.adicionarDespesa(desp3);
            registo.adicionarDespesa(desp4);
            registo.adicionarDespesa(desp5);
            registo.adicionarDespesa(desp6);
            registo.adicionarDespesa(desp7);
            registo.adicionarDespesa(desp8);
            registo.adicionarDespesa(desp9);
            registo.adicionarDespesa(desp10);

            // Adicionar o registo ao modelo
            modelo.adicionarRegisto(registo);

            // Criar controlador e view
            MoneyCountController controller = new MoneyCountController(modelo);
            controller.setDataModelo(mes); // definir mês inicial

            Janela janela = new Janela(controller);
            janela.setVisible(true);
        });
    }
}
