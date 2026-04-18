package main.model;

import java.awt.*;

public class TelaFim {

    public enum Estado { VITORIA, DERROTA, JOGANDO }

    private Estado estado = Estado.JOGANDO;

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Estado getEstado() {
        return estado;
    }

    public boolean isAtiva() {
        return estado != Estado.JOGANDO;
    }

    public void desenhar(Graphics2D g2d, int width, int height) {
        // Fundo escuro semitransparente
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, width, height);

        // Painel central
        int pw = 420, ph = 220;
        int px = width  / 2 - pw / 2;
        int py = height / 2 - ph / 2;

        g2d.setColor(new Color(20, 20, 20, 230));
        g2d.fillRoundRect(px, py, pw, ph, 30, 30);

        if (estado == Estado.VITORIA) {
            g2d.setColor(new Color(50, 220, 50));
        } else {
            g2d.setColor(new Color(220, 60, 60));
        }
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(px, py, pw, ph, 30, 30);
        g2d.setStroke(new BasicStroke(1));

        // Título
        g2d.setFont(new Font("Arial", Font.BOLD, 42));
        String titulo = estado == Estado.VITORIA ? "🌿 VITÓRIA!" : "💀 DERROTA!";
        FontMetrics fm = g2d.getFontMetrics();
        int tx = width / 2 - fm.stringWidth(titulo) / 2;
        g2d.setColor(estado == Estado.VITORIA
                ? new Color(50, 220, 50)
                : new Color(220, 60, 60));
        g2d.drawString(titulo, tx, py + 70);

        // Subtítulo
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.setColor(new Color(200, 200, 200));
        String sub = estado == Estado.VITORIA
                ? "Você salvou o meio ambiente!"
                : "A poluição tomou conta da cidade!";
        fm = g2d.getFontMetrics();
        g2d.drawString(sub, width / 2 - fm.stringWidth(sub) / 2, py + 105);

        // Botão reiniciar
        desenharBotao(g2d, width / 2 - 160, py + 135, 140, 45,
                "Reiniciar", new Color(50, 150, 50));

        // Botão fechar
        desenharBotao(g2d, width / 2 + 20, py + 135, 140, 45,
                "Fechar", new Color(150, 50, 50));
    }

    private void desenharBotao(Graphics2D g2d, int x, int y,
                               int w, int h, String texto, Color cor) {
        g2d.setColor(cor);
        g2d.fillRoundRect(x, y, w, h, 15, 15);
        g2d.setColor(cor.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, w, h, 15, 15);
        g2d.setStroke(new BasicStroke(1));

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(texto,
                x + w / 2 - fm.stringWidth(texto) / 2,
                y + h / 2 + fm.getAscent() / 2 - 2);
    }

    // Verifica se clicou no botão reiniciar
    public boolean clicouReiniciar(int mx, int my, int width, int height) {
        int pw = 420, ph = 220;
        int px = width  / 2 - pw / 2;
        int py = height / 2 - ph / 2;
        int bx = width / 2 - 160;
        int by = py + 135;
        return mx >= bx && mx <= bx + 140 && my >= by && my <= by + 45;
    }

    // Verifica se clicou no botão fechar
    public boolean clicouFechar(int mx, int my, int width, int height) {
        int pw = 420, ph = 220;
        int px = width  / 2 - pw / 2;
        int py = height / 2 - ph / 2;
        int bx = width / 2 + 20;
        int by = py + 135;
        return mx >= bx && mx <= bx + 140 && my >= by && my <= by + 45;
    }
}