import React from 'react';
import './App.css';
import { BrowserRouter as Router, Route, Switch, Link, NavLink } from 'react-router-dom';
import MainScreen from './components/MainScreen';
import Login from './components/Login';
import Signup from './components/Signup';
import Logout from './components/Logout';
import MyPage from './components/MyPage';
import ItemList from './components/ItemList';
import ItemDetail from './components/ItemDetail';
import { AuthProvider, AuthContext } from './context/AuthContext';
import Checkout from './components/Checkout';
import KakaoPaySuccess from './components/KakaoPaySuccess';
import KakaoPayCancel from './components/KakaoPayCancel';
import KakaoPayFail from './components/KakaoPayFail';

const onNaverLogin = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/naver";
};

const onGoogleLogin = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/google";
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <div>
          <nav>
            <ul>
              <li>
                <NavLink exact to="/" activeClassName="active">메인</NavLink>
              </li>
              <AuthContext.Consumer>
                {({ isLoggedIn }) => (
                  <>
                    {!isLoggedIn && (
                      <>
                        <li>
                          <NavLink to="/login" activeClassName="active">로그인</NavLink>
                        </li>
                        <li>
                          <NavLink to="/signup" activeClassName="active">회원가입</NavLink>
                        </li>
                      </>
                    )}
                    {isLoggedIn && (
                      <>
                        <li>
                          <NavLink to="/mypage" activeClassName="active">마이페이지</NavLink>
                        </li>
                        <li>
                          <NavLink to="/item-list" activeClassName="active">상품목록</NavLink>
                        </li>
                        <div className="right-align">
                          <Logout />
                        </div>
                      </>
                    )}
                  </>
                )}
              </AuthContext.Consumer>
            </ul>
          </nav>
          <Switch>
            <Route exact path="/" component={MainScreen} />
            <Route path="/login" component={Login} />
            <Route path="/signup" component={Signup} />
            <Route path="/mypage" component={MyPage} />
            <Route path="/item-list" component={ItemList} />
            <Route path="/item-detail/:itemId" component={ItemDetail} />
            <Route path="/checkout" component={Checkout} />
            <Route path="/payment/:orderId/success" component={KakaoPaySuccess} />
            <Route path="/payment/:orderId/cancel" component={KakaoPayCancel} />
            <Route path="/payment/:orderId/fail" component={KakaoPayFail} />
          </Switch>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;