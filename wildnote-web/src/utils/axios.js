import axios from "axios"
import router from "../router"
import { message } from "ant-design-vue"

const instance = axios.create({
  //baseURL: "http://localhost:8080/"
  baseURL: "/"
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
    throw new Error(response.data.message)
  }
  else {
    //alert(response.data.message)
    message.error(response.data.message)
    throw new Error(response.data.message)
  }
})

export default instance
