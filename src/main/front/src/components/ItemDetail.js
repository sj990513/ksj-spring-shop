import React, { useState, useEffect, useContext } from 'react';
import axiosInstance from '../axiosInstance';
import { useHistory, useParams } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import Reviews from './Reviews';
import Qna from './Qna';
import './ItemDetail.css';

const ItemDetail = () => {
  const [itemDetail, setItemDetail] = useState(null);
  const [activeTab, setActiveTab] = useState('reviews');

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

  if (!itemDetail) {
    return <div>Loading...</div>;
  }

  const { itemDto } = itemDetail;

  return (
    <div className="item-detail-container">
      <img src={itemDto.imageUrl} alt={itemDto.itemname} className="item-detail-image" />
      <h2>{itemDto.itemname}</h2>
      <p>{itemDto.description}</p>
      <p><strong>가격:</strong> {itemDto.price}원</p>
      <p><strong>남은수량:</strong> {itemDto.stock}개</p>
      <br></br>
      <br></br>
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