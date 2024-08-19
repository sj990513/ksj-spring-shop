import React, { useState } from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import './css/MyPage.css';

const PaymentMethod = () => {
    const [paymentMethod, setPaymentMethod] = useState('');
    const history = useHistory();
    const location = useLocation();
    const { amount, itemName, orderId } = location.state || { amount: 0, itemName: '', orderId: null };

    const handlePaymentMethodChange = (event) => {
        setPaymentMethod(event.target.value);
    };

    const handlePayment = async () => {
        if (!paymentMethod) {
            alert('결제 방법을 선택하세요.');
            return;
        }

        try {
            const paymentRequest = {
                paymentDto: {
                    paymentMethod,
                    amount,
                    itemName
                }
            };

            if (paymentMethod === 'KAKAO_PAY') {
                const kakaoPayResponse = await axiosInstance.post(`/payment/ready/${orderId}`, paymentRequest);
                if (kakaoPayResponse.data && kakaoPayResponse.data.next_redirect_pc_url) {
                    window.location.href = kakaoPayResponse.data.next_redirect_pc_url;
                } else {
                    alert('카카오페이 결제 준비 중 오류가 발생했습니다.');
                }
            } else if (paymentMethod === 'VIRTUAL_ACCOUNT') {
                alert("현재 준비중입니다.");
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
        <div>
            <br></br>
            <br></br>
            <div className="payment-method-form">
                <h2>결제 방법 선택</h2>
                <div className="form-group">
                    <label htmlFor="payment-method" className="form-label">결제 방법:</label>
                    <select
                        id="payment-method"
                        value={paymentMethod}
                        onChange={handlePaymentMethodChange}
                        className="form-input"
                    >
                        <option value="">선택하세요</option>
                        <option value="VIRTUAL_ACCOUNT">가상계좌 (준비중)</option>
                        <option value="KAKAO_PAY">카카오페이</option>
                    </select>
                </div>
                <button onClick={handlePayment} className="button next-btn">결제</button>
            </div>
        </div>
    );
};

export default PaymentMethod;