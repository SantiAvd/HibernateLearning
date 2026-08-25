package org.example.service;

import org.example.entity.User;

public record UserRegistrationResult(
        User user,
        boolean isNew
) { }