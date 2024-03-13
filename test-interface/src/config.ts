
const host =  import.meta.env.VITE_HOST_ADDRESS ?? '0.0.0.0'
export const TRACKS_PER_PAGE = 10
export const DOWNLOAD_TRACK_URL = `http://${host}:8080/api/v1/loafloader` // id

export const HOST = `http://${host}:8080`
