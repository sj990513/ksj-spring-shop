import React, { useState, useEffect, useContext } from 'react';
import { useHistory, useLocation, Switch, Route, Redirect } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import { AuthContext } from '../context/AuthContext';
import PersonalInfo from './PersonalInfo';
import OrderStatus from './OrderStatus';
import Cart from './Cart';
import './MyPage.css';

const MyPage = () => {
  const [userData, setUserData] = useState(null);
  const { isLoggedIn, setIsLoggedIn } = useContext(AuthContext);
  const history = useHistory();
  const location = useLocation();

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

  return (
    <div className="mypage-container">
      <div className="sidebar">
        <ul>
          <li className={location.pathname === '/mypage/personal-info' ? 'active' : ''} onClick={() => history.push('/mypage/personal-info')}>개인정보</li>
          <li className={location.pathname === '/mypage/order-status' ? 'active' : ''} onClick={() => history.push('/mypage/order-status')}>주문현황</li>
          <li className={location.pathname === '/mypage/cart' ? 'active' : ''} onClick={() => history.push('/mypage/cart')}>장바구니</li>
        </ul>
      </div>
      <div className="content">
        <Switch>
          <Route path="/mypage/personal-info">
            <PersonalInfo userData={userData} setUserData={setUserData} />
          </Route>
          <Route path="/mypage/order-status">
            <OrderStatus />
          </Route>
          <Route path="/mypage/cart">
            <Cart />
          </Route>
          <Redirect from="/mypage" to="/mypage/personal-info" />
        </Switch>
      </div>
    </div>
  );
};

export default MyPage;