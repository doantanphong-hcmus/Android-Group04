# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run lint (warnings are treated as errors)
./gradlew lint

# Auto-fix ktlint violations
./gradlew ktlintFormat

# Check ktlint without fixing
./gradlew ktlintCheck

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.tangkinhcac.app.ClassName"

# Run instrumented tests (requires connected device or emulator)
./gradlew connectedAndroidTest
```

## Architecture

**Single-activity app.** `MainActivity` hosts a `NavHostFragment` with one nav graph (`nav_main.xml`). The start destination is chosen at launch based on `authRepository.hasAuthenticatedUser` — either `loginFragment` or `homeFragment` — to avoid flashing the login screen on authenticated restart.

**Screen structure:**
- Auth flow: `LoginFragment` → `RegisterFragment` → `RegistrationSuccessFragment`
- Post-auth: `HomeFragment` acts as a tab shell with a `BottomNavigationView`, swapping child fragments via `childFragmentManager`. The Library tab back stack is cleared on re-selection.
  - `CommunityFragment` — social feed with like/repost/bookmark toggles (animated). Post interaction state is kept in `CommunityPostUiStateStore`, a process-scoped in-memory store.
  - `LibraryFragment` — book grid with country and VIP/free filters. Opens `LibrarySearchFragment` on search and `LibraryGenresFragment` on the genres button, both pushed onto `parentFragmentManager` with `addToBackStack`.
  - `EmptyTabFragment` — placeholder for bottom-nav tabs not yet implemented.
- `RankingsActivity` is a temporary prototype launched from `HomeFragment`; it will be integrated into the main nav flow later.
- Community detail and create-post screens are in the main nav graph (`communityPostDetailFragment`, `communityCreatePostFragment`), reached via `Activity.findNavController` from `CommunityFragment`.

**MVVM + Repository pattern:**
- Fragments observe `StateFlow` from ViewModels via `repeatOnLifecycle(STARTED)`.
- ViewModels receive repository interfaces through static `factory()` functions; they never reference Room or HTTP types directly.
- Repositories are instantiated lazily in `TangKinhCacApplication` and injected manually into ViewModels.
- `TangKinhCacApplication` is the single wiring point: it builds the `OkHttpClient`, `Retrofit`, `AuthApi`, `AuthTokenStore`, and all repositories.
- `BuildConfig.API_BASE_URL` defaults to `http://10.0.2.2:8000/api/v1/` (emulator → host machine). Override with `./gradlew assembleDebug -PTANG_KINH_CAC_API_BASE_URL=https://host/api/v1/`.

**Data layer:**
- The app has no Firebase dependency. All remote data goes through the self-hosted Laravel API.
- Auth: `BackendAuthRepository` → `AuthGateway` (`RetrofitAuthGateway`) → `AuthApi`. `AuthRequestInterceptor` attaches the Sanctum bearer token. The repository maps server error codes (`INVALID_CREDENTIALS`, `EMAIL_ALREADY_USED`, ...) to `AuthRepository` failure enums and clears the token on 401.
- `KeystoreAuthTokenStore` encrypts the token with an AES-GCM key held in Android Keystore and stores only ciphertext + IV in SharedPreferences. `save` commits synchronously so navigation cannot outrun persistence.
- `BookRepository` and `BookListViewModel` are empty placeholders; the book catalog gateway is not implemented yet.
- Room (`TangKinhCacDatabase`) provides local caching. Add entities/DAOs only for concrete offline requirements. `CachedBookEntity` / `CachedBookDao` supports `getAll`, `upsertAll`, and `clear`.

**Prototype catalogs (replaced by API data when backend features ship):**
- `LibrarySearchCatalog` — 8 in-memory books used by Library and Rankings. The server seed catalog matches these 8 slugs.
- `CommunityFeedCatalog` — static posts/comments for Community screens. Community is out of scope for backend v1.
- `RankingsMockData` — generates ranked entries by cycling catalog books. Rankings is out of scope for backend v1.

**Package structure:** `feature/<name>/` for each screen (Fragment, ViewModel, UiState); `data/model/`, `data/repository/`, `data/local/`, `data/remote/`; `ui/` for shared custom views.

## Key Conventions

- **UiState is a data class** per feature with typed error enums (e.g., `LoginUiState.Error`, `RegisterUiState.FieldError`), never raw strings. Error enum → string resource mapping lives in the Fragment, not the ViewModel.
- **String resources** for all user-facing text.
- **Input validation** in ViewModels before calling the repository. Emails are trimmed; usernames lowercased with `Locale.ROOT`; phone numbers have separators stripped. `AuthValidation.kt` holds shared constants (`MIN_PASSWORD_LENGTH`, `BIRTH_DATE_PATTERN`, `EMAIL_PATTERN`).
- **Callbacks, not coroutines, across the entire gateway/repository layer.** `AuthGateway` uses `onResult` lambdas; `BackendAuthRepository` follows the same style. ViewModels do not `viewModelScope.launch` for repository calls.
- **Edge-to-edge:** `applySystemBarPadding()` on the content root view handles insets, preserving the view's initial padding.
- **Custom Views in `ui/`:** `CategoryIconView` renders one cell from an 8-icon sprite sheet. `LibraryFilterIconView` renders one of 4 filter icons with per-icon pixel coordinates. `RightAlignedCropImageView` crops a full-screen image from the left to keep right-edge artwork visible.
- **ktlint** is enforced with `android = true`. Run `ktlintFormat` before committing.
- **Lint** runs with `warningsAsErrors = true`. Suppressed checks (`AndroidGradlePluginVersion`, `GradleDependency`, `IconXmlAndPng`) are intentional.
- Room schema files are exported to `app/schemas/`.

## Adding a New Backend Gateway

Follow the pattern established by Auth:

1. Define a `Gateway` interface in `data/remote/<domain>/` returning `AuthGatewayResult`-style sealed results via an `onResult` callback.
2. Create a Retrofit `Api` interface in the same package; add API model data classes in `*ApiModels.kt`.
3. Implement `Retrofit*Gateway` that calls the `Api`, parses the JSON error body (`{"error":{"code","message","fields","request_id"}}`), and maps HTTP responses to the sealed result.
4. Wire the new `Retrofit*Gateway` in `TangKinhCacApplication`, sharing one `OkHttpClient` (with `AuthRequestInterceptor`) and `Retrofit` instance. Today both are built inside the `authApi` lazy block, so lift them into their own lazy properties first.
5. Map gateway results to domain-level outcomes in the Repository; keep HTTP codes and error strings out of ViewModels.

**Server API surface** (`/api/v1`, Sanctum bearer token required except auth endpoints):

| Endpoint | Purpose |
|---|---|
| `POST /auth/register` / `POST /auth/login` | Public auth |
| `GET /auth/me` / `POST /auth/logout` | Authenticated session |
| `GET /books` | Search/list (params: `q`, `genre`, `country`, `access_level`, `sort`, `page`, `per_page`) |
| `GET /books/{id\|slug}` | Book detail |
| `GET /genres` / `GET /genres/{id}/books` | Genre catalog |
| `GET /books/{id}/epub` | EPUB descriptor + signed download URL (expires ~5 min) |
| `GET /books/{id}/audiobook` / `GET /books/{id}/audio-tracks` | Audiobook |
| `GET /media/{file}/download` / `GET /audio-tracks/{id}/stream` | Signed-URL media delivery |

Signed media URLs expire (~5 minutes). Request a fresh descriptor rather than storing or reusing the URL.

The full API contract is in the server repo at `docs/Guide/` (see `authentication.md`, `catalog.md`, `epub.md`, `audiobooks.md`, `conventions-and-errors.md`). The Android-side architecture decision is in `docs/laravel_backend_architecture.md`.

## Backend Context

Naming, layering, and Git rules are in `docs/coding_convention.md`.

Git: branches use `feature/`, `fix/`, `refactor/`, `test/`, `docs/`, `chore/` + kebab-case scope; commits use Conventional Commit prefixes, English imperative subject ≤72 chars, one logical change per commit. CI (`.github/workflows/code-quality.yml`) runs `git diff --check`, `ktlintCheck lintDebug testDebugUnitTest assembleDebug`.

Community, rankings, payments, and real-time features are **out of scope for backend v1**. Those screens remain on in-memory catalog data until their backend requirements are defined. New domain logic must not migrate to the Android client — it goes in the Laravel API.
