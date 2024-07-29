import React, { useState, useEffect } from 'react';
import { useHistory, useRouteMatch, Route, Switch } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import OrderDetail from './OrderDetail';
import './MyPage.css';

const OrderStatus = () => {
  const [orders, setOrders] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const { path, url } = useRouteMatch(); // Add url
  const history = useHistory();

  useEffect(() => {
    fetchOrders(currentPage);
  }, [currentPage]);

  const fetchOrders = async (page) => {
    try {
      const response = await axiosInstance.get('/orders', {
        params: { page: page },
      });
      setOrders(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('There was an error fetching the orders!', error);
    }
  };

  const handlePageChange = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case 'PENDING':
        return '장바구니';
      case 'ORDERED':
        return '주문완료 (결제가 필요합니다.)';
      case 'PAID':
        return '결제완료';
      case 'CANCEL':
        return '취소요청';
      case 'CANCELLED':
        return '취소됨';
      case 'SHIPPED':
        return '배송중';
      case 'DELIVERED':
        return '배송완료';
      default:
        return '알수없음';
    }
  };

  const handleOrderClick = (orderId) => {
    history.push(`${url}/${orderId}`);
  };

  return (
    <Switch>
      <Route exact path={path}>
        <div className="order-status">
          <h2>주문현황</h2>
          <p className="order-notice">주문을 클릭하면 상세정보 조회가 가능합니다.</p>
          {orders.length > 0 ? (
            <div>
              {orders.map((order) => (
                <div key={order.id} className="order-item" onClick={() => handleOrderClick(order.id)}>
                  <div className="form-group">
                    <label>주문 번호:</label>
                    <span className="bold-text">{order.id}</span>
                  </div>
                  <div className="form-group">
                    <label>주문 상태:</label>
                    <span className="bold-text">{getStatusLabel(order.status)}</span>
                  </div>
                  <div className="form-group">
                    <label>주문 날짜:</label>
                    <span className="bold-text">{new Date(order.orderDate).toLocaleDateString()}</span>
                  </div>
                  <div className="form-group">
                    <label>결제 금액:</label>
                    <span className="bold-text">{order.totalprice}원</span>
                  </div>
                </div>
              ))}
              <div className="pagination">
                <button onClick={() => handlePageChange(currentPage - 1)} disabled={currentPage === 1}>이전</button>
                <span>{currentPage} / {totalPages}</span>
                <button onClick={() => handlePageChange(currentPage + 1)} disabled={currentPage === totalPages}>다음</button>
              </div>
            </div>
          ) : (
            <p>주문 내역이 없습니다.</p>
          )}
        </div>
      </Route>
      <Route path={`${path}/:orderId`}>
        <OrderDetail fetchOrders={fetchOrders} />
      </Route>
    </Switch>
  );
};

export default OrderStatus;