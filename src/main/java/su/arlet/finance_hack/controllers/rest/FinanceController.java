package su.arlet.finance_hack.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.arlet.finance_hack.core.Finance;

@RestController
@RequestMapping("${api.path}/finances")
@Tag(name = "Finance API")
public class FinanceController {
    @ApiResponse(responseCode = "200", description = "OK",
            content = {
                    @Content(array = @ArraySchema(schema = @Schema(implementation = FinanceController.class)))
            })
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    @GetMapping("/")
    @Operation(summary = "Get finances by filters")
    public void getFinances(
            @RequestParam String queryObject1,
            @RequestParam String queryObject2
    ) {

    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ad post by ID")
    @ApiResponse(responseCode = "200", description = "Success - found ad post", content = {
            @Content(schema = @Schema(implementation = Finance.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - ad post not found")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> getFinanceByID(@PathVariable Long id) {
        return new ResponseEntity<>(new Finance(), HttpStatus.OK);
    }

    @PostMapping("/")
    @Operation(summary = "Create a new ad post")
    @ApiResponse(
            responseCode = "201", description = "Created ad post id", content = {
            @Content(schema = @Schema(implementation = Long.class))}
    )
    @ApiResponse(
            responseCode = "400", description = "Bad body", content = {
            @Content(schema = @Schema(implementation = String.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - user not found")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> createAdPost(@RequestBody Finance finance) {
        return new ResponseEntity<>(0, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update ad post info")
    @ApiResponse(responseCode = "200", description = "Success - updated ad post", content = {@Content()})
    @ApiResponse(
            responseCode = "400", description = "Bad body", content = {
            @Content(schema = @Schema(implementation = String.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - ad post not found", content = {@Content()})
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> updateAdPost(
            @PathVariable Long id,
            @RequestBody Long updateAdPost
    ) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ad post")
    @ApiResponse(responseCode = "200", description = "Success - deleted ad post", content = {@Content()})
    @ApiResponse(responseCode = "204", description = "No content", content = {@Content()})
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> deleteAdPost(@PathVariable Long id) {
        return ResponseEntity.ok(null);
    }
}
