package io.nuvalence.platform.notification.service.mapper;

import io.nuvalence.events.event.DirectNotificationEvent;
import io.nuvalence.events.event.NotificationEvent;
import io.nuvalence.platform.notification.service.domain.DirectMessage;
import io.nuvalence.platform.notification.service.domain.UserMessage;
import io.nuvalence.platform.notification.service.generated.models.DirectMessageRequestModel;
import io.nuvalence.platform.notification.service.generated.models.DirectMessageResponseModel;
import io.nuvalence.platform.notification.service.generated.models.UserMessageRequestModel;
import io.nuvalence.platform.notification.service.generated.models.UserMessageResponseModel;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for messages.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface MessageMapper {
    /**
     * Map a user message request model to a message.
     *
     * @param messageRequestModel message request model
     * @return message
     */
    @Mapping(source = "templateKey", target = "messageTemplateKey")
    UserMessage userMessageRequestModelToUserMessage(UserMessageRequestModel messageRequestModel);

    /**
     * Map a direct message request model to a message.
     *
     * @param messageRequestModel message request model
     * @return message
     */
    @Mapping(source = "templateKey", target = "messageTemplateKey")
    DirectMessage directMessageRequestModelToUserMessage(
            DirectMessageRequestModel messageRequestModel);

    @Mapping(source = "templateKey", target = "messageTemplateKey")
    UserMessage notificationEventToUserMessage(NotificationEvent notificationEvent);

    @Mapping(source = "templateKey", target = "messageTemplateKey")
    @Mapping(
            target = "communicationMethod",
            expression = "java(directNotificationEvent.getCommunicationMethod().getValue())")
    DirectMessage directNotificationEventToDirectMessage(
            DirectNotificationEvent directNotificationEvent);

    /**
     * Map a message to a user message request model.
     *
     * @param message message
     * @return user message request model
     */
    @Mapping(source = "messageTemplateKey", target = "templateKey")
    UserMessageResponseModel userMessageToMessageResponseModel(UserMessage message);

    /**
     * Map a message to a direct message request model.
     *
     * @param message message
     * @return direct message request model
     */
    @Mapping(source = "messageTemplateKey", target = "templateKey")
    DirectMessageResponseModel directMessageToMessageResponseModel(DirectMessage message);
}
