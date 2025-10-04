package api.repository;

import api.model.BillingSubscription;
import api.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BillingSubscriptionRepository extends JpaRepository<BillingSubscription, Long> {
  Optional<BillingSubscription> findByUserAndStatus(User user, BillingSubscription.SubscriptionStatus status);

  Optional<BillingSubscription> findByUser(User user);
}
