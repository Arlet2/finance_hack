package su.arlet.finance_hack.controllers.rest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.exceptions.ValidationException;
import su.arlet.finance_hack.services.PaymentInfoService;
import su.arlet.finance_hack.services.UserService;


// TODO : использовать общие exceptions
@RestController
@RequestMapping("${api.path}/wastes")
@Tag(name = "WASTE API")
public class WasteController {

    private final PaymentInfoService paymentInfoService;
    private final UserService userService;

    private final Counter bankFaultCounter;

    @Autowired
    public WasteController(PaymentInfoService paymentInfoService, MeterRegistry meterRegistry, UserService userService) {
        this.paymentInfoService = paymentInfoService;
        this.userService = userService;
        bankFaultCounter = meterRegistry.counter("finance_bank_fault_counter");
    }

    @PostMapping("/{username}")
    @Operation(summary = "execute new waste")
    @ApiResponse(responseCode = "201", description = "Success - a new expense has been recorded", content = {
            @Content(schema = @Schema(implementation = Long.class))
    }
    )
    @ApiResponse(responseCode = "400", description = "validation error")
    @ApiResponse(responseCode = "401", description = "user not found")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<?> createWaste(@RequestBody PaymentInfo info, @PathVariable String username) {

        User user = userService.getByUsername(username);

        if (info == null) {
            bankFaultCounter.increment();
            throw new ValidationException("PaymentInfo is not set");
        }

        info.setUser(user);
        return new ResponseEntity<>(paymentInfoService.addWaste(info), HttpStatus.CREATED);
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
            throw new EntityNotFoundException("user");
        }
        paymentInfoService.deleteWaste(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
