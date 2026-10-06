import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class Main {
    private static final int LIMITE_ALEATORIOS = 100_000;
    private static final double PROBABILIDADE_FILA_2 = 0.20;
    private static final double PROBABILIDADE_SAIR_FILA_3 = 0.30;

    private static final GenRandomNumber GERADOR = new GenRandomNumber();
    private static final PriorityQueue<Evento> eventos = new PriorityQueue<>(
            Comparator.comparingDouble(Evento::tempo));

    private static Fila fila1;
    private static Fila fila2;
    private static Fila fila3;
    private static int aleatoriosUsados;
    private static double tempoAtual;

    public static void main(String[] args) {
        inicializar();
        while (!eventos.isEmpty()) {
            Evento evento = eventos.poll();
            acumularTempo(evento.tempo);
            processar(evento);
        }
        imprimirRelatorio();
    }

    private static void inicializar() {
        fila1 = new Fila("Fila 1", 1, Integer.MAX_VALUE, 2, 4, 1, 2);
        fila2 = new Fila("Fila 2", 2, 5, 0, 0, 4, 6);
        fila3 = new Fila("Fila 3", 2, 10, 0, 0, 5, 15);
        eventos.clear();
        aleatoriosUsados = 0;
        tempoAtual = 0;
        eventos.offer(new Evento(2.0, TipoEvento.CHEGADA_FILA_1));
    }

    private static void processar(Evento evento) {
        switch (evento.tipo) {
            case CHEGADA_FILA_1 -> {
                aceitarEntrada(fila1, evento.tempo, TipoEvento.SAIDA_FILA_1);
                if (podeSortear()) {
                    eventos.offer(new Evento(evento.tempo + intervaloChegada(fila1),
                            TipoEvento.CHEGADA_FILA_1));
                }
            }
            case ENTRADA_FILA_1 -> {
                aceitarEntrada(fila1, evento.tempo, TipoEvento.SAIDA_FILA_1);
            }
            case SAIDA_FILA_1 -> {
                processarSaida(fila1, evento.tempo, TipoEvento.SAIDA_FILA_1);
                if (podeSortear()) {
                    TipoEvento destino = sorteio() < PROBABILIDADE_FILA_2
                            ? TipoEvento.ENTRADA_FILA_2 : TipoEvento.ENTRADA_FILA_3;
                    eventos.offer(new Evento(evento.tempo, destino));
                }
            }
            case ENTRADA_FILA_2 -> {
                aceitarEntrada(fila2, evento.tempo, TipoEvento.SAIDA_FILA_2);
            }
            case SAIDA_FILA_2 -> {
                processarSaida(fila2, evento.tempo, TipoEvento.SAIDA_FILA_2);
                if (podeSortear()) {
                    double rota = sorteio();
                    if (rota >= PROBABILIDADE_FILA_2) {
                        eventos.offer(new Evento(evento.tempo,
                            rota < 0.5 ? TipoEvento.ENTRADA_FILA_1
                                        : TipoEvento.ENTRADA_FILA_2));
                    }
                }
            }
            case ENTRADA_FILA_3 -> {
                aceitarEntrada(fila3, evento.tempo, TipoEvento.SAIDA_FILA_3);
            }
            case SAIDA_FILA_3 -> {
                processarSaida(fila3, evento.tempo, TipoEvento.SAIDA_FILA_3);
                if (podeSortear() && sorteio() >= PROBABILIDADE_SAIR_FILA_3) {
                    eventos.offer(new Evento(evento.tempo, TipoEvento.ENTRADA_FILA_3));
                }
            }
            default -> {
                throw new IllegalStateException("Tipo de evento desconhecido");
            }
        }
    }

    private static void aceitarEntrada(Fila fila, double tempo, TipoEvento saida) {
        if (!fila.podeReceber()) {
            fila.registrarPerda();
            return;
        }
        boolean iniciaServico = fila.clientes() < fila.servidores;
        fila.entrar();
        if (iniciaServico && podeSortear()) {
            eventos.offer(new Evento(tempo + intervaloServico(fila), saida));
        }
    }

    private static void processarSaida(Fila fila, double tempo, TipoEvento saida) {
        if (fila.clientes() == 0) {
            return;
        }
        fila.sair();
        if (fila.clientes() >= fila.servidores && podeSortear()) {
            eventos.offer(new Evento(tempo + intervaloServico(fila), saida));
        }
    }

    private static double intervaloServico(Fila fila) {
        return fila.minService + (fila.maxService - fila.minService) * sorteio();
    }

    private static double intervaloChegada(Fila fila) {
        return fila.minArrival + (fila.maxArrival - fila.minArrival) * sorteio();
    }

    private static double sorteio() {
        aleatoriosUsados++;
        return GERADOR.nextRandom();
    }

    private static boolean podeSortear() {
        return aleatoriosUsados < LIMITE_ALEATORIOS;
    }

    private static void acumularTempo(double proximoTempo) {
        double intervalo = proximoTempo - tempoAtual;
        fila1.acumularTempo(intervalo);
        fila2.acumularTempo(intervalo);
        fila3.acumularTempo(intervalo);
        tempoAtual = proximoTempo;
    }

    private static void imprimirRelatorio() {
        System.out.printf("Aleatorios usados: %d%n", aleatoriosUsados);
        System.out.printf("Tempo global da simulacao: %.6f%n", tempoAtual);
        imprimirFila(fila1);
        imprimirFila(fila2);
        imprimirFila(fila3);
    }

    private static void imprimirFila(Fila fila) {
        System.out.printf("%s - perdas: %d%n", fila.nome, fila.perdas);
        for (Map.Entry<Integer, Double> estado : fila.tempoPorEstado.entrySet()) {
            double probabilidade = tempoAtual == 0 ? 0 : estado.getValue() / tempoAtual;
            System.out.printf("  Estado %d: tempo acumulado = %.6f, probabilidade = %.8f%n",
                    estado.getKey(), estado.getValue(), probabilidade);
        }
    }

    private enum TipoEvento {
        CHEGADA_FILA_1, ENTRADA_FILA_1, SAIDA_FILA_1, ENTRADA_FILA_2,
        SAIDA_FILA_2, ENTRADA_FILA_3, SAIDA_FILA_3
    }

    private static final class Evento {
        private final double tempo;
        private final TipoEvento tipo;

        private Evento(double tempo, TipoEvento tipo) {
            this.tempo = tempo;
            this.tipo = tipo;
        }

        private double tempo() {
            return tempo;
        }
    }

    private static final class Fila {
        private final String nome;
        private final int servidores;
        private final int capacidade;
        private final double minArrival;
        private final double maxArrival;
        private final double minService;
        private final double maxService;
        private final Map<Integer, Double> tempoPorEstado = new TreeMap<>();
        private int clientes;
        private int perdas;

        private Fila(String nome, int servidores, int capacidade, double minArrival,
                double maxArrival, double minService, double maxService) {
            this.nome = nome;
            this.servidores = servidores;
            this.capacidade = capacidade;
            this.minArrival = minArrival;
            this.maxArrival = maxArrival;
            this.minService = minService;
            this.maxService = maxService;
        }

        private boolean podeReceber() {
            return clientes < capacidade;
        }

        private int clientes() {
            return clientes;
        }

        private void entrar() {
            clientes++;
        }

        private void sair() {
            clientes--;
        }

        private void registrarPerda() {
            perdas++;
        }

        private void acumularTempo(double intervalo) {
            tempoPorEstado.merge(clientes, intervalo, Double::sum);
        }
    }
}