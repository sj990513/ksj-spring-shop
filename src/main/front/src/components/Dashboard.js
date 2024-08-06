import React from 'react';
import './AdminPage.css';

const Dashboard = ({ adminData }) => (
  <div className="dashboard-container">
    <h2>대시보드</h2>
    <div className="dashboard-section">
      <h3>최근 가입한 회원</h3>
      <div className="admembers-box">
        {adminData.memberDtos.slice(0, 10).map(member => (
          <div key={member.ID} className="admember-item">
            {member.nickname}
          </div>
        ))}
      </div>
    </div>
    <div className="dashboard-section">
      <h3>결제 완료된 주문 - 배송대기중</h3>
      <div className="adorders-box">
        {adminData.orderDtos.map(order => (
          <div key={order.id} className="adorder-item">
            <div>주문번호: {order.id}</div>
            <div>결제금액: {order.totalprice}</div>
          </div>
        ))}
      </div>
    </div>
  </div>
);

export default Dashboard;