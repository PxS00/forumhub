package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.DadosCadastroTopico;
import br.com.alura.forumhub.dto.DadosDetalhamentoTopico;
import br.com.alura.forumhub.model.Curso;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.CursoRepository;
import br.com.alura.forumhub.repository.TopicoRepository;
import br.com.alura.forumhub.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
    public class TopicoService {

        @Autowired
        private TopicoRepository topicoRepository;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private CursoRepository cursoRepository;

    @Transactional
    public DadosDetalhamentoTopico cadastrar(DadosCadastroTopico dados) {

        Usuario autor = usuarioRepository.findById(dados.idAutor())
                .orElseThrow(() -> new EntityNotFoundException("Autor não encontrado"));

        Curso curso = cursoRepository.findById(dados.idCurso())
                .orElseThrow(() -> new EntityNotFoundException("Curso não encontrado"));

        if (topicoRepository.existsByTituloAndMensagem(
                dados.titulo(),
                dados.mensagem())) {

            throw new RuntimeException("Tópico duplicado");
        }

        Topico topico = new Topico(dados);
        topico.definirAutorECurso(autor, curso);

        topicoRepository.save(topico);

        return new DadosDetalhamentoTopico(topico);
    }

}