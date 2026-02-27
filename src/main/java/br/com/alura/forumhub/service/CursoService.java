package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.curso.DadosAtualizacaoCurso;
import br.com.alura.forumhub.dto.curso.DadosCadastroCurso;
import br.com.alura.forumhub.dto.curso.DadosDetalhamentoCurso;
import br.com.alura.forumhub.dto.curso.DadosListagemCurso;
import br.com.alura.forumhub.model.Curso;
import br.com.alura.forumhub.repository.CursoRepository;
import br.com.alura.forumhub.service.validation.curso.cadastrar.ValidadorCadastroCurso;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private List<ValidadorCadastroCurso> validadores;

    private Curso cursoExiste(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Curso não encontrado")
                );
    }

    @Transactional
    public DadosDetalhamentoCurso cadastrar(DadosCadastroCurso dados) {

        validadores.forEach(v -> v.validar(dados));

        Curso curso = new Curso(dados);
        cursoRepository.save(curso);

        return new DadosDetalhamentoCurso(curso);
    }

    public Page<DadosListagemCurso> listar(Pageable paginacao) {

        return cursoRepository
                .findAll(paginacao)
                .map(DadosListagemCurso::new);
    }

    public DadosDetalhamentoCurso detalhar(Long id) {

        Curso curso = cursoExiste(id);

        return new DadosDetalhamentoCurso(curso);
    }

    @Transactional
    public DadosDetalhamentoCurso atualizar(Long id, @Valid DadosAtualizacaoCurso dados) {

        Curso curso = cursoExiste(id);

        curso.atualizarDados(dados);

        return new DadosDetalhamentoCurso(curso);
    }

    @Transactional
    public void deletar(Long id) {
        Curso curso = cursoExiste(id);

        cursoRepository.delete(curso);
    }
}
