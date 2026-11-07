package com.scc.model;

import java.sql.Timestamp;

public record ActivityLog(
        int id,
        int userId,
        String actionType,
        String description,
        Timestamp timestamp
) {}