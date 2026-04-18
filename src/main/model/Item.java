package main.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Item extends Entidade {
    public enum Tipo { POSITIVO, NEGATIVO }

    private Tipo tipo;
    private int valor;
    private BufferedImage sprite;
    private double rotacao  = 0;
    private double pulsacao = 0;

    public Item(int x, int y, Tipo tipo) {
        super(x, y, 32, 32);
        this.tipo  = tipo;
        this.valor = tipo == Tipo.POSITIVO ? 10 : -15;
        sprite = criarSprite();
    }

    private BufferedImage criarSprite() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (tipo == Tipo.POSITIVO) {
            // Fundo neon sólido
            g.setColor(new Color(0, 220, 60));
            g.fillOval(0, 0, 32, 32);

            // Anel externo brilhante
            g.setColor(new Color(100, 255, 100));
            g.setStroke(new BasicStroke(2.5f));
            g.drawOval(1, 1, 30, 30);
            g.setStroke(new BasicStroke(1));

            // Folha principal bem sólida
            g.setColor(new Color(0, 180, 40));
            int[] xp = {16, 28, 20, 26, 16, 6,  12, 4};
            int[] yp = {2,  10, 12, 22, 16, 22, 12, 10};
            g.fillPolygon(xp, yp, 8);

            // Nervura central
            g.setColor(new Color(0, 100, 20));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(16, 4, 16, 26);

            // Nervuras laterais
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(16, 10, 22, 14);
            g.drawLine(16, 15, 22, 18);
            g.drawLine(16, 10, 10, 14);
            g.drawLine(16, 15, 10, 18);
            g.setStroke(new BasicStroke(1));

            // Brilho branco no topo
            g.setColor(new Color(255, 255, 255, 180));
            g.fillOval(10, 4, 9, 7);

            // Caule
            g.setColor(new Color(0, 140, 30));
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(16, 26, 14, 30);
            g.setStroke(new BasicStroke(1));

        } else {
            // Fundo brilhante laranja
            g.setColor(new Color(255, 100, 0, 60));
            g.fillOval(0, 0, 32, 32);

            // Barril de lixo tóxico
            g.setColor(new Color(60, 60, 60));
            g.fillRoundRect(6, 8, 20, 20, 6, 6);
            g.setColor(new Color(80, 80, 80));
            g.fillRoundRect(7, 9, 18, 18, 5, 5);

            // Faixas amarelas de perigo
            g.setColor(new Color(255, 200, 0));
            g.fillRect(6, 14, 20, 4);
            g.setColor(new Color(200, 150, 0));
            g.setStroke(new BasicStroke(0.8f));
            g.drawRect(6, 14, 20, 4);
            g.setStroke(new BasicStroke(1));

            // Tampa
            g.setColor(new Color(50, 50, 50));
            g.fillRoundRect(5, 6, 22, 5, 4, 4);
            g.setColor(new Color(90, 90, 90));
            g.fillRoundRect(6, 7, 20, 3, 3, 3);

            // Símbolo radioativo
            g.setColor(new Color(255, 200, 0));
            g.fillOval(13, 17, 6, 6);
            g.setColor(new Color(60, 60, 60));
            g.fillOval(14, 18, 4, 4);

            // Vazamento tóxico
            g.setColor(new Color(100, 220, 0, 180));
            g.fillOval(8,  26, 5, 4);
            g.fillOval(14, 27, 4, 3);
            g.fillOval(20, 26, 4, 4);

            // Contorno
            g.setColor(new Color(40, 40, 40));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(6, 8, 20, 20, 6, 6);
            g.setStroke(new BasicStroke(1));
        }

        g.dispose();
        return img;
    }

    public Tipo getTipo()  { return tipo; }
    public int getValor()  { return valor; }

    @Override
    public void atualizar() {
        rotacao  += tipo == Tipo.POSITIVO ? 0.03 : 0.06;
        pulsacao += 0.08;
    }

    @Override
    public void desenhar(Graphics2D g2d) {
        // Sombra no chão
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillOval(x + 4, y + 28, 24, 6);

        // Pulsação leve (sobe e desce)
        int offsetY = (int)(Math.sin(pulsacao) * 3);

        // Aura neon pulsante ao redor do item verde
        if (tipo == Tipo.POSITIVO) {
            int auraAlpha = (int)(80 + Math.sin(pulsacao) * 60);
            int auraSize  = (int)(4  + Math.sin(pulsacao) * 3);
            g2d.setColor(new Color(0, 255, 80, auraAlpha));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawOval(x - auraSize / 2, y + offsetY - auraSize / 2,
                    32 + auraSize, 32 + auraSize);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.setColor(new Color(150, 255, 150, auraAlpha / 2));
            g2d.drawOval(x - auraSize, y + offsetY - auraSize,
                    32 + auraSize * 2, 32 + auraSize * 2);
            g2d.setStroke(new BasicStroke(1));
        }

        AffineTransform old = g2d.getTransform();
        g2d.rotate(rotacao, x + 16, y + 16 + offsetY);
        g2d.drawImage(sprite, x, y + offsetY, largura, altura, null);
        g2d.setTransform(old);

        // Brilho laranja para item negativo
        if (tipo == Tipo.NEGATIVO) {
            int alpha = (int)(40 + Math.sin(pulsacao) * 30);
            g2d.setColor(new Color(255, 100, 0, alpha));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawOval(x + 1, y + 1 + offsetY, 30, 30);
            g2d.setStroke(new BasicStroke(1));
        }
    }
}