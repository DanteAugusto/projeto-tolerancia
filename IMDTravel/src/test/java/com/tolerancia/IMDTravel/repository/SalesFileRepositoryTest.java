package com.tolerancia.IMDTravel.repository;

import com.tolerancia.IMDTravel.dto.SaleRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalesFileRepositoryTest {

    @Test
    void appendAndFindAllShouldPersistAndReadRecords(@TempDir Path tempDir) throws Exception {
        Path salesFile = tempDir.resolve("sales.jsonl");
        SalesFileRepository repository = new SalesFileRepository(salesFile.toString(), new ObjectMapper());

        assertTrue(repository.isWritable());
        assertTrue(repository.findAll().isEmpty());

        SaleRecord record = new SaleRecord(
                "cid-test-1",
                "tx-123",
                "IMD100",
                LocalDate.of(2026, 12, 1),
                "ana",
                new BigDecimal("350.50"),
                new BigDecimal("1927.75"),
                Instant.parse("2026-11-23T12:00:00Z")
        );

        repository.append(record);

        List<SaleRecord> all = repository.findAll();
        assertEquals(1, all.size());
        assertEquals(record, all.get(0));
    }
}
