import { useAppDispatch, useAppSelector } from "../redux/hooks"
import { toggleLikedTracks, togglePlaylist } from "../redux/slices/trackSlice"
import { fetchPlaylist, generateNewFlowPlaylist } from "../redux/thunks"

const Control = () => {
  const dispatch = useAppDispatch()
  const likedMode = useAppSelector(
    (state) => state.tracksSlice.control.likedList
  )
  const control = useAppSelector(state => state.tracksSlice.control)

  return (
    <div class="boxer flex-col">
      <h4 class="boxer-title min-w-[100px]">
        Controls
      </h4>

      <button class='hidden md:inline' onClick={() => dispatch(generateNewFlowPlaylist())}>
        Generate<br/>playlist
      </button>

      <button class='md:hidden' onClick={() => dispatch(generateNewFlowPlaylist())}>
        Generate playlist
      </button>

      <button class={`hidden md:inline ${control.playlist ? 'bg-gray-200 rounded-sm' : ''}`} onClick={() => dispatch(fetchPlaylist()).then(i=>dispatch(togglePlaylist()))}>Open<br/>playlist</button>
      <button class={`md:hidden ${control.playlist ? 'bg-gray-200 rounded-sm' : ''}`} onClick={() => dispatch(fetchPlaylist()).then(i=>dispatch(togglePlaylist()))}>Open playlist</button>
      
      <button class={`${control.likedList ? 'bg-gray-200 rounded-sm' : ''}`} onClick={() => dispatch(toggleLikedTracks())}>
        Liked
      </button>
    </div>
  )
}

export default Control
