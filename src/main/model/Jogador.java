package main.model;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Classe Jogador — representa o personagem controlado pelo jogador.
 *
 * Esta classe estende a classe abstrata Entidade, demonstrando o conceito
 * de HERANÇA da Programação Orientada a Objetos. Ao herdar de Entidade,
 * o Jogador recebe automaticamente os atributos de posição (x, y) e
 * dimensões (largura, altura), além do método getBounds() para detecção
 * de colisão, sem necessidade de reescrever esse código.
 *
 * O Jogador implementa os métodos abstratos atualizar() e desenhar()
 * herdados de Entidade, demonstrando o conceito de POLIMORFISMO — cada
 * subclasse de Entidade implementa esses métodos de forma diferente,
 * de acordo com seu comportamento específico.
 *
 * CONCEITOS DE POO APLICADOS:
 * - Herança: estende a classe abstrata Entidade
 * - Polimorfismo: implementa atualizar() e desenhar() de forma específica
 * - Encapsulamento: atributos privados acessados por métodos públicos
 */
public class Jogador extends Entidade {

    /**
     * Velocidade de movimentação do personagem em pixels por quadro.
     * Declarada como constante (final) pois não deve ser alterada
     * durante a execução do jogo.
     */
    private static final int VELOCIDADE = 5;

    /**
     * Flags de direção do movimento do personagem.
     * Cada atributo representa uma tecla de movimento (W, S, A, D).
     * Quando verdadeiro, indica que a tecla correspondente está pressionada.
     * São atualizados pelo KeyAdapter da classe EcoMetropole a cada
     * evento de teclado recebido.
     */
    public boolean up, down, left, right;

    /**
     * Imagem pre-renderizada do sprite do personagem.
     * O sprite é gerado uma única vez no construtor e reutilizado
     * a cada quadro, evitando o custo computacional de regenerá-lo
     * a cada chamada do método desenhar().
     */
    private BufferedImage sprite;

    /**
     * Contador de quadros utilizado para controlar as animações.
     * Incrementado a cada chamada do método atualizar(), é utilizado
     * para calcular o deslocamento das pernas do personagem usando
     * a função seno, criando um movimento oscilante natural.
     */
    private int animFrame = 0;

    /**
     * Construtor da classe Jogador.
     *
     * Inicializa o personagem na posição fornecida com dimensões de
     * 56x56 pixels e gera o sprite do personagem programaticamente.
     *
     * @param x posição horizontal inicial em pixels
     * @param y posição vertical inicial em pixels
     */
    public Jogador(int x, int y) {
        super(x, y, 56, 56);
        sprite = criarSprite();
    }

    /**
     * Gera o sprite do personagem programaticamente usando Graphics2D.
     *
     * O sprite é desenhado em uma imagem BufferedImage de 56x56 pixels,
     * composto por formas geométricas simples como óvalos, retângulos
     * arredondados e arcos. Essa abordagem elimina a necessidade de
     * arquivos de imagem externos, tornando o projeto completamente
     * autocontido.
     *
     * O personagem é composto por: sombra, pernas, sapatos, corpo,
     * detalhes da roupa, cinto, pescoço, cabeça, cabelo, orelhas,
     * olhos, sobrancelhas, boca e braços.
     *
     * @return BufferedImage contendo o sprite do personagem
     */
    private BufferedImage criarSprite() {
        BufferedImage img = new BufferedImage(56, 56,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Ativa antialiasing para bordas suaves
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Sombra elíptica abaixo do personagem
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(6, 46, 44, 10);

        // Pernas — desenhadas antes do corpo para ficarem atrás
        g.setColor(new Color(30, 80, 160));
        g.fillRoundRect(14, 38, 10, 14, 5, 5);
        g.fillRoundRect(32, 38, 10, 14, 5, 5);

        // Sapatos
        g.setColor(new Color(40, 40, 40));
        g.fillRoundRect(12, 48, 13, 7, 4, 4);
        g.fillRoundRect(30, 48, 13, 7, 4, 4);

        // Corpo principal
        g.setColor(new Color(30, 144, 255));
        g.fillRoundRect(10, 20, 36, 24, 14, 14);

        // Bolso da roupa
        g.setColor(new Color(20, 110, 200));
        g.fillRoundRect(14, 26, 10, 8, 4, 4);
        g.setColor(new Color(10, 80, 160));
        g.drawRoundRect(14, 26, 10, 8, 4, 4);

        // Cinto com fivela dourada
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

        // Olhos — branco, íris azul, pupila preta e brilho
        g.setColor(Color.WHITE);
        g.fillOval(20, 13, 8, 7);
        g.fillOval(30, 13, 8, 7);
        g.setColor(new Color(50, 100, 200));
        g.fillOval(22, 14, 5, 5);
        g.fillOval(32, 14, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(23, 15, 3, 3);
        g.fillOval(33, 15, 3, 3);
        g.setColor(Color.WHITE);
        g.fillOval(24, 15, 2, 2);
        g.fillOval(34, 15, 2, 2);

        // Sobrancelhas com traço arredondado
        g.setColor(new Color(80, 50, 20));
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.drawLine(20, 11, 27, 12);
        g.drawLine(30, 12, 37, 11);
        g.setStroke(new BasicStroke(1));

        // Boca sorrindo
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

        // Contorno do corpo
        g.setColor(new Color(10, 90, 180));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(10, 20, 36, 24, 14, 14);
        g.setStroke(new BasicStroke(1));

        // Libera os recursos do contexto gráfico temporário
        g.dispose();
        return img;
    }

    /**
     * Atualiza o estado do jogador a cada quadro do game loop.
     *
     * Implementação do método abstrato herdado de Entidade.
     * Calcula o deslocamento do personagem com base nas flags de direção
     * ativas, aplica o movimento e garante que o personagem não ultrapasse
     * os limites da tela.
     *
     * O movimento é diagonal — cada tecla contribui com deslocamento
     * em duas direções simultaneamente, criando um movimento isométrico:
     * W = nordeste, S = sudoeste, A = noroeste, D = sudeste.
     *
     * O método Math.max e Math.min são usados para limitar a posição
     * do personagem dentro dos limites da área de jogo.
     */
    @Override
    public void atualizar() {
        int dx = 0, dy = 0;

        // Calcula deslocamento com base nas teclas pressionadas
        if (up)    { dx += VELOCIDADE; dy -= VELOCIDADE; } // nordeste
        if (down)  { dx -= VELOCIDADE; dy += VELOCIDADE; } // sudoeste
        if (left)  { dx -= VELOCIDADE; dy -= VELOCIDADE; } // noroeste
        if (right) { dx += VELOCIDADE; dy += VELOCIDADE; } // sudeste

        // Aplica o deslocamento à posição atual
        x += dx;
        y += dy;

        // Limita a posição dentro dos bordas da tela
        x = Math.max(0, Math.min(920, x));
        y = Math.max(0, Math.min(620, y));

        // Incrementa o contador de animação
        animFrame++;
    }

    /**
     * Define o estado da tecla W (movimento nordeste).
     * @param b true se a tecla está pressionada, false se foi liberada
     */
    public void setUp(boolean b)    { up = b; }

    /**
     * Define o estado da tecla S (movimento sudoeste).
     * @param b true se a tecla está pressionada, false se foi liberada
     */
    public void setDown(boolean b)  { down = b; }

    /**
     * Define o estado da tecla A (movimento noroeste).
     * @param b true se a tecla está pressionada, false se foi liberada
     */
    public void setLeft(boolean b)  { left = b; }

    /**
     * Define o estado da tecla D (movimento sudeste).
     * @param b true se a tecla está pressionada, false se foi liberada
     */
    public void setRight(boolean b) { right = b; }

    /**
     * Retorna o retângulo de colisão do jogador.
     *
     * Sobrescreve o método getBounds() da superclasse Entidade para
     * definir uma hitbox menor que o sprite visual do personagem.
     * Isso torna o jogo mais justo, pois pequenos erros de posicionamento
     * não resultam em colisão imediata com os inimigos.
     *
     * @return Rectangle representando a área de colisão do jogador
     */
    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 10, y + 20, 36, 30);
    }

    /**
     * Renderiza o jogador na tela a cada quadro do game loop.
     *
     * Implementação do método abstrato herdado de Entidade.
     * Primeiro desenha as pernas animadas (que ficam atrás do sprite),
     * depois desenha o sprite do personagem sobre elas.
     *
     * A animação das pernas é calculada usando a função seno aplicada
     * ao contador animFrame, criando um movimento oscilante natural
     * que simula a caminhada do personagem. O deslocamento alterna
     * entre as duas pernas de forma oposta (legOffset e -legOffset).
     *
     * @param g2d contexto gráfico 2D fornecido pelo sistema Swing
     */
    @Override
    public void desenhar(Graphics2D g2d) {
        // Calcula deslocamento das pernas usando função seno
        // Math.sin retorna valores entre -1 e +1, multiplicados por 5
        // para criar um deslocamento de até 5 pixels para cada lado
        int legOffset = (int)(Math.sin(animFrame * 0.3) * 5);

        // Desenha pernas animadas (ficam atrás do sprite)
        g2d.setColor(new Color(30, 80, 160));
        g2d.fillRoundRect(x + 14 + legOffset, y + 38, 10, 14, 5, 5);
        g2d.fillRoundRect(x + 32 - legOffset, y + 38, 10, 14, 5, 5);

        // Desenha sapatos animados
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRoundRect(x + 12 + legOffset, y + 48, 13, 7, 4, 4);
        g2d.fillRoundRect(x + 30 - legOffset, y + 48, 13, 7, 4, 4);

        // Desenha o sprite pre-renderizado sobre as pernas
        g2d.drawImage(sprite, x, y, largura, altura, null);
    }
}