import controller.*;
import model.*;
import view.Janela;
import utils.Serializer;

import java.time.YearMonth;

import javax.swing.*;

public class Main {
    private static final String FICHEIRO_MODELO = "moneycount.dat";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Tentar carregar modelo
            MoneyCount modelo = Serializer.ler(FICHEIRO_MODELO);
            if (modelo == null) {
                modelo = new MoneyCount();
            }

            // Exemplo: adicionar dados se modelo estiver vazio
            if (modelo.getRegistos().isEmpty()) {
                YearMonth mes = YearMonth.now();
                Registo registo = new Registo(mes, 0);

                modelo.adicionarRegisto(registo);
            }

            // Criar controlador e view
            MoneyCountController controller = new MoneyCountController(modelo);
            controller.setDataModelo(YearMonth.now());

            Janela janela = new Janela(controller);
            SwingUtilities.invokeLater(() -> {
                janela.atualizarVista();
            });
            janela.setVisible(true);

            final MoneyCount modeloFinal = modelo;

            // Guardar ao sair
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Serializer.guardar(FICHEIRO_MODELO, modeloFinal);
            }));
        });
    }
}
