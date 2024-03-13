import { configureStore } from '@reduxjs/toolkit'
import tracksSlice from './slices/trackSlice'
import userSlice from './slices/userSlice'


const store = configureStore({
  reducer: {
    tracksSlice,
    userSlice
  },
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

export default store
