package br.com.alura.forumhub.service.validation.comum;

import br.com.alura.forumhub.dto.DadosComAutor;
import br.com.alura.forumhub.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAutorExiste {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void validar(DadosComAutor dados) {

        if (!usuarioRepository.existsById(dados.idAutor())) {
            throw new EntityNotFoundException("Autor não encontrado");
        }
    }
}
