import React, { useEffect } from 'react';
import { useHistory } from 'react-router-dom';

const KakaoPayFail = () => {
  const history = useHistory();

  useEffect(() => {
    alert('결제에 실패하였습니다.');
    history.push('/'); // 메인 화면으로 리다이렉트
  }, [history]);

  return (
    <div className="kakao-pay-fail">
      <h2>결제 실패</h2>
      <p>결제에 실패하였습니다.</p>
    </div>
  );
};

export default KakaoPayFail;