package view;

import controller.MoneyCountController;
import model.Despesa;
import model.DespesaFixa;

import javax.swing.*;
import javax.swing.GroupLayout.Group;

import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.scene.control.Tooltip;


public class Janela extends JFrame {
    private MoneyCountController controller;
    private JTable tabelaDespesas;
    private JLabel lblMontante, lblSaldo, lblDespesas;
    private JLabel lblData;
    private JPanel listaDespesas;
    private CaixaTituloValor caixaSalario, caixaSaldo, caixaDespesas, caixaPoupanca;
    private JFXPanel fxPanel;
    private final int alturaGrafico = 300;

    private void atualizarGrafico(JFXPanel fxPanel) {
        List<Despesa> despesas = controller.getDespesasMesAtual();

        // Agrupar por nome e somar valores
        Map<String, Double> somaPorNome = new HashMap<>();
        Map<String, java.awt.Color> coresPorNome = new HashMap<>();
        for (Despesa d : despesas) {
            somaPorNome.put(d.getNome(), somaPorNome.getOrDefault(d.getNome(), 0.0) + d.getMontante());
            coresPorNome.put(d.getNome(), gerarCorParaNome(d.getNome()));
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        somaPorNome.forEach((nome, valor) -> pieChartData.add(new PieChart.Data(nome, valor)));

        Platform.runLater(() -> {
            PieChart chart = new PieChart(pieChartData);
            chart.setLegendVisible(false);
            chart.setLabelsVisible(true);
            chart.setPrefHeight(400);
            chart.setPrefWidth(600);
            chart.setStyle("-fx-background-color: white;");

            Scene scene = new Scene(chart);
            scene.setFill(javafx.scene.paint.Color.WHITE); // <- FUNDO BRANCO
            fxPanel.setScene(scene);

            // Espera um instante para garantir que os nós do gráfico existem
            // depois de fxPanel.setScene(new Scene(chart));
            Platform.runLater(() -> {
                double total = somaPorNome.values().stream().mapToDouble(Double::doubleValue).sum();

                for (PieChart.Data data : chart.getData()) {
                    // captura valores que usaremos no listener
                    final String nome = data.getName();
                    final double valor = data.getPieValue();
                    final double percentagem = total == 0 ? 0 : (valor / total) * 100.0;

                    // cor (aplica quando o node ficar disponível)
                    final java.awt.Color awtColor = coresPorNome.get(nome);
                    final String rgb = awtColor == null ? null :
                            String.format("%d, %d, %d",
                                    awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

                    // listener que instala tooltip e aplica cor quando node existir
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            // aplica cor se disponível
                            if (rgb != null) {
                                newNode.setStyle("-fx-pie-color: rgb(" + rgb + ");");
                            }
                            // instala tooltip com nome + valor + percentagem
                            Tooltip tip = new Tooltip(String.format("%s: %.2f € (%.1f%%)", nome, valor, percentagem));
                            Tooltip.install(newNode, tip);
                        }
                    });

                    // se node já existe (caso raro), aplica imediatamente
                    if (data.getNode() != null) {
                        if (rgb != null) data.getNode().setStyle("-fx-pie-color: rgb(" + rgb + ");");
                        Tooltip tip = new Tooltip(String.format("%s: %.2f € (%.1f%%)", nome, valor, percentagem));
                        Tooltip.install(data.getNode(), tip);
                    }
                }
            });

        });
    }



    public void atualizarVista() {
        // Atualiza o mês no topo
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.of("pt", "PT"));
        lblData.setText(controller.getDataModelo().format(formatter));

        // Atualiza os valores das caixas de resumo
        String montante = String.format("%.2f €", controller.getRendimentoAtual());
        String saldo = String.format("%.2f €", controller.getSaldoAtual());
        String totalDespesas = String.format("%.2f €", controller.getTotalDespesas());
        String poupanca = String.format("%.2f €", controller.getPoupancaAcumulada());

        caixaSalario.setValor(montante);
        caixaSaldo.setValor(saldo);
        caixaDespesas.setValor(totalDespesas);
        caixaPoupanca.setValor(poupanca);

        // Atualiza a tabela de despesas
        atualizarTabelaDespesas();

        listaDespesas.removeAll();
        listaDespesas.setLayout(new GridBagLayout());
        listaDespesas.setBackground(new Color(238, 238, 238)); // mesmo cinzento do painel direito
        listaDespesas.setOpaque(true);

        GridBagConstraints gbcList = new GridBagConstraints();
        gbcList.gridx = 0;
        gbcList.gridy = 0;
        gbcList.fill = GridBagConstraints.HORIZONTAL;
        gbcList.anchor = GridBagConstraints.NORTH;
        gbcList.insets = new Insets(2, 2, 2, 2); // espaçamento entre itens

        for (Despesa despesa : controller.getDespesasMesAtual()) {
            int idDespesa = despesa.getIdDespesa();
            String nome = despesa.getNome();
            String valor = String.format("%.2f €", despesa.getMontante());
            boolean fixa = despesa instanceof DespesaFixa;
            boolean paga = fixa && ((DespesaFixa) despesa).isPago();

            listaDespesas.add(new ItemDespesa(idDespesa, nome, valor, fixa, paga), gbcList);
            gbcList.gridy++;
        }

        // cola elástica para empurrar o conteúdo para cima quando há poucas despesas
        gbcList.weighty = 1.0;
        listaDespesas.add(Box.createVerticalGlue(), gbcList);

        listaDespesas.revalidate();
        listaDespesas.repaint();
        SwingUtilities.invokeLater(() -> listaDespesas.updateUI());
        // Atualiza gráfico
        atualizarGrafico(fxPanel);

    }


    private void atualizarTabelaDespesas() {
        DefaultTableModel model = (DefaultTableModel) tabelaDespesas.getModel();
        model.setRowCount(0); // Limpa todas as linhas

        for (Despesa despesa : controller.getDespesasMesAtual()) {
            model.addRow(new Object[]{
                despesa.getNome(),
                String.format("%.2f €", despesa.getMontante()),
                despesa instanceof DespesaFixa ? "Fixa" : "Normal"
            });
        }
    }


    // Reutiliza a função que você já tem para gerar cores do nome
    private Color gerarCorParaNome(String nome) {
        int hash = nome.hashCode();
        int r = 100 + Math.abs(hash) % 100;
        int g = 100 + Math.abs(hash / 100) % 100;
        int b = 100 + Math.abs(hash / 10000) % 100;
        return new Color(r, g, b);

    }



    public Janela(MoneyCountController controller) {
        this.controller = controller;
        fxPanel = new JFXPanel();
        lblData = new JLabel();
        lblMontante = new JLabel();
        lblSaldo = new JLabel();
        lblDespesas = new JLabel();

        tabelaDespesas = new JTable(new DefaultTableModel(new Object[]{"Nome", "Valor"}, 0));
        tabelaDespesas.setFillsViewportHeight(true);
        tabelaDespesas.setPreferredScrollableViewportSize(new Dimension(300, 150));

        setTitle("MoneyↃount");

        Toolkit kit = Toolkit.getDefaultToolkit();  
        Dimension tamTela = kit.getScreenSize();  

        //Pega largura e altura da tela 
        int larg = tamTela.width;  
        int alt = tamTela.height;

        // Janela maximizada mas com barra de título visível
        setSize(larg,alt);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        inicializarComponentes();
        setVisible(true);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout());

        Toolkit kit = Toolkit.getDefaultToolkit();  
        Dimension tamTela = kit.getScreenSize();  

        //Pega largura e altura da tela 
        int larg = tamTela.width;  
        int alt = tamTela.height;

        // Painel do mês no topo
        JPanel painelMes = new PainelComCantosInferioresArredondados();
        painelMes.setLayout(new BorderLayout());

        painelMes.setBackground(new Color(0x54, 0x54, 0x54)); // #545454

        // Estilo dos botões de navegação
        BotaoRedondo btnAnterior = new BotaoRedondo("←");
        BotaoRedondo btnSeguinte = new BotaoRedondo("→");

        btnSeguinte.addActionListener(e -> {
            controller.avancarMes();
            atualizarVista();
        });

        btnAnterior.addActionListener(e -> {
            controller.retrocederMes();
            atualizarVista();
        });
    
        // Painel com margens para os botões (60px do centro)
        JPanel painelEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 400, 5));
        painelEsquerda.setOpaque(false);
        painelEsquerda.add(btnAnterior);

        JPanel painelDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 400, 5));
        painelDireita.setOpaque(false);
        painelDireita.add(btnSeguinte);

        // Texto central do mês
        lblData = new JLabel(controller.getDataModeloString(), SwingConstants.CENTER);
        lblData.setForeground(Color.WHITE);
        lblData.setFont(new Font("Arial", Font.BOLD, 22));

        // Montagem final
        painelMes.add(painelEsquerda, BorderLayout.WEST);
        painelMes.add(lblData, BorderLayout.CENTER);
        painelMes.add(painelDireita, BorderLayout.EAST);

        add(painelMes, BorderLayout.NORTH);


        

        // Painéis principal (dividido)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(larg/2);
        splitPane.setEnabled(false); // Impede o utilizador de arrastar
        splitPane.setDividerSize(0); 

        

        // PAINEL ESQUERDO — Gráfico + Resumo
        Box painelEsquerdo = Box.createVerticalBox();
        painelEsquerdo.setBackground(Color.WHITE);
        painelEsquerdo.setOpaque(true);

        fxPanel = new JFXPanel();
        fxPanel.setPreferredSize(new Dimension(600, alturaGrafico));
        fxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));
        painelEsquerdo.add(fxPanel);

        // Atualiza gráfico
        atualizarGrafico(fxPanel);

        // PAINEL RESUMO (abaixo do gráfico)
        String rendimento = String.format("%.2f €", controller.getRendimentoAtual());
        String saldo = String.format("%.2f €", controller.getSaldoAtual());
        String totalDespesas = String.format("%.2f €", controller.getTotalDespesas());
        String poupanca = String.format("%.2f €", controller.getPoupancaAcumulada());

        JPanel painelResumo = new JPanel(new GridBagLayout());
        painelResumo.setBackground(Color.WHITE);
        painelResumo.setOpaque(true);
        painelResumo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300)); // altura igual à anterior da direita

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Montantes
        gbc.gridx = 0;
        gbc.gridy = 0;
        caixaSalario = new CaixaTituloValor("Salário", rendimento);
        painelResumo.add(caixaSalario, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        caixaSaldo = new CaixaTituloValor("Saldo Disp.", saldo);
        painelResumo.add(caixaSaldo, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        caixaDespesas = new CaixaTituloValor("Despesas", totalDespesas);
        painelResumo.add(caixaDespesas, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        caixaPoupanca = new CaixaTituloValor("Poupança", poupanca);
        //gbc.gridwidth = 2;
        painelResumo.add(caixaPoupanca, gbc);

        painelEsquerdo.add(Box.createVerticalStrut(10));
        painelEsquerdo.add(painelResumo);

        // PAINEL DIREITO — Despesas
        JPanel painelDireito = new JPanel();
        painelDireito.setLayout(new BorderLayout());
        painelDireito.setBackground(new Color(238, 238, 238));
        painelDireito.setOpaque(true);
        painelDireito.setPreferredSize(new Dimension(400, Integer.MAX_VALUE)); // altura igual ao painelEsquerdo

        // Painel horizontal para o título "Despesas" + botões
        JPanel painelTituloComBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        painelTituloComBotoes.setOpaque(false);

        // Botões
        JButton btnDespesa = new BotaoEstiloTitulo("+ Despesa", new Color(174, 239, 164));   // verde pastel
        JButton btnDespesaFixa = new BotaoEstiloTitulo("+ Despesa Fixa", new Color(220, 167, 235)); // lilás pastel

        btnDespesa.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome da despesa:");
            if (nome == null || nome.trim().isEmpty()) return;

            String valorStr = JOptionPane.showInputDialog(this, "Valor da despesa (€):");
            if (valorStr == null || valorStr.trim().isEmpty()) return;

            try {
                double valor = Double.parseDouble(valorStr);
                controller.adicionarDespesa(nome, valor, false);
                atualizarVista();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDespesaFixa.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome da despesa fixa:");
            if (nome == null || nome.trim().isEmpty()) return;

            String valorStr = JOptionPane.showInputDialog(this, "Valor da despesa (€):");
            if (valorStr == null || valorStr.trim().isEmpty()) return;

            try {
                double valor = Double.parseDouble(valorStr);
                controller.adicionarDespesa(nome, valor, true);
                atualizarVista();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        painelTituloComBotoes.add(btnDespesa);
        painelTituloComBotoes.add(btnDespesaFixa);
        painelDireito.add(painelTituloComBotoes, BorderLayout.NORTH);

        // Lista de despesas com scroll
        listaDespesas = new JPanel(new GridBagLayout());
        listaDespesas.setBackground(new Color(238, 238, 238)); // fundo igual ao painel direito
        listaDespesas.setOpaque(true);

        GridBagConstraints gbcList = new GridBagConstraints();
        gbcList.gridx = 0;
        gbcList.gridy = 0;
        gbcList.fill = GridBagConstraints.HORIZONTAL;
        gbcList.anchor = GridBagConstraints.NORTH;
        gbcList.insets = new Insets(2, 2, 2, 2); // espaçamento entre itens

        for (Despesa despesa : controller.getDespesasMesAtual()) {
            int idDespesa = despesa.getIdDespesa();
            String nome = despesa.getNome();
            String valor = String.format("%.2f €", despesa.getMontante());
            listaDespesas.add(new ItemDespesa(idDespesa, nome, valor), gbcList);
            gbcList.gridy++;
        }

        // “cola” flexível no fim para manter a distância
        gbcList.weighty = 1.0;
        listaDespesas.add(Box.createVerticalGlue(), gbcList);

        JScrollPane scrollDespesas = new JScrollPane(listaDespesas);
        scrollDespesas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollDespesas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDespesas.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
        scrollDespesas.setPreferredSize(new Dimension(400, 300));
        scrollDespesas.getVerticalScrollBar().setUI(new ScrollBarCinzaArredondada());
        painelDireito.add(scrollDespesas, BorderLayout.CENTER);

        // Adiciona ao JSplitPane
        splitPane.setLeftComponent(painelEsquerdo);
        splitPane.setRightComponent(painelDireito);


        // Adiciona à janela principal
        add(splitPane, BorderLayout.CENTER);
    }

    private JButton criarBotaoEstilizado(String texto, Color fundo, Color textoCor) {
        JButton botao = new JButton(texto);
        botao.setBackground(fundo);
        botao.setForeground(textoCor);
        botao.setFocusPainted(false);
        botao.setFont(new Font("Arial", Font.BOLD, 14));
        botao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return botao;
    }

    private static class BotaoRedondo extends JButton {
        public BotaoRedondo(String texto) {
            super(texto);
            setPreferredSize(new Dimension(35, 35));
            setMargin(new Insets(0, 0, 0, 0)); 
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 20));
        }


        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0x3A, 0x3A, 0x3A)); // Fundo escuro
            g2.fillOval(0, 0, getWidth(), getHeight());

            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            // Sem borda
        }

    }

    private static class PainelComCantosInferioresArredondados extends JPanel {
        private final int arc = 30;

        public PainelComCantosInferioresArredondados() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);  // mantém os componentes filhos

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Path2D path = new Path2D.Float();
            path.moveTo(0, 0);
            path.lineTo(0, h - arc);
            path.quadTo(0, h, arc, h); // canto inferior esquerdo
            path.lineTo(w - arc, h);
            path.quadTo(w, h, w, h - arc); // canto inferior direito
            path.lineTo(w, 0);
            path.closePath();

            g2.setColor(new Color(0x54, 0x54, 0x54)); // fundo da barra
            g2.fill(path);

            g2.dispose();
        }
    }

    private static class CaixaTituloValor extends JPanel {
        private JLabel lblValor;

        public CaixaTituloValor(String titulo) {
            this(titulo, null);
        }
        
        public CaixaTituloValor(String titulo, String valor) {
            setLayout(new BorderLayout());
            setOpaque(false);

            // Painel arredondado para o título
            JPanel fundoTitulo = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(220, 220, 220));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 70, 70);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    // Calcula tamanho baseado no texto e botão (se houver)
                    Component[] comps = getComponents();
                    int totalWidth = 20; // margens laterais
                    int maxHeight = 0;

                    for (Component c : comps) {
                        Dimension d = c.getPreferredSize();
                        totalWidth += d.width + 10; // espaçamento
                        maxHeight = Math.max(maxHeight, d.height);
                    }

                    return new Dimension(totalWidth, maxHeight + 10); // margem vertical
                }
            };
            fundoTitulo.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
            fundoTitulo.setOpaque(false);

            JLabel lblTitulo = new JLabel(titulo);
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 42));

            fundoTitulo.add(lblTitulo);

            // Só adiciona o botão se for "Salário"
            if (titulo.equalsIgnoreCase("Salário")) {
                JButton btnEditar = new BotaoRedondoAzul("✏️");
                btnEditar.setToolTipText("Editar Montante");
                fundoTitulo.add(btnEditar);

                btnEditar.addActionListener(e -> {
                    String novoSalarioStr = JOptionPane.showInputDialog(this, "Novo salário (€):");
                    if (novoSalarioStr == null || novoSalarioStr.trim().isEmpty()) return;

                    try {
                        double novoSalario = Double.parseDouble(novoSalarioStr);
                        ((Janela) SwingUtilities.getWindowAncestor(this)).controller.atualizarRendimento(novoSalario);
                        ((Janela) SwingUtilities.getWindowAncestor(this)).atualizarVista();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Valor inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }

            lblValor = new JLabel(valor);
            lblValor.setFont(new Font("Arial", Font.PLAIN, 26));
            lblValor.setHorizontalAlignment(SwingConstants.LEFT);
            JPanel valorPanel = new JPanel(new BorderLayout());
            valorPanel.setOpaque(false);
            valorPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0)); // 5px à esquerda
            valorPanel.add(lblValor, BorderLayout.CENTER);

            add(valorPanel, BorderLayout.SOUTH);
            add(fundoTitulo, BorderLayout.CENTER);
            
        }

        // Botão azul redondo com emoji
        private static class BotaoRedondoAzul extends JButton {
            public BotaoRedondoAzul(String texto) {
                super(texto);
                setPreferredSize(new Dimension(35, 35));
                setMargin(new Insets(0, 0, 0, 0)); 
                setFocusPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setBorderPainted(false);
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 22));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(82,113,255)); 
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {}
        }

        public void setValor(String novoValor) {
            lblValor.setText(novoValor);
        }

    }

    private static class BotaoEstiloTitulo extends JButton {
        private final Color corFundo;

        public BotaoEstiloTitulo(String texto, Color corFundo) {
            super(texto);
            this.corFundo = corFundo;
            setFont(new Font("Arial", Font.BOLD, 26));
            setForeground(new Color(84, 84, 84));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorderPainted(false);
            setMargin(new Insets(10, 20, 10, 20)); // Espaçamento interno
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int width = fm.stringWidth(getText()) + 50;
            int height = 50; // Altura semelhante à do título
            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(corFundo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            // Sem borda
        }
    }

    private static class ItemDespesa extends JPanel {
        private JPanel painelConteudo;
        private JLabel lblNome;
        private JLabel lblValor;

        public ItemDespesa(int idDespesa, String nome, String valor) {
            this(idDespesa, nome, valor, false, true); // compatibilidade
        }

        public ItemDespesa(int idDespesa, String nome, String valor, boolean fixa, boolean paga) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            setOpaque(false);

            // Checkbox apenas se for despesa fixa
            final JCheckBox chkPaga = fixa ? new JCheckBox() : null;
            if (chkPaga != null) {
                chkPaga.setSelected(paga);
                chkPaga.setOpaque(false);
                chkPaga.setToolTipText("Marcar como paga");
            }

            // Painel colorido arredondado
            // Painel de conteúdo (onde fica o fundo colorido e todos os elementos)
            painelConteudo = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Janela janela = (Janela) SwingUtilities.getWindowAncestor(ItemDespesa.this);
                    Color base = janela.gerarCorParaNome(nome);

                    int alpha = 255;

                    if (fixa && chkPaga != null && !chkPaga.isSelected()) {
                        alpha = 100;
                        lblNome.setForeground(new Color(90, 90, 90));
                        lblValor.setForeground(new Color(90, 90, 90));

                    } else if (fixa && chkPaga != null && chkPaga.isSelected()) {
                        alpha = 255;
                        lblNome.setForeground(Color.WHITE);
                        lblValor.setForeground(Color.WHITE);

                    }

                    base = new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                    g2.dispose();
                }
            };

            painelConteudo.setOpaque(false);
            painelConteudo.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            painelConteudo.setPreferredSize(new Dimension(500, 50));
            painelConteudo.setMaximumSize(new Dimension(500, 50));

            

            // CheckBox para despesa fixa
            if (chkPaga != null) {
                painelConteudo.add(chkPaga);

                chkPaga.addActionListener(e -> {
                    Janela janela = (Janela) SwingUtilities.getWindowAncestor(ItemDespesa.this);
                    boolean novoEstado = chkPaga.isSelected();
                    janela.controller.marcarDespesaComoPaga(idDespesa, novoEstado);
                    repaint(); // apenas repinta a cor, não desativa nada
                    SwingUtilities.invokeLater(() -> janela.atualizarVista());
                });
            }


            // Labels
            lblNome = new JLabel(nome);
            lblNome.setFont(new Font("Arial", Font.BOLD, 20));
            lblNome.setForeground(Color.WHITE);

            lblValor = new JLabel(valor);
            lblValor.setFont(new Font("Arial", Font.PLAIN, 20));
            lblValor.setForeground(Color.WHITE);

            painelConteudo.add(lblNome);
            painelConteudo.add(lblValor);

            // Botões
            JButton btnEditar = new BotaoRedondoColorido("✏️", new Color(82, 113, 255));
            JButton btnRemover = new BotaoRedondoColorido("🗑️", new Color(255, 82, 82));

            btnEditar.addActionListener(e -> {
                String novoValorStr = JOptionPane.showInputDialog(this, "Novo valor para " + nome + ":");
                if (novoValorStr == null || novoValorStr.trim().isEmpty()) return;
                try {
                    double novoValor = Double.parseDouble(novoValorStr);
                    Janela janela = (Janela) SwingUtilities.getWindowAncestor(this);
                    Despesa despesaAtual = janela.controller.getRegistoAtual().getDespesas().get(idDespesa);

                    if (despesaAtual instanceof DespesaFixa) {
                        janela.controller.editarDespesaFixaFuturas(idDespesa, novoValor);
                    } else {
                        janela.controller.editarDespesa(idDespesa, novoValor);
                    }
                    janela.atualizarVista();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Valor inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            btnRemover.addActionListener(e -> {
                int opcao = JOptionPane.showConfirmDialog(this,
                        "Remover despesa \"" + nome + "\"?",
                        "Confirmação",
                        JOptionPane.YES_NO_OPTION);
                if (opcao == JOptionPane.YES_OPTION) {
                    Janela janela = (Janela) SwingUtilities.getWindowAncestor(this);
                    Despesa despesaAtual = janela.controller.getRegistoAtual().getDespesas().get(idDespesa);

                    // proteção contra null (evita crash)
                    if (despesaAtual == null) {
                        // tenta encontrar pela lista de despesas (fallback)
                        for (Despesa d : janela.controller.getRegistoAtual().getDespesas().values()) {
                            if (d.getNome().equalsIgnoreCase(nome) && d instanceof DespesaFixa) {
                                despesaAtual = d;
                                break;
                            }
                        }
                    }

                    if (despesaAtual == null) {
                        // informa o utilizador e aborta
                        JOptionPane.showMessageDialog(this, "Despesa não encontrada (id " + idDespesa + ").", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (despesaAtual instanceof DespesaFixa) {
                        janela.controller.removerDespesaFixaFuturas(despesaAtual.getIdDespesa());
                    } else {
                        janela.controller.removerDespesa(despesaAtual.getIdDespesa());
                    }
                    janela.atualizarVista();
                }
            });


            add(painelConteudo);
            add(btnEditar);
            add(btnRemover);
        }
        

    }


        // Botão colorido redondo
        private static class BotaoRedondoColorido extends JButton {
            private final Color corFundo;

            public BotaoRedondoColorido(String emoji, Color corFundo) {
                super(emoji);
                this.corFundo = corFundo;
                setPreferredSize(new Dimension(40, 40));
                setMargin(new Insets(0, 0, 0, 0));
                setFocusPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setBorderPainted(false);
                setFont(new Font("Arial", Font.BOLD, 18));
                setForeground(Color.WHITE);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(corFundo);
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {}
        }

    private static class ScrollBarCinzaArredondada extends BasicScrollBarUI {
        private final int arc = 20;

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(180, 180, 180); // cor cinzenta
            trackColor = new Color(230, 230, 230); // fundo claro
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, arc, arc);

            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, arc, arc);
            g2.dispose();
        }

        // Remove os botões ↑ ↓
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }
    }
}



