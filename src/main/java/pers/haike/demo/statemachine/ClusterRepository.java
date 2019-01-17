package pers.haike.demo.statemachine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pers.haike.demo.statemachine.entity.Cluster;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, String> {
}
