import React, { useEffect } from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import axios from 'axios';
import axiosInstance from '../axiosInstance';

const KakaoPaySuccess = () => {
  const location = useLocation();
  const history = useHistory();

  useEffect(() => {
    const urlParams = new URLSearchParams(location.search);
    const token = urlParams.get('token');
    const pgToken = urlParams.get('pg_token');
    const orderId = location.pathname.split('/')[2];

    if (!token || !pgToken || !orderId) {
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

        const response = await customAxios.get(`/payment/${orderId}/success?pg_token=${pgToken}`);

        if (response.status === 200) {
          alert('결제가 성공적으로 완료되었습니다.');
          history.push('/mypage/order-status'); // 주문 현황 페이지로 이동
        } else {
          throw new Error('결제 실패');
        }
      } catch (error) {
        console.error('결제 완료 처리 중 오류가 발생했습니다.', error);
        alert('결제 완료 처리 중 오류가 발생했습니다.');
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

export default KakaoPaySuccess;