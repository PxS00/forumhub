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
import br.com.alura.forumhub.service.validation.resposta.atualizar.ValidationAtualizarResposta;
import br.com.alura.forumhub.service.validation.resposta.cadastrar.ValidationCadastrarResposta;
import br.com.alura.forumhub.service.validation.resposta.excluir.ValidationExcluirResposta;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RespostaService {

    @Autowired
    private RespostaRepository respostaRepository;

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private List<ValidationCadastrarResposta> validationCadastrarResposta;

    @Autowired
    private List<ValidationAtualizarResposta> validationAtualizarResposta;

    @Autowired
    private List<ValidationExcluirResposta> validationExcluirResposta;


    private Resposta respostaExiste(Long id) {
        return respostaRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Resposta não encontrada")
                );
    }

    @Transactional
    public DadosDetalhamentoResposta cadastrar(DadosCadastroResposta dados) {

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
