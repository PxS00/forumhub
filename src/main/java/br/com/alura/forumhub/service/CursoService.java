package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.curso.DadosCadastroCurso;
import br.com.alura.forumhub.dto.curso.DadosDetalhamentoCurso;
import br.com.alura.forumhub.model.Curso;
import br.com.alura.forumhub.repository.CursoRepository;
import br.com.alura.forumhub.service.validation.curso.cadastrar.ValidadorCadastroCurso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private List<ValidadorCadastroCurso> validadores;

    @Transactional
    public DadosDetalhamentoCurso cadastrar(DadosCadastroCurso dados) {

        validadores.forEach(v -> v.validar(dados));

        Curso curso = new Curso(dados);
        cursoRepository.save(curso);

        return new DadosDetalhamentoCurso(curso);
    }
}
