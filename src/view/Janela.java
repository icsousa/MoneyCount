package view;

import controller.MoneyCountController;
import model.Despesa;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.awt.geom.Path2D;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;

public class Janela extends JFrame {
    private MoneyCountController controller;
    private JTable tabelaDespesas;
    private JLabel lblMontante, lblSaldo, lblDespesas;
    private JLabel lblData;
    private JPanel listaDespesas;
    private CaixaTituloValor caixaSalario, caixaSaldo, caixaDespesas, caixaPoupanca;



    private void atualizarVista() {
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

        // Atualiza a lista de ItemDespesa na esquerda
        listaDespesas.removeAll();
        for (Despesa despesa : controller.getDespesasMesAtual()) {
            String nome = despesa.getNome();
            String valor = String.format("%.2f €", despesa.getMontante());
            listaDespesas.add(new ItemDespesa(nome, valor));
        }
        listaDespesas.revalidate();
        listaDespesas.repaint();
    }


    private void atualizarTabelaDespesas() {
        DefaultTableModel model = (DefaultTableModel) tabelaDespesas.getModel();
        model.setRowCount(0); // Limpa todas as linhas

        for (Despesa despesa : controller.getDespesasMesAtual()) {
            model.addRow(new Object[]{despesa.getNome(), String.format("%.2f €", despesa.getMontante())});
        }
    }


    public Janela(MoneyCountController controller) {
        this.controller = controller;
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

        

        // PAINEL ESQUERDO
        Box painelEsquerdo = Box.createVerticalBox();
        painelEsquerdo.setBackground(Color.LIGHT_GRAY);

        // Gráfico falso (para já)
        int alturaJanela = Toolkit.getDefaultToolkit().getScreenSize().height;
        int alturaGrafico = (int) (alturaJanela * 0.55);

        JPanel painelGrafico = new JPanel();
        painelGrafico.setPreferredSize(new Dimension(600, alturaGrafico));
        painelGrafico.setMaximumSize(new Dimension(Integer.MAX_VALUE, alturaGrafico));
        painelGrafico.setBackground(Color.WHITE); // Placeholder, trocar por gráfico real
        painelGrafico.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        painelEsquerdo.add(painelGrafico);

        // Painel horizontal para o título "Despesas" + botões
        // Cria o painel horizontal
        JPanel painelTituloComBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        painelTituloComBotoes.setOpaque(false);

        // Adiciona o título "Despesas"
        painelTituloComBotoes.add(new CaixaTituloValor("Despesas"));

        // Cria os botões
        JButton btnDespesa = new BotaoEstiloTitulo("+ Despesa", new Color(126, 217, 87)); // Verde
        JButton btnDespesaFixa = new BotaoEstiloTitulo("+ Despesa Fixa", new Color(203, 108, 230)); // Roxo

        // Adiciona os botões
        painelTituloComBotoes.add(btnDespesa);
        painelTituloComBotoes.add(btnDespesaFixa);

        // Adiciona o painel à esquerda (painelEsquerdo)
        painelEsquerdo.add(Box.createVerticalStrut(10));
        painelEsquerdo.add(painelTituloComBotoes);
        painelEsquerdo.add(Box.createVerticalStrut(10));


        // Lista de despesas com scroll
        listaDespesas = new JPanel();
        listaDespesas.setLayout(new BoxLayout(listaDespesas, BoxLayout.Y_AXIS));
        
        List<Despesa> despesas = controller.getDespesasMesAtual();

        for (Despesa despesa : despesas) {
            String nome = despesa.getNome();
            String valor = String.format("%.2f €", despesa.getMontante());
            listaDespesas.add(new ItemDespesa(nome, valor));
        }


        JScrollPane scrollDespesas = new JScrollPane(listaDespesas);
        scrollDespesas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollDespesas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDespesas.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
        scrollDespesas.setPreferredSize(new Dimension(300, 300));
        scrollDespesas.getVerticalScrollBar().setUI(new ScrollBarCinzaArredondada());

        painelEsquerdo.add(scrollDespesas);


        // PAINEL DIREITO — Resumo
        String rendimento = String.format("%.2f €", controller.getRendimentoAtual());
        String saldo = String.format("%.2f €", controller.getSaldoAtual());
        String totalDespesas = String.format("%.2f €", controller.getTotalDespesas());
        String poupanca = String.format("%.2f €", controller.getPoupancaAcumulada());


        JPanel painelDireito = new JPanel(new GridBagLayout());
        painelDireito.setBackground(Color.WHITE);

        // Novo painel com os blocos de montantes
        JPanel painelMontantes = new JPanel(new GridBagLayout());
        painelMontantes.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Montante no topo, centralizado (linha 0, col 0 e colSpan 2)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        caixaSalario = new CaixaTituloValor("Salário", rendimento);
        painelMontantes.add(caixaSalario, gbc);

        // Saldo à esquerda (linha 1, col 0)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        caixaSaldo = new CaixaTituloValor("Saldo Disp.", saldo);
        painelMontantes.add(caixaSaldo, gbc);

        // Despesas à direita (linha 1, col 1)
        gbc.gridx = 1;
        gbc.gridy = 1;
        caixaDespesas = new CaixaTituloValor("Despesas", totalDespesas);
        painelMontantes.add(caixaDespesas, gbc);

        // Poupança em baixo, centralizado (linha 2, col 0 e colSpan 2)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        caixaPoupanca = new CaixaTituloValor("Poupança", poupanca);
        painelMontantes.add(caixaPoupanca, gbc);

        painelDireito.add(painelMontantes, gbc);

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
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 90, 90);
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
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 62));

            fundoTitulo.add(lblTitulo);

            // Só adiciona o botão se for "Salário"
            if (titulo.equalsIgnoreCase("Salário")) {
                JButton btnEditar = new BotaoRedondoAzul("✏️");
                btnEditar.setToolTipText("Editar Montante");
                fundoTitulo.add(btnEditar);
            }

            lblValor = new JLabel(valor);
            lblValor.setFont(new Font("Arial", Font.PLAIN, 46));
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
                setPreferredSize(new Dimension(55, 55));
                setMargin(new Insets(0, 0, 0, 0)); 
                setFocusPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setBorderPainted(false);
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 42));
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
            setFont(new Font("Arial", Font.BOLD, 36));
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
            int height = 70; // Altura semelhante à do título
            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(corFundo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 75, 75);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            // Sem borda
        }
    }

    private static class ItemDespesa extends JPanel {
        public ItemDespesa(String nome, String valor) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            setOpaque(false);

            // Painel colorido arredondado com nome e valor
            JPanel painelConteudo = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(gerarCorParaNome(nome));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                    g2.dispose();
                }
            };
            painelConteudo.setOpaque(false);
            painelConteudo.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
            painelConteudo.setPreferredSize(new Dimension(500, 50)); // Tamanho fixo do retângulo
            painelConteudo.setMaximumSize(new Dimension(500, 50));

            JLabel lblNome = new JLabel(nome);
            lblNome.setFont(new Font("Arial", Font.BOLD, 20));
            lblNome.setForeground(Color.WHITE);

            JLabel lblValor = new JLabel(valor);
            lblValor.setFont(new Font("Arial", Font.PLAIN, 20));
            lblValor.setForeground(Color.WHITE);

            painelConteudo.add(lblNome);
            painelConteudo.add(lblValor);

            JButton btnEditar = new BotaoRedondoColorido("✏️", new Color(82, 113, 255));
            JButton btnRemover = new BotaoRedondoColorido("🗑️", new Color(255, 82, 82));

            add(painelConteudo);
            add(btnEditar);
            add(btnRemover);
        }

        // Gera uma cor baseada no hash do nome (consistente para cada nome)
        private Color gerarCorParaNome(String nome) {
            int hash = nome.hashCode();
            int r = 100 + Math.abs(hash) % 100;
            int g = 100 + Math.abs(hash / 100) % 100;
            int b = 100 + Math.abs(hash / 10000) % 100;
            return new Color(r, g, b);
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
