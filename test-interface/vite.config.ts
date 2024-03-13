import { defineConfig } from 'vite';
import preact from '@preact/preset-vite';

import { viteSingleFile } from 'vite-plugin-singlefile';
import { reserved } from './reserved';
// https://vitejs.dev/config/
export default defineConfig({
	build: {
		minify: 'terser',
    terserOptions: {
      // keep_classnames: true,
      keep_fnames: true,
      mangle: {
        // Пример аннотации, указывающей Terser сохранить имя функции "sc"
        properties: {
          keep_quoted: true,
          reserved: reserved,
										regex: /^sc/
        }
      }
    }
	},
	server: {
		watch: {
		  usePolling: true,
		},
		host: true, // needed for the Docker Container port mapping to work
		strictPort: true,
		port: 3000,
	},
	plugins: [
		preact(),
		viteSingleFile(),
	],
});

//<script type="module" crossorigin src="/assets/index-3f277a05.js"></script>
//<link rel="stylesheet" href="/assets/index-e7275a14.css">
