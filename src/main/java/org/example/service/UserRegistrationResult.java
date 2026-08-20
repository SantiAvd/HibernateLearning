package org.example.service;

import org.example.model.User;

public record UserRegistrationResult(
        User user,
        boolean isNew
) { }