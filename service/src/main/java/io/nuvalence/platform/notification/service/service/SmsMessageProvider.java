package io.nuvalence.platform.notification.service.service;

import static io.nuvalence.platform.notification.service.service.MessageBuilderUtils.getLocalizedTemplate;
import static io.nuvalence.platform.notification.service.service.MessageBuilderUtils.replaceParameterInTemplate;

import com.github.jknack.handlebars.Handlebars;
import io.nuvalence.platform.notification.service.domain.LocalizedStringTemplateLanguage;
import io.nuvalence.platform.notification.service.domain.Message;
import io.nuvalence.platform.notification.service.domain.MessageTemplate;
import io.nuvalence.platform.notification.service.domain.SmsFormat;
import io.nuvalence.platform.notification.service.exception.UnprocessableNotificationException;
import io.nuvalence.platform.notification.usermanagent.client.generated.models.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Message provider for SMS messages.
 */
@Slf4j
@Service
public class SmsMessageProvider implements SendMessageProvider {

    private static final String SUPPORTED_METHOD = "sms";
    private static final String DEFAULT_LANGUAGE = "en";

    private final SmsProvider smsProvider;
    private final Handlebars handlebars;

    public SmsMessageProvider(SmsProvider smsProvider) {
        this.smsProvider = smsProvider;
        this.handlebars = new Handlebars();
    }

    @Override
    public void sendMessage(UserDTO user, Message message, MessageTemplate template) {
        String preferredLanguage =
                user.getPreferences() != null
                                && user.getPreferences().getPreferredLanguage() != null
                        ? user.getPreferences().getPreferredLanguage()
                        : DEFAULT_LANGUAGE;

        SmsFormat smsFormat = template.getSmsFormat();
        Optional<LocalizedStringTemplateLanguage> smsTemplate =
                getLocalizedTemplate(smsFormat.getLocalizedStringTemplate(), preferredLanguage);
        if (smsTemplate.isEmpty()) {
            String templateNotFound =
                    String.format(
                            "No template found for language: %s, templateKey: %s",
                            preferredLanguage, template.getKey());
            log.error(templateNotFound);
            throw new UnprocessableNotificationException(templateNotFound);
        }
        String smsToSend =
                replaceParameterInTemplate(
                        smsTemplate.get().getTemplate(), message.getParameters(), handlebars);

        smsProvider.sendSms(user.getPhoneNumber(), smsToSend);
    }

    @Override
    public String supportedMethod() {
        return SUPPORTED_METHOD;
    }
}
