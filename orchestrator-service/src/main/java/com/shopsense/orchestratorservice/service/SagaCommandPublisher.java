package com.shopsense.orchestratorservice.service;

import com.shopsense.orchestratorservice.entity.SagaCommandRecord;
import com.shopsense.orchestratorservice.repository.SagaCommandRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaCommandPublisher {

    private final SagaCommandRecordRepository commands;

    private final KafkaTemplate<String, Object> kafka;

    @Scheduled(fixedDelayString = "1000")
    @SchedulerLock(name = "publishSagaCommands", lockAtMostFor = "5m", lockAtLeastFor = "0s")
    @Transactional
    public void publishPending() {
        for ( SagaCommandRecord command : commands.findUnpublished(PageRequest.of(0, 100)) ) publish(command);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void publishImmediatelyAfterCommit( SagaCommandRecord queuedCommand ) {
        commands.findById(queuedCommand.getId()).ifPresent(this::publish);
    }

    private synchronized void publish( SagaCommandRecord command ) {
        if ( command.getPublishedAt() != null ) return;
        try {
            kafka.send(command.getTopic(), command.getOrderId().toString(), command.getPayload()).get();
            command.setPublishedAt(Instant.now());
        } catch ( Exception exception ) {
            command.setAttempts(command.getAttempts() + 1);
            log.warn("Unable to publish saga command {}", command.getId(), exception);
        }
    }
}
