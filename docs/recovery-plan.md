# Recovery plan

Updated: 2026-11-05. Delivery: 2026-12-02, confirmed by the project owner.

## Current product authority

Read [product-baseline.md](product-baseline.md) first. The owner confirmed Stripe test Checkout plus real webhooks, APK/emulator delivery and potential Play publication later. This replaces the earlier Premium-flag simulation. Product/billing alignment, documentation and representative visual validation now precede the previous implementation order. Historical checkpoints below remain evidence of what was implemented; their pending lists and baseline rows are dated snapshots, not a current feature inventory.

## Constraints and scope

All previously listed product features remain required. Implementation order is not a reduction in scope. Android uses Kotlin, XML Views, Retrofit, RecyclerView and explicit Intents. The API uses Java, Spring Boot, H2 and LoadDatabase. Source code and identifiers are written in English. New code contains no comments. Changes use semantic commits.

Academic references:

- Android: `/Users/rafa/AndroidStudioProjects/MyApplication`.
- API: `/Users/rafa/IdeaProjects/corp-financial-api`.
- Historical product documentation: `/Users/rafa/Library/Mobile Documents/iCloud~md~obsidian/Documents/second-brain/05_projects/active/viaja`.

The Android reference demonstrates Retrofit callbacks, typed models, XML layouts, RecyclerView with Adapter/ViewHolder, and Intent extras. It is an instructional example, not a complete architecture to copy. The API reference demonstrates repositories, controllers, assemblers, paginated HATEOAS collections, HTTP responses and database seeding. Domain-specific behavior must be evaluated before reuse.

## Initial diagnostic baseline (historical)

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

## Earlier delivery sequence (superseded by product-baseline.md)

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

Saved places and itinerary entries are separate associations. Tourism/work mode must have one clear owner and observable behavior. Premium now uses Stripe test Checkout/webhooks as defined in product-baseline.md; live charges are outside the delivery. AI produces a draft that requires user approval before changing an itinerary. External booking opens a provider rather than processing a booking internally.

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

## Saved places checkpoint — November 5

Saved places now use Retrofit and the authenticated user's identifier, without a fallback to user 1. The Android repository reads every HAL page, then fetches each saved place's real detail; the Adapter no longer reads MockDataSource. Detail loads bookmark state from the API and changes it only after a successful write. Guests are routed to login. The list refreshes on return and exposes loading, empty and error/retry states. The existing saved-items layout is included with this integration; a zero-width name constraint in the saved card was corrected, and the unrelated stock photo is hidden.

API saved endpoints check the authenticated user's ownership before reads or writes. Creation ignores client-provided saved-record/user identifiers, validates the place and returns a resolvable Location/self link. Deletion runs in a transaction. Tests cover cross-user GET/POST/DELETE rejection, missing place, duplicate save, attempted record-ID injection, paginated records and removal. These protections currently apply to saved endpoints only; trips and other private resources remain to be audited.

Validation: Maven verification and Android debug build passed; all eight API smoke checks passed. On the emulator, the real saved place opened its detail, removal updated the API, and returning displayed the empty state. Saving again through detail restored the place to the list with its real name. Offline failure/retry and multi-page Android rendering were not manually exercised. Local H2 still resets on server restart; no hosted deployment was performed.

The saved list currently fetches place details sequentially after reading its pages. This keeps the classroom contract straightforward but will need reconsideration if collections become large. Trip actions and profile identity remain outside this checkpoint.

## Trips and itinerary checkpoint — November 5

TripRepository and CityRepository now use typed Retrofit responses and read every HAL page. The trip list requires a real session, displays the API user's trips, refreshes after creation and exposes loading/empty/error states. The existing create dialog now offers cities from the API and fixed tourism/work choices. Dates remain optional. Cards show actual dates/budgets rather than template values. Dashboard loads the trip and its first ordered stop from the API; weather is explicitly unavailable. Adding a place opens trip selection and appends it to day 1 after API confirmation; duplicate entries and places outside the destination city are rejected.

API creation previously passed the user ID as city ID and left the owner null. Creation now derives ownership from the authenticated user, validates name/city/mode/dates/budget, sets timestamps and returns 201 with Location. Reads, updates and deletes require ownership, and global trip/stop lists are scoped to the authenticated user. Stops validate positive day/order, matching city and duplicate association. Trip deletion removes its stops, notes, memos and trip media in one transaction. The unused API TripResponse DTO was removed; existing entity assemblers remain the response pattern. No new production source files or architecture layers were introduced.

Validation: Maven verification, Android debug build and nine API smoke checks passed. The emulator demonstrated a user-scoped list, creating a trip with the city picker, reading seeded trip details and appending a place to the newly created trip, where it appeared as the first stop. The API smoke test covers cross-user trip/stop requests, invalid inputs, optional dates, create/update/delete and itinerary mutations. Direct database assertions for note/memo/media cleanup remain untested.

Still pending: Android editing of dates/budget/status, trip deletion UI, full itinerary editor/day selection/reordering and all-stop display, and real weather. Appending to day 1 is the current minimal behavior, not a complete itinerary editor. Note/memo/user endpoints still require their own ownership audit. AI chat and other dashboard sections remain incomplete. Hosted deployment has not been performed.
