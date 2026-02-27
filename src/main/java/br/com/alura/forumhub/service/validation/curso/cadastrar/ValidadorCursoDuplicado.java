package br.com.alura.forumhub.service.validation.curso.cadastrar;

import br.com.alura.forumhub.dto.curso.DadosCadastroCurso;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorCursoDuplicado implements ValidadorCadastroCurso {

    @Autowired
    private CursoRepository repository;

    @Override
    public void validar(DadosCadastroCurso dados) {

        if (repository.existsByNome(dados.nome())) {
            throw new ValidacaoException("Curso já existe");
        }
    }
}
