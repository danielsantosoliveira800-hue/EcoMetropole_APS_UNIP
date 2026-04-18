package main.model;

import java.awt.*;

public class NuvemToxica extends Entidade {

    private static final int   VELOCIDADE    = 2;
    private static final Color COR_NUVEM     = new Color(180, 80,  220);
    private static final Color COR_NUVEM_ESC = new Color(120, 40,  160);
    private static final Color COR_BRILHO    = new Color(220, 150, 255, 80);
    private static final Color COR_OLHO      = new Color(255, 50,  50);

    private double posX;     // posição real em double
    private double posY;
    private double dx    = 0;
    private double dy    = 0;
    private int    animFrame = 0;
    private double pulsacao  = 0;

    public NuvemToxica(int x, int y) {
        super(x, y, 48, 36);
        posX = x;  // inicializa com a posição inicial
        posY = y;
    }

    public void atualizar(int alvoX, int alvoY) {
        double diffX = alvoX - posX;
        double diffY = alvoY - posY;
        double dist  = Math.sqrt(diffX * diffX + diffY * diffY);

        if (dist > 0) {
            dx += (diffX / dist * VELOCIDADE - dx) * 0.08;
            dy += (diffY / dist * VELOCIDADE - dy) * 0.08;
        }

        // Atualiza posição em double (sem perder decimais!)
        posX += dx;
        posY += dy;

        posX = Math.max(0, Math.min(950, posX));
        posY = Math.max(0, Math.min(650, posY));

        // Sincroniza x, y inteiros da superclasse para desenho e colisão
        x = (int) posX;
        y = (int) posY;

        animFrame++;
        pulsacao += 0.1;
    }

    @Override
    public void atualizar() {
        // sem alvo, não faz nada — use atualizar(alvoX, alvoY)
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 8, y + 8, 32, 22);
    }

    @Override
    public void desenhar(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int pulso = (int)(Math.sin(pulsacao) * 3);

        // Sombra
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillOval(x + 4, y + largura - 8, largura - 8, 10);

        // Corpo escuro
        g2d.setColor(COR_NUVEM_ESC);
        g2d.fillOval(x,      y + 10, 28 + pulso, 22 + pulso);
        g2d.fillOval(x + 16, y + 6,  26 + pulso, 24 + pulso);
        g2d.fillOval(x + 26, y + 12, 22 + pulso, 20 + pulso);

        // Corpo claro
        g2d.setColor(COR_NUVEM);
        g2d.fillOval(x + 2,  y + 8,  26 + pulso, 20 + pulso);
        g2d.fillOval(x + 14, y + 4,  24 + pulso, 22 + pulso);
        g2d.fillOval(x + 24, y + 10, 20 + pulso, 18 + pulso);

        // Brilho topo
        g2d.setColor(COR_BRILHO);
        g2d.fillOval(x + 14, y + 5, 16, 10);

        // Olhos
        g2d.setColor(COR_OLHO);
        g2d.fillOval(x + 16, y + 12, 7, 7);
        g2d.fillOval(x + 26, y + 12, 7, 7);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + 18, y + 14, 3, 3);
        g2d.fillOval(x + 28, y + 14, 3, 3);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + 19, y + 14, 2, 2);
        g2d.fillOval(x + 29, y + 14, 2, 2);

        // Partículas orbitando
        desenharParticulas(g2d);
    }

    private void desenharParticulas(Graphics2D g2d) {
        int[] angulos = {0, 90, 180, 270};
        for (int i = 0; i < angulos.length; i++) {
            double angulo = Math.toRadians(angulos[i] + animFrame * 2);
            int px = x + 24 + (int)(Math.cos(angulo) * 22);
            int py = y + 18 + (int)(Math.sin(angulo) * 14);
            int tamanho = 4 + (i % 2) * 3;
            g2d.setColor(new Color(180, 80, 220, 120));
            g2d.fillOval(px, py, tamanho, tamanho);
        }
    }
}