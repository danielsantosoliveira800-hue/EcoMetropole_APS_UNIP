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

public class EcoMetropole extends JPanel implements Runnable {
    private static final int FPS = 60;
    private static final int FPS_DELAY = 1000 / FPS;

    private GerenciadorJogo gerenciador;
    private Thread gameThread;
    private volatile boolean running = true;

    public EcoMetropole() {
        gerenciador = new GerenciadorJogo();
        setFocusable(true);
        setPreferredSize(new Dimension(1000, 700));
        setBackground(Color.decode("#1a3c1a"));

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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                gerenciador.clicar(e.getX(), e.getY(), getWidth(), getHeight());
                requestFocusInWindow(); // mantém foco no painel após clicar
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void run() {
        while (running) {
            long start = System.currentTimeMillis();
            atualizar();
            repaint();

            long elapsed = System.currentTimeMillis() - start;
            if (elapsed < FPS_DELAY) {
                try {
                    Thread.sleep(FPS_DELAY - elapsed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
        }
    }

    private void atualizar() {
        gerenciador.atualizar();
        gerenciador.ordenarEntidadesPorY();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        gerenciador.desenhar(g2d, getWidth(), getHeight());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🌳 EcoMetrópole - Nature Strikes Back");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            EcoMetropole painel = new EcoMetropole();
            frame.add(painel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
            painel.requestFocusInWindow();
        });
    }
}