package cl.powernut.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.powernut.model.RefreshToken;





public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{

    Optional<RefreshToken> findByToken(String token);
    
}
