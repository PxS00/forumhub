package br.com.alura.forumhub.controllers;

import br.com.alura.forumhub.dto.resposta.DadosAtualizacaoResposta;
import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import br.com.alura.forumhub.dto.resposta.DadosDetalhamentoResposta;
import br.com.alura.forumhub.dto.resposta.DadosListagemResposta;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.service.RespostaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@WithMockUser
@ActiveProfiles("test")
@DisplayName("Testes de integração do RespostaController")
class RespostaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RespostaService service;

    // =========================================================
    // POST /respostas — cadastrar
    // =========================================================

    @Test
    @DisplayName("POST /respostas - deve retornar 201 Created quando dados válidos")
    void cadastrar_dadosValidos_deveRetornar201() throws Exception {
        var dadosEntrada = new DadosCadastroResposta("Resposta válida", 1L, 1L);
        var dadosResposta = new DadosDetalhamentoResposta(1L, "Resposta válida",
                LocalDateTime.of(2025, 3, 1, 10, 0), false, 1L, 1L);

        given(service.cadastrar(any(DadosCadastroResposta.class))).willReturn(dadosResposta);

        mockMvc.perform(post("/respostas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mensagem").value("Resposta válida"))
                .andExpect(jsonPath("$.idTopico").value(1))
                .andExpect(jsonPath("$.idAutor").value(1));
    }

    @Test
    @DisplayName("POST /respostas - deve retornar 400 quando mensagem está em branco")
    void cadastrar_mensagemBranca_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroResposta("", 1L, 1L);

        mockMvc.perform(post("/respostas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("mensagem"));
    }

    @Test
    @DisplayName("POST /respostas - deve retornar 400 quando idTopico é nulo")
    void cadastrar_idTopicoNulo_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroResposta("Mensagem válida", null, 1L);

        mockMvc.perform(post("/respostas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("idTopico"));
    }

    @Test
    @DisplayName("POST /respostas - deve retornar 400 quando idAutor é nulo")
    void cadastrar_idAutorNulo_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroResposta("Mensagem válida", 1L, null);

        mockMvc.perform(post("/respostas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("idAutor"));
    }

    @Test
    @DisplayName("POST /respostas - deve retornar 400 quando tópico está fechado")
    void cadastrar_topicoFechado_deveRetornar400() throws Exception {
        var dadosEntrada = new DadosCadastroResposta("Resposta", 1L, 1L);

        given(service.cadastrar(any())).willThrow(
                new ValidacaoException("Não é permitido responder um tópico fechado"));

        mockMvc.perform(post("/respostas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Não é permitido responder um tópico fechado"));
    }

    @Test
    @DisplayName("POST /respostas - deve retornar 404 quando autor não existe")
    void cadastrar_autorNaoEncontrado_deveRetornar404() throws Exception {
        var dadosEntrada = new DadosCadastroResposta("Resposta", 1L, 99L);

        given(service.cadastrar(any())).willThrow(new EntityNotFoundException("Autor não encontrado"));

        mockMvc.perform(post("/respostas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Autor não encontrado"));
    }

    // =========================================================
    // GET /respostas — listar
    // =========================================================

    @Test
    @DisplayName("GET /respostas - deve retornar 200 com página de respostas")
    void listar_deveRetornar200ComPagina() throws Exception {
        var resposta = new DadosListagemResposta(1L, "Resposta válida", "Ana Silva", "Dúvida sobre Spring");
        var pagina = new PageImpl<>(List.of(resposta), PageRequest.of(0, 10), 1);

        given(service.listar(any())).willReturn(pagina);

        mockMvc.perform(get("/respostas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].mensagem").value("Resposta válida"))
                .andExpect(jsonPath("$.content[0].nomeAutor").value("Ana Silva"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /respostas - deve retornar 200 com lista vazia quando não há respostas")
    void listar_semRespostas_deveRetornar200ComListaVazia() throws Exception {
        given(service.listar(any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/respostas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // =========================================================
    // GET /respostas/{id} — detalhar
    // =========================================================

    @Test
    @DisplayName("GET /respostas/{id} - deve retornar 200 com detalhamento quando ID existe")
    void detalhar_idValido_deveRetornar200() throws Exception {
        var detalhes = new DadosDetalhamentoResposta(1L, "Resposta válida",
                LocalDateTime.of(2025, 3, 1, 10, 0), false, 1L, 1L);

        given(service.detalhar(1L)).willReturn(detalhes);

        mockMvc.perform(get("/respostas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mensagem").value("Resposta válida"))
                .andExpect(jsonPath("$.idTopico").value(1));
    }

    @Test
    @DisplayName("GET /respostas/{id} - deve retornar 404 quando ID não existe")
    void detalhar_idInexistente_deveRetornar404() throws Exception {
        given(service.detalhar(999L)).willThrow(new EntityNotFoundException("Resposta não encontrada"));

        mockMvc.perform(get("/respostas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resposta não encontrada"));
    }

    // =========================================================
    // PUT /respostas/{id} — atualizar
    // =========================================================

    @Test
    @DisplayName("PUT /respostas/{id} - deve retornar 200 com dados atualizados")
    void atualizar_dadosValidos_deveRetornar200() throws Exception {
        var dadosEntrada = new DadosAtualizacaoResposta("Mensagem atualizada");
        var dadosResposta = new DadosDetalhamentoResposta(1L, "Mensagem atualizada",
                LocalDateTime.of(2025, 3, 1, 10, 0), false, 1L, 1L);

        given(service.atualizar(eq(1L), any(DadosAtualizacaoResposta.class))).willReturn(dadosResposta);

        mockMvc.perform(put("/respostas/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Mensagem atualizada"));
    }

    @Test
    @DisplayName("PUT /respostas/{id} - deve retornar 400 quando mensagem está em branco")
    void atualizar_mensagemBranca_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosAtualizacaoResposta("");

        mockMvc.perform(put("/respostas/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("mensagem"));
    }

    @Test
    @DisplayName("PUT /respostas/{id} - deve retornar 404 quando resposta não existe")
    void atualizar_idInexistente_deveRetornar404() throws Exception {
        var dadosEntrada = new DadosAtualizacaoResposta("Mensagem");

        given(service.atualizar(eq(999L), any())).willThrow(new EntityNotFoundException("Resposta não encontrada"));

        mockMvc.perform(put("/respostas/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resposta não encontrada"));
    }

    @Test
    @DisplayName("PUT /respostas/{id} - deve retornar 400 quando usuário não é autor")
    void atualizar_usuarioNaoEAutor_deveRetornar400() throws Exception {
        var dadosEntrada = new DadosAtualizacaoResposta("Mensagem");

        given(service.atualizar(eq(1L), any())).willThrow(
                new ValidacaoException("Usuário não autorizado"));

        mockMvc.perform(put("/respostas/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usuário não autorizado"));
    }

    // =========================================================
    // DELETE /respostas/{id} — excluir
    // =========================================================

    @Test
    @DisplayName("DELETE /respostas/{id} - deve retornar 204 No Content quando ID existe")
    void excluir_idValido_deveRetornar204() throws Exception {
        willDoNothing().given(service).excluir(1L);

        mockMvc.perform(delete("/respostas/1").with(csrf()))
                .andExpect(status().isNoContent());

        then(service).should().excluir(1L);
    }

    @Test
    @DisplayName("DELETE /respostas/{id} - deve retornar 404 quando ID não existe")
    void excluir_idInexistente_deveRetornar404() throws Exception {
        willThrow(new EntityNotFoundException("Resposta não encontrada")).given(service).excluir(999L);

        mockMvc.perform(delete("/respostas/999").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resposta não encontrada"));
    }

    // =========================================================
    // 401 — não autenticado
    // =========================================================

    @Test
    @DisplayName("GET /respostas - deve retornar 401 quando não autenticado")
    @org.springframework.security.test.context.support.WithAnonymousUser
    void listar_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/respostas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /respostas - deve retornar 401 quando não autenticado")
    @org.springframework.security.test.context.support.WithAnonymousUser
    void cadastrar_semAutenticacao_deveRetornar401() throws Exception {
        var dadosEntrada = new DadosCadastroResposta("Mensagem", 1L, 1L);

        mockMvc.perform(post("/respostas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // 403 — autorização em nível de serviço (não-autor, não-admin)
    // =========================================================

    @Test
    @DisplayName("PUT /respostas/{id} - deve retornar 400 quando usuário autenticado não é autor nem admin")
    @WithMockUser(roles = "USER")
    void atualizar_usuarioNaoEhAutorNemAdmin_deveRetornar400() throws Exception {
        var dadosEntrada = new DadosAtualizacaoResposta("Mensagem");

        given(service.atualizar(eq(1L), any()))
                .willThrow(new ValidacaoException("Usuário não autorizado"));

        mockMvc.perform(put("/respostas/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usuário não autorizado"));
    }

    @Test
    @DisplayName("DELETE /respostas/{id} - deve retornar 400 quando usuário autenticado não é autor nem admin")
    @WithMockUser(roles = "USER")
    void excluir_usuarioNaoEhAutorNemAdmin_deveRetornar400() throws Exception {
        willThrow(new ValidacaoException("Usuário não autorizado"))
                .given(service).excluir(1L);

        mockMvc.perform(delete("/respostas/1").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usuário não autorizado"));
    }
}

