package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.resposta.DadosAtualizacaoResposta;
import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import br.com.alura.forumhub.dto.resposta.DadosDetalhamentoResposta;
import br.com.alura.forumhub.dto.resposta.DadosListagemResposta;
import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Curso;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Testes unitários de RespostaService")
class RespostaServiceTest {

    @InjectMocks
    private RespostaService service;

    @Mock
    private RespostaRepository respostaRepository;

    @Mock
    private TopicoRepository topicoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ValidadorAutorExiste validadorAutorExiste;

    @Mock
    private ValidationCadastrarResposta validadorCadastro;

    @Mock
    private ValidationAtualizarResposta validadorAtualizacao;

    @Mock
    private ValidationExcluirResposta validadorExcluir;

    private Usuario autorFake;
    private Topico topicoFake;
    private Resposta respostaFake;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "validationCadastrarResposta", List.of(validadorCadastro));
        ReflectionTestUtils.setField(service, "validationAtualizarResposta", List.of(validadorAtualizacao));
        ReflectionTestUtils.setField(service, "validationExcluirResposta", List.of(validadorExcluir));

        autorFake = new Usuario(1L, "Ana Silva", "ana@email.com", "senha123", true, new java.util.HashSet<>());
        Curso cursoFake = new Curso(1L, "Spring Boot", "Programação");

        topicoFake = new Topico(new DadosCadastroTopico("Dúvida sobre Spring", "Como funciona o IoC?", 1L, 1L));
        topicoFake.definirAutorECurso(autorFake, cursoFake);
        ReflectionTestUtils.setField(topicoFake, "id", 1L);

        respostaFake = new Resposta(new DadosCadastroResposta("Resposta de teste", 1L, 1L));
        respostaFake.definirTopicoEAutor(topicoFake, autorFake);
        ReflectionTestUtils.setField(respostaFake, "id", 1L);
        ReflectionTestUtils.setField(respostaFake, "dataCriacao", LocalDateTime.of(2025, 3, 1, 10, 0));
    }

    // =========================================================
    // CADASTRAR
    // =========================================================

    @Test
    @DisplayName("Deve cadastrar resposta com sucesso quando dados válidos")
    void cadastrar_comDadosValidos_deveRetornarDetalhamento() {
        var dados = new DadosCadastroResposta("Resposta válida", 1L, 1L);

        willDoNothing().given(validadorAutorExiste).validar(dados);
        willDoNothing().given(validadorCadastro).validar(dados);
        given(topicoRepository.getReferenceById(1L)).willReturn(topicoFake);
        given(usuarioRepository.getReferenceById(1L)).willReturn(autorFake);
        given(respostaRepository.save(any(Resposta.class))).willAnswer(invocation -> {
            Resposta r = invocation.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 1L);
            return r;
        });

        DadosDetalhamentoResposta resultado = service.cadastrar(dados);

        assertThat(resultado).isNotNull();
        assertThat(resultado.mensagem()).isEqualTo("Resposta válida");
        assertThat(resultado.idTopico()).isEqualTo(1L);
        assertThat(resultado.idAutor()).isEqualTo(1L);

        then(respostaRepository).should().save(any(Resposta.class));
    }

    @Test
    @DisplayName("Deve chamar validadorAutorExiste e validadores de cadastro")
    void cadastrar_deveInvocarValidadores() {
        var dados = new DadosCadastroResposta("Resposta", 1L, 1L);

        given(topicoRepository.getReferenceById(1L)).willReturn(topicoFake);
        given(usuarioRepository.getReferenceById(1L)).willReturn(autorFake);
        given(respostaRepository.save(any())).willAnswer(i -> {
            Resposta r = i.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 2L);
            return r;
        });

        service.cadastrar(dados);

        then(validadorAutorExiste).should().validar(dados);
        then(validadorCadastro).should().validar(dados);
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando tópico fechado")
    void cadastrar_topicoFechado_deveLancarValidacaoException() {
        var dados = new DadosCadastroResposta("Resposta", 1L, 1L);

        willThrow(new ValidacaoException("Não é permitido responder um tópico fechado"))
                .given(validadorCadastro).validar(dados);

        assertThatThrownBy(() -> service.cadastrar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Não é permitido responder um tópico fechado");

        then(respostaRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando autor não existe")
    void cadastrar_autorInexistente_deveLancarEntityNotFoundException() {
        var dados = new DadosCadastroResposta("Resposta", 1L, 99L);

        willThrow(new EntityNotFoundException("Autor não encontrado")).given(validadorAutorExiste).validar(dados);

        assertThatThrownBy(() -> service.cadastrar(dados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Autor não encontrado");

        then(respostaRepository).should(never()).save(any());
    }

    // =========================================================
    // LISTAR
    // =========================================================

    @Test
    @DisplayName("Deve retornar página de respostas")
    void listar_deveRetornarPaginaDeRespostas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Resposta> pagina = new PageImpl<>(List.of(respostaFake));
        given(respostaRepository.findAll(pageable)).willReturn(pagina);

        Page<DadosListagemResposta> resultado = service.listar(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).mensagem()).isEqualTo("Resposta de teste");
        assertThat(resultado.getContent().get(0).nomeAutor()).isEqualTo("Ana Silva");
        assertThat(resultado.getContent().get(0).tituloTopico()).isEqualTo("Dúvida sobre Spring");
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há respostas")
    void listar_semRespostas_deveRetornarPaginaVazia() {
        Pageable pageable = PageRequest.of(0, 10);
        given(respostaRepository.findAll(pageable)).willReturn(Page.empty(pageable));

        Page<DadosListagemResposta> resultado = service.listar(pageable);

        assertThat(resultado).isEmpty();
    }

    // =========================================================
    // DETALHAR
    // =========================================================

    @Test
    @DisplayName("Deve retornar detalhamento da resposta quando ID válido")
    void detalhar_idValido_deveRetornarDetalhamento() {
        given(respostaRepository.findById(1L)).willReturn(Optional.of(respostaFake));

        DadosDetalhamentoResposta resultado = service.detalhar(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.mensagem()).isEqualTo("Resposta de teste");
        assertThat(resultado.idTopico()).isEqualTo(1L);
        assertThat(resultado.idAutor()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando resposta não encontrada pelo ID")
    void detalhar_idInexistente_deveLancarEntityNotFoundException() {
        given(respostaRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalhar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Resposta não encontrada");
    }

    // =========================================================
    // ATUALIZAR
    // =========================================================

    @Test
    @DisplayName("Deve atualizar resposta com sucesso quando dados válidos")
    void atualizar_comDadosValidos_deveRetornarRespostaAtualizada() {
        var dados = new DadosAtualizacaoResposta("Mensagem atualizada");

        given(respostaRepository.findById(1L)).willReturn(Optional.of(respostaFake));
        willDoNothing().given(validadorAtualizacao).validar(respostaFake, dados);

        DadosDetalhamentoResposta resultado = service.atualizar(1L, dados);

        assertThat(resultado.mensagem()).isEqualTo("Mensagem atualizada");
        then(validadorAtualizacao).should().validar(respostaFake, dados);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar resposta inexistente")
    void atualizar_idInexistente_deveLancarEntityNotFoundException() {
        var dados = new DadosAtualizacaoResposta("Mensagem");
        given(respostaRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(999L, dados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Resposta não encontrada");
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando usuário não é autor ao atualizar")
    void atualizar_usuarioNaoEAutor_deveLancarValidacaoException() {
        var dados = new DadosAtualizacaoResposta("Mensagem");

        given(respostaRepository.findById(1L)).willReturn(Optional.of(respostaFake));
        willThrow(new ValidacaoException("Usuário não autorizado"))
                .given(validadorAtualizacao).validar(respostaFake, dados);

        assertThatThrownBy(() -> service.atualizar(1L, dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Usuário não autorizado");
    }

    // =========================================================
    // EXCLUIR
    // =========================================================

    @Test
    @DisplayName("Deve excluir resposta com sucesso quando ID válido")
    void excluir_idValido_deveExcluirSemErros() {
        given(respostaRepository.findById(1L)).willReturn(Optional.of(respostaFake));
        willDoNothing().given(validadorExcluir).validar(respostaFake);
        willDoNothing().given(respostaRepository).delete(respostaFake);

        assertThatCode(() -> service.excluir(1L)).doesNotThrowAnyException();

        then(respostaRepository).should().delete(respostaFake);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao excluir resposta inexistente")
    void excluir_idInexistente_deveLancarEntityNotFoundException() {
        given(respostaRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Resposta não encontrada");

        then(respostaRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando usuário não é autor ao excluir")
    void excluir_usuarioNaoEAutor_deveLancarValidacaoException() {
        given(respostaRepository.findById(1L)).willReturn(Optional.of(respostaFake));
        willThrow(new ValidacaoException("Usuário não autorizado"))
                .given(validadorExcluir).validar(respostaFake);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Usuário não autorizado");

        then(respostaRepository).should(never()).delete(any());
    }
}

