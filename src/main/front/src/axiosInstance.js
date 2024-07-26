import axios from 'axios';

// Create an instance of axios
const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true, // Include cookies in requests
});

// Add a request interceptor
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

// Add a response interceptor
axiosInstance.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    // If error response status is 401 and not retried yet
    if (error.response.status === 401 && !originalRequest._retry && error.response.data === 'access token expired') {
      originalRequest._retry = true;

      try {
        // Attempt to refresh the token
        const response = await axiosInstance.post('/reissue');
        const newAccessToken = response.headers['access'];
        localStorage.setItem('access', newAccessToken);
        axiosInstance.defaults.headers.common['access'] = newAccessToken;

        // Retry the original request with the new token
        originalRequest.headers['access'] = newAccessToken;
        return axiosInstance(originalRequest);
      } catch (reissueError) {
        console.error('Token reissue failed', reissueError);

        // 재요청 불가하게 access헤더 제거
        localStorage.removeItem('access');
        // Redirect to login if refresh token has expired
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;