package me.trihung.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.trihung.entity.Role;
import me.trihung.entity.RoleDTO;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseDTO{
    private UUID id;
    private String fullName;
    private String email;
    private String username;
//    private String phone;
    private String avatarUrl;
//    private String description;
//    private LocalDate birthday;
    private Set<RoleDTO> roles = new HashSet<>();
}