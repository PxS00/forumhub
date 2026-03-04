package br.com.alura.forumhub.service.validation.curso.atualizar;

import br.com.alura.forumhub.dto.curso.DadosAtualizacaoCurso;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.repository.CursoRepository;
import org.springframework.stereotype.Component;

@Component
public class ValidadorCursoDuplicadoAtualizacao implements ValidationAtualizacaoCurso {

    private final CursoRepository repository;

    public ValidadorCursoDuplicadoAtualizacao(CursoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(Long id, DadosAtualizacaoCurso dados) {

        if (dados.nome() == null) return;

        if (repository.existsByNomeAndIdNot(
                dados.nome(), id)) {

            throw new ValidacaoException("Curso já existe");
        }

    }
}
