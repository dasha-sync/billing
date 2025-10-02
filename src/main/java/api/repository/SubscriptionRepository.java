package api.repository;

import api.model.Subscription;
import api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserAndStatus(User user, Subscription.SubscriptionStatus status);
    Optional<Subscription> findByUser(User user);
}
