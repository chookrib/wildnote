/**
 * 格式化日期时间，格式为: YYYY-MM-DD HH:mm:ss
 */
const formatDateTime = (dt: number) => {
  if (!dt) return '';
  const date = new Date();
  date.setTime(dt);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const MM = month.toString().padStart(2, '0');
  const day = date.getDate();
  const dd = day.toString().padStart(2, '0');
  const hour = date.getHours();
  const HH = hour.toString().padStart(2, '0');
  const minute = date.getMinutes();
  const mm = minute.toString().padStart(2, '0');
  const second = date.getSeconds();
  const ss = second.toString().padStart(2, '0');
  return `${year}-${MM}-${dd} ${HH}:${mm}:${ss}`;
};

/**
 * 格式化时长，格式为: ?天?小时?分钟?秒
 */
const formatDuration = (time: string) => {
  if (!time) return null;
  const t = Number(time);
  const days = Math.floor(t / (24 * 60 * 60 * 1000));
  const hours = Math.floor((t % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000));
  const minutes = Math.floor((t % (60 * 60 * 1000)) / (60 * 1000));
  const seconds = Math.floor((t % (60 * 1000)) / 1000);
  let result = '';
  if (days > 0) result += days + '天';
  if (hours > 0 || days > 0) result += hours + '小时';
  if (minutes > 0 || hours > 0 || days > 0) result += minutes + '分钟';
  result += seconds + '秒';
  return result;
};

export { formatDateTime, formatDuration };
