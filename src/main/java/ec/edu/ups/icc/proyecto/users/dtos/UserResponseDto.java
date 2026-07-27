package ec.edu.ups.icc.proyecto.users.dtos;

import ec.edu.ups.icc.proyecto.users.entities.UserStatus;

public class UserResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private UserStatus status;

    // Constructor vacío
    public UserResponseDto() {
    }

    // Constructor lleno
    public UserResponseDto(Long id, String firstName, String lastName, String fullName, String email,
            UserStatus status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}