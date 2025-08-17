package ru.practicum.ewm.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {

    @NotBlank
    @Size(min = 2, max = 250)
    private String name;

    @NotEmpty
    @Email
    @Size(min = 6, max = 254)
    private String email;
}