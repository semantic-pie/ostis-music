import { HTMLProps } from "preact/compat"

export const PrevIcon = (props: HTMLProps<SVGSVGElement>) => {
 return (
  <svg fill="none" viewBox="0 0 24 24" {...props}>
  <path
    fill="currentColor"
    fillRule="evenodd"
    d="M8 8a1 1 0 011 1v6a1 1 0 11-2 0V9a1 1 0 011-1zm8 7.464L10 12l6-3.464v6.928z"
    clipRule="evenodd"
  />
  <path
    fill="currentColor"
    fillRule="evenodd"
    d="M19 3H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2zM5 1a4 4 0 00-4 4v14a4 4 0 004 4h14a4 4 0 004-4V5a4 4 0 00-4-4H5z"
    clipRule="evenodd"
  />
</svg>
 )
}

export const NextIcon = (props: HTMLProps<SVGSVGElement>) => {
 return (
  <svg fill="none" viewBox="0 0 24 24" {...props}>
  <path
    fill="currentColor"
    d="M15 9a1 1 0 112 0v6a1 1 0 11-2 0V9zM14 12l-6 3.464V8.536L14 12z"
  />
  <path
    fill="currentColor"
    fillRule="evenodd"
    d="M1 5a4 4 0 014-4h14a4 4 0 014 4v14a4 4 0 01-4 4H5a4 4 0 01-4-4V5zm4-2h14a2 2 0 012 2v14a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2z"
    clipRule="evenodd"
  />
</svg>
 )
}