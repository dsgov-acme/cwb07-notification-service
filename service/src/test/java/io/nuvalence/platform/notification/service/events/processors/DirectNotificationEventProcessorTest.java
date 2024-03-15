package io.nuvalence.platform.notification.service.events.processors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.events.event.DirectNotificationEvent;
import io.nuvalence.events.event.EventMetadata;
import io.nuvalence.events.exception.EventProcessingException;
import io.nuvalence.platform.notification.service.domain.DirectMessage;
import io.nuvalence.platform.notification.service.mapper.MessageMapper;
import io.nuvalence.platform.notification.service.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

class DirectNotificationEventProcessorTest {

    @Mock private MessageMapper messageMapper;

    @Mock private MessageService messageService;

    @InjectMocks private DirectNotificationEventProcessor directNotificationEventProcessor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute() throws EventProcessingException {
        DirectNotificationEvent event = mock(DirectNotificationEvent.class);
        EventMetadata eventMetadata = mock(EventMetadata.class);
        when(event.getMetadata()).thenReturn(eventMetadata);
        when(eventMetadata.getId()).thenReturn(UUID.randomUUID());
        when(eventMetadata.getType()).thenReturn("DirectMessage");
        DirectMessage message = mock(DirectMessage.class);
        when(messageMapper.directNotificationEventToDirectMessage(event)).thenReturn(message);

        directNotificationEventProcessor.execute(event);

        verify(messageService, times(1)).save(message);
    }
}
