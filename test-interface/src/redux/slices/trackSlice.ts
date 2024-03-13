import { createSlice } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'
import { logout } from './userSlice'
import { Genre, Track } from '../interfaces'
import { TRACKS_PER_PAGE } from '../../config'
import { dislikeTrack, fetchAllTracks, fetchPlaylist, likeTrack } from '../thunks'

export interface TracksState {
  tracks: {
    all: Track[]
    playlist: Track[]
  }

  statuses: {
    all: {
      loading: boolean,
      error?: string
    }
    playlist: {
      loading: boolean,
      error?: string
    }
  }

  searchQuery: string

  current: {
    tracks: Track[]
    track: Track
    page: number
  }

  control: {
    likedList: boolean
    playlist: boolean
  }
}

const initialState: TracksState = {
  tracks: { all: [], playlist: [] },
  statuses: {
    all: { loading: false },
    playlist: { loading: false }
  },

  searchQuery: '',

  current: {
    tracks: [],
    track: undefined,
    page: 1
  },

  control: {
    likedList: false,
    playlist: false
  }
}

export const tracksSlice = createSlice({
  name: 'tracks',
  initialState,
  reducers: {
    changeQuery: (state, action: PayloadAction<string>) => {
      state.searchQuery = action.payload
    },

    search: (state, action: PayloadAction<string>) => {
      if (action.payload.length > 0) {
        const query = action.payload.toLocaleLowerCase().split(' ').filter(q => q.length)
        console.log(query)
        const result = []

        for (const track of state.current.tracks) {
          for (const subquery of query) {
            if (track.title?.toLowerCase().includes(subquery)
              || track.author?.toLowerCase().includes(subquery)
              || track.genre?.name.toLowerCase().includes(subquery)) {
              result.push(track)
              break;
            }
          }
        }

        state.current.tracks = result
      } else {
        if (state.control.likedList)
          state.current.tracks = state.current.tracks.filter(t => t.liked)
        else if (state.control.playlist)
          state.current.tracks = state.tracks.playlist
        else
          state.current.tracks = state.tracks.all
      }
    },
    selectTrack: (state, action: PayloadAction<Track>) => {
      state.current.track = action.payload
    },
    toggleLikedTracks: (state) => {
      state.control.playlist = false
      state.control.likedList = !state.control.likedList

      if (state.control.likedList)
        state.current.tracks = state.tracks.all.filter(t => t.liked)
      else if (state.control.playlist)
        state.current.tracks = state.tracks.playlist
      else
        state.current.tracks = state.tracks.all
    },
    togglePlaylist: (state) => {
      state.control.likedList = false
      state.control.playlist = !state.control.playlist

      if (state.control.playlist)
        state.current.tracks = state.tracks.playlist
      else if (state.control.likedList)
        state.current.tracks = state.tracks.all.filter(t => t.liked)
      else
        state.current.tracks = state.tracks.all
    },
    changePage: (state, action: PayloadAction<number>) => {
      const pageNumber = action.payload


      if (pageNumber > 1 && state.current.tracks.length > (pageNumber * TRACKS_PER_PAGE - TRACKS_PER_PAGE)) {
        console.log('pageNumber1: ', pageNumber)
        state.current.page = pageNumber
      } else {
        state.current.page = 1
      }
    },
    nextTrack: (state) => {
      const current = state.current.track
      const currentIndex = state.current.tracks.sort((a, b) =>
        a.title.localeCompare(b.title)
      ).findIndex((t) => t.hash === current.hash)
      const nextIndex = currentIndex + 1
      if (currentIndex != -1 && nextIndex < state.current.tracks.length && nextIndex >= 0) {
        const nextTrack = state.current.tracks[nextIndex]
        state.current.track = nextTrack
      }
    },
    prevTrack: (state) => {
      const current = state.current.track


      const currentIndex = state.current.tracks.sort((a, b) =>
        a.title.localeCompare(b.title)
      ).findIndex((t) => t.hash === current.hash)
      const prevIndex = currentIndex - 1

      console.log('current: ', current)
      console.log('currentIndex: ', currentIndex)
      console.log('prevIndex: ', prevIndex)
      if (currentIndex != -1 && prevIndex < state.current.tracks.length && prevIndex >= 0) {
        console.log('prev: ', state.current.tracks[prevIndex])
        const prevTrack = state.current.tracks[prevIndex]
        state.current.track = prevTrack
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchAllTracks.pending, (state, action) => {
        state.statuses.all.loading = true
      })
      .addCase(fetchAllTracks.fulfilled, (state, action) => {
        state.statuses.all.loading = false
        state.tracks.all = action.payload
        state.current.tracks = action.payload
        // state.genres = Array.from(new Set(state.allTracks.map(t => t.genre).filter(g => !!g && !!g.name).map(g => JSON.stringify(g)))).map(g => JSON.parse(g) as Genre);
        // state.pageTracks = state.allTracks.slice(0, TRACKS_PER_PAGE)
        // console.log('tracks: ', action.payload)
      })
      .addCase(fetchAllTracks.rejected, (state, action) => {
        state.statuses.all.loading = false;
        state.statuses.all.error = action.error.message;
        console.log('rejected: [fetchAllTracks]')
      })
      .addCase(logout, (state) => {
        state.tracks.all = state.tracks.all.map(t => ({ ...t, liked: false }))
        state.tracks.playlist = []

        state.control.likedList = false
        state.control.playlist = false
      })
      .addCase(fetchPlaylist.fulfilled, (state, action) => {
        state.tracks.playlist = action.payload;
        state.current.tracks = action.payload;
      })
      .addCase(fetchPlaylist.rejected, (state, action) => {
        console.log('rejected: [fetchPlaylist]')
      })
      .addCase(likeTrack.fulfilled, (state) => {
        const track = state.current.track
        if (!track.liked) {
          state.tracks.all = getUpdatedWithLikeTracks(track, true, state.tracks.all)
          state.tracks.playlist = getUpdatedWithLikeTracks(track, true, state.tracks.playlist)
          state.current.tracks = getUpdatedWithLikeTracks(track, true, state.current.tracks)

          if (state.control.likedList)
            state.current.tracks = state.tracks.all.filter(t => t.liked)
        }
      })
      .addCase(likeTrack.rejected, (state, action) => {
        console.log('rejected: [likeTrack]')
      })
      .addCase(dislikeTrack.fulfilled, (state, action) => {
        const track = state.current.track
        if (track.liked) {
          state.tracks.all = getUpdatedWithLikeTracks(track, false, state.tracks.all)
          state.tracks.playlist = getUpdatedWithLikeTracks(track, false, state.tracks.playlist)
          state.current.tracks = getUpdatedWithLikeTracks(track, false, state.current.tracks)

          if (state.control.likedList)
            state.current.tracks = state.tracks.all.filter(t => t.liked)
        }
      })
      .addCase(dislikeTrack.rejected, (state, action) => {
        console.log('rejected: [dislikeTrack]')
      })
  }
})

const getUpdatedWithLikeTracks = (track: Track, like: boolean, tracks: Track[]) => {
  const updatedTracks = tracks.filter(t => t.hash !== track.hash)
  if (updatedTracks.length !== tracks.length) {
    track.liked = like
    updatedTracks.push(track)
  }


  return updatedTracks
}

export const { changePage, selectTrack, toggleLikedTracks, togglePlaylist, search, changeQuery, nextTrack, prevTrack } = tracksSlice.actions

export default tracksSlice.reducer
