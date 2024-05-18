package su.arlet.finance_hack.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.services.AuthService;

@RestController
@RequestMapping("${api.path}/users")
@Tag(name = "User API")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    @Operation(summary = "Create a new user")
    @ApiResponse(
            responseCode = "201", description = "Created user id", content = {
            @Content(schema = @Schema(implementation = String.class))
    }
    )
    @ApiResponse(
            responseCode = "400", description = "Bad body", content = {
            @Content(schema = @Schema(implementation = String.class)),
    }
    )
    @ApiResponse(responseCode = "409", description = "User with given username already exists", content = {@Content()})
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> createUser(
            @RequestBody AuthService.CreateUserEntity createUserEntity
    ) {
        createUserEntity.validate();

        String jwtToken = authService.registerUser(createUserEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body(jwtToken);
    }

    @PostMapping("/login")
    @Operation(summary = "Login to existing user")
    @ApiResponse(
            responseCode = "200", description = "Logged in", content = {
            @Content(schema = @Schema(implementation = String.class))
    }
    )
    @ApiResponse(
            responseCode = "400", description = "Bad body", content = {
            @Content(schema = @Schema(implementation = String.class)),
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - user not found", content = {})
    @ApiResponse(responseCode = "500", description = "Server error", content = {})
    public ResponseEntity<?> login(
            @RequestBody String username, String password
    ) {

        String jwtToken = authService.loginUser(username, password);
        return ResponseEntity.status(HttpStatus.CREATED).body(jwtToken);
    }


    @GetMapping("/me")
    @Operation(summary = "Get User by username")
    @ApiResponse(responseCode = "200", description = "Success - found User", content = {
            @Content(schema = @Schema(implementation = User.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - User not found")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> getUserByUsername(HttpServletRequest httpServletRequest) {
        var username = authService.getUsernameFromHttpRequest(httpServletRequest);
        return new ResponseEntity<>(authService.getByUsername(username), HttpStatus.OK);
    }

    @GetMapping("/")
    @Operation(summary = "Get all Users")
    @ApiResponse(responseCode = "200", description = "Success - found Users", content = {
            @Content(schema = @Schema(implementation = User.class))
    }
    )
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> getUsers() {
        return new ResponseEntity<>(authService.getAllUsers(), HttpStatus.OK);
    }

    @PatchMapping("/")
    @Operation(summary = "Update User")
    @ApiResponse(responseCode = "200", description = "Success - updated User", content = {@Content()})
    @ApiResponse(
            responseCode = "400", description = "Bad body", content = {
            @Content(schema = @Schema(implementation = String.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - User not found", content = {@Content()})
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> updateUser(
            @RequestBody AuthService.UpdateUserEntity updateUserEntity, HttpServletRequest httpServletRequest
    ) {
        var username = authService.getUsernameFromHttpRequest(httpServletRequest);
        updateUserEntity.validate();
        authService.updateUserByUsername(updateUserEntity, username);

        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/")
    @Operation(summary = "Delete User")
    @ApiResponse(responseCode = "200", description = "Success - deleted User", content = {@Content()})
    @ApiResponse(responseCode = "204", description = "No content", content = {@Content()})
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> deleteUser(HttpServletRequest httpServletRequest) {
        var username = authService.getUsernameFromHttpRequest(httpServletRequest);
        authService.delete(username);
        return ResponseEntity.ok(null);
    }
}
