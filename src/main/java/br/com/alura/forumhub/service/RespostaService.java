package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.resposta.DadosAtualizacaoResposta;
import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import br.com.alura.forumhub.dto.resposta.DadosDetalhamentoResposta;
import br.com.alura.forumhub.dto.resposta.DadosListagemResposta;
import br.com.alura.forumhub.model.Resposta;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.RespostaRepository;
import br.com.alura.forumhub.repository.TopicoRepository;
import br.com.alura.forumhub.repository.UsuarioRepository;
import br.com.alura.forumhub.service.validation.comum.ValidadorAutorExiste;
import br.com.alura.forumhub.service.validation.resposta.atualizar.ValidationAtualizarResposta;
import br.com.alura.forumhub.service.validation.resposta.cadastrar.ValidationCadastrarResposta;
import br.com.alura.forumhub.service.validation.resposta.excluir.ValidationExcluirResposta;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RespostaService {

    private final RespostaRepository respostaRepository;
    private final TopicoRepository topicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final List<ValidationCadastrarResposta> validationCadastrarResposta;
    private final List<ValidationAtualizarResposta> validationAtualizarResposta;
    private final List<ValidationExcluirResposta> validationExcluirResposta;
    private final ValidadorAutorExiste validadorAutorExiste;

    public RespostaService(RespostaRepository respostaRepository,
                           TopicoRepository topicoRepository,
                           UsuarioRepository usuarioRepository,
                           List<ValidationCadastrarResposta> validationCadastrarResposta,
                           List<ValidationAtualizarResposta> validationAtualizarResposta,
                           List<ValidationExcluirResposta> validationExcluirResposta,
                           ValidadorAutorExiste validadorAutorExiste) {
        this.respostaRepository = respostaRepository;
        this.topicoRepository = topicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.validationCadastrarResposta = validationCadastrarResposta;
        this.validationAtualizarResposta = validationAtualizarResposta;
        this.validationExcluirResposta = validationExcluirResposta;
        this.validadorAutorExiste = validadorAutorExiste;
    }


    private Resposta respostaExiste(Long id) {
        return respostaRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Resposta não encontrada")
                );
    }

    @Transactional
    public DadosDetalhamentoResposta cadastrar(DadosCadastroResposta dados) {

        validadorAutorExiste.validar(dados);
        validationCadastrarResposta.forEach(v -> v.validar(dados));

        Topico topico = topicoRepository.getReferenceById(dados.idTopico());
        Usuario autor = usuarioRepository.getReferenceById(dados.idAutor());

        Resposta resposta = new Resposta(dados);
        resposta.definirTopicoEAutor(topico, autor);

        respostaRepository.save(resposta);

        return new DadosDetalhamentoResposta(resposta);
    }

    public Page<DadosListagemResposta> listar(Pageable paginacao) {

        return respostaRepository
                .findAll(paginacao)
                .map(DadosListagemResposta::new);
    }

    public DadosDetalhamentoResposta detalhar(Long id) {

        Resposta resposta = respostaExiste(id);
        return new DadosDetalhamentoResposta(resposta);
    }

    @Transactional
    public DadosDetalhamentoResposta atualizar(Long id, DadosAtualizacaoResposta dados) {

        Resposta resposta = respostaExiste(id);
        validationAtualizarResposta.forEach(v -> v.validar(resposta, dados));

        resposta.atualizarDados(dados);

        return new DadosDetalhamentoResposta(resposta);
    }

    @Transactional
    public void excluir(Long id) {

        Resposta resposta = respostaExiste(id);
        validationExcluirResposta.forEach(v -> v.validar(resposta));

        respostaRepository.delete(resposta);
    }
}
