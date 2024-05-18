package su.arlet.finance_hack.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.arlet.finance_hack.core.PaymentInfo;
import su.arlet.finance_hack.core.PaymentInfoFilter;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.core.enums.PaymentType;
import su.arlet.finance_hack.services.AuthService;
import su.arlet.finance_hack.services.PaymentInfoService;
import su.arlet.finance_hack.services.UserService;

import java.util.List;

@RestController
@RequestMapping("${api.path}/internal_wastes")
@Tag(name = "api for internal work with expenses")
public class InternalWastingController {

    private final PaymentInfoService paymentInfoService;
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public InternalWastingController(PaymentInfoService paymentInfoService, AuthService authService, UserService userService) {
        this.paymentInfoService = paymentInfoService;
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping("/get_by_filter")
    @Operation(summary = "get wastes by filter")
    @ApiResponse(responseCode = "200", description = "Values are successfully obtained", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(implementation = PaymentInfo.class)
            ))
    )
    @ApiResponse(responseCode = "400", description = "validation error")
    @ApiResponse(responseCode = "401", description = "user not found")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<?> getByFilter(@RequestBody PaymentInfoFilter filter, HttpServletRequest servletRequest) {

        String username = authService.getUsernameFromHttpRequest(servletRequest);
        User user = userService.getByUsername(username);

        return new ResponseEntity<>(paymentInfoService.getByFilter(filter, user), HttpStatus.OK);
    }


    @PatchMapping("/")
    @Operation(summary = "change values in PaymentInfo")
    @ApiResponse(responseCode = "200", description = "successfully updated values", content = {
            @Content(schema = @Schema(implementation = Long.class))
    }
    )
    @ApiResponse(responseCode = "400", description = "validation error")
    @ApiResponse(responseCode = "401", description = "user not found")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<?> updateWastes(@RequestBody List<PaymentInfo> paymentInfoList, HttpServletRequest servletRequest) {
        String username = authService.getUsernameFromHttpRequest(servletRequest);

        User user = userService.getByUsername(username);

        if (paymentInfoList == null || paymentInfoList.isEmpty())
            throw new ValidationException("paymentInfos on accept undefined");

        for (var info : paymentInfoList) {
            if (info.getUser() == null)
                throw new ValidationException("user undefined");
            if (!info.getUser().getUsername().equals(user.getUsername())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            if (info.getIsTransfer() && ((info.getPaymentType() == null || info.getPaymentType() == PaymentType.UNKNOWN)) ||
                    (!info.getIsTransfer() && info.getItemCategory() == null)) {
                throw new ValidationException("item has undefined state");
            }
        }
        return new ResponseEntity<>(paymentInfoService.updateWastes(paymentInfoList).size(), HttpStatus.OK);
    }


}
