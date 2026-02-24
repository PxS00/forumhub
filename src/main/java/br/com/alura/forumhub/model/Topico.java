package br.com.alura.forumhub.model;

import br.com.alura.forumhub.dto.DadosCadastroTopico;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "topicos",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"titulo", "mensagem"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Topico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String mensagem;

    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    private StatusTopico status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY)
    private Curso curso;

    @OneToMany(mappedBy = "topico", cascade = CascadeType.ALL)
    private List<Resposta> respostas = new ArrayList<>();

    public Topico(DadosCadastroTopico dados) {
        this.titulo = dados.titulo();
        this.mensagem = dados.mensagem();
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusTopico.NAO_RESPONDIDO;
        // O autor e o curso serão definidos posteriormente, após a criação do tópico, para garantir que ambos existam no banco de dados
        // respostas inicia como lista vazia, pois o tópico ainda não tem respostas no momento da criação
    }

    public void definirAutorECurso(Usuario autor, Curso curso) {
        this.autor = autor;
        this.curso = curso;
    }
}
