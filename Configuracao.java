import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

final class Configuracao {
    private final Map<String, String> valores;

    private Configuracao(Map<String, String> valores) {
        this.valores = valores;
    }

    static Configuracao carregar(Path caminho) throws IOException {
        Map<String, String> valores = new HashMap<>();
        Deque<Secao> secoes = new ArrayDeque<>();

        for (String linha : Files.readAllLines(caminho)) {
            String semComentario = linha.split("#", 2)[0].stripTrailing();
            if (semComentario.isBlank()) {
                continue;
            }

            if (semComentario.strip().startsWith("!")) {
                continue;
            }

            int indentacao = semComentario.length() - semComentario.stripLeading().length();
            String conteudo = semComentario.strip();
            int separador = conteudo.indexOf(':');
            if (separador <= 0) {
                throw new IllegalArgumentException("Linha YAML invalida: " + linha);
            }

            while (!secoes.isEmpty() && secoes.peek().indentacao() >= indentacao) {
                secoes.pop();
            }

            String chave = conteudo.substring(0, separador).strip();
            String valor = conteudo.substring(separador + 1).strip();
            String caminhoCompleto = chave;
            for (Secao secao : secoes) {
                caminhoCompleto = secao.chave() + "." + caminhoCompleto;
            }

            if (valor.isEmpty()) {
                secoes.push(new Secao(indentacao, chave));
            } else {
                valores.put(caminhoCompleto, removerAspas(valor));
            }
        }
        return new Configuracao(valores);
    }

    String texto(String chave) {
        String valor = valores.get(chave);
        if (valor == null) {
            throw new IllegalArgumentException("Configuracao ausente: " + chave);
        }
        return valor;
    }

    int inteiro(String chave) {
        return Integer.parseInt(texto(chave));
    }

    double numero(String chave) {
        return Double.parseDouble(texto(chave));
    }

    private static String removerAspas(String valor) {
        if (valor.length() >= 2
                && ((valor.startsWith("\"") && valor.endsWith("\""))
                        || (valor.startsWith("'") && valor.endsWith("'")))) {
            return valor.substring(1, valor.length() - 1);
        }
        return valor;
    }

    private record Secao(int indentacao, String chave) {
    }
}
