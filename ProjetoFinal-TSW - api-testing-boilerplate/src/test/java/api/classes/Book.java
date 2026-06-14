package api.classes;



public class Book {

    private Integer id;
    private String title;
    private String author;
    private String publisher;
    private Integer editionYear;
    private String edition;
    private String description;
    private String isbn;
    private BookStatus status;

    public Book() {
    }

    public Book(String title, String author, String publisher, Integer editionYear, String edition,
            String description, String isbn, BookStatus status) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.editionYear = editionYear;
        this.edition = edition;
        this.description = description;
        this.isbn = isbn;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getEditionYear() {
        return editionYear;
    }

    public void setEditionYear(Integer editionYear) {
        this.editionYear = editionYear;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }
}
