import {Review} from '../../models/Review';
import ReviewItem from './ReviewItem';

interface ReviewListProps {
  reviews: Review[];
}

const ReviewList = ({reviews}: ReviewListProps) => {
  if (!reviews || reviews.length === 0) {
    return (
      <div className="bg-white border border-gray-200 rounded-lg p-6 mt-8">
        <h3 className="text-xl font-semibold text-gray-900 mb-2">Reviews</h3>
        <p className="text-gray-600 text-base">No reviews yet.</p>
      </div>
    );
  }

  return (
    <div className="bg-white border border-gray-200 rounded-lg p-6 mt-8">
      <h3 className="text-xl font-semibold text-gray-900 mb-5">Reviews ({reviews.length})</h3>

      <ul className="space-y-8">
        {[...reviews]
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
          .map(review => (
            <ReviewItem key={review.id} review={review}/>
          ))}
      </ul>
    </div>
  );
};

export default ReviewList;
