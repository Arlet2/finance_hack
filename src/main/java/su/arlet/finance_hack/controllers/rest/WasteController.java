package su.arlet.finance_hack.controllers.rest;

import io.micrometer.core.instrument.MeterRegistry;
import io.prometheus.client.Counter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.arlet.finance_hack.core.PaymentInfo;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.UserNotFoundException;
import su.arlet.finance_hack.repos.UserRepo;
import su.arlet.finance_hack.services.AuthService;
import su.arlet.finance_hack.services.PaymentInfoService;


// TODO : использовать общие exceptions
@RestController
@RequestMapping("${api.path}/wastes")
@Tag(name = "WASTE API")
public class WasteController {

    private final PaymentInfoService paymentInfoService;
    private final AuthService authService;

    private final Counter bankFaultCounter;

    @Autowired
    public WasteController(PaymentInfoService paymentInfoService, AuthService authService, MeterRegistry meterRegistry) {
        this.paymentInfoService = paymentInfoService;
        this.authService = authService;
        bankFaultCounter = (Counter) meterRegistry.counter("bank_fault_counter");
    }

    @PostMapping("/{username}")
    @Operation(summary = "execute new waste")
    @ApiResponse(responseCode = "200", description = "Success - a new expense has been recorded", content = {
            @Content(schema = @Schema(implementation = Long.class))
    }
    )
    @ApiResponse(responseCode = "400", description = "validation error")
    @ApiResponse(responseCode = "401", description = "user not found")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<?> createWaste(@RequestBody PaymentInfo info, @PathVariable String username) {

        User user = authService.getByUsername(username);

        if (info == null) {
            bankFaultCounter.inc();
            throw new ValidationException("PaymentInfo is not set");
        }

        info.setUser(user);
        return new ResponseEntity<>(paymentInfoService.addWaste(info), HttpStatus.OK);
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "delete the expense")
    @ApiResponse(responseCode = "200", description = "Success - a expense has been removed")
    @ApiResponse(responseCode = "204", description = "expense has already been deleted")
    @ApiResponse(responseCode = "401", description = "authorization failed, username and paymentInfo have different usernames")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<?> deleteWaste(@RequestParam Long id, @PathVariable String username) {
        PaymentInfo paymentInfo = paymentInfoService.getByIdBeforeDeleting(id);
        if (!paymentInfo.getUser().getUsername().equals(username)) {
            throw new UserNotFoundException();
        }
        paymentInfoService.deleteWaste(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
