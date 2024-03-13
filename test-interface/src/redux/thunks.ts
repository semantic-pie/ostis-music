import axios, { AxiosHeaders } from "axios";
import { AuthResponse, Track, UserAuthData, UserSignUpData } from "./interfaces";
import Cookies from "universal-cookie";
import { createAsyncThunk } from "@reduxjs/toolkit";
import { HOST } from "../config";

export const fetchAllTracks = createAsyncThunk('tracks/fetchAll', async () => {
  const token = new Cookies().get('jwt')

  let headers: AxiosHeaders = new AxiosHeaders()
  if (token) headers.setAuthorization(`Bearer ${token}`)

  return axios.get(`${HOST}/api/v1/derezhor/tracks?page=1&limit=1000`, { headers })
    .then(data => data.data as Track[])
});

export const fetchPlaylist = createAsyncThunk('tracks/fetchPlaylist', async () => {
  const token = new Cookies().get('jwt')

  let headers: AxiosHeaders = new AxiosHeaders()
  if (token) headers.setAuthorization(`Bearer ${token}`)

  return axios.get(`${HOST}/api/v1/derezhor/tracks/flow_playlist/playlist`, { headers })
    .then(data => data.data as Track[])
});


export const likeTrack = createAsyncThunk('tracks/like', async (hash: string) => {
  const token = new Cookies().get('jwt')

  let headers: AxiosHeaders = new AxiosHeaders()
  if (token) headers.setAuthorization(`Bearer ${token}`)

  console.log('token: ', token)
  return axios.post(`${HOST}/api/v1/derezhor/tracks/${hash}/like`, {}, { headers })
    .then(data => data.data)
});

export const generateNewFlowPlaylist = createAsyncThunk('tracks/generate-flow-playlist', async () => {
  const token = new Cookies().get('jwt')

  let headers: AxiosHeaders = new AxiosHeaders()
  if (token) headers.setAuthorization(`Bearer ${token}`)

  console.log('token: ', token)
  return axios.post(`${HOST}/api/v1/derezhor/tracks/playlist/generate`, {}, { headers })
    .then(data => data.data)
});

export const dislikeTrack = createAsyncThunk(
  "tracks/dislike",
  async (hash: string) => {
    const token = new Cookies().get('jwt');

    let headers: AxiosHeaders = new AxiosHeaders();
    if (token) headers.setAuthorization(`Bearer ${token}`);

    console.log("token: ", token);
    return axios
      .post(
        `${HOST}/api/v1/derezhor/tracks/${hash}/dislike`,
        {},
        { headers }
      )
      .then((data) => data.data);
  }
);

export const auth = createAsyncThunk('user/auth', async (userData: UserAuthData, { rejectWithValue }) => {
  return fetch(`${HOST}/api/v1/derezhor/auth`, {
    method: 'POST',
    body: JSON.stringify(userData),
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
  })
    .then((response) => {
      if (response.ok)
        return response.json() as Promise<AuthResponse>
      throw new Error('Errorrrrrrr');
    })
});

export const signUp = createAsyncThunk('user/signup', async (userData: UserSignUpData, { rejectWithValue }) => {
  return fetch(`${HOST}/api/v1/derezhor/signup`, {
    method: 'POST',
    body: JSON.stringify(userData),
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
  })
    .then((response) => {
      if (response.ok)
        return response.json() as Promise<AuthResponse>
      throw new Error('Errorrrrrrr');
    })
});