package api.repository;

import api.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findUserByUsername(String username);

  Optional<User> findUserByEmail(String email);

  Boolean existsUserByUsername(String username);

  Boolean existsUserByEmail(String email);

  List<User> findAllByOrderByUsernameAsc();

  List<User> findByUsernameContainingIgnoreCaseAndEmailContainingIgnoreCase(String username,
                                                                            String email);
}
