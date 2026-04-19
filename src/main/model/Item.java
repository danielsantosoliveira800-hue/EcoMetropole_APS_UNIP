package main.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Classe Item — representa os itens coletáveis espalhados pelo mapa.
 *
 * Esta classe estende a classe abstrata Entidade, demonstrando o conceito
 * de HERANÇA da Programação Orientada a Objetos. Cada item possui um tipo
 * definido pelo enumerador interno Tipo, que determina seu visual, seu valor
 * e seus efeitos sobre os índices do jogo ao ser coletado pelo jogador.
 *
 * CONCEITOS DE POO APLICADOS:
 * - Herança: estende a classe abstrata Entidade
 * - Polimorfismo: implementa atualizar() e desenhar() de forma específica
 * - Encapsulamento: atributos privados acessados por métodos públicos
 * - Abstração: o enumerador Tipo abstrai os dois tipos de item possíveis
 */
public class Item extends Entidade {

    /**
     * Enumerador que define os dois tipos de item do jogo.
     *
     * POSITIVO — representa recursos sustentáveis (folha verde).
     *            Ao ser coletado: +10 sustentabilidade, -5 poluição,
     *            +10 pontos.
     *
     * NEGATIVO — representa resíduos tóxicos (barril de lixo).
     *            Ao ser coletado: +15 poluição, -15 sustentabilidade.
     */
    public enum Tipo { POSITIVO, NEGATIVO }

    /**
     * Tipo do item — determina visual, valor e efeitos ao ser coletado.
     */
    private Tipo tipo;

    /**
     * Valor numérico do impacto do item sobre o índice de sustentabilidade.
     * Positivo (+10) para itens sustentáveis, negativo (-15) para tóxicos.
     */
    private int valor;

    /**
     * Imagem pre-renderizada do sprite do item.
     * Gerada uma única vez no construtor e reutilizada a cada quadro.
     */
    private BufferedImage sprite;

    /**
     * Ângulo de rotação atual do item em radianos.
     * Incrementado a cada quadro para criar a animação de rotação contínua.
     */
    private double rotacao = 0;

    /**
     * Fase da função seno utilizada para calcular a flutuação vertical.
     * Incrementada a cada quadro para criar a animação de flutuação suave.
     */
    private double pulsacao = 0;

    /**
     * Construtor da classe Item.
     *
     * Inicializa o item na posição fornecida com dimensões de 32x32 pixels,
     * define o valor com base no tipo e gera o sprite correspondente.
     *
     * @param x    posição horizontal inicial em pixels
     * @param y    posição vertical inicial em pixels
     * @param tipo tipo do item (POSITIVO ou NEGATIVO)
     */
    public Item(int x, int y, Tipo tipo) {
        super(x, y, 32, 32);
        this.tipo  = tipo;
        this.valor = tipo == Tipo.POSITIVO ? 10 : -15;
        sprite = criarSprite();
    }

    /**
     * Gera o sprite do item programaticamente usando Graphics2D.
     *
     * O sprite varia conforme o tipo do item:
     * - POSITIVO: folha verde com nervuras, caule e brilho neon
     * - NEGATIVO: barril de lixo tóxico com faixas de perigo e vazamento
     *
     * @return BufferedImage contendo o sprite do item
     */
    private BufferedImage criarSprite() {
        BufferedImage img = new BufferedImage(32, 32,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (tipo == Tipo.POSITIVO) {
            // Fundo circular verde neon
            g.setColor(new Color(0, 220, 60));
            g.fillOval(0, 0, 32, 32);

            // Anel externo brilhante
            g.setColor(new Color(100, 255, 100));
            g.setStroke(new BasicStroke(2.5f));
            g.drawOval(1, 1, 30, 30);
            g.setStroke(new BasicStroke(1));

            // Folha principal — polígono de 8 pontos
            g.setColor(new Color(0, 180, 40));
            int[] xp = {16, 28, 20, 26, 16, 6,  12, 4};
            int[] yp = {2,  10, 12, 22, 16, 22, 12, 10};
            g.fillPolygon(xp, yp, 8);

            // Nervura central da folha
            g.setColor(new Color(0, 100, 20));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g.drawLine(16, 4, 16, 26);

            // Nervuras laterais
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(16, 10, 22, 14);
            g.drawLine(16, 15, 22, 18);
            g.drawLine(16, 10, 10, 14);
            g.drawLine(16, 15, 10, 18);
            g.setStroke(new BasicStroke(1));

            // Brilho no topo da folha
            g.setColor(new Color(255, 255, 255, 180));
            g.fillOval(10, 4, 9, 7);

            // Caule
            g.setColor(new Color(0, 140, 30));
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g.drawLine(16, 26, 14, 30);
            g.setStroke(new BasicStroke(1));

        } else {
            // Fundo circular laranja semitransparente
            g.setColor(new Color(255, 100, 0, 60));
            g.fillOval(0, 0, 32, 32);

            // Corpo do barril
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

            // Tampa do barril
            g.setColor(new Color(50, 50, 50));
            g.fillRoundRect(5, 6, 22, 5, 4, 4);
            g.setColor(new Color(90, 90, 90));
            g.fillRoundRect(6, 7, 20, 3, 3, 3);

            // Símbolo de alerta radioativo
            g.setColor(new Color(255, 200, 0));
            g.fillOval(13, 17, 6, 6);
            g.setColor(new Color(60, 60, 60));
            g.fillOval(14, 18, 4, 4);

            // Vazamento tóxico na base
            g.setColor(new Color(100, 220, 0, 180));
            g.fillOval(8,  26, 5, 4);
            g.fillOval(14, 27, 4, 3);
            g.fillOval(20, 26, 4, 4);

            // Contorno do barril
            g.setColor(new Color(40, 40, 40));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(6, 8, 20, 20, 6, 6);
            g.setStroke(new BasicStroke(1));
        }

        g.dispose();
        return img;
    }

    /**
     * Retorna o tipo do item.
     * @return Tipo.POSITIVO ou Tipo.NEGATIVO
     */
    public Tipo getTipo() { return tipo; }

    /**
     * Retorna o valor numérico do impacto do item.
     * @return +10 para POSITIVO, -15 para NEGATIVO
     */
    public int getValor() { return valor; }

    /**
     * Atualiza o estado do item a cada quadro do game loop.
     *
     * Implementação do método abstrato herdado de Entidade.
     * Incrementa os atributos de animação rotacao e pulsacao,
     * controlando respectivamente a rotação e a flutuação do item.
     * Itens negativos rotacionam mais rápido que positivos.
     */
    @Override
    public void atualizar() {
        // Itens negativos rotacionam mais rápido (0.06 vs 0.03)
        rotacao  += tipo == Tipo.POSITIVO ? 0.03 : 0.06;
        pulsacao += 0.08;
    }

    /**
     * Renderiza o item na tela a cada quadro do game loop.
     *
     * Implementação do método abstrato herdado de Entidade.
     * Aplica animações de rotação e flutuação ao sprite do item.
     * Itens positivos possuem uma aura neon pulsante ao redor.
     *
     * A rotação é aplicada usando AffineTransform para rotacionar
     * o contexto gráfico ao redor do centro do item. A flutuação
     * é calculada usando a função seno aplicada ao atributo pulsacao.
     *
     * @param g2d contexto gráfico 2D fornecido pelo sistema Swing
     */
    @Override
    public void desenhar(Graphics2D g2d) {
        // Sombra elíptica no chão
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillOval(x + 4, y + 28, 24, 6);

        // Deslocamento vertical de flutuação calculado pela função seno
        int offsetY = (int)(Math.sin(pulsacao) * 3);

        // Aura neon pulsante exclusiva do item positivo
        if (tipo == Tipo.POSITIVO) {
            int auraAlpha = (int)(80 + Math.sin(pulsacao) * 60);
            int auraSize  = (int)(4  + Math.sin(pulsacao) * 3);
            g2d.setColor(new Color(0, 255, 80, auraAlpha));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawOval(x - auraSize / 2,
                    y + offsetY - auraSize / 2,
                    32 + auraSize, 32 + auraSize);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.setColor(new Color(150, 255, 150, auraAlpha / 2));
            g2d.drawOval(x - auraSize,
                    y + offsetY - auraSize,
                    32 + auraSize * 2, 32 + auraSize * 2);
            g2d.setStroke(new BasicStroke(1));
        }

        // Salva o estado atual da transformação gráfica
        AffineTransform old = g2d.getTransform();

        // Aplica rotação ao redor do centro do item
        g2d.rotate(rotacao, x + 16, y + 16 + offsetY);

        // Desenha o sprite com deslocamento de flutuação
        g2d.drawImage(sprite, x, y + offsetY, largura, altura, null);

        // Restaura a transformação original
        g2d.setTransform(old);

        // Aura laranja pulsante do item negativo
        if (tipo == Tipo.NEGATIVO) {
            int alpha = (int)(40 + Math.sin(pulsacao) * 30);
            g2d.setColor(new Color(255, 100, 0, alpha));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawOval(x + 1, y + 1 + offsetY, 30, 30);
            g2d.setStroke(new BasicStroke(1));
        }
    }
}