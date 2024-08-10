import React, { useState, useEffect } from 'react';
import { useHistory, useRouteMatch, Route, Switch } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import OrderDetail from './OrderDetail';
import './MyPage.css';

const statusOptions = {
    ORDERED: [
        { value: 'ORDERED', label: '주문완료' },
    ],
    PAID: [
        { value: 'PAID', label: '결제완료' },
        { value: 'SHIPPED', label: '배송중' },
    ],
    CANCEL: [
        { value: 'CANCEL', label: '취소요청' },
        { value: 'CANCELLED', label: '취소됨' },
    ],
    CANCELLED: [
        { value: 'CANCELLED', label: '취소됨' },
    ],
    SHIPPED: [
        { value: 'SHIPPED', label: '배송중' },
        { value: 'DELIVERED', label: '배송완료' },
    ],
    DELIVERED: [
        { value: 'DELIVERED', label: '배송완료' },
    ],
};

const OrderManagement = () => {
    const [orders, setOrders] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [status, setStatus] = useState('ALL');
    const [selectedStatus, setSelectedStatus] = useState({});
    const { path, url } = useRouteMatch();
    const history = useHistory();

    useEffect(() => {
        fetchOrders(currentPage, status);
    }, [currentPage, status]);

    const fetchOrders = async (page, status) => {
        try {
            const response = await axiosInstance.get('/admin/order-list', {
                params: { page: page, status: status },
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

    const handleStatusChange = (e) => {
        setStatus(e.target.value);
        setCurrentPage(1); // Reset to first page on status change
    };

    const getStatusLabel = (status) => {
        const allOptions = Object.values(statusOptions).flat();
        const option = allOptions.find(option => option.value === status);
        return option ? option.label : '알수없음';
    };

    const handleOrderClick = (orderId) => {
        history.push(`${url}/${orderId}`, { from: 'order-management' });
    };

    const handleStatusSelectChange = (orderId, newStatus) => {
        setSelectedStatus((prevStatus) => ({
            ...prevStatus,
            [orderId]: newStatus,
        }));
    };

    const handleStatusUpdate = async (orderId) => {
        const statusToUpdate = selectedStatus[orderId] || orders.find(order => order.id === orderId)?.status;

        if (!statusToUpdate) {
            alert('상태를 선택해주세요.');
            return;
        }

        try {
            await axiosInstance.patch(`/admin/order-list/${orderId}/update-status`, {
                status: statusToUpdate,
            });
            alert('주문 상태가 업데이트되었습니다.');
            fetchOrders(currentPage, status); // Refresh the list
        } catch (error) {
            console.error('There was an error updating the order status!', error);
            alert('주문 상태 업데이트에 실패했습니다.');
        }
    };

    const renderStatusOptions = (currentStatus) => {
        return statusOptions[currentStatus]?.map((statusOption) => (
            <option key={statusOption.value} value={statusOption.value}>
                {statusOption.label}
            </option>
        )) || null;
    };

    // 중복 제거된 상태 옵션들을 가져옴
    const uniqueStatusOptions = Object.keys(statusOptions).map(statusKey => ({
        value: statusKey,
        label: getStatusLabel(statusKey),
    }));

    return (
        <Switch>
            <Route exact path={path}>
                <div className="order-status">
                    <h2>주문 관리</h2>
                    <p className="order-notice">주문을 클릭하면 상세정보 조회가 가능합니다.</p>
                    <div className="filter-bar">
                        <select value={status} onChange={handleStatusChange} className="status-select">
                            <option value="ALL">모든 주문</option>
                            {uniqueStatusOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </div>
                    {orders.length > 0 ? (
                        <div>
                            {orders
                                .filter(order => order.status !== 'PENDING')
                                .map((order) => (
                                    <div key={order.id} className="order-item">
                                        <div className="form-group">
                                            <label>주문 번호:</label>
                                            <span className="bold-text">{order.id}</span>
                                        </div>
                                        <div className="form-group">
                                            <label>주문 상태:</label>
                                            {['CANCEL', 'PAID', 'SHIPPED'].includes(order.status) ? (
                                                <>
                                                    <select
                                                        value={selectedStatus[order.id] || order.status}
                                                        onChange={(e) => handleStatusSelectChange(order.id, e.target.value)}
                                                        className="zzstatus-select"
                                                    >
                                                        {renderStatusOptions(order.status)}
                                                    </select>
                                                    <button onClick={() => handleStatusUpdate(order.id)} className="zzupdate-btn">확인</button>
                                                </>
                                            ) : (
                                                <span className="bold-text">{getStatusLabel(order.status)}</span>
                                            )}
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

export default OrderManagement;