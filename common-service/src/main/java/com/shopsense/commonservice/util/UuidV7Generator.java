package com.shopsense.commonservice.util;

import com.fasterxml.uuid.Generators;

import java.util.UUID;

public final class UuidV7Generator {

    private UuidV7Generator() {
    }

    public static UUID generate() {
        return Generators.timeBasedEpochGenerator()
                .generate();
    }

}
