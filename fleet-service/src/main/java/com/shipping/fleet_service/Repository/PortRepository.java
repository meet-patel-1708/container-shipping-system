    package com.shipping.fleet_service.Repository;

    import com.shipping.fleet_service.Entity.Port;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;

    @Repository
    public interface PortRepository extends JpaRepository<Port,Long> {
        boolean existsByPortCode(String PortCode);
        Optional<Port> findByPortCode(String portCode);
        Optional<Port> findByPortName(String portName);
    }
