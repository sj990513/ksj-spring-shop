import React, { useState, useEffect, useContext } from 'react';
import axiosInstance from '../axiosInstance';
import { useHistory } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import PersonalInfo from './PersonalInfo';
import OrderStatus from './OrderStatus';
import Cart from './Cart';
import './MyPage.css';

const MyPage = () => {
  const [userData, setUserData] = useState(null);
  const [activeTab, setActiveTab] = useState('personalInfo');

  const { isLoggedIn, setIsLoggedIn } = useContext(AuthContext);
  const history = useHistory();

  useEffect(() => {
    const checkAuth = async () => {
      try {
        await axiosInstance.get('/check-auth');
        setIsLoggedIn(true);
      } catch (error) {
        setIsLoggedIn(false);
        alert("로그인해주세요");
        history.push('/');
      }
    };

    if (!isLoggedIn) {
      checkAuth();
    } else {
      axiosInstance.get('/user-info/user')
        .then(response => {
          setUserData(response.data);
        })
        .catch(error => {
          console.error('There was an error fetching the user data!', error);
        });
    }
  }, [isLoggedIn, history, setIsLoggedIn]);

  if (!userData) {
    return <div>Loading...</div>;
  }

  const renderContent = () => {
    switch (activeTab) {
      case 'personalInfo':
        return <PersonalInfo userData={userData} setUserData={setUserData} />;
      case 'orderStatus':
        return <OrderStatus />;
      case 'cart':
        return <Cart />;
      default:
        return null;
    }
  };

  return (
    <div className="mypage-container">
      <div className="sidebar">
        <ul>
          <li className={activeTab === 'personalInfo' ? 'active' : ''} onClick={() => setActiveTab('personalInfo')}>개인정보</li>
          <li className={activeTab === 'orderStatus' ? 'active' : ''} onClick={() => setActiveTab('orderStatus')}>주문현황</li>
          <li className={activeTab === 'cart' ? 'active' : ''} onClick={() => setActiveTab('cart')}>장바구니</li>
        </ul>
      </div>
      <div className="content">
        {renderContent()}
      </div>
    </div>
  );
};

export default MyPage;