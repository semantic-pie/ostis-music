export interface Track {
  hash: string
  title: string
  author: string
  liked?: boolean
  genre: Genre
  scAddr?: number
}

export interface Genre {
  idtf: string
  name: string
}

export interface UserAuthData {
  username: string,
  password: string
}

export type ROLE = 'ROLE_ADMIN' | 'ROLE_USER'

export interface UserSignUpData {
  username: string,
  password: string,
  userRole: ROLE,
  favoriteGenres: { name: string, weight: number }[]
}

export interface UserLogInData {
  username: string,
  password: string,
}

export interface AuthResponse {
  token: string
}

export interface AuthBadResponse {
  message: string
}