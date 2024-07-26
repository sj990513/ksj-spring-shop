import React, { useState, useEffect } from 'react';
import axiosInstance from '../axiosInstance';
import './ItemDetail.css';

const Reviews = ({ itemId, itemDetail }) => {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [reviewList, setReviewList] = useState([]);
  const [reviewPage, setReviewPage] = useState(1);
  const [reviewTotalPages, setReviewTotalPages] = useState(1);

  useEffect(() => {
    fetchReviewList(1); // Fetch first page of reviews
  }, []);

  useEffect(() => {
    fetchReviewList(reviewPage);
  }, [reviewPage]);

  const fetchReviewList = async (page) => {
    try {
      const response = await axiosInstance.get(`/items/${itemId}/review`, {
        params: { page: page },
      });
      setReviewList(response.data.content);
      setReviewTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('There was an error fetching the reviews!', error);
    }
  };

  const handleRatingChange = (e) => {
    setRating(Number(e.target.value));
  };

  const handleCommentChange = (e) => {
    setComment(e.target.value);
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0 || comment.trim() === '') {
      alert('별점과 리뷰 내용을 입력해주세요.');
      return;
    }

    try {
      await axiosInstance.post(`/items/${itemId}/review/add-review`, {
        rating,
        comment,
      });
      alert('리뷰가 추가되었습니다.');
      setRating(0);
      setComment('');
      // Refresh reviews
      fetchReviewList(reviewPage);
    } catch (error) {
      console.error('There was an error adding the review!', error);
      alert('배송 완료된 제품만 리뷰 작성이 가능합니다.');
    }
  };

  const handleReviewDelete = async (reviewId) => {
    if (!window.confirm('정말로 리뷰를 삭제하시겠습니까?')) {
      return;
    }
    try {
      const response = await axiosInstance.delete(`/items/${itemId}/review/${reviewId}/delete-review`);
      if (response.data === '삭제성공') {
        alert('리뷰가 삭제되었습니다.');
        // Refresh reviews
        fetchReviewList(reviewPage);
      } else {
        alert(response.data);
      }
    } catch (error) {
      console.error('리뷰 삭제에 실패했습니다.', error);
      alert('리뷰 삭제는 본인 및 관리자만 가능합니다.');
    }
  };

  const renderStars = (rating) => {
    const stars = [];
    for (let i = 0; i < 5; i++) {
      stars.push(
        <span key={i} className={i < rating ? 'filled-star' : 'empty-star'}>
          ★
        </span>
      );
    }
    return stars;
  };

  return (
    <div className="reviews">
      <h3>상품 리뷰</h3>
      <p className="review-notice">
        리뷰는 배송완료된 제품만 작성가능하고 리뷰 수정 및 삭제는 본인만 가능합니다.
      </p>
      {reviewList && reviewList.length > 0 ? (
        reviewList.map((review, index) => (
          <div key={index} className="review">
            <p><strong>별점:</strong> {renderStars(review.rating)}</p>
            <p><strong>작성자:</strong> {review.userNickname}</p>
            <p><strong>리뷰내용:</strong> {review.comment}</p>
            <p><strong>리뷰개시날짜:</strong> {new Date(review.createDate).toLocaleDateString()}</p>
            {review.userId === itemDetail.userId && (
              <button onClick={() => handleReviewDelete(review.id)} className="delete-review-btn">
                삭제
              </button>
            )}
          </div>
        ))
      ) : (
        <p>아직 리뷰가 존재하지 않습니다.</p>
      )}

      <div className="pagination">
        <button
          onClick={() => setReviewPage(reviewPage - 1)}
          disabled={reviewPage === 1}
        >
          이전
        </button>
        <span>{reviewPage} / {reviewTotalPages}</span>
        <button
          onClick={() => setReviewPage(reviewPage + 1)}
          disabled={reviewPage === reviewTotalPages}
        >
          다음
        </button>
      </div>

      <div className="add-review">
        <h3>리뷰 추가</h3>
        <form onSubmit={handleReviewSubmit} className="review-form">
          <div className="form-group">
            <label>별점:</label>
            <select value={rating} onChange={handleRatingChange} required>
              <option value={0}>선택</option>
              <option value={1}>1</option>
              <option value={2}>2</option>
              <option value={3}>3</option>
              <option value={4}>4</option>
              <option value={5}>5</option>
            </select>
          </div>
          <div className="form-group">
            <label>리뷰내용:</label>
            <textarea
              value={comment}
              onChange={handleCommentChange}
              rows="4"
              required
            />
          </div>
          <button type="submit">리뷰 추가</button>
        </form>
      </div>
    </div>
  );
};

export default Reviews;