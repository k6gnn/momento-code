# Implementation Notes

## Prototype scope
The implementation follows the supplied prototype plan and required flows: authentication, capsule creation, nearby map discovery, proximity unlock, points/profile, discovery history, dropped capsules, expiry, and deletion.

## Security choices
- Firebase ID tokens are validated by the backend.
- S3 remains private and media is returned only as short-lived signed URLs after unlock authorization.
- Self-discovery is blocked.
- Discoveries are unique per user and capsule.
- Opaque notification payloads are assumed.

## Practical prototype trade-offs
- The mobile app sends media metadata and the backend currently uploads placeholder bytes; switch this to multipart upload or direct signed PUT upload for production.
- The database duplicate-coordinate restriction is approximated by rounded coordinates plus same-day uniqueness; if you need exact rolling 24-hour enforcement, add a trigger.
- Notification service is scaffolded, not fully wired.
- Google and Apple sign-in are not yet added on the mobile side; email/password is first-class.

## Best production upgrade path
1. Replace placeholder media upload with presigned PUT upload from app to S3.
2. Add FCM token registration and actual send calls.
3. Add rolling 24-hour duplicate rule via trigger.
4. Add anti-spoofing, rate limits, audit logs, and DTO validation hardening.
5. Add account deletion and data export flows.
