
let accessToken = localStorage.getItem('accessToken') || ''

const getLocalAccessToken = function() {
  return accessToken
}

const setLocalAccessToken = function(accessToken) {
  localStorage.setItem('accessToken', accessToken)
  accessToken = accessToken
}

const removeLocalAccessToken = function() {
  localStorage.removeItem('accessToken')
  accessToken = ''
}

const pinnedPaths = localStorage.getItem('pinnedPaths') ?
  JSON.parse(localStorage.getItem('pinnedPaths')) : []

const getLocalPinnedPaths = function() {
  // const pinnedPaths = localStorage.getItem('pinnedPaths')
  // if (pinnedPaths) {
  //   return JSON.parse(pinnedPaths)
  // }
  // return []
  return [...pinnedPaths]
}

const isLocalPinnedPath = function(path) {
  // return getAll().indexOf(path) === -1
  return pinnedPaths.includes(path)
}

const localPinPath = function(path) {
  // const pinnedPaths = getAll()
  // if (!pinnedPaths.includes(path)) {
  //   pinnedPaths.push(path)
  //   localStorage.setItem('pinnedPaths', JSON.stringify(pinnedPaths))
  // }
  if (!pinnedPaths.includes(path)) {
    pinnedPaths.push(path)
    localStorage.setItem('pinnedPaths', JSON.stringify(pinnedPaths))
  }
}

const localUnpinPath = function(path) {
  // const pinnedPaths = getAll()
  // if (pinnedPaths.includes(path)) {
  //   pinnedPaths.splice(pinnedPaths.indexOf(path), 1)
  //   localStorage.setItem('pinnedPaths', JSON.stringify(pinnedPaths))
  // }
  if (pinnedPaths.includes(path)) {
    pinnedPaths.splice(pinnedPaths.indexOf(path), 1)
    localStorage.setItem('pinnedPaths', JSON.stringify(pinnedPaths))
  }
}

const localMovePinnedPath = function(dragIndex, dropIndex){
  if (dragIndex === null || dragIndex === dropIndex) return
  const moved = pinnedPaths.splice(dragIndex, 1)[0]
  pinnedPaths.splice(dropIndex, 0, moved)
  localStorage.setItem('pinnedPaths', JSON.stringify(pinnedPaths))
}

export {
  getLocalAccessToken, setLocalAccessToken, removeLocalAccessToken,
  getLocalPinnedPaths, isLocalPinnedPath, localPinPath, localUnpinPath, localMovePinnedPath,
  //xxx as xxx
}
