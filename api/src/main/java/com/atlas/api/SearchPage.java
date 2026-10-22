package com.atlas.api;

import java.util.List;

public record SearchPage(List<Product> items, long total, int page, int size) {
}
