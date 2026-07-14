package com.kyriakos.compose.project.demo.zalexhumanresources.sorting;

import lombok.Getter;

@Getter
public enum SortField {
    ISSUED_ON("issuedOn"),
    STATUS("status");

    private final String fieldName;

    SortField(String fieldName) { this.fieldName = fieldName; }

}
