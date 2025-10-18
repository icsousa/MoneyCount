package utils;

import java.io.*;

/**
 * Classe utilitária para serializar e desserializar objetos em ficheiros .dat
 * compatível com todas as classes do teu projeto (Despesa, Registo, MoneyCount, etc.)
 */
public class Serializer {

    private static final String BASE_PATH = "resources/";

    /**
     * Guarda um objeto serializável num ficheiro .dat dentro de ../resources/
     *
     * @param nomeFicheiro Nome do ficheiro (sem caminho completo)
     * @param objeto        Objeto a serializar
     * @return true se gravou com sucesso, false se ocorreu erro
     */
    public static boolean guardar(String nomeFicheiro, Object objeto) {
        File pasta = new File(BASE_PATH);
        if (!pasta.exists()) {
            pasta.mkdirs(); // cria pasta caso não exista
        }

        File ficheiro = new File(pasta, nomeFicheiro);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ficheiro))) {
            oos.writeObject(objeto);
            oos.flush();
            System.out.println("✅ Dados guardados em: " + ficheiro.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("⚠️ Erro ao guardar ficheiro " + ficheiro.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Lê um objeto serializado de um ficheiro .dat
     *
     * @param nomeFicheiro Nome do ficheiro (sem caminho completo)
     * @param <T> Tipo do objeto esperado
     * @return O objeto lido, ou null se ocorrer erro
     */
    @SuppressWarnings("unchecked")
    public static <T> T ler(String nomeFicheiro) {
        File ficheiro = new File(BASE_PATH, nomeFicheiro);
        if (!ficheiro.exists()) {
            System.err.println("⚠️ Ficheiro não encontrado: " + ficheiro.getAbsolutePath());
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ficheiro))) {
            T objeto = (T) ois.readObject();
            System.out.println("📂 Dados lidos de: " + ficheiro.getAbsolutePath());
            return objeto;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("⚠️ Erro ao ler ficheiro " + ficheiro.getName() + ": " + e.getMessage());
            return null;
        }
    }
}
