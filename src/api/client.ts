import axios from 'axios'

const instance = axios.create({ baseURL: '/api', timeout: 15000 })

instance.interceptors.response.use(
  (resp) => resp,
  (err) => {
    const msg = err?.response?.data?.message || err.message
    return Promise.reject(new Error(msg))
  }
)

export default instance




