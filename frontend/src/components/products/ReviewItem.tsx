import {Star} from 'lucide-react';
import {Review} from '../../models/Review';

interface ReviewItemProps {
  review: Review;
}

const clamp = (v: number) => Math.max(0, Math.min(5, Math.round(v)));

const ReviewItem = ({review}: ReviewItemProps) => {
  const rating = clamp(review.rating);
  const date = review.createdAt ? new Date(review.createdAt).toLocaleDateString() : '';

  return (
    <li className="border-b last:border-b-0 py-6">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-3">
          <div
            className="font-medium text-gray-900 text-base">{review.userId ? `${review.userDto?.firstName} ${review.userDto?.lastName}` : 'User'}</div>
          <div className="text-sm text-gray-500">{date}</div>
        </div>
        <div className="flex items-center gap-0.5">
          {Array.from({length: 5}).map((_, i) => (
            <Star
              key={i}
              size={20}
              className={i < rating ? 'text-yellow-400 fill-current' : 'text-gray-300'}
            />
          ))}
        </div>
      </div>

      {review.comment && (
        <p className="text-gray-700 text-base whitespace-pre-wrap">{review.comment}</p>
      )}
    </li>
  );
};

export default ReviewItem;
