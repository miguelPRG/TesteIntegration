package api.classes;

/*
    "id": 0,
    "memberId": 0,
    "bookId": 0,
    "reservationDate": "2026-06-14T22:06:29.076Z",
    "returnDate": "2026-06-14T22:06:29.076Z"
*/
public class Reserva {
    Integer id;
    Integer memberId;
    Integer bookId;
    String reservationDate;
    String returnDate;

    public Reserva() {
    }

    public Reserva(Integer id, Integer memberId, Integer bookId, String reservationDate, String returnDate) {
        this.id = id;
        this.memberId = memberId;
        this.bookId = bookId;
        this.reservationDate = reservationDate;
        this.returnDate = returnDate;
    
    }

    public Integer getId() {
        return id;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public String getReturnDate() {
        return returnDate;
    }
}
