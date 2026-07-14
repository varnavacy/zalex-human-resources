package com.kyriakos.compose.project.demo.zalexhumanresources.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address (
        String firstLine,
        String secondLine,
        String postCode,
        String city,
        String Country
) {
}
