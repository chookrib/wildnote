const getAll = function() {
  const pinnedNotes = localStorage.getItem('pinnedNotes')
  if (pinnedNotes) {
    return JSON.parse(pinnedNotes)
  }
  return []
}

const isPinned = function(path) {
  //return getAll().indexOf(path) === -1
  return getAll().includes(path)
}

const pin = function(path) {
  const pinnedNotes = getAll()
  if (!pinnedNotes.includes(path)) {
    pinnedNotes.push(path)
    localStorage.setItem('pinnedNotes', JSON.stringify(pinnedNotes))
  }
}

const unpin = function(path) {
  const pinnedNotes = getAll()
  if (pinnedNotes.includes(path)) {
    pinnedNotes.splice(pinnedNotes.indexOf(path), 1)
    localStorage.setItem('pinnedNotes', JSON.stringify(pinnedNotes))
  }
}

export { getAll, isPinned, pin, unpin }
