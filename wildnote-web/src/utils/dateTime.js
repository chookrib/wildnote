export function showDateTime(dt) {
  const date = new Date()
  date.setTime(dt)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const MM = month.toString().padStart(2, '0')
  const day = date.getDate()
  const dd = day.toString().padStart(2, '0')
  const hour = date.getHours()
  const HH = hour.toString().padStart(2, '0')
  const minute = date.getMinutes()
  const mm = minute.toString().padStart(2, '0')
  const second = date.getSeconds()
  const ss = second.toString().padStart(2, '0')
  return `${year}-${MM}-${dd} ${HH}:${mm}:${ss}`
}
