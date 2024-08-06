import React, { useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import './MyPage.css';

const OrderDetail = ({ fetchOrders }) => {
  const { orderId } = useParams();
  const history = useHistory();
  const [order, setOrder] = useState(null);

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        const response = await axiosInstance.get(`/orders/${orderId}`);
        setOrder(response.data);
      } catch (error) {
        console.error('There was an error fetching the order detail!', error);
      }
    };
    fetchOrder();
  }, [orderId]);

  const getStatusLabel = (status) => {
    switch (status) {
      case 'PENDING':
        return '장바구니';
      case 'ORDERED':
        return '주문 완료 (결제가 필요합니다.)';
      case 'PAID':
        return '결제 완료';
      case 'CANCEL':
        return '취소 요청';
      case 'CANCELLED':
        return '취소됨';
      case 'SHIPPED':
        return '배송중';
      case 'DELIVERED':
        return '배송 완료';
      default:
        return '알수없음';
    }
  };

  const getDeliveryStatusLabel = (status) => {
    switch (status) {
      case 'READY':
        return '배송 준비중';
      case 'SHIPPED':
        return '배송 중';
      case 'DELIVERED':
        return '배송 완료';
      case 'CANCELLED':
        return '취소됨';
      default:
        return '알수없음';
    }
  };

  const handleCancelOrder = async () => {
    const isConfirmed = window.confirm('정말로 주문을 취소하시겠습니까?');
    if (isConfirmed) {
      try {
        await axiosInstance.patch(`/orders/${orderId}/cancel`);
        alert('주문이 취소되었습니다.');
        fetchOrders();  // 주문 목록을 다시 가져옵니다.
        history.push('/mypage/order-status');  // 주문현황으로 이동
      } catch (error) {
        console.error('There was an error cancelling the order!', error);
        alert('주문 취소에 실패했습니다.');
      }
    }
  };

  const handleCheckout = () => {
    const totalPrice = orderItemDtos.reduce((total, item) => total + item.orderprice * item.count, 0);

    const generateItemName = () => {
      if (orderItemDtos.length === 0) return '';
      const firstItemName = orderItemDtos[0].itemName;
      const additionalItemCount = orderItemDtos.length - 1;
      return additionalItemCount > 0
        ? `${firstItemName} 외 ${additionalItemCount}건`
        : firstItemName;
    };

    const itemName = generateItemName();
    history.push(`/checkout/payment-method/${orderId}`, { amount: totalPrice, itemName, orderId });
  };

  const getFirstImageUrl = (imageUrls) => {
    if (!imageUrls) return ''; // Return an empty string if imageUrls is null or undefined
    const urls = imageUrls.split(',');
    return urls[0];
  };

  if (!order) {
    return <div>Loading...</div>;
  }

  const { orderDto, orderItemDtos, deliveryDto, paymentDto } = order;

  return (
    <div className="order-detail">
      <button onClick={() => history.push('/mypage/order-status')} className="button back-btn">목록으로 돌아가기</button>
      <h3 className="order-details-header">
        주문 상세 정보 {(orderDto.status === 'ORDERED') && (
          <>
            <button onClick={handleCancelOrder} className="button cancel-btn">주문 취소</button>
            <button onClick={handleCheckout} className="button checkout-btn">결제하기</button>
          </>
        )}
      </h3>
      <h4>주문 상품 정보</h4>
      {orderItemDtos.length > 0 ? orderItemDtos.map((item) => (
        <div key={item.ID} className="order-item-detail">
          <div className="form-group">
            <label>상품 이름:</label>
            <span className="bold-text">{item.itemName}</span>
          </div>
          <div className="form-group">
            <label>주문 가격:</label>
            <span className="bold-text">{item.orderprice}원</span>
          </div>
          <div className="form-group">
            <label>주문 수량:</label>
            <span className="bold-text">{item.count}개</span>
          </div>
          <div className="form-group">
            <img src={getFirstImageUrl(item.imageUrl)} alt={item.itemName} className="product-image" />
          </div>
        </div>
      )) : <p>주문 상품 정보가 없습니다.</p>}
      <h4>배송 정보</h4>
      {deliveryDto ? (
        <div className="delivery-info">
          <div className="form-group">
            <label>배송 주소:</label>
            <span className="bold-text">{deliveryDto.address}</span>
          </div>
          <div className="form-group">
            <label>배송 상태:</label>
            <span className="bold-text">{getDeliveryStatusLabel(deliveryDto.status)}</span>
          </div>
        </div>
      ) : <p>배송 정보가 아직 입력되지 않았습니다.</p>}
      <h4>결제 정보</h4>
      {paymentDto ? (
        <div className="payment-info">
          <div className="form-group">
            <label>결제 금액:</label>
            <span className="bold-text">{paymentDto.amount}원</span>
          </div>
          <div className="form-group">
            <label>결제 방법:</label>
            <span className="bold-text">{paymentDto.paymentMethod}</span>
          </div>
          <div className="form-group">
            <label>결제 날짜:</label>
            <span className="bold-text">{new Date(paymentDto.paymentdate).toLocaleDateString()}</span>
          </div>
        </div>
      ) : <p>결제 정보가 아직 입력되지 않았습니다.</p>}
    </div>
  );
};

export default OrderDetail;