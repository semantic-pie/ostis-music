import { HOST } from "../config";
import { Track } from "../redux/interfaces";

export const fetchTracks = async (page: number, limit: number): Promise<Track[]> => {
return fetch(`http://${HOST}/api/v1/derezhor/tracks?page=${page}&limit=${limit}`)
.then(data => data.json())
}
 