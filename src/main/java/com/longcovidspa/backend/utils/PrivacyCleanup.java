package com.longcovidspa.backend.utils;

import com.longcovidspa.backend.repositories.ExportJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PrivacyCleanup {
    private final ExportJobRepository exportRepo;

    // nightly
    @Scheduled(cron="0 30 2 * * *")
    public void purgeExpiredExports() {
        var expired = exportRepo.findByExpiresAtBefore(Instant.now());
        for (var j : expired) {
            try {
                if (j.getFilePath()!=null) Files.deleteIfExists(Path.of(j.getFilePath()));
            } catch (Exception ignored) {}
            exportRepo.delete(j);
        }
    }
}
