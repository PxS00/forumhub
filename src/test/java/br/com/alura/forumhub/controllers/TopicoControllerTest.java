package br.com.alura.forumhub.controllers;

import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;
import br.com.alura.forumhub.dto.topico.DadosDetalhamentoTopico;
import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
import br.com.alura.forumhub.dto.topico.DadosListagemTopico;
import br.com.alura.forumhub.exception.TopicoDuplicadoException;
import br.com.alura.forumhub.model.StatusTopico;
import br.com.alura.forumhub.service.TopicoService;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@WithMockUser
@ActiveProfiles("test")
@DisplayName("Testes de integração do TopicoController")
class TopicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TopicoService service;

    // =========================================================
    // POST /topicos — cadastrar
    // =========================================================

    @Test
    @DisplayName("POST /topicos - deve retornar 201 Created com corpo quando dados válidos")
    void cadastrar_dadosValidos_deveRetornar201() throws Exception {
        var dadosEntrada = new DadosCadastroTopico("Dúvida sobre Spring", "Como funciona o IoC?", 1L, 1L);
        var dadosResposta = new DadosDetalhamentoTopico(1L, "Dúvida sobre Spring", "Como funciona o IoC?", "Ana Silva", "Spring Boot");

        given(service.cadastrar(any(DadosCadastroTopico.class))).willReturn(dadosResposta);

        mockMvc.perform(post("/topicos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/topicos/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Dúvida sobre Spring"))
                .andExpect(jsonPath("$.mensagem").value("Como funciona o IoC?"))
                .andExpect(jsonPath("$.nomeAutor").value("Ana Silva"))
                .andExpect(jsonPath("$.nomeCurso").value("Spring Boot"));
    }

    @Test
    @DisplayName("POST /topicos - deve retornar 400 quando título está em branco")
    void cadastrar_tituloBranco_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroTopico("", "Mensagem válida", 1L, 1L);

        mockMvc.perform(post("/topicos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("titulo"));
    }

    @Test
    @DisplayName("POST /topicos - deve retornar 400 quando mensagem está em branco")
    void cadastrar_mensagemBranca_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroTopico("Título válido", "", 1L, 1L);

        mockMvc.perform(post("/topicos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("mensagem"));
    }

    @Test
    @DisplayName("POST /topicos - deve retornar 400 quando idCurso é nulo")
    void cadastrar_idCursoNulo_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroTopico("Título", "Mensagem", null, 1L);

        mockMvc.perform(post("/topicos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("idCurso"));
    }

    @Test
    @DisplayName("POST /topicos - deve retornar 400 quando idAutor é nulo")
    void cadastrar_idAutorNulo_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroTopico("Título", "Mensagem", 1L, null);

        mockMvc.perform(post("/topicos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("idAutor"));
    }

    @Test
    @DisplayName("POST /topicos - deve retornar 409 quando tópico já existe (duplicado)")
    void cadastrar_topicoDuplicado_deveRetornar409() throws Exception {
        var dadosEntrada = new DadosCadastroTopico("Título duplicado", "Mensagem duplicada", 1L, 1L);

        given(service.cadastrar(any())).willThrow(new TopicoDuplicadoException());

        mockMvc.perform(post("/topicos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Tópico já cadastrado com esse título e mensagem"));
    }

    @Test
    @DisplayName("POST /topicos - deve retornar 404 quando autor não encontrado")
    void cadastrar_autorNaoEncontrado_deveRetornar404() throws Exception {
        var dadosEntrada = new DadosCadastroTopico("Título", "Mensagem", 1L, 99L);

        given(service.cadastrar(any())).willThrow(new EntityNotFoundException("Autor não encontrado"));

        mockMvc.perform(post("/topicos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Autor não encontrado"));
    }

    @Test
    @DisplayName("POST /topicos - deve retornar 400 quando corpo JSON está malformado")
    void cadastrar_jsonMalformado_deveRetornar400() throws Exception {
        mockMvc.perform(post("/topicos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-a-json"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // GET /topicos — listar
    // =========================================================

    @Test
    @DisplayName("GET /topicos - deve retornar 200 com página de tópicos")
    void listar_semFiltros_deveRetornar200ComPagina() throws Exception {
        var topico = new DadosListagemTopico(
                "Dúvida sobre Spring",
                "Como funciona o IoC?",
                LocalDateTime.of(2025, 1, 10, 10, 0),
                StatusTopico.NAO_RESPONDIDO,
                "Ana Silva",
                "Spring Boot"
        );
        var pagina = new PageImpl<>(List.of(topico), PageRequest.of(0, 10), 1);

        given(service.listar(isNull(), isNull(), any())).willReturn(pagina);

        mockMvc.perform(get("/topicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titulo").value("Dúvida sobre Spring"))
                .andExpect(jsonPath("$.content[0].nomeAutor").value("Ana Silva"))
                .andExpect(jsonPath("$.content[0].status").value("NAO_RESPONDIDO"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /topicos - deve filtrar por curso quando parâmetro informado")
    void listar_comFiltroCurso_devePassarFiltroAoServico() throws Exception {
        given(service.listar(eq("Spring Boot"), isNull(), any()))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/topicos").param("curso", "Spring Boot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("GET /topicos - deve filtrar por ano quando parâmetro informado")
    void listar_comFiltroAno_devePassarFiltroAoServico() throws Exception {
        given(service.listar(isNull(), eq(2025), any()))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/topicos").param("ano", "2025"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /topicos - deve retornar 200 com lista vazia quando não há tópicos")
    void listar_semTopicos_deveRetornar200ComListaVazia() throws Exception {
        given(service.listar(any(), any(), any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/topicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // =========================================================
    // GET /topicos/{id} — detalhar
    // =========================================================

    @Test
    @DisplayName("GET /topicos/{id} - deve retornar 200 com detalhamento quando ID existe")
    void detalhar_idValido_deveRetornar200() throws Exception {
        var detalhes = new DadosDetalhamentoTopico(1L, "Dúvida sobre Spring", "Como funciona o IoC?", "Ana Silva", "Spring Boot");

        given(service.detalhar(1L)).willReturn(detalhes);

        mockMvc.perform(get("/topicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Dúvida sobre Spring"))
                .andExpect(jsonPath("$.nomeAutor").value("Ana Silva"));
    }

    @Test
    @DisplayName("GET /topicos/{id} - deve retornar 404 quando ID não existe")
    void detalhar_idInexistente_deveRetornar404() throws Exception {
        given(service.detalhar(999L)).willThrow(new EntityNotFoundException("Tópico não encontrado"));

        mockMvc.perform(get("/topicos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Tópico não encontrado"));
    }

    // =========================================================
    // PUT /topicos/{id} — atualizar
    // =========================================================

    @Test
    @DisplayName("PUT /topicos/{id} - deve retornar 200 com dados atualizados quando dados válidos")
    void atualizar_dadosValidos_deveRetornar200() throws Exception {
        var dadosEntrada = new DadosAtualizacaoTopico("Novo título", "Nova mensagem", 1L);
        var dadosResposta = new DadosDetalhamentoTopico(1L, "Novo título", "Nova mensagem", "Ana Silva", "Spring Boot");

        given(service.atualizar(eq(1L), any(DadosAtualizacaoTopico.class))).willReturn(dadosResposta);

        mockMvc.perform(put("/topicos/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Novo título"))
                .andExpect(jsonPath("$.mensagem").value("Nova mensagem"));
    }

    @Test
    @DisplayName("PUT /topicos/{id} - deve retornar 400 quando título está em branco")
    void atualizar_tituloBranco_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosAtualizacaoTopico("", "Mensagem válida", 1L);

        mockMvc.perform(put("/topicos/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("titulo"));
    }

    @Test
    @DisplayName("PUT /topicos/{id} - deve retornar 404 quando tópico não existe")
    void atualizar_idInexistente_deveRetornar404() throws Exception {
        var dadosEntrada = new DadosAtualizacaoTopico("Título", "Mensagem", 1L);

        given(service.atualizar(eq(999L), any())).willThrow(new EntityNotFoundException("Tópico não encontrado"));

        mockMvc.perform(put("/topicos/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tópico não encontrado"));
    }

    @Test
    @DisplayName("PUT /topicos/{id} - deve retornar 409 quando dados causam duplicidade")
    void atualizar_topicoDuplicado_deveRetornar409() throws Exception {
        var dadosEntrada = new DadosAtualizacaoTopico("Título duplicado", "Mensagem duplicada", 1L);

        given(service.atualizar(eq(1L), any())).willThrow(new TopicoDuplicadoException());

        mockMvc.perform(put("/topicos/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // =========================================================
    // DELETE /topicos/{id} — remover
    // =========================================================

    @Test
    @DisplayName("DELETE /topicos/{id} - deve retornar 204 No Content quando ID existe")
    void remover_idValido_deveRetornar204() throws Exception {
        willDoNothing().given(service).deletar(1L);

        mockMvc.perform(delete("/topicos/1").with(csrf()))
                .andExpect(status().isNoContent());

        then(service).should().deletar(1L);
    }

    @Test
    @DisplayName("DELETE /topicos/{id} - deve retornar 404 quando ID não existe")
    void remover_idInexistente_deveRetornar404() throws Exception {
        willThrow(new EntityNotFoundException("Tópico não encontrado")).given(service).deletar(999L);

        mockMvc.perform(delete("/topicos/999").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tópico não encontrado"));
    }
}
