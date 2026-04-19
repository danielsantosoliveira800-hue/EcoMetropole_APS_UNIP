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

/**
 * Classe GerenciadorJogo — núcleo lógico do jogo.
 *
 * Esta classe é responsável por coordenar todos os aspectos do jogo:
 * atualização das entidades, detecção de colisões, spawn de itens,
 * gerenciamento dos índices de sustentabilidade e poluição, renderização
 * de todos os elementos visuais do cenário e transições de estado.
 *
 * A lista de entidades utiliza o tipo pai Entidade para armazenar
 * objetos de tipos diferentes (Jogador e Item), demonstrando o conceito
 * de POLIMORFISMO — o código itera sobre a lista chamando os métodos
 * atualizar() e desenhar() sem precisar saber o tipo específico de cada
 * objeto, confiando que cada um executará sua própria implementação.
 *
 * CONCEITOS DE POO APLICADOS:
 * - Polimorfismo: lista de Entidade armazena Jogador e Item
 * - Encapsulamento: índices e estado do jogo protegidos por métodos
 * - Separação de responsabilidades: lógica separada da interface gráfica
 */
public class GerenciadorJogo {

    /**
     * Lista de entidades do jogo — jogador e itens coletáveis.
     * Utiliza o tipo pai Entidade para demonstrar polimorfismo:
     * objetos de tipos diferentes são tratados de forma uniforme.
     */
    private ArrayList<Entidade> entidades;

    /**
     * Referência direta ao jogador para acesso rápido.
     * Necessária pois o jogador recebe tratamento especial —
     * nunca é removido da lista e tem seus índices atualizados
     * diretamente pelo GerenciadorJogo.
     */
    private Jogador jogador;

    /**
     * Referência à nuvem tóxica — inimigo do jogo.
     * Gerenciada separadamente da lista de entidades para que
     * o método atualizar(alvoX, alvoY) possa ser chamado diretamente,
     * passando as coordenadas do jogador como alvo de perseguição.
     */
    private NuvemToxica nuvemToxica;

    /**
     * Referência à tela de fim de jogo.
     * Gerencia a exibição da tela de vitória ou derrota e
     * detecta os cliques nos botões de reiniciar e fechar.
     */
    private TelaFim telaFim;

    /**
     * Contador de quadros desde o início da partida.
     * Utilizado para controlar o spawn de itens (a cada 120 quadros)
     * e o dano da nuvem (a cada 60 quadros).
     */
    private int frameCount = 0;

    /**
     * Índice de sustentabilidade da metrópole (0 a 100).
     * Aumenta ao coletar itens verdes, diminui ao coletar itens laranjas
     * e ao receber dano da nuvem. Vitória quando atinge 100.
     */
    private int sustentabilidade = 50;

    /**
     * Índice de poluição da metrópole (0 a 100).
     * Aumenta ao coletar itens laranjas e ao receber dano da nuvem.
     * Diminui ao coletar itens verdes. Derrota quando atinge 100.
     */
    private int poluicao = 20;

    /**
     * Pontuação acumulada pelo jogador.
     * Aumenta em 10 pontos a cada item verde coletado.
     */
    private int pontuacao = 0;

    /**
     * Contador de quadros do efeito de flash de dano.
     * Quando maior que zero, a tela pisca em vermelho.
     * Decrementado a cada quadro até chegar a zero.
     */
    private int danoFlash = 0;

    /**
     * Duração do efeito de flash de dano em quadros (20 quadros = ~0.33s).
     */
    private static final int FLASH_DURACAO = 20;

    /**
     * Gerador de números aleatórios para spawn de itens.
     */
    private Random random = new Random();

    // Paleta de cores do terreno — declaradas como constantes
    // para evitar a criação de novos objetos Color a cada quadro
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

    /** Tamanho de cada tile do terreno em pixels. */
    private static final int TILE = 32;

    // Paleta de cores do cenário urbano
    private static final Color ASFALTO        = new Color(60,  60,  65);
    private static final Color ASFALTO_CLARO  = new Color(80,  80,  85);
    private static final Color CALCADA        = new Color(180, 170, 155);
    private static final Color CALCADA_ESCURA = new Color(150, 140, 125);
    private static final Color FAIXA          = new Color(240, 220, 80);

    /**
     * Construtor da classe GerenciadorJogo.
     *
     * Inicializa todos os objetos do jogo: lista de entidades, jogador,
     * nuvem tóxica e tela de fim. O jogador é adicionado à lista de
     * entidades, mas a nuvem é gerenciada separadamente.
     */
    public GerenciadorJogo() {
        entidades = new ArrayList<>();
        jogador = new Jogador(400, 300);
        nuvemToxica = new NuvemToxica(100, 100);
        telaFim = new TelaFim();
        entidades.add(jogador);
    }

    /**
     * Reinicia o jogo para uma nova partida.
     *
     * Limpa a lista de entidades, cria novos objetos para o jogador,
     * nuvem e tela de fim, e reseta todos os índices para os valores
     * iniciais. Chamado quando o jogador clica em Reiniciar.
     */
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

    /**
     * Atualiza o estado do jogo a cada quadro do game loop.
     *
     * Sequência de atualização:
     * 1. Verifica se o jogo está pausado (tela de fim ativa)
     * 2. Atualiza o jogador com base nas teclas pressionadas
     * 3. Atualiza a nuvem com as coordenadas do jogador como alvo
     * 4. Verifica colisão entre jogador e nuvem (dano por contato)
     * 5. Faz spawn de novos itens a cada 2 segundos (120 quadros)
     * 6. Verifica colisões entre jogador e itens
     * 7. Verifica condições de vitória e derrota
     */
    public void atualizar() {
        // Pausa o jogo quando a tela de fim está ativa
        if (telaFim.isAtiva()) return;

        frameCount++;

        // Atualiza posição do jogador
        jogador.atualizar();

        // Nuvem persegue o jogador passando suas coordenadas como alvo
        nuvemToxica.atualizar(jogador.getX(), jogador.getY());

        // Verifica colisão entre jogador e nuvem
        // Dano aplicado uma vez por segundo (60 quadros)
        if (nuvemToxica.getBounds().intersects(jogador.getBounds())) {
            if (frameCount % 60 == 0) {
                poluicao         = Math.min(100, poluicao + 5);
                sustentabilidade = Math.max(0,   sustentabilidade - 3);
                danoFlash        = FLASH_DURACAO;
                GerenciadorSom.dano(); // feedback auditivo do dano
            }
        }

        // Decrementa o contador do flash de dano
        if (danoFlash > 0) danoFlash--;

        // Spawn de novo item a cada 2 segundos (120 quadros a 60 FPS)
        if (frameCount % 120 == 0) {
            Item.Tipo tipo = random.nextBoolean()
                    ? Item.Tipo.POSITIVO : Item.Tipo.NEGATIVO;
            int x = random.nextInt(700) + 50;
            int y = random.nextInt(500) + 120; // abaixo dos prédios
            entidades.add(new Item(x, y, tipo));
        }

        checarColisoes();

        // Verifica condições de vitória e derrota
        if (sustentabilidade >= 100) {
            GerenciadorSom.vitoria();
            telaFim.setEstado(TelaFim.Estado.VITORIA);
        } else if (poluicao >= 100) {
            GerenciadorSom.derrota();
            telaFim.setEstado(TelaFim.Estado.DERROTA);
        }
    }

    /**
     * Verifica colisões entre o jogador e os itens coletáveis.
     *
     * Utiliza o método removeIf() da ArrayList, que remove da lista
     * todos os elementos para os quais o predicado retorna verdadeiro.
     * O predicado verifica se a entidade é o jogador ou a nuvem
     * (que nunca são removidos) e se há interseção entre os retângulos
     * de colisão do jogador e da entidade.
     *
     * Quando uma colisão com item é detectada, os efeitos são aplicados
     * aos índices do jogo e o som correspondente é reproduzido.
     */
    private void checarColisoes() {
        entidades.removeIf(entidade -> {
            // Jogador e nuvem nunca são removidos
            if (entidade == jogador) return false;

            // Verifica interseção dos retângulos de colisão
            if (jogador.getBounds().intersects(entidade.getBounds())) {
                if (entidade instanceof Item item) {
                    int valor = item.getValor();
                    if (valor > 0) {
                        // Item positivo: aumenta sustentabilidade
                        sustentabilidade = Math.min(100,
                                sustentabilidade + valor);
                        poluicao = Math.max(0, poluicao - 5);
                        pontuacao += 10;
                        GerenciadorSom.itemVerde();
                    } else {
                        // Item negativo: aumenta poluição
                        poluicao = Math.min(100, poluicao - valor);
                        sustentabilidade = Math.max(0,
                                sustentabilidade + valor);
                        GerenciadorSom.itemLaranja();
                    }
                }
                return true; // remove o item da lista
            }
            return false;
        });
    }

    /**
     * Ordena as entidades por posição vertical (coordenada Y).
     *
     * Entidades com Y maior são desenhadas por cima das entidades
     * com Y menor, criando a ilusão de profundidade — objetos mais
     * abaixo na tela parecem estar na frente dos objetos mais acima.
     * Esse efeito é chamado de painter's algorithm (algoritmo do pintor).
     */
    public void ordenarEntidadesPorY() {
        entidades.sort((a, b) -> Integer.compare(a.getY(), b.getY()));
    }

    /**
     * Renderiza todos os elementos visuais do jogo na tela.
     *
     * Os elementos são desenhados em camadas, da mais distante para
     * a mais próxima do observador, garantindo sobreposição correta:
     * 1. Fundo do céu (azul escuro)
     * 2. Prédios ao fundo
     * 3. Terreno (tiles de grama, terra e pedra)
     * 4. Estradas e calçadas
     * 5. Árvores
     * 6. Postes de iluminação
     * 7. Entidades (jogador e itens)
     * 8. Nuvem tóxica (sempre por cima das entidades)
     * 9. Efeito de flash de dano
     * 10. HUD (interface do jogador)
     * 11. Tela de fim (se ativa)
     *
     * @param g2d    contexto gráfico 2D fornecido pelo sistema Swing
     * @param width  largura da tela em pixels
     * @param height altura da tela em pixels
     */
    public void desenhar(Graphics2D g2d, int width, int height) {
        // Camada 1: fundo do céu para a área dos prédios
        g2d.setColor(new Color(40, 50, 70));
        g2d.fillRect(0, 0, width, 120);

        // Camadas 2-6: cenário da metrópole
        desenharPredios(g2d, width);
        desenharChaoIsometrico(g2d, width, height);
        desenharEstradas(g2d, width, height);
        desenharArvores(g2d);
        desenharPostes(g2d, width, height);

        // Camada 7: entidades — cópia da lista evita ConcurrentModificationException
        // (a lista pode ser modificada pela thread do game loop enquanto
        // a thread de renderização do Swing está iterando sobre ela)
        List<Entidade> copia = new ArrayList<>(entidades);
        for (Entidade e : copia) {
            // Atualiza animação dos itens durante o desenho
            if (e instanceof Item) ((Item) e).atualizar();
            e.desenhar(g2d);
        }

        // Camada 8: nuvem tóxica (sempre na frente das entidades)
        nuvemToxica.desenhar(g2d);

        // Camada 9: efeito de flash vermelho ao receber dano
        if (danoFlash > 0) {
            // Transparência proporcional ao tempo restante do flash
            int alpha = (int)(120 * ((double) danoFlash / FLASH_DURACAO));
            g2d.setColor(new Color(220, 0, 0, alpha));
            g2d.fillRect(0, 0, width, height);
        }

        // Camada 10: HUD com barras e pontuação
        desenharHUD(g2d, width, height);

        // Camada 11: tela de fim (vitória ou derrota)
        if (telaFim.isAtiva()) {
            telaFim.desenhar(g2d, width, height);
        }
    }

    /**
     * Renderiza os prédios ao fundo da cena.
     *
     * Cada prédio é definido por um array de 6 valores:
     * [x, largura, altura, r, g, b]
     *
     * Os prédios são posicionados com py = 120 - ph para que
     * cresçam de baixo para cima a partir da linha do horizonte (y=120).
     * Cada prédio possui corpo, lateral (efeito 3D), topo, janelas
     * e antena ocasional.
     *
     * @param g2d   contexto gráfico 2D
     * @param width largura da tela
     */
    private void desenharPredios(Graphics2D g2d, int width) {
        // [x, largura, altura, vermelho, verde, azul]
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

            // py calculado para o prédio crescer de baixo para cima
            // a partir da linha do horizonte (y = 120)
            int py = 120 - ph;

            // Corpo principal do prédio
            g2d.setColor(new Color(p[3], p[4], p[5]));
            g2d.fillRect(px, py, pw, ph);

            // Fachada lateral direita — cor mais escura cria efeito 3D
            g2d.setColor(new Color(
                    Math.max(0, p[3] - 25),
                    Math.max(0, p[4] - 25),
                    Math.max(0, p[5] - 25)));
            g2d.fillRect(px + pw, py + 4, 8, ph - 4);

            // Topo do prédio — cor mais clara
            g2d.setColor(new Color(
                    Math.min(255, p[3] + 20),
                    Math.min(255, p[4] + 20),
                    Math.min(255, p[5] + 20)));
            g2d.fillRect(px, py, pw + 8, 5);

            // Janelas iluminadas ou apagadas
            desenharJanelas(g2d, px, py + 8, pw, ph - 8);

            // Antena em prédios cujo x é divisível por 3
            if (px % 3 == 0) {
                g2d.setColor(new Color(80, 80, 90));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(px + pw / 2, py, px + pw / 2, py - 16);
                g2d.setColor(new Color(220, 50, 50));
                g2d.fillOval(px + pw / 2 - 3, py - 19, 6, 6);
                g2d.setStroke(new BasicStroke(1));
            }
        }

        // Linha de separação entre prédios e terreno
        g2d.setColor(new Color(30, 35, 50));
        g2d.fillRect(0, 118, width, 4);
    }

    /**
     * Renderiza as janelas de um prédio em grade regular.
     *
     * As janelas são distribuídas em colunas e linhas calculadas
     * com base nas dimensões do prédio. Cada janela é aleatoriamente
     * iluminada (amarela) ou apagada (azul escuro), usando a posição
     * como semente para garantir consistência entre quadros.
     *
     * @param g2d contexto gráfico 2D
     * @param px  posição horizontal do prédio
     * @param py  posição vertical das janelas
     * @param pw  largura do prédio
     * @param ph  altura disponível para janelas
     */
    private void desenharJanelas(Graphics2D g2d,
                                 int px, int py,
                                 int pw, int ph) {
        int cols = pw / 14; // número de colunas de janelas
        int rows = ph / 16; // número de linhas de janelas

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                int wx = px + 5 + c * 14;
                int wy = py + 4 + r * 16;

                // Janela acesa ou apagada baseada na posição (determinístico)
                boolean acesa = (px + c * 7 + r * 13) % 3 != 0;
                g2d.setColor(acesa
                        ? new Color(255, 240, 160, 200) // amarela (acesa)
                        : new Color(40,  50,  70,  180)); // azul (apagada)
                g2d.fillRect(wx, wy, 8, 10);

                // Moldura da janela
                g2d.setColor(new Color(60, 70, 90, 120));
                g2d.drawRect(wx, wy, 8, 10);
            }
        }
    }

    /**
     * Renderiza as estradas e calçadas do cenário.
     *
     * Cria uma estrada horizontal central e uma estrada vertical,
     * formando um cruzamento. Cada estrada possui calçadas nas laterais,
     * faixas de pedestre e linhas de centro tracejadas.
     *
     * @param g2d    contexto gráfico 2D
     * @param width  largura da tela
     * @param height altura da tela
     */
    private void desenharEstradas(Graphics2D g2d, int width, int height) {
        // Estrada horizontal
        g2d.setColor(ASFALTO);
        g2d.fillRect(0, 310, width, 60);
        g2d.setColor(ASFALTO_CLARO);
        g2d.fillRect(0, 312, width, 2);
        g2d.fillRect(0, 366, width, 2);

        // Faixa de pedestre no cruzamento
        g2d.setColor(FAIXA);
        for (int fx = 460; fx < 540; fx += 14) {
            g2d.fillRect(fx, 310, 8, 60);
        }

        // Calçadas horizontais com detalhes de pedras
        g2d.setColor(CALCADA);
        g2d.fillRect(0, 295, width, 15);
        g2d.fillRect(0, 370, width, 15);
        g2d.setColor(CALCADA_ESCURA);
        for (int cx = 0; cx < width; cx += 20) {
            g2d.drawRect(cx, 295, 20, 15);
            g2d.drawRect(cx, 370, 20, 15);
        }

        // Estrada vertical
        g2d.setColor(ASFALTO);
        g2d.fillRect(460, 120, 80, height);
        g2d.setColor(ASFALTO_CLARO);
        g2d.fillRect(462, 120, 2, height);
        g2d.fillRect(536, 120, 2, height);

        // Linhas de centro tracejadas (estilo de rodovia)
        g2d.setColor(FAIXA);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10, new float[]{12, 10}, 0));
        g2d.drawLine(500, 120, 500, height);   // vertical
        g2d.drawLine(0,   340, 460, 340);      // horizontal esquerda
        g2d.drawLine(540, 340, width, 340);    // horizontal direita
        g2d.setStroke(new BasicStroke(1));
    }

    /**
     * Renderiza as árvores espalhadas pelo mapa.
     *
     * Cada árvore é composta por três camadas de copa (escura, média e
     * clara) para criar profundidade, um tronco e uma sombra elíptica.
     * As posições são fixas para garantir consistência visual.
     */
    private void desenharArvores(Graphics2D g2d) {
        // [x, y] de cada árvore no mapa
        int[][] arvores = {
                { 80,  200}, {200, 420}, {350, 180}, {620, 430},
                {750, 200}, {880, 410}, {140, 450}, {420, 500},
                { 60, 380}, {300, 250}, {700, 480}, {820, 260},
        };

        for (int[] a : arvores) {
            int ax = a[0], ay = a[1];

            // Sombra elíptica no chão
            g2d.setColor(new Color(0, 0, 0, 40));
            g2d.fillOval(ax - 12, ay + 18, 30, 10);

            // Tronco
            g2d.setColor(new Color(90, 55, 25));
            g2d.fillRoundRect(ax - 4, ay + 10, 10, 20, 4, 4);

            // Copa — três camadas para criar volume e profundidade
            g2d.setColor(new Color(20, 100, 20));   // camada escura (fundo)
            g2d.fillOval(ax - 18, ay,      38, 32);
            g2d.setColor(new Color(34, 139, 34));   // camada média
            g2d.fillOval(ax - 15, ay - 8,  32, 28);
            g2d.setColor(new Color(60, 179, 60));   // camada clara (frente)
            g2d.fillOval(ax - 10, ay - 14, 22, 22);

            // Brilho no topo da copa
            g2d.setColor(new Color(150, 255, 100, 80));
            g2d.fillOval(ax - 5, ay - 12, 10, 8);
        }
    }

    /**
     * Renderiza os postes de iluminação nas calçadas.
     *
     * Cada poste é composto por haste vertical, braço horizontal,
     * luminária e halo de luz amarelo semitransparente.
     *
     * @param g2d    contexto gráfico 2D
     * @param width  largura da tela (não utilizado, mantido por simetria)
     * @param height altura da tela (não utilizado, mantido por simetria)
     */
    private void desenharPostes(Graphics2D g2d, int width, int height) {
        // [x, y] de cada poste — posicionados nas calçadas
        int[][] postes = {
                {100, 290}, {280, 290}, {650, 290}, {830, 290},
                {100, 370}, {280, 370}, {650, 370}, {830, 370},
        };

        for (int[] p : postes) {
            int px = p[0], py = p[1];

            // Sombra do poste no chão
            g2d.setColor(new Color(0, 0, 0, 40));
            g2d.fillOval(px - 4, py + 2, 10, 4);

            // Haste e braço do poste
            g2d.setColor(new Color(70, 70, 75));
            g2d.setStroke(new BasicStroke(3,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(px, py, px, py - 45);        // haste vertical
            g2d.drawLine(px, py - 45, px + 14, py - 45); // braço horizontal
            g2d.setStroke(new BasicStroke(1));

            // Luminária
            g2d.setColor(new Color(50, 50, 55));
            g2d.fillRoundRect(px + 8, py - 50, 14, 8, 4, 4);

            // Luz amarela acesa
            g2d.setColor(new Color(255, 230, 100, 220));
            g2d.fillOval(px + 10, py - 48, 10, 6);

            // Halo de luz ao redor da luminária
            g2d.setColor(new Color(255, 230, 100, 40));
            g2d.fillOval(px + 4, py - 54, 22, 18);
        }
    }

    /**
     * Renderiza o terreno usando tiles gerados proceduralmente.
     *
     * O terreno é dividido em tiles de TILE x TILE pixels. Para cada
     * tile, uma semente determinística é calculada com base na posição,
     * garantindo que o mesmo tile sempre tenha a mesma aparência.
     * A semente determina o tipo do tile (grama, terra ou pedra) e
     * os detalhes visuais de cada tile.
     *
     * O antialiasing é desativado durante o desenho dos tiles para
     * preservar a estética pixel art e reativado após.
     *
     * @param g2d    contexto gráfico 2D
     * @param width  largura da tela
     * @param height altura da tela
     */
    private void desenharChaoIsometrico(Graphics2D g2d,
                                        int width, int height) {
        // Desativa antialiasing para estética pixel art
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        for (int tx = 0; tx < width; tx += TILE) {
            // Tiles começam em y=120 para não cobrir os prédios
            for (int ty = 120; ty < height; ty += TILE) {
                // Semente determinística baseada na posição do tile
                int seed = (tx / TILE) * 31 + (ty / TILE) * 17;
                int tipo = seed % 10;

                // Distribuição: 60% grama, 20% terra, 20% pedra
                if (tipo <= 5)      desenharTileGrama(g2d, tx, ty, seed);
                else if (tipo <= 7) desenharTileTerra(g2d, tx, ty, seed);
                else                desenharTilePedra(g2d, tx, ty, seed);
            }
        }

        // Reativa antialiasing para o restante do jogo
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * Renderiza um tile de grama com variação de cor, tufos e flores.
     *
     * A variação de cor é criada por sub-blocos de 4x4 pixels com cores
     * ligeiramente diferentes, criando textura pixel art. Tufos de grama
     * e flores são posicionados usando a semente como offset determinístico.
     *
     * @param g2d  contexto gráfico 2D
     * @param tx   posição horizontal do tile
     * @param ty   posição vertical do tile
     * @param seed semente para variação determinística
     */
    private void desenharTileGrama(Graphics2D g2d,
                                   int tx, int ty, int seed) {
        // Base verde
        g2d.setColor(GRAMA_BASE);
        g2d.fillRect(tx, ty, TILE, TILE);

        // Variação de cor em sub-blocos 4x4 (textura pixel art)
        for (int px = 0; px < TILE; px += 4) {
            for (int py = 0; py < TILE; py += 4) {
                int v = (seed + px * 3 + py * 7) % 6;
                if (v == 0)      g2d.setColor(GRAMA_CLARA);
                else if (v == 1) g2d.setColor(GRAMA_ESCURA);
                else             continue;
                g2d.fillRect(tx + px, ty + py, 4, 4);
            }
        }

        // Borda escura sutil separando os tiles
        g2d.setColor(GRAMA_ESCURA);
        g2d.drawRect(tx, ty, TILE - 1, TILE - 1);

        // Tufo de grama com posição determinística
        int tufoX = tx + (seed % 20) + 4;
        int tufoY = ty + ((seed / 3) % 20) + 4;
        g2d.setColor(GRAMA_CLARA);
        g2d.fillRect(tufoX,     tufoY,     2, 5);
        g2d.fillRect(tufoX + 3, tufoY + 1, 2, 4);
        g2d.fillRect(tufoX + 6, tufoY,     2, 6);

        // Flores ocasionais (1 em cada 3 tiles de grama)
        if (seed % 3 == 0) {
            int fx = tx + (seed % 18) + 7;
            int fy = ty + ((seed * 3) % 18) + 7;
            Color cor = (seed % 2 == 0) ? FLOR_AMARELA : FLOR_ROSA;
            g2d.setColor(MUSGO);
            g2d.fillRect(fx + 1, fy + 3, 2, 4); // caule
            g2d.setColor(cor);
            g2d.fillRect(fx,     fy + 1, 4, 2); // pétalas horizontais
            g2d.fillRect(fx + 1, fy,     2, 4); // pétalas verticais
            g2d.setColor(FLOR_AMARELA);
            g2d.fillRect(fx + 1, fy + 1, 2, 2); // centro
        }

        // Manchas de musgo ocasionais (1 em cada 5 tiles)
        if (seed % 5 == 0) {
            int mx = tx + (seed % 22) + 3;
            int my = ty + ((seed * 2) % 22) + 3;
            g2d.setColor(MUSGO);
            g2d.fillOval(mx, my, 6, 4);
        }
    }

    /**
     * Renderiza um tile de terra com textura granulada e pedrinhas.
     *
     * @param g2d  contexto gráfico 2D
     * @param tx   posição horizontal do tile
     * @param ty   posição vertical do tile
     * @param seed semente para variação determinística
     */
    private void desenharTileTerra(Graphics2D g2d,
                                   int tx, int ty, int seed) {
        // Base marrom
        g2d.setColor(TERRA);
        g2d.fillRect(tx, ty, TILE, TILE);

        // Textura granulada com sub-blocos 3x3
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

        // Borda escura
        g2d.setColor(new Color(70, 45, 20));
        g2d.drawRect(tx, ty, TILE - 1, TILE - 1);

        // Pedrinhas ocasionais
        if (seed % 4 == 0) {
            int px = tx + (seed % 20) + 5;
            int py = ty + ((seed * 4) % 20) + 5;
            g2d.setColor(PEDRA);
            g2d.fillOval(px, py, 5, 3);
            g2d.setColor(PEDRA_CLARA);
            g2d.fillOval(px, py, 2, 1);
        }
    }

    /**
     * Renderiza um tile de pedra com textura e rachaduras.
     *
     * @param g2d  contexto gráfico 2D
     * @param tx   posição horizontal do tile
     * @param ty   posição vertical do tile
     * @param seed semente para variação determinística
     */
    private void desenharTilePedra(Graphics2D g2d,
                                   int tx, int ty, int seed) {
        // Base cinza
        g2d.setColor(PEDRA);
        g2d.fillRect(tx, ty, TILE, TILE);

        // Textura em blocos 5x5
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

        // Rachadura diagonal
        g2d.setColor(new Color(70, 70, 65));
        int rx = tx + (seed % 10) + 5;
        int ry = ty + ((seed * 2) % 10) + 5;
        g2d.drawLine(rx,     ry,     rx + 8,  ry + 6);
        g2d.drawLine(rx + 8, ry + 6, rx + 10, ry + 12);

        // Borda escura
        g2d.setColor(new Color(80, 80, 74));
        g2d.drawRect(tx, ty, TILE - 1, TILE - 1);

        // Manchas de musgo nas pedras
        if (seed % 3 == 0) {
            g2d.setColor(MUSGO);
            g2d.fillRect(tx + 2,        ty + 2,        6, 3);
            g2d.fillRect(tx + TILE - 8, ty + TILE - 5, 5, 3);
        }
    }

    /**
     * Renderiza o HUD (Heads-Up Display) do jogo.
     *
     * O HUD exibe três informações essenciais para o jogador:
     * - Barra de sustentabilidade (canto inferior esquerdo)
     * - Barra de poluição (canto inferior direito)
     * - Contador de pontos (centro inferior)
     *
     * As barras mudam de cor conforme os valores:
     * - Sustentabilidade: amarela abaixo de 80%, verde acima
     * - Poluição: laranja abaixo de 80%, vermelha acima
     *
     * @param g2d    contexto gráfico 2D
     * @param width  largura da tela
     * @param height altura da tela
     */
    private void desenharHUD(Graphics2D g2d, int width, int height) {
        // Fundo semitransparente do HUD
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(15, height - 130, width - 30, 105, 25, 25);

        g2d.setFont(new Font("Arial", Font.BOLD, 26));

        // Barra de sustentabilidade
        g2d.setColor(sustentabilidade >= 80 ? Color.GREEN : Color.YELLOW);
        int verdeW = (int)(170 * (sustentabilidade / 100.0));
        g2d.fillRoundRect(35, height - 105, verdeW, 30, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(35, height - 105, 170, 30, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Sustentabilidade: " + sustentabilidade + "%",
                40, height - 75);

        // Barra de poluição
        g2d.setColor(poluicao >= 80 ? Color.RED : new Color(255, 165, 0));
        int laranjaW = (int)(170 * (poluicao / 100.0));
        g2d.fillRoundRect(width - 205, height - 105, laranjaW, 30, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(width - 205, height - 105, 170, 30, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Poluicao: " + poluicao + "%",
                width - 190, height - 75);

        // Contador de pontos centralizado
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.setColor(new Color(255, 220, 50));
        String pts = "Pontos: " + pontuacao;
        int ptW = g2d.getFontMetrics().stringWidth(pts);
        g2d.drawString(pts, width / 2 - ptW / 2, height - 78);

        // Título do jogo
        g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 36));
        g2d.setColor(new Color(0, 255, 255, 200));
        g2d.drawString("EcoMetropole", width / 2 - 140, 45);

        // Instruções de controle
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("WASD mover | Fuja da nuvem! | Verde +10 | Laranja -15",
                width / 2 - 210, 70);
    }

    /**
     * Processa cliques do mouse na tela de fim de jogo.
     *
     * Verifica se o clique ocorreu sobre os botões de reiniciar ou
     * fechar e executa a ação correspondente. Chamado pelo MouseAdapter
     * da classe EcoMetropole a cada evento de clique.
     *
     * @param mx     coordenada horizontal do clique
     * @param my     coordenada vertical do clique
     * @param width  largura da tela
     * @param height altura da tela
     */
    public void clicar(int mx, int my, int width, int height) {
        if (!telaFim.isAtiva()) return;
        if (telaFim.clicouReiniciar(mx, my, width, height)) {
            reiniciar();
        } else if (telaFim.clicouFechar(mx, my, width, height)) {
            System.exit(0);
        }
    }

    // Getters para acesso externo aos dados do jogo

    /** Retorna a referência ao objeto Jogador. */
    public Jogador getJogador()      { return jogador; }

    /** Retorna o índice atual de sustentabilidade (0-100). */
    public int getSustentabilidade() { return sustentabilidade; }

    /** Retorna o índice atual de poluição (0-100). */
    public int getPoluicao()         { return poluicao; }
}