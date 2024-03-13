import { useEffect, useState } from "preact/hooks"
import { HTMLProps } from "preact/compat"
import { useAppDispatch, useAppSelector } from "../redux/hooks"
import { auth, signUp } from "../redux/thunks"
import { logout, tryAuth } from "../redux/slices/userSlice"
import { getGenre } from "../utils/helpers"

const UserAuth = () => {
  const dispatch = useAppDispatch()

  const authData = useAppSelector((state) => state.userSlice.auth)

  const [openLogin, setOpenLogin] = useState(false)
  const [openSignUp, setOpenSignIn] = useState(false)

  const closeLogin = () => setOpenLogin(false)
  const closeSigUp = () => setOpenSignIn(false)

  useEffect(() => {
    dispatch(tryAuth())
  }, [dispatch])

  return (
    <div class="boxer w-full flex-col w-100%">
      {authData.authenticated ? (
        <>
          <Authenticated />
        </>
      ) : (
        <div>
          <div class="flex gap-[5px]">
            {!openSignUp && (
              <Login
                close={closeLogin}
                onClick={() => setOpenLogin(true)}
                isOpen={openLogin}
              />
            )}
            {!openLogin && (
              <SignUp
                close={closeSigUp}
                onClick={() => setOpenSignIn(true)}
                isOpen={openSignUp}
              />
            )}
          </div>
          {!openLogin && !openSignUp && <span>{authData.message}</span>}
        </div>
      )}
    </div>
  )
}

const Authenticated = () => {
  const dispatch = useAppDispatch()
  const auth = useAppSelector((state) => state.userSlice.auth)
  return (
    <div style={{ display: "flex", gap: "5px" }}>
      <div>Hi, {auth.username}</div>
      <button class="flex" onClick={() => dispatch(logout())}>
        Logout
      </button>
    </div>
  )
}

const SignUp = (
  props: HTMLProps<HTMLButtonElement> & { isOpen: boolean; close: () => void }
) => {
  const dispatch = useAppDispatch()

  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")

  const genres = useAppSelector((state) =>
    getGenre(state.tracksSlice.tracks.all)
  )
  const [userGenres, setUserGenres] = useState<string[]>([])

  const toggleGenre = (genre: string) => {
    if (userGenres.includes(genre)) {
      setUserGenres(userGenres.filter((g) => g != genre))
    } else {
      setUserGenres([...userGenres, genre])
    }
  }

  return (
    <>
      {!props.isOpen ? (
        <button class="flex" onClick={props.onClick}>
          SignUp
        </button>
      ) : (
        <div class="w-full flex gap-4 flex-col">
          <div class='flex flex-col md:flex-row gap-3 md:justify-between'>
            <div class="flex md:gap-2 flex-col md:flex-row">
              <div class="flex md:gap-2 flex-col md:flex-row">
                <label htmlFor="name">username</label>
                <input
                  onChange={(e) => setUsername(e.currentTarget.value)}
                  id="name"
                  type="text"
                />
              </div>
              <div class="flex md:gap-2 flex-col md:flex-row">
                <label htmlFor="password">password</label>
                <input
                  onChange={(e) => setPassword(e.currentTarget.value)}
                  id="password"
                  type="password"
                />
              </div>
            </div>

            <button
              onClick={() => {
                dispatch(
                  signUp({
                    username,
                    password,
                    favoriteGenres: [
                      ...userGenres.map((g) => ({ name: g, weight: 5 })),
                    ],
                    userRole: "ROLE_USER",
                  })
                ).then((ignored) => {
                  dispatch(auth({ username, password }))
                  setPassword("")
                  setUsername("")
                  setUserGenres([])
                  props.close()
                })
              }}
            >
              SignUp
            </button>
          </div>


          <div>
            <div class="flex flex-wrap gap-x-3 gap-y-[10px] mt-[10px] ">
              {genres.map((g) => (
                <span
                  class={`px-[10px] border-gray-800 transition-all duration-300 ${ 
                    userGenres.includes(g.idtf)
                      ? "text-white bg-gray-400 cursor-pointer"
                      : " border-solid border-[1px] cursor-pointer "
                  }`}
                  onClick={() => toggleGenre(g.idtf)}
                >
                  {g.name}
                </span>
              ))}
            </div>
          </div>
        </div>
      )}
    </>
  )
}

const Login = (
  props: HTMLProps<HTMLButtonElement> & { isOpen: boolean; close: () => void }
) => {
  const dispatch = useAppDispatch()

  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")

  return (
    <>
      {!props.isOpen ? (
        <button class="flex" onClick={props.onClick}>
          Login
        </button>
      ) : (
        <div class="flex w-full gap-4 md:gap-0 md:justify-between flex-col md:flex-row">
          <div class="flex flex-col md:flex-row gap-3 md:gap-5">
            <div class="flex md:gap-2 flex-col md:flex-row">
              <label htmlFor="name">username:</label>
              <input
                class="flex"
                onChange={(e) => setUsername(e.currentTarget.value)}
                id="name"
                type="text"
              />
            </div>

            <div class="flex md:gap-2 flex-col md:flex-row">
              <label htmlFor="password">password:</label>
              <input
                class="flex"
                onChange={(e) => setPassword(e.currentTarget.value)}
                id="password"
                type="password"
              />
            </div>
          </div>

          <button
            onClick={() => {
              dispatch(auth({ username, password })), props.close()
            }}
          >
            Login
          </button>
        </div>
      )}
    </>
  )
}

export default UserAuth
