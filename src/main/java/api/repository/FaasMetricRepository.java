package api.repository;

import api.model.FaasMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaasMetricRepository extends JpaRepository<FaasMetric, Long> {
}
