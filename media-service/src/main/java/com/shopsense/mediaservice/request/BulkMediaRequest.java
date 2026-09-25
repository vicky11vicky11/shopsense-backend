package com.shopsense.mediaservice.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BulkMediaRequest {

    @NotEmpty(message = "Media IDs are required")
    @Size(max = 10, message = "Maximum 10 media IDs are allowed")
    private List<UUID> ids;

}
