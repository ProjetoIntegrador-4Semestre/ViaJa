# Recovery plan

Updated: 2026-11-05. Delivery: 2026-12-02, confirmed by the project owner.

## Constraints and scope

All previously listed product features remain required. Implementation order is not a reduction in scope. Android uses Kotlin, XML Views, Retrofit, RecyclerView and explicit Intents. The API uses Java, Spring Boot, H2 and LoadDatabase. Source code and identifiers are written in English. New code contains no comments. Changes use semantic commits.

Academic references:

- Android: `/Users/rafa/AndroidStudioProjects/MyApplication`.
- API: `/Users/rafa/IdeaProjects/corp-financial-api`.
- Historical product documentation: `/Users/rafa/Library/Mobile Documents/iCloud~md~obsidian/Documents/second-brain/05_projects/active/viaja`.

The Android reference demonstrates Retrofit callbacks, typed models, XML layouts, RecyclerView with Adapter/ViewHolder, and Intent extras. It is an instructional example, not a complete architecture to copy. The API reference demonstrates repositories, controllers, assemblers, paginated HATEOAS collections, HTTP responses and database seeding. Domain-specific behavior must be evaluated before reuse.

## Verified baseline

| Area | Evidence | Meaning |
| --- | --- | --- |
| Android build | `./gradlew :app:assembleDebug --offline` succeeds | Compilation alone does not establish working navigation or integration |
| Android launch | Installed on emulator-5554; existing session redirects Welcome to Home | Home crashed while inflating its Material 3 TextInputLayout under a Material Components daytime theme |
| Theme correction | Daytime theme aligned with existing Material 3 nighttime theme; rebuilt and launched | Home remains visible after launch; remaining screens need individual verification |
| API package | `./mvnw verify` succeeds after fetching missing packaging dependencies | No Java automated test suite was found in the source inventory |
| API startup | Local process on port 18080 starts H2 and completes LoadDatabase | Initial data can be exercised without an external database |
| Pagination | `/cities?page=0&size=2` returns three total records, two pages and navigation links | Pagination and HATEOAS already exist and should be verified resource by resource |
| API documentation | `/v3/api-docs` returns 200 | Availability does not establish accurate schemas and examples |
| Authentication | Seeded user logs in, receives Authorization header and accesses `/trips` | Invalid password incorrectly returned 404 before the authentication-specific handler was added |
| Android trips | TripRepository returns local mock data and simulated mutation responses | Creating or deleting through it does not demonstrate API persistence |
| Android places | CityRepository and PlaceRepository read MockDataSource | Current displayed lists do not establish a working API-to-screen flow |

The working tree already contained staged and unstaged Android changes before this audit. Preserve them and keep recovery commits limited to the files changed during recovery.

Historical Obsidian documents still describe an older PHP baseline. Use them as product history, not as evidence of the current implementation.

## Delivery sequence

These are target checkpoints, not claims of completed work.

| Target | Work | Exit condition |
| --- | --- | --- |
| November 5–8 | Stabilize launch/navigation, inventory actual features, define contracts and ownership rules | Reproducible execution, explicit pending-feature list, and a verified API contract for the first flow |
| November 9–15 | Real place list/detail, authentication/session, trips, saved places and itinerary | API data reaches RecyclerView; Intent opens the selected detail; mutations survive refetch; users cannot modify each other's records |
| November 16–22 | Notes/checklists, expenses/receipts, maps, weather, AI itinerary draft and approval | Each feature has explicit input/output, error behavior and a demonstrated Android/API flow |
| November 23–27 | Google access, password recovery/reset, profile, simulated Premium/subscription, PDF export, offline reading and trip history | External prerequisites configured; every required feature has recorded verification |
| November 28–December 1 | Full regression, visual review, Swagger examples, seed consistency and academic artifacts | Fresh installation and fresh API startup reproduce the demonstration using documented steps |
| December 2 | Delivery | Verified application and API, documentation and required academic materials |

External-account prerequisites must be inventoried immediately, despite the later feature checkpoints: Google OAuth configuration, AI provider credentials, email delivery for recovery, and any selected map/weather provider. Keep credentials outside source control and the APK. The owner's requirement is all functionality; do not silently substitute success messages for these integrations.

## First integrated flow

1. Define the list and detail payloads for a place, including consistent identifiers and price meaning.
2. Check the existing API responses: `/places/search` maps to PlaceResponse, while `/places/{id}` exposes Local through EntityModel. Do not assume these are identical.
3. Define city selection using city identity. Current API text search covers place name/address, so searching a city name is not a reliable city filter.
4. Replace the mock for this flow with typed Retrofit callbacks following the classroom pattern.
5. Show loading, empty, failure and successful states.
6. Open details using an explicit Intent with the selected identifier.
7. Verify against seeded data, failed requests and a missing identifier.

Do not propagate the current unrestricted use of `Any` into new API consumers. Add only abstractions that serve an identified behavior. Existing ViewModels/repositories must be evaluated before either retaining or deleting them.

## Product and visual rules

For each flow, record purpose, inputs, visible information, actions, navigation, ownership and failure states before building the screen. Use a small shared set of colors, spacing, typography and XML components. Historical prototypes are references, not acceptance criteria.

Saved places and itinerary entries are separate associations. Tourism/work mode must have one clear owner and observable behavior. Premium is simulated, without real payment processing. AI produces a draft that requires user approval before changing an itinerary. External booking opens a provider rather than processing a booking internally.

## Remaining audit work

- Reproduce login, registration, visitor access and each navigation destination in both display modes.
- Audit session restoration and expiry; the existing emulator session displayed an empty greeting.
- Resolve overlapping ProfileActivity implementations after tracing every reference.
- Verify authorization and resource ownership for all private endpoints.
- Replace the broad RuntimeException-to-404 policy with explicit error semantics resource by resource.
- Verify validation, create/update/delete statuses, Location headers and invalid payloads.
- Reconcile entity relationships, deletion behavior, price units and seed data.
- Reconcile Swagger schemas/examples with actual requests and responses.
- Inventory each required feature as absent, simulated, partial or verified; file existence is not completion.
- Test every completed flow on the emulator and record its evidence before expanding to the next one.

## Place integration checkpoint — November 5

PlaceRepository now uses typed Retrofit callbacks for search and detail. API search includes city names; list/detail share PlaceResponse, with description and integer price in cents. Detail retains HATEOAS links. Existing collection/write contracts remain unchanged. The Adapter's rating/price identifiers were corrected to match XML, fixing a reproduced RecyclerView crash. Currency formatting preserves cents. List loading, empty, error/retry and result count are visible.

Validation: Maven verification, Android debug compilation and seven API smoke checks passed. On Medium_Phone, Home → Explore displayed three API places; selecting the museum opened its description, rating and BRL 150.00 price through the existing Intent. This test used `adb reverse tcp:18080 tcp:18080` and `./gradlew :app:assembleDebug -PapiBaseUrl=http://127.0.0.1:18080/`. Direct emulator-host routing did not complete in this session; the reverse route worked. UI empty/error states were implemented but not exercised manually in this checkpoint.

The hosted API has not been deployed. Its update is required before using the default hosted Android URL with this new contract. Home city cards, saved actions, trip actions, partner-site action, photos and other detail decorations still include mock/illustrative behavior and remain pending. This checkpoint establishes the search-to-detail read flow only.

## Session checkpoint — November 5

AuthRepository now uses the API's Authorization response header instead of a placeholder token. It rejects successful responses without a real user or a structurally valid, unexpired token, stores user identifiers as Long and retains the user's default mode. Existing login/register Retrofit callbacks were retained. ApiClient reads the persisted token for requests and clears the matching session after a 401; rejection of an older token cannot erase a newer session. Local expiry checking controls UI session restoration only; JWT signature verification and authorization remain server responsibilities. Legacy simulated sessions are invalidated. Session preferences are excluded from cloud backup and device transfer.

Validation: Android debug build passed. An isolated instrumented test passed on the emulator, covering simulated/expired tokens, session restoration, large identifiers and matching-token invalidation. The test ran directly through AndroidJUnitRunner because Gradle's offline test launcher dependency was unavailable. Manual login displayed Rafael Reis; restarting the app returned to Home. The JWT persisted by Android was accepted by the local API's protected trips endpoint without printing its value.

Remaining: encrypted token storage at rest, explicit expired-session navigation while a private screen is already open, removal of mock identity from profile/saved/trip screens, ownership checks throughout private API endpoints, and integration of real saved/trip mutations. The hosted API was not deployed in this checkpoint.
