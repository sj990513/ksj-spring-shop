import React, { useEffect, useContext } from 'react';
import axiosInstance from '../axiosInstance';
import { Link, useHistory } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

function MainScreen() {
  const { user, setUser, setIsLoggedIn } = useContext(AuthContext);  // setIsLoggedIn도 추가
  const history = useHistory();

  useEffect(() => {
    const reissueToken = async () => {
      try {
        const response = await axiosInstance.post('/reissue');
        const newAccessToken = response.headers['access'];
        localStorage.setItem('access', newAccessToken);
        axiosInstance.defaults.headers.common['access'] = newAccessToken;

        const userInfoResponse = await axiosInstance.get('/check-auth');
        setUser(userInfoResponse.data);
        setIsLoggedIn(true);  // 로그인 상태로 설정

      } catch (error) {
        console.error('토큰 재발급 실패 또는 사용자 정보 가져오기 실패', error);
        localStorage.removeItem('access');
        setIsLoggedIn(false);  // 로그아웃 상태로 설정
        history.push('/');
      }
    };

    reissueToken();
  }, [history, setUser, setIsLoggedIn]);

  return (
    <div>
      <h1>메인화면</h1>
      {user ? (
        <div>
          <p>로그인중인 사용자: [{user.nickname}]</p>
        </div>
      ) : (
        <div>
          <p>로그인 해주세요.</p>
        </div>
      )}
      <Link to="/item-list">
        <button>상품목록</button>
      </Link>
    </div>
  );
}

export default MainScreen;