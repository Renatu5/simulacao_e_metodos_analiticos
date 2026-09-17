package filasTandem;

import java.io.FileWriter;
import java.io.IOException;

public class GenRandomNumber {
    private double a = 16807;
    private double c = 76;
    private double m = Math.pow(2, 31) - 1;
    private double seed = 1;
    private double anterior = 0;

    public void write() throws IOException {

        // int formula = (anterior * a + c) % m;
        FileWriter writer = new FileWriter("output.txt");
        FileWriter uniformWriter = new FileWriter("uniform.txt");

        for (int i = 0; i <= 1000; i++) {
            if (anterior == 0) {
                anterior = seed;
            } else {
                anterior = (anterior * a + c) % m;
                writer.write(anterior + "\n");
                uniformWriter.write(anterior / m + ",  ");
            }
        }

        writer.close();
        uniformWriter.close();
    }

    public double nextRandom() {

        if (anterior == 0) {
            anterior = seed;
        } else {
            anterior = (anterior * a + c) % m;
        }
        return anterior / m;
    }
}