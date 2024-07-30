import React, { useState, useEffect, useContext } from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import { AuthContext } from '../context/AuthContext';
import './MyPage.css';

const Checkout = () => {
  const [address, setAddress] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('');
  const [currentStep, setCurrentStep] = useState(1); // 현재 단계를 관리하는 상태 변수
  const { isLoggedIn, setIsLoggedIn } = useContext(AuthContext);
  const history = useHistory();
  const location = useLocation();
  const { amount, itemName, orderId } = location.state || { amount: 0, itemName: '', orderId: null };
  const [userData, setUserData] = useState(null);

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

  const handleAddressChange = (event) => {
    setAddress(event.target.value);
  };

  const handlePaymentMethodChange = (event) => {
    setPaymentMethod(event.target.value);
  };

  const handleCreateOrder = async () => {
    if (!address) {
      alert('배송 주소를 입력하세요.');
      return;
    }

    try {
      const orderRequest = {
        deliveryDto: {
          address,
        },
        paymentDto: {
          amount,
          itemName
        }
      };

      const orderResponse = await axiosInstance.post('/orders/cart/create', orderRequest);
      setCurrentStep(2); // 다음 단계로 이동
    } catch (error) {
      console.error('주문 생성 중 오류가 발생했습니다.', error);
      alert('주문 생성 중 오류가 발생했습니다.');
    }
  };

  const handlePayment = async () => {
    if (!paymentMethod) {
      alert('결제 방법을 선택하세요.');
      return;
    }
  
    try {
      const paymentRequest = {
        paymentDto: { // paymentDto를 포함시킴
          paymentMethod,
          amount,
          itemName
        }
      };
  
      if (paymentMethod === 'kakao_pay') {
        const kakaoPayResponse = await axiosInstance.post(`/payment/ready/${orderId}`, paymentRequest);
        if (kakaoPayResponse.data && kakaoPayResponse.data.next_redirect_pc_url) {
          window.location.href = kakaoPayResponse.data.next_redirect_pc_url;
        } else {
          alert('카카오페이 결제 준비 중 오류가 발생했습니다.');
        }
      } else {
        alert('결제가 완료되었습니다.');
        // 결제 완료 후 처리
      }
    } catch (error) {
      console.error('결제 준비 중 오류가 발생했습니다.', error);
      alert('결제 준비 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="checkout">
      {currentStep === 1 && (
        <>
          <h2>주소 입력</h2>
          <div className="form-group">
            <label>배송 주소:</label>
            <input type="text" value={address} onChange={handleAddressChange} className="form-control" />
          </div>
          <button onClick={handleCreateOrder} className="button next-btn">다음</button>
        </>
      )}
      {currentStep === 2 && (
        <>
          <h2>결제 방법 선택</h2>
          <div className="form-group">
            <label>결제 방법:</label>
            <select value={paymentMethod} onChange={handlePaymentMethodChange} className="form-control">
              <option value="">선택하세요</option>
              <option value="virtual_account">가상계좌</option>
              <option value="kakao_pay">카카오페이</option>
            </select>
          </div>
          <button onClick={handlePayment} className="button next-btn">결제</button>
        </>
      )}
    </div>
  );
};

export default Checkout;

