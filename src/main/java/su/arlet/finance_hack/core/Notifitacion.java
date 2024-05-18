package su.arlet.finance_hack.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notifitacion {

    private String notificationText;

    private NotificationType notificationType;

    private String address;

}
