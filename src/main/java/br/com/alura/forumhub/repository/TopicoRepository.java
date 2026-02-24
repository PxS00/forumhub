package br.com.alura.forumhub.repository;

import br.com.alura.forumhub.model.Topico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TopicoRepository extends JpaRepository<Topico, Long> {

    boolean existsByTituloAndMensagem(String titulo, String mensagem);

    @Query("""
       SELECT t
       FROM Topico t
       WHERE (:curso IS NULL OR t.curso.nome = :curso)
       AND (:ano IS NULL OR YEAR(t.dataCriacao) = :ano)
       """)
    Page<Topico> buscarPorCursoEAno(
            String curso,
            Integer ano,
            Pageable pageable
    );
}
