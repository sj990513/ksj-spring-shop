import React, { useEffect, useContext } from 'react';
import { BrowserRouter as Router, Route, Switch, useHistory, Redirect } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import { AuthContext } from '../context/AuthContext';
import AddressForm from './AddressForm';
import PaymentMethod from './PaymentMethod';
import KakaoPaySuccess from './KakaoPaySuccess';
import './css/MyPage.css';

const Checkout = () => {
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
    }
  }, [isLoggedIn, setIsLoggedIn, history]);

  return (
    <Router>
      <Switch>
        <Route path="/checkout/address" component={AddressForm} />
        <Route path="/checkout/payment-method/:orderId" component={PaymentMethod} />
        <Route path="/payment/success" component={KakaoPaySuccess} />
        <Redirect from="/checkout" to="/checkout/address" />
      </Switch>
    </Router>
  );
};

export default Checkout;