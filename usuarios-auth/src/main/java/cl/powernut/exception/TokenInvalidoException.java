package cl.powernut.exception;

public class TokenInvalidoException extends RuntimeException{

    public TokenInvalidoException(){
        super("Refresh token inválido, vencido o revocado");
    }
    
}
