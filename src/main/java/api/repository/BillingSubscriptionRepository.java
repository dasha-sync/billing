package api.repository;

import api.model.BillingSubscription;
import api.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingSubscriptionRepository extends JpaRepository<BillingSubscription, Long> {
  Optional<BillingSubscription> findByUserAndStatus(User user, BillingSubscription.SubscriptionStatus status);

  Optional<BillingSubscription> findByUser(User user);

  List<BillingSubscription> findByStatus(BillingSubscription.SubscriptionStatus status);

  @Query("SELECT bs FROM BillingSubscription bs JOIN FETCH bs.user WHERE bs.status = :status")
  List<BillingSubscription> findByStatusWithUser(@Param("status") BillingSubscription.SubscriptionStatus status);

}
