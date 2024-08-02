import React, { useState } from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import './MyPage.css';

const AddressForm = () => {
    const [address, setAddress] = useState('');
    const history = useHistory();
    const location = useLocation();
    const { amount, itemName, orderId } = location.state || { amount: 0, itemName: '', orderId: null };

    const handleAddressChange = (event) => {
        setAddress(event.target.value);
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

            await axiosInstance.post('/orders/cart/create', orderRequest);
            history.push(`/checkout/payment-method/${orderId}`, { amount, itemName, orderId });
        } catch (error) {
            console.error('주문 생성 중 오류가 발생했습니다.', error);
            alert('주문 생성 중 오류가 발생했습니다.');
        }
    };

    return (
        <div>
            <br></br>
            <br></br>
            <div className="address-form">
                <h2>주소 입력</h2>
                <div className="form-group">
                    <label htmlFor="address" className="form-label">배송 주소:</label>
                    <input
                        type="text"
                        id="address"
                        value={address}
                        onChange={handleAddressChange}
                        className="form-input"
                        placeholder="배송 주소를 입력하세요"
                    />
                </div>
                <button onClick={handleCreateOrder} className="button next-btn">다음</button>
            </div>
        </div>
    );
};

export default AddressForm;