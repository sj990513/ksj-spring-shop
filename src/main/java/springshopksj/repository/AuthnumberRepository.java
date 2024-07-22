package springshopksj.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Authnumber;

@Repository
public interface AuthnumberRepository extends JpaRepository<Authnumber, Long> {
    Authnumber findByPhonenumber(String phoneNumber);
    @Transactional
    void deleteByPhonenumber(String phoneNumber);
}