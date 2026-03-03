package br.com.alura.forumhub.controllers;

import br.com.alura.forumhub.dto.curso.DadosAtualizacaoCurso;
import br.com.alura.forumhub.dto.curso.DadosCadastroCurso;
import br.com.alura.forumhub.dto.curso.DadosDetalhamentoCurso;
import br.com.alura.forumhub.dto.curso.DadosListagemCurso;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.service.CursoService;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@WithMockUser
@ActiveProfiles("test")
@DisplayName("Testes de integração do CursoController")
class CursoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CursoService service;

    // =========================================================
    // POST /cursos — cadastrar
    // =========================================================

    @Test
    @DisplayName("POST /cursos - deve retornar 201 Created com corpo quando dados válidos")
    void cadastrar_dadosValidos_deveRetornar201() throws Exception {
        var dadosEntrada = new DadosCadastroCurso("Spring Boot", "Programação");
        var dadosResposta = new DadosDetalhamentoCurso(1L, "Spring Boot", "Programação");

        given(service.cadastrar(any(DadosCadastroCurso.class))).willReturn(dadosResposta);

        mockMvc.perform(post("/cursos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/cursos/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Spring Boot"))
                .andExpect(jsonPath("$.categoria").value("Programação"));
    }

    @Test
    @DisplayName("POST /cursos - deve retornar 400 quando nome está em branco")
    void cadastrar_nomeBranco_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroCurso("", "Programação");

        mockMvc.perform(post("/cursos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("nome"));
    }

    @Test
    @DisplayName("POST /cursos - deve retornar 400 quando categoria está em branco")
    void cadastrar_categoriaBranca_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroCurso("Spring Boot", "");

        mockMvc.perform(post("/cursos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("categoria"));
    }

    @Test
    @DisplayName("POST /cursos - deve retornar 400 quando curso já existe (duplicado)")
    void cadastrar_cursoDuplicado_deveRetornar400() throws Exception {
        var dadosEntrada = new DadosCadastroCurso("Spring Boot", "Programação");

        given(service.cadastrar(any())).willThrow(new ValidacaoException("Curso já existe"));

        mockMvc.perform(post("/cursos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Curso já existe"));
    }

    // =========================================================
    // GET /cursos — listar
    // =========================================================

    @Test
    @DisplayName("GET /cursos - deve retornar 200 com página de cursos")
    void listar_deveRetornar200ComPagina() throws Exception {
        var curso = new DadosListagemCurso(1L, "Spring Boot", "Programação");
        var pagina = new PageImpl<>(List.of(curso), PageRequest.of(0, 10), 1);

        given(service.listar(any())).willReturn(pagina);

        mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome").value("Spring Boot"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /cursos - deve retornar 200 com lista vazia quando não há cursos")
    void listar_semCursos_deveRetornar200ComListaVazia() throws Exception {
        given(service.listar(any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // =========================================================
    // GET /cursos/{id} — detalhar
    // =========================================================

    @Test
    @DisplayName("GET /cursos/{id} - deve retornar 200 com detalhamento quando ID existe")
    void detalhar_idValido_deveRetornar200() throws Exception {
        var detalhes = new DadosDetalhamentoCurso(1L, "Spring Boot", "Programação");

        given(service.detalhar(1L)).willReturn(detalhes);

        mockMvc.perform(get("/cursos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Spring Boot"))
                .andExpect(jsonPath("$.categoria").value("Programação"));
    }

    @Test
    @DisplayName("GET /cursos/{id} - deve retornar 404 quando ID não existe")
    void detalhar_idInexistente_deveRetornar404() throws Exception {
        given(service.detalhar(999L)).willThrow(new EntityNotFoundException("Curso não encontrado"));

        mockMvc.perform(get("/cursos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Curso não encontrado"));
    }

    // =========================================================
    // PUT /cursos/{id} — atualizar
    // =========================================================

    @Test
    @DisplayName("PUT /cursos/{id} - deve retornar 200 com dados atualizados quando dados válidos")
    void atualizar_dadosValidos_deveRetornar200() throws Exception {
        var dadosEntrada = new DadosAtualizacaoCurso("Spring Framework", "Back-end");
        var dadosResposta = new DadosDetalhamentoCurso(1L, "Spring Framework", "Back-end");

        given(service.atualizar(eq(1L), any(DadosAtualizacaoCurso.class))).willReturn(dadosResposta);

        mockMvc.perform(put("/cursos/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Spring Framework"))
                .andExpect(jsonPath("$.categoria").value("Back-end"));
    }

    @Test
    @DisplayName("PUT /cursos/{id} - deve retornar 400 quando nome está em branco")
    void atualizar_nomeBranco_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosAtualizacaoCurso("", "Categoria válida");

        mockMvc.perform(put("/cursos/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("nome"));
    }

    @Test
    @DisplayName("PUT /cursos/{id} - deve retornar 404 quando curso não existe")
    void atualizar_idInexistente_deveRetornar404() throws Exception {
        var dadosEntrada = new DadosAtualizacaoCurso("Nome", "Categoria");

        given(service.atualizar(eq(999L), any())).willThrow(new EntityNotFoundException("Curso não encontrado"));

        mockMvc.perform(put("/cursos/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Curso não encontrado"));
    }

    // =========================================================
    // DELETE /cursos/{id} — remover
    // =========================================================

    @Test
    @DisplayName("DELETE /cursos/{id} - deve retornar 204 No Content quando ID existe")
    void remover_idValido_deveRetornar204() throws Exception {
        willDoNothing().given(service).deletar(1L);

        mockMvc.perform(delete("/cursos/1").with(csrf()))
                .andExpect(status().isNoContent());

        then(service).should().deletar(1L);
    }

    @Test
    @DisplayName("DELETE /cursos/{id} - deve retornar 404 quando ID não existe")
    void remover_idInexistente_deveRetornar404() throws Exception {
        willThrow(new EntityNotFoundException("Curso não encontrado")).given(service).deletar(999L);

        mockMvc.perform(delete("/cursos/999").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Curso não encontrado"));
    }

    // =========================================================
    // 401 — não autenticado
    // =========================================================

    @Test
    @DisplayName("GET /cursos - deve retornar 401 quando não autenticado")
    @org.springframework.security.test.context.support.WithAnonymousUser
    void listar_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/cursos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /cursos - deve retornar 401 quando não autenticado")
    @org.springframework.security.test.context.support.WithAnonymousUser
    void cadastrar_semAutenticacao_deveRetornar401() throws Exception {
        var dadosEntrada = new DadosCadastroCurso("Spring Boot", "Programação");

        mockMvc.perform(post("/cursos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isUnauthorized());
    }
}



