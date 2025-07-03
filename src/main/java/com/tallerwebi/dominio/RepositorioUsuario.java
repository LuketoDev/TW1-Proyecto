package com.tallerwebi.dominio;

public interface RepositorioUsuario {

    Usuario buscarUsuario(String email);

    Usuario buscarUsuarioPorEmailYPassword(String email, String password);

    void guardar(Usuario usuario);
    Usuario buscar(String email);
    void modificar(Usuario usuario);
    void eliminarUsuario(Long idUsuario);
}

