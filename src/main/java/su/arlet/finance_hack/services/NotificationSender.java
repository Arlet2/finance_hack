package su.arlet.finance_hack.services;

import org.springframework.stereotype.Component;
import su.arlet.finance_hack.core.Notification;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class NotificationSender {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Future<?> sendNotification(Notification notification) {
        return executorService.submit(() ->
                send(notification)
        );
    }

    private void send(Notification notification) {
        // do something...
    }


}
