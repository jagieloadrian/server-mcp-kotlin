# Unit Test Specification - Kotlin

## 📋 Overview
This specification describes the universal conventions and patterns used to write unit tests in Kotlin projects. Tests are written with Kotest and MockK, compatible with JUnit 5.

## 🎯 General Conventions

### 1. Test Naming
- **Format**: `` `should [action] when [condition]` ``
- **Examples**:
  - `` `should return true when given valid input` ``
  - `` `should throw ValidationException when input is invalid` ``
  - `` `should save entity when all validations pass` ``

### 2. Test Structure (Given-When-Then)
Every test should have a clear structure:
```kotlin
@Test
fun `should [action] when [condition]`() {
    // Given - prepare test data and mocks
    val input = "test data"
    every { mock.method() } returns expectedValue

    // When - call the method under test
    val result = service.method(input)

    // Then - assert the results
    result shouldBe expectedValue
}
```

### 3. Tools and Libraries
- **Kotest Assertions**: `shouldBe`, `shouldThrow`, `shouldNotBeNull`
- **MockK**: for mocking dependencies (annotation-based or manual)
- **JUnit 5**: as the test framework

## 🛠️ Implementation Patterns

### 1. Mocking Dependencies

#### Annotation-Based Version (Recommended)
```kotlin
@ExtendWith(MockKExtension::class)
class ServiceTest : MockKAnnotationsTest {

    @MockK
    private lateinit var repository: Repository

    @RelaxedMockK
    private lateinit var externalService: ExternalService

    @InjectMockKs
    private lateinit var service: Service
    
}
```

#### Manual Version (Alternative)
```kotlin
class ServiceTest {
    private val repository = mockk<Repository>()
    private val externalService = mockk<ExternalService>(relaxed = true)
    private val service = Service(repository, externalService)
}
```

### 2. Assertions
```kotlin
// Simple comparison
result shouldBe expectedValue
result shouldNotBe null

// Checking exceptions
shouldThrow<ExpectedException> {
    service.method(invalidInput)
}

// Checking collections
list shouldHaveSize 3
list shouldContain "element"

// Checking object properties
user.name shouldBe "John"
user.age shouldBeGreaterThan 18
```

### 3. Testing Exceptions
```kotlin
@Test
fun `should throw SpecificException when invalid condition`() {
    // Given
    val invalidInput = "invalid"
    every { dependency.validate(invalidInput) } throws ValidationException("Invalid")

    // When & Then
    shouldThrow<SpecificException> {
        service.process(invalidInput)
    }
}
```

### 4. Testing the Happy Path
```kotlin
@Test
fun `should return expected result when all conditions are met`() {
    // Given
    val validInput = ValidInput("data")
    every { dependency.process(validInput) } returns expectedResult

    // When
    val result = service.process(validInput)

    // Then
    result shouldBe expectedResult
    verify(exactly = 1) { dependency.process(validInput) }
}
```

## 📁 Test Organization

### 1. Directory Structure
```
src/test/kotlin/com/example/project/
├── domain/
│   ├── entity/
│   │   └── EntityTest.kt
│   └── service/
│       └── ServiceTest.kt
├── infrastructure/
│   ├── repository/
│   │   └── RepositoryTest.kt
│   └── external/
│       └── ExternalServiceTest.kt
└── application/
    ├── controller/
    │   └── ControllerTest.kt
    └── dto/
        └── DtoTest.kt
```

### 2. Grouping Tests in a Class
```kotlin
class ServiceTest {

    // ==================== CREATE TESTS ====================
    @Test
    fun `should create entity when valid request`()

    @Test
    fun `should throw exception when invalid request`()

    // ==================== UPDATE TESTS ====================
    @Test
    fun `should update entity when exists`()

    // ==================== DELETE TESTS ====================
    @Test
    fun `should delete entity when exists`()
}
```

## 🔧 Detailed Patterns

### 1. Testing Validators
```kotlin
class ValidatorTest {

    private val validator = Validator()
    private val mockContext = mockk<ConstraintValidatorContext>(relaxed = true)

    @Test
    fun `should return true when valid input`() {
        // Given
        val validInput = "valid@example.com"

        // When
        val result = validator.isValid(validInput, mockContext)

        // Then
        result shouldBe true
    }

    @Test
    fun `should return false when null input`() {
        // Given
        val nullInput = null

        // When
        val result = validator.isValid(nullInput, mockContext)

        // Then
        result shouldBe false
    }
}
```

### 2. Testing Repository/Service Classes
```kotlin
@ExtendWith(MockKExtension::class)
class UserServiceTest : MockKAnnotationsTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var emailService: EmailService

    @InjectMockKs
    private lateinit var userService: UserService

    @Test
    fun `should create user when email is unique`() {
        // Given
        val request = CreateUserRequest("john@example.com", "John Doe")
        val savedUser = User(1L, "john@example.com", "John Doe")

        every { userRepository.existsByEmail("john@example.com") } returns false
        every { userRepository.save(any()) } returns savedUser
        every { emailService.sendWelcomeEmail("john@example.com") } just Runs

        // When
        val result = userService.createUser(request)

        // Then
        result.id shouldBe 1L
        result.email shouldBe "john@example.com"
        verify { emailService.sendWelcomeEmail("john@example.com") }
    }
}
```

### 3. Testing Controller Classes
```kotlin
class UserControllerTest : MockKAnnotationsTest {

    @MockK
    private lateinit var userService: UserService

    @InjectMockKs
    private lateinit var controller: UserController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Test
    fun `should return user when found by id`() {
        // Given
        val user = User(1L, "john@example.com", "John Doe")
        every { userService.findById(1L) } returns user

        // When & Then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("john@example.com"))
    }
}
```

### 4. Testing Utility Classes
```kotlin
class StringUtilsTest {

    @Test
    fun `should capitalize first letter when given lowercase string`() {
        // Given
        val input = "hello world"

        // When
        val result = StringUtils.capitalize(input)

        // Then
        result shouldBe "Hello world"
    }

    @Test
    fun `should return empty string when input is null`() {
        // Given
        val input = null

        // When
        val result = StringUtils.capitalize(input)

        // Then
        result shouldBe ""
    }
}
```

## 📊 Test Coverage

### Types of Scenarios to Test:
1. **Happy Path** - valid input data
2. **Edge Cases** - boundary cases (null, empty, max/min values)
3. **Error Cases** - invalid data, exceptions
4. **Business Logic** - business rules, validations
5. **Integration Points** - interactions with dependencies
6. **Boundary Conditions** - boundary values

### Coverage Metrics:
- **Line Coverage**: > 80%
- **Branch Coverage**: > 75%
- **Mutation Coverage**: > 70% (if used)

## 🚀 Running Tests

### Gradle Commands:
```bash
# All tests
./gradlew test

# A specific test class
./gradlew test --tests "*ClassNameTest"

# With coverage
./gradlew test jacocoTestReport

# Tagged tests
./gradlew test --tags "unit"
```

### Maven Commands:
```bash
# All tests
mvn test

# With coverage
mvn test jacoco:report
```

### IDE:
- IntelliJ IDEA: right-click on a class/method → Run Tests
- VS Code: use the Kotlin extension
- Eclipse: use the Kotlin Plugin

## 📝 Checklist for New Tests

- [ ] Test name in the format `` `should [action] when [condition]` ``
- [ ] Given-When-Then structure with comments
- [ ] Appropriate mocking of dependencies (annotations or manual)
- [ ] Assertions checking the expected results
- [ ] Exception testing for error scenarios
- [ ] Verification of mock calls (if needed)
- [ ] Readable variable names (input, result, expected, actual)
- [ ] Test compiles without errors
- [ ] No warnings in the IDE

## 🔍 Complete Test Examples

### Annotation-Based Test
```kotlin
@ExtendWith(MockKExtension::class)
class OrderServiceTest : MockKAnnotationsTest {

    @MockK
    private lateinit var orderRepository: OrderRepository

    @MockK
    private lateinit var paymentService: PaymentService

    @InjectMockKs
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should create order when payment succeeds`() {
        // Given
        val request = CreateOrderRequest(items = listOf("item1"), total = 100.0)
        val order = Order(id = "order123", total = 100.0)

        every { paymentService.processPayment(100.0) } returns PaymentResult.SUCCESS
        every { orderRepository.save(any()) } returns order

        // When
        val result = orderService.createOrder(request)

        // Then
        result.id shouldBe "order123"
        result.total shouldBe 100.0
        verify { paymentService.processPayment(100.0) }
        verify { orderRepository.save(any()) }
    }
}
```

### Manual Test
```kotlin
class CalculatorServiceTest {

    private val calculator = mockk<Calculator>()
    private val logger = mockk<Logger>(relaxed = true)
    private val service = CalculatorService(calculator, logger)

    @Test
    fun `should return sum when both numbers are positive`() {
        // Given
        val a = 5
        val b = 3
        every { calculator.add(a, b) } returns 8

        // When
        val result = service.add(a, b)

        // Then
        result shouldBe 8
        verify { logger.info("Adding 5 and 3") }
    }
}
```

## 📚 Additional Rules and Best Practices

### 1. General Rules
- **One Concept per Test** - each test checks one thing
- **Readability** - test code should be self-documenting
- **Independence** - tests should not depend on each other
- **Speed** - unit tests should be fast (< 100ms)
- **Stability** - tests should be deterministic

### 2. Mocking
- **Mock External Dependencies** - databases, APIs, filesystem
- **Stub Internal Dependencies** - if you are testing integration
- **Use Relaxed Mocks** - for dependencies whose behavior is not critical
- **Verify Important Calls** - not every call needs verify

### 3. Naming
- **Methods**: `shouldReturnWhen`, `shouldThrowWhen`, `shouldCallWhen`
- **Variables**: `input`, `result`, `expected`, `actual`, `request`, `response`
- **Constants**: `VALID_EMAIL`, `INVALID_INPUT`, `EXPECTED_RESULT`

### 4. Code Organization
- **Helper Methods** - for repeated setups
- **Test Data Builders** - for complex objects
- **Constants** - for frequently used values
- **Nested Classes** - for grouping related tests

### 5. Advanced Techniques
- **Parameterized Tests** - for many similar scenarios
- **Test Fixtures** - for shared setups
- **Property-Based Testing** - for generating test data
- **Mutation Testing** - for verifying test quality

## 🔗 Useful Links and Resources

- [Kotest Documentation](https://kotest.io/)
- [MockK Documentation](https://mockk.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Kotlin Testing Best Practices](https://kotlinlang.org/docs/reference/testing.html)

---
*Universal specification for Kotlin projects*
*Created: May 13, 2026*
*Author: GitHub Copilot*
*Version: 2.0 - Universal Kotlin Edition*

## 🔍 Project Navigation for AI

### How an AI Agent Should Explore the Project

When you get a task to write tests, **always explore the project structure first**, because it may differ from the one presented in this specification.

#### 1. Check the Top-Level Structure
```bash
# View the directory structure
list_dir path="."

# Check the files in the root directory
list_dir path="src"

# Find the source code directories
list_dir path="src/main"
list_dir path="src/test"
```

#### 2. Find the Classes to Test
```bash
# Search the source code
grep_search query="class.*Service" includePattern="*.kt"
grep_search query="class.*Repository" includePattern="*.kt"
grep_search query="class.*Controller" includePattern="*.kt"

# Find a specific class
grep_search query="class IpAddressValidator" includePattern="*.kt"
```

#### 3. Check Existing Tests
```bash
# See which tests already exist
list_dir path="src/test/kotlin"

# Check the test package structure
grep_search query="package.*test" includePattern="*.kt"
```

#### 4. Analyze the Class Dependencies
```bash
# Find the constructor and dependencies
read_file filePath="src/main/kotlin/.../Service.kt"

# Check imports to understand dependencies
grep_search query="import.*Repository" includePattern="*.kt"
grep_search query="import.*Service" includePattern="*.kt"
```

#### 5. Check build.gradle.kts or pom.xml
```bash
# See which test libraries are available
read_file filePath="build.gradle.kts"

# Check the test dependencies
grep_search query="testImplementation" includePattern="*.gradle.kts"
```

### Adapting to Different Structures

#### Structures You May Encounter:

**Standard Structure (Spring Boot):**
```
src/
├── main/kotlin/com/example/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
└── test/kotlin/com/example/
    ├── controller/
    ├── service/
    └── repository/
```

**Modular Structure:**
```
modules/
├── user/
│   ├── src/main/kotlin/
│   └── src/test/kotlin/
├── order/
│   ├── src/main/kotlin/
│   └── src/test/kotlin/
```

**Feature-Based Structure:**
```
src/
├── main/kotlin/features/
│   ├── user/
│   ├── order/
│   └── payment/
└── test/kotlin/features/
    ├── user/
    ├── order/
    └── payment/
```

**Legacy Structure:**
```
src/
├── com/example/
│   ├── Service.java
│   └── ServiceTest.java
└── kotlin/
    ├── NewService.kt
    └── NewServiceTest.kt
```

### Strategy for AI

#### Step 1: Identify the Project Type
- Is it Spring Boot? (look for `@SpringBootApplication`)
- Is it a plain Kotlin application? (check the main function)
- Is it multi-module? (check settings.gradle.kts)

#### Step 2: Find the Naming Conventions
```kotlin
// Check how packages are named
grep_search query="package com\." includePattern="*.kt"

// Check class conventions
grep_search query="class.*Service" includePattern="*.kt"
grep_search query="interface.*Repository" includePattern="*.kt"
```

#### Step 3: Adapt the Test Structure
- **If tests are in the same package**: create `ServiceTest.kt` next to `Service.kt`
- **If tests are in a separate package**: create it in `src/test/kotlin/...`
- **If the structure is feature-based**: organize tests the same way as the features

#### Step 4: Check the Available Test Tools
```kotlin
// Check whether Kotest is available
grep_search query="io\.kotest" includePattern="*.gradle.kts"

// Check whether MockK is available
grep_search query="io\.mockk" includePattern="*.gradle.kts"

// Check whether JUnit 5 is available
grep_search query="junit-jupiter" includePattern="*.gradle.kts"
```

### Communicating with the User

#### When to Ask Questions:
- If the project structure is unusual
- If you can't find the class to test
- If you don't know which libraries to use
- If there are conflicts in naming conventions

#### Example Questions:
> "I can see the project uses a modular structure. Should the tests for UserService go in the `user` module or in a shared test module?"

> "The project uses JUnit 4 instead of JUnit 5. Should I adapt the tests to JUnit 4?"

> "I can't find the ProductRepository class. Has it been implemented yet?"

### Project Exploration Example

#### Scenario: A New Class to Test
```
1. User: "Write tests for UserService"

2. AI checks the structure:
   list_dir path="src/main/kotlin"
   → finds "com/company/project/"

3. AI searches for the class:
   grep_search query="class UserService" includePattern="*.kt"
   → finds "src/main/kotlin/com/company/project/service/UserService.kt"

4. AI checks the dependencies:
   read_file filePath="src/main/kotlin/com/company/project/service/UserService.kt"
   → sees a constructor with UserRepository and EmailService

5. AI checks existing tests:
   list_dir path="src/test/kotlin/com/company/project"
   → sees the test structure

6. AI writes tests according to the specification
```

### Best Practices for AI

1. **Always explore first** - don't assume the structure
2. **Ask if unsure** - better to ask than to make wrong assumptions
3. **Adapt to existing conventions** - look at the existing code
4. **Check library availability** - don't use tools that aren't there
5. **Explain your choices** - explain why you chose a given structure

---
*Appendix: Project navigation for AI*
*Version: 2.1*
