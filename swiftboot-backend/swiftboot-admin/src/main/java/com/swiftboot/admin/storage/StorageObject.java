package com.swiftboot.admin.storage;

import lombok.Getter;

import java.io.InputStream;

/**
 * Opened object resource from storage.
 */
@Getter
public class StorageObject implements AutoCloseable {

    private final InputStream inputStream;

    private final Long contentLength;

    private final String contentType;

    private final AutoCloseable closeAction;

    public StorageObject(InputStream inputStream, Long contentLength, String contentType, AutoCloseable closeAction) {
        this.inputStream = inputStream;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.closeAction = closeAction;
    }

    @Override
    public void close() throws Exception {
        if (closeAction != null) {
            closeAction.close();
        } else if (inputStream != null) {
            inputStream.close();
        }
    }
}
