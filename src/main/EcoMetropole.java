package main;

import main.engine.GerenciadorJogo;
import main.model.Jogador;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.RenderingHints;

/**
 * Classe EcoMetropole — classe principal do projeto e ponto de entrada.
 *
 * Esta classe é responsável por três funções fundamentais:
 * 1. Criar e configurar a janela do jogo (JFrame e JPanel)
 * 2. Implementar o game loop que mantém o jogo rodando a 60 FPS
 * 3. Capturar e processar eventos de teclado e mouse do jogador
 *
 * A classe estende JPanel para servir como superfície de renderização
 * personalizada, sobrescrevendo o método paintComponent para desenhar
 * todos os elementos do jogo usando Graphics2D.
 *
 * A classe implementa a interface Runnable para que o game loop possa
 * ser executado em uma thread dedicada, separada da thread principal
 * do Swing (EDT — Event Dispatch Thread), evitando que o jogo trave
 * a interface gráfica durante a execução.
 *
 * CONCEITOS DE POO APLICADOS:
 * - Herança: estende JPanel da biblioteca Swing
 * - Polimorfismo: sobrescreve paintComponent() e addNotify()
 * - Encapsulamento: atributos privados do game loop
 * - Interface: implementa Runnable para execução em thread
 */
public class EcoMetropole extends JPanel implements Runnable {

    /**
     * Taxa de quadros por segundo alvo do game loop.
     * Define quantas vezes por segundo o jogo é atualizado e redesenhado.
     */
    private static final int FPS = 60;

    /**
     * Intervalo de tempo em milissegundos entre cada quadro.
     * Calculado como 1000ms / 60 = ~16.67ms por quadro.
     */
    private static final int FPS_DELAY = 1000 / FPS;

    /**
     * Referência ao gerenciador de jogo, responsável por toda a lógica.
     * Segue o princípio de separação de responsabilidades — EcoMetropole
     * cuida da janela e do loop, GerenciadorJogo cuida da lógica do jogo.
     */
    private GerenciadorJogo gerenciador;

    /**
     * Thread dedicada ao game loop.
     * Separada da EDT do Swing para não bloquear a interface gráfica.
     */
    private Thread gameThread;

    /**
     * Flag de controle do game loop.
     * Quando false, o loop é encerrado e a thread termina.
     * Declarada como volatile para garantir visibilidade entre threads —
     * sem volatile, a thread do game loop poderia não enxergar a mudança
     * realizada pela thread principal.
     */
    private volatile boolean running = true;

    /**
     * Construtor da classe EcoMetropole.
     *
     * Inicializa o gerenciador de jogo, configura o painel gráfico
     * e registra os listeners de teclado e mouse.
     *
     * setFocusable(true) é essencial para que o painel receba
     * eventos de teclado — sem isso, as teclas WASD não funcionam.
     */
    public EcoMetropole() {
        gerenciador = new GerenciadorJogo();
        setFocusable(true);
        setPreferredSize(new Dimension(1000, 700));
        setBackground(Color.decode("#1a3c1a"));

        // KeyAdapter para captura de eventos de teclado
        // Atualiza as flags de direção do jogador a cada tecla
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Jogador p = gerenciador.getJogador();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> p.setUp(true);
                    case KeyEvent.VK_S -> p.setDown(true);
                    case KeyEvent.VK_A -> p.setLeft(true);
                    case KeyEvent.VK_D -> p.setRight(true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                Jogador p = gerenciador.getJogador();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> p.setUp(false);
                    case KeyEvent.VK_S -> p.setDown(false);
                    case KeyEvent.VK_A -> p.setLeft(false);
                    case KeyEvent.VK_D -> p.setRight(false);
                }
            }
        });

        // MouseAdapter para captura de cliques nos botões da tela de fim
        // requestFocusInWindow() garante que o painel mantenha o foco
        // após o clique, permitindo que o teclado continue funcionando
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                gerenciador.clicar(e.getX(), e.getY(),
                        getWidth(), getHeight());
                requestFocusInWindow();
            }
        });
    }

    /**
     * Inicia o game loop quando o painel é adicionado à janela.
     *
     * Sobrescreve o método addNotify() do JPanel, que é chamado
     * automaticamente pelo Swing quando o painel se torna visível.
     * Garante que o game loop só inicie após o painel estar pronto.
     * A verificação isAlive() evita múltiplas threads simultâneas.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    /**
     * Implementação do game loop — coração do jogo.
     *
     * Executado pela thread dedicada do jogo a 60 quadros por segundo.
     * A cada iteração:
     * 1. Registra o tempo de início do quadro
     * 2. Chama atualizar() para processar a lógica do jogo
     * 3. Chama repaint() para solicitar o redesenho da tela
     * 4. Calcula o tempo gasto e dorme pelo tempo restante
     *
     * O sleep garante que o jogo rode a velocidade consistente
     * independentemente do hardware — em máquinas mais rápidas,
     * o loop dorme mais; em máquinas mais lentas, dorme menos.
     *
     * InterruptedException é tratada restaurando o estado de
     * interrupção da thread e encerrando o loop de forma segura.
     */
    @Override
    public void run() {
        while (running) {
            long start = System.currentTimeMillis();

            // Atualiza a lógica do jogo
            atualizar();

            // Solicita o redesenho do painel
            repaint();

            // Calcula quanto tempo restou do quadro e dorme
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed < FPS_DELAY) {
                try {
                    Thread.sleep(FPS_DELAY - elapsed);
                } catch (InterruptedException e) {
                    // Restaura o estado de interrupção da thread
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
        }
    }

    /**
     * Atualiza o estado do jogo a cada quadro.
     *
     * Delega a atualização para o GerenciadorJogo e em seguida
     * ordena as entidades por posição vertical para garantir
     * que entidades mais abaixo na tela sejam desenhadas por cima
     * das entidades mais acima, criando a ilusão de profundidade.
     */
    private void atualizar() {
        gerenciador.atualizar();
        gerenciador.ordenarEntidadesPorY();
    }

    /**
     * Renderiza todos os elementos visuais do jogo na tela.
     *
     * Sobrescreve o método paintComponent() do JPanel para desenhar
     * o jogo usando Graphics2D com antialiasing habilitado para
     * bordas suaves. Delega o desenho ao GerenciadorJogo, passando
     * o contexto gráfico e as dimensões atuais do painel.
     *
     * Este método é chamado automaticamente pelo Swing em resposta
     * às chamadas repaint() do game loop.
     *
     * @param g contexto gráfico fornecido pelo sistema Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Habilita antialiasing para renderização suave
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Delega o desenho ao gerenciador de jogo
        gerenciador.desenhar(g2d, getWidth(), getHeight());
    }

    /**
     * Ponto de entrada do programa.
     *
     * Cria e exibe a janela do jogo na Event Dispatch Thread (EDT)
     * do Swing usando SwingUtilities.invokeLater(), garantindo que
     * a criação da interface gráfica ocorra na thread correta.
     *
     * O painel é salvo em variável antes de ser adicionado ao frame
     * para que requestFocusInWindow() possa ser chamado no painel
     * específico — sem isso, o foco vai para o JFrame e o teclado
     * não funciona.
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(
                    "EcoMetropole - Nature Strikes Back");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Salva o painel em variável para controlar o foco
            EcoMetropole painel = new EcoMetropole();
            frame.add(painel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);

            // Direciona o foco ao painel para o teclado funcionar
            painel.requestFocusInWindow();
        });
    }
}