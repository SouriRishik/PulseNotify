package com.pulsenotify.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class SignupRequest {
    @NotBlank
    @Email
    @jakarta.validation.constraints.Pattern(regexp = "^[\\w-\\.]+@gmail\\.com$", message = "Only @gmail.com addresses are allowed")
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private Set<String> roles;
}
