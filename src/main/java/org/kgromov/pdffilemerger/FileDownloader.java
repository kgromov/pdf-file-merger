package org.kgromov.pdffilemerger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringComponent
@Slf4j
@UIScope
@RequiredArgsConstructor
public class FileDownloader extends VerticalLayout {

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (attachEvent.isInitialAttach()) {
            setMargin(true);
        }
    }

    public void addLinkToDownload(File file) {
        removeAll();
        H4 downloadHeader = new H4("Download result file:");
        StreamResource streamResource = new StreamResource(file.getName(), () -> getStream(file));
        Anchor link = new Anchor(streamResource, file.getName());
        link.getElement().setAttribute("download", true);
        add(downloadHeader, link);
    }

    private InputStream getStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
           log.error("Can't read file {}", file);
           throw new RuntimeException(e);
        }
    }
}
