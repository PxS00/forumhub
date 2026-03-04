package br.com.alura.forumhub.controllers;

import br.com.alura.forumhub.dto.resposta.DadosAtualizacaoResposta;
import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import br.com.alura.forumhub.dto.resposta.DadosDetalhamentoResposta;
import br.com.alura.forumhub.dto.resposta.DadosListagemResposta;
import br.com.alura.forumhub.service.RespostaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/respostas")
public class RespostaController {

    private final RespostaService service;

    public RespostaController(RespostaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DadosDetalhamentoResposta> cadastro(@RequestBody @Valid DadosCadastroResposta dados, UriComponentsBuilder uriBuilder) {

        DadosDetalhamentoResposta resposta = service.cadastrar(dados);

        var uri = uriBuilder
                .path("/resposta/{id}")
                .buildAndExpand(resposta.id())
                .toUri();

        return ResponseEntity.created(uri).body(resposta);
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemResposta>> listagem(@PageableDefault(size = 10, sort = "dataCriacao", direction = Sort.Direction.ASC) Pageable paginacao) {
        var page = service.listar(paginacao);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoResposta> detalhamento(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoResposta> atualizacao(@PathVariable Long id, @RequestBody @Valid DadosAtualizacaoResposta dados) {
        return ResponseEntity.ok(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> exclusao(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
