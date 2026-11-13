# Hybrid Data Storage Decision

- **Status:** Superseded
- **Date:** 2026-10-30
- **Superseded on:** 2026-11-13
- **Replaced by:** [Laravel Backend Architecture Decision](laravel_backend_architecture.md)
- **Scope:** Application data, offline data, and media storage

> This document is retained as historical context. Firebase is no longer part of the target architecture.

## Context

Tang Kinh Cac needs to support a cloud-backed library, community features, reading and listening progress,
offline access, and large media files. A single storage technology would either make offline behavior difficult or
place binary content in a database that is not designed for it.

The project is maintained by one developer as an academic project, so the architecture must remain simple to set
up and operate. A dedicated backend and relational cloud database would add deployment, authentication,
migration, and synchronization work that the current feature scope does not require.

## Decision

The application will use a lightweight hybrid storage architecture:

- **Cloud Firestore** is the cloud source of truth for structured application data.
- **Room** stores local structured data required for caching and offline use.
- **Cloud Storage for Firebase** stores remote binary assets such as cover images, ebook files, audiobook files,
  and blog images.
- **Firebase Authentication** remains responsible for user identity. Storage systems reference users by their
  Firebase UID.

Fragments and ViewModels must not access these data sources directly. Repository interfaces will preserve the
existing MVVM boundary and coordinate remote, local, and media operations.

## Data Ownership

Each data category has one authoritative owner. The same record must not be treated as independently authoritative
in multiple storage systems.

| Data | Authoritative storage | Notes |
| --- | --- | --- |
| User identity | Firebase Authentication | Other stores reference the Firebase UID. |
| User profiles and public metadata | Cloud Firestore | Protected by Firestore Security Rules. |
| Books, chapters, and catalog metadata | Cloud Firestore | Binary content is referenced by a storage path. |
| Blogs, comments, reviews, and notifications | Cloud Firestore | Suitable for document-based and realtime access. |
| Reading and listening progress | Cloud Firestore | Room caches the latest state for offline use. |
| Cached metadata, chapter text, and pending writes | Room | Rebuilt or synchronized through repositories. |
| Downloaded media index and local file paths | Room | Downloaded binary files remain in app-private file storage. |
| Covers, ebooks, audiobooks, and blog images | Cloud Storage for Firebase | Firestore stores paths only. |

## Data Flow

```text
UI
  -> ViewModel
    -> Repository
      -> Cloud Firestore
      -> Room
      -> Cloud Storage for Firebase
```

Repositories decide whether a feature reads from the local cache, refreshes from Firestore, or resolves a media
path from Cloud Storage. Synchronization and conflict handling must be defined by each feature because reading
progress, downloaded content, and community content have different consistency requirements.

## Rationale

This approach was selected because it:

1. Reuses the existing Firebase Authentication and Firestore integration.
2. Supports controlled offline reading and listening through Room.
3. Keeps large files out of Firestore documents.
4. Avoids deploying and maintaining a separate backend during the current project phase.
5. Keeps future migration possible through repository interfaces instead of exposing Firebase or Room types to
   the UI layer.

## Consequences

### Benefits

- Faster implementation than introducing a custom backend and PostgreSQL now.
- Explicit local caching and offline behavior.
- Scalable delivery of media files without increasing Firestore document sizes.
- Clear ownership boundaries between cloud data, local data, and binary assets.

### Costs and Risks

- Repository implementations must coordinate cache invalidation and synchronization.
- Firestore and Storage Security Rules must evolve with every new protected path.
- Offline writes can conflict with newer cloud data and require feature-specific resolution.
- Room schema changes require Android database migrations once the application has persistent user data.

## Alternatives Considered

### Firestore only

Rejected because it does not provide enough control over downloaded content, structured local caching, and
feature-specific offline behavior.

### PostgreSQL with a custom backend

Deferred because authentication verification, API development, hosting, database migrations, and operations would
add substantial work without supporting a current requirement. It should be reconsidered if real payments,
financial ledgers, book ownership, or complex reporting become production requirements.

### Storing media in Firestore or Room

Rejected because databases should store media metadata and references, not large ebook, image, or audio binaries.

## Implementation Constraints

- Do not store the same authoritative record independently in both Firestore and Room.
- Do not store binary media in Firestore documents or Room columns.
- Do not expose Firestore snapshots, Room entities, or Firebase Storage SDK types outside the data layer.
- Validate cloud data with Firestore and Storage Security Rules; the Android client is untrusted.
- Add Room only when a feature has a concrete local or offline requirement.
- Introduce additional infrastructure such as PostgreSQL, Redis, or a search service only when a measured
  requirement justifies its operational cost.

## Reconsider This Decision When

- Real-money transactions or auditable financial records are introduced.
- Cross-collection reporting becomes a core application requirement.
- Firestore query limitations require extensive duplicated data or background aggregation.
- Media delivery requires a dedicated CDN, transcoding pipeline, or digital rights management.
- Offline synchronization rules become too complex for feature-level repositories.
