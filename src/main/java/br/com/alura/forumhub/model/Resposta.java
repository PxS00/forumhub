package br.com.alura.forumhub.model;

import br.com.alura.forumhub.dto.resposta.DadosAtualizacaoResposta;
import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "respostas")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mensagem;

    private LocalDateTime dataCriacao;

    private Boolean solucao;

    @ManyToOne
    private Topico topico;

    @ManyToOne
    private Usuario autor;

    public Resposta(DadosCadastroResposta dados) {
        this.mensagem = dados.mensagem();
        this.dataCriacao = LocalDateTime.now();
        this.solucao = false;
    }

    public void definirTopicoEAutor(Topico topico, Usuario autor) {
        this.topico = topico;
        this.autor = autor;
    }

    public void atualizarDados(DadosAtualizacaoResposta dados) {

        this.mensagem = dados.mensagem();
    }
}
