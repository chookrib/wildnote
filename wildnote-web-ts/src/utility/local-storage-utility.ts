let accessToken = localStorage.getItem('access_token') || '';

const getAccessToken = () => {
  return accessToken;
};

const setAccessToken = (newAccessToken: string) => {
  localStorage.setItem('access_token', newAccessToken);
  accessToken = newAccessToken;
};

const delAccessToken = () => {
  localStorage.removeItem('access_token');
  accessToken = '';
};

//======================================================================================================================

const favoritePathsStorage = localStorage.getItem('favorite_paths');
let favoritePaths = favoritePathsStorage ? JSON.parse(favoritePathsStorage) : [];

const getFavoritePaths = () => {
  return [...favoritePaths];
};

const setFavoritePaths = (paths: string[]) => {
  favoritePaths = [...paths];
  localStorage.setItem('favorite_paths', JSON.stringify(favoritePaths));
};

const isFavoritePath = (path: string) => {
  return favoritePaths.includes(path);
};

const addFavoritePath = (path: string) => {
  if (!favoritePaths.includes(path)) {
    favoritePaths.push(path);
    localStorage.setItem('favorite_paths', JSON.stringify(favoritePaths));
  }
};

const delFavoritePath = (path: string) => {
  if (favoritePaths.includes(path)) {
    favoritePaths.splice(favoritePaths.indexOf(path), 1);
    localStorage.setItem('favorite_paths', JSON.stringify(favoritePaths));
  }
};

const moveFavoritePath = (dragIndex: number, dropIndex: number) => {
  if (dragIndex === null || dragIndex === dropIndex) return;
  const moved = favoritePaths.splice(dragIndex, 1)[0];
  favoritePaths.splice(dropIndex, 0, moved);
  localStorage.setItem('favorite_paths', JSON.stringify(favoritePaths));
};

export {
  getAccessToken,
  setAccessToken,
  delAccessToken,
  getFavoritePaths,
  setFavoritePaths,
  isFavoritePath,
  addFavoritePath,
  delFavoritePath,
  moveFavoritePath,
  //xxx as xxx
};
