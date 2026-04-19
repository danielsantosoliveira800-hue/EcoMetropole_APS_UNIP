package main.model;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Classe abstrata Entidade — base de toda a hierarquia de objetos do jogo.
 *
 * Esta classe representa o conceito genérico de um objeto do cenário do jogo,
 * aplicando o princípio da ABSTRAÇÃO da Programação Orientada a Objetos.
 *
 * Por ser abstrata, não pode ser instanciada diretamente — ela serve apenas
 * como modelo para as subclasses: Jogador, Item e NuvemToxica.
 *
 * Todos os objetos do jogo compartilham os atributos definidos aqui:
 * posição (x, y), dimensões (largura, altura) e limites de colisão.
 *
 * CONCEITOS DE POO APLICADOS:
 * - Abstração: define o contrato que todas as entidades devem seguir
 * - Encapsulamento: atributos protegidos acessados por getters e setters
 * - Herança: Jogador, Item e NuvemToxica herdam desta classe
 * - Polimorfismo: os métodos abstratos são implementados de forma diferente
 *   em cada subclasse
 */
public abstract class Entidade {

    /**
     * Posição horizontal (x) e vertical (y) da entidade na tela.
     * Declarados como protected para que as subclasses possam
     * acessá-los diretamente sem precisar dos getters/setters,
     * o que simplifica o código de movimentação e animação.
     */
    protected int x, y;

    /**
     * Dimensões da entidade.
     * largura — tamanho horizontal em pixels
     * altura  — tamanho vertical em pixels
     * Também declarados como protected para acesso direto pelas subclasses.
     */
    protected int largura, altura;

    /**
     * Construtor da classe Entidade.
     *
     * Inicializa a posição e as dimensões da entidade com os valores
     * fornecidos pelas subclasses no momento da criação do objeto.
     *
     * @param x       posição horizontal inicial em pixels
     * @param y       posição vertical inicial em pixels
     * @param largura largura da entidade em pixels
     * @param altura  altura da entidade em pixels
     */
    public Entidade(int x, int y, int largura, int altura) {
        this.x       = x;
        this.y       = y;
        this.largura = largura;
        this.altura  = altura;
    }

    /**
     * Retorna o retângulo delimitador (hitbox) da entidade.
     *
     * Este método é utilizado pelo GerenciadorJogo para detectar colisões
     * entre o jogador e os itens ou a nuvem tóxica. O retângulo é calculado
     * com base na posição e nas dimensões da entidade.
     *
     * As subclasses podem sobrescrever este método para definir uma hitbox
     * diferente das dimensões reais da entidade — por exemplo, o Jogador
     * utiliza uma hitbox menor que seu sprite para tornar o jogo mais justo.
     *
     * @return Rectangle representando os limites de colisão da entidade
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, largura, altura);
    }

    /**
     * Retorna a posição horizontal atual da entidade.
     * @return valor inteiro da coordenada x em pixels
     */
    public int getX() { return x; }

    /**
     * Retorna a posição vertical atual da entidade.
     * @return valor inteiro da coordenada y em pixels
     */
    public int getY() { return y; }

    /**
     * Retorna a largura da entidade em pixels.
     * @return valor inteiro da largura
     */
    public int getLargura() { return largura; }

    /**
     * Retorna a altura da entidade em pixels.
     * @return valor inteiro da altura
     */
    public int getAltura() { return altura; }

    /**
     * Define a posição horizontal da entidade.
     * @param x nova coordenada horizontal em pixels
     */
    public void setX(int x) { this.x = x; }

    /**
     * Define a posição vertical da entidade.
     * @param y nova coordenada vertical em pixels
     */
    public void setY(int y) { this.y = y; }

    /**
     * Método abstrato de atualização — CONTRATO DA ABSTRAÇÃO.
     *
     * Toda subclasse OBRIGATORIAMENTE deve implementar este método,
     * definindo como o estado interno da entidade é atualizado a cada
     * quadro do game loop.
     *
     * Exemplos de uso nas subclasses:
     * - Jogador: atualiza posição com base nas teclas pressionadas
     * - Item: atualiza ângulo de rotação e fase de flutuação
     * - NuvemToxica: não faz nada (usa atualizar(alvoX, alvoY))
     *
     * A declaração como abstract garante em tempo de compilação que
     * nenhuma subclasse seja criada sem implementar este comportamento.
     */
    public abstract void atualizar();

    /**
     * Método abstrato de desenho — CONTRATO DA ABSTRAÇÃO.
     *
     * Toda subclasse OBRIGATORIAMENTE deve implementar este método,
     * definindo como a entidade é renderizada visualmente na tela.
     *
     * Este método é chamado a cada quadro pelo GerenciadorJogo, que
     * itera sobre a lista de entidades e chama desenhar() em cada uma
     * sem saber o tipo específico de cada objeto — isso é POLIMORFISMO.
     *
     * @param g2d contexto gráfico 2D fornecido pelo sistema Swing,
     *            utilizado para desenhar formas, imagens e textos na tela
     */
    public abstract void desenhar(Graphics2D g2d);
}