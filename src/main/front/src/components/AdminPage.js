import React, { useState, useEffect, useContext } from 'react';
import { useHistory, useLocation, Switch, Route, Redirect } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import { AuthContext } from '../context/AuthContext';
import './AdminPage.css';
import Dashboard from './Dashboard';
import MemberManagement from './MemberManagement';
import ItemManagement from './ItemManagement';
import ItemAdd from './ItemAdd';
import QnaManagement from './QnaManagement';
import OrderMangement from './OrderManagement'

const AdminPage = () => {
  const [adminData, setAdminData] = useState(null);
  const { isLoggedIn, user, setIsLoggedIn } = useContext(AuthContext); // user를 추가로 가져옴
  const history = useHistory();
  const location = useLocation();

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const response = await axiosInstance.get('/check-auth');
        setIsLoggedIn(true);
        if (response.data.role !== 'ROLE_ADMIN') {
          alert('접근 권한이 없습니다.');
          history.push('/'); // 권한이 없으면 메인 페이지로 리다이렉트
        }
      } catch (error) {
        setIsLoggedIn(false);
        alert("로그인해주세요");
        history.push('/');
      }
    };

    const fetchAdminData = async () => {
      try {
        const response = await axiosInstance.get('/admin');
        setAdminData(response.data);
      } catch (error) {
        console.error('There was an error fetching the admin data!', error);
      }
    };

    if (!isLoggedIn) {
      checkAuth();
    } else {
      if (user && user.role !== 'ROLE_ADMIN') {
        alert('접근 권한이 없습니다.');
        history.push('/'); // 권한이 없으면 메인 페이지로 리다이렉트
      } else {
        fetchAdminData();
      }
    }
  }, [isLoggedIn, user, history, setIsLoggedIn]);

  if (!adminData) {
    return <div>Loading...</div>;
  }

  return (
    <div className="adminpage-container">
      <div className="sidebar">
        <ul>
          <li className={location.pathname === '/admin/dashboard' ? 'active' : ''} onClick={() => history.push('/admin/dashboard')}>대시보드</li>
          <li className={location.pathname === '/admin/member-management' ? 'active' : ''} onClick={() => history.push('/admin/member-management')}>회원 관리</li>
          <li className={location.pathname === '/admin/item-management' ? 'active' : ''} onClick={() => history.push('/admin/item-management')}>아이템 관리</li>
          <li className={location.pathname === '/admin/qna-management' ? 'active' : ''} onClick={() => history.push('/admin/qna-management')}>Q&A 관리</li>
          <li className={location.pathname === '/admin/order-management' ? 'active' : ''} onClick={() => history.push('/admin/order-management')}>주문 관리</li>
        </ul>
      </div>
      <div className="content">
        <Switch>
          <Route path="/admin/dashboard">
            <Dashboard adminData={adminData} />
          </Route>
          <Route path="/admin/member-management">
            <MemberManagement />
          </Route>
          <Route path="/admin/item-management">
            <ItemManagement />
          </Route>
          <Route path="/admin/qna-management">
            <QnaManagement />
          </Route>
          <Route path="/admin/order-management">
            <OrderMangement />
          </Route>
          <Route path="/admin/item-list/add-item">
            <ItemAdd />
          </Route>
          <Redirect from="/admin" to="/admin/dashboard" />
        </Switch>
      </div>
    </div>
  );
};

export default AdminPage;