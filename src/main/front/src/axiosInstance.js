import axios from 'axios';

// axios 인스턴스 생성
const axiosInstance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL+"/api",
  withCredentials: true, // 요청에 쿠키를 포함
});

// 요청 인터셉터 추가
axiosInstance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('access');
    if (token) {
      config.headers['access'] = token;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 추가
axiosInstance.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    // 에러 응답 상태가 401이고 재시도가 아니며 'access token expired'인 경우
    if (error.response.status === 401 && !originalRequest._retry && error.response.data === 'access token expired') {
      originalRequest._retry = true;

      try {
        // 토큰 갱신 시도
        const response = await axiosInstance.post('/reissue');
        const newAccessToken = response.headers['access'];
        localStorage.setItem('access', newAccessToken);
        axiosInstance.defaults.headers.common['access'] = newAccessToken;

        // 새로운 토큰으로 원래 요청을 재시도
        originalRequest.headers['access'] = newAccessToken;
        return axiosInstance(originalRequest);
      } catch (reissueError) {
        console.error('토큰 갱신 실패', reissueError);

        // 갱신 토큰이 만료된 경우 액세스 토큰 제거 및 메인 페이지로 리디렉션
        localStorage.removeItem('access');
        window.location.href = '/';
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;