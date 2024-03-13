import { createSlice } from '@reduxjs/toolkit'
import Cookies from 'universal-cookie';
import { jwtDecode } from "jwt-decode";
import { auth, signUp } from '../thunks';


type AuthState = { authenticated: boolean, message?: string, username?: string, uuid?: string }
export interface UserState {
  auth: AuthState
}

const initialState: UserState = {
  auth: {
    authenticated: false,
  }
}

export const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    logout: (state) => {
      const cookies = new Cookies();
      cookies.remove('jwt')
      state.auth = logoutState()
    },
    tryAuth: (state) => {
      const cookies = new Cookies();
      const token = cookies.get('jwt')

      if (token) {
        const { username, uuid } = jwtDecode<{ username: string, uuid: string }>(token)
        state.auth = loginState({ username, uuid })
        console.log(`Token founded, user [${username}] authenticated`)
      } else {
        console.log('Token NOT founded')
        state.auth = logoutState()
      }
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(auth.fulfilled, (state, action) => {
        console.log('fulfilled: ', action.payload.token)
        if (action.payload.token) {
          const cookies = new Cookies();
          cookies.remove('jwt')
          console.log('new token: ', action.payload.token)
          cookies.set('jwt', action.payload.token, { path: '/', expires: new Date(new Date().getTime() + (30 * 60 * 1000)) }); // 30 minutes expiration
          const {username, uuid} = jwtDecode<{ username: string, uuid: string }>(action.payload.token)
          state.auth = loginState({username, uuid})
        }
      })
      .addCase(auth.rejected, (state) => {
        state.auth.message = 'Wrong password or username'
        state.auth.authenticated = false
      })
    builder
      .addCase(signUp.fulfilled, (state) => {
        state.auth.message = undefined
      })
  }
})

const logoutState = (): AuthState => {
  return { authenticated: false, message: undefined, username: undefined, uuid: undefined }
}

const loginState = ({ message = undefined, username, uuid }: { message?: string, username?: string, uuid?: string }): AuthState => {
  return { authenticated: true, message, username, uuid }
}

export const { tryAuth, logout } = userSlice.actions

export default userSlice.reducer