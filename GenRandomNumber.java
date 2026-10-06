public class GenRandomNumber {
    private static final long MULTIPLICADOR = 16_807;
    private static final long INCREMENTO = 76;
    private static final long MODULO = 2_147_483_647;

    private long estado = 1;

    public double nextRandom() {
        estado = (estado * MULTIPLICADOR + INCREMENTO) % MODULO;
        return (double) estado / MODULO;
    }
}