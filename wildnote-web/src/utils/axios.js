import axios from "axios"
//import router from "../router"
import { message } from "ant-design-vue"

const instance = axios.create({
  baseURL: "http://localhost:8080/"
  //baseURL: "/"
})

instance.interceptors.request.use(config => {
  const accessToken = localStorage.getItem('accessToken')
  config.headers['Access-Token'] = accessToken
  return config
})

instance.interceptors.response.use(response => {

  // console.log(response)

  // try {
  //   JSON.parse(response.data)
  // } catch (e) {
  //   console.log(e)
  //   router.push('/login')
  //   throw new Error(e)
  // }

  if (response.data.success) {
    return response
  }

  if (response.data.code === -1) {
    //router.push('/login')
    window.location.href = "/login.html"
    throw new Error(response.data.message)
  }
  else {
    //alert(response.data.message)
    message.error(response.data.message)
    throw new Error(response.data.message)
  }
}, error => {
  //axios全局错误处理
  console.log(error)
  message.error(error.message)
  throw new Error(error.message)
})

export default instance
