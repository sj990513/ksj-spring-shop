import React, { useEffect } from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import axios from 'axios';

const KakaoPayCancel = () => {
  const location = useLocation();
  const history = useHistory();

  useEffect(() => {
    const urlParams = new URLSearchParams(location.search);
    const token = urlParams.get('token');
    const orderId = location.pathname.split('/')[2];

    if (!token || !orderId) {
      alert('필요한 정보가 부족합니다.');
      return;
    }

    const completeKakaoPayment = async () => {
      try {
        // 새로운 인스턴스를 만들어서 헤더에 token을 추가
        const customAxios = axios.create({
          baseURL: 'http://localhost:8080',
          headers: {
            'access': token,
          },
        });

        const response = await customAxios.get(`/payment/${orderId}/cancel`);

        if (response.status === 200) {
          const { amount, itemName } = response.data;
          alert('결제를 취소했습니다.');
          // 결제 취소 후 PaymentMethod 페이지로 이동
          history.push({
            pathname: `/checkout/payment-method/${orderId}`,
            state: { orderId, amount, itemName }
          });
        } else {
          throw new Error('오류');
        }
      } catch (error) {
        console.error('결제 처리 중 오류가 발생했습니다.', error);
        alert('결제 처리 중 오류가 발생했습니다.');
        history.push('/failure'); // 실패 페이지로 이동
      }
    };

    completeKakaoPayment();
  }, [location, history]);

  return (
    <div>
      <h2>결제 처리 중...</h2>
    </div>
  );
};

export default KakaoPayCancel;