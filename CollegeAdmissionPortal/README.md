# College Admission Portal (OOPS Project)

A complete, clean, and interactive **College Admission Portal** modeled after the **State CET Cell Centralized Admission Process (CAP)** for undergraduate engineering admissions.

Built strictly using:
* **Frontend:** HTML5 + Vanilla CSS3 + Vanilla JavaScript (No React, No Angular, No third-party UI libraries)
* **Backend:** Core Java Standard Library only (`com.sun.net.httpserver.HttpServer` — No Spring Boot, No external JARs)
* **Data Storage:** Plain text files (`.txt`) using `BufferedReader` and `BufferedWriter` + organized server-side document directory (`data/documents/`)

---

## 1. Complete Centralized Admission Workflow

The admission process follows the official 6-phase centralized seat allotment lifecycle:

```
Student Registration
        ↓
   Student Login
        ↓
Personal & Contact Details
        ↓
  Academic Marks Entry (SSC, HSC, CET)
        ↓
Mandatory Document Upload (Aadhaar, SSC, HSC, CET marksheets - PDF/JPG/PNG)
        ↓
Branch Preference Selection (Arranged in Priority Order 1 to 6)
        ↓
Final Application Submission
        ↓
Admin Scrutiny & Document Verification (Approve or Reject credentials)
        ↓
Admin Branch Allocation (Assign engineering seat with allocation remarks)
        ↓
Student Notification & Provisional Allotment Review
        ↓
Student Action:
   ├── ACCEPT ALLOCATION ──→ Admission Confirmed / Seat Freezed 🎓
   └── REJECT ALLOCATION ──→ Returned to Admission Committee for Reallocation ↺
```

---

## 2. Project Folder Structure

```
CollegeAdmissionPortal/
│
├── run.bat                     # Double-click runner script for Windows
├── README.md                   # Project documentation & viva guide
│
├── frontend/                   # All user interface files (HTML + CSS + Vanilla JS)
│   ├── index.html              # Home page with portal overview & admission steps
│   ├── register.html           # Student registration form
│   ├── login.html              # Student login form
│   ├── application.html        # Multi-section admission form (Academic, Document Uploads, Branch Preferences)
│   ├── status.html             # Application status tracker, document previews, & branch acceptance/rejection
│   ├── admin-login.html        # Admission officer / Admin login
│   ├── admin-dashboard.html    # Admin scrutiny modal, document verification, & branch allocation panel
│   └── style.css               # Professional government/CET portal styling with modals and responsive grids
│
├── backend/                    # Core Java classes demonstrating OOP
│   ├── User.java               # Abstract superclass (Encapsulation, Inheritance, Abstraction)
│   ├── Student.java            # Subclass representing a student (Inheritance, Polymorphism)
│   ├── Admin.java              # Subclass representing an admin (Inheritance, Polymorphism)
│   ├── Document.java           # Model class encapsulating uploaded credentials (Encapsulation)
│   ├── Application.java        # Model class encapsulating application details, preferences, and documents
│   ├── AdmissionSystem.java    # Central business controller, ArrayLists, and file handling I/O
│   └── Main.java               # Core Java built-in HttpServer connecting frontend & backend REST APIs
│
└── data/                       # Plain text database and document storage
    ├── students.txt            # Registered student accounts (ID|Name|Email|Password|Phone)
    ├── applications.txt        # Full admission records with preferences, allocation, and document maps
    ├── admin.txt               # Administrator credentials
    └── documents/              # Organized server-side storage for candidate document uploads
        ├── APP1001/
        ├── APP1004/
        └── APP1005/
```

---

## 3. Key Feature Modules & Implementation

### 1. Student Document Upload
- **Mandatory Documents Supported:**
  1. Aadhaar Card
  2. SSC (10th) Marksheet
  3. HSC (12th) Marksheet
  4. MHT-CET Scorecard
  5. Other Supporting Document (Caste, Income, Domicile)
- **Validation:** Enforces supported formats (`.pdf`, `.jpg`, `.jpeg`, `.png`) and file size limits (maximum 5 MB per document).
- **Server Storage:** Uploaded files are organized in `data/documents/<appId>/` with sanitized filenames and mapped in `Application`. Files remain permanently accessible across server restarts.
- **Preview & Download:** Candidates and admins can preview files in-modal (`/api/document/view` with `Content-Disposition: inline`) or download them (`/api/document/download` with `Content-Disposition: attachment`).

### 2. Branch Preference Selection
- Candidates choose from 6 engineering branches:
  1. Computer Engineering
  2. Information Technology
  3. Artificial Intelligence & Data Science
  4. Electronics & Telecommunication
  5. Mechanical Engineering
  6. Civil Engineering
- **Priority Ordering:** Interactive priority selector (1st Priority to 6th Priority) with **Up (▲)** and **Down (▼)** buttons to arrange preferences.
- **Validation:** Automatic duplicate branch detection preventing duplicate selections.
- **Review Summary:** Live summary box allowing candidates to review their ordered choices prior to submission.

### 3. Admin Document Verification
- In `admin-dashboard.html`, clicking **Scrutiny & Allocate** opens a comprehensive scrutiny modal.
- Displays candidate full name, registered email, phone number, category, and academic scores (10th %, 12th %, CET percentile).
- Lists all uploaded marksheets with real-time status and **Preview** / **Download** buttons.
- Admin can mark documents as **Documents Verified** or **Verification Rejected** with official scrutiny remarks.

### 4. Admin Branch Allocation
- Admin inspects candidate merit, document status, and the candidate's prioritized branch choices.
- Admin selects an available engineering branch from a dropdown and provides official allocation remarks.
- Submitting updates the allocation status to **Awaiting Student Acceptance** and dispatches the decision to the candidate.
- Supported Allocation Statuses:
  - `Pending Allocation`
  - `Branch Allocated`
  - `Awaiting Student Acceptance`
  - `Accepted` (Admission Confirmed)
  - `Rejected`
  - `Allocation Rejected by Student`

### 5. Student Branch Acceptance & Confirmation
- In `status.html`, when a seat is allocated, a prominent **Provisional Seat Allocation Card** is displayed showing the allocated branch, merit scores, and admin remarks.
- Features explicit **Accept** and **Reject** buttons:
  - **Accept:** Sets status to `Accepted` / `Admission Confirmed`. The provisional admission certificate is unlocked and displayed.
  - **Reject:** Sets status to `Allocation Rejected by Student` and returns the application to the admin panel with the candidate's reason for reallocation.
  - Candidates are **never** considered admitted before explicitly accepting their allocation.

---

## 4. REST API Endpoints Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/register` | Student account registration |
| `POST` | `/api/login` | Student authentication |
| `POST` | `/api/apply` | Application submission with academic marks and branch preferences |
| `POST` | `/api/upload-document` | Uploads binary document file (PDF/JPG/PNG) for candidate |
| `GET` | `/api/document/view` | Streams file with `inline` disposition for in-browser preview |
| `GET` | `/api/document/download` | Streams file with `attachment` disposition for download |
| `GET` | `/api/status?email=...` | Retrieves student application status, preferences, documents, and allocation |
| `POST` | `/api/student/allocation-response` | Candidate submits `Accept` or `Reject` decision |
| `POST` | `/api/admin-login` | Administrator authentication |
| `GET` | `/api/admin/applications` | Retrieves all applications with full details for scrutiny |
| `POST` | `/api/admin/verify-documents` | Updates document verification status (`Documents Verified` / `Verification Rejected`) |
| `POST` | `/api/admin/allocate-branch` | Assigns branch and sets status to `Awaiting Student Acceptance` |
| `POST` | `/api/admin/action` | Legacy status update (`Approved` / `Rejected`) |

---

## 5. OOPS Concepts Viva Cheat Sheet

| OOP Concept | Where it is Used in Code | Explanation for Viva |
| :--- | :--- | :--- |
| **Classes & Objects** | All Java files | `Student`, `Admin`, `Document`, and `Application` are blueprints (classes). Calling `new Application(...)` instantiates an object in memory. |
| **Encapsulation** | `User.java`, `Document.java`, `Application.java` | Member variables are declared `private` to prevent unauthorized modification. Controlled access is provided through public getters and setters. |
| **Inheritance** | `Student extends User`, `Admin extends User` | `User` is the base parent class containing common attributes (`id`, `name`, `email`, `password`, `phone`). Both `Student` and `Admin` inherit them, eliminating duplication. |
| **Constructor Chaining (`super`)** | `Student.java`, `Admin.java` | Child constructors call `super(...)` to invoke the parent `User` constructor. |
| **Abstraction** | `User.java` | `User` is declared `abstract`. It cannot be directly instantiated and declares the abstract method `public abstract String getRoleDescription()`. |
| **Polymorphism (Method Overriding)** | `getRoleDescription()` in `Student` and `Admin` | Subclasses override `getRoleDescription()` to return role-specific descriptions at runtime. |
| **Method Overloading** | `Application.java`, `AdmissionSystem.java` | Multiple constructors and `submitApplication(...)` methods with different parameter signatures provide flexibility. |
| **Collections (`ArrayList`, `Map`)** | `AdmissionSystem.java`, `Application.java` | Dynamic lists `ArrayList<Student>` and `ArrayList<Application>` manage memory records; `Map<String, String>` maps uploaded documents. |
| **File Handling & Binary I/O** | `AdmissionSystem.java`, `Main.java` | Uses `BufferedReader`/`BufferedWriter` for text records, and `FileOutputStream`/`FileInputStream` for binary document uploads. |

---

## 6. How to Compile and Run on Windows

### Option 1: One-Click Runner (Easiest)
1. Open the `CollegeAdmissionPortal` folder.
2. Double-click the file named **`run.bat`**.
3. It compiles the Java backend, starts the server at `http://localhost:8080/`, and launches your browser.

---

### Option 2: Manual Terminal / PowerShell
1. Open **Command Prompt** (`cmd`) or **PowerShell**.
2. Navigate to the `backend` folder:
   ```cmd
   cd "c:\Projects\oops project\CollegeAdmissionPortal\backend"
   ```
3. Compile all Java files:
   ```cmd
   javac *.java
   ```
4. Run the server:
   ```cmd
   java Main
   ```
5. Open your web browser to:
   ```
   http://localhost:8080/
   ```

To stop the server at any time, press **`Ctrl + C`** in the terminal window.

---

## 7. Pre-configured Demo Accounts

### Student Accounts:
* **Email:** `rahul@gmail.com` | **Password:** `rahul123`
* **Email:** `aniket@gmail.com` | **Password:** `aniket123`
* *(Or click "New Registration" to create a new applicant account)*

### Admin Officer Account:
* **Email:** `admin@cet.gov.in`
* **Password:** `admin123`
* *(Pre-configured in `data/admin.txt`)*
