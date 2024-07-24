import React, { useState, useEffect, useContext } from 'react';
import axiosInstance from '../axiosInstance';
import { useHistory, useParams } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './ItemDetail.css';

const ItemDetail = () => {
  const [itemDetail, setItemDetail] = useState(null);
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const { isLoggedIn, setIsLoggedIn } = useContext(AuthContext);
  const history = useHistory();
  const { itemId } = useParams();

  useEffect(() => {
    const checkAuth = async () => {
      try {
        await axiosInstance.get('/check-auth');
        setIsLoggedIn(true);
      } catch (error) {
        setIsLoggedIn(false);
        alert("로그인해주세요");
        history.push('/login');
      }
    };

    const fetchItemDetail = async () => {
      try {
        const response = await axiosInstance.get(`/items/${itemId}`);
        setItemDetail(response.data);
      } catch (error) {
        console.error('There was an error fetching the item details!', error);
      }
    };

    if (!isLoggedIn) {
      checkAuth().then(fetchItemDetail);
    } else {
      fetchItemDetail();
    }
  }, [isLoggedIn, history, itemId, setIsLoggedIn]);

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
      // Refresh item details to show the new review
      const response = await axiosInstance.get(`/items/${itemId}`);
      setItemDetail(response.data);
    } catch (error) {
      console.error('There was an error adding the review!', error);
      alert('리뷰 추가에 실패했습니다.');
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
        // Refresh item details to show the updated reviews
        const newResponse = await axiosInstance.get(`/items/${itemId}`);
        setItemDetail(newResponse.data);
      } else {
        alert(response.data);
      }
    } catch (error) {
      console.error('리뷰 삭제에 실패했습니다.', error);
      alert('리뷰 삭제는 본인만 가능합니다.');
    }
  };

  if (!itemDetail) {
    return <div>Loading...</div>;
  }

  const { itemDto, reviewListDto } = itemDetail;

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
    <div className="item-detail-container">
      <img src={itemDto.imageUrl} alt={itemDto.itemname} className="item-detail-image" />
      <h2>{itemDto.itemname}</h2>
      <p>{itemDto.description}</p>
      <p><strong>가격:</strong> {itemDto.price}원</p>
      <p><strong>남은수량:</strong> {itemDto.stock}개</p>
      <br />
      <div className="reviews">
        <h3>상품리뷰</h3>
        <p className="review-notice">
          리뷰는 배송완료된 제품만 작성가능하고 리뷰 수정 및 삭제는 본인만 가능합니다.
        </p>
        {reviewListDto && reviewListDto.length > 0 ? (
          reviewListDto.map((review, index) => (
            <div key={index} className="review">
              <p><strong>별점:</strong> {renderStars(review.rating)}</p>
              <p><strong>작성자:</strong> {review.userNickname}</p>
              <p><strong>리뷰내용:</strong> {review.comment}</p>
              <p><strong>리뷰개시날짜:</strong> {new Date(review.createDate).toLocaleDateString()}</p>
              {review.userId === itemDetail.userId && ( // Check if the review belongs to the logged-in user
                <button onClick={() => handleReviewDelete(review.id)} className="delete-review-btn">
                  삭제
                </button>
              )}
            </div>
          ))
        ) : (
          <p>아직 리뷰가 존재하지 않습니다.</p>
        )}
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

export default ItemDetail;