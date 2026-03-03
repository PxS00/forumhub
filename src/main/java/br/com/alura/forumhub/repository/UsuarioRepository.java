package br.com.alura.forumhub.repository;

import br.com.alura.forumhub.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Usuario> findByAtivoTrue(Pageable paginacao);

    boolean existsByEmailAndAtivoTrue(String email);
}
