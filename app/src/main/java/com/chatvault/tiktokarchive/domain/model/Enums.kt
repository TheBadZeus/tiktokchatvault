package com.chatvault.tiktokarchive.domain.model

/** How a record entered the archive. Never guessed: it is set by the code path that saved the record. */
enum class CaptureMethod { DEMO, OFFICIAL_API, DATA_EXPORT_IMPORT, NOTIFICATION }

/** UNVERIFIED = the hash has not been re-checked yet. It is never set to INTACT without actually recomputing. */
enum class VerificationStatus { UNVERIFIED, INTACT, MODIFIED }

enum class Direction { INCOMING, OUTGOING, UNKNOWN }

enum class MessageType { TEXT, IMAGE, VIDEO, ATTACHMENT, UNKNOWN }

enum class AttachmentStatus { AVAILABLE, MISSING, NOT_CAPTURED }

enum class EventType { DEMO_LOAD, IMPORT, NOTIFICATION, VERIFY, EXPORT, DELETE, TAMPER_TEST }

enum class ThemeMode { SYSTEM, LIGHT, DARK }
