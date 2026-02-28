package br.com.alura.forumhub.repository;

import br.com.alura.forumhub.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(@NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email);
}
