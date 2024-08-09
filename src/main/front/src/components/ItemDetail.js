import React, { useState, useEffect, useContext } from 'react';
import axiosInstance from '../axiosInstance';
import { useHistory, useParams, useLocation } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import Reviews from './Reviews';
import Qna from './Qna';
import './ItemDetail.css';

const ItemDetail = () => {
  const [itemDetail, setItemDetail] = useState(null);
  const [activeTab, setActiveTab] = useState('reviews');
  const [count, setCount] = useState(1);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  const { isLoggedIn, user } = useContext(AuthContext);
  const history = useHistory();
  const { itemId } = useParams();
  const location = useLocation();

  useEffect(() => {
    const checkAuth = async () => {
      try {
        await axiosInstance.get('/check-auth');
      } catch (error) {
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
  }, [isLoggedIn, history, itemId]);

  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const qnaId = queryParams.get('qnaId');
    if (qnaId) {
      setActiveTab('qna');
    }
  }, [location]);

  const handleAddToCart = async () => {
    try {
      const orderRequest = {
        orderItems: [
          {
            itemID: itemId,
            count: count
          }
        ]
      };
      await axiosInstance.post('/orders/cart/add-cart', orderRequest);
      alert('장바구니에 추가되었습니다.');
    } catch (error) {
      console.error('There was an error adding the item to the cart!', error);
      alert('장바구니에 추가하는데 오류가 발생했습니다.');
    }
  };

  const handleOrderNow = async () => {
    try {
      const orderRequest = {
        orderItems: [
          {
            itemID: itemId,
            count: count
          }
        ]
      };
      const totalAmount = itemDetail.itemDto.price * count;
      const itemName = itemDetail.itemDto.itemname;
      const response = await axiosInstance.post('/orders/order-now', orderRequest);
      const { id } = response.data;
      history.push('/checkout/address', { amount: totalAmount, itemName, orderId: id });
    } catch (error) {
      console.error('There was an error placing the order!', error);
      alert('즉시 주문에 실패했습니다.');
    }
  };

  const handleNextImage = () => {
    setCurrentImageIndex((prevIndex) => (prevIndex + 1) % itemImages.length);
  };

  const handlePrevImage = () => {
    setCurrentImageIndex((prevIndex) => (prevIndex - 1 + itemImages.length) % itemImages.length);
  };

  const handleEditItem = () => {
    history.push(`/admin/item-edit/${itemId}`);
  };

  const handleDeleteItem = async () => {
    const isConfirmed = window.confirm('정말로 이 아이템을 삭제하시겠습니까?');
    if (isConfirmed) {
      try {
        await axiosInstance.delete(`/admin/item-list/${itemId}/delete-item`);
        alert('아이템이 성공적으로 삭제되었습니다.');
        history.push('/item-list');
      } catch (error) {
        console.error('There was an error deleting the item!', error);
        alert('아이템 삭제에 실패했습니다.');
      }
    }
  };

  if (!itemDetail) {
    return <div>Loading...</div>;
  }

  const { itemDto } = itemDetail;
  const itemImages = itemDto.imageUrl.split(',');

  return (
    <div className="item-detail-container">
      <div className="image-carousel">
        <button className="carousel-arrow left-arrow" onClick={handlePrevImage}>&#10094;</button>
        <img src={itemImages[currentImageIndex]} alt={itemDto.itemname} className="item-detail-image" />
        <button className="carousel-arrow right-arrow" onClick={handleNextImage}>&#10095;</button>
      </div>
      <h2>{itemDto.itemname}</h2>
      <p>{itemDto.description}</p>
      <p><strong>가격:</strong> {itemDto.price}원</p>
      <p><strong>재고:</strong> {itemDto.stock}개</p>
      <br />
      <br />
      <div className="quantity-container">
        <label htmlFor="quantity">수량:</label>
        <input
          type="number"
          id="quantity"
          value={count}
          min="1"
          max={itemDto.stock}
          onChange={(e) => setCount(e.target.value)}
        />
      </div>
      <button className="add-to-cart-btn" onClick={handleAddToCart}>장바구니에 추가</button>
      <button className="order-now-btn" onClick={handleOrderNow}>즉시 주문</button>
      <br />
      <br />
      {user?.role === 'ROLE_ADMIN' && (
        <>
          <button className="edit-item-btn" onClick={handleEditItem}>상품 수정하기</button>
          <button className="delete-item-btn" onClick={handleDeleteItem}>상품 삭제하기</button>
        </>
      )}
      <br/>
      <br/>
      <br/>
      <div className="tab-container">
        <button className={`tab ${activeTab === 'reviews' ? 'active' : ''}`} onClick={() => setActiveTab('reviews')}>
          리뷰
        </button>
        <button className={`tab ${activeTab === 'qna' ? 'active' : ''}`} onClick={() => setActiveTab('qna')}>
          Q&A
        </button>
      </div>

      {activeTab === 'reviews' && <Reviews itemId={itemId} itemDetail={itemDetail} />}
      {activeTab === 'qna' && <Qna itemId={itemId} />}
    </div>
  );
};

export default ItemDetail;