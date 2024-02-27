package org.kgromov.pdffilemerger;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

@SpringComponent
@Slf4j
@UIScope
@RequiredArgsConstructor
public class FileUploader extends VerticalLayout {
    private Upload fileUploader;

    // TODO: add publish event on all finished (uploaded)
    public List<File> getUploadedFiles() {
        fileUploader.addAllFinishedListener(event -> {
            // publish event; init uploaded files
        });
        return null;
    }
}
