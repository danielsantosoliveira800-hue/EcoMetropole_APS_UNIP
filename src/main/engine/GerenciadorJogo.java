package main.engine;

import main.model.Entidade;
import main.model.Jogador;
import main.model.Item;
import main.model.NuvemToxica;
import main.model.TelaFim;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GerenciadorJogo {
    private ArrayList<Entidade> entidades;
    private Jogador jogador;
    private NuvemToxica nuvemToxica;
    private TelaFim telaFim;
    private int frameCount       = 0;
    private int sustentabilidade = 50;
    private int poluicao         = 20;
    private int pontuacao        = 0;
    private int danoFlash        = 0;
    private static final int FLASH_DURACAO = 20;
    private Random random = new Random();

    private static final Color GRAMA_BASE    = new Color(34,  139, 34);
    private static final Color GRAMA_CLARA   = new Color(60,  179, 60);
    private static final Color GRAMA_ESCURA  = new Color(22,  100, 22);
    private static final Color TERRA         = new Color(101, 67,  33);
    private static final Color TERRA_CLARA   = new Color(130, 90,  44);
    private static final Color PEDRA         = new Color(120, 120, 110);
    private static final Color PEDRA_CLARA   = new Color(160, 160, 148);
    private static final Color FLOR_AMARELA  = new Color(255, 220, 50);
    private static final Color FLOR_ROSA     = new Color(255, 150, 180);
    private static final Color MUSGO         = new Color(80,  160, 40);
    private static final int   TILE          = 32;

    private static final Color ASFALTO        = new Color(60,  60,  65);
    private static final Color ASFALTO_CLARO  = new Color(80,  80,  85);
    private static final Color CALCADA        = new Color(180, 170, 155);
    private static final Color CALCADA_ESCURA = new Color(150, 140, 125);
    private static final Color FAIXA          = new Color(240, 220, 80);

    public GerenciadorJogo() {
        entidades = new ArrayList<>();
        jogador = new Jogador(400, 300);
        nuvemToxica = new NuvemToxica(100, 100);
        telaFim = new TelaFim();
        entidades.add(jogador);
    }

    public void reiniciar() {
        entidades.clear();
        jogador = new Jogador(400, 300);
        nuvemToxica = new NuvemToxica(100, 100);
        telaFim = new TelaFim();
        frameCount       = 0;
        sustentabilidade = 50;
        poluicao         = 20;
        pontuacao        = 0;
        danoFlash        = 0;
        entidades.add(jogador);
    }

    public void atualizar() {
        if (telaFim.isAtiva()) return;

        frameCount++;
        jogador.atualizar();
        nuvemToxica.atualizar(jogador.getX(), jogador.getY());

        if (nuvemToxica.getBounds().intersects(jogador.getBounds())) {
            if (frameCount % 60 == 0) {
                poluicao         = Math.min(100, poluicao + 5);
                sustentabilidade = Math.max(0,   sustentabilidade - 3);
                danoFlash        = FLASH_DURACAO;
                GerenciadorSom.dano();
            }
        }
        if (danoFlash > 0) danoFlash--;

        if (frameCount % 120 == 0) {
            Item.Tipo tipo = random.nextBoolean() ? Item.Tipo.POSITIVO : Item.Tipo.NEGATIVO;
            int x = random.nextInt(700) + 50;
            int y = random.nextInt(500) + 120;
            entidades.add(new Item(x, y, tipo));
        }

        checarColisoes();

        if (sustentabilidade >= 100) {
            GerenciadorSom.vitoria();
            telaFim.setEstado(TelaFim.Estado.VITORIA);
        } else if (poluicao >= 100) {
            GerenciadorSom.derrota();
            telaFim.setEstado(TelaFim.Estado.DERROTA);
        }
    }

    private void checarColisoes() {
        entidades.removeIf(entidade -> {
            if (entidade == jogador) return false;
            if (jogador.getBounds().intersects(entidade.getBounds())) {
                if (entidade instanceof Item item) {
                    int valor = item.getValor();
                    if (valor > 0) {
                        sustentabilidade = Math.min(100, sustentabilidade + valor);
                        poluicao         = Math.max(0,   poluicao - 5);
                        pontuacao       += 10;
                        GerenciadorSom.itemVerde();
                    } else {
                        poluicao         = Math.min(100, poluicao - valor);
                        sustentabilidade = Math.max(0,   sustentabilidade + valor);
                        GerenciadorSom.itemLaranja();
                    }
                }
                return true;
            }
            return false;
        });
    }

    public void ordenarEntidadesPorY() {
        entidades.sort((a, b) -> Integer.compare(a.getY(), b.getY()));
    }

    public void desenhar(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(40, 50, 70));
        g2d.fillRect(0, 0, width, 120);

        desenharPredios(g2d, width);
        desenharChaoIsometrico(g2d, width, height);
        desenharEstradas(g2d, width, height);
        desenharArvores(g2d);
        desenharPostes(g2d, width, height);

        List<Entidade> copia = new ArrayList<>(entidades);
        for (Entidade e : copia) {
            if (e instanceof Item) ((Item) e).atualizar();
            e.desenhar(g2d);
        }

        nuvemToxica.desenhar(g2d);

        if (danoFlash > 0) {
            int alpha = (int)(120 * ((double) danoFlash / FLASH_DURACAO));
            g2d.setColor(new Color(220, 0, 0, alpha));
            g2d.fillRect(0, 0, width, height);
        }

        desenharHUD(g2d, width, height);

        if (telaFim.isAtiva()) {
            telaFim.desenhar(g2d, width, height);
        }
    }

    private void desenharPredios(Graphics2D g2d, int width) {
        int[][] predios = {
                {  20,  60,  90, 100, 110, 130},
                {  90,  80, 110,  80,  90, 110},
                { 180,  50,  70, 120, 130, 150},
                { 240,  70, 100,  70,  80, 100},
                { 320,  55,  80, 110, 120, 140},
                { 390,  90, 115,  60,  70,  90},
                { 490,  65,  95, 100, 110, 130},
                { 565,  75, 105,  75,  85, 105},
                { 650,  55,  75, 115, 125, 145},
                { 715,  85, 100,  65,  75,  95},
                { 810,  60,  85,  95, 105, 125},
                { 880,  70, 110,  80,  90, 110},
        };

        for (int[] p : predios) {
            int px = p[0], pw = p[1], ph = p[2];
            int py = 120 - ph;

            g2d.setColor(new Color(p[3], p[4], p[5]));
            g2d.fillRect(px, py, pw, ph);

            g2d.setColor(new Color(
                    Math.max(0, p[3] - 25),
                    Math.max(0, p[4] - 25),
                    Math.max(0, p[5] - 25)));
            g2d.fillRect(px + pw, py + 4, 8, ph - 4);

            g2d.setColor(new Color(
                    Math.min(255, p[3] + 20),
                    Math.min(255, p[4] + 20),
                    Math.min(255, p[5] + 20)));
            g2d.fillRect(px, py, pw + 8, 5);

            desenharJanelas(g2d, px, py + 8, pw, ph - 8);

            if (px % 3 == 0) {
                g2d.setColor(new Color(80, 80, 90));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(px + pw / 2, py, px + pw / 2, py - 16);
                g2d.setColor(new Color(220, 50, 50));
                g2d.fillOval(px + pw / 2 - 3, py - 19, 6, 6);
                g2d.setStroke(new BasicStroke(1));
            }
        }

        g2d.setColor(new Color(30, 35, 50));
        g2d.fillRect(0, 118, width, 4);
    }

    private void desenharJanelas(Graphics2D g2d, int px, int py, int pw, int ph) {
        int cols = pw / 14;
        int rows = ph / 16;
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                int wx = px + 5 + c * 14;
                int wy = py + 4 + r * 16;
                boolean acesa = (px + c * 7 + r * 13) % 3 != 0;
                g2d.setColor(acesa
                        ? new Color(255, 240, 160, 200)
                        : new Color(40,  50,  70,  180));
                g2d.fillRect(wx, wy, 8, 10);
                g2d.setColor(new Color(60, 70, 90, 120));
                g2d.drawRect(wx, wy, 8, 10);
            }
        }
    }

    private void desenharEstradas(Graphics2D g2d, int width, int height) {
        g2d.setColor(ASFALTO);
        g2d.fillRect(0, 310, width, 60);
        g2d.setColor(ASFALTO_CLARO);
        g2d.fillRect(0, 312, width, 2);
        g2d.fillRect(0, 366, width, 2);

        g2d.setColor(FAIXA);
        for (int fx = 460; fx < 540; fx += 14) {
            g2d.fillRect(fx, 310, 8, 60);
        }

        g2d.setColor(CALCADA);
        g2d.fillRect(0, 295, width, 15);
        g2d.fillRect(0, 370, width, 15);
        g2d.setColor(CALCADA_ESCURA);
        for (int cx = 0; cx < width; cx += 20) {
            g2d.drawRect(cx, 295, 20, 15);
            g2d.drawRect(cx, 370, 20, 15);
        }

        g2d.setColor(ASFALTO);
        g2d.fillRect(460, 120, 80, height);
        g2d.setColor(ASFALTO_CLARO);
        g2d.fillRect(462, 120, 2, height);
        g2d.fillRect(536, 120, 2, height);

        g2d.setColor(FAIXA);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10, new float[]{12, 10}, 0));
        g2d.drawLine(500, 120, 500, height);
        g2d.drawLine(0,   340, 460, 340);
        g2d.drawLine(540, 340, width, 340);
        g2d.setStroke(new BasicStroke(1));
    }

    private void desenharArvores(Graphics2D g2d) {
        int[][] arvores = {
                { 80,  200}, {200, 420}, {350, 180}, {620, 430},
                {750, 200}, {880, 410}, {140, 450}, {420, 500},
                { 60, 380}, {300, 250}, {700, 480}, {820, 260},
        };

        for (int[] a : arvores) {
            int ax = a[0], ay = a[1];

            g2d.setColor(new Color(0, 0, 0, 40));
            g2d.fillOval(ax - 12, ay + 18, 30, 10);

            g2d.setColor(new Color(90, 55, 25));
            g2d.fillRoundRect(ax - 4, ay + 10, 10, 20, 4, 4);

            g2d.setColor(new Color(20, 100, 20));
            g2d.fillOval(ax - 18, ay,      38, 32);
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillOval(ax - 15, ay - 8,  32, 28);
            g2d.setColor(new Color(60, 179, 60));
            g2d.fillOval(ax - 10, ay - 14, 22, 22);

            g2d.setColor(new Color(150, 255, 100, 80));
            g2d.fillOval(ax - 5, ay - 12, 10, 8);
        }
    }

    private void desenharPostes(Graphics2D g2d, int width, int height) {
        int[][] postes = {
                {100, 290}, {280, 290}, {650, 290}, {830, 290},
                {100, 370}, {280, 370}, {650, 370}, {830, 370},
        };

        for (int[] p : postes) {
            int px = p[0], py = p[1];

            g2d.setColor(new Color(0, 0, 0, 40));
            g2d.fillOval(px - 4, py + 2, 10, 4);

            g2d.setColor(new Color(70, 70, 75));
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(px, py, px, py - 45);
            g2d.drawLine(px, py - 45, px + 14, py - 45);
            g2d.setStroke(new BasicStroke(1));

            g2d.setColor(new Color(50, 50, 55));
            g2d.fillRoundRect(px + 8, py - 50, 14, 8, 4, 4);

            g2d.setColor(new Color(255, 230, 100, 220));
            g2d.fillOval(px + 10, py - 48, 10, 6);

            g2d.setColor(new Color(255, 230, 100, 40));
            g2d.fillOval(px + 4, py - 54, 22, 18);
        }
    }

    private void desenharChaoIsometrico(Graphics2D g2d, int width, int height) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        for (int tx = 0; tx < width; tx += TILE) {
            for (int ty = 120; ty < height; ty += TILE) {
                int seed = (tx / TILE) * 31 + (ty / TILE) * 17;
                int tipo = seed % 10;

                if (tipo <= 5)      desenharTileGrama(g2d, tx, ty, seed);
                else if (tipo <= 7) desenharTileTerra(g2d, tx, ty, seed);
                else                desenharTilePedra(g2d, tx, ty, seed);
            }
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void desenharTileGrama(Graphics2D g2d, int tx, int ty, int seed) {
        g2d.setColor(GRAMA_BASE);
        g2d.fillRect(tx, ty, TILE, TILE);

        for (int px = 0; px < TILE; px += 4) {
            for (int py = 0; py < TILE; py += 4) {
                int v = (seed + px * 3 + py * 7) % 6;
                if (v == 0)      g2d.setColor(GRAMA_CLARA);
                else if (v == 1) g2d.setColor(GRAMA_ESCURA);
                else             continue;
                g2d.fillRect(tx + px, ty + py, 4, 4);
            }
        }

        g2d.setColor(GRAMA_ESCURA);
        g2d.drawRect(tx, ty, TILE - 1, TILE - 1);

        int tufoX = tx + (seed % 20) + 4;
        int tufoY = ty + ((seed / 3) % 20) + 4;
        g2d.setColor(GRAMA_CLARA);
        g2d.fillRect(tufoX,     tufoY,     2, 5);
        g2d.fillRect(tufoX + 3, tufoY + 1, 2, 4);
        g2d.fillRect(tufoX + 6, tufoY,     2, 6);

        if (seed % 3 == 0) {
            int fx = tx + (seed % 18) + 7;
            int fy = ty + ((seed * 3) % 18) + 7;
            Color cor = (seed % 2 == 0) ? FLOR_AMARELA : FLOR_ROSA;
            g2d.setColor(MUSGO);
            g2d.fillRect(fx + 1, fy + 3, 2, 4);
            g2d.setColor(cor);
            g2d.fillRect(fx,     fy + 1, 4, 2);
            g2d.fillRect(fx + 1, fy,     2, 4);
            g2d.setColor(FLOR_AMARELA);
            g2d.fillRect(fx + 1, fy + 1, 2, 2);
        }

        if (seed % 5 == 0) {
            int mx = tx + (seed % 22) + 3;
            int my = ty + ((seed * 2) % 22) + 3;
            g2d.setColor(MUSGO);
            g2d.fillOval(mx, my, 6, 4);
        }
    }

    private void desenharTileTerra(Graphics2D g2d, int tx, int ty, int seed) {
        g2d.setColor(TERRA);
        g2d.fillRect(tx, ty, TILE, TILE);

        for (int px = 0; px < TILE; px += 3) {
            for (int py = 0; py < TILE; py += 3) {
                int v = (seed + px * 5 + py * 11) % 5;
                if (v == 0) {
                    g2d.setColor(TERRA_CLARA);
                    g2d.fillRect(tx + px, ty + py, 2, 2);
                } else if (v == 1) {
                    g2d.setColor(GRAMA_ESCURA);
                    g2d.fillRect(tx + px, ty + py, 2, 2);
                }
            }
        }

        g2d.setColor(new Color(70, 45, 20));
        g2d.drawRect(tx, ty, TILE - 1, TILE - 1);

        if (seed % 4 == 0) {
            int px = tx + (seed % 20) + 5;
            int py = ty + ((seed * 4) % 20) + 5;
            g2d.setColor(PEDRA);
            g2d.fillOval(px, py, 5, 3);
            g2d.setColor(PEDRA_CLARA);
            g2d.fillOval(px, py, 2, 1);
        }
    }

    private void desenharTilePedra(Graphics2D g2d, int tx, int ty, int seed) {
        g2d.setColor(PEDRA);
        g2d.fillRect(tx, ty, TILE, TILE);

        for (int px = 0; px < TILE; px += 5) {
            for (int py = 0; py < TILE; py += 5) {
                int v = (seed + px * 7 + py * 13) % 4;
                if (v == 0) {
                    g2d.setColor(PEDRA_CLARA);
                    g2d.fillRect(tx + px, ty + py, 3, 3);
                } else if (v == 1) {
                    g2d.setColor(new Color(90, 90, 82));
                    g2d.fillRect(tx + px, ty + py, 3, 3);
                }
            }
        }

        g2d.setColor(new Color(70, 70, 65));
        int rx = tx + (seed % 10) + 5;
        int ry = ty + ((seed * 2) % 10) + 5;
        g2d.drawLine(rx,     ry,     rx + 8,  ry + 6);
        g2d.drawLine(rx + 8, ry + 6, rx + 10, ry + 12);

        g2d.setColor(new Color(80, 80, 74));
        g2d.drawRect(tx, ty, TILE - 1, TILE - 1);

        if (seed % 3 == 0) {
            g2d.setColor(MUSGO);
            g2d.fillRect(tx + 2,        ty + 2,        6, 3);
            g2d.fillRect(tx + TILE - 8, ty + TILE - 5, 5, 3);
        }
    }

    private void desenharHUD(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(15, height - 130, width - 30, 105, 25, 25);

        g2d.setFont(new Font("Arial", Font.BOLD, 26));

        g2d.setColor(sustentabilidade >= 80 ? Color.GREEN : Color.YELLOW);
        int verdeW = (int)(170 * (sustentabilidade / 100.0));
        g2d.fillRoundRect(35, height - 105, verdeW, 30, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(35, height - 105, 170, 30, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.drawString("🌿 " + sustentabilidade + "%", 40, height - 75);

        g2d.setColor(poluicao >= 80 ? Color.RED : new Color(255, 165, 0));
        int laranjaW = (int)(170 * (poluicao / 100.0));
        g2d.fillRoundRect(width - 205, height - 105, laranjaW, 30, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(width - 205, height - 105, 170, 30, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.drawString(poluicao + "% ☢️", width - 190, height - 75);

        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.setColor(new Color(255, 220, 50));
        String pts = "⭐ Pontos: " + pontuacao;
        int ptW = g2d.getFontMetrics().stringWidth(pts);
        g2d.drawString(pts, width / 2 - ptW / 2, height - 78);

        g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 36));
        g2d.setColor(new Color(0, 255, 255, 200));
        g2d.drawString("EcoMetrópole", width / 2 - 140, 45);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("WASD mover | Fuja da nuvem! | Verde +10 | Laranja -15",
                width / 2 - 210, 70);
    }

    public void clicar(int mx, int my, int width, int height) {
        if (!telaFim.isAtiva()) return;
        if (telaFim.clicouReiniciar(mx, my, width, height)) reiniciar();
        else if (telaFim.clicouFechar(mx, my, width, height)) System.exit(0);
    }

    public Jogador getJogador()      { return jogador; }
    public int getSustentabilidade() { return sustentabilidade; }
    public int getPoluicao()         { return poluicao; }
}