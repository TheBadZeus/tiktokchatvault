# TikTok Chat Vault

An **independent** Android app that keeps a private, on-device archive of direct messages from *your own* TikTok
account, with a TikTok-style inbox/conversation UI. It is not made by, affiliated with, or a replacement for TikTok.

**It can only archive what it actually receives.** It cannot recover messages it never captured, and it never reads
TikTok's private network traffic, passwords, cookies or tokens.

## Status

| Milestone | State |
|---|---|
| 1. Runnable UI (inbox, conversation, evidence details, settings, themes, search) | Written. **Not yet compiled or run** (see "First build") |
| 2. Local archive (Room entities, DAOs, paging, hashing, audit log) | Written, same caveat |
| 3. Import of TikTok's data download | Not started. Needs a real sample file first; the parser must not be written from guesses |
| 4. Official API investigation + notification capture + WorkManager | Not started |
| 5. Encryption, app lock, JSON/CSV/HTML/ZIP export with manifest | Not started |
| 6. Device testing, signed release APK | Not started |

Until Milestone 5: the database is **not encrypted by the app** (it sits in Android's private app storage), and the
app-lock, export and import entries in Settings are visible but disabled.

## Build the APK from your phone (no PC)

GitHub builds the APK for you in the cloud (free for private repos within the monthly allowance).

1. Create a free GitHub account if you don't have one.
2. In Termux:
   ```
   pkg update && pkg install -y git unzip gh
   termux-setup-storage
   cd ~ && unzip ~/storage/downloads/TikTokChatVault.zip && cd TikTokChatVault
   gh auth login -s repo,workflow      # follow the on-screen device-code steps in your browser
   git init -b main && git add -A && git commit -m "Initial commit"
   gh repo create tiktok-chat-vault --private --source=. --push
   ```
3. Open the repo on github.com → **Actions** → wait for "Build APK" to turn green (about 5–10 minutes the first time).
4. Open **Releases** → latest "Debug build N" → tap `app-debug.apk` → install (Android will ask you to allow
   installs from your browser).

The debug APK is signed with a debug key: fine for your own phone, not for distribution.

## First build

This code was written without access to an Android SDK, so it has not been compiled. If the Actions run fails, open
the failed run, copy the red error lines, and send them back; they will normally be a small fix.

## Architecture

MVVM + repository, hand-wired container (`ChatVaultApp.kt`).

```
ui (Compose screens, ViewModels)
   │  StateFlow / PagingData
data/repository/ArchiveRepository ── single place that writes records, seals hashes, logs events
   │
data/dao + data/database (Room)       evidence/IntegrityHasher (SHA-256)
   ▲
capture/CaptureSource  ← adapters: officialapi (placeholder), importarchive (M3), notification (M4)
```

Every way of getting messages in implements `CaptureSource`, so the archive never depends on where data came from.

## Data model (Room, schema v1)

`contacts`, `conversations`, `archived_messages`, `message_attachments`, `capture_events` (append-only audit log),
`evidence_exports`, `app_settings`. Schema JSON is exported to `app/schemas/`. Migrations go in
`data/database/Migrations.kt`; destructive migration is never used.

Honest-data rules enforced by the model:
- `originalTimestamp` is null when the source didn't give one; the UI then labels the time as "captured", never as sent.
- `sortTimestamp` exists only for ordering and is never shown as a fact.
- Verification starts as `UNVERIFIED`; only "Verify now" recomputes the hash and may set `INTACT` or `MODIFIED`.
- Demo records use `captureMethod = DEMO` and are labelled DEMO everywhere.

## What the hash does and doesn't do

Hashed: id fields, sender, direction, type, content, original and capture timestamps, capture method and source.
Not hashed: the sort key, "saved" star, verification fields. A matching SHA-256 means the record is unchanged since
this app saved it. It does **not** prove the message is authentic or who sent it, and nobody should treat a local
record as automatically admissible evidence. Someone with database access could change a record and its hash.
(Talk to a lawyer about preserving evidence for any legal matter.)

## Privacy / security (current)

- No internet permission in the manifest; the build cannot transmit data.
- Cloud backup and device transfer are excluded (`data_extraction_rules.xml`, `allowBackup=false`).
- Analytics, ads, third-party SDKs: none.
- **Not yet encrypted.** Planned for M5: SQLCipher with a key wrapped by Android Keystore, so search keeps working.
  Metadata such as file size, row counts and file timestamps will remain visible to anyone with file access.

## Versions

| Item | Version |
|---|---|
| JDK | 17 |
| Gradle | 8.9 (installed by the workflow; no wrapper jar is committed) |
| Android Gradle Plugin | 8.5.2 |
| Kotlin / Compose compiler plugin | 2.0.20 |
| KSP | 2.0.20-1.0.25 |
| compileSdk / targetSdk / minSdk | 34 / 34 / 28 |
| Compose BOM | 2024.09.00 |
| Room | 2.6.1 |
| Paging | 3.3.2 |
| Navigation Compose | 2.8.0 |
| Lifecycle | 2.8.5 |
| DataStore | 1.1.1 |

## Using Android Studio later (optional)

Open the folder, let it sync, press Run. The Gradle wrapper isn't included; run `gradle wrapper --gradle-version 8.9`
once, or just use Studio's bundled Gradle.

## Project layout

See `app/src/main/java/com/chatvault/tiktokarchive/`: `data/`, `domain/`, `capture/`, `evidence/`, `ui/`,
`navigation/`, plus empty `security/`, `workers/`, `data/importexport/` reserved for later milestones.

## Legal

Use only with your own account and your own device. Respect TikTok's terms and the privacy of the people you chat
with. This README is not legal advice.
# tiktokchatvault
