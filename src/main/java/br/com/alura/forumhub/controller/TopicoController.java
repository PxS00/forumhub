package br.com.alura.forumhub.controller;

import br.com.alura.forumhub.dto.DadosCadastroTopico;
import br.com.alura.forumhub.dto.DadosDetalhamentoTopico;
import br.com.alura.forumhub.service.TopicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/topicos")
public class TopicoController {

    @Autowired
    private TopicoService service;

    @PostMapping
    public ResponseEntity<DadosDetalhamentoTopico> cadastrar(@RequestBody @Valid DadosCadastroTopico dados, UriComponentsBuilder uriBuilder) {

    // Constrói a URI do recurso recém-criado (ex.: /topicos/{id}) e retorna HTTP 201 Created com o header Location apontando para onde o novo recurso pode ser acessado

        DadosDetalhamentoTopico topico = service.cadastrar(dados);

        var uri = uriBuilder
                .path("/topicos/{id}")
                .buildAndExpand(topico.id())
                .toUri();

        return ResponseEntity.created(uri).body(topico);
    }

}
