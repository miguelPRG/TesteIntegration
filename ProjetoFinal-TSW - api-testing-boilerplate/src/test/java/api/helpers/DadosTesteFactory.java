package api.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import api.classes.Book;
import api.classes.BookStatus;
import api.classes.Member;

public final class DadosTesteFactory {

    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis());

    private DadosTesteFactory() {
    }

    public static Book criarLivroValido() {
        return criarLivroValido(gerarIsbnValido(), "Livro para reserva");
    }

    public static Book criarLivroValido(String isbn, String description) {
        return new Book(
            "Effective Java",
            "Joshua Bloch",
            "Addison-Wesley",
            2018,
            "3",
            description,
            isbn,
            BookStatus.AVAILABLE
        );
    }

    public static Member criarMembroValido() {
        long valorUnico = gerarValorUnico();

        return new Member(
            "João",
            "Silva",
            "Rua A",
            "1234-567",
            "Lisboa",
            "Portugal",
            912345678,
            gerarNifValido(valorUnico),
            "joao.silva" + valorUnico + "@example.com",
            "1990-01-01",
            "2023-01-01"
        );
    }

    public static Map<String, Object> criarLivroComDatatypesInvalidos() {
        Map<String, Object> livroInvalido = new HashMap<>();
        livroInvalido.put("title", 12345);
        livroInvalido.put("author", true);
        livroInvalido.put("publisher", 67890);
        livroInvalido.put("editionYear", "Ano inválido");
        livroInvalido.put("edition", 1);
        livroInvalido.put("description", 12345);
        livroInvalido.put("isbn", 9780134685991L);
        livroInvalido.put("status", 123);
        return livroInvalido;
    }

    public static Map<String, Object> criarLivroComStatusInvalido(String isbn) {
        Map<String, Object> livroInvalido = new HashMap<>();
        livroInvalido.put("title", "Título de Teste");
        livroInvalido.put("author", "Autor de Teste");
        livroInvalido.put("publisher", "Editora Teste");
        livroInvalido.put("editionYear", 2024);
        livroInvalido.put("edition", "1");
        livroInvalido.put("description", "Descrição do livro de teste");
        livroInvalido.put("isbn", isbn);
        livroInvalido.put("status", "INVALID_STATUS");
        return livroInvalido;
    }

    public static Map<String, Object> criarMembroComDatatypesInvalidos() {
        Map<String, Object> membroInvalido = new HashMap<>();
        membroInvalido.put("firstName", 12345);
        membroInvalido.put("lastName", true);
        membroInvalido.put("address", 67890);
        membroInvalido.put("postalCode", false);
        membroInvalido.put("city", 12345);
        membroInvalido.put("country", 67890);
        membroInvalido.put("phoneNumber", "telefone inválido");
        membroInvalido.put("nif", "nif inválido");
        membroInvalido.put("email", 12345);
        membroInvalido.put("birthDate", 12345);
        membroInvalido.put("registrationDate", true);
        return membroInvalido;
    }

    public static Member criarMembroComCamposFormatoInvalido() {
        Member membroInvalido = criarMembroValido();
        membroInvalido.setPostalCode("1234567");
        membroInvalido.setCity("Lisboa123");
        membroInvalido.setCountry("Portugal123");
        membroInvalido.setPhoneNumber(12345);
        membroInvalido.setNif(123456780);
        membroInvalido.setEmail("email-invalido");
        return membroInvalido;
    }

    public static String gerarIsbnValido() {
        String base = "978" + String.format("%09d", gerarValorUnico() % 1_000_000_000);
        int soma = 0;

        for (int i = 0; i < base.length(); i++) {
            int digito = Character.getNumericValue(base.charAt(i));
            soma += (i % 2 == 0) ? digito : digito * 3;
        }

        int digitoControlo = (10 - (soma % 10)) % 10;
        return base + digitoControlo;
    }

    public static Integer gerarNifValido(long valorUnico) {
        String base = "2" + String.format("%07d", valorUnico % 10_000_000);
        int soma = 0;

        for (int i = 0; i < base.length(); i++) {
            int digito = Character.getNumericValue(base.charAt(i));
            soma += digito * (9 - i);
        }

        int digitoControlo = 11 - (soma % 11);
        if (digitoControlo >= 10) {
            digitoControlo = 0;
        }

        return Integer.parseInt(base + digitoControlo);
    }

    private static long gerarValorUnico() {
        return SEQUENCE.incrementAndGet() % 1_000_000;
    }
}
