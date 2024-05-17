package su.arlet.finance_hack.controllers.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import su.arlet.finance_hack.core.PaymentInfo;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.InvalidPaymentInfoException;
import su.arlet.finance_hack.exceptions.UserNotFoundException;
import su.arlet.finance_hack.exceptions.UsernameNotSetException;
import su.arlet.finance_hack.services.AuthService;
import su.arlet.finance_hack.services.PaymentInfoService;

@RestController
@RequestMapping("/waste")
@Tag(name = "WASTE API")
public class WasteController {

    private class PaymentInfoEntity {

        private PaymentInfo info;
        private String username;

    }

    private final PaymentInfoService paymentInfoService;
    private final AuthService authService;

    @Autowired
    public WasteController(PaymentInfoService paymentInfoService, AuthService authService) {
        this.paymentInfoService = paymentInfoService;
        this.authService = authService;
    }

    public ResponseEntity<?> createWaste(@RequestBody PaymentInfoEntity infoEntity) {

        if (infoEntity.username == null) {
            throw new UsernameNotSetException();
        }

        User user = authService.getByUsername(infoEntity.username);
        PaymentInfo info = infoEntity.info;
        if (info == null)
            throw new InvalidPaymentInfoException();

        info.setUser(user);
        return new ResponseEntity<>(paymentInfoService.addWaste(info), HttpStatus.OK);
    }

    public ResponseEntity<?> deleteWaste(@RequestParam Long id, HttpServletRequest servletRequest) {
        String username = authService.getUsernameByHttpRequest(servletRequest);
        PaymentInfo paymentInfo = paymentInfoService.getById(id);


        if (!paymentInfo.getUser().getUsername().equals(username)) {
            throw new UserNotFoundException();
        }


    }



}
