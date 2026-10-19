
package com.shopsense.commonservice.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shopsense.commonservice.enums.EventSource;
import com.shopsense.commonservice.enums.EventType;
import com.shopsense.commonservice.util.UuidV7Generator;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class KafkaBaseEvent {

    @Builder.Default
    private UUID eventId = UuidV7Generator.generate();

    private UUID correlationId;

    private String idempotencyKey;

    private EventType eventType;

    private EventSource eventSource;

    @Builder.Default
    private Instant occurredAt = Instant.now();

    @Builder.Default
    private Integer version = 1;
}
