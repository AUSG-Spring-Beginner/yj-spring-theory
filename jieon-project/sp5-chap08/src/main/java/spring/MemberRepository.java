package spring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    @Query("SELECT p FROM Member p ORDER BY p.email DESC")
    Optional<Member> findByEmail(String email);
}
