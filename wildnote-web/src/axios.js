import axios from "axios"
import router from "./router"
import { ElMessage } from "element-plus"

const instance = axios.create({
  baseURL: "http://localhost:8080/"
})

instance.interceptors.request.use(config => {
  const accessToken = localStorage.getItem('accessToken')
  config.headers['Access-Token'] = accessToken
  return config
})
instance.interceptors.response.use(response => {
  if (response.data.isSuccess) {
    return response
  }

  if (response.data.code === 2) {
    router.push('/login')
  }
  else {
    //alert(response.data.message)
    ElMessage.error(response.data.message)
    throw new Error(response.data.message)
  }
})

export default instance
