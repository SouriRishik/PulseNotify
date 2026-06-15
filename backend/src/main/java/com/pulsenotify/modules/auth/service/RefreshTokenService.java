package com.pulsenotify.modules.auth.service;

import com.pulsenotify.exception.TokenRefreshException;
import com.pulsenotify.modules.auth.entity.RefreshToken;
import com.pulsenotify.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    @Value("${app.jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final UserRepository userRepository;
    private final DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "refresh_tokens";

    public Optional<RefreshToken> findByToken(String token) {
        Map<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("token", AttributeValue.builder().s(token).build());

        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(TABLE_NAME)
                .build();

        try {
            GetItemResponse response = dynamoDbClient.getItem(request);
            if (response.hasItem()) {
                Map<String, AttributeValue> item = response.item();
                RefreshToken rt = new RefreshToken();
                rt.setToken(item.get("token").s());
                rt.setExpiresAt(Instant.parse(item.get("expiresAt").s()));
                rt.setIsRevoked(item.get("isRevoked").bool());
                
                UUID userId = UUID.fromString(item.get("userId").s());
                userRepository.findById(userId).ifPresent(rt::setUser);
                
                return Optional.of(rt);
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
        return Optional.empty();
    }

    public RefreshToken createRefreshToken(UUID userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setIsRevoked(false);

        Map<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("token", AttributeValue.builder().s(refreshToken.getToken()).build());
        itemValues.put("userId", AttributeValue.builder().s(userId.toString()).build());
        itemValues.put("expiresAt", AttributeValue.builder().s(refreshToken.getExpiresAt().toString()).build());
        itemValues.put("isRevoked", AttributeValue.builder().bool(refreshToken.getIsRevoked()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(itemValues)
                .build();

        try {
            dynamoDbClient.putItem(request);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Failed to save refresh token to DynamoDB");
        }

        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().compareTo(Instant.now()) < 0) {
            deleteToken(token.getToken());
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        if (token.getIsRevoked()) {
            throw new TokenRefreshException(token.getToken(), "Refresh token is revoked.");
        }
        return token;
    }
    
    private void deleteToken(String token) {
        Map<String, AttributeValue> keyToDelete = new HashMap<>();
        keyToDelete.put("token", AttributeValue.builder().s(token).build());

        DeleteItemRequest deleteReq = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(keyToDelete)
                .build();

        dynamoDbClient.deleteItem(deleteReq);
    }

    public int deleteByUserId(UUID userId) {
        // DynamoDB does not support deleting by a non-key attribute easily without querying first.
        // For local development, we can do a Scan and then delete (not for production)
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("userId = :userId")
                .expressionAttributeValues(Map.of(":userId", AttributeValue.builder().s(userId.toString()).build()))
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        int deleted = 0;
        for (Map<String, AttributeValue> item : response.items()) {
            deleteToken(item.get("token").s());
            deleted++;
        }
        return deleted;
    }
}
