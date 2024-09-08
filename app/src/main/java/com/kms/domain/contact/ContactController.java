package com.kms.domain.contact;

import com.kms.domain.contact.dto.SaveContactRequest;
import com.kms.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Contact Management", description = "APIs for managing contacts")
@RequestMapping("/contacts")
public interface ContactController {

  @Operation(summary = "Get a specific contact by its ID")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Contact found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Contact.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid contact ID",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Contact not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{id}")
  Contact getContact(
      @Parameter(description = "ID of the contact to be retrieved", example = "1") @PathVariable
          int id);

  @Operation(summary = "Update a contact")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Contact updated successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Contact.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Contact not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/{id}")
  Contact updateContact(
      @Parameter(description = "ID of the contact to be saved", example = "1") @PathVariable int id,
      @Valid @RequestBody SaveContactRequest request);

  @Operation(summary = "Delete a contact by its ID")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Contact deleted successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Contact.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid contact ID",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Contact not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/{id}")
  Contact deleteContact(
      @Parameter(description = "ID of the contact to be deleted", example = "1") @PathVariable
          int id);

  @Operation(summary = "Get all contacts")
  @ApiResponse(
      responseCode = "200",
      description = "List of all contacts",
      content =
          @Content(
              mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = Contact.class))))
  @GetMapping
  List<Contact> getAllContacts();

  @Operation(summary = "Add contacts to the database")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Contacts created successfully",
        content =
            @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Contact.class)))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  List<Contact> addContacts(@Valid @RequestBody List<SaveContactRequest> saveContactRequest);

  @Operation(summary = "Search contacts by a given name")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "List of contacts matching the search criteria",
        content =
            @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Contact.class)))),
    @ApiResponse(
        responseCode = "404",
        description = "No contacts found matching the search criteria",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/search")
  List<Contact> getContactsByName(
      @Parameter(description = "Name to search for", example = "John") @RequestParam("name")
          String name);

  @Operation(summary = "Import contact records from a CSV file to the database")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Contacts imported successfully",
        content =
            @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Contact.class)))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid CSV file",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Error while parsing the CSV file",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/import")
  @ResponseStatus(HttpStatus.CREATED)
  List<Contact> importContacts(
      @Parameter(description = "CSV file to import contacts from") @RequestParam("file")
          MultipartFile file);

  @Operation(summary = "Export all contact records from the database to a CSV file")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Contacts exported successfully"),
    @ApiResponse(responseCode = "500", description = "Error while exporting data")
  })
  @GetMapping("/export")
  void exportContacts(HttpServletResponse response) throws IOException;
}
