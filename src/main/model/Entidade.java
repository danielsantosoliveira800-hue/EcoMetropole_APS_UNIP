package main.model;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Entidade {
    protected int x, y;
    protected int largura, altura;

    public Entidade(int x, int y, int largura, int altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, largura, altura);
    }

    public int getX()       { return x; }
    public int getY()       { return y; }
    public int getLargura() { return largura; }
    public int getAltura()  { return altura; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public abstract void atualizar();
    public abstract void desenhar(Graphics2D g2d);
}