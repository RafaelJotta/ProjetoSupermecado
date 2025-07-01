package br.com.supermercado.service;

import br.com.supermercado.dao.UsuarioDAO;
import br.com.supermercado.dao.impl.UsuarioDAOImpl;
import br.com.supermercado.model.Usuario;
import br.com.supermercado.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioDAO usuarioDAO;

    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    public boolean salvarOuAtualizarUsuario(Usuario usuario, boolean isNewUser, String senhaPlainText) throws IllegalArgumentException {
        validarUsuario(usuario, isNewUser, senhaPlainText);

        if (senhaPlainText != null && !senhaPlainText.trim().isEmpty()) {
            usuario.setSenhaHash(PasswordUtil.hashPassword(senhaPlainText));
        }

        if (isNewUser) {
            logger.info("Tentando salvar novo usuário: {}", usuario.getLogin());
            if (usuarioDAO.findByLogin(usuario.getLogin()).isPresent()) {
                throw new IllegalArgumentException("Login '" + usuario.getLogin() + "' já está em uso.");
            }
            return usuarioDAO.save(usuario) != null;
        } else {
            logger.info("Tentando atualizar usuário ID: {}", usuario.getId());
            Optional<Usuario> existenteComMesmoLogin = usuarioDAO.findByLogin(usuario.getLogin());
            if (existenteComMesmoLogin.isPresent() && !existenteComMesmoLogin.get().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("Login '" + usuario.getLogin() + "' já está em uso por outro usuário.");
            }
            return usuarioDAO.update(usuario);
        }
    }

    public List<Usuario> buscarTodosUsuarios() {
        return usuarioDAO.findAll();
    }

    public boolean ativarDesativarUsuario(int id, boolean ativar) {
        if (!ativar) {
            Optional<Usuario> usuarioOpt = usuarioDAO.findById(id);
            if (usuarioOpt.isPresent() && usuarioOpt.get().getPerfil() == Usuario.PerfilUsuario.GERENTE) {
                long gerentesAtivos = usuarioDAO.findAll().stream()
                        .filter(u -> u.getPerfil() == Usuario.PerfilUsuario.GERENTE && u.isAtivo() && !u.getId().equals(id))
                        .count();
                if (gerentesAtivos == 0) {
                    throw new IllegalStateException("Não é possível desativar o único gerente ativo do sistema.");
                }
            }
        }
        return usuarioDAO.activateDeactivate(id, ativar);
    }

    private void validarUsuario(Usuario usuario, boolean isNewUser, String senhaPlainText) {
        if (usuario.getNomeCompleto() == null || usuario.getNomeCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome completo é obrigatório.");
        }
        if (usuario.getLogin() == null || !LOGIN_PATTERN.matcher(usuario.getLogin().trim()).matches()) {
            throw new IllegalArgumentException("Login inválido. Deve ter entre 3 e 20 caracteres (letras, números ou _).");
        }
        if (isNewUser && (senhaPlainText == null || senhaPlainText.trim().length() < MIN_PASSWORD_LENGTH)) {
            throw new IllegalArgumentException("Senha é obrigatória e deve ter no mínimo " + MIN_PASSWORD_LENGTH + " caracteres.");
        }
        if (senhaPlainText != null && !senhaPlainText.trim().isEmpty() && senhaPlainText.trim().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Nova senha deve ter no mínimo " + MIN_PASSWORD_LENGTH + " caracteres.");
        }
    }
}
