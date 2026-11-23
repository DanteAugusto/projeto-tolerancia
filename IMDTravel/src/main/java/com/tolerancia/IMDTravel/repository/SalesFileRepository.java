package com.tolerancia.IMDTravel.repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.tolerancia.IMDTravel.dto.SaleRecord;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

@Repository
public class SalesFileRepository {

    private final Path filePath;
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public SalesFileRepository(
            @Value("${imdtravel.sales.file:/data/sales.jsonl}") String filePath,
            ObjectMapper objectMapper) throws IOException {
        this.filePath = Path.of(filePath);
        this.objectMapper = objectMapper;
        Path parent = this.filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (Files.notExists(this.filePath)) {
            Files.createFile(this.filePath);
        }
    }

    public void append(SaleRecord record) {
        lock.writeLock().lock();
        try {
            String jsonLine = objectMapper.writeValueAsString(record) + System.lineSeparator();
            Files.writeString(
                    filePath,
                    jsonLine,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gravar venda no arquivo local", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<SaleRecord> findAll() {
        lock.readLock().lock();
        try (Stream<String> lines = Files.lines(filePath)) {
            return lines
                    .filter(line -> !line.isBlank())
                    .map(line -> objectMapper.readValue(line, SaleRecord.class))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao ler vendas do arquivo local", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isWritable() {
        return Files.isWritable(filePath);
    }
}
