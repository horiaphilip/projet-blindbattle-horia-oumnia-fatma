package fr.miage.blindbattle.dto;

import java.util.List;

public record PageView<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
