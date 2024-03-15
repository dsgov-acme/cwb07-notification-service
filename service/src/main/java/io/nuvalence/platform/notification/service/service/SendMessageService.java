package io.nuvalence.platform.notification.service.service;

import io.nuvalence.platform.notification.service.domain.DirectMessage;
import io.nuvalence.platform.notification.service.domain.Message;
import io.nuvalence.platform.notification.service.domain.MessageTemplate;
import io.nuvalence.platform.notification.service.domain.UserMessage;
import io.nuvalence.platform.notification.service.exception.UnprocessableNotificationException;
import io.nuvalence.platform.notification.service.service.usermanagementapi.UserManagementClientService;
import io.nuvalence.platform.notification.usermanagent.client.ApiException;
import io.nuvalence.platform.notification.usermanagent.client.generated.models.UserDTO;
import io.nuvalence.platform.notification.usermanagent.client.generated.models.UserPreferenceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for sending messages.
 */
@Slf4j
@Service
public class SendMessageService {

    private final UserManagementClientService userManagementClientService;

    private final TemplateService templateService;

    private Map<String, SendMessageProvider> sendMessageProviderMap = new HashMap<>();

    /**
     * Constructor.
     *
     * @param userManagementClientService user management client service
     * @param sendMessageProviders        list of send message providers
     * @param templateService             template service
     */
    public SendMessageService(
            UserManagementClientService userManagementClientService,
            List<SendMessageProvider> sendMessageProviders,
            TemplateService templateService) {
        this.userManagementClientService = userManagementClientService;
        this.templateService = templateService;
        for (SendMessageProvider sendMessageProvider : sendMessageProviders) {
            sendMessageProviderMap.put(sendMessageProvider.supportedMethod(), sendMessageProvider);
        }
    }

    /**
     * Send a message.
     *
     * @param message message
     * @throws ApiException if an error occurs while querying user management service
     * @throws IOException possibly thrown by apis.
     * @throws UnprocessableNotificationException if an error occurs while sending the message
     */
    public void sendMessage(Message message) throws ApiException, IOException {
        if (message instanceof UserMessage userMessage) {
            sendUserMessage(userMessage);
        } else if (message instanceof DirectMessage directMessage) {
            sendDirectMessage(directMessage);
        } else {
            throw new UnprocessableNotificationException("Message type not supported");
        }
    }

    private void sendUserMessage(UserMessage message) throws ApiException, IOException {
        UUID userId = UUID.fromString(message.getUserId());

        // Query user management service for user preferences
        Optional<UserDTO> user = userManagementClientService.getUser(userId);
        if (user.isEmpty()) {
            String userNotFoundMessage =
                    String.format("Message could not be sent. User not found %s", userId);
            log.error(userNotFoundMessage);
            throw new UnprocessableNotificationException(userNotFoundMessage);
        }

        UserPreferenceDTO userPreferences = user.get().getPreferences();

        if (userPreferences == null || userPreferences.getPreferredCommunicationMethod() == null) {
            String communicationPreferencesNotFound =
                    String.format(
                            "Message could not be sent. Communication preferences not found for"
                                    + " user %s",
                            userId);
            log.error(communicationPreferencesNotFound);
            throw new UnprocessableNotificationException(communicationPreferencesNotFound);
        }

        SendMessageProvider messageProvider =
                getSendMessageProvider(
                        userPreferences.getPreferredCommunicationMethod(), userId.toString());
        MessageTemplate template = getMessageTemplate(message.getMessageTemplateKey());

        messageProvider.sendMessage(user.get(), message, template);
    }

    private void sendDirectMessage(DirectMessage message) throws IOException {
        SendMessageProvider messageProvider =
                getSendMessageProvider(message.getCommunicationMethod(), message.getDestination());
        MessageTemplate template = getMessageTemplate(message.getMessageTemplateKey());

        UserDTO user = new UserDTO();
        user.setEmail(message.getDestination());
        user.setPhoneNumber(message.getDestination());
        messageProvider.sendMessage(user, message, template);
    }

    private SendMessageProvider getSendMessageProvider(String communicationMethod, String userId) {
        SendMessageProvider messageProvider = sendMessageProviderMap.get(communicationMethod);
        if (messageProvider == null) {
            String communicationPreferencesNotAvailable =
                    String.format(
                            "Message could not be sent. Preferred communication method %s not"
                                    + " supported for user %s",
                            communicationMethod, userId);
            log.error(communicationPreferencesNotAvailable);
            throw new UnprocessableNotificationException(communicationPreferencesNotAvailable);
        }
        return messageProvider;
    }

    private MessageTemplate getMessageTemplate(String messageTemplateKey) {
        Optional<MessageTemplate> template = templateService.getTemplate(messageTemplateKey);
        if (template.isEmpty()) {
            String templateNotFound =
                    String.format(
                            "Message could not be sent. Template not found for template key %s",
                            messageTemplateKey);
            log.error(templateNotFound);
            throw new UnprocessableNotificationException(templateNotFound);
        }
        return template.get();
    }
}
