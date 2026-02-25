package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.DadosCadastroTopico;
import br.com.alura.forumhub.dto.DadosDetalhamentoTopico;
import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
import br.com.alura.forumhub.dto.topico.DadosListagemTopico;
import br.com.alura.forumhub.model.Curso;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.CursoRepository;
import br.com.alura.forumhub.repository.TopicoRepository;
import br.com.alura.forumhub.repository.UsuarioRepository;
import br.com.alura.forumhub.service.validation.topico.atualizar.ValidationAtualizacaoTopico;
import br.com.alura.forumhub.service.validation.topico.cadastrar.ValidationCadastroTopico;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
    public class TopicoService {

        @Autowired
        private TopicoRepository topicoRepository;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private CursoRepository cursoRepository;

        @Autowired
        private List<ValidationCadastroTopico> validationCadastroTopico;

        @Autowired
        private List<ValidationAtualizacaoTopico> validationAtualizacaoTopico;

    private Topico topicoExiste(Long id){
        return topicoRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Tópico não encontrado")
                );
    }

    @Transactional
    public DadosDetalhamentoTopico cadastrar(DadosCadastroTopico dados) {

        validationCadastroTopico.forEach(v -> v.validar(dados));

        Usuario autor = usuarioRepository.getReferenceById(dados.idAutor());
        Curso curso = cursoRepository.getReferenceById(dados.idCurso());

        Topico topico = new Topico(dados);
        topico.definirAutorECurso(autor, curso);

        topicoRepository.save(topico);

        return new DadosDetalhamentoTopico(topico);
    }

    public Page<DadosListagemTopico> listar(String curso, Integer ano, Pageable paginacao) {

        return topicoRepository
                .buscarPorCursoEAno(curso, ano, paginacao)
                .map(DadosListagemTopico::new);
    }

    public DadosDetalhamentoTopico detalhar(Long id) {
        Topico topico = topicoExiste(id);

        return new DadosDetalhamentoTopico(topico);
    }

    @Transactional
    public DadosDetalhamentoTopico atualizar(Long id, DadosAtualizacaoTopico dados) {

        Topico topico = topicoExiste(id);

        Curso curso = cursoRepository.findById(dados.idCurso())
                .orElseThrow(() ->
                        new EntityNotFoundException("Curso não encontrado")
                );

        validationAtualizacaoTopico.forEach(v -> v.validar(id, dados));

        topico.atualizarDados(dados, curso);

        return new DadosDetalhamentoTopico(topico);
    }

    @Transactional
    public void deletar(Long id) {
        Topico topico = topicoExiste(id);
        topicoRepository.deleteById(id);


    }
}