package view;

import controller.MoneyCountController;
import model.Despesa;
import model.DespesaFixa;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class Janela extends JFrame {
    private MoneyCountController controller;
    private JLabel lblData;
    private JPanel listaDespesas;
    private CaixaTituloValor caixaSalario, caixaSaldo, caixaDespesas, caixaPoupanca;
    private JFXPanel fxPanel;
    private final int alturaGrafico = 300;

    public Janela(MoneyCountController controller) {
        this.controller = controller;
        setUIFont(new FontUIResource("Arial", Font.PLAIN, 14));
        
        configurarJanelaBase();
        inicializarComponentes();
        configurarHotkeys();
        
        setVisible(true);
    }

    private void configurarJanelaBase() {
        setTitle("MoneyCount");
        try {
            String caminhoIcone = "resources/LOGO.png"; 
            Image icon = new ImageIcon(caminhoIcone).getImage();
            if (icon != null) {
                setIconImage(icon);
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar o ícone: " + e.getMessage());
        }
        Toolkit kit = Toolkit.getDefaultToolkit();  
        Dimension tamTela = kit.getScreenSize();  
        setSize(tamTela.width, tamTela.height);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        add(criarPainelTopo(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2);
        splitPane.setEnabled(false); // Impede o utilizador de arrastar
        splitPane.setDividerSize(0); 

        splitPane.setLeftComponent(criarPainelEsquerdo());
        splitPane.setRightComponent(criarPainelDireito());

        add(splitPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // MÉTODOS DE CRIAÇÃO DE PAINÉIS (UI)
    // =========================================================================

    private JPanel criarPainelTopo() {
        JPanel painelMes = new PainelComCantosInferioresArredondados();
        painelMes.setLayout(new BorderLayout());
        painelMes.setBackground(new Color(0x54, 0x54, 0x54));

        BotaoRedondo btnAnterior = new BotaoRedondo("←");
        BotaoRedondo btnSeguinte = new BotaoRedondo("→");

        btnSeguinte.addActionListener(e -> { controller.avancarMes(); atualizarVista(); });
        btnAnterior.addActionListener(e -> { controller.retrocederMes(); atualizarVista(); });
    
        JPanel painelEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        painelEsquerda.setOpaque(false);
        painelEsquerda.add(btnAnterior);

        JPanel painelDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        painelDireita.setOpaque(false);
        painelDireita.add(btnSeguinte);

        lblData = new JLabel(controller.getDataModeloString(), SwingConstants.CENTER);
        lblData.setForeground(Color.WHITE);
        lblData.setFont(new Font("Arial", Font.BOLD, 22));

        painelMes.add(painelEsquerda, BorderLayout.WEST);
        painelMes.add(lblData, BorderLayout.CENTER);
        painelMes.add(painelDireita, BorderLayout.EAST);

        return painelMes;
    }

    private Component criarPainelEsquerdo() {
        Box painelEsquerdo = Box.createVerticalBox();
        painelEsquerdo.setBackground(Color.WHITE);
        painelEsquerdo.setOpaque(true);

        fxPanel = new JFXPanel();
        fxPanel.setPreferredSize(new Dimension(600, alturaGrafico));
        fxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));
        painelEsquerdo.add(fxPanel);
        atualizarGrafico(fxPanel);

        // Painel Resumo
        JPanel painelResumo = new JPanel(new GridBagLayout());
        painelResumo.setBackground(Color.WHITE);
        painelResumo.setOpaque(true);
        painelResumo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        caixaSalario = new CaixaTituloValor("Salário", String.format("%.2f €", controller.getRendimentoAtual()));
        caixaSaldo = new CaixaTituloValor("Saldo Disp.", String.format("%.2f €", controller.getSaldoAtual()));
        caixaDespesas = new CaixaTituloValor("Despesas", String.format("%.2f €", controller.getTotalDespesas()));
        caixaPoupanca = new CaixaTituloValor("Poupança", String.format("%.2f €", controller.getPoupancaAcumulada()));

        gbc.gridx = 0; gbc.gridy = 0; painelResumo.add(caixaSalario, gbc);
        gbc.gridx = 1; gbc.gridy = 0; painelResumo.add(caixaDespesas, gbc);
        gbc.gridx = 0; gbc.gridy = 1; painelResumo.add(caixaPoupanca, gbc);
        gbc.gridx = 1; gbc.gridy = 1; painelResumo.add(caixaSaldo, gbc);

        painelEsquerdo.add(Box.createVerticalStrut(10));
        painelEsquerdo.add(painelResumo);

        return painelEsquerdo;
    }

    private JPanel criarPainelDireito() {
        JPanel painelDireito = new JPanel(new BorderLayout());
        painelDireito.setBackground(new Color(238, 238, 238));
        painelDireito.setOpaque(true);
        painelDireito.setPreferredSize(new Dimension(400, Integer.MAX_VALUE));

        JPanel painelTituloComBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        painelTituloComBotoes.setOpaque(false);

        JButton btnDespesa = new BotaoEstiloTitulo("+ Despesa", new Color(174, 239, 164));
        JButton btnDespesaFixa = new BotaoEstiloTitulo("+ Despesa Fixa", new Color(220, 167, 235));

        btnDespesa.addActionListener(e -> abrirDialogoNovaDespesa(false));
        btnDespesaFixa.addActionListener(e -> abrirDialogoNovaDespesa(true));

        painelTituloComBotoes.add(btnDespesa);
        painelTituloComBotoes.add(btnDespesaFixa);
        painelDireito.add(painelTituloComBotoes, BorderLayout.NORTH);

        listaDespesas = new JPanel(new GridBagLayout());
        listaDespesas.setBackground(new Color(238, 238, 238));
        listaDespesas.setOpaque(true);

        preencherListaDespesas();

        JScrollPane scrollDespesas = new JScrollPane(listaDespesas);
        scrollDespesas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollDespesas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDespesas.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
        scrollDespesas.getVerticalScrollBar().setUI(new ScrollBarCinzaArredondada());
        
        painelDireito.add(scrollDespesas, BorderLayout.CENTER);

        return painelDireito;
    }

    // =========================================================================
    // LÓGICA DE ATUALIZAÇÃO E UTILITÁRIOS
    // =========================================================================

    public void atualizarVista() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.of("pt", "PT"));
        lblData.setText(controller.getDataModelo().format(formatter));

        caixaSalario.setValor(String.format("%.2f €", controller.getRendimentoAtual()));
        caixaSaldo.setValor(String.format("%.2f €", controller.getSaldoAtual()));
        caixaDespesas.setValor(String.format("%.2f €", controller.getTotalDespesas()));
        caixaPoupanca.setValor(String.format("%.2f €", controller.getPoupancaAcumulada()));

        preencherListaDespesas();
        atualizarGrafico(fxPanel);
    }

    private void preencherListaDespesas() {
        listaDespesas.removeAll();
        GridBagConstraints gbcList = new GridBagConstraints();
        gbcList.gridx = 0; gbcList.gridy = 0;
        gbcList.fill = GridBagConstraints.HORIZONTAL;
        gbcList.anchor = GridBagConstraints.NORTH;
        gbcList.insets = new Insets(2, 2, 2, 2);

        for (Despesa despesa : getDespesasOrdenadas()) {
            boolean fixa = despesa instanceof DespesaFixa;
            boolean paga = fixa && ((DespesaFixa) despesa).isPago();
            listaDespesas.add(new ItemDespesa(despesa.getIdDespesa(), despesa.getNome(), 
                              String.format("%.2f €", despesa.getMontante()), fixa, paga), gbcList);
            gbcList.gridy++;
        }

        gbcList.weighty = 1.0;
        listaDespesas.add(Box.createVerticalGlue(), gbcList);

        listaDespesas.revalidate();
        listaDespesas.repaint();
        SwingUtilities.invokeLater(() -> listaDespesas.updateUI());
    }

    private List<Despesa> getDespesasOrdenadas() {
        List<Despesa> originais = controller.getDespesasMesAtual();
        if (originais == null) return new ArrayList<>(); // Proteção contra null
        
        List<Despesa> despesas = new ArrayList<>(originais);
        despesas.sort((d1, d2) -> {
            boolean d1Fixa = d1 instanceof DespesaFixa;
            boolean d2Fixa = d2 instanceof DespesaFixa;
            
            if (d1Fixa && !d2Fixa) return -1;
            if (!d1Fixa && d2Fixa) return 1;
            
            return Integer.compare(d2.getIdDespesa(), d1.getIdDespesa());
        });
        return despesas;
    }

    // Método auxiliar para criar janelas de input personalizadas, modernas e sem barra superior
    private String mostrarDialogoInputSemBorda(String mensagem) {
        JDialog dialog = new JDialog(this, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        
        try {
            // Tenta tornar a janela do sistema transparente
            dialog.setBackground(new Color(0, 0, 0, 0)); 
        } catch (Exception e) {
            // Ignora se o sistema operativo não suportar
        }

        // Usa o nosso novo painel que projeta a sombra
        PainelSombra painelSombra = new PainelSombra();

        // O conteúdo interno agora é transparente (setOpaque(false)) para vermos o fundo desenhado pela sombra
        JPanel painelConteudo = new JPanel(new BorderLayout(10, 15));
        painelConteudo.setOpaque(false); 
        painelConteudo.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblMensagem = new JLabel(mensagem);
        lblMensagem.setFont(new Font("Arial", Font.BOLD, 16));

        JTextField campoTexto = new JTextField(15);
        campoTexto.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JPanel painelCentro = new JPanel(new BorderLayout(5, 10));
        painelCentro.setOpaque(false);
        painelCentro.add(lblMensagem, BorderLayout.NORTH);
        painelCentro.add(campoTexto, BorderLayout.CENTER);

        JButton btnOk = new JButton("OK");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnOk.setBackground(new Color(82, 113, 255));
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false);
        
        btnCancelar.setBackground(new Color(220, 220, 220));
        btnCancelar.setFocusPainted(false);

        final String[] resultado = {null};

        btnOk.addActionListener(e -> {
            resultado[0] = campoTexto.getText();
            dialog.dispose();
        });

        btnCancelar.addActionListener(e -> {
            resultado[0] = null;
            dialog.dispose();
        });

        campoTexto.addActionListener(e -> btnOk.doClick());

        dialog.getRootPane().setDefaultButton(btnOk);
        dialog.getRootPane().registerKeyboardAction(
            e -> btnCancelar.doClick(),
            KeyStroke.getKeyStroke("ESCAPE"), 
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotoes.setOpaque(false);
        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnOk);

        painelConteudo.add(painelCentro, BorderLayout.CENTER);
        painelConteudo.add(painelBotoes, BorderLayout.SOUTH);

        painelSombra.add(painelConteudo, BorderLayout.CENTER);

        dialog.add(painelSombra);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        dialog.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                campoTexto.requestFocusInWindow();
            }
        });

        dialog.setVisible(true);
        return resultado[0];
    }

    public boolean mostrarDialogoConfirmacaoSemBorda(String mensagem) {
        JDialog dialog = new JDialog(this, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        try {
            dialog.setBackground(new Color(0, 0, 0, 0)); // Fundo transparente
        } catch (Exception e) {}

        PainelSombra painelSombra = new PainelSombra();

        JPanel painelConteudo = new JPanel(new BorderLayout(10, 20));
        painelConteudo.setOpaque(false);
        painelConteudo.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblMensagem = new JLabel(mensagem);
        lblMensagem.setFont(new Font("Arial", Font.BOLD, 16));
        lblMensagem.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel painelCentro = new JPanel(new BorderLayout());
        painelCentro.setOpaque(false);
        painelCentro.add(lblMensagem, BorderLayout.CENTER);

        JButton btnRemover = new JButton("Remover");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnRemover.setBackground(new Color(255, 82, 82)); 
        btnRemover.setForeground(Color.WHITE);
        btnRemover.setFocusPainted(false);
        
        btnCancelar.setBackground(new Color(220, 220, 220));
        btnCancelar.setFocusPainted(false);

        final boolean[] resultado = {false};

        btnRemover.addActionListener(e -> {
            resultado[0] = true;
            dialog.dispose();
        });

        btnCancelar.addActionListener(e -> {
            resultado[0] = false;
            dialog.dispose();
        });

        dialog.getRootPane().setDefaultButton(btnRemover);
        dialog.getRootPane().registerKeyboardAction(
            e -> btnCancelar.doClick(),
            KeyStroke.getKeyStroke("ESCAPE"), 
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        painelBotoes.setOpaque(false);
        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnRemover);

        painelConteudo.add(painelCentro, BorderLayout.CENTER);
        painelConteudo.add(painelBotoes, BorderLayout.SOUTH);

        painelSombra.add(painelConteudo, BorderLayout.CENTER);

        dialog.add(painelSombra);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        
        dialog.setVisible(true);
        return resultado[0];
    }

    private void abrirDialogoNovaDespesa(boolean eFixa) {
        String tipo = eFixa ? "fixa" : "";
        String nome = mostrarDialogoInputSemBorda("Nome da despesa " + tipo + ":");
        if (nome == null || nome.trim().isEmpty()) return;

        String valorStr = mostrarDialogoInputSemBorda("Valor da despesa (€):");
        if (valorStr == null || valorStr.trim().isEmpty()) return;

        try {
            double valor = Double.parseDouble(valorStr.replace(",", "."));
            controller.adicionarDespesa(nome, valor, eFixa);
            atualizarVista();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarGrafico(JFXPanel fxPanel) {
        List<Despesa> despesas = controller.getDespesasMesAtual();
        if (despesas == null) return;

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
            chart.setPrefSize(600, 400);
            chart.setStyle("-fx-background-color: white;");

            Scene scene = new Scene(chart);
            scene.setFill(javafx.scene.paint.Color.WHITE);
            fxPanel.setScene(scene);

            Platform.runLater(() -> {
                double total = somaPorNome.values().stream().mapToDouble(Double::doubleValue).sum();
                for (PieChart.Data data : chart.getData()) {
                    final String nome = data.getName();
                    final double valor = data.getPieValue();
                    final double percentagem = total == 0 ? 0 : (valor / total) * 100.0;
                    final java.awt.Color awtColor = coresPorNome.get(nome);
                    final String rgb = awtColor == null ? null : String.format("%d, %d, %d", awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            if (rgb != null) newNode.setStyle("-fx-pie-color: rgb(" + rgb + ");");
                            Tooltip.install(newNode, new Tooltip(String.format("%s: %.2f € (%.1f%%)", nome, valor, percentagem)));
                        }
                    });

                    if (data.getNode() != null) {
                        if (rgb != null) data.getNode().setStyle("-fx-pie-color: rgb(" + rgb + ");");
                        Tooltip.install(data.getNode(), new Tooltip(String.format("%s: %.2f € (%.1f%%)", nome, valor, percentagem)));
                    }
                }
            });
        });
    }

    public Color gerarCorParaNome(String nome) {
        int hash = nome.hashCode();
        return new Color(100 + Math.abs(hash) % 100, 100 + Math.abs(hash / 100) % 100, 100 + Math.abs(hash / 10000) % 100);
    }

    private void configurarHotkeys() {
        JRootPane rootPane = this.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "avancarMes");
        actionMap.put("avancarMes", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { controller.avancarMes(); atualizarVista(); }
        });

        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "retrocederMes");
        actionMap.put("retrocederMes", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { controller.retrocederMes(); atualizarVista(); }
        });
    }

    private static void setUIFont(FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) UIManager.put(key, f);
        }
    }

    // =========================================================================
    // CLASSES INTERNAS (COMPONENTES VISUAIS CUSTOMIZADOS)
    // =========================================================================

    private static class BotaoRedondo extends JButton {
        public BotaoRedondo(String texto) {
            super(texto);
            setPreferredSize(new Dimension(35, 35));
            setMargin(new Insets(0, 0, 0, 0)); 
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 20));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x3A, 0x3A, 0x3A));
            g2.fillOval(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
            g2.dispose();
        }
    }

    private static class PainelComCantosInferioresArredondados extends JPanel {
        public PainelComCantosInferioresArredondados() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Path2D path = new Path2D.Float();
            path.moveTo(0, 0); path.lineTo(0, getHeight()); path.lineTo(getWidth(), getHeight()); path.lineTo(getWidth(), 0); path.closePath();
            g2.setColor(new Color(0x54, 0x54, 0x54));
            g2.fill(path);
            g2.dispose();
        }
    }

    private static class CaixaTituloValor extends JPanel {
        private JLabel lblValor;

        public CaixaTituloValor(String titulo, String valor) {
            setLayout(new BorderLayout());
            setOpaque(false);

            JPanel fundoTitulo = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(220, 220, 220));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 70, 70);
                    g2.dispose();
                }
            };
            fundoTitulo.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
            fundoTitulo.setOpaque(false);

            JLabel lblTitulo = new JLabel(titulo);
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 42));
            fundoTitulo.add(lblTitulo);

            if (titulo.equalsIgnoreCase("Salário")) {
                JButton btnEditar = new BotaoRedondoAzul("✏️");
                fundoTitulo.add(btnEditar);
                btnEditar.addActionListener(e -> {
                    Janela j = (Janela) SwingUtilities.getWindowAncestor(this);
                    String novoStr = j.mostrarDialogoInputSemBorda("Novo salário (€):");
                    if (novoStr == null || novoStr.trim().isEmpty()) return;
                    try {
                        j.controller.atualizarRendimento(Double.parseDouble(novoStr.replace(",", ".")));
                        j.atualizarVista();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Valor inválido!");
                    }
                });
            }

            lblValor = new JLabel(valor);
            lblValor.setFont(new Font("Arial", Font.PLAIN, 30));
            JPanel valorPanel = new JPanel(new BorderLayout());
            valorPanel.setOpaque(false);
            valorPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            valorPanel.add(lblValor, BorderLayout.CENTER);

            add(valorPanel, BorderLayout.SOUTH);
            add(fundoTitulo, BorderLayout.CENTER);
        }

        public void setValor(String novoValor) { lblValor.setText(novoValor); }

        private static class BotaoRedondoAzul extends JButton {
            public BotaoRedondoAzul(String texto) {
                super(texto);
                setPreferredSize(new Dimension(45, 45));
                setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
                setForeground(Color.WHITE); setFont(new Font("SansSerif", Font.PLAIN, 12));
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(82,113,255)); 
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        }
    }

    private static class BotaoEstiloTitulo extends JButton {
        private final Color corFundo;
        public BotaoEstiloTitulo(String texto, Color corFundo) {
            super(texto); this.corFundo = corFundo;
            setFont(new Font("Arial", Font.BOLD, 26)); setForeground(new Color(84, 84, 84));
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setMargin(new Insets(10, 20, 10, 20));
        }
        @Override public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + 50, 50);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(corFundo); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
            super.paintComponent(g); g2.dispose();
        }
    }

    private static class ItemDespesa extends JPanel {
        public ItemDespesa(int id, String nome, String valor, boolean fixa, boolean paga) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            setOpaque(false);

            JCheckBox chkPaga = fixa ? new JCheckBox() : null;
            if (chkPaga != null) { chkPaga.setSelected(paga); chkPaga.setOpaque(false); }

            JLabel lblNome = new JLabel(nome);
            lblNome.setFont(new Font("Arial", Font.BOLD, 20)); lblNome.setForeground(Color.WHITE);

            JLabel lblValor = new JLabel(valor);
            lblValor.setFont(new Font("Arial", Font.PLAIN, 20)); lblValor.setForeground(Color.WHITE);

            JPanel painelConteudo = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    Janela j = (Janela) SwingUtilities.getWindowAncestor(this);
                    Color base = j != null ? j.gerarCorParaNome(nome) : Color.GRAY;
                    
                    if (fixa && chkPaga != null && !chkPaga.isSelected()) {
                        base = new Color(base.getRed(), base.getGreen(), base.getBlue(), 100);
                        lblNome.setForeground(new Color(90, 90, 90)); lblValor.setForeground(new Color(90, 90, 90));
                    } else {
                        lblNome.setForeground(Color.WHITE); lblValor.setForeground(Color.WHITE);
                    }
                    g2.setColor(base); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50); g2.dispose();
                }
            };
            painelConteudo.setOpaque(false); painelConteudo.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            painelConteudo.setPreferredSize(new Dimension(500, 50));

            if (chkPaga != null) {
                painelConteudo.add(chkPaga);
                chkPaga.addActionListener(e -> {
                    Janela j = (Janela) SwingUtilities.getWindowAncestor(this);
                    j.controller.marcarDespesaComoPaga(id, chkPaga.isSelected());
                    SwingUtilities.invokeLater(() -> j.atualizarVista());
                });
            }
            painelConteudo.add(lblNome); painelConteudo.add(lblValor);

            JButton btnEditar = new BotaoRedondoColorido("✏️", new Color(82, 113, 255));
            JButton btnRemover = new BotaoRedondoColorido("🗑️", new Color(255, 82, 82));

            btnEditar.addActionListener(e -> {
                Janela j = (Janela) SwingUtilities.getWindowAncestor(this);
                Color corDaDespesa = j.gerarCorParaNome(nome);
                String hexCor = String.format("#%02x%02x%02x", corDaDespesa.getRed(), corDaDespesa.getGreen(), corDaDespesa.getBlue());
                String msg = "<html>Novo valor para <b><font color='" + hexCor + "'>" + nome + "</font></b>:</html>";
                String novoVal = j.mostrarDialogoInputSemBorda(msg);
                if (novoVal == null || novoVal.trim().isEmpty()) return;
                try {
                    if (fixa) j.controller.editarDespesaFixaFuturas(id, Double.parseDouble(novoVal.replace(",", ".")));
                    else j.controller.editarDespesa(id, Double.parseDouble(novoVal.replace(",", ".")));
                    j.atualizarVista();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro ao editar!"); }
            });

            btnRemover.addActionListener(e -> {
                Janela j = (Janela) SwingUtilities.getWindowAncestor(this);
                Color corDaDespesa = j.gerarCorParaNome(nome);
                String hexCor = String.format("#%02x%02x%02x", corDaDespesa.getRed(), corDaDespesa.getGreen(), corDaDespesa.getBlue());
                String msg = "<html>Tem a certeza que deseja remover a despesa <b><font color='" + hexCor + "'>" + nome + "</font></b>?</html>";
                boolean confirmar = j.mostrarDialogoConfirmacaoSemBorda(msg);
                if (confirmar) {
                    if (fixa) {
                        j.controller.removerDespesaFixaFuturas(id);
                    } else {
                        j.controller.removerDespesa(id);
                    }
                    j.atualizarVista();
                }
            });

            add(painelConteudo); add(btnEditar); add(btnRemover);
        }

        private static class BotaoRedondoColorido extends JButton {
            private final Color corFundo;
            public BotaoRedondoColorido(String emoji, Color corFundo) {
                super(emoji); this.corFundo = corFundo;
                setPreferredSize(new Dimension(45, 45)); setFocusPainted(false); setContentAreaFilled(false);
                setBorderPainted(false); setForeground(Color.WHITE); setFont(new Font("SansSerif", Font.PLAIN, 12));
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(corFundo); g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g); g2.dispose();
            }
        }
    }

    private static class ScrollBarCinzaArredondada extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(180, 180, 180); trackColor = new Color(230, 230, 230);
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor); g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20); g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(trackColor);
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20); g2.dispose();
        }
        @Override protected JButton createDecreaseButton(int o) { JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        @Override protected JButton createIncreaseButton(int o) { JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
    }

    // Painel personalizado que desenha uma sombra esfumada à volta do fundo branco
    private static class PainelSombra extends JPanel {
        private final int tamanhoSombra = 5; // Espessura da sombra

        public PainelSombra() {
            setOpaque(false); // O painel base tem de ser transparente
            setLayout(new BorderLayout());
            // Margem para a sombra não ser cortada pelos limites da janela
            setBorder(BorderFactory.createEmptyBorder(tamanhoSombra, tamanhoSombra, tamanhoSombra, tamanhoSombra));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Desenha várias camadas sobrepostas para criar o "blur" da sombra
            for (int i = 0; i < tamanhoSombra; i++) {
                float opacidade = 0.05f * (1.0f - ((float) i / tamanhoSombra));
                g2.setColor(new Color(0, 0, 0, opacidade)); // Preto com transparência
                g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 12, 12);
            }

            // 2. Desenha o fundo branco principal por cima da sombra
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(tamanhoSombra, tamanhoSombra, getWidth() - tamanhoSombra * 2, getHeight() - tamanhoSombra * 2, 8, 8);
            
            g2.dispose();
        }
    }
}