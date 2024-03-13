import { render } from "preact"
import "./styles/tailwind.css"
import UserAuth from "./components/user-auth"
import Player from "./components/player"
import TracksList from "./components/tracks-list"
import store from "./redux/store"
import { Provider } from "react-redux"
import { useEffect, useState } from "preact/hooks"
import Control from "./components/control"
import { useAppDispatch, useAppSelector } from "./redux/hooks"
import { fetchAllTracks } from "./redux/thunks"
import { changePage, selectTrack } from "./redux/slices/trackSlice"
import Search from "./components/search"
import { getLastPage } from "./utils/helpers"

export function AppWrapper() {
  return (
    <Provider store={store}>
      <div class='flex justify-center'>
        <App />
      </div>
    </Provider>
  )
}

export function App() {
  const dispatch = useAppDispatch()

  const statuses = useAppSelector(state => state.tracksSlice.statuses.all)
  const track = useAppSelector((state) => state.tracksSlice.current.track)
  const currentPage = useAppSelector((state) => state.tracksSlice.current.page)
  const pageTracks = useAppSelector((state) => state.tracksSlice.current.tracks)
  const maxPage = useAppSelector((state) =>
    getLastPage(state.tracksSlice.current.tracks.length)
  )

  const { authenticated, username } = useAppSelector(
    (state) => state.userSlice.auth
  )

  const [lastUpdate, setLastUpadte] = useState<Date>()

  useEffect(() => {
    console.log("use effect")
    if (!lastUpdate || Date.now() - lastUpdate.getTime() > 2000) {
      console.log("fetch")
      console.log("username: ", username)
      dispatch(fetchAllTracks())
      setLastUpadte(new Date())
    }
  }, [username])

  return (
    <div class="boxer flex-col mx-auto w-11/12  sm:w-11/12 md:min-w-[400px] md:w-11/12 lg:w-8/12 mt-[15px] sm:mt-[30px] md:mt-[50px]  mb-[220px] md:mb-0" >
      <h1>OSTIS music</h1>

      <div class='flex'>
        <UserAuth />
      </div>

      <Search />
      <section class='flex flex-col md:flex-row gap-2'>
        {authenticated && <Control />}

        <Player currentTrack={track} />

        {pageTracks.length > 0 ? (
          <TracksList
            tracks={pageTracks}
            setCurrentTrack={(track) => dispatch(selectTrack(track))}
            currentPage={currentPage}
            maxPage={maxPage}
            changePage={(page) => dispatch(changePage(page))}
          />
        ) : statuses.loading ? <TrackListSkeleton /> : <div class='flex w-full'><span class='mx-auto my-auto'>Empty (</span></div> }
      </section>
    </div>
  )
}

const TrackListSkeleton = () => {
  return (
    <div
      role="status"
      class="w-full space-y-4 border border-gray-200 divide-y divide-gray-200 rounded animate-pulse mt-5 md:p-1"
    >
      <div class="flex items-center justify-between">
        <div>
          <div class="h-2.5 bg-gray-300 w-[260px] mb-2.5"></div>
          <div class="w-[190px] h-2 bg-gray-200  "></div>
        </div>
        <div class="h-2.5 bg-gray-300 w-12"></div>
      </div>
      <div class="flex items-center justify-between pt-4">
        <div>
          <div class="h-2.5 bg-gray-300  w-[230px] mb-2.5"></div>
          <div class="w-[160px] h-2 bg-gray-200  "></div>
        </div>
        <div class="h-2.5 bg-gray-300  w-12"></div>
      </div>
      <div class="flex items-center justify-between pt-4">
        <div>
          <div class="h-2.5 bg-gray-300    w-[250px] mb-2.5"></div>
          <div class="w-[170px] h-2 bg-gray-200  "></div>
        </div>
        <div class="h-2.5 bg-gray-300   w-12"></div>
      </div>
      <div class="flex items-center justify-between pt-4">
        <div>
          <div class="h-2.5 bg-gray-300    w-[240px] mb-2.5"></div>
          <div class="w-[190px] h-2 bg-gray-200  "></div>
        </div>
        <div class="h-2.5 bg-gray-300   w-12"></div>
      </div>
      <div class="flex items-center justify-between pt-4">
        <div>
          <div class="h-2.5 bg-gray-300   w-[260px] mb-2.5"></div>
          <div class="w-32 h-2 bg-gray-200 "></div>
        </div>
        <div class="h-2.5 bg-gray-300  w-12"></div>
      </div>
      <div class="flex items-center justify-between">
        <div>
          <div class="h-2.5 bg-gray-300 w-[260px] mb-2.5"></div>
          <div class="w-[190px] h-2 bg-gray-200  "></div>
        </div>
        <div class="h-2.5 bg-gray-300 w-12"></div>
      </div>
      <div class="flex items-center justify-between pt-4">
        <div>
          <div class="h-2.5 bg-gray-300  w-[230px] mb-2.5"></div>
          <div class="w-[160px] h-2 bg-gray-200  "></div>
        </div>
        <div class="h-2.5 bg-gray-300  w-12"></div>
      </div>
      <div class="flex items-center justify-between pt-4">
        <div>
          <div class="h-2.5 bg-gray-300    w-[250px] mb-2.5"></div>
          <div class="w-[170px] h-2 bg-gray-200  "></div>
        </div>
        <div class="h-2.5 bg-gray-300   w-12"></div>
      </div>
    </div>
  )
}

render(<AppWrapper />, document.getElementById("app"))
