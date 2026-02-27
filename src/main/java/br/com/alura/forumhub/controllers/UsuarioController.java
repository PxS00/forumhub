package br.com.alura.forumhub.controllers;

import br.com.alura.forumhub.dto.usuario.DadosCadastroUsuario;
import br.com.alura.forumhub.dto.usuario.DadosDetalhamentoUsuario;
import br.com.alura.forumhub.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<DadosDetalhamentoUsuario> cadastrarUsuario(@RequestBody @Valid DadosCadastroUsuario dados, UriComponentsBuilder uriBuilder) {

        DadosDetalhamentoUsuario usuario = usuarioService.cadastrar(dados);

        var uri = uriBuilder
                .path("/usuario/{id}")
                .buildAndExpand(usuario.id())
                .toUri();

        return ResponseEntity.created(uri).body(usuario);
    }

}
