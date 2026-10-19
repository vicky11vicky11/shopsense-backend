package com.shopsense.orchestratorservice.repository;

import com.shopsense.orchestratorservice.entity.SagaCommandRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface SagaCommandRecordRepository extends JpaRepository<SagaCommandRecord, UUID> {
    @Query("select c from SagaCommandRecord c where c.publishedAt is null order by c.createdAt")
    List<SagaCommandRecord> findUnpublished( Pageable pageable );
}
