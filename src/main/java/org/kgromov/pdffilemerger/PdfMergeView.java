package org.kgromov.pdffilemerger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Route("")
@PageTitle("Merge pdf files")
@Slf4j
@RequiredArgsConstructor
public class PdfMergeView extends VerticalLayout {
    private final PdfMerger pdfMerger;
    private final FileDownloader downloader;
    private MultiFileMemoryBuffer memoryBuffer;
    private Upload fileUploader;
    private Span errorField;
    private final Button uploadAllButton = new Button("Upload");

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (attachEvent.isInitialAttach()) {
            this.configureFileUploader();
            add(fileUploader, errorField, uploadAllButton, downloader);
        }
    }

    private void configureFileUploader() {
        memoryBuffer = new MultiFileMemoryBuffer();
        fileUploader = new Upload(memoryBuffer);
        fileUploader.setAutoUpload(false);
        fileUploader.setMaxFiles(10);
        fileUploader.setAcceptedFileTypes("application/pdf", ".pdf");
        fileUploader.setDropLabel(new Span("Drop pdf files to merge"));
        errorField = new Span();
        errorField.setVisible(false);
        errorField.getStyle().set("color", "red");
        disableAutoUpload();
        // listeners
        List<String> fileNamesSorted = new ArrayList<>();
        fileUploader.addFailedListener(event -> this.showErrorMessage(event.getReason().getMessage()));
        fileUploader.addFileRejectedListener(event -> this.showErrorMessage(event.getErrorMessage()));
        fileUploader.addStartedListener(event -> {
            log.info("Start processing file = {}", event.getFileName());
            fileNamesSorted.add(event.getFileName());
            this.errorField.setVisible(false);
        });
        fileUploader.addAllFinishedListener(event -> {
            var filesToMerge = memoryBuffer.getFiles()
                    .stream()
                    .sorted(Comparator.comparingInt(fileNamesSorted::indexOf))
                    .map(fileName -> this.createTempFile(memoryBuffer.getInputStream(fileName), fileName))
                    .toList();
            File resultFile = pdfMerger.mergeFiles(filesToMerge);
            downloader.addLinkToDownload(resultFile);
            fileUploader.clearFileList();
        });
    }

    private void disableAutoUpload() {
        UploadI18N i18n = new UploadI18N();
        i18n.setAddFiles(new UploadI18N.AddFiles());
        i18n.getAddFiles().setMany("Select Files...");
        fileUploader.setI18n(i18n);
        uploadAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        uploadAllButton.addClickListener(event -> {
            // No explicit Flow API for this at the moment
            fileUploader.getElement().callJsFunction("uploadFiles");
        });
        add(uploadAllButton);
    }

    private void showErrorMessage(String message) {
        errorField.setVisible(true);
        errorField.setText(message);
    }

    @SneakyThrows
    public File createTempFile(InputStream in, String fileNameWithExtension) {
        String fileName = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));
        String extension = fileNameWithExtension.substring(fileNameWithExtension.lastIndexOf('.'));
        final File tempFile = File.createTempFile(fileName, extension);
        tempFile.deleteOnExit();
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }
}
