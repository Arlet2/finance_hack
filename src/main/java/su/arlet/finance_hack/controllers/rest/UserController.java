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
import su.arlet.finance_hack.services.UserService;

@RestController
@RequestMapping("${api.path}/users")
@Tag(name = "User API")
public class UserController {
    private final AuthService authService;
    private final UserService userService;

    public UserController(AuthService authService, UserService userService) {
        this.userService = userService;
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
            @RequestBody UserService.CreateUserEntity createUserEntity
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
            @RequestBody AuthService.LoginUser loginUser
    ) {
        loginUser.validate();
        String jwtToken = authService.loginUser(loginUser.getUsername(), loginUser.getPassword());
        return ResponseEntity.status(HttpStatus.OK).body(jwtToken);
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
        User user = authService.getUserFromHttpRequest(httpServletRequest);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/")
    @Operation(summary = "Get all Users")
    @ApiResponse(responseCode = "200", description = "Success - found Users", content = {
            @Content(schema = @Schema(implementation = User.class))
    }
    )
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> getUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
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
            @RequestBody UserService.UpdateUserEntity updateUserEntity, HttpServletRequest httpServletRequest
    ) {
        User user = authService.getUserFromHttpRequest(httpServletRequest);
        updateUserEntity.validate();
        userService.updateUserByUsername(updateUserEntity, user.getUsername());

        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/")
    @Operation(summary = "Delete User")
    @ApiResponse(responseCode = "200", description = "Success - deleted User", content = {@Content()})
    @ApiResponse(responseCode = "204", description = "No content", content = {@Content()})
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> deleteUser(HttpServletRequest httpServletRequest) {
        User user = authService.getUserFromHttpRequest(httpServletRequest);

        userService.delete(user.getUsername());
        return ResponseEntity.ok(null);
    }

    @GetMapping("/limit")
    @Operation(summary = "Get limit by username")
    @ApiResponse(responseCode = "200", description = "Success - found User", content = {
            @Content(schema = @Schema(implementation = long.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - User not found")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> getLimitByUsername(HttpServletRequest httpServletRequest) {
        User user = authService.getUserFromHttpRequest(httpServletRequest);

        return new ResponseEntity<>(user.getLimit(), HttpStatus.OK);
    }

    @GetMapping("/wastings")
    @Operation(summary = "Get current wastings by username")
    @ApiResponse(responseCode = "200", description = "Success - found User", content = {
            @Content(schema = @Schema(implementation = long.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - User not found")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> getCurrentWastingsByUsername(HttpServletRequest httpServletRequest) {
        User user = authService.getUserFromHttpRequest(httpServletRequest);

        return new ResponseEntity<>(user.getCurrentWastings(), HttpStatus.OK);
    }
}
