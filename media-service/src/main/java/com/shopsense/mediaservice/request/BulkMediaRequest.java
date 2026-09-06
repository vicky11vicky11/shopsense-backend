package com.shopsense.mediaservice.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkMediaRequest {

    @NotEmpty(message = "Media IDs are required")
    @Size(max = 1000, message = "Maximum 1000 media IDs are allowed")
    private List<String> ids;

}
