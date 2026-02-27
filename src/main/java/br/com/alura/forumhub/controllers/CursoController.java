package br.com.alura.forumhub.controllers;

import br.com.alura.forumhub.dto.curso.DadosAtualizacaoCurso;
import br.com.alura.forumhub.dto.curso.DadosCadastroCurso;
import br.com.alura.forumhub.dto.curso.DadosDetalhamentoCurso;
import br.com.alura.forumhub.dto.curso.DadosListagemCurso;
import br.com.alura.forumhub.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private CursoService service;

    @PostMapping
    public ResponseEntity<DadosDetalhamentoCurso> cadastro(@RequestBody @Valid DadosCadastroCurso dados, UriComponentsBuilder uriBuilder) {

        DadosDetalhamentoCurso curso = service.cadastrar(dados);

        var uri = uriBuilder
                .path("/cursos/{id}")
                .buildAndExpand(curso.id())
                .toUri();

        return ResponseEntity.created(uri).body(curso);

    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemCurso>> listagem(@PageableDefault(
            size = 10,
            sort = "nome",
            direction = Sort.Direction.ASC
    ) Pageable paginacao) {

        var page = service.listar(paginacao);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoCurso> detalhamento(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoCurso> atualizacao(@PathVariable Long id, @RequestBody @Valid DadosAtualizacaoCurso dados) {
        return ResponseEntity.ok(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remocao(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
