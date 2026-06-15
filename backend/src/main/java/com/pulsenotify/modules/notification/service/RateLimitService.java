package com.pulsenotify.modules.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "rate_limits";
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    @PostConstruct
    public void initTable() {
        try {
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("user_id_minute").attributeType(ScalarAttributeType.S).build()
                    )
                    .keySchema(
                            KeySchemaElement.builder().attributeName("user_id_minute").keyType(KeyType.HASH).build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                    .build());
            log.info("DynamoDB table '{}' created successfully.", TABLE_NAME);
        } catch (ResourceInUseException e) {
            log.info("DynamoDB table '{}' already exists.", TABLE_NAME);
        }
    }

    public boolean isRateLimited(UUID userId) {
        String currentMinute = Instant.now().truncatedTo(ChronoUnit.MINUTES).toString();
        String key = userId.toString() + "#" + currentMinute;

        Map<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put("user_id_minute", AttributeValue.builder().s(key).build());

        Map<String, AttributeValueUpdate> updates = new HashMap<>();
        updates.put("request_count", AttributeValueUpdate.builder()
                .action(AttributeAction.ADD)
                .value(AttributeValue.builder().n("1").build())
                .build());

        try {
            UpdateItemRequest request = UpdateItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(keyMap)
                    .attributeUpdates(updates)
                    .returnValues(ReturnValue.UPDATED_NEW)
                    .build();

            UpdateItemResponse response = dynamoDbClient.updateItem(request);
            int currentCount = Integer.parseInt(response.attributes().get("request_count").n());

            return currentCount > MAX_REQUESTS_PER_MINUTE;
        } catch (DynamoDbException e) {
            log.error("Failed to update rate limit in DynamoDB", e);
            // Fail open to avoid blocking valid requests if DB is down
            return false;
        }
    }
}
