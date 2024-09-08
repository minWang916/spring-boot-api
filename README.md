## [View API Documentation in Swagger Editor](https://editor.swagger.io/?url=https://raw.githubusercontent.com/quangdo916/api-guide/main/openapi.json)


# Assignment API Requirement

## Common Requirements
- [ ] Follow models described in the API Documentation.
- [ ] Implement unit tests.
- [ ] Ensure code coverage for business logic code is greater than 70%.
- [ ] Handle errors appropriately.
- [X] Store logs in a file and print to the console.
- [X] Store data in an RDBMS database.
  - [ ] Apply DB Migration tool to manage DB schema and initial data.
- [ ] Return HTTP status codes correctly.
- [ ] Ensure data belongs to its own user; other users cannot see it.
- [ ] Validate data.
- [ ] Leverage web framework (Spring Boot, ASP.NET Core, Django, NestJS, etc.) to handle:
  - [ ] Input validation.
  - [ ] Global exception handling.
  - [ ] Logging configuration.
- [ ] Ensure all APIs, except `/auth/login`, only allow authenticated users to access.
- [ ] Use Authorization header: `Authorization: Bearer <JWT Token>`.
- [ ] Handle CORS for different host/port configurations.
  - [ ] Handle OPTIONS method.
  - [ ] Return correct `Access-Control-Allow-Origin` and `Access-Control-Allow-Headers` (e.g., Content-Type).

## Account API
- [ ] Implement `/auth/register` API.
  - [X] Body includes fields: email, password, fullname.
  - [ ] Send verification email.
  - [X] Store password securely using bcrypt.
- [ ] Implement `/auth/login` API.
  - [ ] Return HTTP status 401 if username or password is incorrect.
- [ ] Implement successful login response.
  - [ ] Return JWT token in response body.
  - [ ] Do NOT return password field in the response.
- [ ] Implement logout API.
  - [ ] Invalidate the JWT token in the backend.
  - [ ] Invalidate refresh token (if any).

## Dashboard API
- [ ] Implement required Dashboard APIs.

## Contact API
- [ ] Implement the following 5 APIs as shown in the document.
- [ ] Implement search contacts API.
  - [ ] Use GET method.
  - [ ] Query param: `keyword`.
- [ ] Implement Import/Export APIs.
  - [ ] Import contacts from a CSV file.
  - [ ] Export contacts to a CSV file.

## Task API
- [ ] Implement the following 5 APIs as shown in the document.
- [ ] Implement search tasks API.
  - [ ] Use GET method.
  - [ ] Query param: `keyword`.

## Report API
- [ ] Implement 1 API to count by field in a collection.
  - [ ] Example: Count the number of completed tasks in the `tasks` collection.
  - [ ] Other examples:
    - [ ] Count the number of each title (EM, TE, SE, BA) in the Contact collection.
    - [ ] Count the number of completed and not completed tasks in the Task collection.

## Optional Requirements
- [ ] Implement refresh token.


# Testing Guidelines

## Controller Tests

**Purpose:** Verify REST API endpoints' functionality, including responses, status codes, and error handling.

- **Endpoint Availability:**
  - **GET Requests:** Ensure endpoints return expected HTTP status codes (e.g., 200 OK) and correct response bodies.
  - **POST Requests:** Verify creation of new resources and accurate response data.
  - **PUT Requests:** Check for correct updates to existing resources and accurate responses.
  - **DELETE Requests:** Ensure resources are deleted successfully with the correct response.

- **Validation:**
  - **Input Validation:** Handle invalid inputs and return appropriate error messages (e.g., 400 Bad Request).

- **Authentication and Authorization:**
  - **Access Control:** Test endpoints with different user roles and verify access control.
  - **Security:** Confirm endpoints require authentication where necessary.

- **Exception Handling:**
  - **Error Scenarios:** Validate proper exception handling and meaningful error responses (e.g., 404 Not Found).

- **Integration with Other Components:**
  - **Service Layer:** Ensure proper interaction with the service layer.

## Service Tests

**Purpose:** Verify business logic correctness and service operations.

- **Business Logic:**
  - **Correctness:** Ensure business logic behaves as expected.
  - **Edge Cases:** Test service handling of edge cases or unusual inputs.

- **Interactions with Other Components:**
  - **Repository Layer:** Use mocks to isolate service logic and verify interactions with repositories.
  - **External Services:** Mock external APIs or services to test proper handling.

- **Error Handling:**
  - **Exceptions:** Ensure service handles exceptions appropriately.

- **Transactional Behavior:**
  - **Transactions:** Verify correct transaction management (commit or rollback).

## Repository Tests

**Purpose:** Verify the repository layer's interaction with the database and CRUD operations.

- **CRUD Operations:**
  - **Create:** Test if new entities are correctly saved.
  - **Read:** Ensure correct retrieval of entities based on various criteria.
  - **Update:** Verify updates to existing entities.
  - **Delete:** Test entity deletion.

- **Query Methods:**
  - **Custom Queries:** Ensure custom query methods return correct results.

- **Data Integrity:**
  - **Consistency:** Confirm data consistency and adherence to constraints.

## Configuration Tests

**Purpose:** Ensure that the application's configuration settings are correctly applied and functioning as expected.

- **General Configuration:**
  - **Database Configuration:** Verify that the application correctly loads PostgreSQL connection settings, such as JDBC URL, username, and password. Confirm that JPA settings, including Hibernate properties, are correctly applied.
  - **JPA Settings:** Test that Hibernate settings such as `ddl-auto` and SQL logging are configured as specified in the properties file.

- **CORS Configuration:**
  - **Allowed Origins:** Ensure that CORS settings permit requests from the specified origins. Verify that the allowed methods and headers are correctly configured.
  - **Configuration Validation:** Confirm that CORS policy is correctly applied and restricts or allows access based on the configuration.

- **Security Configuration:**
  - **Access Control:** Verify that security rules are enforced for various endpoints, ensuring proper authentication and authorization mechanisms are in place.
  - **Endpoint Protection:** Ensure that endpoints are secured as intended and that unauthorized access is properly restricted.


