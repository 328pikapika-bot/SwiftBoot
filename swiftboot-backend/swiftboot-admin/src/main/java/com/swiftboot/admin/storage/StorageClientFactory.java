package com.swiftboot.admin.storage;

import com.swiftboot.common.core.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolve storage client by type.
 */
@Component
public class StorageClientFactory {

    private final Map<String, StorageClient> clientMap;

    public StorageClientFactory(List<StorageClient> clients) {
        this.clientMap = clients.stream().collect(Collectors.toMap(StorageClient::getType, Function.identity()));
    }

    public StorageClient getClient(String type) {
        StorageClient client = clientMap.get(type);
        if (client == null) {
            throw new BusinessException("Unsupported storage type: " + type);
        }
        return client;
    }
}
