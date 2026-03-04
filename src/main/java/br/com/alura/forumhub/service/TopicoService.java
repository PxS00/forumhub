package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;
import br.com.alura.forumhub.dto.topico.DadosDetalhamentoTopico;
import br.com.alura.forumhub.dto.topico.DadosListagemTopico;
import br.com.alura.forumhub.model.Curso;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.CursoRepository;
import br.com.alura.forumhub.repository.TopicoRepository;
import br.com.alura.forumhub.repository.UsuarioRepository;
import br.com.alura.forumhub.service.validation.comum.ValidadorAutorExiste;
import br.com.alura.forumhub.service.validation.topico.atualizar.ValidationAtualizarTopico;
import br.com.alura.forumhub.service.validation.topico.cadastrar.ValidationCadastrarTopico;
import br.com.alura.forumhub.service.validation.topico.excluir.ValidationExcluirTopico;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TopicoService {

    private final TopicoRepository topicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final List<ValidationCadastrarTopico> validationCadastrarTopico;
    private final List<ValidationAtualizarTopico> validationAtualizarTopico;
    private final ValidadorAutorExiste validadorAutorExiste;
    private final List<ValidationExcluirTopico> validationExcluirTopico;

    public TopicoService(TopicoRepository topicoRepository,
                         UsuarioRepository usuarioRepository,
                         CursoRepository cursoRepository,
                         List<ValidationCadastrarTopico> validationCadastrarTopico,
                         List<ValidationAtualizarTopico> validationAtualizarTopico,
                         ValidadorAutorExiste validadorAutorExiste,
                         List<ValidationExcluirTopico> validationExcluirTopico) {
        this.topicoRepository = topicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.validationCadastrarTopico = validationCadastrarTopico;
        this.validationAtualizarTopico = validationAtualizarTopico;
        this.validadorAutorExiste = validadorAutorExiste;
        this.validationExcluirTopico = validationExcluirTopico;
    }

    private Topico topicoExiste(Long id){
        return topicoRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Tópico não encontrado")
                );
    }

    @Transactional
    public DadosDetalhamentoTopico cadastrar(DadosCadastroTopico dados) {

        validadorAutorExiste.validar(dados);
        validationCadastrarTopico.forEach(v -> v.validar(dados));

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

        validationAtualizarTopico.forEach(v -> v.validar(topico, dados));

        Curso curso = cursoRepository.findById(dados.idCurso())
                .orElseThrow(() -> new EntityNotFoundException("Curso não encontrado"));

        topico.atualizarDados(dados, curso);

        return new DadosDetalhamentoTopico(topico);
    }

    @Transactional
    public void deletar(Long id) {
        Topico topico = topicoExiste(id);
        validationExcluirTopico.forEach(v -> v.validar(topico));
        topicoRepository.delete(topico);
    }
}