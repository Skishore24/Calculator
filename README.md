# Apex Matrix Calculator - Spring Boot Web Application

A full-stack, responsive calculator website engineered with **HTML5**, **CSS3 (Modern Glassmorphic Dark Theme)**, **JavaScript**, and **Spring Boot 3 REST API**.

---

## 🌟 Key Features

1. **Precision Math Engine (Spring Boot Backend)**:
   - Evaluates complex expressions using an expression parser.
   - Operations: `+`, `-`, `×`, `÷`, `^` (power), `%` (modulo), `√` (square root), `n!` (factorial), `1/x` (reciprocal), `x²` (square), and nested parentheses `( )`.
   - Division-by-zero protection and validation with clear error messaging.

2. **Modern Glassmorphic UI**:
   - Deep obsidian gradient palette with ambient glow highlights.
   - Dual-row OLED-style display with secondary expression history line, primary glowing digits, and live preview.
   - Error shake animations for illegal syntax or math errors.

3. **Real-time Server History Sync**:
   - Every calculation evaluated by the Spring Boot backend is recorded with timestamps.
   - Interactive history sidebar: click any previous calculation to recall the expression or result.
   - "Clear All" button synchronized with `DELETE /api/calculator/history`.

4. **Dual Modes & Live Preview**:
   - Client-side instantaneous preview as you type.
   - Backend validation & computation with live round-trip latency tracking (`ms`).

5. **Acoustic & Keyboard Support**:
   - Web Audio API synthesized soft acoustic clicks and success chords (toggleable).
   - Full keyboard navigation: `0-9`, operators (`+`, `-`, `*`, `/`, `^`, `%`), `Enter`/`=` to calculate, `Backspace`, and `Esc` (All Clear).

6. **Automated CI/CD**:
   - Includes declarative `Jenkinsfile` for automated compilation, JUnit 5 testing, packaging, test result publishing, and artifact archiving.

---

## 🚀 Running the Application

### Option 1: Using the Maven Wrapper (Recommended)

To run the development server:
```powershell
.\mvnw.cmd spring-boot:run
```

To specify custom port (e.g., port `8085`):
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8085"
```

### Option 2: Building and Running the Executable JAR

```powershell
# 1. Clean and package
.\mvnw.cmd clean package

# 2. Run the JAR
java -jar target\calculator-0.0.1-SNAPSHOT.jar --server.port=8085
```

Once started, open your web browser and navigate to:
```
http://localhost:8085/
```

---

## 📡 REST API Reference

| Method | Endpoint | Description | Request Body Example |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/calculator/health` | Service health status | *None* |
| `POST` | `/api/calculator/calculate` | Evaluate a mathematical expression | `{"expression": "25 * 4 + 18 / 2"}` |
| `GET` | `/api/calculator/history` | Retrieve recent calculations | *None* |
| `DELETE` | `/api/calculator/history` | Clear calculation history | *None* |

---

## 🧪 Running Unit Tests

Run the JUnit 5 test suite:
```powershell
.\mvnw.cmd test
```

---

## 📁 Project Structure

```
c:\MyFiles\LEARN\jenkins\
├── Jenkinsfile                          # Declarative Jenkins CI/CD pipeline
├── pom.xml                              # Maven configuration (Spring Boot 3.4.2)
├── mvnw & mvnw.cmd                      # Maven Wrapper scripts
├── README.md                            # Documentation
└── src
    ├── main
    │   ├── java\com\example\calculator
    │   │   ├── CalculatorApplication.java
    │   │   ├── controller
    │   │   │   └── CalculatorController.java
    │   │   ├── dto
    │   │   │   ├── CalculationRequest.java
    │   │   │   └── CalculationResponse.java
    │   │   └── service
    │   │       └── CalculatorService.java
    │   └── resources
    │       ├── application.properties
    │       └── static                   # Static web assets
    │           ├── index.html           # Semantic HTML5 UI
    │           ├── css\styles.css       # Glassmorphic CSS design system
    │           └── js\app.js            # Client controller & Audio synth
    └── test
        └── java\com\example\calculator
            ├── CalculatorApplicationTests.java
            └── CalculatorServiceTest.java
```
