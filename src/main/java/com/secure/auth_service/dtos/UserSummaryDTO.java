package com.secure.auth_service.dtos;

import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.models.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class UserSummaryDTO extends AbstractDTO {

    @Schema(description = "ID do usuário", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID id;

    @Schema(description = "Nome do usuário", example = "João Silva")
    private String name;

    @Schema(description = "Login do usuário", example = "joao.silva@example.com")
    private String login;

    @Schema(description = "Cargo do usuário", example = "USER")
    private Roles role;

    @Schema(description = "Permissões do usuário", example = "[\"CREATE\", \"UPDATE\"]")
    private Set<Authority> authorities;

    @Schema(description = "Status do usuário", example = "true")
    private Boolean enabled;

    public UserSummaryDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.login = user.getLogin();
        this.role = user.getRole();
        this.authorities = user.getAuthorities().stream()
                .map(grantedAuthority -> Authority.valueOf(grantedAuthority.getAuthority()))
                .collect(Collectors.toSet());
        this.enabled = user.getEnabled();
    }

    public User toEntity() {
        return User.builder()
                .id(this.id)
                .name(this.name)
                .login(this.login)
                .role(this.role)
                .authorities(new HashSet<>(this.authorities))
                .enabled(this.enabled)
                .build();
    }
}
