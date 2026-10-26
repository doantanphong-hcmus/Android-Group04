# Tàng Kinh Các Coding Conventions

These conventions apply to all code, tests, resources, and Git contributions in the Tàng Kinh Các Android project.

The project follows the official Kotlin coding style. Prefer clear, simple code over premature abstractions.

## 1. General Rules

- Write all code, identifiers, comments, commit messages, and technical documentation in English.
- Use Vietnamese only for user-facing text stored in Android string resources.
- Use four spaces for indentation. Do not use tabs.
- Keep lines at or below 120 characters when practical.
- Do not use wildcard imports.
- Remove unused code and imports before review.
- Do not keep commented-out code. Git already preserves history.
- Prefer immutable values (`val`) over mutable values (`var`).
- Add a dependency only when the Android SDK or existing dependencies cannot reasonably solve the problem.

## 2. Kotlin Naming

| Element | Style | Example |
|---|---|---|
| Package | lowercase, no underscores | `com.tangkinhcac.app.feature.auth` |
| Class/interface/object | `PascalCase` | `LoginViewModel`, `BookRepository` |
| Function | `lowerCamelCase`, verb first | `loadPublishedBooks()` |
| Variable/property | `lowerCamelCase`, descriptive noun | `selectedBook`, `emailAddress` |
| Boolean | Start with `is`, `has`, `can`, or `should` | `isLoading`, `hasNextChapter` |
| Constant | `UPPER_SNAKE_CASE` | `MIN_PASSWORD_LENGTH` |
| Enum value | `UPPER_SNAKE_CASE` | `PUBLISHED`, `DRAFT` |
| Generic type | Short `PascalCase` | `T`, `ResultT` |
| Test method | Backtick sentence describing behavior | `` `login returns error for invalid credentials` `` |

Additional rules:

- Use meaningful names. Avoid names such as `data`, `item`, `temp`, `obj`, or `manager` unless their meaning is obvious in the local scope.
- Do not include the type in a Kotlin variable name: use `books`, not `bookList`.
- Use `_name` only for a private mutable backing property:

```kotlin
private val _uiState = MutableStateFlow(LoginUiState())
val uiState = _uiState.asStateFlow()
```

- Name repository interfaces by domain, and implementations by data source:

```kotlin
interface BookRepository
class FirebaseBookRepository : BookRepository
```

## 3. File and Type Naming

- A file containing one main class must have the same name as that class: `LoginViewModel.kt`.
- Use one public top-level class or interface per file.
- Name screen classes by role: `LoginFragment`, `LoginViewModel`, and `LoginUiState`.
- Name models as singular nouns: `Book`, `Chapter`, and `User`.
- Extension files must describe their target or purpose: `ViewExtensions.kt`.
- Do not create `Utils`, `Helpers`, `Common`, or `Base` files without a concrete repeated use case.

## 4. Android Resource Naming

Use lowercase `snake_case` for every Android resource.

| Resource | Pattern | Example |
|---|---|---|
| Activity layout | `activity_<name>` | `activity_main.xml` |
| Fragment layout | `fragment_<name>` | `fragment_login.xml` |
| List item layout | `item_<name>` | `item_book.xml` |
| Drawable | `<purpose>` or `ic_<name>` | `background_reader.xml`, `ic_home.xml` |
| Menu | `menu_<name>` | `menu_main_bottom_navigation.xml` |
| Navigation graph | `nav_<name>` | `nav_main.xml` |
| String | `<feature>_<purpose>` | `auth_login_button`, `library_empty_message` |
| Color | Semantic purpose | `text_primary`, `surface_background` |
| Dimension | Semantic purpose | `spacing_medium`, `text_size_title` |
| View ID | `<purpose>_<view_type>` | `email_input`, `login_button`, `books_list` |

- Never hard-code user-facing text in Kotlin or layout files. Put it in `strings.xml`.
- Reuse semantic colors and dimensions instead of repeating literal values.
- Avoid abbreviations such as `btn`, `txt`, or `rv` in new resource IDs and variables.

## 5. Architecture Rules

The application uses a single-activity MVVM architecture with repositories:

```text
Fragment → ViewModel → Repository interface → Firebase implementation
```

### Fragment

- Renders state and forwards user actions to the ViewModel.
- Contains navigation and Android view code only.
- Must not call Firebase directly or contain business rules.
- Collects observable state with the view lifecycle.

### ViewModel

- Owns one immutable UI state exposed to the Fragment.
- Coordinates screen behavior and calls repository interfaces.
- Must not hold `Activity`, `Fragment`, `View`, or `Context` references.
- Converts technical failures into user-friendly state or events.

### Repository

- Defines domain-focused operations such as `login()` or `getPublishedBooks()`.
- Keeps Firebase SDK calls inside concrete Firebase repository classes.
- Must not expose `DocumentSnapshot`, `QuerySnapshot`, or other Firestore implementation details to UI code.
- Returns explicit success or failure through `Result`, a project result type, or a documented stream.

### Models and UI state

- Use immutable `data class` models with safe defaults when Firestore deserialization requires them.
- Keep screen-only properties in UI state, not in data models.
- Name UI state properties consistently: `isLoading`, `data`, and `errorMessage` where appropriate.
- Create a new package only when its first real class is added.

## 6. Functions and Control Flow

- Keep functions focused on one responsibility.
- Prefer early returns over deeply nested conditions.
- Use named arguments when multiple parameters share the same type or their meaning is unclear.
- Do not use `!!` in production code. Handle nullable values explicitly.
- Do not swallow exceptions. Either handle them or propagate a meaningful failure.
- Avoid boolean parameters that obscure intent. Prefer separate functions or an enum when behavior differs significantly.
- Extract shared code only after real duplication appears.

## 7. Comments and Documentation

- Comments explain **why**, not what the code already says.
- Keep comments short and update them when behavior changes.
- Use KDoc for public APIs whose purpose, constraints, or side effects are not obvious.
- Do not add comments that merely repeat a function or variable name.
- Reference tracked work in TODO comments:

```kotlin
// TODO(TKC-123): Restore the exact reading position after font settings are implemented.
```

- Mark an intentional MVP limitation with the project shortcut format:

```kotlin
// shortcut: progress is stored per chapter only, add text offset when reader customization is implemented.
```

- Never include credentials, personal data, access tokens, or internal URLs in comments or logs.

## 8. Error Handling and Logging

- Validate user input before calling Firebase.
- Show user-friendly messages; do not display raw exception messages directly in the UI.
- Preserve the original exception for logs or debugging when useful.
- Do not log passwords, authentication tokens, personal content, or full payment data.
- Use one consistent tag per class when logging is necessary.
- Remove temporary debug logs before merging.

## 9. Firebase Rules

- Treat the Android client as untrusted.
- Keep book and chapter writes denied unless an explicit admin workflow is implemented.
- Allow users to read or write private data only under their own UID.
- Deny access by default for collections that are not explicitly supported.
- Validate required fields and allowed value types at the security-rule boundary.
- Never commit service-account files, `.env` files, signing keys, or `google-services.json`.
- Update and test Firestore rules whenever a data path or permission changes.

## 10. Git Branch Naming

Use lowercase kebab-case with one of these prefixes:

| Purpose | Pattern | Example |
|---|---|---|
| New feature | `feature/<scope>` | `feature/auth-flow` |
| Bug fix | `fix/<scope>` | `fix/login-validation` |
| Refactoring | `refactor/<scope>` | `refactor/book-repository` |
| Tests | `test/<scope>` | `test/firebase-rules` |
| Documentation | `docs/<scope>` | `docs/coding-conventions` |
| Build/tooling | `chore/<scope>` | `chore/firebase-setup` |

Branch rules:

- Create branches from the latest accepted `main`.
- Keep each branch focused on one cohesive feature, fix, or maintenance task.
- Do not use personal names, dates, uppercase letters, spaces, or vague names such as `update` or `new-feature`.
- Delete merged branches after confirming the merge is complete.

## 11. Commit Messages

Use Conventional Commit prefixes:

```text
feat: add email registration flow
fix: prevent duplicate login requests
refactor: inject book repository into view model
test: cover failed registration state
docs: add coding conventions
chore: configure Firebase dependencies
```

- Write the subject in English and imperative mood.
- Keep the subject concise, preferably no longer than 72 characters.
- Use one logical change per commit.
- Do not use messages such as `update`, `fix stuff`, `done`, or `ok`.

## 12. Tests

- Test behavior, not private implementation details.
- Every non-trivial branch, security rule, money operation, or permission check requires a test.
- ViewModel tests must cover loading, success, empty, and failure states where applicable.
- Firebase Rules tests must cover both allowed and denied access.
- Use fake repository implementations in ViewModel unit tests. Do not call live Firebase services.
- A bug fix should include a regression test when the behavior can be tested reasonably.

## 13. Review Checklist

Before requesting review, confirm that:

- The change matches its task and contains no unrelated refactoring.
- Naming follows this document and no new vague abstraction was introduced.
- Fragments do not call Firebase and ViewModels depend on repository interfaces.
- Loading, empty, success, and error states are handled where relevant.
- User-facing text is stored in resources.
- Secrets and sensitive values are not present in code, logs, or Git.
- Relevant tests were added or updated.
- `assembleDebug`, unit tests, and `git diff --check` pass.
- The branch and commit names follow the agreed conventions.

