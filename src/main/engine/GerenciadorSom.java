package main.engine;

import javax.sound.sampled.*;

/**
 * Classe GerenciadorSom — responsável pela síntese e reprodução de sons.
 *
 * Esta classe sintetiza todos os sons do jogo diretamente em Java,
 * sem utilizar arquivos de áudio externos. Os sons são gerados
 * matematicamente a partir de formas de onda senoidais e ruído branco,
 * utilizando a API javax.sound.sampled do Java.
 *
 * Todos os métodos são estáticos, permitindo que sejam chamados
 * diretamente pelo nome da classe sem necessidade de instanciação:
 * GerenciadorSom.itemVerde(), GerenciadorSom.dano(), etc.
 *
 * Cada som é reproduzido em uma thread separada para não bloquear
 * o game loop, garantindo que a reprodução de áudio não cause
 * atrasos perceptíveis na animação do jogo.
 *
 * CONCEITOS TÉCNICOS APLICADOS:
 * - Síntese de áudio digital por formas de onda senoidais
 * - Envoltória de amplitude com ataque e decaimento suaves
 * - Multithreading para reprodução assíncrona
 * - Taxa de amostragem de 44100 Hz (padrão CD)
 */
public class GerenciadorSom {

    /**
     * Reproduz um som a partir de um array de bytes em uma thread separada.
     *
     * Cria uma nova thread para cada reprodução, garantindo que o game loop
     * não seja bloqueado enquanto o som está sendo reproduzido. Em caso de
     * falha, o erro é silenciado para não interromper o jogo.
     *
     * @param dados  array de bytes representando o áudio em formato PCM
     * @param volume ajuste de volume em decibéis (negativo = mais baixo)
     */
    private static void tocar(byte[] dados, float volume) {
        new Thread(() -> {
            try {
                // Define o formato de áudio: 44100 Hz, 16 bits, mono
                AudioFormat format = new AudioFormat(
                        44100, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(
                        SourceDataLine.class, format);
                SourceDataLine line =
                        (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);

                // Ajusta o volume usando controle de ganho
                FloatControl fc = (FloatControl)
                        line.getControl(FloatControl.Type.MASTER_GAIN);
                fc.setValue(volume);

                // Reproduz o áudio e aguarda a conclusão
                line.start();
                line.write(dados, 0, dados.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                // Falha silenciosa para nao interromper o jogo
            }
        }).start();
    }

    /**
     * Gera um tom senoidal com a frequência, duração e amplitude indicadas.
     *
     * A forma de onda senoidal é a mais pura musicalmente, gerando um
     * tom limpo sem harmônicos. A fórmula utilizada é:
     * valor = sin(2 * PI * frequencia * i / taxaAmostragem)
     *
     * Uma envoltória de amplitude é aplicada para suavizar o início e o
     * fim do som, evitando o clique audível que ocorreria com início/fim
     * abrupto. O fade corresponde a 20ms ou 1/4 da duração, o que for menor.
     *
     * @param frequencia frequência do tom em Hz (ex: 440 = nota Lá)
     * @param duracaoMs  duração do tom em milissegundos
     * @param amplitude  amplitude (volume) do tom entre 0.0 e 1.0
     * @return array de bytes representando o tom em formato PCM 16 bits
     */
    private static byte[] gerarTom(double frequencia,
                                   int duracaoMs,
                                   double amplitude) {
        int amostras = (int)(44100 * duracaoMs / 1000.0);
        byte[] dados = new byte[amostras * 2]; // 2 bytes por amostra (16 bits)

        for (int i = 0; i < amostras; i++) {
            // Calcula envoltória de amplitude (fade in e fade out)
            double envelope = 1.0;
            int fadeMs = Math.min(20, duracaoMs / 4);
            int fadeSamples = (int)(44100 * fadeMs / 1000.0);
            if (i < fadeSamples)
                envelope = (double) i / fadeSamples;
            if (i > amostras - fadeSamples)
                envelope = (double)(amostras - i) / fadeSamples;

            // Gera amostra senoidal com envoltória
            short valor = (short)(Math.sin(
                    2 * Math.PI * frequencia * i / 44100)
                    * amplitude * envelope * Short.MAX_VALUE);

            // Armazena em little-endian (byte menos significativo primeiro)
            dados[i * 2]     = (byte)(valor & 0xFF);
            dados[i * 2 + 1] = (byte)((valor >> 8) & 0xFF);
        }
        return dados;
    }

    /**
     * Gera ruído branco com a duração e amplitude indicadas.
     *
     * O ruído branco é gerado por valores aleatórios distribuídos
     * uniformemente entre -amplitude e +amplitude. É utilizado para
     * criar efeitos sonoros de impacto e explosão.
     *
     * @param duracaoMs duração do ruído em milissegundos
     * @param amplitude amplitude (volume) entre 0.0 e 1.0
     * @return array de bytes representando o ruído em formato PCM 16 bits
     */
    private static byte[] gerarRuido(int duracaoMs, double amplitude) {
        int amostras = (int)(44100 * duracaoMs / 1000.0);
        byte[] dados = new byte[amostras * 2];

        for (int i = 0; i < amostras; i++) {
            // Envoltória de amplitude para fade in e fade out
            double envelope = 1.0;
            int fadeSamples = (int)(44100 * 0.01);
            if (i < fadeSamples)
                envelope = (double) i / fadeSamples;
            if (i > amostras - fadeSamples)
                envelope = (double)(amostras - i) / fadeSamples;

            // Gera amostra aleatória (ruído branco)
            short valor = (short)((Math.random() * 2 - 1)
                    * amplitude * envelope * Short.MAX_VALUE);

            dados[i * 2]     = (byte)(valor & 0xFF);
            dados[i * 2 + 1] = (byte)((valor >> 8) & 0xFF);
        }
        return dados;
    }

    /**
     * Concatena múltiplos arrays de bytes em um único array.
     * Utilizado para criar sons compostos por múltiplos tons sequenciais.
     *
     * @param partes arrays de bytes a serem concatenados
     * @return array de bytes resultante da concatenação
     */
    private static byte[] concatenar(byte[]... partes) {
        int total = 0;
        for (byte[] p : partes) total += p.length;
        byte[] resultado = new byte[total];
        int pos = 0;
        for (byte[] p : partes) {
            System.arraycopy(p, 0, resultado, pos, p.length);
            pos += p.length;
        }
        return resultado;
    }

    /**
     * Reproduz o som de coleta de item positivo (folha verde).
     * Dois tons ascendentes criam uma sensação de recompensa positiva.
     * Frequências: 520 Hz (Do#5) seguido de 780 Hz (Sol5).
     */
    public static void itemVerde() {
        tocar(concatenar(
                gerarTom(520,  80, 0.3),
                gerarTom(780, 120, 0.3)
        ), -10f);
    }

    /**
     * Reproduz o som de coleta de item negativo (barril tóxico).
     * Dois tons descendentes criam uma sensação de consequência negativa.
     * Frequências: 300 Hz seguido de 180 Hz (tons graves descendentes).
     */
    public static void itemLaranja() {
        tocar(concatenar(
                gerarTom(300,  80, 0.3),
                gerarTom(180, 150, 0.3)
        ), -10f);
    }

    /**
     * Reproduz o som de dano causado pela nuvem tóxica.
     * Ruído branco seguido de tom grave cria sensação de impacto.
     */
    public static void dano() {
        tocar(concatenar(
                gerarRuido(60, 0.4),
                gerarTom(150, 120, 0.25)
        ), -8f);
    }

    /**
     * Reproduz o som de vitória.
     * Melodia ascendente de quatro notas cria sensação de conquista.
     * Frequências: 520, 660, 780 e 1040 Hz (escala ascendente).
     */
    public static void vitoria() {
        tocar(concatenar(
                gerarTom(520,  100, 0.3),
                gerarTom(660,  100, 0.3),
                gerarTom(780,  100, 0.3),
                gerarTom(1040, 300, 0.35)
        ), -8f);
    }

    /**
     * Reproduz o som de derrota.
     * Melodia descendente de quatro notas cria sensação sombria de falha.
     * Frequências: 440, 370, 300 e 220 Hz (escala descendente).
     */
    public static void derrota() {
        tocar(concatenar(
                gerarTom(440, 150, 0.3),
                gerarTom(370, 150, 0.3),
                gerarTom(300, 150, 0.3),
                gerarTom(220, 400, 0.35)
        ), -8f);
    }
}