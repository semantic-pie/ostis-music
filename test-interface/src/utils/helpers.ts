import { TRACKS_PER_PAGE } from "../config";
import { Genre, Track } from "../redux/interfaces";

export const setCover = (trackUrl, ref) => {
  console.log('setCover')
  var jsmediatags = window.jsmediatags;
  console.log('jsmediatags', jsmediatags)
  console.log('trackUrl', trackUrl)
  new jsmediatags.Reader(trackUrl).setTagsToRead(["picture"]).read({
    onSuccess: function (tag) {
      console.log('onSuccess: ', tag)
      var tags = tag.tags;
      var image = tags.picture;

      var base64String = "";
      for (var i = 0; i < image.data.length; i++) {
          base64String += String.fromCharCode(image.data[i]);
      }
      var base64 = "data:image/jpeg;base64," +
              window.btoa(base64String);

      ref.current.src = base64
    },
    onError: function (error) {
      console.log('err: ', error)
    },
  })
  console.log('after')
}

export const getGenre = (tracks: Track[]) => Array.from(new Set(tracks.map(t => t.genre).filter(g => !!g && !!g.name).map(g => JSON.stringify(g)))).map(g => JSON.parse(g) as Genre); 

export function onlyUnique(value, index, array) {
  return array.indexOf(value) === index;
}

export function getLastPage(totalItems: number): number {
  return Math.ceil(totalItems / TRACKS_PER_PAGE)
}

export const isSafari = () => {
  const userAgent = window.navigator.userAgent.toLowerCase();
  return userAgent.indexOf('safari') !== -1 && userAgent.indexOf('chrome') === -1;
};
