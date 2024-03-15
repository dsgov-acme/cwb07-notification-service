package io.nuvalence.platform.notification.service.controller;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.platform.notification.service.domain.DirectMessage;
import io.nuvalence.platform.notification.service.domain.Message;
import io.nuvalence.platform.notification.service.domain.UserMessage;
import io.nuvalence.platform.notification.service.generated.controllers.SendNotificationApiDelegate;
import io.nuvalence.platform.notification.service.generated.models.DirectMessageRequestModel;
import io.nuvalence.platform.notification.service.generated.models.DirectMessageResponseModel;
import io.nuvalence.platform.notification.service.generated.models.UserMessageRequestModel;
import io.nuvalence.platform.notification.service.generated.models.UserMessageResponseModel;
import io.nuvalence.platform.notification.service.mapper.MessageMapper;
import io.nuvalence.platform.notification.service.service.MessageService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of SendNotificationApiDelegate.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SendNotificationApiDelegateImpl implements SendNotificationApiDelegate {

    private final MessageMapper messageMapper;
    private final MessageService messageService;
    private final AuthorizationHandler authorizationHandler;

    @Override
    public ResponseEntity<UserMessageResponseModel> sendMessage(
            UserMessageRequestModel messageRequestModel) {
        if (!authorizationHandler.isAllowed("send", Message.class)) {
            throw new ForbiddenException();
        }

        UserMessage message =
                (UserMessage)
                        messageService.save(
                                messageMapper.userMessageRequestModelToUserMessage(
                                        messageRequestModel));
        return ResponseEntity.ok(messageMapper.userMessageToMessageResponseModel(message));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getMessageById(String id) {
        if (!authorizationHandler.isAllowed("view", Message.class)) {
            throw new ForbiddenException();
        }

        return messageService
                .findBy(UUID.fromString(id))
                .filter(message -> authorizationHandler.isAllowedForInstance("view", message))
                .map(
                        message -> {
                            try {
                                Map<String, Object> mapResponse = PropertyUtils.describe(message);
                                return ResponseEntity.ok(mapResponse);
                            } catch (IllegalAccessException
                                    | InvocationTargetException
                                    | NoSuchMethodException e) {
                                throw new InternalServerErrorException("Error getting message", e);
                            }
                        })
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<DirectMessageResponseModel> sendDirectMessage(
            DirectMessageRequestModel directMessageRequestModel) {
        if (!authorizationHandler.isAllowed("send", Message.class)) {
            throw new ForbiddenException();
        }

        DirectMessage message =
                (DirectMessage)
                        messageService.save(
                                messageMapper.directMessageRequestModelToUserMessage(
                                        directMessageRequestModel));
        return ResponseEntity.ok(messageMapper.directMessageToMessageResponseModel(message));
    }
}
