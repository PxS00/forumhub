package br.com.alura.forumhub.repository;

import br.com.alura.forumhub.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
