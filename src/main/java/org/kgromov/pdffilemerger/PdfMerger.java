package org.kgromov.pdffilemerger;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Component
@Slf4j
public class PdfMerger {

    @SneakyThrows
    public File mergeFiles(List<File> pdfFiles) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("merge-pdf-files");
        try {
            PDFMergerUtility pdfMerger = new PDFMergerUtility();
            pdfFiles.forEach(file -> {
                try {
                    var bufferedFile = new RandomAccessReadBufferedFile(file);
                    pdfMerger.addSource(bufferedFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            File resultFile = File.createTempFile("result", ".pdf");
            pdfMerger.setDestinationFileName(resultFile.getAbsolutePath());
//        pdfMerger.mergeDocuments(() -> new ScratchFile(resultFile.getParentFile()));    // 328 ms
            pdfMerger.mergeDocuments(null);                        // 350 ms
            return resultFile;
        } finally {
            stopWatch.stop();
            log.info("Time to merge {} pfd files took {} ms", pdfFiles.size(), stopWatch.getLastTaskTimeMillis());
        }
    }
}
