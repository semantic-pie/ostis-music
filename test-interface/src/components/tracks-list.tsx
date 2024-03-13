import { FunctionComponent } from "preact"
import { HTMLProps, useEffect } from "preact/compat"
import { useAppDispatch, useAppSelector, useKeyDown } from "../redux/hooks"
import { Track } from "../redux/interfaces"
import { TRACKS_PER_PAGE } from "../config"
import { nextTrack, prevTrack } from "../redux/slices/trackSlice"

type TracksListProps = {
  tracks: Track[]
  setCurrentTrack: (t: Track) => void
  currentPage: number
  maxPage: number
  changePage: (page: number) => void
}

const TracksList = (props: TracksListProps) => {
  const control = useAppSelector((state) => state.tracksSlice.control)
  const currentTrack = useAppSelector(
    (state) => state.tracksSlice.current.track
  )

  let allTracks = [...props.tracks].sort((a, b) =>
    a.title.localeCompare(b.title)
  )
  let tracks: Track[] = []

  if (props.currentPage === 1) {
    tracks = allTracks.slice(0, TRACKS_PER_PAGE)
  } else {
    tracks = allTracks.slice(
      props.currentPage * TRACKS_PER_PAGE - TRACKS_PER_PAGE,
      props.currentPage * TRACKS_PER_PAGE
    )
  }

  const dispatch = useAppDispatch()

  useKeyDown(() => {
    dispatch(prevTrack())
  }, ['ArrowUp'])

  useKeyDown(() => {
    dispatch(nextTrack())
  }, ['ArrowDown'])

  useKeyDown(() => {
    const next = (props.currentPage + 1)

    props.changePage(next)
    
    if (next > 0 && next < allTracks.length)
      props.setCurrentTrack(allTracks[props.currentPage * TRACKS_PER_PAGE ])
  }, ['ArrowRight'])

  useKeyDown(() => {
    const prev = (props.currentPage - 1)
    
    props.changePage(prev)

    if (prev > 0 && prev < allTracks.length)
      props.setCurrentTrack(allTracks[props.currentPage * TRACKS_PER_PAGE - TRACKS_PER_PAGE * 2])
  }, ['ArrowLeft'])

  return (
    <div class="boxer flex-col md:w-full">
      <h4 class="boxer-title">
        {control.likedList
          ? "Liked tracks:"
          : control.playlist
          ? "Playlist"
          : "Tracks list:"}
      </h4>
      <div class="flex flex-col gap-[2px] px-[3px]">
        {tracks.map((t) => (
          <TrackCard
            title={t.title}
            author={t.author}
            liked={t.liked}
            currentPlay={currentTrack?.hash !== t.hash}
            onClick={() => props.setCurrentTrack(t)}
          />
        ))}
      </div>

      <div style={{ display: "flex", justifyContent: "center", gap: "7px" }}>
        {props.currentPage - 1 > 0 && (
          <div
            class="pagination-button34"
            onClick={() => props.changePage(props.currentPage - 1)}
          >
            -
          </div>
        )}
        {props.currentPage - 2 > 0 && (
          <div
            class="pagination-button34"
            onClick={() => props.changePage(props.currentPage - 2)}
          >
            {props.currentPage - 2}
          </div>
        )}
        {props.currentPage - 1 > 0 && (
          <div
            class="pagination-button34"
            onClick={() => props.changePage(props.currentPage - 1)}
          >
            {props.currentPage - 1}
          </div>
        )}
        <div class="pagination-button34 text-gray-400" disabled>
          {props.currentPage}
        </div>
        {props.maxPage - props.currentPage > 0 && (
          <div
            class="pagination-button34"
            onClick={() => props.changePage(props.currentPage + 1)}
          >
            {props.currentPage + 1}
          </div>
        )}
        {props.maxPage - props.currentPage > 1 && (
          <div
            class="pagination-button34"
            onClick={() => props.changePage(props.currentPage + 2)}
          >
            {props.currentPage + 2}
          </div>
        )}
        {props.maxPage - props.currentPage > 0 && (
          <div
            class="pagination-button34"
            onClick={() => props.changePage(props.currentPage + 1)}
          >
            +
          </div>
        )}
      </div>
    </div>
  )
}

type TrackProps = {
  title: string
  author: string
  liked: boolean
  currentPlay: boolean
}
const TrackCard: FunctionComponent<HTMLProps<HTMLDivElement> & TrackProps> = ({
  title,
  author,
  ...props
}) => (
  <div
  class={`flex flex-row gap-1 justify-between p-[2px] cursor-pointer border border-1 border-black ${props.currentPlay ? "" : "border-solid border-1"}`}
    onClick={props.onClick}
  >
    <div
    class='flex flex-col'
      style={{
        display: "flex",
        flexDirection: "column",
      }}
    >
      <span>{title}</span>
      <span class='text-gray-400'>{author}</span>
    </div>
    {props.liked && (
      <svg
        class='my-auto'
        viewBox="0 0 1024 1024"
        fill="currentColor"
        height="1em"
        width="1em"
      >
        <path d="M923 283.6a260.04 260.04 0 00-56.9-82.8 264.4 264.4 0 00-84-55.5A265.34 265.34 0 00679.7 125c-49.3 0-97.4 13.5-139.2 39-10 6.1-19.5 12.8-28.5 20.1-9-7.3-18.5-14-28.5-20.1-41.8-25.5-89.9-39-139.2-39-35.5 0-69.9 6.8-102.4 20.3-31.4 13-59.7 31.7-84 55.5a258.44 258.44 0 00-56.9 82.8c-13.9 32.3-21 66.6-21 101.9 0 33.3 6.8 68 20.3 103.3 11.3 29.5 27.5 60.1 48.2 91 32.8 48.9 77.9 99.9 133.9 151.6 92.8 85.7 184.7 144.9 188.6 147.3l23.7 15.2c10.5 6.7 24 6.7 34.5 0l23.7-15.2c3.9-2.5 95.7-61.6 188.6-147.3 56-51.7 101.1-102.7 133.9-151.6 20.7-30.9 37-61.5 48.2-91 13.5-35.3 20.3-70 20.3-103.3.1-35.3-7-69.6-20.9-101.9z" />
      </svg>
    )}
  </div>
)
export default TracksList
