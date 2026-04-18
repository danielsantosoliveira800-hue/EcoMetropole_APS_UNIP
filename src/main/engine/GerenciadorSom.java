package main.engine;

import javax.sound.sampled.*;

public class GerenciadorSom {

    private static void tocar(byte[] dados, float volume) {
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);

                FloatControl fc = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                fc.setValue(volume);

                line.start();
                line.write(dados, 0, dados.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                // Som falhou silenciosamente
            }
        }).start();
    }

    private static byte[] gerarTom(double frequencia, int duracaoMs, double amplitude) {
        int amostras = (int)(44100 * duracaoMs / 1000.0);
        byte[] dados = new byte[amostras * 2];
        for (int i = 0; i < amostras; i++) {
            double envelope = 1.0;
            int fadeMs = Math.min(20, duracaoMs / 4);
            int fadeSamples = (int)(44100 * fadeMs / 1000.0);
            if (i < fadeSamples) envelope = (double) i / fadeSamples;
            if (i > amostras - fadeSamples) envelope = (double)(amostras - i) / fadeSamples;

            short valor = (short)(Math.sin(2 * Math.PI * frequencia * i / 44100)
                    * amplitude * envelope * Short.MAX_VALUE);
            dados[i * 2]     = (byte)(valor & 0xFF);
            dados[i * 2 + 1] = (byte)((valor >> 8) & 0xFF);
        }
        return dados;
    }

    private static byte[] gerarRuido(int duracaoMs, double amplitude) {
        int amostras = (int)(44100 * duracaoMs / 1000.0);
        byte[] dados = new byte[amostras * 2];
        for (int i = 0; i < amostras; i++) {
            double envelope = 1.0;
            int fadeSamples = (int)(44100 * 0.01);
            if (i < fadeSamples) envelope = (double) i / fadeSamples;
            if (i > amostras - fadeSamples) envelope = (double)(amostras - i) / fadeSamples;

            short valor = (short)((Math.random() * 2 - 1) * amplitude * envelope * Short.MAX_VALUE);
            dados[i * 2]     = (byte)(valor & 0xFF);
            dados[i * 2 + 1] = (byte)((valor >> 8) & 0xFF);
        }
        return dados;
    }

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

    public static void itemVerde() {
        byte[] som = concatenar(
                gerarTom(520,  80, 0.3),
                gerarTom(780, 120, 0.3)
        );
        tocar(som, -10f);
    }

    public static void itemLaranja() {
        byte[] som = concatenar(
                gerarTom(300,  80, 0.3),
                gerarTom(180, 150, 0.3)
        );
        tocar(som, -10f);
    }

    public static void dano() {
        byte[] som = concatenar(
                gerarRuido(60, 0.4),
                gerarTom(150, 120, 0.25)
        );
        tocar(som, -8f);
    }

    public static void vitoria() {
        byte[] som = concatenar(
                gerarTom(520,  100, 0.3),
                gerarTom(660,  100, 0.3),
                gerarTom(780,  100, 0.3),
                gerarTom(1040, 300, 0.35)
        );
        tocar(som, -8f);
    }

    public static void derrota() {
        byte[] som = concatenar(
                gerarTom(440, 150, 0.3),
                gerarTom(370, 150, 0.3),
                gerarTom(300, 150, 0.3),
                gerarTom(220, 400, 0.35)
        );
        tocar(som, -8f);
    }
}