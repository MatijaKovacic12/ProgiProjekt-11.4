package touristAgency.dto;

import touristAgency.repositories.projection.TopItem;

public record TopItemResponse (
        Long id,
        String name,
        Long total) {

    public static TopItemResponse from(TopItem item) {
        return new TopItemResponse(item.getId(), item.getName(), item.getTotal());
    }
}
