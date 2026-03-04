package br.com.alura.forumhub.service.validation.topico.cadastrar;

import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;
import br.com.alura.forumhub.repository.CursoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorCursoExiste implements ValidationCadastrarTopico {

    private final CursoRepository cursoRepository;

    public ValidadorCursoExiste(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Override
    public void validar(DadosCadastroTopico dados) {

        boolean cursoExiste = cursoRepository.existsById(dados.idCurso());
        if (!cursoExiste) {
            throw new EntityNotFoundException("Curso não encontrado");
        }
    }
}
