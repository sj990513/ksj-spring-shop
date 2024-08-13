import React, { useEffect, useContext, useState } from 'react';
import axiosInstance from '../axiosInstance';
import { Link, useHistory } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './MainScreen.css';

function MainScreen() {
  const { user, setUser, setIsLoggedIn } = useContext(AuthContext);
  const history = useHistory();
  const [categories, setCategories] = useState([
    { value: 'top', label: '상의' },
    { value: 'pants', label: '하의' },
    { value: 'cap', label: '모자' },
    { value: 'shoes', label: '신발' },
  ]);
  const [categoryItems, setCategoryItems] = useState({});

  useEffect(() => {
    const reissueToken = async () => {
      try {
        const response = await axiosInstance.post('/reissue');
        const newAccessToken = response.headers['access'];
        localStorage.setItem('access', newAccessToken);
        axiosInstance.defaults.headers.common['access'] = newAccessToken;

        const userInfoResponse = await axiosInstance.get('/check-auth');
        setUser(userInfoResponse.data);
        setIsLoggedIn(true);

        // Fetch items for each category
        fetchCategoryItems();

      } catch (error) {
        console.error('토큰 재발급 실패 또는 사용자 정보 가져오기 실패', error);
        localStorage.removeItem('access');
        setIsLoggedIn(false);
        fetchCategoryItems();
      }
    };

    const fetchCategoryItems = async () => {
      const itemsPerCategory = {};
      for (const category of categories) {
        try {
          const response = await axiosInstance.get(`/items/item-list/${category.value}`, {
            params: { page: 1 }, // 첫 페이지의 9개 아이템을 가져옴
          });
          itemsPerCategory[category.value] = response.data.content;
        } catch (error) {
          console.error(`Failed to fetch items for category ${category.value}:`, error);
        }
      }
      setCategoryItems(itemsPerCategory);
    };

    reissueToken();
  }, [history, setUser, setIsLoggedIn]);

  const getFirstImageUrl = (imageUrls) => {
    if (!imageUrls) return ''; // Return an empty string if imageUrls is null or undefined
    const urls = imageUrls.split(',');
    return urls[0];
  };

  const scrollLeft = (category) => {
    const container = document.getElementById(`slider-${category}`);
    container.scrollLeft -= 200; // Adjust the scroll amount as needed
  };

  const scrollRight = (category) => {
    const container = document.getElementById(`slider-${category}`);
    container.scrollLeft += 200; // Adjust the scroll amount as needed
  };

  return (
    <div className="main-screen">
      <div className="hero-section">
        <h1>Spring-shop에 오신걸 환영합니다.</h1>
        <p>다양한 제품들을 살펴보세요!</p>
        <Link to="/item-list">
          <button className="shop-now-btn">쇼핑하기</button>
        </Link>
      </div>

      <div className="categories-section">
        <h2>카테고리별 상품</h2>
        <p className="review-notice">
          상품 클릭시 해당 상품으로 이동합니다. <br></br> 상품 상세보기는 로그인 후 이용 가능합니다.
        </p>
        {categories.map((category) => (
          <div key={category.value} className="category-section">
            <h3>{category.label}</h3>
            <div className="product-slider-container">
              <button className="scroll-btn left" onClick={() => scrollLeft(category.value)}>&lt;</button>
              <div id={`slider-${category.value}`} className="product-slider">
                {categoryItems[category.value]?.map((product) => (
                  <div key={product.id} className="product-card">
                    <Link to={`/item-detail/${product.id}`}>
                      <img src={getFirstImageUrl(product.imageUrl)} alt={product.itemname} className="product-image" />
                      <p className="product-name">{product.itemname}</p>
                      <p className="product-price">{product.price}원</p>
                    </Link>
                  </div>
                ))}
              </div>
              <button className="scroll-btn right" onClick={() => scrollRight(category.value)}>&gt;</button>
            </div>
            <div>
              <Link to={`/item-list?category=${category.value}`}>
                <button className="show-more-btn">상품 더보기 {'>'}{'>'}{'>'}</button>
              </Link>
            </div>
          </div>
        ))}
      </div>

      <div className="footer">
        <p>© 2024 KSJ store. All Rights Reserved.</p>
      </div>
    </div>
  );
}

export default MainScreen;