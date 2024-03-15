package io.nuvalence.platform.notification.service.events.processors;

import io.nuvalence.events.event.DirectNotificationEvent;
import io.nuvalence.events.exception.EventProcessingException;
import io.nuvalence.events.subscriber.EventProcessor;
import io.nuvalence.platform.notification.service.mapper.MessageMapper;
import io.nuvalence.platform.notification.service.service.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for processing notification events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DirectNotificationEventProcessor implements EventProcessor<DirectNotificationEvent> {

    private final MessageMapper messageMapper;
    private final MessageService messageService;

    @Override
    @Transactional
    public void execute(DirectNotificationEvent event) throws EventProcessingException {
        try {
            log.debug(
                    "Received event {} of type {}",
                    event.getMetadata().getId(),
                    event.getMetadata().getType());

            messageService.save(messageMapper.directNotificationEventToDirectMessage(event));
        } catch (Exception e) {
            throw new EventProcessingException(e);
        }
    }

    @Override
    public Class<DirectNotificationEvent> getEventClass() {
        return DirectNotificationEvent.class;
    }
}
