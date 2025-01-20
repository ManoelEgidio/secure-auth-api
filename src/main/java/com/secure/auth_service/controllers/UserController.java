package com.secure.auth_service.controllers;

import com.secure.auth_service.dtos.AuthenticationDTO;
import com.secure.auth_service.dtos.UserRegisterDTO;
import com.secure.auth_service.dtos.UserSummaryDTO;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.services.UserService;
import com.secure.auth_service.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Operações relacionadas aos usuários")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Registrar um usuário")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody UserRegisterDTO userDTO) {
        SecurityUtils.checkRoleAndAuthority(Roles.ADMIN, Authority.CREATE);
        userService.register(userDTO.toEntity());
    }

    @Operation(summary = "Atualizar informações do usuário")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable UUID id, @Valid @RequestBody UserSummaryDTO userDTO) {
        SecurityUtils.checkAuthority(Authority.EDIT);
        userService.update(id, userDTO);
    }

    @Operation(summary = "Alterar senha do usuário")
    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.OK)
    public void updatePassword(@PathVariable UUID id, @Valid @RequestBody AuthenticationDTO authDTO) {
        if (authDTO.getLogin() == null || authDTO.getPassword() == null) {
            throw new CustomException("Login e senha são obrigatórios para alteração.");
        }
        SecurityUtils.checkAuthority(Authority.EDIT);
        userService.updatePassword(id, authDTO.getPassword());
    }

    @Operation(summary = "Desabilitar um usuário")
    @PutMapping("/disable/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void disableUser(@PathVariable UUID id) {
        SecurityUtils.checkRoleAndAuthority(Roles.ADMIN, Authority.DISABLE);
        userService.disableUser(id);
    }

    @Operation(summary = "Buscar um usuário por ID")
    @GetMapping("/{id}")
    public UserSummaryDTO getById(@PathVariable UUID id) {
        SecurityUtils.checkAuthority(Authority.VIEW);
        return userService.getUserSummary(id)
                .orElseThrow(() -> new CustomException("Usuário não encontrado."));
    }

    @Operation(summary = "Deletar um usuário")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable UUID id) {
        SecurityUtils.checkRoleAndAuthority(Roles.ADMIN, Authority.DISABLE);
        userService.deleteUser(id);
    }

    @Operation(summary = "Filtrar usuários")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public Page<UserSummaryDTO> search(@RequestParam(required = false) String name,
                                       @RequestParam(required = false) String login,
                                       @RequestParam(required = false) Roles role,
                                       @RequestParam(required = false, defaultValue = "0") Integer page,
                                       @RequestParam(required = false, defaultValue = "20") Integer size,
                                       @RequestParam(required = false, defaultValue = "name") String sort,
                                       @RequestParam(required = false, defaultValue = "asc") String direction) {

        SecurityUtils.checkAuthority(Authority.SEARCH);
        final var filters = new HashMap<String, Object>();
        if (name != null) filters.put("name", name);
        if (login != null) filters.put("login", login);
        if (role != null) filters.put("role", role);

        return userService.search(filters, page, size, sort, direction);
    }
}
