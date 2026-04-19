package main.model;

import java.awt.*;

/**
 * Classe TelaFim — gerencia e renderiza a tela de fim de jogo.
 *
 * Esta classe é responsável por exibir o resultado da partida ao jogador
 * quando o jogo é encerrado por vitória ou derrota, e por detectar os
 * cliques nos botões de reiniciar e fechar.
 *
 * A classe utiliza um enumerador interno para representar os três estados
 * possíveis do jogo, demonstrando boas práticas de design orientado a
 * objetos ao evitar o uso de constantes numéricas arbitrárias para
 * representar estados.
 *
 * CONCEITOS DE POO APLICADOS:
 * - Encapsulamento: estado interno protegido por métodos de acesso
 * - Abstração: enumerador Estado representa os estados do jogo
 */
public class TelaFim {

    /**
     * Enumerador que representa os três estados possíveis do jogo.
     *
     * JOGANDO — partida em andamento, tela de fim não é exibida
     * VITORIA — jogador venceu (sustentabilidade atingiu 100%)
     * DERROTA — jogador perdeu (poluição atingiu 100%)
     */
    public enum Estado { VITORIA, DERROTA, JOGANDO }

    /**
     * Estado atual do jogo.
     * Inicializado como JOGANDO e alterado pelo GerenciadorJogo
     * quando as condições de vitória ou derrota são detectadas.
     */
    private Estado estado = Estado.JOGANDO;

    /**
     * Define o estado atual do jogo.
     * Chamado pelo GerenciadorJogo quando a partida é encerrada.
     * @param estado novo estado do jogo (VITORIA ou DERROTA)
     */
    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    /**
     * Retorna o estado atual do jogo.
     * @return estado atual (VITORIA, DERROTA ou JOGANDO)
     */
    public Estado getEstado() {
        return estado;
    }

    /**
     * Verifica se a tela de fim está ativa.
     * Utilizado pelo GerenciadorJogo para pausar o jogo e
     * pelo método desenhar para decidir se deve renderizar a tela.
     * @return true se o jogo foi encerrado (vitória ou derrota)
     */
    public boolean isAtiva() {
        return estado != Estado.JOGANDO;
    }

    /**
     * Renderiza a tela de fim de jogo sobre o cenário.
     *
     * Desenha um fundo semitransparente sobre toda a tela, um painel
     * centralizado com a mensagem de resultado, uma descrição e dois
     * botões — Reiniciar e Fechar. As cores variam conforme o resultado:
     * verde para vitória e vermelho para derrota.
     *
     * @param g2d    contexto gráfico 2D fornecido pelo sistema Swing
     * @param width  largura da tela em pixels
     * @param height altura da tela em pixels
     */
    public void desenhar(Graphics2D g2d, int width, int height) {
        // Fundo escuro semitransparente sobre o cenário
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, width, height);

        // Dimensões e posição do painel central
        int pw = 420, ph = 220;
        int px = width  / 2 - pw / 2;
        int py = height / 2 - ph / 2;

        // Fundo do painel
        g2d.setColor(new Color(20, 20, 20, 230));
        g2d.fillRoundRect(px, py, pw, ph, 30, 30);

        // Borda colorida conforme resultado
        if (estado == Estado.VITORIA) {
            g2d.setColor(new Color(50, 220, 50));
        } else {
            g2d.setColor(new Color(220, 60, 60));
        }
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(px, py, pw, ph, 30, 30);
        g2d.setStroke(new BasicStroke(1));

        // Título do resultado
        g2d.setFont(new Font("Arial", Font.BOLD, 42));
        String titulo = estado == Estado.VITORIA ? "VITORIA!" : "DERROTA!";
        FontMetrics fm = g2d.getFontMetrics();
        int tx = width / 2 - fm.stringWidth(titulo) / 2;
        g2d.setColor(estado == Estado.VITORIA
                ? new Color(50, 220, 50)
                : new Color(220, 60, 60));
        g2d.drawString(titulo, tx, py + 70);

        // Mensagem descritiva
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.setColor(new Color(200, 200, 200));
        String sub = estado == Estado.VITORIA
                ? "Voce salvou o meio ambiente!"
                : "A poluicao tomou conta da cidade!";
        fm = g2d.getFontMetrics();
        g2d.drawString(sub,
                width / 2 - fm.stringWidth(sub) / 2, py + 105);

        // Botões de ação
        desenharBotao(g2d, width / 2 - 160, py + 135, 140, 45,
                "Reiniciar", new Color(50, 150, 50));
        desenharBotao(g2d, width / 2 + 20,  py + 135, 140, 45,
                "Fechar",    new Color(150, 50, 50));
    }

    /**
     * Renderiza um botão na tela.
     *
     * Método auxiliar privado que desenha um botão com fundo colorido,
     * borda mais escura e texto centralizado. Os botões são desenhados
     * usando formas geométricas do Graphics2D, sem utilizar componentes
     * Swing como JButton, mantendo consistência visual com o jogo.
     *
     * @param g2d    contexto gráfico 2D
     * @param x      posição horizontal do botão
     * @param y      posição vertical do botão
     * @param w      largura do botão
     * @param h      altura do botão
     * @param texto  texto exibido no botão
     * @param cor    cor de fundo do botão
     */
    private void desenharBotao(Graphics2D g2d, int x, int y,
                               int w, int h,
                               String texto, Color cor) {
        // Fundo do botão
        g2d.setColor(cor);
        g2d.fillRoundRect(x, y, w, h, 15, 15);

        // Borda mais escura
        g2d.setColor(cor.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, w, h, 15, 15);
        g2d.setStroke(new BasicStroke(1));

        // Texto centralizado no botão
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(texto,
                x + w / 2 - fm.stringWidth(texto) / 2,
                y + h / 2 + fm.getAscent() / 2 - 2);
    }

    /**
     * Verifica se o clique do mouse ocorreu sobre o botão Reiniciar.
     *
     * Calcula a posição do botão com base nas dimensões da tela e
     * verifica se as coordenadas do clique estão dentro dessa área.
     *
     * @param mx     coordenada horizontal do clique
     * @param my     coordenada vertical do clique
     * @param width  largura da tela
     * @param height altura da tela
     * @return true se o clique foi dentro do botão Reiniciar
     */
    public boolean clicouReiniciar(int mx, int my,
                                   int width, int height) {
        int py = height / 2 - 110;
        int bx = width / 2 - 160;
        int by = py + 135;
        return mx >= bx && mx <= bx + 140
                && my >= by && my <= by + 45;
    }

    /**
     * Verifica se o clique do mouse ocorreu sobre o botão Fechar.
     *
     * Mesma lógica que clicouReiniciar(), mas para o botão Fechar.
     *
     * @param mx     coordenada horizontal do clique
     * @param my     coordenada vertical do clique
     * @param width  largura da tela
     * @param height altura da tela
     * @return true se o clique foi dentro do botão Fechar
     */
    public boolean clicouFechar(int mx, int my,
                                int width, int height) {
        int py = height / 2 - 110;
        int bx = width / 2 + 20;
        int by = py + 135;
        return mx >= bx && mx <= bx + 140
                && my >= by && my <= by + 45;
    }
}