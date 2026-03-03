package br.com.alura.forumhub.controllers;

import br.com.alura.forumhub.dto.usuario.DadosAtualizacaoUsuario;
import br.com.alura.forumhub.dto.usuario.DadosCadastroUsuario;
import br.com.alura.forumhub.dto.usuario.DadosDetalhamentoUsuario;
import br.com.alura.forumhub.dto.usuario.DadosListagemUsuario;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.service.UsuarioService;
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
@DisplayName("Testes de integração do UsuarioController")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService service;

    // =========================================================
    // POST /usuario — cadastrar
    // =========================================================

    @Test
    @DisplayName("POST /usuario - deve retornar 201 Created quando dados válidos")
    void cadastrar_dadosValidos_deveRetornar201() throws Exception {
        var dadosEntrada = new DadosCadastroUsuario("Ana Silva", "ana@email.com", "Senha@123");
        var dadosResposta = new DadosDetalhamentoUsuario(1L, "Ana Silva", "ana@email.com");

        given(service.cadastrar(any(DadosCadastroUsuario.class))).willReturn(dadosResposta);

        mockMvc.perform(post("/usuario")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/usuario/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value("ana@email.com"));
    }

    @Test
    @DisplayName("POST /usuario - deve retornar 400 quando nome está em branco")
    void cadastrar_nomeBranco_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroUsuario("", "ana@email.com", "Senha@123");

        mockMvc.perform(post("/usuario")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /usuario - deve retornar 400 quando e-mail é inválido")
    void cadastrar_emailInvalido_deveRetornar400() throws Exception {
        var dadosInvalidos = new DadosCadastroUsuario("Ana Silva", "email-invalido", "Senha@123");

        mockMvc.perform(post("/usuario")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("email"));
    }

    @Test
    @DisplayName("POST /usuario - deve retornar 400 quando e-mail já está em uso")
    void cadastrar_emailDuplicado_deveRetornar400() throws Exception {
        var dadosEntrada = new DadosCadastroUsuario("Ana Silva", "ana@email.com", "Senha@123");

        given(service.cadastrar(any())).willThrow(
                new ValidacaoException("Já existe um usuário cadastrado com este e-mail"));

        mockMvc.perform(post("/usuario")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe um usuário cadastrado com este e-mail"));
    }

    // =========================================================
    // GET /usuario — listar (apenas ativos)
    // =========================================================

    @Test
    @DisplayName("GET /usuario - deve retornar 200 com página de usuários ativos")
    void listar_deveRetornar200ComPagina() throws Exception {
        var usuario = new DadosListagemUsuario(1L, "Ana Silva", "ana@email.com", true);
        var pagina = new PageImpl<>(List.of(usuario), PageRequest.of(0, 10), 1);

        given(service.listar(any())).willReturn(pagina);

        mockMvc.perform(get("/usuario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome").value("Ana Silva"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // =========================================================
    // GET /usuario/listar-todos — listar todos (admin)
    // =========================================================

    @Test
    @DisplayName("GET /usuario/listar-todos - deve retornar 200 para usuário ADMIN")
    @WithMockUser(roles = "ADMIN")
    void listarTodos_comRoleAdmin_deveRetornar200() throws Exception {
        var usuario = new DadosListagemUsuario(1L, "Ana Silva", "ana@email.com", true);
        var pagina = new PageImpl<>(List.of(usuario), PageRequest.of(0, 10), 1);

        given(service.listarTodos(any())).willReturn(pagina);

        mockMvc.perform(get("/usuario/listar-todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // =========================================================
    // GET /usuario/{id} — detalhar
    // =========================================================

    @Test
    @DisplayName("GET /usuario/{id} - deve retornar 200 com detalhamento quando ID existe")
    void detalhar_idValido_deveRetornar200() throws Exception {
        var detalhes = new DadosDetalhamentoUsuario(1L, "Ana Silva", "ana@email.com");

        given(service.detalhar(1L)).willReturn(detalhes);

        mockMvc.perform(get("/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value("ana@email.com"));
    }

    @Test
    @DisplayName("GET /usuario/{id} - deve retornar 404 quando ID não existe")
    void detalhar_idInexistente_deveRetornar404() throws Exception {
        given(service.detalhar(999L)).willThrow(new EntityNotFoundException("Usuário não encontrado"));

        mockMvc.perform(get("/usuario/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }

    // =========================================================
    // PUT /usuario/{id} — atualizar
    // =========================================================

    @Test
    @DisplayName("PUT /usuario/{id} - deve retornar 200 com dados atualizados quando dados válidos")
    void atualizar_dadosValidos_deveRetornar200() throws Exception {
        var dadosEntrada = new DadosAtualizacaoUsuario("Ana Souza", "ana.souza@email.com", "NovaSenha@1");
        var dadosResposta = new DadosDetalhamentoUsuario(1L, "Ana Souza", "ana.souza@email.com");

        given(service.atualizar(eq(1L), any(DadosAtualizacaoUsuario.class))).willReturn(dadosResposta);

        mockMvc.perform(put("/usuario/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("ana.souza@email.com"));
    }

    @Test
    @DisplayName("PUT /usuario/{id} - deve retornar 404 quando usuário não existe")
    void atualizar_idInexistente_deveRetornar404() throws Exception {
        var dadosEntrada = new DadosAtualizacaoUsuario("Nome", "email@test.com", "Senha@123");

        given(service.atualizar(eq(999L), any())).willThrow(new EntityNotFoundException("Usuário não encontrado"));

        mockMvc.perform(put("/usuario/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }

    @Test
    @DisplayName("PUT /usuario/{id} - deve retornar 400 quando usuário não autorizado")
    void atualizar_usuarioNaoAutorizado_deveRetornar400() throws Exception {
        var dadosEntrada = new DadosAtualizacaoUsuario("Nome", "email@test.com", "Senha@123");

        given(service.atualizar(eq(1L), any())).willThrow(
                new ValidacaoException("Usuário não autorizado"));

        mockMvc.perform(put("/usuario/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosEntrada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usuário não autorizado"));
    }

    // =========================================================
    // DELETE /usuario/{id} — remover (desativar)
    // =========================================================

    @Test
    @DisplayName("DELETE /usuario/{id} - deve retornar 204 No Content quando ID existe")
    void remover_idValido_deveRetornar204() throws Exception {
        willDoNothing().given(service).deletar(1L);

        mockMvc.perform(delete("/usuario/1").with(csrf()))
                .andExpect(status().isNoContent());

        then(service).should().deletar(1L);
    }

    @Test
    @DisplayName("DELETE /usuario/{id} - deve retornar 404 quando ID não existe")
    void remover_idInexistente_deveRetornar404() throws Exception {
        willThrow(new EntityNotFoundException("Usuário não encontrado")).given(service).deletar(999L);

        mockMvc.perform(delete("/usuario/999").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }

    @Test
    @DisplayName("DELETE /usuario/{id} - deve retornar 400 quando usuário não autorizado")
    void remover_usuarioNaoAutorizado_deveRetornar400() throws Exception {
        willThrow(new ValidacaoException("Usuário não autorizado")).given(service).deletar(1L);

        mockMvc.perform(delete("/usuario/1").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usuário não autorizado"));
    }
}

