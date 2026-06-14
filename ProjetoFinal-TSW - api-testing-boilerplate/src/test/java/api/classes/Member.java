package api.classes;

public class Member {
    private Integer id;
    private String firstName;
    private String lastName;
    private String address;
    private String postalCode;
    private String city;
    private String country;
    private Integer phoneNumber;
    private Integer nif;
    private String email;
    private String birthDate;
    private String registrationDate;

    public Member() {
    }

    public Member(String firstName, String lastName, String address, String postalCode, String city, String country, Integer phoneNumber, Integer nif, String email, String birthDate, String registrationDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.phoneNumber = phoneNumber;
        this.nif = nif;
        this.email = email;
        this.birthDate = birthDate;
        this.registrationDate = registrationDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Integer phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getNif() {
        return nif;
    }

    public void setNif(Integer nif) {
        this.nif = nif;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

}
