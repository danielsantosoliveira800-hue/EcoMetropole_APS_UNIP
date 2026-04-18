package main.model;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Jogador extends Entidade {
    private static final int VELOCIDADE = 5;
    public boolean up, down, left, right;
    private BufferedImage sprite;
    private int animFrame = 0;

    public Jogador(int x, int y) {
        super(x, y, 56, 56);
        sprite = criarSprite();
    }

    private BufferedImage criarSprite() {
        BufferedImage img = new BufferedImage(56, 56, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Sombra
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(6, 46, 44, 10);

        // Pernas (base)
        g.setColor(new Color(30, 80, 160));
        g.fillRoundRect(14, 38, 10, 14, 5, 5);
        g.fillRoundRect(32, 38, 10, 14, 5, 5);

        // Sapatos
        g.setColor(new Color(40, 40, 40));
        g.fillRoundRect(12, 48, 13, 7, 4, 4);
        g.fillRoundRect(30, 48, 13, 7, 4, 4);

        // Corpo
        g.setColor(new Color(30, 144, 255));
        g.fillRoundRect(10, 20, 36, 24, 14, 14);

        // Detalhe roupa - bolso
        g.setColor(new Color(20, 110, 200));
        g.fillRoundRect(14, 26, 10, 8, 4, 4);
        g.setColor(new Color(10, 80, 160));
        g.drawRoundRect(14, 26, 10, 8, 4, 4);

        // Cinto
        g.setColor(new Color(80, 50, 20));
        g.fillRect(10, 38, 36, 5);
        g.setColor(new Color(200, 170, 50));
        g.fillRect(24, 38, 8, 5);

        // Pescoço
        g.setColor(new Color(255, 200, 150));
        g.fillRect(23, 14, 10, 8);

        // Cabeça
        g.setColor(new Color(255, 210, 160));
        g.fillOval(14, 4, 28, 26);

        // Cabelo
        g.setColor(new Color(80, 50, 20));
        g.fillArc(14, 4, 28, 18, 0, 180);
        g.fillRect(14, 4, 5, 10);
        g.fillRect(37, 4, 5, 10);

        // Orelhas
        g.setColor(new Color(240, 190, 140));
        g.fillOval(11, 12, 7, 9);
        g.fillOval(38, 12, 7, 9);

        // Olhos brancos
        g.setColor(Color.WHITE);
        g.fillOval(20, 13, 8, 7);
        g.fillOval(30, 13, 8, 7);

        // Íris
        g.setColor(new Color(50, 100, 200));
        g.fillOval(22, 14, 5, 5);
        g.fillOval(32, 14, 5, 5);

        // Pupilas
        g.setColor(Color.BLACK);
        g.fillOval(23, 15, 3, 3);
        g.fillOval(33, 15, 3, 3);

        // Brilho olhos
        g.setColor(Color.WHITE);
        g.fillOval(24, 15, 2, 2);
        g.fillOval(34, 15, 2, 2);

        // Sobrancelhas
        g.setColor(new Color(80, 50, 20));
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(20, 11, 27, 12);
        g.drawLine(30, 12, 37, 11);
        g.setStroke(new BasicStroke(1));

        // Boca / sorriso
        g.setColor(new Color(180, 80, 80));
        g.drawArc(22, 21, 12, 6, 200, 140);

        // Braços
        g.setColor(new Color(30, 144, 255));
        g.fillRoundRect(4,  22, 9, 18, 6, 6);
        g.fillRoundRect(43, 22, 9, 18, 6, 6);

        // Mãos
        g.setColor(new Color(255, 200, 150));
        g.fillOval(4,  38, 9, 9);
        g.fillOval(43, 38, 9, 9);

        // Contorno corpo
        g.setColor(new Color(10, 90, 180));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(10, 20, 36, 24, 14, 14);
        g.setStroke(new BasicStroke(1));

        g.dispose();
        return img;
    }

    @Override
    public void atualizar() {
        int dx = 0, dy = 0;

        if (up)    { dx += VELOCIDADE; dy -= VELOCIDADE; }
        if (down)  { dx -= VELOCIDADE; dy += VELOCIDADE; }
        if (left)  { dx -= VELOCIDADE; dy -= VELOCIDADE; }
        if (right) { dx += VELOCIDADE; dy += VELOCIDADE; }

        x += dx;
        y += dy;

        x = Math.max(0, Math.min(920, x));
        y = Math.max(0, Math.min(620, y));

        animFrame++;
    }

    public void setUp(boolean b)    { up = b; }
    public void setDown(boolean b)  { down = b; }
    public void setLeft(boolean b)  { left = b; }
    public void setRight(boolean b) { right = b; }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 10, y + 20, 36, 30);
    }

    @Override
    public void desenhar(Graphics2D g2d) {
        // Pernas animadas
        int legOffset = (int)(Math.sin(animFrame * 0.3) * 5);
        g2d.setColor(new Color(30, 80, 160));
        g2d.fillRoundRect(x + 14 + legOffset, y + 38, 10, 14, 5, 5);
        g2d.fillRoundRect(x + 32 - legOffset, y + 38, 10, 14, 5, 5);
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRoundRect(x + 12 + legOffset, y + 48, 13, 7, 4, 4);
        g2d.fillRoundRect(x + 30 - legOffset, y + 48, 13, 7, 4, 4);

        g2d.drawImage(sprite, x, y, largura, altura, null);
    }
}