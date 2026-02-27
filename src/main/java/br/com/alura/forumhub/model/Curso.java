package br.com.alura.forumhub.model;

import br.com.alura.forumhub.dto.curso.DadosAtualizacaoCurso;
import br.com.alura.forumhub.dto.curso.DadosCadastroCurso;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cursos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String categoria;

    public Curso(DadosCadastroCurso dados) {
        this.nome = dados.nome();
        this.categoria = dados.categoria();
    }

    public void atualizarDados(@Valid DadosAtualizacaoCurso dados) {

        this.nome = dados.nome();
        this.categoria = dados.categoria();
    }
}
