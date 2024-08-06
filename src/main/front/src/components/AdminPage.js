import React, { useState, useEffect, useContext } from 'react';
import { useHistory, useLocation, Switch, Route, Redirect } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import { AuthContext } from '../context/AuthContext';
import './AdminPage.css';
import Dashboard from './Dashboard';
import MemberManagement from './MemberManagement';
import ItemManagement from './ItemManagement';
import ItemAdd from './ItemAdd';

const AdminPage = () => {
  const [adminData, setAdminData] = useState(null);
  const { isLoggedIn, setIsLoggedIn } = useContext(AuthContext);
  const history = useHistory();
  const location = useLocation();

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
      fetchAdminData();
    }
  }, [isLoggedIn, history, setIsLoggedIn]);

  if (!adminData) {
    return <div>Loading...</div>;
  }

  return (
    <div className="adminpage-container">
      <div className="sidebar">
        <ul>
          <li className={location.pathname === '/admin/dashboard' ? 'active' : ''} onClick={() => history.push('/admin/dashboard')}>대시보드</li>
          <li className={location.pathname === '/admin/member-management' ? 'active' : ''} onClick={() => history.push('/admin/member-management')}>회원 관리</li>
          <li className={location.pathname === '/admin/menu2' ? 'active' : ''} onClick={() => history.push('/admin/menu2')}>아이템 관리</li>
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
          <Route path="/admin/menu2">
            <ItemManagement />
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