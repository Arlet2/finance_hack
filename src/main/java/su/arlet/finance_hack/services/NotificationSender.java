package su.arlet.finance_hack.services;

import org.springframework.stereotype.Component;
import su.arlet.finance_hack.core.Notifitacion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class NotificationSender {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Future<?> sendNotification(Notifitacion notifitacion) {
        return executorService.submit(() ->
                send(notifitacion)
        );
    }

    private void send(Notifitacion notifitacion) {
        // do something...
    }


}
