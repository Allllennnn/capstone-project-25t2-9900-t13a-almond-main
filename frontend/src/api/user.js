import http from './http'

// User Login
export function login(data) {
  return http.post('/api/login', data)
}

// User Register
export function register(data) {
  return http.post('/api/register', data)
}
