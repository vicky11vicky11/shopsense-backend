package com.shopsense.orchestratorservice.repository;

import com.shopsense.orchestratorservice.entity.SagaEventRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SagaEventRecordRepository extends JpaRepository<SagaEventRecord, UUID> {
    boolean existsByEventId( UUID eventId );
}
