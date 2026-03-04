package br.com.alura.forumhub.service.validation.comum;

import br.com.alura.forumhub.dto.DadosComAutor;
import br.com.alura.forumhub.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAutorExiste {

    private final UsuarioRepository usuarioRepository;

    public ValidadorAutorExiste(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void validar(DadosComAutor dados) {

        if (!usuarioRepository.existsById(dados.idAutor())) {
            throw new EntityNotFoundException("Autor não encontrado");
        }
    }
}
