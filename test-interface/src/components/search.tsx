import { useEffect } from "preact/hooks"
import { changeQuery, search } from "../redux/slices/trackSlice"
import { useAppDispatch, useAppSelector } from "../redux/hooks"

const Search = () => {
  const dispatch = useAppDispatch()
  const query = useAppSelector((state) => state.tracksSlice.searchQuery)

  useEffect(() => {
    dispatch(search(query))
  }, [query])
  return (
    <div class="boxer flex-col">
      <div class="flex gap-2 justify-between">
        <input
          onChange={(e) => dispatch(changeQuery(e.currentTarget.value))}
          value={query}
          class="w-full"
        />
        <button>Search</button>
      </div>
    </div>
  )
}

export default Search
