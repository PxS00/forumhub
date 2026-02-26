package br.com.alura.forumhub.service.validation.topico.cadastrar;

import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;
import br.com.alura.forumhub.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidarAutorExiste implements ValidationCadastroTopico{

    @Autowired
    UsuarioRepository usuarioRepository;

    @Override
    public void validar(DadosCadastroTopico dados) {
        boolean autorExiste = usuarioRepository.existsById(dados.idAutor());
        if (!autorExiste) {
            throw new EntityNotFoundException("Autor não encontrado");
        }
    }
}
