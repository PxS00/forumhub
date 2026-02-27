package br.com.alura.forumhub.repository;

import br.com.alura.forumhub.model.Curso;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    boolean existsByNome(@NotBlank String nome);
}
