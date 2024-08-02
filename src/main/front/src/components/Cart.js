import React, { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import './MyPage.css';

const Cart = () => {
  const [cartItems, setCartItems] = useState([]);
  const [quantities, setQuantities] = useState({});
  const history = useHistory();

  useEffect(() => {
    fetchCartItems();
  }, []);

  const fetchCartItems = async () => {
    try {
      const response = await axiosInstance.get('/orders/cart');
      setCartItems(response.data);
      const initialQuantities = response.data.reduce((acc, item) => {
        acc[item.id] = item.count;
        return acc;
      }, {});
      setQuantities(initialQuantities);
    } catch (error) {
      console.error('There was an error fetching the cart items!', error);
    }
  };

  const handleQuantityChange = (itemId, delta) => {
    setQuantities((prevQuantities) => {
      const newQuantities = { ...prevQuantities };
      newQuantities[itemId] = Math.max(1, newQuantities[itemId] + delta);
      return newQuantities;
    });
  };

  const handleUpdateCart = async () => {
    const orderItems = cartItems.map(item => ({
      id: item.id,
      itemID: item.itemID,
      count: quantities[item.id]
    }));

    try {
      const response = await axiosInstance.patch('/orders/cart/update', {
        orderItems,
      });
      setCartItems(response.data); // Update cart items after updating quantities
      alert('장바구니가 업데이트되었습니다.');
    } catch (error) {
      console.error('There was an error updating the cart!', error);
      alert('장바구니 업데이트에 실패했습니다.');
    }
  };

  const handleDeleteItem = async (orderItemId) => {
    const isConfirmed = window.confirm('정말로 이 항목을 삭제하시겠습니까?');
    if (isConfirmed) {
      try {
        const response = await axiosInstance.delete(`/orders/cart/${orderItemId}/delete-item`);
        setCartItems(response.data); // Update cart items after deletion
        alert('항목이 삭제되었습니다.');
      } catch (error) {
        console.error('There was an error deleting the cart item!', error);
        alert('항목 삭제에 실패했습니다.');
      }
    }
  };

  const handleUpdateButtonClick = () => {
    // Ensure state is updated before sending the request
    setTimeout(() => handleUpdateCart(), 0);
  };

  const calculateTotalPrice = () => {
    return cartItems.reduce((total, item) => {
      return total + item.orderprice * quantities[item.id];
    }, 0);
  };

  const generateItemName = () => {
    if (cartItems.length === 0) return '';
    const firstItemName = cartItems[0].itemName;
    const additionalItemCount = cartItems.length - 1;
    return additionalItemCount > 0
      ? `${firstItemName} 외 ${additionalItemCount}건`
      : firstItemName;
  };

  const handleCheckout = () => {
    const totalPrice = calculateTotalPrice();
    const itemName = generateItemName();
    const orderId = cartItems[0].orderID; // Assuming orderId is same for all items in the cart
    history.push('/checkout/address', { amount: totalPrice, itemName, orderId });
  };

  return (
    <div className="cart">
      <h2>장바구니</h2>
      {cartItems.length > 0 ? (
        cartItems.map((item) => (
          <div key={item.id} className="cart-item">
            <div className="form-group">
              <label>상품 이름:</label>
              <span className="bold-text">{item.itemName}</span>
            </div>
            <div className="form-group">
              <label>주문 가격:</label>
              <span className="bold-text">{item.orderprice}원</span>
            </div>
            <div className="form-group">
              <label>주문 수량:</label>
              <div className="quantity-control">
                <button onClick={() => handleQuantityChange(item.id, -1)} className="button quantity-btn">-</button>
                <span className="bold-text">{quantities[item.id]}</span>
                <button onClick={() => handleQuantityChange(item.id, 1)} className="button quantity-btn">+</button>
              </div>
            </div>
            {item.imageUrl && (
              <div className="form-group">
                <img src={item.imageUrl} alt={item.itemName} className="cart-item-image" />
              </div>
            )}
            <button onClick={() => handleDeleteItem(item.id)} className="button delete-btn">항목 삭제</button>
          </div>
        ))
      ) : (
        <p>장바구니에 담긴 상품이 없습니다.</p>
      )}
      {cartItems.length > 0 && (
        <>
          <button onClick={handleUpdateButtonClick} className="button update-cart-btn">장바구니 업데이트</button>
          <div className="total-price">
            <br />
            <h3>총 가격: {calculateTotalPrice()}원</h3>
          </div>
          <button onClick={handleCheckout} className="button checkout-btn">결제</button>
        </>
      )}
    </div>
  );
};

export default Cart;