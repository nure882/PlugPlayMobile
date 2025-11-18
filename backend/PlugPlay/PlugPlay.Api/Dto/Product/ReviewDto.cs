using PlugPlay.Domain.Entities;

namespace PlugPlay.Api.Dto.Product;

public record ReviewDto
{
    public int Id { get; set; }

    public int ProductId { get; set; }

    public int? UserId { get; set; }

    public int Rating { get; set; }

    public string Comment { get; set; }

    public UserDto UserDto { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime? UpdatedAt { get; set; }

    public static ReviewDto MapReview(Review review)
    {
        return new ReviewDto{
            Id = review.Id,
            ProductId = review.ProductId,
            UserId = review.UserId,
            Rating = review.Rating,
            Comment = review.Comment,
            UserDto = UserDto.MapUser(review.User),
            CreatedAt = review.CreatedAt,
            UpdatedAt = review.UpdatedAt};
    }
}
