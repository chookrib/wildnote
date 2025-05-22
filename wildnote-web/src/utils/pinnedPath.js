const pinnedPaths = localStorage.getItem('pinnedPaths') ?
  JSON.parse(localStorage.getItem('pinnedPaths')) : []

const getAll = function() {
  // const pinnedPaths = localStorage.getItem('pinnedPaths')
  // if (pinnedPaths) {
  //   return JSON.parse(pinnedPaths)
  // }
  // return []
  return [...pinnedPaths]
}

const isPinned = function(path) {
  // return getAll().indexOf(path) === -1
  // return getAll().includes(path)
  return pinnedPaths.includes(path)
}

const pin = function(path) {
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

const unpin = function(path) {
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

const move = function(dragIndex, dropIndex){
  if (dragIndex === null || dragIndex === dropIndex) return
  const moved = pinnedPaths.splice(dragIndex, 1)[0]
  pinnedPaths.splice(dropIndex, 0, moved)
  localStorage.setItem('pinnedPaths', JSON.stringify(pinnedPaths))
}

export { getAll as getAllPinned, isPinned, pin, unpin, move as movePinned }
