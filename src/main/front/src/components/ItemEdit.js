import React, { useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import './ItemEdit.css';

const categories = [
  { value: 'top', label: '상의' },
  { value: 'pants', label: '하의' },
  { value: 'cap', label: '모자' },
  { value: 'shoes', label: '신발' },
];

const ItemEdit = () => {
  const { itemId } = useParams();
  const history = useHistory();
  const [itemData, setItemData] = useState({
    itemname: '',
    price: '',
    stock: '',
    category: '',
    description: '',
    imageUrl: '',
  });
  const [files, setFiles] = useState([]);
  const [existingImages, setExistingImages] = useState([]);

  useEffect(() => {
    const fetchItemDetail = async () => {
      try {
        const response = await axiosInstance.get(`/items/${itemId}`);
        const { itemDto } = response.data;
        setItemData(itemDto);
        setExistingImages(itemDto.imageUrl ? itemDto.imageUrl.split(',') : []);
      } catch (error) {
        console.error('There was an error fetching the item details!', error);
      }
    };

    fetchItemDetail();
  }, [itemId]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setItemData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleFileChange = (e) => {
    const selectedFiles = Array.from(e.target.files);
    const validFiles = selectedFiles.filter(file => file.type === 'image/jpeg' || file.type === 'image/png');

    if (validFiles.length !== selectedFiles.length) {
      alert('JPG 및 PNG 파일만 업로드할 수 있습니다.');
    }

    setFiles(validFiles);
  };

  const handleRemoveExistingImage = (url) => {
    const isConfirmed = window.confirm('이 이미지를 삭제하시겠습니까?');
    if (isConfirmed) {
      setExistingImages(existingImages.filter(imageUrl => imageUrl !== url));
    }
  };

  const uploadToCloudinary = async (file) => {
    const url = `https://api.cloudinary.com/v1_1/dm8jlhglf/upload`;
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', 'y4j4ewxk'); // replace with your upload preset

    const response = await fetch(url, {
      method: 'POST',
      body: formData,
    });

    const data = await response.json();
    return data.secure_url;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      let imageUrlString = existingImages.join(',');

      if (files.length > 0) {
        const uploadedImageUrls = await Promise.all(files.map(file => uploadToCloudinary(file)));
        imageUrlString = [...existingImages, ...uploadedImageUrls].join(',');
      }

      const updatedItemData = {
        ...itemData,
        imageUrl: imageUrlString,
      };

      await axiosInstance.patch(`/admin/item-list/${itemId}/update`, updatedItemData);
      alert('아이템이 성공적으로 수정되었습니다.');
      history.push(`/item-detail/${itemId}`); // Redirect to item detail page after edit
    } catch (error) {
      console.error('There was an error updating the item!', error);
      alert('아이템 수정 실패');
    }
  };

  return (
    <div className="item-edit-container">
      <h2>아이템 수정</h2>
      <form onSubmit={handleSubmit} className="item-edit-form">
        <div className="form-group">
          <label htmlFor="itemname">아이템명</label>
          <input
            type="text"
            id="itemname"
            name="itemname"
            value={itemData.itemname}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="price">가격</label>
          <input
            type="number"
            id="price"
            placeholder="숫자만 입력"
            name="price"
            value={itemData.price}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="stock">재고</label>
          <input
            type="number"
            id="stock"
            placeholder="숫자만 입력"
            name="stock"
            value={itemData.stock}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="category">카테고리</label>
          <select
            id="category"
            name="category"
            value={itemData.category}
            onChange={handleChange}
            required
          >
            {categories.map((cat) => (
              <option key={cat.value} value={cat.value}>
                {cat.label}
              </option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label htmlFor="description">설명</label>
          <textarea
            id="description"
            name="description"
            value={itemData.description}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label>기존 이미지</label>
          <div className="existing-images">
            {existingImages.map((url, index) => (
              <div key={index} className="existing-image">
                <img src={url} alt={`Existing ${index}`} className="item-detail-image" />
                <button type="button" onClick={() => handleRemoveExistingImage(url)}>삭제</button>
              </div>
            ))}
          </div>
        </div>
        <div className="form-group">
          <label htmlFor="imageUrl">새로운 이미지 추가</label>
          <input
            type="file"
            id="imageUrl"
            name="imageUrl"
            accept="image/jpeg, image/png"
            multiple
            onChange={handleFileChange}
          />
        </div>
        <button type="submit" className="submit-btn">수정하기</button>
      </form>
    </div>
  );
};

export default ItemEdit;