# OnlyFreeAI — Product Requirements Document

> *Only Free. Only AI.*
> **v1.0 | May 2026 | Confidential**

---

## 1. Product Overview

OnlyFreeAI is an Android application that serves as the **definitive directory of verified free AI tools**. Every tool listed is manually tested and confirmed to be genuinely free — no credit cards, no trials, no hidden paywalls. Users can browse, search, save, and submit AI tools with full confidence.

| Field | Value |
|---|---|
| **App Name** | OnlyFreeAI |
| **Platform** | Android (Kotlin) |
| **Version** | 1.0 |
| **Document Status** | Final |
| **Last Updated** | May 2026 |

---

## 2. Problem Statement

AI tool directories today list thousands of tools across paid, freemium, and free tiers. Users waste time clicking through tools only to hit a credit card wall. There is no trusted, dedicated source for tools that are 100% free with no deception.

### Core Pain Points
- Existing directories mix paid and free tools with no clear distinction
- Tools labelled "free" often require credit cards or have unusable free tiers
- No directory manually verifies free tier claims
- Users get burned repeatedly and lose trust

---

## 3. Solution

OnlyFreeAI solves this with one promise: **every tool on the platform has been manually tested and verified to be genuinely free**. The Verified Free badge is our brand. Trust is our product.

### Key Differentiators
- Only free tools — no paid, no freemium, no trials
- **Verified Free** badge on every listing
- "What's NOT free" section — brutally honest about limitations
- Community flagging when tools go paid
- Developer submission with auto-fetch from URL

---

## 4. Target Users

| Segment | Need |
|---|---|
| **Students** | Need free tools for studying, writing, designing, coding |
| **Freelancers** | Want to cut costs without sacrificing quality |
| **Creators** | Need free design, video, audio, writing tools |
| **Developers** | Building projects without budget for paid tools |
| **Early Builders** | Solopreneurs validating ideas on zero budget |

---

## 5. Features

### 5.1 Must Have — Launch

| Feature | Description | Priority |
|---|---|---|
| **Google Sign In** | Every user logs in via Google. Firebase Auth handles this securely. | 🟢 Must Have |
| **Tool Cards** | Each tool shows logo, name, 2-3 line description, Verified Free badge, category tag, and Visit Tool button. | 🟢 Must Have |
| **Smart Search** | Search by tool name or what you want to do. Instant results. | 🟢 Must Have |
| **Category Filter** | Single-select dropdown to filter tools by category. | 🟢 Must Have |
| **Tool Detail Page** | Full page showing what is free, what is NOT free, best for, and similar tools. | 🟢 Must Have |
| **Save to My Stack** | Users save tools to their personal collection. | 🟢 Must Have |
| **Submit a Tool** | Developers paste URL — app auto-fetches name, logo, description. They add category and free tier details. | 🟢 Must Have |
| **Admin Panel** | Only admin account can approve or reject submissions. | 🟢 Must Have |
| **Verified Free Badge** | Manually verified by admin before going live. | 🟢 Must Have |
| **Dark Mode** | Full dark mode support. Non-negotiable for the target audience. | 🟢 Must Have |
| **Newest First** | Tools sorted by date added by default. | 🟢 Must Have |

### 5.2 Should Have — Post Launch

| Feature | Description | Priority |
|---|---|---|
| **Tool of the Day** | One tool spotlighted daily. Push notification sent. | 🟡 Should Have |
| **Collections / Packs** | Pre-built free tool stacks by use case — YouTubers, Students, Startups. | 🟡 Should Have |
| **Gone Paid Alert** | Users flag tools that went paid. Admin gets notified. Tool removed. | 🟡 Should Have |
| **Trending This Week** | Most saved tools in the last 7 days. | 🟡 Should Have |
| **Onboarding Flow** | 2 questions on first open to personalize home screen. | 🟡 Should Have |
| **Offline Mode** | Saved stack accessible without internet. | 🟡 Should Have |
| **Was This Free? Rating** | One tap thumbs up/down on each tool. Strengthens verification. | 🟡 Should Have |
| **Share a Tool** | Share tool page via link or WhatsApp. | 🟡 Should Have |

### 5.3 Future Consideration

| Feature | Description | Priority |
|---|---|---|
| **Affiliate Upgrades** | When free tier is not enough, suggest best paid upgrade. Earn commission. | ⚪ Nice to Have |
| **Web Version** | Browser version for shareable tool links. | ⚪ Nice to Have |
| **GitHub Login** | Additional login option for developer-heavy users. | ⚪ Nice to Have |
| **Multi-language** | Expand beyond English after establishing core user base. | ⚪ Nice to Have |

---

## 6. Screen Flow

### 6.1 Home Screen
- Big app name and logo at top
- Search bar — full width
- Category dropdown — right aligned
- Tool cards list — newest first
- Each card: logo, name, description, Verified Free badge, Visit Tool button

### 6.2 Tool Detail Page
- Large logo and tool name
- Verified Free badge
- Full description
- What is free section — bullet list
- What is NOT free section — honest limitations
- Best for — user types
- Visit Tool button
- Save to Stack button
- Similar free tools section
- Gone Paid? flag button

### 6.3 My Stack
- All tools the user has saved
- Remove from stack option
- Organized by category

### 6.4 Submit a Tool
- Paste URL — everything auto-fetches in 2 seconds
- User confirms or edits name, description, logo
- User selects category
- User describes free tier in detail
- Submit button — goes to pending queue

### 6.5 Admin Panel (Admin Only)
- Pending tab — all submissions awaiting review
- Each submission: logo, name, description, Visit Site button, Approve / Reject buttons
- Approved tab — all live tools with remove option
- Rejected tab — history with reasons
- Push notification on new submission

---

## 7. Technical Stack

| Component | Technology |
|---|---|
| **Language** | Kotlin |
| **Platform** | Android |
| **Authentication** | Firebase Auth — Google Sign In |
| **Database** | Cloud Firestore — 3 collections |
| **File Storage** | Firebase Storage — logos and images |
| **Push Notifications** | Firebase Cloud Messaging (FCM) |
| **Crash Reporting** | Firebase Crashlytics |
| **Analytics** | Firebase Analytics |
| **URL Fetching** | OkHttp + Jsoup — auto-fetch meta from submitted URLs |
| **UI Design Tool** | Stitch — generate UI screens |

---

## 8. Database Structure

### 8.1 Tools Collection

| Field | Description |
|---|---|
| `id` | Unique tool identifier |
| `name` | Tool name e.g. Canva |
| `description` | 2-3 line description |
| `logoUrl` | Auto-fetched from submitted URL |
| `websiteUrl` | Tool website link |
| `category` | Single category tag |
| `isVerified` | true / false — set by admin |
| `isFree` | Always true — this is the whole brand |
| `status` | live / pending / rejected / removed |
| `saves` | Number of users who saved this tool |
| `dateAdded` | Timestamp |
| `submittedBy` | userId or admin |
| `whatsFree` | Bullet list of free features |
| `whatsNotFree` | Honest list of paid limitations |
| `bestFor` | User types this tool suits |

### 8.2 Users Collection

| Field | Description |
|---|---|
| `id` | Firebase Auth UID |
| `name` | From Google account |
| `email` | From Google account |
| `photoUrl` | From Google account |
| `dateJoined` | Timestamp |
| `savedTools` | Array of tool IDs |
| `submittedTools` | Array of tool IDs |
| `isAdmin` | true for admin account only |
| `submissionsToday` | Rate limiting — max 3 per day |
| `lastSubmissionDate` | Date of last submission |

### 8.3 Submissions Collection

| Field | Description |
|---|---|
| `id` | Unique submission identifier |
| `submittedBy` | userId |
| `websiteUrl` | URL submitted by developer |
| `name` | Auto-fetched tool name |
| `description` | Auto-fetched description |
| `logoUrl` | Auto-fetched logo |
| `category` | Selected by developer |
| `whatsFree` | Written by developer |
| `status` | pending / approved / rejected |
| `dateSubmitted` | Timestamp |
| `reviewedBy` | Admin userId |
| `reviewNote` | Rejection reason if applicable |

---

## 9. Security

### 9.1 Firebase Security Rules
- **Tools collection** — anyone logged in can read, only admin can write or delete
- **Submissions** — logged in users can create, users can only read their own, admin reads all
- **Users** — each user can only read and write their own data

### 9.2 Additional Security
- Rate limiting — maximum 3 submissions per user per day
- URL validation — check before auto-fetching from submitted links
- Input sanitization — strip HTML and scripts from fetched content
- Character limits — tool name 50 chars max, description 300 chars max
- `google-services.json` added to `.gitignore` — never pushed to GitHub
- `isAdmin` field only editable directly in Firestore — never exposed in app code

---

## 10. Growth Strategy

### Pre-Launch
- Manually add 50-100 verified free tools before launch
- Test every tool personally — verify free tier works
- Set up all Firebase services and security rules
- Set up backup export before hitting 100 users

### Launch
- Post on Reddit — r/SideProject, r/artificial, r/androidapps
- Post on Product Hunt
- Share on Twitter / X with demo video

### Organic Growth Engine
- Developers submit their tools — they bring their own audience
- Developers display *Verified by OnlyFreeAI* badge on their sites
- Users share tool cards on social media
- Tool of the Day push notification brings users back daily
- Going Paid alerts make the app indispensable

### Monetization (Later)
- Affiliate links — when free tier is not enough, suggest best paid upgrade
- Featured listings — developers pay to be featured
- Newsletter sponsorships — weekly free tools digest

---

## 11. Success Metrics

| Milestone | Target |
|---|---|
| **Month 1** | 200+ users, 100+ tools listed, 10+ developer submissions |
| **Month 3** | 1,000+ users, 300+ tools, 50+ developer submissions |
| **Month 6** | 5,000+ users, 500+ tools, featured on Product Hunt |
| **Year 1** | 20,000+ users, first affiliate revenue, web version launched |

---

## 12. Out of Scope for v1.0

- Comments and reviews — too much to moderate at launch
- Social following between users
- Gamification or points system
- Web version — mobile first
- Multiple languages — English first
- CI/CD pipeline — manual Play Store uploads for now
- Docker containers — Firebase handles all infrastructure
- Database backup — set up before 100 users, not before launch

---

> *OnlyFreeAI • PRD v1.0 • Confidential*
