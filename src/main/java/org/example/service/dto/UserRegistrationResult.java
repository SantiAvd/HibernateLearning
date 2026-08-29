package org.example.service.dto;

import org.example.entity.User;

public record UserRegistrationResult(
        User user,
        boolean isNew
) { }