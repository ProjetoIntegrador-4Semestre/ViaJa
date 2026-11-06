# Viajá: product, billing and screen baseline

Updated 2026-11-05. This is the current product reference. Historical Obsidian specifications remain research/history; they do not override this baseline. This document defines decisions and acceptance criteria, not implemented functionality. Code status and evidence remain in recovery-plan.md.

## Confirmed constraints

- Delivery on 2026-12-02 as APK/emulator demonstration. Possible Google Play distribution next semester.
- Kotlin/XML Views, Retrofit, RecyclerView and Intents; Java/Spring Boot, H2 and LoadDatabase.
- All previously confirmed feature areas remain in scope. No addition of collaborators, bookings processed inside the app, or real payment collection for this delivery.
- Stripe test environment with real Checkout Session creation and verified webhook processing replaces the previous Premium flag simulation. No real cards or charges in the presentation.
- The owner delegated benefits/pricing decisions. The following commercial model is the working decision, not market-validated pricing.

## Product promise

Plan and carry a trip in one place: discover places, build a day-by-day itinerary, keep notes and expenses, and use optional AI assistance. The same trip supports tourism or work priorities. Manual planning must remain useful without paying.

## Free and Premium

| Capability | Free | Premium |
| --- | --- | --- |
| Explore, place details, favorites, manual trips and itineraries | Included without artificial quantity limits | Included |
| Notes/checklists and basic expense recording/totals | Included | Included |
| Maps, available weather, account and trip history | Included | Included |
| AI itinerary generation or substantial revision | 2 successful requests per calendar month | 20 successful requests per monthly billing period |
| Basic itinerary PDF | Included | Included |
| Complete travel pack PDF: itinerary, notes/checklists and expense summary | Not included | Included |
| Explicitly downloaded trip for offline reading | Not included | Included |

One Premium plan at BRL 19.90/month is the initial commercial hypothesis and Stripe test price. No annual plan, trial that converts into a charge, paid quota top-ups, ads, commissions or invented discounts in this delivery. Revisit price only after measuring demand and costs. Offline is read-only, not collaborative synchronization or offline maps/weather/AI. Previously downloaded/exported information is not deleted on cancellation; creation/refresh of Premium packs requires current access.

AI limits are visible before subscribing and while using the assistant. A request means generating a draft or materially revising one, not each chat message. Failed provider calls do not consume the allowance. No unlimited-AI promise. Bound input/output size, duration of suggested itineraries and provider usage; show remaining requests and reset date. Preserve drafts and manual editing after the allowance is reached or a subscription expires. The assistant uses available places and must not invent reservations, live availability or opening hours. User approval is required to apply a draft.

### Commercial rationale

Free solves the main problem and lets a traveler trust the product before paying. Premium charges for saved planning effort and portability, rather than restricting access to the user's own trip. AI quotas place a measurable ceiling on variable costs; complete PDFs and offline trip reading add value without another paid provider for each use.

This is not a profitability claim. Measure net contribution as subscription revenue minus payment processing, Billing charges, AI usage, applicable taxes, refunds and the allocated hosting/support cost. Stripe's current Brazil pricing page lists 3.99% + BRL 0.39 for domestic card transactions: at BRL 19.90 that alone is about BRL 1.18. It excludes Billing and all other costs. A target variable AI budget of at most BRL 3 per fully used Premium cycle is an internal design target to validate, not an observed cost or a model-price quote. Measure both average and maximum-quota consumption before live launch. Track aggregate margin including the cost of free users; do not claim that a positive per-subscriber contribution alone proves overall profit.

## Payment experience

Use Stripe-hosted Checkout launched with Android Custom Tabs, with external browser fallback. The tab uses the user's browser engine and can be visually integrated with the app; it is not a WebView containing a homemade checkout. Preserve XML for Viajá screens. Use Stripe Customer Portal for payment details, invoices and cancellation. This replaces the existing pretend card-management screens.

Flow:

1. Logged-in user opens Premium and sees benefits, limits, BRL/month and cancellation terms.
2. Authenticated API creates a subscription Checkout Session for the user's Stripe Customer and a server-approved Price ID. Never accept an amount, owner or arbitrary redirect URL from the Android client.
3. Android opens the returned Checkout URL. Stripe handles payment fields and payment authentication.
4. Success/cancel URL leads to a controlled return page and then the app. Use verified HTTPS App Links when a domain is available; provide an explicit return link/fallback for the academic environment. URLs carry no JWT or payment secrets.
5. Android refreshes its own subscription through the authenticated API. Returning to the app does not grant Premium.
6. A signed webhook updates the local subscription record. Account activation, renewal, failed invoices and cancellation must work even if the user closes the browser and never returns.

If the webhook is late, show “Confirmando sua assinatura” with refresh and the option to continue using Free. Closing Checkout is not a failed payment. Never display “Pagamento aprovado” merely because a URL was opened.

### Subscription policy

- Successful, verified initial payment grants access through its paid period. No trial for this delivery.
- User cancellation is scheduled for the period end; explain the exact access-end date.
- Failed renewal displays an action to resolve payment and does not extend access beyond the already paid period. Pending payment is not active access.
- Keep Stripe status, paid-through date and cancellation flag distinct from the app's effective access decision.
- The backend checks Premium privileges. A screen flag or a general subscription CRUD update cannot grant paid access.
- Use one customer/subscription association per user; reuse an open session where appropriate and prevent duplicate active purchases.
- Verify the webhook signature against the raw body, persist processed event IDs and apply updates transactionally. Repeated/concurrent deliveries must not grant duplicated benefits. Reconcile current provider state when events arrive out of order. Do not introduce a queue, microservice or event framework for this project.
- Current H2 in-memory/create-drop setup cannot reliably retain Stripe customer, subscription, paid-period and event records across restarts. Keep H2 for the course, but use persistent file storage for the billing demonstration and controlled seed behavior. Do not seed fake paid subscribers as verified Stripe subscriptions.
- Secrets remain server environment variables. Use test keys, test Price IDs and Stripe CLI forwarding for local webhooks. Live charging is outside this delivery.

### Future Google Play gate

APK/emulator demonstration does not settle Play distribution eligibility. Premium contains digital in-app benefits. Before publishing through Play, check the applicable billing requirements and alternative-billing/external-link programs for the target market. Do not promise that a Stripe web page or Custom Tab by itself makes the app compliant.

## Screen direction

Retain green/cream identity but replace inconsistent component behavior. One typography family, explicit Material theme colors, dark readable text on pale surfaces, one selection treatment, conventional icons and consistent Portuguese labels. Initial light palette: green #087A49, cream #F7F5EF, surface #FFFFFF, text #19382A and secondary text #53675A. Calculated contrast is 5.40:1 for white on green, 11.74:1 for main text on cream and 6.08:1 for secondary text on white. These pair checks are not a full accessibility audit. Base spacing 8/16/24dp, screen margins 16dp, touch targets at least 48dp. Screen titles around 24sp, section titles 18sp, body 16sp and secondary labels 14sp. Use content-driven heights, scrolling and font scaling; do not copy a screenshot with absolute placement.

The November 5 emulator review found: white greeting text on a pale lavender card; purple defaults in buttons/chips; duplicate active-state treatment in bottom navigation; a clipped destination chip; excessive card height; fictional profile name/country count and placeholder avatar. These are direct observations, not a full accessibility certification.

| Screen | Primary content and action | Required behavior |
| --- | --- | --- |
| Home | Greeting, next trip or create-trip empty state; secondary destination search | No giant empty greeting card; show loading/error when destinations cannot load |
| Explore | City/place search and explicit filters, followed by actual results | Filters really filter; unavailable photos do not become unrelated beach images |
| Place | Name, verified description/location, price meaning, favorite and add-to-trip | No invented amenities/gallery count; maps/site actions use actual data |
| Trips | User's trips and create action; name, destination, dates/mode | Create/edit requires a city; optional dates are clearly optional |
| Trip | Itinerary by day, then notes and expenses | List all stops; allow deliberate editing/removal/order; weather never fabricated |
| Saved | Real saved places, empty state leading to Explore | Saving stays separate from adding to a trip |
| Profile | Real account, interests, subscription entry and sign out | No fake travel statistics; trip mode belongs to each trip, profile preference only supplies a default |
| Premium | One plan, three clear benefits, Free comparison, price and limits | Primary “Continuar no Stripe”; visible test-environment notice for the demonstration |
| Checkout | Stripe-hosted payment page | Stripe owns payment forms; do not rebuild them in XML or WebView |
| Return | Confirming, active, needs action or no completed purchase | Backend-verified state; no optimistic activation |
| Subscription | Current access, allowance/reset date, renewal/end date, manage action | Customer Portal handles billing actions; no fake card numbers |
| AI | Context, remaining allowance, draft, approval and edit action | No automatic itinerary mutation; errors retain the user's input |
| Recovery | Email input, neutral submission result, token-based reset | Do not claim email was sent without provider confirmation |

An interactive reference for Home/Premium/return/subscription was prepared in the conversation. It is a design proposal with illustrative data, not an Android implementation or a real payment session. It demonstrates readable hierarchy, a single green selection treatment and explicit return states.

Do not design separate pages for every possible subscription action. Premium, subscription and a reusable return/status view are enough alongside Stripe Checkout/Portal. Resolve overlapping Premium/Profile implementations after the screen flow is validated.

## Implementation order replacing the old feature-first sequence

1. Adopt this product/billing baseline, correct conflicting documentation and validate representative Home/Profile/Premium/return layouts.
2. Define screen states and data requirements for remaining flows. Reuse existing colors/styles/layout components; no speculative class inventory.
3. Close authorization, password-field exposure and session errors before exposing billing or connecting a publicly reachable webhook.
4. Implement one test Checkout → webhook → access → return → cancellation flow with persistent records and regression tests.
5. Apply the visual system to the main journeys, then finish itinerary, profile, notes, expenses and external integrations with each screen's contract.
6. Complete offline/PDF/AI entitlements, clean checkout validation, hosted demonstration and academic artifacts.

Acceptance of the baseline does not mean features are implemented. Present to the faculty the distinction between observed working flows, remaining features, pricing hypotheses and measured costs.

## Sources consulted on November 5, 2026

- Stripe Checkout: https://docs.stripe.com/payments/checkout
- Fulfillment and return/webhook distinction: https://docs.stripe.com/checkout/fulfillment?ui=full-page
- Subscription events: https://docs.stripe.com/billing/subscriptions/webhooks
- Signature verification, repeated delivery and local forwarding: https://docs.stripe.com/webhooks
- Customer Portal: https://docs.stripe.com/customer-management
- Brazil processing pricing: https://stripe.com/br/pricing
- Android Custom Tabs: https://developer.chrome.com/docs/android/custom-tabs
- Play payments policy: https://support.google.com/googleplay/android-developer/answer/9858738
