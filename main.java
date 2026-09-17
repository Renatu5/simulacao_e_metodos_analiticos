package filasTandem;

import java.util.Comparator;
import java.util.PriorityQueue;

public class main {

    private static final int LIMITE_ALEATORIOS = 100000;
    private static final PriorityQueue<Evento> eventos = new PriorityQueue<>(
            Comparator.comparingDouble(Evento::getTempo));
    private static Fila fila1;
    private static Fila fila2;
    private static GenRandomNumber randomNumber = new GenRandomNumber();
    private static double tempoGlobal;
    private static long aleatoriosUsados;

    public static void main(String[] args) {

        fila1 = new Fila(2, 3, 1, 5, 4, 5);
        fila2 = new Fila(1, 5, 0, 0, 1, 3);
        tempoGlobal = 0;
        aleatoriosUsados = 0;

        eventos.clear();
        eventos.offer(new Evento(2.5, "chegada"));

        while (!eventos.isEmpty() && aleatoriosUsados < LIMITE_ALEATORIOS) {
            Evento evento = eventos.poll();
            acumularTempo(evento.getTempo());

            switch (evento.tipo) {
                case "chegada":
                    CHEGADA(evento);
                    break;
                case "saida1":
                    SAIDA(evento);
                    break;
                case "passagem":
                    PASSAGEM(evento);
                    break;
                case "saida2":
                    SAIDA2(evento);
                    break;
                default:
                    break;
            }
        }

        imprimirRelatorio();
    }

    private static void CHEGADA(Evento evento) {

        fila1.times = evento.getTempo();

        if (fila1.Status() < fila1.capacity) {

            fila1.In();

        } else {
            fila1.loss++;
        }

        if (aleatoriosUsados < LIMITE_ALEATORIOS) {
            eventos.offer(new Evento(fila1.times + intervaloChegada(fila1), "chegada"));
        }

        if (fila1.Status() > 0 && fila1.Status() <= fila1.Servers()
                && aleatoriosUsados < LIMITE_ALEATORIOS) {
            eventos.offer(new Evento(fila1.times + intervaloServico(fila1), "saida1"));
        }
    }

    private static void SAIDA(Evento evento) {
        fila1.times = evento.getTempo();

        if (fila1.Status() == 0) {
            return;
        }

        fila1.Out();

        if (fila1.Status() >= fila1.Servers() && aleatoriosUsados < LIMITE_ALEATORIOS) {
            eventos.offer(new Evento(fila1.times + intervaloServico(fila1), "saida1"));
        }

        eventos.offer(new Evento(fila1.times, "passagem"));
    }

    private static void PASSAGEM(Evento evento) {
        fila2.times = evento.getTempo();

        if (fila2.Status() < fila2.capacity) {
            fila2.In();

            if (fila2.Status() <= fila2.Servers() && aleatoriosUsados < LIMITE_ALEATORIOS) {
                eventos.offer(new Evento(fila2.times + intervaloServico(fila2), "saida2"));
            }
        } else {
            fila2.loss++;
        }
    }

    private static void SAIDA2(Evento evento) {
        fila2.times = evento.getTempo();

        if (fila2.Status() == 0) {
            return;
        }

        fila2.Out();

        if (fila2.Status() >= fila2.Servers() && aleatoriosUsados < LIMITE_ALEATORIOS) {
            eventos.offer(new Evento(fila2.times + intervaloServico(fila2), "saida2"));

        }

    }

    private static double intervaloServico(Fila fila) {
        return (fila.maxService - fila.minService) * proximoAleatorio()
                + fila.minService;
    }

    private static double intervaloChegada(Fila fila) {
        return (fila.maxArrival - fila.minArrival) * proximoAleatorio()
                + fila.minArrival;
    }

    private static double proximoAleatorio() {
        aleatoriosUsados++;
        return randomNumber.nextRandom();
    }

    private static void acumularTempo(double proximoTempo) {
        double delta = proximoTempo - tempoGlobal;
        if (delta < 0) {
            throw new IllegalStateException("Evento fora de ordem cronologica");
        }
        fila1.tempoPorEstado[fila1.Status()] += delta;
        fila2.tempoPorEstado[fila2.Status()] += delta;
        tempoGlobal = proximoTempo;
    }

    private static void imprimirRelatorio() {
        System.out.printf("Tempo global da simulacao: %.6f%n", tempoGlobal);
        imprimirFila("Fila 1", fila1);
        imprimirFila("Fila 2", fila2);
    }

    private static void imprimirFila(String nome, Fila fila) {
        System.out.printf("%s - perdas: %d%n", nome, fila.loss);
        for (int estado = 0; estado <= fila.capacity; estado++) {
            double tempo = fila.tempoPorEstado[estado];
            double probabilidade = tempoGlobal == 0 ? 0 : tempo / tempoGlobal;
            System.out.printf("  Estado %d: tempo acumulado = %.6f, probabilidade = %.8f%n",
                    estado, tempo, probabilidade);
        }
    }

    public static class Fila {
        private final int server;
        private final int capacity;
        private int custumers;
        private int loss;
        private final double minArrival;
        private final double maxArrival;
        private final double minService;
        private final double maxService;
        private double times;
        private final double[] tempoPorEstado;

        public Fila(int server, int capacity, double minArrival, double maxArrival,
                double minService, double maxService) {
            this.server = server;
            this.capacity = capacity;
            this.custumers = 0;
            this.loss = 0;
            this.minArrival = minArrival;
            this.maxArrival = maxArrival;
            this.minService = minService;
            this.maxService = maxService;
            this.times = 0;
            this.tempoPorEstado = new double[capacity + 1];
        }

        public int Status() {
            return custumers;
        }

        public int Servers() {
            return server;
        }

        public void In() {
            custumers++;
        }

        public void Out() {
            custumers--;
        }
    }

    public static class Evento {
        private final double tempo;
        private final String tipo;

        public Evento(double tempo, String tipo) {
            this.tempo = tempo;
            this.tipo = tipo;
        }

        public double getTempo() {
            return tempo;
        }
    }
}
