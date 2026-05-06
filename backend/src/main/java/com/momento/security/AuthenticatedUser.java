package com.momento.security;

import java.util.UUID;

public record AuthenticatedUser(UUID internalUserId, String firebaseUid, String email, String displayName) {}
