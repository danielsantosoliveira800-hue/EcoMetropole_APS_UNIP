package main.model;

import java.awt.*;

/**
 * Classe NuvemToxica — representa o inimigo do jogo.
 *
 * Esta classe estende a classe abstrata Entidade, demonstrando o conceito
 * de HERANÇA da Programação Orientada a Objetos. A nuvem tóxica representa
 * metaforicamente os efeitos da poluição atmosférica sobre a saúde humana
 * nas grandes metrópoles, perseguindo continuamente o jogador pelo mapa.
 *
 * A NuvemToxica implementa dois métodos atualizar() com assinaturas
 * diferentes, demonstrando o conceito de POLIMORFISMO por sobrecarga:
 * - atualizar() sem parâmetros: satisfaz o contrato da classe abstrata
 * - atualizar(alvoX, alvoY): implementa a lógica de perseguição com IA
 *
 * O algoritmo de perseguição utiliza aritmética vetorial e suavização
 * de movimento para criar um comportamento fluido e orgânico.
 *
 * CONCEITOS DE POO APLICADOS:
 * - Herança: estende a classe abstrata Entidade
 * - Polimorfismo: sobrecarga do método atualizar()
 * - Encapsulamento: atributos privados de posição e velocidade
 */
public class NuvemToxica extends Entidade {

    /**
     * Velocidade máxima de perseguição em pixels por quadro.
     * Controla o quão rápido a nuvem pode se mover em direção ao jogador.
     */
    private static final int VELOCIDADE = 2;

    // Cores utilizadas na renderização da nuvem
    private static final Color COR_NUVEM     = new Color(180, 80,  220);
    private static final Color COR_NUVEM_ESC = new Color(120, 40,  160);
    private static final Color COR_BRILHO    = new Color(220, 150, 255, 80);
    private static final Color COR_OLHO      = new Color(255, 50,  50);

    /**
     * Posição horizontal em ponto flutuante.
     * Armazenada como double para preservar a precisão do cálculo
     * de movimento a cada quadro. Sem isso, os pequenos deslocamentos
     * calculados pelo algoritmo de suavização seriam truncados para
     * zero ao serem convertidos para inteiro, impedindo o movimento.
     */
    private double posX;

    /**
     * Posição vertical em ponto flutuante.
     * Mesma justificativa que posX.
     */
    private double posY;

    /**
     * Componente horizontal da velocidade atual.
     * Atualizada gradualmente a cada quadro pelo algoritmo de suavização,
     * criando um movimento fluido em vez de brusco.
     */
    private double dx = 0;

    /**
     * Componente vertical da velocidade atual.
     * Mesma justificativa que dx.
     */
    private double dy = 0;

    /**
     * Contador de quadros para controle das animações.
     * Utilizado para calcular o movimento orbital das partículas.
     */
    private int animFrame = 0;

    /**
     * Fase da função seno para a animação de pulsação da nuvem.
     * Cria o efeito de expansão e contração suave do corpo da nuvem.
     */
    private double pulsacao = 0;

    /**
     * Construtor da classe NuvemToxica.
     *
     * Inicializa a nuvem na posição fornecida com dimensões de 48x36 pixels
     * e armazena a posição inicial nas variáveis de ponto flutuante.
     *
     * @param x posição horizontal inicial em pixels
     * @param y posição vertical inicial em pixels
     */
    public NuvemToxica(int x, int y) {
        super(x, y, 48, 36);
        posX = x;
        posY = y;
    }

    /**
     * Atualiza a posição da nuvem perseguindo o alvo indicado.
     *
     * Este método implementa o algoritmo de perseguição com suavização:
     * 1. Calcula o vetor diferença entre a posição do alvo e a posição atual
     * 2. Normaliza o vetor dividindo pelo seu módulo (comprimento)
     * 3. Multiplica pelo valor de VELOCIDADE para obter a velocidade desejada
     * 4. Ajusta a velocidade atual gradualmente em direção à desejada,
     *    multiplicando a diferença pelo fator de suavização (0.08)
     *
     * O fator 0.08 determina a inércia do movimento — quanto menor,
     * mais suave e lento; quanto maior, mais abrupto e rápido.
     *
     * @param alvoX coordenada horizontal do alvo (jogador)
     * @param alvoY coordenada vertical do alvo (jogador)
     */
    public void atualizar(int alvoX, int alvoY) {
        // Calcula o vetor diferença entre alvo e posição atual
        double diffX = alvoX - posX;
        double diffY = alvoY - posY;

        // Calcula o módulo (comprimento) do vetor diferença
        double dist = Math.sqrt(diffX * diffX + diffY * diffY);

        if (dist > 0) {
            // Normaliza o vetor e aplica suavização de movimento
            // O fator 0.08 controla a inércia da perseguição
            dx += (diffX / dist * VELOCIDADE - dx) * 0.08;
            dy += (diffY / dist * VELOCIDADE - dy) * 0.08;
        }

        // Atualiza posição em double para preservar precisão
        posX += dx;
        posY += dy;

        // Limita a posição dentro dos bordas da tela
        posX = Math.max(0, Math.min(950, posX));
        posY = Math.max(0, Math.min(650, posY));

        // Sincroniza coordenadas inteiras da superclasse para
        // uso no desenho e na detecção de colisão
        x = (int) posX;
        y = (int) posY;

        animFrame++;
        pulsacao += 0.1;
    }

    /**
     * Implementação vazia do método abstrato herdado de Entidade.
     *
     * Este método não realiza nenhuma ação pois a NuvemToxica requer
     * as coordenadas do alvo para se mover. Use atualizar(alvoX, alvoY)
     * para atualizar a posição com lógica de perseguição.
     */
    @Override
    public void atualizar() {
        // Sem alvo, nao faz nada — use atualizar(alvoX, alvoY)
    }

    /**
     * Retorna o retângulo de colisão da nuvem.
     *
     * Define uma hitbox menor que o sprite visual da nuvem,
     * tornando o jogo mais justo para o jogador.
     *
     * @return Rectangle representando a área de colisão da nuvem
     */
    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 8, y + 8, 32, 22);
    }

    /**
     * Renderiza a nuvem tóxica na tela a cada quadro do game loop.
     *
     * Implementação do método abstrato herdado de Entidade.
     * Desenha o corpo da nuvem em duas camadas (escura e clara) para
     * criar profundidade, adiciona olhos vermelhos brilhantes e
     * partículas orbitando ao redor do corpo.
     *
     * A pulsação é calculada usando a função seno aplicada ao atributo
     * pulsacao, criando uma variação suave no tamanho dos óvalos que
     * compõem o corpo da nuvem.
     *
     * @param g2d contexto gráfico 2D fornecido pelo sistema Swing
     */
    @Override
    public void desenhar(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Calcula variação de tamanho pela função seno (pulsação)
        int pulso = (int)(Math.sin(pulsacao) * 3);

        // Sombra elíptica no chão
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillOval(x + 4, y + largura - 8, largura - 8, 10);

        // Camada escura do corpo (dá profundidade)
        g2d.setColor(COR_NUVEM_ESC);
        g2d.fillOval(x,      y + 10, 28 + pulso, 22 + pulso);
        g2d.fillOval(x + 16, y + 6,  26 + pulso, 24 + pulso);
        g2d.fillOval(x + 26, y + 12, 22 + pulso, 20 + pulso);

        // Camada clara do corpo (fica na frente)
        g2d.setColor(COR_NUVEM);
        g2d.fillOval(x + 2,  y + 8,  26 + pulso, 20 + pulso);
        g2d.fillOval(x + 14, y + 4,  24 + pulso, 22 + pulso);
        g2d.fillOval(x + 24, y + 10, 20 + pulso, 18 + pulso);

        // Brilho no topo da nuvem
        g2d.setColor(COR_BRILHO);
        g2d.fillOval(x + 14, y + 5, 16, 10);

        // Olhos vermelhos com pupila e brilho
        g2d.setColor(COR_OLHO);
        g2d.fillOval(x + 16, y + 12, 7, 7);
        g2d.fillOval(x + 26, y + 12, 7, 7);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + 18, y + 14, 3, 3);
        g2d.fillOval(x + 28, y + 14, 3, 3);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + 19, y + 14, 2, 2);
        g2d.fillOval(x + 29, y + 14, 2, 2);

        // Partículas orbitando ao redor da nuvem
        desenharParticulas(g2d);
    }

    /**
     * Renderiza as partículas que orbitam ao redor da nuvem.
     *
     * As partículas são posicionadas usando funções trigonométricas
     * (cosseno e seno) aplicadas a ângulos que aumentam com animFrame,
     * criando o efeito de órbita circular contínua.
     *
     * @param g2d contexto gráfico 2D fornecido pelo sistema Swing
     */
    private void desenharParticulas(Graphics2D g2d) {
        // Quatro partículas posicionadas a 90 graus entre si
        int[] angulos = {0, 90, 180, 270};
        for (int i = 0; i < angulos.length; i++) {
            // Calcula ângulo atual somando animFrame para criar rotação
            double angulo = Math.toRadians(angulos[i] + animFrame * 2);

            // Calcula posição orbital usando cosseno (x) e seno (y)
            int px = x + 24 + (int)(Math.cos(angulo) * 22);
            int py = y + 18 + (int)(Math.sin(angulo) * 14);

            // Alterna tamanho entre partículas pares e ímpares
            int tamanho = 4 + (i % 2) * 3;

            g2d.setColor(new Color(180, 80, 220, 120));
            g2d.fillOval(px, py, tamanho, tamanho);
        }
    }
}