
let accessToken = localStorage.getItem('access_token') || ''

const getAccessToken = function() {
    return accessToken
}

const setAccessToken = function(newAccessToken:string) {
    localStorage.setItem('access_token', newAccessToken)
    accessToken = newAccessToken
}

const dropAccessToken = function() {
    localStorage.removeItem('access_token')
    accessToken = ''
}

//======================================================================================================================

const rawFavoriteNotePaths = localStorage.getItem('favorite_note_path')
let favoriteNotePaths = rawFavoriteNotePaths ? JSON.parse(rawFavoriteNotePaths) : []

const getFavoriteNotePaths = function() {
    return [...favoriteNotePaths]
}

const setFavoriteNotePaths = function(paths: string[]) {
    favoriteNotePaths = [...paths]
    localStorage.setItem('favorite_note_path', JSON.stringify(favoriteNotePaths))
}

const isFavoriteNotePath = function(path:string) {
    return favoriteNotePaths.includes(path)
}

const addFavoritePath = function(path:string) {
    if (!favoriteNotePaths.includes(path)) {
        favoriteNotePaths.push(path)
        localStorage.setItem('favorite_note_path', JSON.stringify(favoriteNotePaths))
    }
}

const dropFavoriteNotePath = function(path:string) {
    if (favoriteNotePaths.includes(path)) {
        favoriteNotePaths.splice(favoriteNotePaths.indexOf(path), 1)
        localStorage.setItem('favorite_note_path', JSON.stringify(favoriteNotePaths))
    }
}

const moveFavoritePath = function(dragIndex:number, dropIndex:number){
    if (dragIndex === null || dragIndex === dropIndex) return
    const moved = favoriteNotePaths.splice(dragIndex, 1)[0]
    favoriteNotePaths.splice(dropIndex, 0, moved)
    localStorage.setItem('favorite_note_path', JSON.stringify(favoriteNotePaths))
}

export {
    getAccessToken, setAccessToken, dropAccessToken,
    getFavoriteNotePaths, setFavoriteNotePaths,
    isFavoriteNotePath, addFavoritePath, dropFavoriteNotePath, moveFavoritePath
    //xxx as xxx
}
