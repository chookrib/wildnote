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

const rawFavoriteNotePaths = localStorage.getItem('favorite_note_path');
let favoriteNotePaths = rawFavoriteNotePaths ? JSON.parse(rawFavoriteNotePaths) : [];

const getFavoriteNotePaths = () => {
  return [...favoriteNotePaths];
};

const setFavoriteNotePaths = (paths: string[]) => {
  favoriteNotePaths = [...paths];
  localStorage.setItem('favorite_note_path', JSON.stringify(favoriteNotePaths));
};

const isFavoriteNotePath = (path: string) => {
  return favoriteNotePaths.includes(path);
};

const addFavoritePath = (path: string) => {
  if (!favoriteNotePaths.includes(path)) {
    favoriteNotePaths.push(path);
    localStorage.setItem('favorite_note_path', JSON.stringify(favoriteNotePaths));
  }
};

const delFavoriteNotePath = (path: string) => {
  if (favoriteNotePaths.includes(path)) {
    favoriteNotePaths.splice(favoriteNotePaths.indexOf(path), 1);
    localStorage.setItem('favorite_note_path', JSON.stringify(favoriteNotePaths));
  }
};

const moveFavoritePath = (dragIndex: number, dropIndex: number) => {
  if (dragIndex === null || dragIndex === dropIndex) return;
  const moved = favoriteNotePaths.splice(dragIndex, 1)[0];
  favoriteNotePaths.splice(dropIndex, 0, moved);
  localStorage.setItem('favorite_note_path', JSON.stringify(favoriteNotePaths));
};

export {
  getAccessToken,
  setAccessToken,
  delAccessToken,
  getFavoriteNotePaths,
  setFavoriteNotePaths,
  isFavoriteNotePath,
  addFavoritePath,
  delFavoriteNotePath,
  moveFavoritePath,
  //xxx as xxx
};
