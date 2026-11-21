package cl.powernut.dto;

 //lo que devuelve la api
import cl.powernut.model.Usuario;

public record UsuarioResponse(Long id, String nombre, String correo, String region, String comuna, String rol) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(),
                u.getRegion(), u.getComuna(), u.getRol().name());
    }
}