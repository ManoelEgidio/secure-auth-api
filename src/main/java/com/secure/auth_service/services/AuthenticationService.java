package com.secure.auth_service.services;

import com.secure.auth_service.dtos.EmailDTO;
import com.secure.auth_service.enums.TokenType;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import com.secure.auth_service.repositories.UserRepository;
import com.secure.auth_service.utils.RedisUtils;
import com.secure.auth_service.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Value("${feature.activation.enabled:true}")
    private boolean activationEnabled;

    @Value("${feature.recovery.enabled:true}")
    private boolean recoveryEnabled;

    @Value("${activation.token.ttl:3600}") // 1 hora em segundos
    private long activationTokenTTL;

    @Value("${recovery.token.ttl:1800}") // 30 minutos em segundos
    private long recoveryTokenTTL;

    @Value("${app.domain}")
    private String domain;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils;
    private final EmailService emailService; // Supondo que exista
    private final TokenUtils tokenUtils; // Já existente no seu projeto

    /**
     * Envia email de ativação para o usuário.
     *
     * @param user Usuário a ser ativado
     */
    public void sendActivationEmail(User user) {
        if (!activationEnabled) {
            log.info("Fluxo de ativação está desabilitado via properties. Nada será feito.");
            return;
        }

        // Gera um token JWT específico para ativação
        String activationToken = tokenUtils.generateActivationToken(user);

        // Armazena no Redis com TTL configurado
        redisUtils.storeToken(TokenType.ACTIVATION, activationToken, user.getId().toString(), activationTokenTTL);

        // Monta link de ativação
        String activationLink = getDomain() + "/auth/activate?token=" + activationToken;

        // Envia email
        String subject = "Ativação de Conta";
        String content = String.format(
                "Olá %s,\n\nClique no link abaixo para ativar sua conta:\n%s\n\nSe você não solicitou, ignore este email.",
                user.getName(),
                activationLink
        );

        emailService.sendEmail(new EmailDTO(user.getLogin(), subject, content));
    }

    /**
     * Confirma a ativação da conta usando o token fornecido.
     *
     * @param token Token de ativação
     */
    public void confirmActivation(String token) {
        if (!activationEnabled) {
            throw new CustomException("Fluxo de ativação de conta está desabilitado.");
        }

        // Valida o token JWT
        if (!tokenUtils.validateActivationToken(token)) {
            throw new CustomException("Token de ativação inválido ou expirado.");
        }

        // Recupera o userId do token via Redis
        String userIdStr = redisUtils.getUserIdByToken(TokenType.ACTIVATION, token);
        if (userIdStr == null) {
            throw new CustomException("Token de ativação inválido ou expirado.");
        }

        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Usuário não encontrado."));

        // Ativa o usuário
        user.setEnabled(true);
        userRepository.save(user);

        // Remove o token do Redis
        redisUtils.removeToken(TokenType.ACTIVATION, token);

        log.info("Usuário {} ativado com sucesso.", user.getLogin());
    }

    /**
     * Envia email de recuperação de senha para o usuário.
     *
     * @param email Email do usuário
     */
    public void sendPasswordRecoveryEmail(String email) {
        if (!recoveryEnabled) {
            log.info("Fluxo de recuperação de senha está desabilitado via properties. Nada será feito.");
            return;
        }

        // Busca o usuário por email (login)
        User user = (User) userRepository.findByLogin(email);

        if (user == null) {
            // Para segurança, não indicar se o usuário existe ou não
            log.warn("Usuário não encontrado com e-mail: {}", email);
            return;
        }

        // Gera um token JWT específico para recuperação
        String recoveryToken = tokenUtils.generateRecoveryToken(user);

        // Armazena no Redis com TTL configurado
        redisUtils.storeToken(TokenType.RECOVERY, recoveryToken, user.getId().toString(), recoveryTokenTTL);

        // Monta link de recuperação
        String recoveryLink = getDomain() + "/auth/reset-password?token=" + recoveryToken;

        // Envia email
        String subject = "Recuperação de Senha";
        String content = String.format(
                "Olá %s,\n\nClique no link abaixo para redefinir sua senha:\n%s\n\nSe você não solicitou, ignore este email.",
                user.getName(),
                recoveryLink
        );

        emailService.sendEmail(new EmailDTO(user.getLogin(), subject, content));
    }

    /**
     * Reseta a senha do usuário usando o token fornecido.
     *
     * @param token       Token de recuperação
     * @param newPassword Nova senha
     */
    public void resetPassword(String token, String newPassword) {
        if (!recoveryEnabled) {
            throw new CustomException("Fluxo de recuperação de senha está desabilitado.");
        }

        // Valida o token JWT
        if (!tokenUtils.validateRecoveryToken(token)) {
            throw new CustomException("Token de recuperação inválido ou expirado.");
        }

        // Recupera o userId do token via Redis
        String userIdStr = redisUtils.getUserIdByToken(TokenType.RECOVERY, token);
        if (userIdStr == null) {
            throw new CustomException("Token de recuperação inválido ou expirado.");
        }

        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Usuário não encontrado para reset de senha."));

        // Atualiza a senha
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Remove o token do Redis
        redisUtils.removeToken(TokenType.RECOVERY, token);

        log.info("Senha do usuário {} foi resetada com sucesso.", user.getLogin());
    }

    /**
     * Método auxiliar para obter o domínio configurado com HTTPS.
     *
     * @return Domínio com prefixo https://
     */
    private String getDomain() {
        return domain.startsWith("https://") ? domain : "https://" + domain;
    }
}
