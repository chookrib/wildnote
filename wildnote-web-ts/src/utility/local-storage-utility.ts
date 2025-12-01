let accessToken = localStorage.getItem('access_token') || '';

const getAccessToken = () => {
  return accessToken;
};

const setAccessToken = (newAccessToken: string) => {
  localStorage.setItem('access_token', newAccessToken);
  accessToken = newAccessToken;
};

const deleteAccessToken = () => {
  localStorage.removeItem('access_token');
  accessToken = '';
};

//======================================================================================================================

const favoriteListStored = localStorage.getItem('favorite_list');
let favoriteList = favoriteListStored ? JSON.parse(favoriteListStored) : [];

const getFavorite = () => {
  return [...favoriteList];
};

const setFavorite = (list: string[]) => {
  favoriteList = [...list];
  localStorage.setItem('favorite_list', JSON.stringify(favoriteList));
};

const isFavorite = (item: string) => {
  return favoriteList.includes(item);
};

const addFavorite = (item: string) => {
  if (!favoriteList.includes(item)) {
    favoriteList.push(item);
    localStorage.setItem('favorite_list', JSON.stringify(favoriteList));
  }
};

const deleteFavorite = (item: string) => {
  if (favoriteList.includes(item)) {
    favoriteList.splice(favoriteList.indexOf(item), 1);
    localStorage.setItem('favorite_list', JSON.stringify(favoriteList));
  }
};

const moveFavorite = (dragIndex: number, dropIndex: number) => {
  if (dragIndex === null || dragIndex === dropIndex) return;
  const moved = favoriteList.splice(dragIndex, 1)[0];
  favoriteList.splice(dropIndex, 0, moved);
  localStorage.setItem('favorite_list', JSON.stringify(favoriteList));
};

export {
  getAccessToken,
  setAccessToken,
  deleteAccessToken,
  getFavorite,
  setFavorite,
  isFavorite,
  addFavorite,
  deleteFavorite,
  moveFavorite,
  //xxx as xxx
};
