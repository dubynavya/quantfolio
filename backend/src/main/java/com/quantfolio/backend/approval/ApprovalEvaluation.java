package com.quantfolio.backend.approval;

import java.util.List;

public record ApprovalEvaluation(int requiredLevel, boolean autoApproved, List<String> reasons) {}
